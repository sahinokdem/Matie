package com.sahinokdem.housemate.repository;

import com.sahinokdem.housemate.domain.university.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UniversityRepository extends JpaRepository<University, UUID> {

    List<University> findAllByActiveTrueOrderByNameAsc();
}
