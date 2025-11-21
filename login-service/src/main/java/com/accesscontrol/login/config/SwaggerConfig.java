package com.accesscontrol.login.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI loginServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Login Service API")
                        .description("API para autenticación y registro de usuarios del Sistema de Control de Acceso Peatonal")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sistema de Control de Acceso")
                                .email("support@accesscontrol.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}



