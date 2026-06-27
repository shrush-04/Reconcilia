package com.reconcilia.service;

import com.reconcilia.dto.ReconciliationSummary;
import com.reconcilia.entity.Transaction;
import com.reconcilia.entity.TransactionStatus;
import com.reconcilia.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service handling bank statement reconciliation.
 * Reconciles transactions by:
 * 1. Identifying duplicates (same amount, date, and reference appearing more than once).
 * 2. Matching remaining transactions across different source banks using a date-tolerance window.
 * 3. Classifying unmatched transactions.
 */
@Service
@Transactional
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);

    private final TransactionRepository transactionRepository;

    public ReconciliationService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Key used to group and identify duplicate transactions in the database.
     */
    private record DuplicateKey(BigDecimal amount, LocalDate date, String referenceNumber) {
        public DuplicateKey {
            // Strip trailing zeros to ensure different scales (e.g. 10.00 vs 10) are treated as equal
            amount = amount != null ? amount.stripTrailingZeros() : null;
        }
    }

    /**
     * Runs the reconciliation algorithm on all transactions in the database.
     *
     * @param daysTolerance the maximum allowed date difference (in days) to match transactions across sources
     * @return summary statistics of the reconciliation run
     */
    public ReconciliationSummary runReconciliation(int daysTolerance) {
        log.info("Running reconciliation with daysTolerance={}", daysTolerance);

        List<Transaction> allTx = transactionRepository.findAll();
        int totalProcessed = allTx.size();

        if (allTx.isEmpty()) {
            return new ReconciliationSummary(0, 0, 0, 0);
        }

        // Reset all statuses to PENDING before running logic
        for (Transaction tx : allTx) {
            tx.setStatus(TransactionStatus.PENDING);
        }

        // 1. Detect duplicates: same amount, date, reference number
        Map<DuplicateKey, List<Transaction>> groups = allTx.stream()
                .collect(Collectors.groupingBy(tx -> new DuplicateKey(tx.getAmount(), tx.getDate(), tx.getReferenceNumber())));

        List<Transaction> candidates = new ArrayList<>();
        int duplicateCount = 0;

        for (Map.Entry<DuplicateKey, List<Transaction>> entry : groups.entrySet()) {
            List<Transaction> group = entry.getValue();
            if (group.size() > 1) {
                // All of these are marked as DUPLICATE
                for (Transaction tx : group) {
                    tx.setStatus(TransactionStatus.DUPLICATE);
                    duplicateCount++;
                }
            } else {
                candidates.add(group.get(0));
            }
        }

        // Sort candidates by date to process chronologically
        candidates.sort(Comparator.comparing(Transaction::getDate));

        int matchedCount = 0;

        // 2. Match across sources: different source banks, matching absolute amount, within date-tolerance window
        for (int i = 0; i < candidates.size(); i++) {
            Transaction t1 = candidates.get(i);
            if (t1.getStatus() == TransactionStatus.MATCHED) {
                continue;
            }

            Transaction bestMatch = null;
            long minDiff = Long.MAX_VALUE;

            for (int j = 0; j < candidates.size(); j++) {
                if (i == j) continue;
                Transaction t2 = candidates.get(j);

                if (t2.getStatus() == TransactionStatus.MATCHED) {
                    continue;
                }
                // Must be different sources
                if (t1.getSourceBank() == t2.getSourceBank()) {
                    continue;
                }
                // Must have the same absolute amount
                if (t1.getAmount().abs().compareTo(t2.getAmount().abs()) != 0) {
                    continue;
                }
                // Check date tolerance
                long diff = Math.abs(ChronoUnit.DAYS.between(t1.getDate(), t2.getDate()));
                if (diff <= daysTolerance) {
                    if (diff < minDiff) {
                        minDiff = diff;
                        bestMatch = t2;
                    }
                }
            }

            if (bestMatch != null) {
                t1.setStatus(TransactionStatus.MATCHED);
                bestMatch.setStatus(TransactionStatus.MATCHED);
                matchedCount += 2;
            }
        }

        // 3. Mark the remaining non-duplicate, non-matched candidates as UNMATCHED
        int unmatchedCount = 0;
        for (Transaction tx : candidates) {
            if (tx.getStatus() != TransactionStatus.MATCHED) {
                tx.setStatus(TransactionStatus.UNMATCHED);
                unmatchedCount++;
            }
        }

        // Persist all classification updates
        transactionRepository.saveAll(allTx);

        log.info("Reconciliation run finished: total={}, matched={}, unmatched={}, duplicates={}",
                totalProcessed, matchedCount, unmatchedCount, duplicateCount);

        return new ReconciliationSummary(totalProcessed, matchedCount, unmatchedCount, duplicateCount);
    }
}
