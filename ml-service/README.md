# AquaFlow ML Leak Detection Service

The **AquaFlow ML Leak Detection Service** is an intelligent, privacy-preserving microservice built with FastAPI, LightGBM, and Scikit-Learn to detect residential water leaks per flat.

---

## 1. Architecture & Design Principles

- **Separation of Concerns**: The ML service is a dedicated Python service. It **never** connects directly to MySQL. All consumption records, peer metrics, and household metadata are transmitted over a secure REST contract.
- **Fail-Safe & Non-Blocking**: If the ML service is down, slow, or disabled (`ml.enabled=false`), the backend seamlessly falls back to the statistical mean + 2*SD rule. Water billing and meter reading imports **never** fail.
- **Privacy by Design**: No resident names, emails, phone numbers, or PII are ever received or stored by the ML service. Only internal identifiers (`apartment_id`, `usage_id`), numeric household counts, and consumption series are processed.
- **Quantile Regression (LightGBM)**: Learns flat-level personalized consumption dynamics for 10th (q10), 50th (q50), and 95th (q95) percentiles. Risk scores are squashed via logistic functions to produce smooth [0.0, 1.0] confidence-calibrated assessments.

---

## 2. Quick Start

### 2.1 Requirements
- Python 3.10+ (Recommended: Python 3.12)
- Dependencies listed in `requirements.txt`

### 2.2 Setup Virtual Environment
```bash
# Windows
py -3.12 -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt

# Linux / macOS
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

### 2.3 Configuration
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```
Ensure `ML_API_KEY` matches the backend's configured key (`ML_API_KEY` in environment or Spring `application.yml`).

---

## 3. Training & Evaluation Pipeline

### 3.1 Generate Synthetic Dataset
Generates 16 flats across 18 billing cycles with mixed unit types, household sizes, vacations, move-ins, and injected leak patterns:
```bash
python -m training.generate_synthetic_data
```

### 3.2 Train Quantile Models
Trains LightGBM models for q10, q50, and q95 on normal baseline cycles:
```bash
python -m training.train_model
```
Model artifacts and `model_version.json` are saved to `models/`.

### 3.3 Evaluate Model
Compares the new model against the old Mean + 2*SD rule and a model without household features:
```bash
python -m training.evaluate_model
```

### 3.4 Retrain from Resident & Owner Feedback
Retrains the model with validated feedback data exported from the backend (`data/feedback/leak_feedback_export.csv`), incorporating label poisoning safeguards:
```bash
python -m training.retrain_from_feedback
```

---

## 4. Running Tests
Run the complete test suite covering feature engineering, predictor quantiles, API endpoints, and edge cases:
```bash
pytest -v
```

---

## 5. Starting the Service
Start the FastAPI service on port 8000:
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Interactive Swagger documentation is available at:
`http://localhost:8000/docs`

---

## 6. Docker Deployment
```bash
docker build -t aquaflow-ml-service .
docker run -p 8000:8000 --env ML_API_KEY=test-ml-secret-key-aqua-flow-2026 aquaflow-ml-service
```
