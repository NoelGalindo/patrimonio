# Despliegue de pruebas en el servidor (Docker Compose)

Tres contenedores: **db** (PostgreSQL 17), **backend** (Spring Boot) y **web** (nginx, que sirve el frontend compilado y reenvía `/api` al backend). Solo `web` publica un puerto; la base de datos y el backend no se pueden alcanzar desde internet.

Se supone un servidor Ubuntu/Debian con acceso `ssh root@<IP>` (o un usuario con `sudo`).

## 1. Preparar el servidor (una sola vez)

```bash
# Docker + Compose
curl -fsSL https://get.docker.com | sh

# Firewall del servidor: solo SSH y web
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw enable
```

En el panel de IONOS (Red → Directivas de firewall) deja abiertos solo los puertos **22, 80 y 443**.

## 2. Subir el código

Opción recomendada: repositorio privado (GitHub/GitLab) y en el servidor:

```bash
git clone <url-del-repo> /opt/patrimonio
cd /opt/patrimonio
```

Sin repositorio, desde tu PC (Git Bash, en la carpeta del proyecto) se empaqueta solo el código y se manda por SSH:

```bash
tar czf - --exclude=node_modules --exclude=dist --exclude=target --exclude=.git \
  --exclude=.ds-sync --exclude=ds-bundle --exclude='*.xlsx' --exclude='*.docx' . \
  | ssh root@<IP> "mkdir -p /opt/patrimonio && tar xzf - -C /opt/patrimonio"
```

El mismo comando sirve para actualizar (sobrescribe los archivos; luego `docker compose up -d --build` en el servidor). No toca el `.env` del servidor porque en tu PC no existe.

## 3. Configurar los secretos

```bash
cd /opt/patrimonio
cp .env.example .env
openssl rand -base64 48     # copia el resultado en JWT_SECRET
nano .env                   # llena DB_PASSWORD, JWT_SECRET, ADMIN_EMAIL, ADMIN_PASSWORD
chmod 600 .env
```

Usa contraseñas **nuevas**, distintas a las de tu PC. `.env` está en `.gitignore`: nunca lo subas a git.

## 4. Arrancar

```bash
docker compose up -d --build     # la primera vez tarda unos minutos (descarga y compila)
docker compose ps                # los 3 servicios deben estar "Up"
docker compose logs -f backend   # espera "Tomcat started on port 8080"
```

Abre `http://<IP-del-servidor>` y entra con el `ADMIN_EMAIL`/`ADMIN_PASSWORD` del `.env`. Flyway crea las tablas solo. Después del primer arranque puedes borrar esas dos líneas del `.env`.

## 5. Operación diaria

| Tarea | Comando (en `/opt/patrimonio`) |
|---|---|
| Actualizar a la última versión | `git pull && docker compose up -d --build` |
| Ver logs | `docker compose logs -f backend` (o `web`, `db`) |
| Reiniciar | `docker compose restart` |
| Detener (conserva datos) | `docker compose down` |
| Respaldo de la BD | `docker compose exec -T db pg_dump -U patrimonio patrimonio \| gzip > respaldo-$(date +%F).sql.gz` |
| Restaurar un respaldo | `gunzip -c respaldo.sql.gz \| docker compose exec -T db psql -U patrimonio patrimonio` |

Los contenedores se levantan solos si el servidor se reinicia (`restart: unless-stopped`). Los datos viven en el volumen `pgdata`. **`docker compose down -v` borra la base de datos**: no uses `-v` en el servidor.

Respaldo diario automático (cron, 2:00 a.m., guarda 14 días):

```bash
mkdir -p /opt/respaldos
crontab -e
# agregar:
0 2 * * * cd /opt/patrimonio && docker compose exec -T db pg_dump -U patrimonio patrimonio | gzip > /opt/respaldos/patrimonio-$(date +\%F).sql.gz && find /opt/respaldos -name '*.sql.gz' -mtime +14 -delete
```

## 6. Antes de usarlo con datos reales: HTTPS

Sin HTTPS, contraseñas y tokens viajan sin cifrar. Cuando tengas un dominio o subdominio apuntando a la IP del servidor (registro A), lo más simple es poner **Caddy** delante, que obtiene y renueva el certificado de Let's Encrypt solo. Pide ayuda para ese paso cuando tengas el dominio.

## Notas

- Las pruebas del backend no se corren al compilar la imagen (`-DskipTests`): usan Testcontainers y se corren en desarrollo con `./mvnw test`.
- En la PC de desarrollo, `docker compose build` falla con `PKIX path building failed` porque **Avast** intercepta HTTPS (Web Shield). En el servidor no pasa. Para probar en local hay que desactivar el análisis HTTPS de Avast o compilar el jar y el frontend fuera de Docker.
