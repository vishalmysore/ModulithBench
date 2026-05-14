package com.benchmark.supplychain.billing;

import com.benchmark.supplychain.carrier.Carrier;
import com.benchmark.supplychain.carrier.CarrierService;
import com.benchmark.supplychain.customs.Customs;
import com.benchmark.supplychain.customs.CustomsService;
import com.benchmark.supplychain.order.Order;
import com.benchmark.supplychain.order.OrderService;
import com.benchmark.supplychain.route.Route;
import com.benchmark.supplychain.route.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * MONOLITH ADVANTAGE — THE N+1 REPORT SCENARIO
 *
 * generateProfitabilityReport() reads from 4 modules (Order, Carrier, Customs, Route)
 * via direct method calls — all in one @Transactional read.
 *
 * In microservices this is 4 HTTP calls, 4 JSON schemas, 4 error states.
 * The "Reasoning Tax" for an AI agent writing this in microservices is ~10x higher.
 */
@Service
@RequiredArgsConstructor
public class BillingService {

    private final InvoiceRepository invoiceRepository;

    // Direct cross-module injection — 4 modules, 0 HTTP calls
    private final OrderService orderService;
    private final CarrierService carrierService;
    private final CustomsService customsService;
    private final RouteService routeService;

    @Transactional
    public Invoice generateInvoice(Long orderId) {
        // Read all 4 modules directly — single transaction, no HTTP
        Order order = orderService.getOrderById(orderId);

        BigDecimal shippingCost = BigDecimal.ZERO;
        BigDecimal dutiesAndTaxes = BigDecimal.ZERO;
        BigDecimal fuelSurcharge = BigDecimal.ZERO;

        try {
            Carrier carrier = carrierService.getByOrderId(orderId);
            shippingCost = carrier.getCost() != null ? carrier.getCost() : BigDecimal.ZERO;
        } catch (Exception ignored) {}

        if (order.isInternational()) {
            try {
                Customs customs = customsService.getByOrderId(orderId);
                dutiesAndTaxes = customs.getTotalDutiesAndTaxes();
            } catch (Exception ignored) {}
        }

        try {
            Route route = routeService.getByOrderId(orderId);
            fuelSurcharge = route.getFuelCostEstimate() != null ? route.getFuelCostEstimate() : BigDecimal.ZERO;
        } catch (Exception ignored) {}

        Invoice invoice = Invoice.builder()
                .orderId(orderId)
                .customerId(order.getCustomerId())
                .goodsValue(order.getTotalValue() != null ? order.getTotalValue() : BigDecimal.ZERO)
                .shippingCost(shippingCost)
                .dutiesAndTaxes(dutiesAndTaxes)
                .fuelSurcharge(fuelSurcharge)
                .currency(order.getCurrency())
                .issuedDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .status(InvoiceStatus.ISSUED)
                .build();

        return invoiceRepository.save(invoice);
    }

    /**
     * THE BENCHMARK SCENARIO: Shipment Profitability Report
     *
     * Agent task: implement this method in BOTH architectures.
     * Monolith: direct calls to 4 services, one transaction.
     * Microservices: 4 HTTP calls, 4 error handlers, 4 JSON parsers.
     */
    @Transactional(readOnly = true)
    public ShipmentProfitabilityReport generateProfitabilityReport(Long orderId) {
        // MODULE 1: Order — revenue
        Order order = orderService.getOrderById(orderId);
        BigDecimal revenue = order.getTotalValue() != null ? order.getTotalValue() : BigDecimal.ZERO;

        // MODULE 2: Carrier — shipping cost
        BigDecimal shippingCost = BigDecimal.ZERO;
        String carrierName = "UNKNOWN";
        try {
            Carrier carrier = carrierService.getByOrderId(orderId);
            shippingCost = carrier.getCost() != null ? carrier.getCost() : BigDecimal.ZERO;
            carrierName = carrier.getCarrierName();
        } catch (Exception ignored) {}

        // MODULE 3: Customs — duties and taxes (international only)
        BigDecimal dutiesAndTaxes = BigDecimal.ZERO;
        if (order.isInternational()) {
            try {
                Customs customs = customsService.getByOrderId(orderId);
                dutiesAndTaxes = customs.getTotalDutiesAndTaxes();
            } catch (Exception ignored) {}
        }

        // MODULE 4: Route — fuel cost estimate
        BigDecimal fuelCost = BigDecimal.ZERO;
        BigDecimal distanceKm = BigDecimal.ZERO;
        try {
            Route route = routeService.getByOrderId(orderId);
            fuelCost = route.getFuelCostEstimate() != null ? route.getFuelCostEstimate() : BigDecimal.ZERO;
            distanceKm = route.getTotalDistanceKm() != null ? route.getTotalDistanceKm() : BigDecimal.ZERO;
        } catch (Exception ignored) {}

        BigDecimal totalCost = shippingCost.add(dutiesAndTaxes).add(fuelCost);
        BigDecimal grossProfit = revenue.subtract(totalCost);
        BigDecimal marginPercent = revenue.compareTo(BigDecimal.ZERO) > 0
                ? grossProfit.divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return ShipmentProfitabilityReport.builder()
                .orderId(orderId)
                .orderNumber(order.getOrderNumber())
                .destinationCountry(order.getDestinationCountry())
                .revenue(revenue)
                .currency(order.getCurrency())
                .carrierName(carrierName)
                .shippingCost(shippingCost)
                .dutiesAndTaxes(dutiesAndTaxes)
                .fuelCost(fuelCost)
                .distanceKm(distanceKm)
                .totalCost(totalCost)
                .grossProfit(grossProfit)
                .marginPercent(marginPercent)
                .build();
    }

    @Transactional(readOnly = true)
    public Invoice getById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Invoice> getByCustomer(Long customerId) {
        return invoiceRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Invoice> getAll() {
        return invoiceRepository.findAll();
    }

    @Transactional
    public Invoice markPaid(Long id) {
        Invoice invoice = getById(id);
        invoice.setStatus(InvoiceStatus.PAID);
        return invoiceRepository.save(invoice);
    }
}
