package com.benchmark.supplychain.customs;

import com.benchmark.supplychain.order.Order;
import com.benchmark.supplychain.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MONOLITH ADVANTAGE: Creating a customs declaration reads the full Order
 * (items, declared value, countries) in one direct call.
 * In microservices, customs-service would HTTP-call order-service and
 * potentially carrier-service for country info — 2+ roundtrips.
 */
@Service
@RequiredArgsConstructor
public class CustomsService {

    private final CustomsRepository customsRepository;
    private final OrderService orderService;

    @Transactional
    public Customs createDeclaration(Long orderId) {
        Order order = orderService.getOrderById(orderId);

        if (!order.isInternational()) {
            throw new IllegalStateException("Order " + orderId + " is domestic — no customs declaration needed");
        }

        // Calculate duties based on declared value (simplified — real impl uses HS codes + country rules)
        BigDecimal dutyRate = new BigDecimal("0.05");
        BigDecimal taxRate = new BigDecimal("0.10");
        BigDecimal declaredValue = order.getTotalValue() != null ? order.getTotalValue() : BigDecimal.ZERO;

        Customs customs = Customs.builder()
                .orderId(orderId)
                .originCountry("US")
                .destinationCountry(order.getDestinationCountry())
                .declaredValue(declaredValue)
                .dutyAmount(declaredValue.multiply(dutyRate))
                .taxAmount(declaredValue.multiply(taxRate))
                .currency(order.getCurrency())
                .hsCodes("[]")
                .build();

        return customsRepository.save(customs);
    }

    @Transactional(readOnly = true)
    public Customs getById(Long id) {
        return customsRepository.findById(id)
                .orElseThrow(() -> new CustomsNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Customs getByOrderId(Long orderId) {
        return customsRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomsNotFoundException("No customs declaration for order: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<Customs> getHeldDeclarations() {
        return customsRepository.findByStatus(CustomsStatus.HELD);
    }

    @Transactional(readOnly = true)
    public List<Customs> getAll() {
        return customsRepository.findAll();
    }

    @Transactional
    public Customs submit(Long id) {
        Customs customs = getById(id);
        customs.setStatus(CustomsStatus.SUBMITTED);
        customs.setSubmittedAt(LocalDateTime.now());
        return customsRepository.save(customs);
    }

    @Transactional
    public Customs clear(Long id) {
        Customs customs = getById(id);
        customs.setStatus(CustomsStatus.CLEARED);
        customs.setClearedAt(LocalDateTime.now());
        return customsRepository.save(customs);
    }

    @Transactional
    public Customs hold(Long id, String reason) {
        Customs customs = getById(id);
        customs.setStatus(CustomsStatus.HELD);
        customs.setNotes(reason);
        return customsRepository.save(customs);
    }
}
