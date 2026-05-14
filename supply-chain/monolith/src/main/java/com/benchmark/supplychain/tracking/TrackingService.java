package com.benchmark.supplychain.tracking;

import com.benchmark.supplychain.carrier.Carrier;
import com.benchmark.supplychain.carrier.CarrierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final TrackingRepository trackingRepository;
    private final CarrierService carrierService;

    // Called when carrier confirms pickup — reads carrier data directly
    @Transactional
    public Tracking initializeTracking(Long orderId) {
        Carrier carrier = carrierService.getByOrderId(orderId);

        Tracking tracking = Tracking.builder()
                .orderId(orderId)
                .carrierId(carrier.getId())
                .trackingNumber(carrier.getTrackingNumber())
                .currentStatus("PICKED_UP")
                .currentLocation("Origin Warehouse")
                .estimatedArrival(carrier.getEstimatedDelivery() != null
                        ? carrier.getEstimatedDelivery().atTime(18, 0) : LocalDateTime.now().plusDays(3))
                .build();

        return trackingRepository.save(tracking);
    }

    @Transactional
    public Tracking updateLocation(Long orderId, String location, String status,
                                    BigDecimal latitude, BigDecimal longitude) {
        // Create a new tracking event (event log pattern)
        Tracking event = Tracking.builder()
                .orderId(orderId)
                .currentLocation(location)
                .currentStatus(status)
                .latitude(latitude)
                .longitude(longitude)
                .build();
        return trackingRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Tracking getLatestByOrder(Long orderId) {
        return trackingRepository.findFirstByOrderIdOrderByLastUpdatedDesc(orderId)
                .orElseThrow(() -> new TrackingNotFoundException("No tracking for order: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<Tracking> getHistoryByOrder(Long orderId) {
        return trackingRepository.findByOrderIdOrderByLastUpdatedDesc(orderId);
    }

    @Transactional(readOnly = true)
    public List<Tracking> getAll() {
        return trackingRepository.findAll();
    }
}
