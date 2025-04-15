package com.fxapp.currencyconversion.util;

import com.fxapp.currencyconversion.dtos.conversionhistory.ConversionHistoryRequestDTO;
import com.fxapp.currencyconversion.entities.Conversion;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class ConversionSpecificationCreator {
  public static Specification<Conversion> create(ConversionHistoryRequestDTO request) {
    Specification<Conversion> spec = Specification.where(null);

    if (request.getUsername() != null) {
      spec = spec.and(hasUsername(request.getUsername().trim().toLowerCase()));
    }

    if (request.getTransactionId() != null) {
      spec = spec.and(hasTransactionId(request.getTransactionId()));
    }

    if (request.getTransactionDate() != null) {
      spec = spec.and(timestampAfter(request.getTransactionDate()));
      spec = spec.and(timestampBefore(LocalDateTime.now()));
    }

    return spec;
  }

  private static Specification<Conversion> hasUsername(String username) {
    return (root, query, cb) -> cb.equal(root.get("user").get("username"), username);
  }

  private static Specification<Conversion> hasTransactionId(UUID id) {
    return (root, query, cb) -> cb.equal(root.get("transactionId"), id);
  }

  private static Specification<Conversion> timestampAfter(LocalDateTime date) {
    return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), date);
  }

  private static Specification<Conversion> timestampBefore(LocalDateTime date) {
    return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("timestamp"), date);
  }
}
