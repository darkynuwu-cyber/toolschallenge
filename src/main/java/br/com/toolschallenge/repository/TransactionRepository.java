package br.com.toolschallenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.toolschallenge.entity.TransactionEntity;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
	boolean existsById(String id);
	
    @Query(value = "SELECT nextval('seq_nsu')", nativeQuery = true)
    Long getNextNsu();
}