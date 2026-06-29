package com.reconcilia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Core domain entity representing a single bank transaction, normalised from
 * any supported bank CSV format into a unified schema.
 *
 * <p>Amount sign convention:
 * <ul>
 *   <li>Positive value → credit (money in)</li>
 *   <li>Negative value → debit  (money out)</li>
 * </ul>
 *
 * <p>The {@code reference_number} column carries a unique database constraint
 * (created by Flyway migration V1). Duplicate detection is handled at the service
 * layer before any INSERT is attempted.
 */
@Entity
@Table(
    name = "transactions",
    uniqueConstraints = @UniqueConstraint(
        name = "uc_transactions_reference_number",
        columnNames = "reference_number"
    )
)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Account ID must not be blank")
    @Column(name = "account_id", nullable = false, length = 100)
    private String accountId;

    @NotNull(message = "Transaction date must not be null")
    @Column(name = "transaction_date", nullable = false)
    private LocalDate date;

    /**
     * Signed amount: positive = credit, negative = debit.
     * Precision 19, scale 4 supports values up to ~999 trillion with 4 decimal places.
     */
    @NotNull(message = "Amount must not be null")
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "description", length = 500)
    private String description;

    @NotBlank(message = "Reference number must not be blank")
    @Column(name = "reference_number", nullable = false, length = 100)
    private String referenceNumber;

    @NotNull(message = "Source bank must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "source_bank", nullable = false, length = 20)
    private SourceBank sourceBank;

    @NotNull(message = "Status must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Transaction() {}

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public SourceBank getSourceBank() {
        return sourceBank;
    }

    public void setSourceBank(SourceBank sourceBank) {
        this.sourceBank = sourceBank;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Transaction{id=" + id +
               ", accountId='" + accountId + '\'' +
               ", date=" + date +
               ", amount=" + amount +
               ", referenceNumber='" + referenceNumber + '\'' +
               ", sourceBank=" + sourceBank +
               ", status=" + status + '}';
    }
}
