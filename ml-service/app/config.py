from pathlib import Path
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    ml_api_key: str = "test-ml-secret-key-aqua-flow-2026"
    port: int = 8000
    host: str = "0.0.0.0"
    model_dir: str = "models"
    log_level: str = "INFO"
    schema_version: int = 1

    # Business thresholds
    vacant_threshold_kl_day: float = 0.05
    max_litres_per_person_day: float = 3000.0  # Sanity cap (3.0 kL/person/day)
    low_usage_ratio_threshold: float = 0.20   # Flag if < 20% normal range for occupied flat

    # Risk score mapping thresholds
    watch_threshold: float = 0.50
    potential_leak_threshold: float = 0.80
    high_risk_threshold: float = 0.95

    model_config = SettingsConfigDict(
        env_file=str(Path(__file__).resolve().parent.parent / ".env"),
        env_file_encoding="utf-8",
        extra="ignore"
    )


settings = Settings()
