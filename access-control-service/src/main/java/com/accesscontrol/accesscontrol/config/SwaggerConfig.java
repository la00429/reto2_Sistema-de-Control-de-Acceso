package com.accesscontrol.accesscontrol.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI accessControlServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Access Control Service API")
                        .description("API para control de acceso de empleados del Sistema de Control de Acceso Peatonal")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sistema de Control de Acceso")
                                .email("support@accesscontrol.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}



