package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
public class DailySalesProjection extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String environment;
    private final Long outletId;
    private final LocalDate metricDate;
    private int totalTransactions;
    private BigDecimal totalGross;
    private BigDecimal totalDiscounts;
    private BigDecimal totalTax;
    private BigDecimal totalNet;
    private BigDecimal cashSales;
    private BigDecimal cardSales;
    private BigDecimal creditSales;
    private ZonedDateTime updatedAt;

    public DailySalesProjection(
            Long id,
            Long organizationId,
            String environment,
            Long outletId,
            LocalDate metricDate,
            int totalTransactions,
            BigDecimal totalGross,
            BigDecimal totalDiscounts,
            BigDecimal totalTax,
            BigDecimal totalNet,
            BigDecimal cashSales,
            BigDecimal cardSales,
            BigDecimal creditSales,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.outletId = outletId;
        this.metricDate = metricDate;
        this.totalTransactions = totalTransactions;
        this.totalGross = totalGross;
        this.totalDiscounts = totalDiscounts;
        this.totalTax = totalTax;
        this.totalNet = totalNet;
        this.cashSales = cashSales;
        this.cardSales = cardSales;
        this.creditSales = creditSales;
        this.updatedAt = updatedAt;
    }

    public static DailySalesProjection create(
            Long id,
            Long organizationId,
            String environment,
            Long outletId,
            LocalDate metricDate
    ) {
        if (organizationId == null) {
            throw new AnalyticsInvariantException("Organization ID must not be null");
        }
        if (metricDate == null) {
            throw new AnalyticsInvariantException("Metric date must not be null");
        }
        return new DailySalesProjection(
                id,
                organizationId,
                environment != null ? environment : "live",
                outletId != null ? outletId : 0L,
                metricDate,
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                ZonedDateTime.now()
        );
    }

    public void applySale(BigDecimal gross, BigDecimal discount, BigDecimal tax, BigDecimal net, String paymentMethod) {
        BigDecimal g = gross != null ? gross : BigDecimal.ZERO;
        BigDecimal d = discount != null ? discount : BigDecimal.ZERO;
        BigDecimal t = tax != null ? tax : BigDecimal.ZERO;
        BigDecimal n = net != null ? net : BigDecimal.ZERO;

        this.totalTransactions += 1;
        this.totalGross = this.totalGross.add(g);
        this.totalDiscounts = this.totalDiscounts.add(d);
        this.totalTax = this.totalTax.add(t);
        this.totalNet = this.totalNet.add(n);

        if ("CASH".equalsIgnoreCase(paymentMethod)) {
            this.cashSales = this.cashSales.add(n);
        } else if ("CARD".equalsIgnoreCase(paymentMethod)) {
            this.cardSales = this.cardSales.add(n);
        } else {
            this.creditSales = this.creditSales.add(n);
        }
        this.updatedAt = ZonedDateTime.now();
    }

    public void applyRefund(BigDecimal gross, BigDecimal discount, BigDecimal tax, BigDecimal net, String paymentMethod) {
        BigDecimal g = gross != null ? gross : BigDecimal.ZERO;
        BigDecimal d = discount != null ? discount : BigDecimal.ZERO;
        BigDecimal t = tax != null ? tax : BigDecimal.ZERO;
        BigDecimal n = net != null ? net : BigDecimal.ZERO;

        this.totalGross = this.totalGross.subtract(g).max(BigDecimal.ZERO);
        this.totalDiscounts = this.totalDiscounts.subtract(d).max(BigDecimal.ZERO);
        this.totalTax = this.totalTax.subtract(t).max(BigDecimal.ZERO);
        this.totalNet = this.totalNet.subtract(n).max(BigDecimal.ZERO);

        if ("CASH".equalsIgnoreCase(paymentMethod)) {
            this.cashSales = this.cashSales.subtract(n).max(BigDecimal.ZERO);
        } else if ("CARD".equalsIgnoreCase(paymentMethod)) {
            this.cardSales = this.cardSales.subtract(n).max(BigDecimal.ZERO);
        } else {
            this.creditSales = this.creditSales.subtract(n).max(BigDecimal.ZERO);
        }
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
