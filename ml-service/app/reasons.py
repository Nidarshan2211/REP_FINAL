import html
from typing import Optional
from app.schemas import FlatReadingInput


def sanitize_text(text: str) -> str:
    """Escapes any HTML tags or characters to ensure plain text only."""
    return html.escape(text.strip())


def generate_leak_reason(
    flat: FlatReadingInput,
    status: str,
    pattern: str,
    risk_score: float,
    current_kl: float,
    expected_min_kl: float,
    expected_kl: float,
    expected_max_kl: float,
    peer_avg_kl: Optional[float] = None
) -> str:
    """
    Constructs an informative, non-alarmist, plain-language explanation for the analysis.
    """
    flat_label = f"Flat {flat.apartment_number}" if flat.apartment_number else f"Apartment #{flat.apartment_id or 'unit'}"
    unit_str = (flat.unit_type or "unit").upper()
    hh_size = flat.household_size_avg_in_cycle or 1.0

    # 1. NOT_APPLICABLE / Common meters
    if status == "NOT_APPLICABLE":
        return sanitize_text(f"{flat_label} is a common-area or unassigned meter. Per-flat residential leak scoring is not applicable.")

    # 2. DATA_SUSPECT (Digit typo, negative, or physical impossibility)
    if status == "DATA_SUSPECT":
        return sanitize_text(
            f"{flat_label} recorded {current_kl:.1f} kL, which exceeds plausible daily physical consumption limits "
            f"(> 3,000 litres per person per day). Please verify the physical meter reading for a potential digit typo or meter fault."
        )

    # 3. VACANT_USAGE
    if pattern == "VACANT_USAGE" or (flat.occupancy_active is False and current_kl > 1.5):
        return sanitize_text(
            f"{flat_label} is currently listed as vacant or unassigned, but registered {current_kl:.1f} kL of water consumption. "
            f"Possible plumbing leak, running toilet, or unrecorded occupancy."
        )

    # 4. AWAY_USAGE
    if pattern == "AWAY_USAGE" or (flat.away_days_in_cycle and flat.away_days_in_cycle >= 15 and current_kl > expected_max_kl * 0.7):
        return sanitize_text(
            f"Household was declared away for {flat.away_days_in_cycle} days this cycle, yet usage remained high at {current_kl:.1f} kL "
            f"(expected under {expected_max_kl * 0.5:.1f} kL while away). Possible continuous background leak while unoccupied."
        )

    # 5. LOW_USAGE_SUSPECT (Sudden drop / stuck meter)
    if status == "LOW_USAGE_SUSPECT":
        if flat.away_days_in_cycle and flat.away_days_in_cycle >= 20:
            return sanitize_text(f"{flat_label} recorded low water usage ({current_kl:.1f} kL), consistent with declared vacation / away period.")
        return sanitize_text(
            f"{flat_label} registered unusually low or zero consumption ({current_kl:.1f} kL) for an occupied flat of {hh_size:.0f} person(s). "
            f"Normal baseline is {expected_min_kl:.1f} to {expected_max_kl:.1f} kL. Please inspect the water meter for a possible mechanical jam or bypass."
        )

    # 6. INSUFFICIENT_HISTORY
    if status == "INSUFFICIENT_HISTORY":
        return sanitize_text(
            f"{flat_label} has insufficient historical cycles to establish a high-confidence personalized baseline. "
            f"Usage of {current_kl:.1f} kL is being tracked as history accumulates."
        )

    # 7. LEAKS: WATCH, POTENTIAL_LEAK, HIGH_RISK_LEAK
    ratio = current_kl / max(1.0, expected_kl)
    peer_context = f"; similar {unit_str} flats averaged {peer_avg_kl:.1f} kL" if peer_avg_kl and peer_avg_kl > 0 else ""

    context_notes = []
    if flat.meter_status and flat.meter_status.upper() == "FAULTY":
        context_notes.append("Meter is currently marked as faulty.")
    if flat.household_changed_in_cycle:
        context_notes.append("Household size recently changed.")
    if flat.recent_move_in:
        context_notes.append("New tenant move-in baseline.")
    if flat.meter_recently_replaced:
        context_notes.append("Meter was recently replaced.")
    note_str = " (" + " ".join(context_notes) + ")" if context_notes else ""

    if status == "HIGH_RISK_LEAK":
        if pattern == "GRADUAL_DRIFT":
            leak_nature = "Persistent upward drift detected across consecutive cycles"
        elif pattern == "STEP_CHANGE":
            leak_nature = "Sustained step-increase above baseline detected"
        else:
            leak_nature = f"High consumption spike ({ratio:.1f}x typical normal)"

        return sanitize_text(
            f"{flat_label} used {current_kl:.1f} kL this cycle (expected range: {expected_min_kl:.1f} to {expected_max_kl:.1f} kL{peer_context}){note_str}. "
            f"{leak_nature}. Highly probable continuous leak. Please inspect toilet flappers, flush tanks, concealed pipes, and garden bibs immediately."
        )

    if status == "POTENTIAL_LEAK":
        if pattern == "GRADUAL_DRIFT":
            leak_nature = "Gradual usage increase observed over the last 3+ cycles"
        elif pattern == "STEP_CHANGE":
            leak_nature = "Substantial step jump over past baseline"
        else:
            leak_nature = f"Usage is about {ratio:.1f} times normal"

        return sanitize_text(
            f"{flat_label} used {current_kl:.1f} kL this cycle. Its normal range is {expected_min_kl:.1f} to {expected_max_kl:.1f} kL{peer_context}{note_str}. "
            f"{leak_nature}. Possible internal plumbing leak or fixture fault. Please check toilet cisterns, leaking taps, and pipe joints."
        )

    if status == "WATCH":
        return sanitize_text(
            f"{flat_label} used {current_kl:.1f} kL, slightly exceeding the upper expected threshold of {expected_max_kl:.1f} kL{note_str}. "
            f"Currently flagged on watch to determine if this is temporary visitor usage or an early minor leak."
        )

    # 8. NORMAL
    return sanitize_text(
        f"{flat_label} water consumption ({current_kl:.1f} kL) is within the expected normal range of "
        f"{expected_min_kl:.1f} to {expected_max_kl:.1f} kL for {hh_size:.0f} occupant(s){note_str}."
    )
