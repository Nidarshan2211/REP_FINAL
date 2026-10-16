from typing import Tuple, Optional
import numpy as np
from app.schemas import FlatReadingInput

# Standard baseline litres per person per day by unit type (in kL: 0.150 kL = 150 Litres)
BENCHMARK_KL_PER_PERSON_DAY = {
    "STUDIO": 0.140,
    "1BHK": 0.150,
    "2BHK": 0.160,
    "3BHK": 0.170,
    "PENTHOUSE": 0.180
}


def compute_peer_or_rule_fallback(
    flat: FlatReadingInput,
    clean_history: list[float]
) -> Tuple[Optional[float], Optional[float], Optional[float], str, str, str, str]:
    """
    Computes expected daily range, status, confidence, pattern, and reason when
    ML model is unavailable or flat history is insufficient (< 3 cycles).
    
    Returns:
        (min_daily, expected_daily, max_daily, confidence, status, pattern, reason)
    """
    days = max(1, flat.days_in_cycle)
    daily = flat.current_usage_kl / float(days)
    hh_size = float(flat.household_size_avg_in_cycle if flat.household_size_avg_in_cycle and flat.household_size_avg_in_cycle > 0 else 1.0)
    flat_label = f"Flat {flat.apartment_number}" if flat.apartment_number else f"Apartment #{flat.apartment_id or 'unit'}"

    # 1. Vacant flat handling
    if flat.occupancy_active is False or hh_size == 0:
        if daily > 0.05:
            # Vacant with usage
            return (
                0.0, 0.0, 0.05,
                "HIGH",
                "POTENTIAL_LEAK",
                "VACANT_USAGE",
                f"{flat_label} is listed as vacant but registered {flat.current_usage_kl:.1f} kL this cycle. Possible leak or unauthorized water draw."
            )
        else:
            return (
                0.0, 0.0, 0.05,
                "HIGH",
                "NORMAL",
                "NONE",
                f"{flat_label} is vacant with expected near-zero water usage."
            )

    # 2. If we have at least 2 past cycles, use the system's Mean + 2*SD rule
    if len(clean_history) >= 2:
        mean_daily = float(np.mean(clean_history))
        std_daily = float(np.std(clean_history))
        min_daily = max(0.0, mean_daily - std_daily)
        max_daily = mean_daily + (2.0 * std_daily)
        leak_thresh_daily = max_daily

        # Minimum physical consumption threshold check (10 kL per cycle = 0.33 kL/day)
        is_leak = (daily > leak_thresh_daily) and (flat.current_usage_kl > 10.0)
        status = "POTENTIAL_LEAK" if is_leak else "NORMAL"
        pattern = "SUDDEN_SPIKE" if is_leak else "NONE"
        conf = "MEDIUM"

        if is_leak:
            reason = (
                f"{flat_label} used {flat.current_usage_kl:.1f} kL this cycle. "
                f"Historical normal range is {min_daily * days:.1f} to {max_daily * days:.1f} kL. "
                f"This is significantly above past patterns. Please inspect plumbing fixtures and toilet cisterns."
            )
        else:
            reason = f"{flat_label} water consumption ({flat.current_usage_kl:.1f} kL) is within normal historical baseline."

        return min_daily, mean_daily, max_daily, conf, status, pattern, reason

    # 3. Cold Start (< 2 past cycles): Try Peer Group for unit type & household size
    unit_type = (flat.unit_type or "2BHK").upper()
    peer_avg = flat.peer_avg_daily_kl
    peer_count = flat.peer_count or 0

    if peer_avg is not None and peer_avg > 0 and peer_count >= 3:
        # Scale peer average by household size ratio if peer count >= 3
        expected_daily = peer_avg
        min_daily = max(0.0, peer_avg * 0.6)
        max_daily = peer_avg * 1.6
        conf = "LOW"

        if daily > max_daily and flat.current_usage_kl > 12.0:
            status = "WATCH"  # Never HIGH_RISK on cold start alone
            pattern = "SUDDEN_SPIKE"
            reason = (
                f"{flat_label} has fewer than 2 past cycles. Peer {unit_type} flats averaged {peer_avg * days:.1f} kL. "
                f"Current usage of {flat.current_usage_kl:.1f} kL is elevated compared to peers. Monitored on watch."
            )
        else:
            status = "NORMAL"
            pattern = "NONE"
            reason = f"{flat_label} baseline is based on peer {unit_type} flats ({peer_avg * days:.1f} kL expected). Usage is consistent with peers."

        return min_daily, expected_daily, max_daily, conf, status, pattern, reason

    # 4. Fall back to Building Average per person
    bldg_per_person = flat.building_avg_daily_kl_per_person
    if bldg_per_person is not None and bldg_per_person > 0:
        expected_daily = bldg_per_person * hh_size
        min_daily = max(0.0, expected_daily * 0.5)
        max_daily = expected_daily * 1.8
        conf = "LOW"
        status = "WATCH" if (daily > max_daily and flat.current_usage_kl > 12.0) else "NORMAL"
        pattern = "SUDDEN_SPIKE" if status == "WATCH" else "NONE"
        reason = (
            f"{flat_label} has insufficient flat history. Estimated from building per-person average "
            f"({bldg_per_person * 1000:.0f} L/person/day for {hh_size:.0f} occupants: ~{expected_daily * days:.1f} kL). "
            + ("Usage is somewhat high." if status == "WATCH" else "Usage is within expected range.")
        )
        return min_daily, expected_daily, max_daily, conf, status, pattern, reason

    # 5. Last Resort Fallback: Ingest benchmark benchmark per person
    benchmark_person = BENCHMARK_KL_PER_PERSON_DAY.get(unit_type, 0.160)
    expected_daily = benchmark_person * hh_size
    min_daily = max(0.0, expected_daily * 0.5)
    max_daily = expected_daily * 1.9

    return (
        min_daily,
        expected_daily,
        max_daily,
        "LOW",
        "INSUFFICIENT_HISTORY",
        "NONE",
        f"{flat_label} does not have sufficient billing history or peer data to establish a reliable baseline."
    )
