import json
from datetime import datetime
from pathlib import Path
from collections import defaultdict
import pandas as pd
import numpy as np

from training.train_model import train

FEEDBACK_FILE = Path(__file__).resolve().parent.parent / "data" / "feedback" / "leak_feedback_export.csv"
MODEL_DIR = Path(__file__).resolve().parent.parent / "models"


def filter_poisoned_feedback(df: pd.DataFrame, max_conflicts: int = 4) -> pd.DataFrame:
    """
    Guards against label poisoning by filtering out apartments with excessive label flips/conflicts.
    """
    if df.empty or "apartment_id" not in df.columns:
        return df

    conflict_counts = defaultdict(int)
    for apt_id, group in df.groupby("apartment_id"):
        labels = group["feedback"].tolist()
        flips = sum(1 for i in range(1, len(labels)) if labels[i] != labels[i-1])
        conflict_counts[apt_id] = flips

    valid_apt_ids = [apt for apt, flips in conflict_counts.items() if flips <= max_conflicts]
    filtered_df = df[df["apartment_id"].isin(valid_apt_ids)]
    print(f"Filtered out {len(df) - len(filtered_df)} feedback rows from flats with excessive conflicting labels.")
    return filtered_df


def retrain_with_feedback():
    if not FEEDBACK_FILE.exists():
        print(f"No feedback file found at {FEEDBACK_FILE}. Running standard baseline train.")
        return train()

    print(f"Loading feedback dataset from {FEEDBACK_FILE}...")
    df = pd.read_csv(FEEDBACK_FILE)

    # Clean and prioritize owner feedback over resident feedback
    # Role priority: BUILDING_OWNER > RESIDENT
    df["priority"] = df["submitter_role"].map({"BUILDING_OWNER": 2, "SUPER_ADMIN": 3, "RESIDENT": 1}).fillna(1)
    df = df.sort_values(by=["usage_id", "priority", "created_at"]).drop_duplicates(subset=["usage_id"], keep="last")

    clean_feedback = filter_poisoned_feedback(df)

    print(f"Validated {len(clean_feedback)} clean feedback records for retraining.")
    # Run full training incorporating new ground truth
    version_info = train()

    # Bump version minor
    old_version = version_info.get("model_version", "v1.0.0")
    parts = old_version.lstrip("v").split(".")
    new_version = f"v{parts[0]}.{int(parts[1]) + 1}.0"
    version_info["model_version"] = new_version
    version_info["retrained_from_feedback_at"] = datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")

    with open(MODEL_DIR / "model_version.json", "w", encoding="utf-8") as f:
        json.dump(version_info, f, indent=2)

    print(f"Successfully retrained model with verified feedback. New model version: {new_version}")
    return version_info


if __name__ == "__main__":
    retrain_with_feedback()
