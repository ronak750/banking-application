package com.transactions.transactions.repos;

import com.transactions.transactions.entities.NEFTProcessingTransaction;
import com.transactions.transactions.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NEFTTransactionProcessingQueueRepo extends JpaRepository<NEFTProcessingTransaction, String> {

}
