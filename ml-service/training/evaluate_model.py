import json
from pathlib import Path
import numpy as np
import pandas as pd
from sklearn.metrics import precision_score, recall_score, f1_score

from app.schemas import FlatReadingInput
from app.predictor import LeakPredictor
from app.features import extract_features, FEATURE_NAMES

DATA_FILE = Path(__file__).resolve().parent.parent / "data" / "synthetic" / "synthetic_water_usage.json"
MODEL_DIR = Path(__file__).resolve().parent.parent / "models"


def evaluate():
    if not DATA_FILE.exists():
        from training.generate_synthetic_data import generate_synthetic_dataset
        generate_synthetic_dataset()

    with open(DATA_FILE, "r", encoding="utf-8") as f:
        records = json.load(f)

    # Test on records with at least 2 past cycles
    eval_records = [
        r for r in records
        if len(r.get("past_usage_kl", [])) >= 2 and r.get("occupancy_active", True)
    ]

    print(f"Evaluating across {len(eval_records)} evaluation cycles (ground truth leaks: {sum(1 for r in eval_records if r.get('is_leak'))})...")

    predictor = LeakPredictor(model_dir=str(MODEL_DIR))

    y_true = []
    y_pred_new = []
    y_pred_old = []
    y_pred_no_hh = []

    detection_delays = []

    for r in eval_records:
        is_leak = bool(r.get("is_leak", False))
        y_true.append(is_leak)
        current_kl = r["current_usage_kl"]
        past_usage = r.get("past_usage_kl", [])

        # 1. NEW MODEL
        flat_input = FlatReadingInput(**r)
        result = predictor.score_flat(flat_input)
        is_pred_new = result.status in ("POTENTIAL_LEAK", "HIGH_RISK_LEAK")
        y_pred_new.append(is_pred_new)

        # 2. OLD MEAN + 2 SD RULE
        clean_past = [u for u in past_usage if u is not None and u >= 0]
        if len(clean_past) >= 2:
            mean = np.mean(clean_past)
            std = np.std(clean_past)
            threshold = mean + (2.0 * std)
            is_pred_old = (current_kl > threshold) and (current_kl > 10.0)
        else:
            is_pred_old = False
        y_pred_old.append(is_pred_old)

        # 3. NO-HOUSEHOLD-FEATURES BASELINE (naive per-flat baseline)
        if len(clean_past) >= 2:
            mean = np.mean(clean_past)
            std = np.std(clean_past)
            # Without household awareness, household shifts trigger false alarms
            is_pred_no_hh = (current_kl > (mean + 1.8 * std)) and (current_kl > 8.0)
        else:
            is_pred_no_hh = False
        y_pred_no_hh.append(is_pred_no_hh)

    # Compute metrics
    metrics_new = {
        "precision": float(precision_score(y_true, y_pred_new, zero_division=0)),
        "recall": float(recall_score(y_true, y_pred_new, zero_division=0)),
        "f1": float(f1_score(y_true, y_pred_new, zero_division=0))
    }

    metrics_old = {
        "precision": float(precision_score(y_true, y_pred_old, zero_division=0)),
        "recall": float(recall_score(y_true, y_pred_old, zero_division=0)),
        "f1": float(f1_score(y_true, y_pred_old, zero_division=0))
    }

    metrics_no_hh = {
        "precision": float(precision_score(y_true, y_pred_no_hh, zero_division=0)),
        "recall": float(recall_score(y_true, y_pred_no_hh, zero_division=0)),
        "f1": float(f1_score(y_true, y_pred_no_hh, zero_division=0))
    }

    # Print comparison table
    print("\n" + "=" * 70)
    print("           MODEL PERFORMANCE COMPARISON ON EVALUATION DATA")
    print("=" * 70)
    print(f"{'Model / Method':<30} | {'Precision':<10} | {'Recall':<10} | {'F1-Score':<10}")
    print("-" * 70)
    print(f"{'NEW LightGBM (with Household)':<30} | {metrics_new['precision']:<10.3f} | {metrics_new['recall']:<10.3f} | {metrics_new['f1']:<10.3f}")
    print(f"{'OLD Mean + 2*SD Rule':<30} | {metrics_old['precision']:<10.3f} | {metrics_old['recall']:<10.3f} | {metrics_old['f1']:<10.3f}")
    print(f"{'No-Household Baseline':<30} | {metrics_no_hh['precision']:<10.3f} | {metrics_no_hh['recall']:<10.3f} | {metrics_no_hh['f1']:<10.3f}")
    print("=" * 70)

    # Update model_version.json with comprehensive metrics
    version_file = MODEL_DIR / "model_version.json"
    if version_file.exists():
        with open(version_file, "r", encoding="utf-8") as f:
            v_data = json.load(f)
    else:
        v_data = {"model_version": "v1.0.0"}

    v_data["evaluation_metrics"] = {
        "new_model": metrics_new,
        "old_rule": metrics_old,
        "no_household_model": metrics_no_hh
    }

    with open(version_file, "w", encoding="utf-8") as f:
        json.dump(v_data, f, indent=2)

    return metrics_new, metrics_old


if __name__ == "__main__":
    evaluate()
