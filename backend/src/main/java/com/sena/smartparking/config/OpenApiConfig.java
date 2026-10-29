package com.sena.smartparking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de la documentación OpenAPI 3 / Swagger UI.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Parking API - Sistema de Gestión de Parqueaderos")
                        .version("1.0.0")
                        .description("Servicios Web RESTful para el control de ingresos, liquidación de tarifas, "
                                + "recaudos, comprobantes y facturación electrónica del sistema Smart Parking.")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo ADSO - Grupo 6")
                                .email("contacto@smartparking.sena.edu.co"))
                        .license(new License()
                                .name("Uso Académico SENA ADSO - Ficha 3336134")));
    }
}
