from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field, model_validator


class FlatReadingInput(BaseModel):
    usage_id: int
    apartment_id: Optional[int] = None
    apartment_number: Optional[str] = None
    unit_type: Optional[str] = "2BHK"
    floor_number: Optional[int] = 1
    month: int = Field(ge=1, le=12)
    year: int = Field(ge=2020, le=2100)
    days_in_cycle: int = Field(default=30)
    current_usage_kl: float

    # Past cycle historical arrays
    past_usage_kl: List[float] = Field(default_factory=list)
    past_days_in_cycle: List[int] = Field(default_factory=list)
    past_household_size_avg: List[float] = Field(default_factory=list)
    past_incident_flags: List[bool] = Field(default_factory=list)

    # Household size features
    household_size_avg_in_cycle: Optional[float] = 1.0
    household_size_at_cycle_end: Optional[int] = 1
    household_changed_in_cycle: Optional[bool] = False
    household_size_source: Optional[str] = "DEFAULT_ESTIMATE"
    household_size_confirmed: Optional[bool] = False

    # Occupancy & Away
    away_days_in_cycle: Optional[int] = 0
    occupied_days_in_cycle: Optional[int] = None
    occupancy_active: Optional[bool] = True
    recent_move_in: Optional[bool] = False

    # Meter metadata
    meter_status: Optional[str] = "ACTIVE"
    meter_recently_replaced: Optional[bool] = False

    # Aggregates / peers
    peer_avg_daily_kl: Optional[float] = None
    peer_count: Optional[int] = 0
    building_avg_daily_kl: Optional[float] = None
    building_avg_daily_kl_per_person: Optional[float] = None


class LeakScoreBatchRequest(BaseModel):
    schema_version: int = 1
    billing_cycle_id: Optional[int] = None
    building_id: Optional[int] = None
    flats: List[FlatReadingInput] = Field(..., max_length=500)


class FlatLeakResult(BaseModel):
    usage_id: int
    expected_min_kl: Optional[float] = None
    expected_kl: Optional[float] = None
    expected_max_kl: Optional[float] = None
    risk_score: float
    status: str
    confidence: str
    pattern: str
    data_quality_flag: Optional[str] = None
    reason: str


class LeakScoreBatchResponse(BaseModel):
    model_version: str
    results: List[FlatLeakResult]


class HealthResponse(BaseModel):
    status: str
    model_version: str
    schema_version: int


class ModelInfoResponse(BaseModel):
    model_version: str
    schema_version: int
    training_date: str
    metrics: Dict[str, Any]
    feature_list: List[str]
