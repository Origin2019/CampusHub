package com.campushub.verification;

import com.campushub.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;

    /**
     * 生成并保存验证码（模拟发送短信）
     * @param phone 手机号
     * @param type 类型：REGISTER, LOGIN
     * @return 验证码（实际生产应调用短信接口，这里直接返回）
     */
    @Transactional
    public String sendCode(String phone, String type) {
        // 生成6位随机数
        String code = String.format("%06d", new Random().nextInt(999999));
        // 有效期5分钟
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(5);

        VerificationCode vc = new VerificationCode();
        vc.setPhone(phone);
        vc.setCode(code);
        vc.setType(type);
        vc.setExpireTime(expireTime);
        vc.setUsed(false);
        verificationCodeRepository.save(vc);

        // 模拟发送短信（实际应调用第三方短信接口）
        System.out.println("【验证码】" + phone + " 验证码：" + code + "，有效期5分钟");
        return code;
    }

    /**
     * 校验验证码
     */
    public void verifyCode(String phone, String code, String type) {
        LocalDateTime now = LocalDateTime.now();
        VerificationCode vc = verificationCodeRepository
                .findByPhoneAndCodeAndTypeAndUsedFalseAndExpireTimeAfter(phone, code, type, now)
                .orElseThrow(() -> new BusinessException("验证码无效或已过期"));
        // 标记为已使用
        vc.setUsed(true);
        verificationCodeRepository.save(vc);
    }
}