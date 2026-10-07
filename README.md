# Banco XYZ - BFF Backends

Plataforma backend para los canales Web, Cajero y Móvil de Banco XYZ. Cada BFF entrega sólo la información que su canal necesita y consume servicios centrales de cuentas, pagos y clientes.

## Servicios

| Módulo | Puerto local | Responsabilidad |
|---|---:|---|
| `bff-web` | 8441 | Consultas completas de cuentas, movimientos y transacciones |
| `bff-atm` | 8443 | Consulta de saldo y retiros |
| `bff-mobile` | 8442 | Resumen de cuenta y movimientos recientes optimizados |
| `account-service` | 8081 | Administración de cuentas y saldos |
| `payment-service` | 8082 | Registro de pagos, transferencias y retiros |
| `customer-service` | 8083 | Administración de clientes |
| `api-gateway` | 8080 | Entrada única y enrutamiento |
| `config-server` | 8888 | Configuración centralizada |
| `discovery-server` | 8761 | Registro y descubrimiento de servicios |
| `batch-processing` | — | Procesos diarios, mensuales y anuales |

Los servicios son aplicaciones Spring Boot independientes. En modo local, cada BFF puede ejecutarse con los datos CSV incluidos. En modo distribuido, los BFF consultan los servicios centrales mediante Eureka y Spring Cloud LoadBalancer.

## Requisitos

- Java 17
- PowerShell para generar los certificados locales
- Docker Desktop para ejecutar la plataforma completa
- No es necesario instalar Maven: el repositorio incluye Maven Wrapper

Verifique que `JAVA_HOME` apunte a Java 17 antes de ejecutar los comandos.

## Configuración local

Los servicios requieren contraseñas de acceso, una clave JWT compartida y una contraseña para cada certificado. Defina las variables en las terminales donde iniciará los servicios:

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

`JWT_SECRET_BASE64` debe tener el mismo valor en los tres procesos. Los usuarios locales predeterminados son `web-user`, `atm-user` y `mobile-user`; las contraseñas siempre se reciben desde variables de entorno.

Genere una vez los certificados autofirmados de desarrollo:

```powershell
.\scripts\generar-certificado.ps1 -Servicio web
.\scripts\generar-certificado.ps1 -Servicio atm
.\scripts\generar-certificado.ps1 -Servicio mobile
```

Los archivos PKCS12 se guardan dentro de cada módulo y están excluidos de Git.

## Ejecución

Abra una terminal por servicio, conserve en cada una las variables correspondientes y ejecute desde la raíz del repositorio:

```powershell
.\mvnw.cmd -pl bff-web spring-boot:run
.\mvnw.cmd -pl bff-atm spring-boot:run
.\mvnw.cmd -pl bff-mobile spring-boot:run
```

Los certificados son autofirmados. Para llamadas locales con `curl.exe`, utilice `-k`.

## Ejecución distribuida

Para levantar la plataforma completa:

```powershell
docker compose up --build -d
```

El Gateway queda disponible en `http://localhost:8080` y Keycloak en `http://localhost:8180`. La configuración incluye PostgreSQL, Kafka, Eureka y Config Server. Los detalles de acceso, usuarios y ejecución batch están en [docs/DESPLIEGUE.md](docs/DESPLIEGUE.md).

## Autenticación

En ejecución local, cada BFF expone el mismo punto de entrada en su propio puerto:

```http
POST /api/auth/token
Content-Type: application/json

{
  "username": "web-user",
  "password": "<clave-web>"
}
```

La respuesta contiene un token Bearer válido por 15 minutos. Envíelo en las llamadas protegidas:

```http
Authorization: Bearer <token>
```

En el despliegue distribuido los tokens son emitidos por Keycloak. Un token válido para otro canal recibe `403 Forbidden`; un token ausente, vencido o inválido recibe `401 Unauthorized`.

## Endpoints

### Web

- `GET /api/web/cuentas?tipo=&pagina=0&tamanio=20`
- `GET /api/web/cuentas/{cuentaId}`
- `GET /api/web/cuentas/{cuentaId}/movimientos?pagina=0&tamanio=20`
- `GET /api/web/transacciones?tipo=&desde=&hasta=&pagina=0&tamanio=20`

### Cajero

- `GET /api/atm/cuentas/{cuentaId}/saldo`
- `POST /api/atm/cuentas/{cuentaId}/retiros`

Cuerpo de un retiro:

```json
{
  "monto": 1000
}
```

En modo distribuido, cada retiro se registra en Pagos y actualiza Cuentas mediante eventos Kafka.

### Móvil

- `GET /api/mobile/cuentas/{cuentaId}/resumen`
- `GET /api/mobile/cuentas/{cuentaId}/movimientos-recientes?limite=5`

El límite de movimientos puede estar entre 1 y 10. Las respuestas móviles omiten datos que no se utilizan en ese canal.

## Datos legacy

Los archivos CSV se cargan desde `src/main/resources/data`. Durante la carga se descartan filas incompletas, montos inválidos y duplicados. Los procesos batch guardan en PostgreSQL las transacciones diarias, el cálculo mensual de intereses y los estados de cuenta anuales.

## Verificación

Para ejecutar todas las pruebas automatizadas:

```powershell
.\mvnw.cmd clean test
```

La compilación completa debe finalizar con todos los módulos en estado `SUCCESS`.
