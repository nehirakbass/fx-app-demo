package com.fxapp.currencyconversion.repos;

import com.fxapp.currencyconversion.entities.Conversion;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversionHistoryRepository extends JpaRepository<Conversion, UUID> {

  Page<Conversion> findByTransactionId(UUID transactionId, Pageable pageable);

  Page<Conversion> findByTimestampBetween(
      LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

  Page<Conversion> findByTransactionIdAndTimestampBetween(
      UUID transactionId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
}
