# Smart Water Billing and Management System (AquaFlow)

An enterprise-grade, production-quality, modular, and scalable **Smart Water Billing and Management System** built with **Java 17, Spring Boot 3, Spring Security (JWT), PostgreSQL, React 19, React Router, Bootstrap 5, and Axios**.

---

## 🌟 Key Modules & Features

### 1. Role-Based Access Control (RBAC)
- **Super Admin (`SUPER_ADMIN`)**:
  - System administrator pre-seeded inside database (cannot register via API).
  - Approves or rejects newly registered Building Owners (`PENDING` $\rightarrow$ `APPROVED` / `REJECTED`).
  - Access to system-wide dashboards: total owners, pending approvals, total buildings, total residents, system revenue analytics.
- **Building Owner (`BUILDING_OWNER`)**:
  - Registers through public registration form (initial status `PENDING`).
  - Becomes active only after Super Admin approval.
  - Manages buildings, water meters, resident invitations (sends tokenized email invitation link).
  - Filters/searches residents by name, email, or status (`ACTIVE` / `INACTIVE`).
  - Records monthly meter readings (previous vs current reading).
  - Generates monthly bills based on tiered tariff pricing slabs.
- **Resident (`RESIDENT`)**:
  - Account created **only** via Building Owner invitation (`invitation_status`: `PENDING`).
  - Receives tokenized invitation email link $\rightarrow$ sets password $\rightarrow$ account becomes `ACTIVE`.
  - Accesses personal dashboard to inspect water usage, view bills, pay bills online (mock payment gateway), download PDF invoices, and read notifications.

### 2. Tiered Tariff Billing Engine
- Automated calculation of water bills based on consumption brackets:
  - **Slab 1 (0 - 20 units)**: $2.50 / unit + $10.00 fixed charge.
  - **Slab 2 (21 - 50 units)**: $4.00 / unit + $15.00 fixed charge.
  - **Slab 3 (51+ units)**: $6.50 / unit + $25.00 fixed charge.

### 3. PDF Invoice Generator
- Integrated iText engine generating clean, printable PDF water bill statements (`/api/bills/{id}/pdf`).

---

## 🛠️ Technology Stack

| Layer | Technologies Used |
| :--- | :--- |
| **Backend Framework** | Java 17, Spring Boot 3.2.5, Spring Security, Spring Data JPA, Hibernate, Maven |
| **Security & Auth** | JWT Tokens (`jjwt-0.11.5`), BCrypt Password Hashing, Role-Based Route Protection |
| **Database** | PostgreSQL (schema initialization script `V1__init_schema.sql` & seed data `V2__seed_data.sql`) |
| **Frontend Framework** | React 19, React Router v7, Bootstrap 5.3, Bootstrap Icons, Axios |
| **API Documentation** | Springdoc OpenAPI / Swagger UI 2.5 (`/swagger-ui.html`) |

---

## 🔐 Default Seed Credentials

> [!NOTE]
> All seed passwords are set to `Admin@123456`.

| Role | Email | Password | Status |
| :--- | :--- | :--- | :--- |
| **Super Admin** | `admin@smartwater.com` | `Admin@123456` | `APPROVED` |
| **Approved Owner** | `owner.robert@gmail.com` | `Admin@123456` | `APPROVED` |
| **Pending Owner** | `owner.sarah@gmail.com` | `Admin@123456` | `PENDING` |
| **Active Resident** | `michael.resident@gmail.com` | `Admin@123456` | `ACTIVE` |

---

## 🚀 Setup & Execution Guide

### Prerequisites
- Java JDK 17+
- Apache Maven 3.9+
- Node.js v18+ & npm
- PostgreSQL 14+ (or use H2 in-memory profile for instant testing)

---

### Step 1: Database Setup (PostgreSQL)

Create the PostgreSQL database using psql or PgAdmin:
```sql
CREATE DATABASE smart_water_db;
```
*(Optionally adjust `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` environment variables in `backend/src/main/resources/application.yml` if your local postgres password differs).*

---

### Step 2: Start Backend (Spring Boot)

Navigate to the `backend` directory:
```bash
cd backend
mvn clean spring-boot:run
```
*The Spring Boot server will start on port `8080` with context path `/api`.*
- **OpenAPI / Swagger UI**: [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
- **H2 Console (if using H2 profile)**: [http://localhost:8080/api/h2-console](http://localhost:8080/api/h2-console)

---

### Step 3: Start Frontend (React 19)

Navigate to the `frontend` directory:
```bash
cd frontend
npm install
npm run dev
```
*The React dev server will run on [http://localhost:5173](http://localhost:5173).*

---

## 🌐 REST API Endpoint Summary

### Authentication APIs (`/api/auth`)
- `POST /auth/login` - Authenticate user & return JWT token
- `POST /auth/register-owner` - Register new Building Owner (`PENDING`)
- `POST /auth/accept-invitation` - Set resident password via invitation token

### Super Admin APIs (`/api/admin`)
- `GET /admin/dashboard` - System dashboard metrics
- `GET /admin/owners` - List all building owners
- `GET /admin/owners/pending` - List pending owner registrations
- `PUT /admin/owners/{id}/approve` - Approve building owner
- `PUT /admin/owners/{id}/reject` - Reject building owner

### Building Owner APIs (`/api/owners`)
- `GET /owners/dashboard` - Owner metrics
- `GET /owners/buildings` & `POST /owners/buildings` - Manage buildings
- `GET /owners/residents` - List/filter residents
- `POST /residents/invite` - Invite new resident
- `PUT /owners/residents/{id}` & `DELETE /owners/residents/{id}` - Modify residents

### Billing & Water Meter APIs (`/api`)
- `POST /usage/record` - Record meter readings
- `POST /bills/generate` - Generate monthly bill
- `GET /bills/{id}/pdf` - Download PDF invoice statement
- `POST /resident/pay` - Process online bill payment
