package com.transactions.transactions.repos;

import com.transactions.transactions.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, String> {

    List<Transaction> findBySourceIdOrDestinationId(String sourceId, String destinationId);
}
