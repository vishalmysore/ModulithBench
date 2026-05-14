package com.benchmark.supplychain.tracking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrackingRepository extends JpaRepository<Tracking, Long> {
    Optional<Tracking> findFirstByOrderIdOrderByLastUpdatedDesc(Long orderId);
    List<Tracking> findByOrderIdOrderByLastUpdatedDesc(Long orderId);
    Optional<Tracking> findByTrackingNumber(String trackingNumber);
}
