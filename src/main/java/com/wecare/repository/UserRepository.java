package com.wecare.repository;

import com.wecare.entity.AppUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByCode(String code);
    boolean existsByMobile(String mobile);
    boolean existsByMobileAndIdNot(String mobile, Long id);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    List<AppUser> findAllByOrderByNameAsc();
}
