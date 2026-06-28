package com.reconcilia;

import com.reconcilia.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for all integration tests.
 *
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

    @Autowired
    protected TransactionRepository transactionRepository;

    @Autowired
    protected TestRestTemplate restTemplate;

    /**
     * Wipes all rows before each test so tests are fully independent.
     */
    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
    }
}
