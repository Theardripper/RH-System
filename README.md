# HR System — Sistema de Gestão de Recursos Humanos

Sistema completo de RH desenvolvido como projeto de portfólio, com backend em Java Spring Boot e frontend em Angular 17. Cobre autenticação JWT, gestão de funcionários, departamentos e solicitações de férias, com testes unitários, documentação Swagger e interface web responsiva.

---

## Screenshots

### Login
![alt text](Screenshot_2026-06-10_18-36-52.png)

### Dashboard
![Dashboard](docs/screenshots/dashboard.png)

### Funcionários
![Funcionários](docs/screenshots/employees.png)

### Férias
![Férias](docs/screenshots/vacations.png)

---

## Visão Geral

```
hr-system/
├── backend/    → API REST (Java 21 + Spring Boot 4 + PostgreSQL)
└── frontend/   → Interface web (Angular 17 + TypeScript + SCSS)
```

O backend expõe uma API REST documentada via Swagger e o frontend consome essa API com autenticação stateless via JWT. Os dois projetos são independentes e se comunicam exclusivamente por HTTP.

---

## Stack Tecnológica

### Backend
| Tecnologia | Versão | Uso |
|-----------|--------|-----|
| Java | 21 | Linguagem |
| Spring Boot | 4.0 | Framework principal |
| Spring Security | 7 | Autenticação e autorização |
| Hibernate / JPA | 7 | ORM e mapeamento objeto-relacional |
| PostgreSQL | 16 | Banco de dados relacional |
| JWT (JJWT) | 0.12.5 | Tokens de autenticação |
| Flyway | — | Versionamento do banco de dados |
| Lombok | — | Redução de boilerplate |
| SpringDoc OpenAPI | 3 | Documentação Swagger UI |
| JUnit 5 + Mockito | — | Testes unitários |

### Frontend
| Tecnologia | Versão | Uso |
|-----------|--------|-----|
| Angular | 17 | Framework principal |
| TypeScript | 5.4 | Linguagem |
| RxJS | 7.8 | Programação reativa |
| Angular Signals | — | Estado reativo dos componentes |
| SCSS | — | Estilização com design tokens |

---

## Funcionalidades

### Autenticação e Segurança
- Login com geração de token JWT (expiração configurável)
- Autorização por perfil em todos os endpoints (ADMIN, MANAGER, EMPLOYEE)
- Interceptor HTTP no frontend injeta o token automaticamente
- Redirecionamento automático para login em respostas 401

### Funcionários
- Listagem com paginação, busca global por nome/e-mail e filtro por departamento
- Ordenação por nome e data de contratação
- Cadastro e edição com validação de campos
- Ativação e desativação (soft delete — mantém histórico)
- Visualização detalhada com histórico de férias

### Departamentos
- CRUD completo com confirmação antes de excluir
- Exclusão bloqueada quando há funcionários ativos vinculados
- Contagem de funcionários ativos exibida na listagem

### Férias
- Solicitação de férias com validação de conflito de datas
- Fluxo de aprovação: PENDING → APPROVED / REJECTED
- Cancelamento pelo próprio funcionário
- Filtro por status (Pendentes, Aprovadas, Rejeitadas, Canceladas)
- Preview de dias calculado em tempo real no formulário

### Perfis de Acesso

| Funcionalidade | ADMIN | MANAGER | EMPLOYEE |
|----------------|:-----:|:-------:|:--------:|
| Ver funcionários | ✅ | ✅ | ✅ |
| Criar/editar funcionários | ✅ | ✅ | ❌ |
| Ativar/desativar funcionários | ✅ | ❌ | ❌ |
| Gerenciar departamentos | ✅ | ❌ | ❌ |
| Aprovar/rejeitar férias | ✅ | ✅ | ❌ |
| Solicitar férias | ✅ | ✅ | ✅ |
| Registrar usuários | ✅ | ❌ | ❌ |

---

## Como Executar

### Pré-requisitos

