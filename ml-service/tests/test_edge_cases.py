import pytest
from app.schemas import FlatReadingInput
from app.predictor import LeakPredictor

predictor = LeakPredictor(model_dir="models")


def test_edge_case_common_or_unassigned_meter():
    """6.1: Meters with apartment_id NULL must be marked NOT_APPLICABLE."""
    flat = FlatReadingInput(
        usage_id=501,
        apartment_id=None,  # Common area meter
        current_usage_kl=55.0,
        month=6,
        year=2026,
        days_in_cycle=30
    )
    result = predictor.score_flat(flat)
    assert result.status == "NOT_APPLICABLE"
    assert result.risk_score == 0.0
    assert "common-area or unassigned meter" in result.reason.lower()


def test_edge_case_meter_inactive():
    """6.1: Meter status INACTIVE: skip scoring."""
    flat = FlatReadingInput(
        usage_id=502,
        apartment_id=101,
        apartment_number="101",
        meter_status="INACTIVE",
        current_usage_kl=0.0,
        month=6,
        year=2026,
        days_in_cycle=30
    )
    result = predictor.score_flat(flat)
    assert result.status == "NOT_APPLICABLE"
    assert "inactive" in result.reason.lower()


def test_edge_case_meter_faulty():
    """6.1: Meter status FAULTY: score but cap confidence at LOW and mention faulty meter."""
    flat = FlatReadingInput(
        usage_id=503,
        apartment_id=102,
        apartment_number="102",
        meter_status="FAULTY",
        current_usage_kl=35.0,
        past_usage_kl=[14.0, 14.5, 13.8],
        month=6,
        year=2026,
        days_in_cycle=30
    )
    result = predictor.score_flat(flat)
    assert result.confidence == "LOW"
    assert "faulty" in result.reason.lower()


def test_edge_case_meter_recently_replaced():
    """6.1: Replaced meter in cycle: do not compare across replacement; treat as new baseline (confidence LOW, no HIGH_RISK)."""
    flat = FlatReadingInput(
        usage_id=504,
        apartment_id=103,
        apartment_number="103",
        meter_recently_replaced=True,
        current_usage_kl=28.0,
        past_usage_kl=[14.0, 14.0, 14.0],
        month=6,
        year=2026,
        days_in_cycle=30
    )
    result = predictor.score_flat(flat)
    assert result.confidence == "LOW"
    assert result.status != "HIGH_RISK_LEAK"
    assert "replaced" in result.reason.lower()


def test_edge_case_negative_consumption():
    """6.1: Negative usage: skip scoring, mark DATA_SUSPECT."""
    flat = FlatReadingInput(
        usage_id=505,
        apartment_id=104,
        current_usage_kl=-5.0,
        month=6,
        year=2026,
        days_in_cycle=30
    )
    result = predictor.score_flat(flat)
    assert result.status == "DATA_SUSPECT"
    assert result.data_quality_flag == "NEGATIVE_CONSUMPTION"


def test_edge_case_extreme_outlier_digit_typo():
    """6.2: Extreme outlier (> 3,000 L/person/day): mark DATA_SUSPECT."""
    flat = FlatReadingInput(
        usage_id=506,
        apartment_id=105,
        apartment_number="105",
        household_size_avg_in_cycle=2.0,
        current_usage_kl=250.0,  # 250 kL in 30 days = 4,166 L/person/day (digit typo like 250 instead of 25)
        month=6,
        year=2026,
        days_in_cycle=30
    )
    result = predictor.score_flat(flat)
    assert result.status == "DATA_SUSPECT"
    assert result.data_quality_flag == "EXTREME_OUTLIER"
    assert "verify" in result.reason.lower() or "typo" in result.reason.lower()


def test_edge_case_vacant_flat_with_usage():
    """6.3: Vacant flat with usage above threshold: pattern VACANT_USAGE, strong suspicion."""
    flat = FlatReadingInput(
        usage_id=507,
        apartment_id=106,
        apartment_number="106",
        occupancy_active=False,
        household_size_avg_in_cycle=0.0,
        current_usage_kl=12.0,  # 12 kL in a vacant flat
        month=6,
        year=2026,
        days_in_cycle=30
    )
    result = predictor.score_flat(flat)
    assert result.status in ("POTENTIAL_LEAK", "HIGH_RISK_LEAK")
    assert result.pattern == "VACANT_USAGE"
    assert "vacant" in result.reason.lower()


def test_edge_case_vacant_flat_near_zero_usage():
    """6.3: Vacant flat with near-zero usage: NORMAL."""
    flat = FlatReadingInput(
        usage_id=508,
        apartment_id=107,
        apartment_number="107",
        occupancy_active=False,
        household_size_avg_in_cycle=0.0,
        current_usage_kl=0.02,
        month=6,
        year=2026,
        days_in_cycle=30
    )
    result = predictor.score_flat(flat)
    assert result.status == "NORMAL"
    assert result.pattern == "NONE"


def test_edge_case_cold_start_thin_history():
    """6.2: Fewer than 3 past cycles: peer-group range, confidence LOW, never HIGH_RISK."""
    flat = FlatReadingInput(
        usage_id=509,
        apartment_id=108,
        apartment_number="108",
        unit_type="2BHK",
        current_usage_kl=28.0,
        past_usage_kl=[14.0],  # only 1 past cycle
        past_days_in_cycle=[30],
        peer_avg_daily_kl=0.50,
        peer_count=5,
        month=6,
        year=2026,
        days_in_cycle=30
    )
    result = predictor.score_flat(flat)
    assert result.confidence == "LOW"
    assert result.status != "HIGH_RISK_LEAK"


def test_edge_case_sharp_unexplained_drop_zero_usage():
    """6.2: Zero or sharp drop in occupied, non-away flat: LOW_USAGE_SUSPECT."""
    flat = FlatReadingInput(
        usage_id=510,
        apartment_id=109,
        apartment_number="109",
        unit_type="2BHK",
        occupancy_active=True,
        household_size_avg_in_cycle=3.0,
        away_days_in_cycle=0,
        current_usage_kl=0.0,  # 0 usage in occupied flat
        past_usage_kl=[16.0, 15.5, 16.2, 15.8],
        past_days_in_cycle=[30, 30, 30, 30],
        month=6,
        year=2026,
        days_in_cycle=30
    )
    result = predictor.score_flat(flat)
    assert result.status == "LOW_USAGE_SUSPECT"
    assert "jam" in result.reason.lower() or "bypass" in result.reason.lower() or "low" in result.reason.lower()


def test_edge_case_away_period_with_high_usage():
    """6.3: Declared away for 20 days but water usage remains elevated."""
    flat = FlatReadingInput(
        usage_id=511,
        apartment_id=110,
        apartment_number="110",
        unit_type="2BHK",
        occupancy_active=True,
        household_size_avg_in_cycle=3.0,
        away_days_in_cycle=20,
        current_usage_kl=26.0,  # Away most of the month, yet 26 kL consumed
        past_usage_kl=[15.0, 14.8, 15.2],
        past_days_in_cycle=[30, 30, 30],
        month=6,
        year=2026,
        days_in_cycle=30
    )
    result = predictor.score_flat(flat)
    assert result.status in ("POTENTIAL_LEAK", "HIGH_RISK_LEAK", "WATCH")
    assert "away" in result.reason.lower()
