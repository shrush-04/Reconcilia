package com.reconcilia.service;

import com.reconcilia.dto.IngestionResult;
import com.reconcilia.entity.SourceBank;
import com.reconcilia.entity.Transaction;
import com.reconcilia.entity.TransactionStatus;
import com.reconcilia.exception.TransactionValidationException;
import com.reconcilia.parser.BankStatementParser;
import com.reconcilia.parser.BankStatementParserFactory;
import com.reconcilia.repository.TransactionRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Orchestrates the full CSV ingestion pipeline:
 * <ol>
 *   <li>Select the correct parser via {@link BankStatementParserFactory}.</li>
 *   <li>Parse the file into {@link Transaction} objects.</li>
 *   <li>Validate every row with Bean Validation — fail-fast with all errors.</li>
 *   <li>Detect duplicates in a single DB query (by referenceNumber).</li>
 *   <li>Bulk-save non-duplicate rows and return an {@link IngestionResult}.</li>
 * </ol>
 */
@Service
@Transactional
public class StatementIngestionService {

    private static final Logger log = LoggerFactory.getLogger(StatementIngestionService.class);

    private final BankStatementParserFactory parserFactory;
    private final TransactionRepository transactionRepository;
    private final Validator validator;

    public StatementIngestionService(
            BankStatementParserFactory parserFactory,
            TransactionRepository transactionRepository,
            Validator validator) {
        this.parserFactory = parserFactory;
        this.transactionRepository = transactionRepository;
        this.validator = validator;
    }

    /**
     * Parses, validates, deduplicates and persists the uploaded CSV file.
     *
     * @param file the uploaded multipart CSV file
     * @param bank the bank format to use for parsing
     * @return summary of the ingestion (saved count + duplicate count)
     * @throws IOException if the file stream cannot be read
     */
    public IngestionResult ingest(MultipartFile file, SourceBank bank) throws IOException {
        log.info("Starting ingestion: bank={}, file={}, size={}",
                bank, file.getOriginalFilename(), file.getSize());

        // 1. Parse
        BankStatementParser parser = parserFactory.getParser(bank);
        List<Transaction> transactions = parser.parse(file);
        log.debug("Parsed {} rows from {}", transactions.size(), file.getOriginalFilename());

        // 2. Validate every row; collect all violations before throwing
        validateAll(transactions);

        // 3. Bulk duplicate detection — single SELECT query for all incoming refs
        Set<String> incomingRefs = transactions.stream()
                .map(Transaction::getReferenceNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> existingRefs = incomingRefs.isEmpty()
                ? Collections.emptySet()
                : transactionRepository.findReferenceNumbersIn(incomingRefs);

<<<<<<< HEAD
        // 4. Partition: skip duplicate reference numbers that already exist in the database,
        // but allow duplicates within the same batch to be persisted so that reconciliation can process them.
        List<Transaction> toSave = new ArrayList<>();
=======
        // 4. Partition: skip duplicates (both DB-existing and intra-batch duplicates)
        List<Transaction> toSave = new ArrayList<>();
        Set<String> processedRefs = new HashSet<>();
>>>>>>> e815eb3 (solve the backend doker rleated error)
        int duplicateCount = 0;

        for (Transaction tx : transactions) {
            String ref = tx.getReferenceNumber();
<<<<<<< HEAD
            if (existingRefs.contains(ref)) {
                log.debug("Skipping duplicate referenceNumber={}", ref);
                duplicateCount++;
            } else {
                tx.setStatus(TransactionStatus.PENDING);
=======
            if (existingRefs.contains(ref) || processedRefs.contains(ref)) {
                log.debug("Skipping duplicate referenceNumber={}", ref);
                duplicateCount++;
            } else {
                processedRefs.add(ref);
                tx.setStatus(TransactionStatus.PROCESSED);
>>>>>>> e815eb3 (solve the backend doker rleated error)
                toSave.add(tx);
            }
        }

        // 5. Bulk save
        transactionRepository.saveAll(toSave);
        log.info("Ingestion complete: saved={}, duplicates={}", toSave.size(), duplicateCount);

        return new IngestionResult(bank.name(), file.getOriginalFilename(), toSave.size(), duplicateCount);
    }

    /**
     * Validates all transactions using Bean Validation. Collects every violation
     * across all rows before throwing so the caller receives the complete error list.
     *
     * @throws TransactionValidationException if any row fails validation
     */
    private void validateAll(List<Transaction> transactions) {
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            int rowNum = i + 1;
            Set<ConstraintViolation<Transaction>> violations = validator.validate(transactions.get(i));
            violations.forEach(v ->
                errors.add("Row " + rowNum + " [" + v.getPropertyPath() + "]: " + v.getMessage())
            );
        }

        if (!errors.isEmpty()) {
            throw new TransactionValidationException(errors);
        }
    }
}
