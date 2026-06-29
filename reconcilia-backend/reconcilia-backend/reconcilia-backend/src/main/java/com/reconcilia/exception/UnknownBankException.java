package com.reconcilia.exception;

import com.reconcilia.entity.SourceBank;

import java.util.Arrays;

/**
 * Thrown when an upload request specifies a bank identifier that has no
 * registered {@code BankStatementParser}.
 */
public class UnknownBankException extends RuntimeException {

    public UnknownBankException(String bankName) {
        super("Unknown bank: '" + bankName + "'. Supported values: " +
              Arrays.toString(SourceBank.values()));
    }
}
