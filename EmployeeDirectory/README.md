# Employee Directory API

A Spring Boot REST API for managing employees, departments, and skills. It uses an in-memory H2 database, Spring Data JPA, Bean Validation, and a service/controller architecture.

## Requirements

- Java 17 or newer
- Maven 3.6+

## Run

From the project root:

```sh
mvn spring-boot:run
```

The API is available at `http://localhost:8080`. The H2 database is created automatically and is reset when the application stops. The H2 console is at `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:employeedb`, username `sa`, blank password).

Run tests with:

```sh
mvn test
```

Interactive Swagger UI is at `http://localhost:8080/swagger-ui/index.html`; the OpenAPI 3.0 YAML is also available in the project root as `openapi.yaml`.

## Configuration

Values are externalised in `src/main/resources/application.properties` and injected into application beans:

| Property | Default | Purpose |
| --- | --- | --- |
| `app.name` | `Employee Directory API` | Name returned by `/api/info` |
| `app.version` | `1.0.0` | Version returned by `/api/info` |
| `directory.max-skills-per-employee` | `20` | Maximum skills allowed per employee |
| `pagination.default-page-size` | `20` | Default page size for employee searches |

Override values using Spring's usual mechanisms, for example `--directory.max-skills-per-employee=10`.

## API overview

- `GET /api/info`
- `GET|POST /api/departments`, `GET|PUT|DELETE /api/departments/{id}`
- `GET /api/departments/{id}/employees`
- `GET|POST /api/skills`
- `GET|POST /api/employees`, `GET|PUT|DELETE /api/employees/{id}`
- `POST|DELETE /api/employees/{id}/skills/{sid}`

Employee list endpoints support `departmentId`, `page`, `size`, and `sort` query parameters. Supported sort fields are `name`, `email`, `joiningDate`, and `designation`; page numbering starts at zero and the maximum page size is 200. Departments include a computed `headCount`. Departments with employees and skills assigned to employees cannot be deleted.

## Sample requests

Create a department:

```sh
curl -X POST http://localhost:8080/api/departments \
  -H "Content-Type: application/json" \
  -d '{"name":"Engineering","location":"London"}'
```

Create two skills:

```sh
curl -X POST http://localhost:8080/api/skills -H "Content-Type: application/json" \
  -d '{"name":"Java","category":"Backend"}'
curl -X POST http://localhost:8080/api/skills -H "Content-Type: application/json" \
  -d '{"name":"SQL","category":"Data"}'
```

Create an employee (replace the IDs with those returned above):

```sh
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"Alex Doe","email":"alex@example.com","designation":"Engineer","joiningDate":"2024-01-15","departmentId":1,"skillIds":[1,2]}'
```

Filter and paginate employees:

```sh
curl "http://localhost:8080/api/employees?departmentId=1&page=0&size=10&sort=name"
```

Assign/remove a skill and fetch application info:

```sh
curl -X POST http://localhost:8080/api/employees/1/skills/1
curl -X DELETE http://localhost:8080/api/employees/1/skills/1
curl http://localhost:8080/api/info
```

Errors use a consistent JSON shape with timestamp, HTTP status, error, message, and field-level validation errors where relevant.
