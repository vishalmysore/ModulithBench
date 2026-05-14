package com.benchmark.supplychain.carrier;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarrierService {

    private final CarrierRepository carrierRepository;

    @Transactional
    public Carrier bookCarrier(Long orderId, String carrierName, String serviceType) {
        Carrier booking = Carrier.builder()
                .orderId(orderId)
                .carrierName(carrierName)
                .serviceType(serviceType)
                .trackingNumber(carrierName.toUpperCase().substring(0, 3) + "-" +
                        UUID.randomUUID().toString().substring(0, 10).toUpperCase())
                .pickupScheduledAt(LocalDateTime.now().plusHours(4))
                .status(CarrierStatus.BOOKED)
                .build();
        return carrierRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Carrier getById(Long id) {
        return carrierRepository.findById(id)
                .orElseThrow(() -> new CarrierNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Carrier getByOrderId(Long orderId) {
        return carrierRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CarrierNotFoundException("No carrier booking for order: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<Carrier> getAll() {
        return carrierRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Carrier> getByStatus(CarrierStatus status) {
        return carrierRepository.findByStatus(status);
    }

    // Called by OrderService.cancelOrder() — must happen in same transaction
    @Transactional
    public void cancelBooking(Long orderId) {
        carrierRepository.findByOrderId(orderId).ifPresent(booking -> {
            if (booking.getStatus() == CarrierStatus.PICKED_UP ||
                booking.getStatus() == CarrierStatus.IN_TRANSIT) {
                throw new IllegalStateException(
                    "Cannot cancel carrier booking for order " + orderId +
                    ": carrier has already picked up the package (status: " + booking.getStatus() + ")");
            }
            booking.setStatus(CarrierStatus.CANCELLED);
            carrierRepository.save(booking);
        });
    }

    @Transactional
    public Carrier updateStatus(Long id, CarrierStatus status) {
        Carrier carrier = getById(id);
        carrier.setStatus(status);
        return carrierRepository.save(carrier);
    }
}
