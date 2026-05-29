package com.campushub.verification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByPhoneAndCodeAndTypeAndUsedFalseAndExpireTimeAfter(
            String phone, String code, String type, LocalDateTime now);
}