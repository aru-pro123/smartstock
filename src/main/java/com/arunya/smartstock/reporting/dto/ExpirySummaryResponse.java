package com.arunya.smartstock.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpirySummaryResponse(
        Long storeId,
        LocalDate asOfDate,
        LocalDate expiringThrough,
        long expiredBatchCount,
        BigDecimal expiredQuantity,
        long expiringSoonBatchCount,
        BigDecimal expiringSoonQuantity
) {}

