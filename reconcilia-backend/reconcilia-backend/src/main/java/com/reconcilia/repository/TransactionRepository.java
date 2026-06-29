package com.reconcilia.repository;

import com.reconcilia.entity.Transaction;
<<<<<<< HEAD
import com.reconcilia.entity.TransactionStatus;
=======
>>>>>>> e815eb3 (solve the backend doker rleated error)
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> e815eb3 (solve the backend doker rleated error)
import java.util.Optional;
import java.util.Set;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
<<<<<<< HEAD
     * Returns all transactions with the given status.
     */
    List<Transaction> findByStatus(TransactionStatus status);

    /**
     * Returns all transactions for the given account ID.
     */
    List<Transaction> findByAccountId(String accountId);

    /**
=======
>>>>>>> e815eb3 (solve the backend doker rleated error)
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
