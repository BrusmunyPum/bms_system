# Business Management System (BMS)

Welcome to the BMS project. This is a unified commercial operations system for goods, services, quotations, purchasing, inventory, invoicing, payments, and reporting.

## Architecture
- **Frontend**: Angular 22
- **Backend**: Spring Boot 4.1 (Java 21)
- **Database**: PostgreSQL

## Documentation
Please refer to the `docs/` directory for detailed architecture, requirements, and the implementation backlog:
- `docs/ARCHITECTURE.md` - Technical design and structure
- `docs/BUSINESS_REQUIREMENTS.md` - Business rules and use cases
- `docs/SYSTEM_REBUILD_PLAN.md` - Migration and transition strategy
- `docs/IMPLEMENTATION_TASK_BACKLOG.md` - Development phases and task list

## Local Development Guide

### 1. Configuration & Secrets
Copy the template `.env.example` to `.env` in the root directory:
```bash
cp .env.example .env
```
Update the `.env` file with your local PostgreSQL password. 
**Security Rule**: The `.env` file contains secrets and is explicitly ignored by `.gitignore`. Never commit passwords to Git. 

The environment variables used are:
- `BMS_DB_URL`: The JDBC connection string (default: `jdbc:postgresql://localhost:5432/bms_dev`)
- `BMS_DB_USERNAME`: Database username (default: `postgres`)
- `BMS_DB_PASSWORD`: Database password

### 2. Starting the Services

**Database**
Ensure you have a local PostgreSQL instance running. Alternatively, you can use Docker Compose (once configured in `infra/compose`):
```bash
docker-compose -f infra/compose/docker-compose.yml up -d db
```

**Backend API (Spring Boot)**
Open a terminal in `apps/bms-api/` and run:
```powershell
.\mvnw.cmd spring-boot:run
```
The API will start on `http://localhost:8080`. Flyway will automatically run database migrations on startup.

**Frontend Web (Angular)**
Open a terminal in `apps/web/`, install dependencies, and run:
```powershell
npm install
npm start
```
The frontend will start on `http://localhost:4200` and proxy `/api` calls directly to the Spring Boot backend.

### 3. Testing and Code Quality

Run these commands to verify the quality of your code before committing:

**Check the API (Tests & Compilation)**
```powershell
cd apps/bms-api
.\mvnw.cmd test
```

**Check the Web App (Linting, Formatting, & Tests)**
```powershell
cd apps/web
npm run format:check
npm test
```
*(To auto-fix web formatting issues, run `npm run format`)*

### 4. Resetting the Local Environment
To reset your local database, you can drop the `bms_dev` database and recreate it, or run:
```powershell
cd apps/bms-api
.\mvnw.cmd flyway:clean flyway:migrate
```
