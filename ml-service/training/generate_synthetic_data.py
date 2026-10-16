import os
import json
import random
from pathlib import Path
import numpy as np
import pandas as pd

from app.features import UNIT_TYPE_MAP
from app.fallback import BENCHMARK_KL_PER_PERSON_DAY

RANDOM_SEED = 42
random.seed(RANDOM_SEED)
np.random.seed(RANDOM_SEED)

DATA_DIR = Path(__file__).resolve().parent.parent / "data" / "synthetic"
DATA_DIR.mkdir(parents=True, exist_ok=True)

# 16 Flats: Floors 1 to 4, rooms 01 to 04
FLATS_METADATA = [
    # Floor 1
    {"apt_id": 101, "apt_num": "101", "floor": 1, "unit_type": "1BHK", "default_hh": 2},
    {"apt_id": 102, "apt_num": "102", "floor": 1, "unit_type": "2BHK", "default_hh": 3},
    {"apt_id": 103, "apt_num": "103", "floor": 1, "unit_type": "2BHK", "default_hh": 3},
    {"apt_id": 104, "apt_num": "104", "floor": 1, "unit_type": "STUDIO", "default_hh": 1},
    # Floor 2
    {"apt_id": 201, "apt_num": "201", "floor": 2, "unit_type": "2BHK", "default_hh": 3},
    {"apt_id": 202, "apt_num": "202", "floor": 2, "unit_type": "2BHK", "default_hh": 4},
    {"apt_id": 203, "apt_num": "203", "floor": 2, "unit_type": "1BHK", "default_hh": 2},
    {"apt_id": 204, "apt_num": "204", "floor": 2, "unit_type": "STUDIO", "default_hh": 1},
    # Floor 3
    {"apt_id": 301, "apt_num": "301", "floor": 3, "unit_type": "3BHK", "default_hh": 4},
    {"apt_id": 302, "apt_num": "302", "floor": 3, "unit_type": "2BHK", "default_hh": 3},
    {"apt_id": 303, "apt_num": "303", "floor": 3, "unit_type": "2BHK", "default_hh": 2},
    {"apt_id": 304, "apt_num": "304", "floor": 3, "unit_type": "1BHK", "default_hh": 2},
    # Floor 4
    {"apt_id": 401, "apt_num": "401", "floor": 4, "unit_type": "3BHK", "default_hh": 4},
    {"apt_id": 402, "apt_num": "402", "floor": 4, "unit_type": "2BHK", "default_hh": 3},
    {"apt_id": 403, "apt_num": "403", "floor": 4, "unit_type": "PENTHOUSE", "default_hh": 5},
    {"apt_id": 404, "apt_num": "404", "floor": 4, "unit_type": "PENTHOUSE", "default_hh": 4},
]

# 18 Monthly Cycles (starting 2025-01 through 2026-06)
CYCLES = []
cycle_days = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31, 31, 28, 31, 30, 31, 30]
year = 2025
month = 1
for cid, days in enumerate(cycle_days, start=1):
    CYCLES.append({
        "cycle_id": cid,
        "year": year,
        "month": month,
        "days": days
    })
    month += 1
    if month > 12:
        month = 1
        year += 1


