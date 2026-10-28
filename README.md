# Conectar Laboulaye - Backend API

Servicio de backend para Conectar Laboulaye, una plataforma municipal orientada a la gestión de reclamos urbanos y difusión de alertas para la ciudad de Laboulaye, Córdoba.

Este repositorio corresponde al Trabajo Final Integrador del 2° Semestre de la carrera de Desarrollo de Software.

## Qué hace el sistema
El backend provee una API REST encargada de:
- Autenticación y gestión de usuarios con seguridad basada en roles (Vecino y Municipio).
- Creación, seguimiento e historial de estados de reclamos vecinales.
- Emisión, actualización y cierre de alertas municipales (cortes de calles, emergencias, avisos).
- Administración de zonas urbanas y categorías de reclamos.
- Envío y control de notificaciones dirigidas al vecino.
- Generación de métricas generales de gestión para el panel administrativo.

## Tecnologías utilizadas
- Java 17
- Spring Boot 3
- Spring Security & JWT (HS256)
- Spring Data JPA / Hibernate
- PostgreSQL (Base de datos en Supabase)
- Maven

## Variables de entorno
Para ejecutar el proyecto localmente es necesario configurar las siguientes variables de entorno en el archivo src/main/resources/application.properties o como variables de ambiente del sistema:

SPRING_DATASOURCE_URL=jdbc:postgresql://<HOST>:<PUERTO>/<NOMBRE_BD>
SPRING_DATASOURCE_USERNAME=<USUARIO_BD>
SPRING_DATASOURCE_PASSWORD=<PASSWORD_BD>
JWT_SECRET=<CLAVE_SECRETA_PARA_FIRMA_JWT>

## Cómo correr el proyecto en local

1. Clonar el repositorio:
   git clone https://github.com/lucioreinaudi/ProyectoFinalIntegrador2doSemestre_Back.git
   cd ProyectoFinalIntegrador2doSemestre_Back

2. Configurar la base de datos PostgreSQL con las credenciales correspondientes.

3. Ejecutar el proyecto mediante Maven:
   ./mvnw spring-boot:run

El servidor iniciará por defecto en el puerto 8080 (http://localhost:8080).

## Links de deploy y documentación de API
- API Deployada (Render / Supabase): https://proyectofinalintegrador2dosemestre-back.onrender.com (o la URL de producción configurada)

## Usuarios de prueba

### Usuario Municipio (Administrador)
- Email: municipio@laboulaye.gob.ar
- Rol: municipio
- Nota: La cuenta de municipio se genera de manera directa en la base de datos mediante script SQL.

## Pruebas automáticas

Las pruebas usan **JUnit 5 + MockMvc + spring-security-test** y corren contra una base **H2 en memoria** (modo PostgreSQL), así que **no tocan Supabase** ni necesitan variables de entorno: la configuración está en `src/test/resources/application-test.properties` (perfil `test`).

### Cómo correrlas

Desde la carpeta del proyecto:

```bash
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

O desde IntelliJ: panel **Maven → demo → Lifecycle → test**, o clic derecho sobre `src/test/java` → **Run 'All Tests'**.

> Si Maven no encuentra Java, configurá `JAVA_HOME` apuntando a un JDK 17 o superior (sirve el `jbr` que trae IntelliJ).

Los resultados quedan en `target/surefire-reports/`.

### Qué se prueba

| Clase | Prueba | Resultado esperado |
|---|---|---|
| `SeguridadTest` | `loginCorrectoDevuelveToken` | `POST /api/auth/login` con credenciales válidas → **200** y un token JWT |
| `SeguridadTest` | `loginConPasswordIncorrectaDa401` | Contraseña incorrecta → **401** |
| `SeguridadTest` | `sinTokenDa401` | `GET /api/reclamos/me` sin token → **401** |
| `SeguridadTest` | `vecinoEnZonaDeAdminDa403` | `GET /api/admin/usuarios` con token de vecino → **403** |
| `SeguridadTest` | `municipioEnZonaDeAdminDa200` | `GET /api/admin/usuarios` con token de municipio → **200** |
| `DemoApplicationTests` | `contextLoads` | El contexto de Spring levanta con el perfil `test` |

Cada prueba crea sus propios datos (un usuario municipio y vecinos registrados por la API), la base se crea y se borra en cada corrida.

### Usuario Vecino
- Email: vecino.demo@email.com
- Rol: vecino
- Nota: Se puede registrar cualquier usuario nuevo desde el formulario público del frontend.
