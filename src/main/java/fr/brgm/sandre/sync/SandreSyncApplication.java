package fr.brgm.sandre.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SandreSyncApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(
                SpringApplication.run(SandreSyncApplication.class, args)));
    }
}
