package com.reconcilia.integration;

import com.reconcilia.AbstractIntegrationTest;
import com.reconcilia.dto.IngestionResult;
import com.reconcilia.dto.ReconciliationSummary;
import com.reconcilia.entity.Transaction;
import com.reconcilia.entity.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Transaction Reconciliation — Integration Tests")
class ReconciliationIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("Upload statements → Run reconciliation → Verify classification endpoints")
    void fullUploadAndReconciliationFlow() {
        // 1. Upload Alpha CSV (4 rows: 1 match candidate, 2 duplicates, 1 unmatched candidate)
        ResponseEntity<IngestionResult> alphaResponse = uploadCsv("csv/recon_alpha.csv", "ALPHA");
        assertThat(alphaResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(alphaResponse.getBody()).isNotNull();
        assertThat(alphaResponse.getBody().savedCount()).isEqualTo(4);

        // 2. Upload Beta CSV (2 rows: 1 match candidate, 1 unmatched candidate)
        ResponseEntity<IngestionResult> betaResponse = uploadCsv("csv/recon_beta.csv", "BETA");
        assertThat(betaResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(betaResponse.getBody()).isNotNull();
        assertThat(betaResponse.getBody().savedCount()).isEqualTo(2);

        // Total 6 transactions persisted, all should be PENDING
        assertThat(transactionRepository.count()).isEqualTo(6);
        List<Transaction> pendingTx = transactionRepository.findByStatus(TransactionStatus.PENDING);
        assertThat(pendingTx).hasSize(6);

        // 3. Run reconciliation with tolerance = 1 day
        ResponseEntity<ReconciliationSummary> runResponse = restTemplate.postForEntity(
                "/api/reconciliation/run?daysTolerance=1",
                null,
                ReconciliationSummary.class
        );
        assertThat(runResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ReconciliationSummary summary = runResponse.getBody();
        assertThat(summary).isNotNull();
        assertThat(summary.totalProcessed()).isEqualTo(6);
        assertThat(summary.matchedCount()).isEqualTo(2); // ALPHA-MATCH-1 and BETA-MATCH-1
        assertThat(summary.duplicateCount()).isEqualTo(2); // The two ALPHA-DUP-1 rows
        assertThat(summary.unmatchedCount()).isEqualTo(2); // ALPHA-UNMATCHED and BETA-UNMATCHED

        // 4. Verify GET /api/reconciliation/matched
        ResponseEntity<List<Transaction>> matchedResponse = getTransactionsList("/api/reconciliation/matched");
        assertThat(matchedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Transaction> matchedList = matchedResponse.getBody();
        assertThat(matchedList).hasSize(2);
        assertThat(matchedList).extracting(Transaction::getReferenceNumber)
                .containsExactlyInAnyOrder("ALPHA-MATCH-1", "BETA-MATCH-1");

        // 5. Verify GET /api/reconciliation/unmatched
        ResponseEntity<List<Transaction>> unmatchedResponse = getTransactionsList("/api/reconciliation/unmatched");
        assertThat(unmatchedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Transaction> unmatchedList = unmatchedResponse.getBody();
        assertThat(unmatchedList).hasSize(2);
        assertThat(unmatchedList).extracting(Transaction::getReferenceNumber)
                .containsExactlyInAnyOrder("ALPHA-UNMATCHED", "BETA-UNMATCHED");

        // 6. Verify GET /api/reconciliation/duplicates
        ResponseEntity<List<Transaction>> duplicatesResponse = getTransactionsList("/api/reconciliation/duplicates");
        assertThat(duplicatesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Transaction> duplicatesList = duplicatesResponse.getBody();
        assertThat(duplicatesList).hasSize(2);
        assertThat(duplicatesList).extracting(Transaction::getReferenceNumber)
                .containsOnly("ALPHA-DUP-1");

        // 7. Verify GET /api/transactions/{accountId}
        ResponseEntity<List<Transaction>> acc001Response = getTransactionsList("/api/transactions/ACC001");
        assertThat(acc001Response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(acc001Response.getBody()).hasSize(4);

        ResponseEntity<List<Transaction>> acc002Response = getTransactionsList("/api/transactions/ACC002");
        assertThat(acc002Response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(acc002Response.getBody()).hasSize(2);
    }

    private ResponseEntity<IngestionResult> uploadCsv(String classpathResource, String bank) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource(classpathResource));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        return restTemplate.postForEntity(
                "/api/statements/upload?bank=" + bank,
                request,
                IngestionResult.class
        );
    }

    private ResponseEntity<List<Transaction>> getTransactionsList(String url) {
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Transaction>>() {}
        );
    }
}
