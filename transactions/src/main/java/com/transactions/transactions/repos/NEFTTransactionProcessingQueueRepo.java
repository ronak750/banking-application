package com.transactions.transactions.repos;

import com.transactions.transactions.entity.NEFTProcessingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NEFTTransactionProcessingQueueRepo extends JpaRepository<NEFTProcessingTransaction, String> {

}
