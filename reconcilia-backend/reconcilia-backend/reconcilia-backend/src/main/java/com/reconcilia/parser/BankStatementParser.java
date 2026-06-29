package com.reconcilia.parser;

import com.reconcilia.entity.SourceBank;
import com.reconcilia.entity.Transaction;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Strategy interface for parsing bank-specific CSV formats into normalised
 * {@link Transaction} objects.
 *
 * <h3>Extending to a new bank</h3>
 * <ol>
 *   <li>Add a value to {@link SourceBank}.</li>
 *   <li>Create a class that {@code implements BankStatementParser} and annotate it
 *       with {@code @Component}.</li>
 *   <li>Spring auto-discovers the bean; {@link BankStatementParserFactory} registers
 *       it automatically. No other changes required.</li>
 * </ol>
 */
public interface BankStatementParser {

    /**
     * Returns the {@link SourceBank} this parser handles.
     * Must be unique across all registered parsers.
     */
    SourceBank supportedBank();

    /**
     * Parses the uploaded CSV file into a list of {@link Transaction} objects.
     * Transactions are in {@code PENDING} status; normalisation of sign convention
     * (positive = credit, negative = debit) is the responsibility of each implementation.
     *
     * @param file the uploaded multipart CSV file
     * @return ordered list of parsed transactions (never null, may be empty)
     * @throws IOException         if the file stream cannot be read
     * @throws com.reconcilia.exception.CsvParseException if a row contains malformed data
     */
    List<Transaction> parse(MultipartFile file) throws IOException;
}
