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
 * Parses Beta Bank CSV statements that use a single signed {@code amount} column
 * combined with a {@code type} column ({@code CR} = credit, {@code DR} = debit).
 *
 * <h3>Expected CSV header</h3>
 * <pre>
 * account_id, date, description, reference, amount, type
 * </pre>
 *
 * <h3>Amount normalisation</h3>
 * If {@code type == DR}, the amount is negated so the stored value is negative
 * (consistent with Alpha Bank and the unified schema sign convention).
 */
@Component
public class BetaBankParser implements BankStatementParser {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .build();

    @Override
    public SourceBank supportedBank() {
        return SourceBank.BETA;
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
            BigDecimal amount = new BigDecimal(record.get("amount"));
            String type = record.get("type").trim().toUpperCase();

            if ("DR".equals(type)) {
                amount = amount.negate();       // debit → negative amount
            } else if (!"CR".equals(type)) {
                throw new CsvParseException(
                        "Beta Bank row " + rowNum + ": unrecognised type '" + type + "' (expected CR or DR)", null);
            }

            Transaction tx = new Transaction();
            tx.setAccountId(record.get("account_id"));
            tx.setDate(LocalDate.parse(record.get("date"), DATE_FMT));
            tx.setDescription(record.get("description"));
            tx.setReferenceNumber(record.get("reference"));
            tx.setAmount(amount);
            tx.setSourceBank(SourceBank.BETA);
            tx.setStatus(TransactionStatus.PENDING);
            return tx;

        } catch (NumberFormatException ex) {
            throw new CsvParseException(
                    "Beta Bank row " + rowNum + ": invalid numeric amount — " + ex.getMessage(), ex);
        } catch (DateTimeParseException ex) {
            throw new CsvParseException(
                    "Beta Bank row " + rowNum + ": invalid date format (expected yyyy-MM-dd) — " + record.get("date"), ex);
        } catch (IllegalArgumentException ex) {
            throw new CsvParseException(
                    "Beta Bank row " + rowNum + ": missing or unrecognised column — " + ex.getMessage(), ex);
        }
    }
}
