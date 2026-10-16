from contextlib import asynccontextmanager
import logging
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.responses import JSONResponse

from app.config import settings
from app.logging_config import setup_logging
from app.security import verify_api_key
from app.schemas import (
    LeakScoreBatchRequest,
    LeakScoreBatchResponse,
    HealthResponse,
    ModelInfoResponse
)
from app.predictor import LeakPredictor

setup_logging()
logger = logging.getLogger("ml_service")

predictor = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    global predictor
    logger.info("Initializing AquaFlow ML Leak Service...")
    predictor = LeakPredictor()
    yield
    logger.info("Shutting down AquaFlow ML Leak Service...")


app = FastAPI(
    title="AquaFlow ML Leak Detection Service",
    description="Intelligent water leak detection and quantile consumption estimation service.",
    version="1.0.0",
    lifespan=lifespan
)


def get_predictor() -> LeakPredictor:
    global predictor
    if predictor is None:
        logger.info("Lazily initializing LeakPredictor...")
        predictor = LeakPredictor()
    return predictor


@app.get("/health", response_model=HealthResponse, tags=["Health"])
def health_check():
    """Health check endpoint to verify service and model status."""
    p = get_predictor()
    return HealthResponse(
        status="ok",
        model_version=p.model_version,
        schema_version=settings.schema_version
    )


@app.get("/v1/model/info", response_model=ModelInfoResponse, dependencies=[Depends(verify_api_key)], tags=["Model"])
def model_info():
    """Returns metadata and evaluation metrics for the active ML model."""
    p = get_predictor()
    info = p.model_info or {}
    from app.features import FEATURE_NAMES

    return ModelInfoResponse(
        model_version=p.model_version,
        schema_version=settings.schema_version,
        training_date=info.get("training_date", "N/A"),
        metrics=info.get("metrics", {}),
        feature_list=info.get("features", FEATURE_NAMES)
    )


@app.post("/v1/leak/score", response_model=LeakScoreBatchResponse, dependencies=[Depends(verify_api_key)], tags=["Inference"])
def score_leak_batch(request: LeakScoreBatchRequest):
    """
    Evaluates a batch of flat water meter readings for consumption anomalies and leak patterns.
    Enforces max 500 flats per batch.
    """
    p = get_predictor()

    if request.schema_version != settings.schema_version:
        logger.warning(f"Schema version mismatch: received {request.schema_version}, expected {settings.schema_version}")

    results = p.score_batch(request.flats)

    return LeakScoreBatchResponse(
        model_version=p.model_version,
        results=results
    )


@app.exception_handler(Exception)
async def global_exception_handler(request, exc):
    logger.error(f"Unhandled error processing request {request.url}: {exc}", exc_info=True)
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={"detail": "Internal server error in ML leak service"}
    )
