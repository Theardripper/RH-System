# HR System — Backend

API REST completa para gestão de Recursos Humanos, desenvolvida com Java 21, Spring Boot 4 e PostgreSQL. O sistema cobre autenticação JWT, gestão de funcionários, departamentos e solicitações de férias, com testes unitários e documentação Swagger integrada.

---

## Tecnologias

- **Java 21** + **Spring Boot 4**
- **Spring Data JPA** + **Hibernate 7** — mapeamento objeto-relacional com LAZY loading e JPQL
- **Spring Security 7** — autenticação stateless com JWT (JJWT 0.12.x)
- **PostgreSQL 16** — banco de dados relacional com enums, índices e triggers nativos
- **Flyway** — versionamento e migração automática do schema
- **Lombok** + **MapStruct** — redução de boilerplate e conversão de DTOs
- **SpringDoc OpenAPI 3** — documentação interativa via Swagger UI
- **JUnit 5** + **Mockito** — testes unitários de services e controllers

---

## Funcionalidades

- Autenticação e autorização com JWT, controle de acesso por perfis (ADMIN, MANAGER, EMPLOYEE)
- CRUD completo de funcionários com paginação, busca global e filtro por departamento
- CRUD de departamentos com contagem de funcionários ativos
- Fluxo de solicitações de férias: criação, aprovação, rejeição e cancelamento
- Validação de conflito de datas em solicitações de férias
- Soft delete de funcionários (desativação/reativação)
- Tratamento centralizado de exceções com respostas JSON padronizadas
- Documentação completa dos endpoints via Swagger UI

---

## Estrutura do Projeto

```
backend/
├── src/main/java/com/rh/system/
│   ├── config/          # SecurityConfig, WebConfig (CORS), OpenApiConfig
│   ├── controller/      # AuthController, EmployeeController, DepartmentController, VacationController
│   ├── dto/
│   │   ├── request/     # LoginRequest, RegisterRequest, EmployeeRequest, DepartmentRequest, VacationRequestDto
│   │   └── response/    # AuthResponse, EmployeeResponse, DepartmentResponse, VacationResponse, PageResponse
│   ├── entity/          # User, Employee, Department, VacationRequest + enums
│   ├── exception/       # ResourceNotFoundException, BusinessException, ConflictException, GlobalExceptionHandler
│   ├── repository/      # Repositórios JPA com queries JPQL customizadas
│   ├── security/        # JwtService, JwtAuthenticationFilter
│   └── service/         # AuthService, EmployeeService, DepartmentService, VacationService
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/    # V1__create_initial_schema.sql, V2__seed_initial_data.sql
└── src/test/            # Testes unitários com JUnit 5 e Mockito
```

---

## Como Executar

### Pré-requisitos

- Java 21+
- Maven 3.9+
- PostgreSQL 16+

### 1. Clonar o repositório

```bash
git clone https://github.com/seu-usuario/hr-system.git
cd hr-system/backend
```

### 2. Criar o banco de dados

```bash
psql -U postgres -c "CREATE DATABASE hr_system_db;"
psql -U postgres -d hr_system_db -f hr-schema.sql
```

### 3. Configurar o `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/hr_system_db
spring.datasource.username=postgres
spring.datasource.password=sua_senha

app.jwt.secret=seu_secret_aqui
app.jwt.expiration-ms=86400000
```

### 4. Rodar a aplicação

```bash
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---

## Documentação da API

Acesse o Swagger UI em:

```
http://localhost:8080/swagger-ui.html
```

### Endpoints principais

| Método | Endpoint | Descrição | Role |
|--------|----------|-----------|------|
| POST | `/api/auth/login` | Autenticação — retorna token JWT | Público |
| POST | `/api/auth/register` | Registra novo usuário | ADMIN |
| GET | `/api/employees` | Lista funcionários com paginação e filtros | Autenticado |
| POST | `/api/employees` | Cria funcionário | ADMIN, MANAGER |
| PUT | `/api/employees/{id}` | Atualiza funcionário | ADMIN, MANAGER |
| PATCH | `/api/employees/{id}/deactivate` | Desativa funcionário | ADMIN |
| PATCH | `/api/employees/{id}/activate` | Reativa funcionário | ADMIN |
| GET | `/api/departments` | Lista departamentos com paginação | Autenticado |
| POST | `/api/departments` | Cria departamento | ADMIN |
| PUT | `/api/departments/{id}` | Atualiza departamento | ADMIN |
| DELETE | `/api/departments/{id}` | Remove departamento | ADMIN |
| GET | `/api/vacations/employee/{id}` | Lista férias do funcionário | Autenticado |
| POST | `/api/vacations/employee/{id}` | Solicita férias | Autenticado |
| PATCH | `/api/vacations/{id}/approve` | Aprova solicitação | ADMIN, MANAGER |
| PATCH | `/api/vacations/{id}/reject` | Rejeita solicitação | ADMIN, MANAGER |
| PATCH | `/api/vacations/{id}/cancel` | Cancela solicitação | Autenticado |

### Autenticação

Todas as requisições (exceto `/api/auth/login`) exigem o header:

```
Authorization: Bearer <token>
```

O token é obtido no endpoint de login:

```json
POST /api/auth/login
{
  "username": "admin",
  "password": "Admin@1234"
}
```

---

## Testes

```bash
mvn test
```

Os testes cobrem as camadas de service e controller com JUnit 5 e Mockito, usando banco H2 em memória isolado para não afetar o banco de desenvolvimento.

---

## Decisões de Arquitetura

**Hibernate com LAZY loading** — todos os relacionamentos usam `FetchType.LAZY` com `JOIN FETCH` explícito nas queries que precisam dos dados, evitando o problema N+1.

**DTOs separados por entrada e saída** — `Request` DTOs com Bean Validation para entrada, `Response` DTOs limpos para saída. A entidade nunca é exposta diretamente na API.

**Exceções tipadas** — `ResourceNotFoundException` (404), `ConflictException` (409) e `BusinessException` (400) tratadas de forma centralizada no `GlobalExceptionHandler`.

**Stateless** — nenhuma sessão HTTP é mantida no servidor. O token JWT carrega todas as informações de autenticação necessárias.

**PageResponse genérico** — `PageResponse<T>` encapsula qualquer listagem paginada com metadados (página atual, total de elementos, total de páginas), padronizando todas as respostas de listagem da API.
