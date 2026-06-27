package com.reconcilia.repository;

import com.reconcilia.entity.Transaction;
import com.reconcilia.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Returns all transactions with the given status.
     */
    List<Transaction> findByStatus(TransactionStatus status);

    /**
     * Returns all transactions for the given account ID.
     */
    List<Transaction> findByAccountId(String accountId);

    /**
     * Returns only the reference numbers that already exist in the database,
     * intersected with the provided set. Used for bulk duplicate detection in a
     * single query rather than N individual existence checks.
     */
    @Query("SELECT t.referenceNumber FROM Transaction t WHERE t.referenceNumber IN :refs")
    Set<String> findReferenceNumbersIn(@Param("refs") Set<String> refs);

    /**
     * Finds a single transaction by its unique reference number.
     * Primarily used in integration tests to assert normalisation results.
     */
    Optional<Transaction> findByReferenceNumber(String referenceNumber);
}
