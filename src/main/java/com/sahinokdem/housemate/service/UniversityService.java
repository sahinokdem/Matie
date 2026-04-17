package com.sahinokdem.housemate.service;

import com.sahinokdem.housemate.domain.university.University;
import com.sahinokdem.housemate.dto.university.UniversityRequest;
import com.sahinokdem.housemate.dto.university.UniversityResponse;
import com.sahinokdem.housemate.exception.ResourceNotFoundException;
import com.sahinokdem.housemate.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UniversityService {

    private final UniversityRepository universityRepository;

    @Transactional(readOnly = true)
    public List<UniversityResponse> getActiveUniversities() {
        return universityRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public UniversityResponse createUniversity(UniversityRequest request) {
        University university = University.builder()
                .name(request.getName())
                .shortName(request.getShortName())
                .domain(request.getDomain())
                .active(true)
                .build();

        University saved = universityRepository.save(university);
        return mapToResponse(saved);
    }

    @Transactional
    public UniversityResponse updateUniversity(UUID universityId, UniversityRequest request) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new ResourceNotFoundException("University not found with id: " + universityId));

        university.setName(request.getName());
        university.setShortName(request.getShortName());
        university.setDomain(request.getDomain());

        University updated = universityRepository.save(university);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteUniversity(UUID universityId) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new ResourceNotFoundException("University not found with id: " + universityId));

        university.setActive(false);
        universityRepository.save(university);
    }

    private UniversityResponse mapToResponse(University university) {
        return UniversityResponse.builder()
                .id(university.getId())
                .name(university.getName())
                .shortName(university.getShortName())
                .domain(university.getDomain())
                .build();
    }
}
