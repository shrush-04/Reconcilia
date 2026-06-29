package com.reconcilia;

import com.reconcilia.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
<<<<<<< HEAD
import org.springframework.test.context.ActiveProfiles;
=======
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
>>>>>>> e815eb3 (solve the backend doker rleated error)

/**
 * Base class for all integration tests.
 *
<<<<<<< HEAD
 * <h3>H2 In-Memory Database Strategy</h3>
 * <ul>
 *   <li>Uses an in-memory H2 database with PostgreSQL compatibility mode.</li>
 *   <li>The 'test' profile is activated to load H2 datasource configuration.</li>
 *   <li>Flyway runs its migrations against the H2 database on startup, producing the exact
 *       same schema.</li>
 *   <li>Each test method starts with an empty {@code transactions} table, enforced by
 *       the {@code @BeforeEach} cleanup below.</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

=======
 * <h3>Testcontainers strategy</h3>
 * <ul>
 *   <li>The {@code postgres} container is {@code static} — it starts once for the JVM
 *       and is shared across all test classes that extend this base.</li>
 *   <li>{@link ServiceConnection} (Spring Boot 3.1+) automatically wires the container's
 *       JDBC URL, username, and password into the application context. No
 *       {@code @DynamicPropertySource} boilerplate is required.</li>
 *   <li>Flyway runs its migrations against the container on startup, producing the exact
 *       same schema the production database uses.</li>
 *   <li>Each test method starts with an empty {@code transactions} table, enforced by
 *       the {@code @BeforeEach} cleanup below.</li>
 * </ul>
 *
 * <p><strong>No H2 is involved anywhere in this project.</strong>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    /**
     * Shared PostgreSQL 16 container.
     * {@code @ServiceConnection} makes Spring Boot auto-configure the datasource
     * from the container's connection details — no manual property overrides needed.
     */
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

>>>>>>> e815eb3 (solve the backend doker rleated error)
    @Autowired
    protected TransactionRepository transactionRepository;

    @Autowired
    protected TestRestTemplate restTemplate;

    /**
     * Wipes all rows before each test so tests are fully independent.
<<<<<<< HEAD
=======
     * Uses {@code deleteAll()} which is safe because the schema is recreated
     * by Flyway on first context load, not between tests.
>>>>>>> e815eb3 (solve the backend doker rleated error)
     */
    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
    }
}
