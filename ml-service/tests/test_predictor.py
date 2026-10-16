import pytest
from app.schemas import FlatReadingInput
from app.predictor import LeakPredictor


def test_predictor_fallback_scoring():
    predictor = LeakPredictor(model_dir="non_existent_dir")
    assert not predictor.is_model_ready

    # Flat with 3 historical cycles: should score using Mean + 2*SD fallback
    flat = FlatReadingInput(
        usage_id=1,
        apartment_id=101,
        apartment_number="101",
        unit_type="2BHK",
        month=5,
        year=2026,
        days_in_cycle=30,
        current_usage_kl=14.0,
        past_usage_kl=[13.5, 14.2, 13.8],
        past_days_in_cycle=[30, 30, 30]
    )

    result = predictor.score_flat(flat)
    assert result.status == "NORMAL"
    assert result.risk_score <= 0.50
    assert result.expected_kl is not None
    assert "within normal historical baseline" in result.reason.lower()


def test_predictor_detects_sudden_spike_fallback():
    predictor = LeakPredictor(model_dir="non_existent_dir")

    # Normal is ~14 kL, current spikes to 36 kL
    flat = FlatReadingInput(
        usage_id=2,
        apartment_id=102,
        apartment_number="102",
        unit_type="2BHK",
        month=6,
        year=2026,
        days_in_cycle=30,
        current_usage_kl=36.0,
        past_usage_kl=[14.0, 13.5, 14.2],
        past_days_in_cycle=[30, 30, 30]
    )

    result = predictor.score_flat(flat)
    assert result.status == "POTENTIAL_LEAK"
    assert result.risk_score >= 0.80
    assert result.pattern == "SUDDEN_SPIKE"
    assert "significantly above past patterns" in result.reason.lower() or "check" in result.reason.lower()


def test_risk_score_bounds():
    predictor = LeakPredictor(model_dir="non_existent_dir")

    # Score below median
    r_low = predictor.calculate_risk_score(0.2, 0.25, 0.5, 0.9)
    assert 0.0 <= r_low <= 0.35

    # Score between median and upper bound
    r_mid = predictor.calculate_risk_score(0.7, 0.25, 0.5, 0.9)
    assert 0.35 <= r_mid <= 0.75

    # Score above upper bound
    r_high = predictor.calculate_risk_score(1.5, 0.25, 0.5, 0.9)
    assert 0.75 <= r_high <= 1.0
