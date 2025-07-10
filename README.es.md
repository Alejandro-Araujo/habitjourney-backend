# HabitJourney Backend

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green?style=for-the-badge&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue?style=for-the-badge&logo=postgresql)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)

[Read it in English](README.md)

API REST desarrollada con Java y Spring Boot para la gestión de usuarios y autenticación.

---

## 📖 Acerca de este Proyecto

Este backend fue desarrollado originalmente como el sistema de autenticación y gestión de usuarios para la aplicación Android **HabitJourney**, como parte de mi Proyecto Final de DAM (Desarrollo de Aplicaciones Multiplataforma).

Posteriormente, y como ejercicio de mejora técnica, la aplicación cliente fue desacoplada de este backend y migrada a **Firebase Authentication**.

Por lo tanto, este repositorio se mantiene como una **demostración de concepto (Proof of Concept)** funcional que ilustra la creación de una API REST con las siguientes características:

* Arquitectura orientada a servicios
* Autenticación de usuarios basada en JWT (JSON Web Tokens)
* Gestión de ciclo de vida de usuario (CRUD, cambio de contraseña)
* Configuración para despliegue en contenedores Docker

## 📑 Tabla de Contenidos
- [Tecnologías](#-tecnologías)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación y Configuración](#-instalación-y-configuración)
- [Docker](#-docker)
- [Documentación de la API](#-documentación-de-la-api)
- [Estructura del Proyecto](#️-estructura-del-proyecto)
- [Despliegue](#-despliegue)
- [Seguridad](#-seguridad)
- [Testing](#-testing)
- [Licencia](#-licencia)
- [Contacto](#-contacto)

## 🚀 Tecnologías

* **Lenguaje y Framework:** Java 21, Spring Boot 3.x
* **Seguridad:** Spring Security, JWT
* **Base de Datos:** PostgreSQL, Spring Data JPA
* **Gestión de Dependencias:** Maven
* **Contenerización:** Docker & Docker Compose
* **Documentación API:** Swagger (Springdoc OpenAPI)

## 📋 Requisitos Previos

* JDK 21 o superior
* Maven 3.8+
* PostgreSQL 14+
* Git
* Docker (Opcional, recomendado para la base de datos)

## 🔧 Instalación y Configuración

### 1. Clonar el repositorio
```bash
git clone https://github.com/Alejandro-Araujo/habitjourney-backend.git
cd habitjourney-backend
```

### 2. Configurar la base de datos

#### Opción A: Docker Compose (Recomendado)
El proyecto incluye un docker-compose.yml que levanta un servicio de PostgreSQL configurado.

```bash
# Iniciar el contenedor de PostgreSQL en segundo plano
docker-compose up -d
```

La base de datos estará disponible en localhost:5433.

#### Opción B: Instancia Local de PostgreSQL
Si prefieres usar una instalación local, crea la base de datos:

```sql
CREATE DATABASE habitjourney_backend;
```

### 3. Configurar variables de entorno
Crea un archivo `.env.properties` en la raíz del proyecto a partir del fichero `env.properties.example` y ajústalo con tu configuración.

```properties
# Configuración de la Base de Datos
DB_HOST=localhost
DB_PORT=5433
DB_NAME=habitjourney_backend
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Configuración de JWT
JWT_SECRET=clave_secreta_para_desarrollo_local_con_suficiente_longitud
JWT_EXPIRATION=86400000

# Configuración del Servidor
SERVER_PORT=8080
```

### 4. Ejecutar la aplicación
```bash
# Instalar dependencias
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run
```

La API estará disponible en http://localhost:8080.

## 🐳 Docker

Para ejecutar la aplicación completa dentro de un contenedor Docker (asumiendo que la base de datos ya está corriendo):

```bash
# 1. Construir la imagen Docker de la aplicación
docker build -t habitjourney-backend .

# 2. Ejecutar el contenedor de la API
docker run -p 8080:8080 \
  --network="host" \
  -e DB_HOST=localhost \
  -e DB_PORT=5433 \
  -e DB_NAME=habitjourney_backend \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e JWT_SECRET=tu_clave_secreta \
  --name habitjourney-api \
  habitjourney-backend
```

**Nota:** `--network="host"` se usa para facilitar la conexión a la base de datos que corre en el localhost de la máquina anfitriona.

## 📚 Documentación de la API

La documentación de la API, generada con Swagger, está disponible en la siguiente ruta una vez que la aplicación está en marcha:

**http://localhost:8080/swagger-ui.html**

### Endpoints Principales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/register` | Registra un nuevo usuario |
| POST | `/api/auth/login` | Autentica a un usuario y devuelve un JWT |
| GET | `/api/users/me` | Obtiene los datos del usuario autenticado |
| PUT | `/api/users/me` | Actualiza los datos del usuario |
| POST | `/api/users/me/change-password` | Permite al usuario cambiar su contraseña |
| DELETE | `/api/users/me` | Elimina la cuenta del usuario |

## 🏗️ Estructura del Proyecto

```
src/main/java/backend/
├── auth/          # Lógica de autenticación (Controller, DTOs, Service)
├── user/          # Lógica de gestión de usuarios (Controller, Entity, Repo, Service)
└── common/        # Componentes transversales
    ├── config/    # Configuración de Spring (Security, OpenAPI, etc.)
    ├── exception/ # Manejadores de excepciones globales
    └── security/  # Implementación de JWT (Filtros, Provider, etc.)
```

## 🚀 Despliegue

### Producción (Render.com)

El backend ya está desplegado y operativo en producción:

- **URL Base:** `https://habitjourney-backend.onrender.com`
- **API Docs:** `https://habitjourney-backend.onrender.com/swagger-ui.html`

**Nota:** Esta demo funciona con el plan gratuito de Render. Es posible que el servicio tarde un poco en arrancar si lleva tiempo inactivo. Además, la base de datos asociada tiene una vida útil limitada y no se puede garantizar su disponibilidad.

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

Se han implementado las siguientes medidas de seguridad:

* **Autenticación:** Sistema basado en JSON Web Tokens (JWT) para proteger los endpoints
* **Hashing de Contraseñas:** Se utiliza BCryptPasswordEncoder para almacenar las contraseñas de forma segura
* **Validación de Entradas:** Los DTOs (Data Transfer Objects) validan los datos de entrada para prevenir datos malformados
* **Configuración CORS:** Se ha configurado una política de Cross-Origin Resource Sharing para permitir peticiones desde orígenes específicos

## 🧪 Testing

El proyecto cuenta con tests unitarios y de integración para asegurar el correcto funcionamiento de los controladores y servicios.

Para ejecutar la suite de tests:

```bash
mvn test
```

Para generar el reporte de cobertura (requiere JaCoCo):

```bash
mvn test jacoco:report
```

## 📝 Licencia

Este proyecto está distribuido bajo la Licencia MIT. Consulta el archivo LICENSE para más detalles.

## 📞 Contacto

**Alejandro Araujo Fernández**

* Email: jandroaraujo@gmail.com
* LinkedIn: https://www.linkedin.com/in/alejandro-araujo-fernandez/
* GitHub: https://github.com/Alejandro-Araujo