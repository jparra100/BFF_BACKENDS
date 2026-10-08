# Banco XYZ - BFF Backends

Plataforma backend para modernizar los procesos bancarios de Banco XYZ mediante Spring Boot, Spring Batch y Spring Cloud. La solución separa los servicios de Cuentas, Pagos y Clientes y entrega APIs específicas para los canales Web, Cajero y Móvil.

## Componentes

| Módulo | Puerto | Responsabilidad |
|---|---:|---|
| `api-gateway` | 8080 | Entrada única y enrutamiento |
| `account-service` | 8081 | Administración de cuentas y saldos |
| `payment-service` | 8082 | Pagos, transferencias, depósitos y retiros |
| `customer-service` | 8083 | Administración de clientes |
| `bff-web` | 8441 | Consultas completas, filtros y paginación |
| `bff-mobile` | 8442 | Resúmenes y movimientos recientes |
| `bff-atm` | 8443 | Consulta de saldo y retiros |
| `discovery-server` | 8761 | Registro y descubrimiento con Eureka |
| `config-server` | 8888 | Configuración centralizada |
| `batch-processing` | — | Procesos diarios, mensuales y anuales |

La plataforma utiliza PostgreSQL para persistencia, Kafka para eventos de pagos y alertas, Keycloak para OAuth2/JWT y Resilience4j para controlar fallos en llamadas remotas. Los procesos batch trabajan por bloques y utilizan cuatro workers para procesar chunks en paralelo.

## Documentación

- [Instrucciones de ejecución y pruebas](instrucciones.md)
- [Guía de despliegue local y en AWS](despliegue.md)

## Repositorio

Código fuente disponible en [github.com/jparra100/BFF_BACKENDS](https://github.com/jparra100/BFF_BACKENDS).
