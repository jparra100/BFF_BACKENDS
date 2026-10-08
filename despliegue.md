# Despliegue de la plataforma

## Despliegue local con Docker Compose

La orquestación incluye PostgreSQL, Kafka, Keycloak, Config Server, Eureka, API Gateway, los tres microservicios de negocio y los BFF Web, Cajero y Móvil.

Desde la raíz del proyecto:

```bash
docker compose up --build -d
```

Verifique el estado de los contenedores:

```bash
docker compose ps
```

Puntos de acceso:

- API Gateway: `http://localhost:8080`
- Keycloak: `http://localhost:8180`
- Eureka: `http://localhost:8761`
- Config Server: `http://localhost:8888`

Los trabajos batch se ejecutan a demanda mediante el perfil `batch`:

```bash
docker compose --profile batch run --rm batch-processing
```

Para detener la plataforma:

```bash
docker compose down
```

## Preparación de imágenes

El Dockerfile recibe el módulo que se debe compilar. En un registro de imágenes se recomienda publicar una etiqueta por versión:

```bash
docker build --build-arg MODULE=account-service -t banco-xyz/account-service:1.0.0 .
docker build --build-arg MODULE=payment-service -t banco-xyz/payment-service:1.0.0 .
docker build --build-arg MODULE=customer-service -t banco-xyz/customer-service:1.0.0 .
```

El mismo procedimiento se aplica a Gateway, Config Server, Eureka y los tres BFF.

## Propuesta de despliegue en AWS

### 1. Red y acceso

1. Crear una VPC con al menos dos zonas de disponibilidad.
2. Ubicar el Application Load Balancer en subredes públicas.
3. Ejecutar los servicios en subredes privadas.
4. Permitir acceso externo únicamente por HTTPS.

### 2. Registro y ejecución de contenedores

1. Crear repositorios en Amazon ECR para cada módulo.
2. Etiquetar y publicar las imágenes.
3. Crear un clúster de Amazon ECS con Fargate.
4. Definir un servicio ECS para Gateway, los BFF y los microservicios.
5. Configurar al menos dos tareas para los servicios críticos en producción.

### 3. Servicios administrados

- Amazon RDS para PostgreSQL, con una base lógica para cuentas, pagos, clientes y batch.
- Amazon MSK para Kafka y sus tópicos `pagos.realizados` y `alertas.seguridad`.
- AWS Secrets Manager para contraseñas, claves JWT y credenciales de base de datos.
- AWS Certificate Manager para el certificado HTTPS del balanceador.
- Amazon CloudWatch para logs, métricas y alarmas.

### 4. Variables y seguridad

Las tareas ECS deben recibir las direcciones de PostgreSQL, Kafka, Eureka, Config Server y Keycloak mediante variables de entorno. Las contraseñas no se incluyen en las imágenes y se obtienen desde Secrets Manager.

Los grupos de seguridad deben permitir únicamente la comunicación necesaria entre el balanceador, los servicios y las bases de datos. Los endpoints de administración no deben quedar expuestos públicamente.

### 5. Escalabilidad horizontal

Cada BFF y microservicio puede aumentar su número de tareas sin mantener sesión local. Se recomienda configurar Auto Scaling usando CPU, memoria o cantidad de solicitudes. Eureka y Spring Cloud LoadBalancer distribuyen las llamadas entre las instancias disponibles.

Kafka permite aumentar consumidores dentro de un grupo. Para aprovechar varias instancias, el número de particiones del tópico debe ser igual o superior al número de consumidores activos.

### 6. Proceso batch en la nube

El módulo `batch-processing` puede ejecutarse como una tarea programada de ECS mediante Amazon EventBridge. Las entradas se pueden obtener desde un volumen controlado o desde Amazon S3 y los resultados se almacenan en RDS.

### 7. Verificación posterior

1. Confirmar que los servicios aparecen registrados en Eureka.
2. Consultar los endpoints de salud con Actuator.
3. Solicitar un token y probar una operación por cada BFF.
4. Verificar la publicación y consumo de eventos Kafka.
5. Ejecutar el proceso batch y revisar sus estados en PostgreSQL.
6. Revisar logs, métricas y alarmas en CloudWatch.
