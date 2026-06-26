package com.reconcilia.integration;

import com.reconcilia.AbstractIntegrationTest;
import com.reconcilia.dto.IngestionResult;
import com.reconcilia.entity.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests for the statement ingestion pipeline.
 *
 * <p>Each test hits the live {@code POST /api/statements/upload} endpoint via
 * {@link org.springframework.boot.test.web.client.TestRestTemplate} against a real
 * PostgreSQL 16 database managed by Testcontainers. Flyway migrations run on startup,
 * producing an identical schema to production. No mocks, no H2.
 *
 * <p>The database is wiped before each test via {@code AbstractIntegrationTest#cleanDatabase()}.
 */
@DisplayName("Statement Ingestion — Integration Tests")
class StatementIngestionIT extends AbstractIntegrationTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Happy-path: Alpha Bank
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Upload Alpha CSV → all 10 rows persisted, 0 duplicates")
    void uploadAlphaCsv_shouldPersistAllRows() {
        ResponseEntity<IngestionResult> response = uploadCsv("csv/alpha_sample.csv", "ALPHA");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().savedCount()).isEqualTo(10);
        assertThat(response.getBody().duplicateCount()).isEqualTo(0);
        assertThat(response.getBody().bank()).isEqualTo("ALPHA");
        assertThat(transactionRepository.count()).isEqualTo(10);
    }

    @Test
    @DisplayName("Alpha CSV debit rows → stored with negative amount")
    void uploadAlphaCsv_debitRowsHaveNegativeAmount() {
        uploadCsv("csv/alpha_sample.csv", "ALPHA");

        // ALPHA-REF-002: debit=156.75, credit=empty → amount should be -156.75
        Optional<Transaction> tx = transactionRepository.findByReferenceNumber("ALPHA-REF-002");
        assertThat(tx).isPresent();
        assertThat(tx.get().getAmount()).isNegative();
        assertThat(tx.get().getAmount().toPlainString()).startsWith("-156.75");
    }

    @Test
    @DisplayName("Alpha CSV credit rows → stored with positive amount")
    void uploadAlphaCsv_creditRowsHavePositiveAmount() {
        uploadCsv("csv/alpha_sample.csv", "ALPHA");

        // ALPHA-REF-003: credit=4250.00, debit=empty → amount should be +4250.00
        Optional<Transaction> tx = transactionRepository.findByReferenceNumber("ALPHA-REF-003");
        assertThat(tx).isPresent();
        assertThat(tx.get().getAmount()).isPositive();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Happy-path: Beta Bank
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Upload Beta CSV → all 10 rows persisted, 0 duplicates")
    void uploadBetaCsv_shouldPersistAllRows() {
        ResponseEntity<IngestionResult> response = uploadCsv("csv/beta_sample.csv", "BETA");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().savedCount()).isEqualTo(10);
        assertThat(response.getBody().duplicateCount()).isEqualTo(0);
        assertThat(response.getBody().bank()).isEqualTo("BETA");
        assertThat(transactionRepository.count()).isEqualTo(10);
    }

    @Test
    @DisplayName("Beta CSV DR rows → stored with negative amount")
    void uploadBetaCsv_drRowsHaveNegativeAmount() {
        uploadCsv("csv/beta_sample.csv", "BETA");

        // BETA-REF-002: amount=210.30, type=DR → stored as -210.30
        Optional<Transaction> tx = transactionRepository.findByReferenceNumber("BETA-REF-002");
        assertThat(tx).isPresent();
        assertThat(tx.get().getAmount()).isNegative();
        assertThat(tx.get().getAmount().toPlainString()).startsWith("-210.30");
    }

    @Test
    @DisplayName("Beta CSV CR rows → stored with positive amount")
    void uploadBetaCsv_crRowsHavePositiveAmount() {
        uploadCsv("csv/beta_sample.csv", "BETA");

        // BETA-REF-001: amount=5500.00, type=CR → stored as +5500.00
        Optional<Transaction> tx = transactionRepository.findByReferenceNumber("BETA-REF-001");
        assertThat(tx).isPresent();
        assertThat(tx.get().getAmount()).isPositive();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Duplicate detection
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Upload same Alpha CSV twice → second upload: 0 saved, 10 duplicates")
    void uploadSameAlphaCsvTwice_secondUploadShouldAllBeDuplicates() {
        uploadCsv("csv/alpha_sample.csv", "ALPHA");

        ResponseEntity<IngestionResult> secondResponse = uploadCsv("csv/alpha_sample.csv", "ALPHA");

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondResponse.getBody()).isNotNull();
        assertThat(secondResponse.getBody().savedCount()).isEqualTo(0);
        assertThat(secondResponse.getBody().duplicateCount()).isEqualTo(10);
        // DB still has exactly the original 10 rows
        assertThat(transactionRepository.count()).isEqualTo(10);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Error cases
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Unknown bank identifier → 400 Bad Request")
    void uploadWithUnknownBank_shouldReturn400() {
        ResponseEntity<String> response = uploadCsvRaw("csv/alpha_sample.csv", "GAMMA_BANK");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Unknown bank");
    }

    @Test
    @DisplayName("Bank identifier is case-insensitive (alpha → ALPHA)")
    void uploadWithLowercaseBankParam_shouldSucceed() {
        ResponseEntity<IngestionResult> response = uploadCsv("csv/alpha_sample.csv", "alpha");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().savedCount()).isEqualTo(10);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private ResponseEntity<IngestionResult> uploadCsv(String classpathResource, String bank) {
        HttpEntity<MultiValueMap<String, Object>> request = buildRequest(classpathResource);
        return restTemplate.postForEntity(
                "/api/statements/upload?bank=" + bank,
                request,
                IngestionResult.class
        );
    }

    private ResponseEntity<String> uploadCsvRaw(String classpathResource, String bank) {
        HttpEntity<MultiValueMap<String, Object>> request = buildRequest(classpathResource);
        return restTemplate.postForEntity(
                "/api/statements/upload?bank=" + bank,
                request,
                String.class
        );
    }

    private HttpEntity<MultiValueMap<String, Object>> buildRequest(String classpathResource) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource(classpathResource));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(body, headers);
    }
}
