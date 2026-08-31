package com.gib.tiklasat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AdminDashboardStatsDto {
    private BigDecimal totalSales;
    private Long totalUsers;
    private Long activeAuctions;
    private Long dailyBids;

}
