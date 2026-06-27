package com.reconcilia.parser;

import com.reconcilia.entity.SourceBank;
import com.reconcilia.entity.Transaction;
import com.reconcilia.entity.TransactionStatus;
import com.reconcilia.exception.CsvParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses Alpha Bank CSV statements where debit and credit amounts occupy
 * separate columns.
 *
 * <h3>Expected CSV header</h3>
 * <pre>
 * account_id, date, description, reference, debit, credit
 * </pre>
 *
 * <h3>Amount normalisation</h3>
 * {@code amount = credit - debit}
 * — positive result is a credit, negative result is a debit.
 * Exactly one of the two columns is expected to be populated per row; an empty
 * cell is treated as zero.
 */
@Component
public class AlphaBankParser implements BankStatementParser {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .build();

    @Override
    public SourceBank supportedBank() {
        return SourceBank.ALPHA;
    }

    @Override
    public List<Transaction> parse(MultipartFile file) throws IOException {
        List<Transaction> transactions = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            for (CSVRecord record : CSV_FORMAT.parse(reader)) {
                transactions.add(toTransaction(record));
            }
        }

        return transactions;
    }

    private Transaction toTransaction(CSVRecord record) {
        long rowNum = record.getRecordNumber();
        try {
            String debitStr  = record.get("debit");
            String creditStr = record.get("credit");

            BigDecimal debit  = isBlank(debitStr)  ? BigDecimal.ZERO : new BigDecimal(debitStr);
            BigDecimal credit = isBlank(creditStr) ? BigDecimal.ZERO : new BigDecimal(creditStr);
            BigDecimal amount = credit.subtract(debit);   // positive = credit, negative = debit

            Transaction tx = new Transaction();
            tx.setAccountId(record.get("account_id"));
            tx.setDate(LocalDate.parse(record.get("date"), DATE_FMT));
            tx.setDescription(record.get("description"));
            tx.setReferenceNumber(record.get("reference"));
            tx.setAmount(amount);
            tx.setSourceBank(SourceBank.ALPHA);
            tx.setStatus(TransactionStatus.PENDING);
            return tx;

        } catch (NumberFormatException ex) {
            throw new CsvParseException(
                    "Alpha Bank row " + rowNum + ": invalid numeric amount — " + ex.getMessage(), ex);
        } catch (DateTimeParseException ex) {
            throw new CsvParseException(
                    "Alpha Bank row " + rowNum + ": invalid date format (expected yyyy-MM-dd) — " + record.get("date"), ex);
        } catch (IllegalArgumentException ex) {
            throw new CsvParseException(
                    "Alpha Bank row " + rowNum + ": missing or unrecognised column — " + ex.getMessage(), ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
