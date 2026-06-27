package com.reconcilia.parser;

import com.reconcilia.entity.SourceBank;
import com.reconcilia.exception.UnknownBankException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry that maps each {@link SourceBank} to its {@link BankStatementParser}
 * implementation.
 *
 * <p>All {@code BankStatementParser} beans are injected via the constructor list.
 * New parsers annotated with {@code @Component} are automatically picked up by
 * Spring and registered here without any code change to this factory.
 */
@Component
public class BankStatementParserFactory {

    private final Map<SourceBank, BankStatementParser> registry;

    public BankStatementParserFactory(List<BankStatementParser> parsers) {
        this.registry = parsers.stream()
                .collect(Collectors.toMap(BankStatementParser::supportedBank, Function.identity()));
    }

    /**
     * Returns the parser for the given bank.
     *
     * @param bank the source bank identifier
     * @return the corresponding parser
     * @throws UnknownBankException if no parser is registered for {@code bank}
     */
    public BankStatementParser getParser(SourceBank bank) {
        BankStatementParser parser = registry.get(bank);
        if (parser == null) {
            throw new UnknownBankException(bank.name());
        }
        return parser;
    }
}
