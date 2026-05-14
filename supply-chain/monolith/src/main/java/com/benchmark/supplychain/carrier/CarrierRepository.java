package com.benchmark.supplychain.carrier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarrierRepository extends JpaRepository<Carrier, Long> {
    Optional<Carrier> findByOrderId(Long orderId);
    Optional<Carrier> findByTrackingNumber(String trackingNumber);
    List<Carrier> findByStatus(CarrierStatus status);
    List<Carrier> findByCarrierName(String carrierName);
}
