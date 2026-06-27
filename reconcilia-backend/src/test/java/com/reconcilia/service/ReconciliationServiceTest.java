package com.reconcilia.service;

import com.reconcilia.dto.ReconciliationSummary;
import com.reconcilia.entity.SourceBank;
import com.reconcilia.entity.Transaction;
import com.reconcilia.entity.TransactionStatus;
import com.reconcilia.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReconciliationServiceTest {

    private TransactionRepository transactionRepository;
    private ReconciliationService reconciliationService;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        reconciliationService = new ReconciliationService(transactionRepository);
    }

    @Test
    @DisplayName("Empty database → returns zero stats")
    void emptyDatabase_shouldReturnZeroStats() {
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        ReconciliationSummary summary = reconciliationService.runReconciliation(1);

        assertThat(summary.totalProcessed()).isZero();
        assertThat(summary.matchedCount()).isZero();
        assertThat(summary.unmatchedCount()).isZero();
        assertThat(summary.duplicateCount()).isZero();

        verify(transactionRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Identical transactions → detected as DUPLICATE")
    void duplicateDetection_shouldMarkDuplicates() {
        Transaction tx1 = createTransaction("ACC001", LocalDate.of(2024, 1, 15), new BigDecimal("100.00"), "REF-001", SourceBank.ALPHA);
        Transaction tx2 = createTransaction("ACC001", LocalDate.of(2024, 1, 15), new BigDecimal("100.00"), "REF-001", SourceBank.ALPHA);

        when(transactionRepository.findAll()).thenReturn(List.of(tx1, tx2));

        ReconciliationSummary summary = reconciliationService.runReconciliation(1);

        assertThat(summary.totalProcessed()).isEqualTo(2);
        assertThat(summary.duplicateCount()).isEqualTo(2);
        assertThat(summary.matchedCount()).isZero();
        assertThat(summary.unmatchedCount()).isZero();

        assertThat(tx1.getStatus()).isEqualTo(TransactionStatus.DUPLICATE);
        assertThat(tx2.getStatus()).isEqualTo(TransactionStatus.DUPLICATE);
    }

    @Test
    @DisplayName("Transactions across sources within date-tolerance → matched successfully")
    void crossSourceMatching_shouldMatchWithinTolerance() {
        // ALPHA: debit 100.00 on Jan 15 (stored as -100.00)
        Transaction txAlpha = createTransaction("ACC001", LocalDate.of(2024, 1, 15), new BigDecimal("-100.00"), "ALPHA-REF", SourceBank.ALPHA);
        // BETA: credit 100.00 on Jan 16 (stored as 100.00)
        Transaction txBeta = createTransaction("ACC002", LocalDate.of(2024, 1, 16), new BigDecimal("100.00"), "BETA-REF", SourceBank.BETA);

        when(transactionRepository.findAll()).thenReturn(List.of(txAlpha, txBeta));

        // tolerance = 1 day -> they should match
        ReconciliationSummary summary = reconciliationService.runReconciliation(1);

        assertThat(summary.totalProcessed()).isEqualTo(2);
        assertThat(summary.matchedCount()).isEqualTo(2);
        assertThat(summary.unmatchedCount()).isZero();
        assertThat(summary.duplicateCount()).isZero();

        assertThat(txAlpha.getStatus()).isEqualTo(TransactionStatus.MATCHED);
        assertThat(txBeta.getStatus()).isEqualTo(TransactionStatus.MATCHED);
    }

    @Test
    @DisplayName("Transactions across sources exceeding date-tolerance → UNMATCHED")
    void crossSourceMatching_shouldNotMatchIfExceedingTolerance() {
        Transaction txAlpha = createTransaction("ACC001", LocalDate.of(2024, 1, 15), new BigDecimal("-100.00"), "ALPHA-REF", SourceBank.ALPHA);
        Transaction txBeta = createTransaction("ACC002", LocalDate.of(2024, 1, 17), new BigDecimal("100.00"), "BETA-REF", SourceBank.BETA);

        when(transactionRepository.findAll()).thenReturn(List.of(txAlpha, txBeta));

        // tolerance = 1 day -> they should NOT match (difference is 2 days)
        ReconciliationSummary summary = reconciliationService.runReconciliation(1);

        assertThat(summary.totalProcessed()).isEqualTo(2);
        assertThat(summary.matchedCount()).isZero();
        assertThat(summary.unmatchedCount()).isEqualTo(2);
        assertThat(summary.duplicateCount()).isZero();

        assertThat(txAlpha.getStatus()).isEqualTo(TransactionStatus.UNMATCHED);
        assertThat(txBeta.getStatus()).isEqualTo(TransactionStatus.UNMATCHED);
    }

    @Test
    @DisplayName("1-to-1 matching: matches closest date first")
    void crossSourceMatching_shouldMatch1to1ClosestDate() {
        Transaction txAlpha = createTransaction("ACC001", LocalDate.of(2024, 1, 15), new BigDecimal("100.00"), "ALPHA-REF", SourceBank.ALPHA);
        Transaction txBeta1 = createTransaction("ACC002", LocalDate.of(2024, 1, 16), new BigDecimal("-100.00"), "BETA-REF-1", SourceBank.BETA);
        Transaction txBeta2 = createTransaction("ACC002", LocalDate.of(2024, 1, 15), new BigDecimal("-100.00"), "BETA-REF-2", SourceBank.BETA);

        when(transactionRepository.findAll()).thenReturn(List.of(txAlpha, txBeta1, txBeta2));

        ReconciliationSummary summary = reconciliationService.runReconciliation(1);

        assertThat(summary.totalProcessed()).isEqualTo(3);
        assertThat(summary.matchedCount()).isEqualTo(2);
        assertThat(summary.unmatchedCount()).isEqualTo(1);

        // txAlpha (Jan 15) matches txBeta2 (Jan 15) because it has diff 0, vs txBeta1 (Jan 16) has diff 1
        assertThat(txAlpha.getStatus()).isEqualTo(TransactionStatus.MATCHED);
        assertThat(txBeta2.getStatus()).isEqualTo(TransactionStatus.MATCHED);
        assertThat(txBeta1.getStatus()).isEqualTo(TransactionStatus.UNMATCHED);
    }

    private Transaction createTransaction(String accountId, LocalDate date, BigDecimal amount, String ref, SourceBank bank) {
        Transaction tx = new Transaction();
        tx.setAccountId(accountId);
        tx.setDate(date);
        tx.setAmount(amount);
        tx.setReferenceNumber(ref);
        tx.setSourceBank(bank);
        tx.setStatus(TransactionStatus.PENDING);
        return tx;
    }
}
