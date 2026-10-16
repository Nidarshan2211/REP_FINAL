import math
from typing import Dict, Any, List, Tuple
import numpy as np
from app.schemas import FlatReadingInput

UNIT_TYPE_MAP = {
    "STUDIO": 0,
    "1BHK": 1,
    "2BHK": 2,
    "3BHK": 3,
    "PENTHOUSE": 4
}

FEATURE_NAMES = [
    "history_count",
    "lag_1_daily",
    "lag_2_daily",
    "lag_3_daily",
    "rolling_mean_daily",
    "rolling_median_daily",
    "rolling_std_daily",
    "trend_slope",
    "unit_type_code",
    "floor_number",
    "month_sin",
    "month_cos",
    "household_size",
    "household_changed",
    "household_confirmed",
    "away_ratio",
    "occupied_ratio",
    "recent_move_in",
    "meter_replaced",
    "peer_avg_daily_kl",
    "building_avg_daily_kl_per_person"
]


def extract_clean_history(flat: FlatReadingInput) -> List[float]:
    """
    Extracts daily usage from past cycles, filtering out confirmed incident/leak cycles.
    """
    clean_daily = []
    n_past = len(flat.past_usage_kl)
    for i in range(n_past):
        # Check if cycle was flagged as an open leak or confirmed incident
        if i < len(flat.past_incident_flags) and flat.past_incident_flags[i]:
            continue

        usage = flat.past_usage_kl[i]
        days = flat.past_days_in_cycle[i] if i < len(flat.past_days_in_cycle) else 30
        days = max(1, days)
        if usage is not None and usage >= 0:
            clean_daily.append(usage / float(days))
    return clean_daily


def compute_trend_slope(daily_history: List[float]) -> float:
    """
    Computes slope across up to 6 most recent past cycles (ordered newest to oldest).
    """
    if len(daily_history) < 3:
        return 0.0
    recent = daily_history[:6][::-1]  # Oldest to newest
    x = np.arange(len(recent))
    y = np.array(recent)
    try:
        slope, _ = np.polyfit(x, y, 1)
        return float(slope)
    except Exception:
        return 0.0


def extract_features(flat: FlatReadingInput) -> Tuple[np.ndarray, Dict[str, Any]]:
    """
    Converts a flat reading into a feature vector and metadata dictionary.
    """
    days_in_cycle = max(1, flat.days_in_cycle)
    daily_usage_kl = flat.current_usage_kl / float(days_in_cycle)

    clean_history = extract_clean_history(flat)
    history_count = len(clean_history)

    lag_1 = clean_history[0] if history_count >= 1 else np.nan
    lag_2 = clean_history[1] if history_count >= 2 else np.nan
    lag_3 = clean_history[2] if history_count >= 3 else np.nan

    rolling_window = clean_history[:6]
    rolling_mean = float(np.mean(rolling_window)) if rolling_window else np.nan
    rolling_median = float(np.median(rolling_window)) if rolling_window else np.nan
    rolling_std = float(np.std(rolling_window)) if len(rolling_window) >= 2 else 0.0
    trend_slope = compute_trend_slope(clean_history)

    # Unit type encoding
    unit_str = (flat.unit_type or "2BHK").upper()
    unit_code = UNIT_TYPE_MAP.get(unit_str, 2)
    floor = flat.floor_number or 1

    # Seasonality
    m = flat.month if (flat.month and 1 <= flat.month <= 12) else 6
    month_sin = math.sin(2.0 * math.pi * m / 12.0)
    month_cos = math.cos(2.0 * math.pi * m / 12.0)

    # Household & occupancy
    hh_size = float(flat.household_size_avg_in_cycle if flat.household_size_avg_in_cycle and flat.household_size_avg_in_cycle > 0 else 1.0)
    hh_changed = 1.0 if flat.household_changed_in_cycle else 0.0
    hh_confirmed = 1.0 if flat.household_size_confirmed else 0.0

    away_days = flat.away_days_in_cycle or 0
    away_ratio = min(1.0, max(0.0, away_days / float(days_in_cycle)))
    occupied_days = flat.occupied_days_in_cycle if flat.occupied_days_in_cycle is not None else (days_in_cycle - away_days)
    occupied_ratio = min(1.0, max(0.0, occupied_days / float(days_in_cycle)))

    recent_move_in = 1.0 if flat.recent_move_in else 0.0
    meter_replaced = 1.0 if flat.meter_recently_replaced else 0.0

    # Peer and building aggregates
    peer_avg = flat.peer_avg_daily_kl if flat.peer_avg_daily_kl is not None else np.nan
    bldg_per_person = flat.building_avg_daily_kl_per_person if flat.building_avg_daily_kl_per_person is not None else np.nan

    vector = np.array([
        float(history_count),
        lag_1,
        lag_2,
        lag_3,
        rolling_mean,
        rolling_median,
        rolling_std,
        trend_slope,
        float(unit_code),
        float(floor),
        month_sin,
        month_cos,
        hh_size,
        hh_changed,
        hh_confirmed,
        away_ratio,
        occupied_ratio,
        recent_move_in,
        meter_replaced,
        peer_avg,
        bldg_per_person
    ], dtype=np.float64)

    meta = {
        "daily_usage_kl": daily_usage_kl,
        "clean_history": clean_history,
        "history_count": history_count,
        "hh_size": hh_size,
        "days_in_cycle": days_in_cycle,
        "rolling_median": rolling_median,
        "rolling_mean": rolling_mean,
        "rolling_std": rolling_std,
        "trend_slope": trend_slope
    }

    return vector, meta