def generate_synthetic_dataset():
    print(f"Generating synthetic dataset: 16 flats x 18 cycles ({len(FLATS_METADATA) * len(CYCLES)} records)...")
    records = []

    # Track flat state across cycles
    flat_states = {}
    for f in FLATS_METADATA:
        flat_states[f["apt_id"]] = {
            "hh_size": f["default_hh"],
            "confirmed": True,
            "meter_replaced": False,
            "active": True,
            "history_usage": [],
            "history_days": [],
            "history_hh": [],
            "history_incident": []
        }

    usage_id_counter = 1000

    for c in CYCLES:
        cid = c["cycle_id"]
        c_month = c["month"]
        c_year = c["year"]
        c_days = c["days"]

        # Seasonal multiplier (hotter months June-Aug higher, Dec-Jan lower)
        season_factor = 1.0 + 0.15 * np.sin(2.0 * np.pi * (c_month - 3) / 12.0)

        # Precompute peer and building averages
        cycle_flat_readings = []

        for f in FLATS_METADATA:
            usage_id_counter += 1
            apt_id = f["apt_id"]
            unit_type = f["unit_type"]
            state = flat_states[apt_id]

            # Dynamic life events
            # Event 1: Mid-history household change (e.g., cycle 7 or 12)
            hh_changed = False
            if cid in (7, 13) and apt_id in (102, 301, 403):
                state["hh_size"] = min(6, state["hh_size"] + 1)
                hh_changed = True
            elif cid in (9, 15) and apt_id in (202, 302):
                state["hh_size"] = max(1, state["hh_size"] - 1)
                hh_changed = True

            # Event 2: Away periods / vacation
            away_days = 0
            if cid in (6, 14) and apt_id in (103, 203, 303):
                away_days = random.randint(14, 25)

            # Event 3: Vacancy / move-in
            is_active = True
            recent_move_in = False
            if cid in (4, 5) and apt_id == 104:
                is_active = False  # Vacant in cycles 4 and 5
            elif cid == 6 and apt_id == 104:
                recent_move_in = True  # Moved in cycle 6
                state["history_usage"].clear()
                state["history_days"].clear()
                state["history_hh"].clear()
                state["history_incident"].clear()

            # Event 4: Meter replacement
            meter_replaced = False
            if cid == 8 and apt_id == 201:
                meter_replaced = True

            # Base normal consumption (per person benchmark + individual noise)
            hh_size = state["hh_size"] if is_active else 0
            base_rate = BENCHMARK_KL_PER_PERSON_DAY[unit_type]

            if not is_active:
                # Vacant flat: ~0.01 kL/day
                base_daily = 0.01 + np.random.uniform(0.0, 0.02)
            else:
                effective_occupied_days = max(1, c_days - away_days)
                daily_active = (base_rate * hh_size * season_factor) * np.random.normal(1.0, 0.08)
                daily_away = 0.02 * hh_size
                base_daily = (daily_active * effective_occupied_days + daily_away * away_days) / float(c_days)

            base_daily = max(0.01, base_daily)
            usage_kl = base_daily * c_days

            # Inject leaks in ~8-10% of flat-cycles after cycle 3
            is_leak = False
            leak_type = "NONE"
            if cid >= 3 and random.random() < 0.085:
                leak_choice = random.choice(["SUDDEN_SPIKE", "GRADUAL_DRIFT", "STEP_CHANGE", "VACANT_USAGE"])
                if not is_active:
                    leak_choice = "VACANT_USAGE"

                if leak_choice == "SUDDEN_SPIKE":
                    usage_kl *= np.random.uniform(2.0, 3.2)
                    is_leak = True
                    leak_type = "SUDDEN_SPIKE"
                elif leak_choice == "GRADUAL_DRIFT":
                    usage_kl *= np.random.uniform(1.6, 2.2)
                    is_leak = True
                    leak_type = "GRADUAL_DRIFT"
                elif leak_choice == "STEP_CHANGE":
                    usage_kl *= np.random.uniform(1.5, 2.0)
                    is_leak = True
                    leak_type = "STEP_CHANGE"
                elif leak_choice == "VACANT_USAGE":
                    usage_kl = float(np.random.uniform(8.0, 25.0))
                    is_leak = True
                    leak_type = "VACANT_USAGE"

            cycle_flat_readings.append({
                "usage_id": usage_id_counter,
                "apartment_id": apt_id,
                "apartment_number": f["apt_num"],
                "unit_type": unit_type,
                "floor_number": f["floor"],
                "month": c_month,
                "year": c_year,
                "days_in_cycle": c_days,
                "current_usage_kl": round(usage_kl, 2),
                "daily_usage_kl": round(usage_kl / c_days, 3),
                "household_size_avg_in_cycle": float(hh_size),
                "household_size_at_cycle_end": int(hh_size),
                "household_changed_in_cycle": hh_changed,
                "household_size_source": "RESIDENT_DECLARED",
                "household_size_confirmed": True,
                "away_days_in_cycle": away_days,
                "occupied_days_in_cycle": max(0, c_days - away_days),
                "occupancy_active": is_active,
                "recent_move_in": recent_move_in,
                "meter_status": "ACTIVE",
                "meter_recently_replaced": meter_replaced,
                "is_leak": is_leak,
                "leak_type": leak_type,
                "past_usage_kl": list(state["history_usage"]),
                "past_days_in_cycle": list(state["history_days"]),
                "past_household_size_avg": list(state["history_hh"]),
                "past_incident_flags": list(state["history_incident"])
            })

            # Update history state for next cycles
            state["history_usage"].insert(0, round(usage_kl, 2))
            state["history_days"].insert(0, c_days)
            state["history_hh"].insert(0, float(hh_size))
            state["history_incident"].insert(0, is_leak)

        # Calculate building & peer averages for this cycle
        bldg_daily_sum = sum(r["daily_usage_kl"] for r in cycle_flat_readings)
        bldg_avg_daily = bldg_daily_sum / len(cycle_flat_readings)
        total_people = sum(r["household_size_avg_in_cycle"] for r in cycle_flat_readings)
        bldg_per_person = bldg_daily_sum / max(1.0, total_people)

        for r in cycle_flat_readings:
            # Peers: same unit type excluding this flat
            peers = [p["daily_usage_kl"] for p in cycle_flat_readings if p["unit_type"] == r["unit_type"] and p["apartment_id"] != r["apartment_id"]]
            r["peer_avg_daily_kl"] = round(float(np.mean(peers)), 3) if peers else round(bldg_avg_daily, 3)
            r["peer_count"] = len(peers)
            r["building_avg_daily_kl"] = round(bldg_avg_daily, 3)
            r["building_avg_daily_kl_per_person"] = round(bldg_per_person, 3)
            records.append(r)

    # Convert to dataframe & save
    df = pd.DataFrame(records)
    csv_path = DATA_DIR / "synthetic_water_usage.csv"
    json_path = DATA_DIR / "synthetic_water_usage.json"
    df.to_csv(csv_path, index=False)
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(records, f, indent=2)

    leak_count = df['is_leak'].sum()
    print(f"Generated {len(df)} records ({leak_count} leak cycles, {leak_count/len(df)*100:.1f}%) saved to {csv_path}")
    return df


if __name__ == "__main__":
    generate_synthetic_dataset()
