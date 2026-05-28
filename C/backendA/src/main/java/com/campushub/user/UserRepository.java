package com.campushub.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentId(String studentId);
    Optional<User> findByPhone(String phoneEncrypted);
    boolean existsByStudentId(String studentId);
    boolean existsByPhone(String phoneEncrypted);

    @Query("SELECT u FROM User u WHERE u.studentId = :account")
    Optional<User> findByStudentIdOnly(@Param("account") String account);
}