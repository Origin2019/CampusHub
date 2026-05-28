package com.campushub.user;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String studentId;

    @Column(unique = true, nullable = false)
    private String phone;   // 加密存储

    @Column(nullable = false)
    private String password;

    private String nickname;
    private String avatar;

    @Column(nullable = false)
    private Integer creditScore = 100;

    private Integer status = 1;

    @CreationTimestamp
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}