package com.alejandro.habitjourney.backend.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Clase de configuración para Swagger/OpenAPI 3.
 * Define la información general de la API, los servidores disponibles
 * y los esquemas de seguridad (como JWT Bearer Token) para la documentación interactiva.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Habit Journey Backend API",
                description = "API del backend para la aplicación Habit Journey. " +
                        "Proporciona endpoints para autenticación (registro, login) y gestión de usuarios.",
                version = "1.0",
                contact = @Contact(
                        name = "Alejandro",
                        email = "tu_email@example.com",
                        url = "tu_url_o_perfil (opcional)"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = { // Define los servidores (URLs base) donde la API está desplegada
                @Server(
                        url = "http://localhost:8080", // URL para desarrollo local
                        description = "Servidor de desarrollo local"
                ),
                // @Server( // Ejemplo para un futuro entorno de staging o producción
                //     url = "https://api.habitjourney.staging.com",
                //     description = "Servidor de Staging"
                // ),
                // @Server(
                //     url = "https://api.habitjourney.com",
                //     description = "Servidor de Producción"
                // )
        }

)
// Define el esquema de seguridad 'bearerAuth' que se referenciará en los controllers
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Authentication. Ingresa tu token JWT en el formato 'Bearer {token}'"
)
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("Autenticación")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("Usuario")
                .pathsToMatch("/api/users/**")
                .build();
    }

}