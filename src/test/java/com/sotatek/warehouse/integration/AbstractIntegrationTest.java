package com.sotatek.warehouse.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES;

    static {
        // Testcontainers 1.21.x shaded docker-java uses "api.version" (not DOCKER_API_VERSION)
        // Docker 29.x MinAPIVersion = 1.40; docker-java defaults to 1.32
        System.setProperty("api.version", "1.41");

        POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("warehouse")
                .withUsername("warehouse")
                .withPassword("warehouse");
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
