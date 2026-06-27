package com.reconcilia.entity;

/**
 * Supported source bank identifiers.
 * Add a new value here and create the corresponding {@code BankStatementParser}
 * implementation to support an additional bank format.
 */
public enum SourceBank {
    ALPHA,
    BETA
}
