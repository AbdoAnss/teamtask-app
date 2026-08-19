# TeamFlow

Application de gestion de tâches d'équipe : projets, membres, tâches assignées et
tableau de bord par projet. Projet personnel full stack, jouable en local ou via
Docker Compose.

## Stack

- **Backend** — Spring Boot 3.2 (Java 17) : API REST, Spring Security + JWT,
  Spring Data JPA, Flyway, MapStruct, PostgreSQL, springdoc-openapi.
- **Frontend** — Angular 17 standalone : guards, interceptor JWT ; tous les
  appels HTTP passent par un client généré depuis la spécification OpenAPI.
- **Infra** — Docker Compose (PostgreSQL, backend, nginx) et GitHub Actions
  pour le CI Maven.

## Démarrage local

Backend avec PostgreSQL :

```bash
docker compose up -d postgres
cd backend
mvn spring-boot:run        # Swagger : http://localhost:8080/swagger-ui.html
```

Frontend :

```bash
cd frontend
npm install
npm start                  # http://localhost:4200
```

Ou tout en Docker (les variables DB/JWT se règlent dans `.env`, voir
`.env.example`) :

```bash
docker compose up --build
```

## Client Angular généré depuis l'OpenAPI

La spec est exportée depuis springdoc puis le client TypeScript est régénéré :

```bash
curl http://localhost:8080/v3/api-docs.yaml -o docs/openapi.yaml
cd frontend && npm run generate:client
```

Les services de `src/app/core/` enveloppent les services générés : ils gardent
des modèles de vue stricts et branchent la session JWT via l'interceptor.

## Tests et couverture

```bash
cd backend && mvn verify
```

Tests unitaires JUnit 5 / Mockito sur les services, la sécurité JWT et les
mappers MapStruct, plus un test d'intégration MockMvc de bout en bout sur H2
(inscription, login, projets, tâches, tableau de bord, contrôle d'accès).
Le build échoue en dessous de 80 % de couverture ligne (JaCoCo), seuil appliqué
par GitHub Actions à chaque push ou PR touchant le backend.

## Migrations

Le schéma est géré par Flyway (`backend/src/main/resources/db/migration`) et
Hibernate tourne en `ddl-auto: validate` : une divergence entre entités et SQL
fait échouer le démarrage plutôt que silencieusement régénérer le schéma.
