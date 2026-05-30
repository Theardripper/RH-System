# HR System — Frontend

Interface web do Sistema de Gestão de Recursos Humanos, desenvolvida com Angular 17 utilizando componentes standalone, Signals reativos e lazy loading em todos os módulos. Consome a API REST do backend Spring Boot com autenticação JWT.

---

## Tecnologias

- **Angular 17** — componentes standalone, Signals, Control Flow (`@if`, `@for`)
- **TypeScript 5.4** — tipagem estrita em toda a aplicação
- **RxJS 7** — requisições HTTP, debounce na busca, forkJoin para requisições paralelas
- **Angular Router** — lazy loading por feature module, guards funcionais
- **Reactive Forms** — validação de formulários com feedback visual
- **SCSS** — design tokens via CSS Variables, estilos encapsulados por componente

---

## Funcionalidades

- Login e registro com autenticação JWT persistida no `localStorage`
- Controle de acesso por perfil (ADMIN, MANAGER, EMPLOYEE) via guards e Signals
- Dashboard com estatísticas em tempo real (funcionários, departamentos, férias pendentes)
- Listagem de funcionários com busca global, filtro por departamento, ordenação e paginação
- CRUD completo de funcionários com formulário de criação e edição compartilhado
- Visualização detalhada do funcionário com histórico de férias
- Ativar e desativar funcionários (soft delete) diretamente na listagem
- CRUD de departamentos com modal de confirmação de exclusão
- Gestão de solicitações de férias com filtro por status e aprovação/rejeição inline
- Formulário de solicitação de férias com cálculo de dias em tempo real
- Sidebar colapsável com navegação filtrada por role
- Interceptor HTTP que injeta o token JWT automaticamente em todas as requisições
- Redirecionamento automático para login em respostas 401

---

## Estrutura do Projeto

```
frontend/
├── src/
│   ├── app/
│   │   ├── core/
│   │   │   ├── guards/
│   │   │   │   └── auth.guard.ts         # authGuard, guestGuard, roleGuard
│   │   │   ├── interceptors/
│   │   │   │   └── jwt.interceptor.ts    # Injeta Bearer token + trata 401
│   │   │   ├── models/
│   │   │   │   └── index.ts              # Interfaces TypeScript (Employee, Department, etc)
│   │   │   └── services/
│   │   │       ├── auth.service.ts       # Login, logout, Signals reativos
│   │   │       ├── employee.service.ts
│   │   │       ├── department.service.ts
│   │   │       └── vacation.service.ts
│   │   ├── features/
│   │   │   ├── auth/
│   │   │   │   ├── login/                # Tela de login
│   │   │   │   ├── register/             # Tela de registro (ADMIN)
│   │   │   │   └── auth.routes.ts
│   │   │   ├── dashboard/                # Cards de estatísticas
│   │   │   ├── employees/
│   │   │   │   ├── list/                 # Listagem com paginação e filtros
│   │   │   │   ├── form/                 # Formulário criar/editar
│   │   │   │   ├── detail/               # Detalhes + histórico de férias
│   │   │   │   └── employees.routes.ts
│   │   │   ├── departments/
│   │   │   │   ├── list/                 # Listagem com modal de exclusão
│   │   │   │   ├── form/                 # Formulário criar/editar
│   │   │   │   └── departments.routes.ts
│   │   │   └── vacations/
│   │   │       ├── list/                 # Listagem com tabs de status
│   │   │       ├── form/                 # Formulário de solicitação
│   │   │       └── vacations.routes.ts
│   │   ├── shared/
│   │   │   └── components/
│   │   │       └── layout/               # Sidebar + topbar + router-outlet
│   │   ├── app.component.ts
│   │   ├── app.config.ts                 # Providers: router, http, animations
│   │   └── app.routes.ts                 # Rotas raiz com lazy loading
│   ├── environments/
│   │   ├── environment.ts                # apiUrl: '/api' (dev com proxy)
│   │   └── environment.production.ts
│   ├── index.html
│   ├── main.ts
│   └── styles.scss                       # Design tokens + utilitários globais
├── angular.json
├── proxy.conf.json                       # Proxy /api → localhost:8080
├── tsconfig.json                         # Paths: @core/*, @features/*, @shared/*, @env/*
└── package.json
```

---

## Como Executar

### Pré-requisitos

- Node.js 20+
- npm 10+
- Backend do HR System rodando em `http://localhost:8080`

### 1. Clonar o repositório

```bash
git clone https://github.com/seu-usuario/hr-system.git
cd hr-system/frontend
```

### 2. Instalar dependências

```bash
npm install
```

### 3. Rodar em desenvolvimento

```bash
npm start
```

O frontend estará disponível em `http://localhost:4200`.

O arquivo `proxy.conf.json` redireciona automaticamente todas as chamadas `/api` para `http://localhost:8080`, eliminando problemas de CORS em desenvolvimento.

### 4. Build para produção

```bash
npm run build
```

Os arquivos de build ficam em `dist/hr-system-frontend/`.

---

## Credenciais padrão

| Usuário | Senha       | Perfil |
|---------|-------------|--------|
| admin   | Admin@1234  | ADMIN  |

---

## Perfis de acesso

| Funcionalidade | ADMIN | MANAGER | EMPLOYEE |
|----------------|-------|---------|----------|
| Ver funcionários | ✅ | ✅ | ✅ |
| Criar/editar funcionários | ✅ | ✅ | ❌ |
| Ativar/desativar funcionários | ✅ | ❌ | ❌ |
| Ver departamentos | ✅ | ✅ | ✅ |
| Criar/editar/excluir departamentos | ✅ | ❌ | ❌ |
| Ver todas as férias | ✅ | ✅ | ❌ |
| Aprovar/rejeitar férias | ✅ | ✅ | ❌ |
| Solicitar férias | ✅ | ✅ | ✅ |
| Registrar usuários | ✅ | ❌ | ❌ |

---

## Decisões de Arquitetura

**Signals em vez de BehaviorSubject** — o `AuthService` usa `signal()` e `computed()` do Angular 17 para estado reativo, o que elimina a necessidade de `async pipe` nos templates e torna o código mais simples e previsível.

**Componentes standalone** — nenhum `NgModule` no projeto. Cada componente declara suas próprias dependências via `imports: []`, facilitando o lazy loading e reduzindo o bundle inicial.

**Guards funcionais** — `authGuard`, `guestGuard` e `roleGuard` são funções puras que usam `inject()`, sem necessidade de classes ou `providedIn`. O `roleGuard` é uma factory que recebe os roles permitidos e retorna o guard já configurado.

**Interceptor funcional** — o `jwtInterceptor` é uma função `HttpInterceptorFn`, registrada diretamente no `provideHttpClient(withInterceptors([...]))` sem classe intermediária.

**Lazy loading em todos os módulos** — as rotas usam `loadComponent` e `loadChildren` em todos os níveis, garantindo que apenas o código necessário para cada tela seja carregado.

**Proxy de desenvolvimento** — o `proxy.conf.json` evita configuração de CORS no backend durante o desenvolvimento. Em produção, o Angular serve os arquivos estáticos e o Nginx (ou outro servidor) roteia `/api` para o Spring Boot.

**Design tokens via CSS Variables** — todas as cores, espaçamentos, bordas e sombras são definidos em `:root` no `styles.scss` e reutilizados em todos os componentes, facilitando futuras mudanças de tema.