- Java 21+
- Maven 3.9+
- Node.js 20+
- npm 10+
- PostgreSQL 16+

### 1. Clonar o repositório

```bash
git clone https://github.com/seu-usuario/hr-system.git
cd hr-system
```

### 2. Configurar e rodar o banco de dados

```bash
# Criar o banco
psql -U postgres -c "CREATE DATABASE hr_system_db;"

# Criar as tabelas e dados iniciais
psql -U postgres -d hr_system_db -f hr-schema.sql
```

### 3. Configurar o backend

Edite `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/hr_system_db
spring.datasource.username=postgres
spring.datasource.password=sua_senha

app.jwt.secret=seu_secret_aqui
app.jwt.expiration-ms=86400000
```

### 4. Rodar o backend

```bash
cd backend
mvn spring-boot:run
```

API disponível em `http://localhost:8080`
Swagger UI em `http://localhost:8080/swagger-ui.html`

### 5. Rodar o frontend

```bash
cd frontend
npm install
npm start
```

Interface disponível em `http://localhost:4200`

O proxy de desenvolvimento redireciona automaticamente `/api` para `http://localhost:8080`.

---

## Credenciais padrão

| Usuário | Senha      | Perfil |
|---------|-----------|--------|
| admin   | Admin@1234 | ADMIN  |

---

## Endpoints principais da API

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/auth/login` | Autenticação — retorna token JWT |
| POST | `/api/auth/register` | Registra novo usuário (ADMIN) |
| GET | `/api/employees` | Lista funcionários com paginação e filtros |
| POST | `/api/employees` | Cria funcionário (ADMIN, MANAGER) |
| PUT | `/api/employees/{id}` | Atualiza funcionário (ADMIN, MANAGER) |
| PATCH | `/api/employees/{id}/deactivate` | Desativa funcionário (ADMIN) |
| GET | `/api/departments` | Lista departamentos |
| POST | `/api/departments` | Cria departamento (ADMIN) |
| DELETE | `/api/departments/{id}` | Remove departamento (ADMIN) |
| GET | `/api/vacations` | Lista férias por status (ADMIN, MANAGER) |
| POST | `/api/vacations/employee/{id}` | Solicita férias |
| PATCH | `/api/vacations/{id}/approve` | Aprova solicitação (ADMIN, MANAGER) |
| PATCH | `/api/vacations/{id}/reject` | Rejeita solicitação (ADMIN, MANAGER) |

Documentação completa disponível no Swagger UI após subir o backend.

---

## Testes

```bash
cd backend
mvn test
```

Os testes cobrem as camadas de service e controller com JUnit 5 e Mockito, usando banco H2 em memória isolado para não afetar o banco de desenvolvimento.

---

## Decisões de Arquitetura

**Monorepo** — backend e frontend no mesmo repositório facilitam o versionamento conjunto e o histórico de commits relacionados entre as duas camadas.

**Stateless** — nenhuma sessão HTTP é mantida no servidor. O token JWT carrega todas as informações de autenticação necessárias, o que facilita escalabilidade horizontal.

**Soft delete de funcionários** — funcionários são desativados em vez de deletados, preservando histórico de férias e referências em outras tabelas.

**Hibernate com LAZY loading** — todos os relacionamentos usam `FetchType.LAZY` com `JOIN FETCH` explícito nas queries que precisam dos dados, evitando o problema N+1.

**Angular Signals** — o `AuthService` usa `signal()` e `computed()` do Angular 17 para estado reativo, eliminando a necessidade de `async pipe` e `BehaviorSubject` nos componentes.

**Componentes standalone** — nenhum `NgModule` no frontend. Cada componente declara suas próprias dependências, facilitando o lazy loading e reduzendo o bundle inicial.

**Flyway desabilitado em desenvolvimento** — o schema é criado diretamente pelo script SQL para simplificar o setup inicial. Em produção, recomenda-se habilitar o Flyway para versionamento automático das migrations.
