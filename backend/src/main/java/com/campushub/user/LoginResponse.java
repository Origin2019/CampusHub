package com.campushub.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String studentId;
    private String nickname;
    private boolean needBindStudentId = false;

    public LoginResponse(String token, Long userId, String studentId, String nickname) {
        this.token = token;
        this.userId = userId;
        this.studentId = studentId;
        this.nickname = nickname;
        this.needBindStudentId = false;
    }

    public void setNeedBindStudentId(boolean need) {
        this.needBindStudentId = need;
    }
}