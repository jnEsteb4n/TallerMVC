package com.icesi.alphabackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Se puede ejecutar con mvn spring-boot:run o desplegar como WAR en Tomcat.
 * Al desplegarse en Tomcat se activa el perfil "war" (ver application-war.yml).
 */
@SpringBootApplication
public class AlphaBackendApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(AlphaBackendApplication.class).profiles("war");
    }

    public static void main(String[] args) {
        SpringApplication.run(AlphaBackendApplication.class, args);
    }
}
