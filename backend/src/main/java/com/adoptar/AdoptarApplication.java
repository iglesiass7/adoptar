package com.adoptar;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AdoptarApplication {

    /**
     * Zona horaria de la aplicación. Se fija a mano porque en Windows con hora argentina Java
     * toma el nombre viejo "America/Buenos_Aires", que PostgreSQL 17 rechaza al conectarse.
     * Los tests usan la misma, configurada en el pom (maven-surefire-plugin).
     */
    public static final String ZONA_HORARIA = "America/Argentina/Buenos_Aires";

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZONA_HORARIA));
        SpringApplication.run(AdoptarApplication.class, args);
    }
}
