# TeamFlow

Application full stack de gestion de taches d'equipe (outil interne fictif).

## Structure

- `backend/`: API Spring Boot 3 (Java 17, JWT, JPA, PostgreSQL, Swagger)
- `frontend/`: Angular 17 (standalone, RxJS, guards, interceptor JWT)
- `docs/`: OpenAPI et diagrammes
- `docker-compose.yml`

## Prerequis

- Java 17+
- Maven 3.9+
- Node 20+
- PostgreSQL 15+ (ou Docker)

## Configuration

1. Copier `.env.example` en `.env` a la racine.
2. Adapter les variables DB/JWT.

## Lancement local

### Backend (port 8080)

```powershell
cd backend
mvn spring-boot:run
```

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

### Frontend (port 4200)

```powershell
cd frontend
npm install
npm start
```

## Docker Compose

```powershell
docker compose --env-file .env up --build
```

- Frontend: `http://localhost:4200`
- Backend: `http://localhost:8080`
- Postgres: `localhost:5432`

## OpenAPI Client Angular

1. Exporter la spec:

```powershell
Invoke-WebRequest http://localhost:8080/v3/api-docs.yaml -OutFile .\docs\openapi.yaml
```

2. Generer le client:

```powershell
cd frontend
npm run generate:client
```

## Tests backend

```powershell
cd backend
mvn test
```

Des tests unitaires JUnit5/Mockito sont fournis sur les services metier principaux.
