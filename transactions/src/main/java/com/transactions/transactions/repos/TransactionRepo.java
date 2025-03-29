package com.transactions.transactions.repos;

import com.transactions.transactions.entity.Transaction;
import com.transactions.transactions.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, String> {

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.sourceId = :sourceId OR t.destinationId = :destinationId) " +
            "AND t.transactionType = :transactionType " +
            "ORDER BY t.updatedAt DESC")
    Page<Transaction> findBySourceIdOrDestinationIdAndTransactionType(
            @Param("sourceId") String sourceId,
            @Param("destinationId") String destinationId,
            @Param("transactionType") TransactionType transactionType,
            Pageable pageable
    );

    List<Transaction> findBySourceIdOrDestinationId(String sourceId, String destinationId);
}
