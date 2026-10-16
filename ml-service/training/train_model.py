import json
import os
from datetime import datetime
from pathlib import Path
import numpy as np
import pandas as pd
import lightgbm as lgb
import joblib

from app.schemas import FlatReadingInput
from app.features import extract_features, FEATURE_NAMES

DATA_FILE = Path(__file__).resolve().parent.parent / "data" / "synthetic" / "synthetic_water_usage.json"
MODEL_DIR = Path(__file__).resolve().parent.parent / "models"
MODEL_DIR.mkdir(parents=True, exist_ok=True)


def train():
    if not DATA_FILE.exists():
        from training.generate_synthetic_data import generate_synthetic_dataset
        generate_synthetic_dataset()

    print(f"Loading synthetic dataset from {DATA_FILE}...")
    with open(DATA_FILE, "r", encoding="utf-8") as f:
        records = json.load(f)

    # Train only on normal (non-leak) cycles with valid history (>= 2 past cycles)
    train_records = [
        r for r in records
        if not r.get("is_leak", False)
        and r.get("occupancy_active", True)
        and len(r.get("past_usage_kl", [])) >= 2
    ]

    print(f"Total records: {len(records)}, Training records (normal baseline): {len(train_records)}")

    X_list = []
    y_list = []

    for r in train_records:
        flat_input = FlatReadingInput(**r)
        vec, meta = extract_features(flat_input)
        X_list.append(vec)
        # Target is daily normal consumption
        y_list.append(meta["daily_usage_kl"])

    X = np.array(X_list, dtype=np.float64)
    y = np.array(y_list, dtype=np.float64)

    print(f"Feature matrix shape: {X.shape}, Target shape: {y.shape}")

    quantiles = [0.10, 0.50, 0.95]
    models = {}

    for q in quantiles:
        print(f"Training LightGBM Quantile Regressor for alpha={q}...")
        model = lgb.LGBMRegressor(
            objective="quantile",
            alpha=q,
            n_estimators=120,
            learning_rate=0.05,
            max_depth=4,
            num_leaves=15,
            min_child_samples=5,
            random_state=42,
            verbose=-1
        )
        model.fit(X, y)
        models[f"q{int(q*100)}"] = model

    # Save trained models
    joblib.dump(models["q10"], MODEL_DIR / "model_q10.joblib")
    joblib.dump(models["q50"], MODEL_DIR / "model_q50.joblib")
    joblib.dump(models["q95"], MODEL_DIR / "model_q95.joblib")

    # Compute baseline pinball losses on train
    losses = {}
    for q_name, q_val in [("q10", 0.10), ("q50", 0.50), ("q95", 0.95)]:
        preds = models[q_name].predict(X)
        errors = y - preds
        loss = np.mean(np.maximum(q_val * errors, (q_val - 1.0) * errors))
        losses[f"pinball_loss_{q_name}"] = round(float(loss), 4)

    version_info = {
        "model_version": "v1.0.0",
        "training_date": datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ"),
        "training_samples": len(X),
        "features": FEATURE_NAMES,
        "metrics": losses
    }

    with open(MODEL_DIR / "model_version.json", "w", encoding="utf-8") as f:
        json.dump(version_info, f, indent=2)

    print(f"Model artifacts successfully saved to {MODEL_DIR} (Version: v1.0.0)")
    return version_info


if __name__ == "__main__":
    train()
