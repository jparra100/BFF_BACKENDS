# Instrucciones de ejecución y pruebas

## Requisitos

- Java 17 o superior.
- PowerShell para la ejecución local en Windows.
- Docker Desktop para levantar la plataforma completa.
- Maven no requiere instalación adicional porque el proyecto incluye Maven Wrapper.

Antes de comenzar, verifique Java desde una terminal:

```powershell
java -version
```

## 1. Preparar la configuración local

Desde la raíz del proyecto, defina las contraseñas de los usuarios locales y de los certificados:

```powershell
$env:WEB_AUTH_PASSWORD = "<clave-web>"
$env:ATM_AUTH_PASSWORD = "<clave-cajero>"
$env:MOBILE_AUTH_PASSWORD = "<clave-movil>"

$env:WEB_SSL_KEYSTORE_PASSWORD = "<clave-certificado-web>"
$env:ATM_SSL_KEYSTORE_PASSWORD = "<clave-certificado-cajero>"
$env:MOBILE_SSL_KEYSTORE_PASSWORD = "<clave-certificado-movil>"

$bytesJwt = New-Object byte[] 48
[Security.Cryptography.RandomNumberGenerator]::Fill($bytesJwt)
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytesJwt)
```

El valor de `JWT_SECRET_BASE64` debe ser el mismo para los tres BFF. Los usuarios locales son `web-user`, `atm-user` y `mobile-user`.

Genere una vez los certificados autofirmados de desarrollo:

```powershell
.\scripts\generar-certificado.ps1 -Servicio web
.\scripts\generar-certificado.ps1 -Servicio atm
.\scripts\generar-certificado.ps1 -Servicio mobile
```

## 2. Ejecutar los BFF de forma independiente

Abra una terminal para cada canal y conserve las variables de entorno correspondientes:

```powershell
.\mvnw.cmd -pl bff-web spring-boot:run
.\mvnw.cmd -pl bff-atm spring-boot:run
.\mvnw.cmd -pl bff-mobile spring-boot:run
```

Cada aplicación puede trabajar con los archivos CSV incluidos cuando las llamadas remotas están deshabilitadas. Los certificados locales son autofirmados; en `curl.exe` se puede utilizar `-k` durante las pruebas.

## 3. Obtener un token local

Ejemplo para el canal Web:

```http
POST https://localhost:8441/api/auth/token
Content-Type: application/json

{
  "username": "web-user",
  "password": "<clave-web>"
}
```

El token tiene una duración de 15 minutos y se envía en las solicitudes protegidas:

```http
Authorization: Bearer <token>
```

Use `atm-user` en el puerto 8443 y `mobile-user` en el puerto 8442. Un token de otro canal debe responder con `403 Forbidden`; un token ausente o inválido debe responder con `401 Unauthorized`.

## 4. Probar los endpoints principales

### Canal Web

- `GET /api/web/cuentas?tipo=&pagina=0&tamanio=20`
- `GET /api/web/cuentas/{cuentaId}`
- `GET /api/web/cuentas/{cuentaId}/movimientos?pagina=0&tamanio=20`
- `GET /api/web/transacciones?tipo=&desde=&hasta=&pagina=0&tamanio=20`

### Canal Cajero

- `GET /api/atm/cuentas/{cuentaId}/saldo`
- `POST /api/atm/cuentas/{cuentaId}/retiros`

Cuerpo de ejemplo para un retiro:

```json
{
  "monto": 1000
}
```

### Canal Móvil

- `GET /api/mobile/cuentas/{cuentaId}/resumen`
- `GET /api/mobile/cuentas/{cuentaId}/movimientos-recientes?limite=5`

El límite de movimientos debe estar entre 1 y 10.

## 5. Ejecutar la plataforma completa

Con Docker Desktop iniciado, ejecute:

```powershell
docker compose up --build -d
```

Los accesos principales son:

- API Gateway: `http://localhost:8080`
- Keycloak: `http://localhost:8180`
- Eureka: `http://localhost:8761`
- Config Server: `http://localhost:8888`

Los usuarios distribuidos de desarrollo son:

| Canal | Usuario | Contraseña | Rol |
|---|---|---|---|
| Web | `web-user` | `web123` | `WEB` |
| Cajero | `atm-user` | `atm123` | `ATM` |
| Móvil | `mobile-user` | `mobile123` | `MOBILE` |

Solicite un token a Keycloak y utilice el valor `access_token` en las llamadas al Gateway:

```bash
curl -X POST "http://localhost:8180/realms/banco-xyz/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=bank-channels" \
  -d "username=web-user" \
  -d "password=web123" \
  -d "grant_type=password"
```

## 6. Ejecutar los procesos batch

Los tres jobs procesan transacciones diarias, intereses mensuales y estados de cuenta anuales. Cada step utiliza chunks de 100 registros, reintentos para fallos transitorios y cuatro workers para procesamiento paralelo.

Con la plataforma levantada:

```powershell
docker compose --profile batch run --rm batch-processing
```

Los CSV se montan en modo lectura y los resultados se almacenan en la base `batch` de PostgreSQL.

## 7. Ejecutar las pruebas automatizadas

Desde la raíz del repositorio:

```powershell
.\mvnw.cmd clean test
```

La ejecución debe finalizar con `BUILD SUCCESS`. También se puede validar la sintaxis de la orquestación con:

```powershell
docker compose config --quiet
```

## 8. Detener el ambiente

```powershell
docker compose down
```

Para eliminar además los datos locales de PostgreSQL:

```powershell
docker compose down -v
```
