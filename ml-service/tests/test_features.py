import numpy as np
from app.schemas import FlatReadingInput
from app.features import (
    extract_features,
    extract_clean_history,
    compute_trend_slope,
    FEATURE_NAMES
)


def test_feature_vector_dimension():
    flat = FlatReadingInput(
        usage_id=1,
        apartment_id=101,
        apartment_number="101",
        unit_type="2BHK",
        floor_number=1,
        month=5,
        year=2026,
        days_in_cycle=31,
        current_usage_kl=15.0,
        past_usage_kl=[14.0, 15.5, 13.8],
        past_days_in_cycle=[30, 31, 30],
        past_household_size_avg=[3.0, 3.0, 3.0],
        past_incident_flags=[False, False, False],
        household_size_avg_in_cycle=3.0,
        household_size_at_cycle_end=3,
        household_changed_in_cycle=False,
        household_size_confirmed=True,
        away_days_in_cycle=0,
        occupancy_active=True,
        recent_move_in=False,
        meter_status="ACTIVE",
        meter_recently_replaced=False,
        peer_avg_daily_kl=0.48,
        peer_count=4,
        building_avg_daily_kl=0.50,
        building_avg_daily_kl_per_person=0.16
    )

    vec, meta = extract_features(flat)
    assert len(vec) == len(FEATURE_NAMES)
    assert meta["history_count"] == 3
    assert np.isclose(meta["days_in_cycle"], 31)


def test_clean_history_incident_exclusion():
    flat = FlatReadingInput(
        usage_id=2,
        apartment_id=102,
        apartment_number="102",
        month=6,
        year=2026,
        days_in_cycle=30,
        current_usage_kl=14.0,
        past_usage_kl=[35.0, 14.0, 13.5],  # 35.0 was an open confirmed leak cycle
        past_days_in_cycle=[30, 30, 30],
        past_incident_flags=[True, False, False]
    )

    clean_history = extract_clean_history(flat)
    # The 35.0 kL leak cycle must be omitted from baseline calculations
    assert len(clean_history) == 2
    assert np.isclose(clean_history[0], 14.0 / 30.0)
    assert np.isclose(clean_history[1], 13.5 / 30.0)


def test_compute_trend_slope():
    rising = [0.60, 0.50, 0.40, 0.30]  # Newest to oldest (0.30 -> 0.40 -> 0.50 -> 0.60)
    slope = compute_trend_slope(rising)
    assert slope > 0.05

    flat_history = [0.50, 0.50, 0.50]
    slope_flat = compute_trend_slope(flat_history)
    assert np.isclose(slope_flat, 0.0, atol=1e-3)
