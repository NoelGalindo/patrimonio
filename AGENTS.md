# AGENTS.md

Municipal asset-management system (Patrimonio) for Xochitepec, MX. Git root is this folder. Frontend in `frontend/` (React), backend in `backend/patrimonio/` (Spring Boot + PostgreSQL). The `.xlsx`/`.docx` files in the root are reference templates (`Estructura patrimonio.xlsx` defines the data model; `VALE DE TRASPASO 2026.docx` is the transfer voucher the backend reproduces as PDF) — don't modify them.

## Commands

Frontend (run from `frontend/`):
- Dev server: `npm run dev` (proxies `/api` to `http://localhost:8080`; override with `BACKEND_URL=http://localhost:XXXX`)
- Build: `npm run build` · Lint: `npm run lint` (must stay clean)
- No frontend tests and no TypeScript: do not add a typecheck step.

Backend (run from `backend/patrimonio/`, needs **JDK 17+**; the machine's system `JAVA_HOME` is Temurin JDK 21, with an old JDK 8 also installed):
- Run: `./mvnw spring-boot:run` with env vars `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (≥32 chars), and on first start `ADMIN_EMAIL`/`ADMIN_PASSWORD` to seed the first administrator.
- Tests: `./mvnw test` — integration tests use Testcontainers (PostgreSQL), so **Docker must be running**; without Docker they are skipped, not failed.
- If Maven fails with `PKIX path building failed`, set `MAVEN_OPTS=-Djavax.net.ssl.trustStoreType=Windows-ROOT`.

Deploy (test server): `docker compose up -d --build` from the repo root with a `.env` (see `.env.example`, `DESPLIEGUE.md`). Services: `db` (Postgres 17, volume `pgdata`), `backend`, `web` (nginx serving `frontend/dist`, proxying `/api` with `Origin` stripped — same reason as the Vite proxy). Only `web` publishes a port. On this dev PC, Docker builds fail with `PKIX` because Avast intercepts HTTPS; that doesn't happen on the server.

## Architecture

Backend (Spring Boot 4.1, Java 17, package `com.xochitepec.patrimonio`):
- Schema is owned by Flyway (`src/main/resources/db/migration/V*.sql`); Hibernate runs with `ddl-auto=validate`. Add a new `V{n}__*.sql` for every schema change — never edit an applied migration.
- Security: stateless JWT (HS256, `spring-boot-starter-security-oauth2-resource-server`). `POST /api/auth/login` issues the token; `JwtUsuarioConverter` reloads role/area from the DB on every request, so role changes and deactivation apply immediately.
- Roles (`RolNombre`): `ADMINISTRADOR` (all users, catalogs, signers, `/api/admin/**`; read-only on assets), `PATRIMONIO` (asset CRUD, authorizes transfers, **also manages areas and `USUARIO`-role users** via `/api/admin/areas/**` and `/api/admin/usuarios/**`), `USUARIO` (read-only, **own area only**; can request transfers).
- `UsuarioService` scopes Patrimonio: it only lists/sees `USUARIO` users (others → 404) and can only assign the `USUARIO` role (else 403). Direcciones generales and `/api/admin/parametros` stay admin-only.
- Area isolation is enforced in the service layer (`BienService`, `TraspasoService`) via `CurrentUser`, never in controllers or the frontend. Resources from another area return 404, not 403.
- Write endpoints use `@PreAuthorize`; `GlobalExceptionHandler` maps `AccessDeniedException` to 403 (without it they become 500).
- Asset types share `BienBase` (area, soft-delete `activo`, audit) and the generic `BienService`.
- Transfers (`traspaso/`): USUARIO requests → PATRIMONIO authorizes/rejects. Authorizing moves the assets, snapshots their data into `solicitud_traspaso_detalle`, and assigns a per-year folio (`FolioService`, atomic upsert). `ValeTraspasoPdfService` (OpenPDF) renders the voucher on demand from the snapshot; signers come from `parametro_sistema`.

Frontend (React 19 + Vite 8, MUI v9, react-router v7):
- `src/api/client.js` (fetch wrapper, Bearer token, Spanish error messages from the backend) and `src/api/endpoints.js`.
- Auth: `src/auth/AuthProvider.jsx` + `useAuth()`; token in `localStorage` (`patrimonio.token`), revalidated with `/api/auth/me` on load. `src/routes/ProtectedRoute.jsx` guards routes by role (navigation only — the backend is the real authority).
- Data fetching: `src/hooks/useFetch.js` (`{ data, error, cargando, recargar }`).
- Asset fields for forms/tables/detail are declared once in `src/bienes/tiposBien.js` — add or rename fields there, matching the backend DTOs.
- Brand theme in `src/theme.js` (with `responsiveFontSizes`) — extend it, don't hardcode colors in pages.
- Responsive (the app is used on phones): list data with `src/components/TablaResponsiva.jsx` (table on desktop, cards below 600 px), page titles/actions with `EncabezadoPagina.jsx`, and open dialogs with `fullScreen={useEsMovil()}`. Use responsive `sx` values (`{ xs, sm, md }`) instead of fixed widths.

## Gotchas

- Vite runs with `host: true` (LAN access at `http://<machine-ip>:5173`) and its `/api` proxy strips the browser `Origin` header. Without that, Spring's CORS filter answers `403 Invalid CORS request` to any origin other than `app.cors.allowed-origins` (default `http://localhost:5173`). Opening port 5173 in Windows Firewall needs an admin shell.
- `src/index.css` must stay minimal: the Vite starter CSS it used to contain (dark-mode variables, `h1`/`h2` colors) made dialog titles unreadable on phones in dark mode.
- UI text, comments, and route labels are in **Spanish** — match that in new UI copy and API error messages.
- MUI v9: use `Grid size={{ xs, md }}` (not `item xs`) and `slotProps` (not `InputProps`/`primaryTypographyProps`/`PaperProps`); the old props are silently ignored. Some icon names differ (e.g. `DeleteOutlined`, not `DeleteOutline`).
- `react-hooks` v7 rules forbid synchronous `setState` inside effects; reset form state by remounting with a `key` (see `Altas.jsx`, `ConfirmDialog`) instead of syncing in an effect.
- `react-refresh/only-export-components`: a `.jsx` file may only export components; put contexts/constants/hooks in `.js` files (see `src/auth/`).
