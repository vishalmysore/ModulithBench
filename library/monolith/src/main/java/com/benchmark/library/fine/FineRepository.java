package com.benchmark.library.fine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {

    List<Fine> findByMemberId(Long memberId);

    List<Fine> findByLoanId(Long loanId);

    List<Fine> findByPaid(boolean paid);

    List<Fine> findByMemberIdAndPaid(Long memberId, boolean paid);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.memberId = :memberId AND f.paid = false")
    BigDecimal sumUnpaidFinesByMember(Long memberId);
}
