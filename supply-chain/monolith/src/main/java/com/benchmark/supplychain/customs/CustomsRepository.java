package com.benchmark.supplychain.customs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomsRepository extends JpaRepository<Customs, Long> {
    Optional<Customs> findByOrderId(Long orderId);
    Optional<Customs> findByDeclarationNumber(String declarationNumber);
    List<Customs> findByStatus(CustomsStatus status);
    List<Customs> findByDestinationCountry(String country);
}
