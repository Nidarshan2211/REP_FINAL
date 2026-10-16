from fastapi import Security, HTTPException, status
from fastapi.security import APIKeyHeader
from app.config import settings

API_KEY_HEADER = APIKeyHeader(name="X-API-KEY", auto_error=False)


async def verify_api_key(api_key: str = Security(API_KEY_HEADER)) -> str:
    if not api_key:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing required API key header 'X-API-KEY'."
        )
    if api_key != settings.ml_api_key:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Invalid API key provided."
        )
    return api_key
