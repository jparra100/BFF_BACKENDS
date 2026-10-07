# Despliegue local

La plataforma se puede levantar con Docker Compose. El conjunto incluye PostgreSQL, Kafka, Keycloak, Config Server, Eureka, API Gateway, los tres servicios bancarios y los tres BFF.

## Inicio

```bash
docker compose up --build -d
```

Cuando los servicios terminen de registrarse, los puntos principales quedan disponibles en:

- API Gateway: `http://localhost:8080`
- Keycloak: `http://localhost:8180`
- Eureka: `http://localhost:8761`
- Config Server: `http://localhost:8888`

## Usuarios de desarrollo

| Canal | Usuario | Contraseña | Rol |
|---|---|---|---|
| Web | `web-user` | `web123` | `WEB` |
| Cajero | `atm-user` | `atm123` | `ATM` |
| Móvil | `mobile-user` | `mobile123` | `MOBILE` |

Ejemplo para solicitar un token Web:

```bash
curl -X POST "http://localhost:8180/realms/banco-xyz/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=bank-channels" \
  -d "username=web-user" \
  -d "password=web123" \
  -d "grant_type=password"
```

El valor de `access_token` se envía al Gateway mediante `Authorization: Bearer <token>`.

## Procesos batch

Los tres trabajos batch se ejecutan a demanda con el perfil `batch`:

```bash
docker compose --profile batch run --rm batch-processing
```

Las entradas CSV se montan en modo lectura y los resultados se guardan en la base `batch`.

## Detención

```bash
docker compose down
```

Para borrar también los datos locales de PostgreSQL:

```bash
docker compose down -v
```
