package com.wecare.repository;

import com.wecare.entity.Coach;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachRepository extends JpaRepository<Coach, Long> {
    Optional<Coach> findByCode(String code);
    boolean existsByMobile(String mobile);
    boolean existsByMobileAndIdNot(String mobile, Long id);
    List<Coach> findAllByOrderByNameAsc();
}
