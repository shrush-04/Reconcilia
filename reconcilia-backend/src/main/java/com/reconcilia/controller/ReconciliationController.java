package com.reconcilia.controller;

import com.reconcilia.dto.ReconciliationSummary;
import com.reconcilia.entity.Transaction;
import com.reconcilia.entity.TransactionStatus;
import com.reconcilia.repository.TransactionRepository;
import com.reconcilia.service.ReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing endpoints for running transaction reconciliation
 * and querying classified transactions.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Transaction Reconciliation", description = "Reconcile, match, and query transactions")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;
    private final TransactionRepository transactionRepository;

    public ReconciliationController(ReconciliationService reconciliationService,
                                    TransactionRepository transactionRepository) {
        this.reconciliationService = reconciliationService;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping("/reconciliation/run")
    @Operation(
            summary = "Run the reconciliation engine",
            description = "Triggers the reconciliation algorithm over all persisted transactions. " +
                          "Detects duplicates and matches transactions across banks within the specified date-tolerance window. " +
                          "Updates the status of all transactions to MATCHED, DUPLICATE, or UNMATCHED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reconciliation run completed successfully",
                    content = @Content(schema = @Schema(implementation = ReconciliationSummary.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content)
    })
    public ResponseEntity<ReconciliationSummary> runReconciliation(
            @Parameter(description = "Date tolerance window in days to match transactions across sources", example = "1")
            @RequestParam(value = "daysTolerance", defaultValue = "1") int daysTolerance
    ) {
        ReconciliationSummary summary = reconciliationService.runReconciliation(daysTolerance);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/reconciliation/matched")
    @Operation(
            summary = "Retrieve all matched transactions",
            description = "Returns a list of all transactions that have been successfully matched across sources."
    )
    @ApiResponse(responseCode = "200", description = "List of matched transactions",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Transaction.class))))
    public ResponseEntity<List<Transaction>> getMatchedTransactions() {
        List<Transaction> transactions = transactionRepository.findByStatus(TransactionStatus.MATCHED);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/reconciliation/unmatched")
    @Operation(
            summary = "Retrieve all unmatched transactions",
            description = "Returns a list of all transactions that could not be matched and are not duplicates."
    )
    @ApiResponse(responseCode = "200", description = "List of unmatched transactions",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Transaction.class))))
    public ResponseEntity<List<Transaction>> getUnmatchedTransactions() {
        List<Transaction> transactions = transactionRepository.findByStatus(TransactionStatus.UNMATCHED);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/reconciliation/duplicates")
    @Operation(
            summary = "Retrieve all duplicate transactions",
            description = "Returns a list of all transactions identified as duplicate (same amount, date, and reference appearing more than once)."
    )
    @ApiResponse(responseCode = "200", description = "List of duplicate transactions",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Transaction.class))))
    public ResponseEntity<List<Transaction>> getDuplicateTransactions() {
        List<Transaction> transactions = transactionRepository.findByStatus(TransactionStatus.DUPLICATE);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/{accountId}")
    @Operation(
            summary = "Retrieve all transactions for a specific account",
            description = "Returns a list of all normalized transactions associated with the given account ID."
    )
    @ApiResponse(responseCode = "200", description = "List of transactions for the account",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Transaction.class))))
    public ResponseEntity<List<Transaction>> getTransactionsByAccountId(
            @Parameter(description = "Account ID to retrieve transactions for", required = true, example = "ACC001")
            @PathVariable("accountId") String accountId
    ) {
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);
        return ResponseEntity.ok(transactions);
    }
}
