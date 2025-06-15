# HabitJourney Backend

API REST desarrollada con Spring Boot para la aplicación HabitJourney, proporcionando servicios de autenticación y gestión de usuarios.

## 🚀 Tecnologías

- **Java 21**
- **Spring Boot 3.x**
- **Spring Security + JWT**
- **PostgreSQL**
- **Maven**
- **Docker & Docker Compose** (opcional)
- **Swagger/OpenAPI** para documentación

## 📋 Requisitos Previos

- JDK 21 o superior
- Maven 3.8+
- PostgreSQL 14+
- Git

## 🔧 Instalación y Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/Alejandro-Araujo/habitjourney-backend.git
cd habitjourney-backend
```

### 2. Configurar la base de datos

#### Opción A: Usar Docker Compose (Recomendado)

El proyecto incluye un `docker-compose.yml` con PostgreSQL preconfigurado:

```bash
# Iniciar PostgreSQL con Docker Compose
docker-compose up -d

# La BD estará disponible en localhost:5433
# Database: habitjourney_backend
# User: postgres
# Password: postgres
```

#### Opción B: PostgreSQL local

Si tienes PostgreSQL instalado localmente:

```sql
CREATE DATABASE habitjourney_backend;
```

### 3. Configurar variables de entorno

Crear un archivo `.env.properties` en la raíz del proyecto:

```properties
# Database Configuration (para Docker Compose)
DB_HOST=localhost
DB_PORT=5433
DB_NAME=habitjourney_backend
DB_USERNAME=postgres
DB_PASSWORD=postgres

# JWT Configuration
JWT_SECRET=tu_clave_secreta_muy_segura_de_al_menos_256_bits
JWT_EXPIRATION=86400000

# Server Configuration
SERVER_PORT=8080
```

### 4. Instalar dependencias y ejecutar

```bash
# Instalar dependencias
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

## 🐳 Docker

### Docker Compose (Solo PostgreSQL)

El `docker-compose.yml` incluido levanta únicamente PostgreSQL:

```bash
# Iniciar PostgreSQL
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener
docker-compose down

# Detener y eliminar volúmenes (borra datos)
docker-compose down -v
```

### Ejecutar la API con Docker

Para ejecutar la API en un contenedor Docker local:

```bash
# Construir imagen
docker build -t habitjourney-backend .

# Ejecutar conectando a PostgreSQL del docker-compose
docker run -d -p 8080:8080 \
  --network host \
  -e DB_HOST=localhost \
  -e DB_PORT=5433 \
  -e DB_NAME=habitjourney_backend \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e JWT_SECRET=tu_clave_secreta \
  --name habitjourney-api \
  habitjourney-backend
```

**Nota:** El Dockerfile está optimizado para Render (expone puerto 10000), pero funciona perfectamente en local.

## 📚 Documentación API

La documentación Swagger está disponible en:
- **Local:** `http://localhost:8080/swagger-ui.html`
- **Producción:** `https://habitjourney-backend.onrender.com/swagger-ui.html`

### Endpoints principales

#### Autenticación
- `POST /api/auth/register` - Registrar nuevo usuario
- `POST /api/auth/login` - Iniciar sesión

#### Usuario
- `GET /api/users/me` - Obtener información del usuario actual
- `PUT /api/users/me` - Actualizar información del usuario
- `DELETE /api/users/me` - Eliminar cuenta
- `POST /api/users/me/change-password` - Cambiar contraseña

## 🧪 Testing

Ejecutar todos los tests:

```bash
mvn test
```

Ejecutar con reporte de cobertura:

```bash
mvn test jacoco:report
```

## 🏗️ Estructura del Proyecto

```
src/main/java/backend/
├── auth/                  # Autenticación y autorización
├── user/                  # Gestión de usuarios
├── common/               
│   ├── config/           # Configuraciones (Security, Swagger, etc.)
│   ├── exception/        # Excepciones personalizadas
│   ├── security/         # JWT y filtros de seguridad
│   └── util/            # Utilidades
└── HabitjourneyBackendApplication.java
```

## 🚀 Despliegue

### Producción (Render.com)

El backend ya está desplegado y operativo en producción:

- **URL Base:** `https://habitjourney-backend.onrender.com`
- **API Docs:** `https://habitjourney-backend.onrender.com/swagger-ui.html`
- **Base de datos:** PostgreSQL gestionado por Render

### Desplegar tu propia instancia en Render

1. Fork este repositorio
2. Crear cuenta en [Render.com](https://render.com)
3. Nuevo Web Service → Conectar repositorio GitHub
4. Configurar:
    - Environment: `Docker`
    - Build Command: `docker build -t habitjourney-backend .`
    - Start Command: `docker run -p $PORT:8080 habitjourney-backend`
5. Añadir las variables de entorno necesarias
6. Deploy automático en cada push a `main`

## 🔐 Seguridad

- Autenticación mediante JWT
- Contraseñas hasheadas con BCrypt
- Validación de entrada en todos los endpoints
- CORS configurado para el cliente Android
- Rate limiting implementado

## 🔐 Seguridad

- Autenticación mediante JWT
- Contraseñas hasheadas con BCrypt
- Validación de entrada en todos los endpoints
- CORS configurado para el cliente Android
- Rate limiting implementado

## 🤝 Contribuir

1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## 📝 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## 👥 Autor

- **[Alejandro Araujo Fernández]** - [GitHub](https://github.com/Alejandro-Araujo/habitjourney-backend)

## 🙏 Agradecimientos

- Proyecto desarrollado como Proyecto Final del FP del Desarrollo de Aplicaciones Multiplataforma (DAM) 2025
- Inspirado en la necesidad de una app unificada de productividad personal