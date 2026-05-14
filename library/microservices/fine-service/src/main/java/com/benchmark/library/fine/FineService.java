package com.benchmark.library.fine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FineService {
    private final FineRepository fineRepository;

    @Transactional
    public Fine createFine(Fine fine) {
        return fineRepository.save(fine);
    }

    @Transactional(readOnly = true)
    public Fine getFineById(Long id) {
        return fineRepository.findById(id)
                .orElseThrow(() -> new FineNotFoundException("Fine not found with id: " + id));
    }

    @Transactional
    public Fine updateFine(Long id, Fine fineDetails) {
        Fine fine = getFineById(id);
        return fineRepository.save(fine);
    }

    @Transactional
    public void deleteFine(Long id) {
        Fine fine = getFineById(id);
        fineRepository.delete(fine);
    }
}