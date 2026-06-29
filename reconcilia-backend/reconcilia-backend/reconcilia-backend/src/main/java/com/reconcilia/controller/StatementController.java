package com.reconcilia.controller;

import com.reconcilia.dto.IngestionResult;
import com.reconcilia.entity.SourceBank;
import com.reconcilia.exception.UnknownBankException;
import com.reconcilia.service.StatementIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST controller exposing the bank-statement ingestion endpoint.
 */
@RestController
@RequestMapping("/api/statements")
@Tag(name = "Bank Statements", description = "Upload and ingest CSV bank statements")
public class StatementController {

    private final StatementIngestionService ingestionService;

    public StatementController(StatementIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Uploads a CSV bank statement and ingests it into the reconciliation database.
     *
     * @param file the CSV file (multipart field name: {@code file})
     * @param bank the bank format identifier ({@code ALPHA} or {@code BETA})
     * @return ingestion summary with saved and duplicate row counts
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload a bank statement CSV",
        description = "Parses the uploaded CSV according to the specified bank format, " +
                      "validates each row, skips duplicate referenceNumbers, and persists " +
                      "new transactions. Returns counts of saved and duplicate rows."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File ingested successfully",
            content = @Content(schema = @Schema(implementation = IngestionResult.class))),
        @ApiResponse(responseCode = "400", description = "Invalid bank, malformed CSV, validation errors, or empty file",
            content = @Content),
        @ApiResponse(responseCode = "500", description = "Unexpected server error",
            content = @Content)
    })
    public ResponseEntity<IngestionResult> upload(
            @Parameter(description = "CSV file to upload", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Bank format (ALPHA or BETA)", required = true, example = "ALPHA")
            @RequestParam("bank") String bank
    ) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty — please provide a non-empty CSV file.");
        }

        SourceBank sourceBank;
        try {
            sourceBank = SourceBank.valueOf(bank.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new UnknownBankException(bank);
        }

        IngestionResult result = ingestionService.ingest(file, sourceBank);
        return ResponseEntity.ok(result);
    }
}
