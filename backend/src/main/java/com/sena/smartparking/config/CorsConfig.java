package com.sena.smartparking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de políticas de intercambio de recursos de origen cruzado (CORS).
 * 
 * Permite que las interfaces de usuario web locales (ej. Live Server en puertos 5500 / 3000)
 * puedan consumir los servicios de la API REST sin ser bloqueadas por el navegador,
 * restringiendo el acceso exclusivamente a los orígenes requeridos por el proyecto.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:5500",
                        "http://127.0.0.1:5500",
                        "http://localhost:3000",
                        "http://127.0.0.1:3000",
                        "http://localhost:8080",
                        "http://127.0.0.1:8080"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
