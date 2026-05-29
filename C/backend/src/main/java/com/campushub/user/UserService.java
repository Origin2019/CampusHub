package com.campushub.user;

import com.campushub.common.BusinessException;
import com.campushub.util.AesUtil;
import com.campushub.util.BcryptUtil;
import com.campushub.util.JwtUtil;
import com.campushub.verification.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BcryptUtil bcryptUtil;
    private final AesUtil aesUtil;
    private final JwtUtil jwtUtil;
    private final VerificationCodeService verificationCodeService;

    // 注册（需要短信验证码）
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // 1. 校验短信验证码
        verificationCodeService.verifyCode(request.getPhone(), request.getSmsCode(), "REGISTER");

        // 2. 检查学号唯一性
        if (userRepository.existsByStudentId(request.getStudentId())) {
            throw new BusinessException("学号已注册");
        }
        // 3. 加密手机号并检查唯一性
        String encryptedPhone = aesUtil.encrypt(request.getPhone());
        if (userRepository.existsByPhone(encryptedPhone)) {
            throw new BusinessException("手机号已注册");
        }

        // 4. 创建用户
        User user = new User();
        user.setStudentId(request.getStudentId());
        user.setPhone(encryptedPhone);
        user.setPassword(bcryptUtil.encode(request.getPassword()));
        user.setNickname("同学" + request.getStudentId().substring(0, 4));
        user.setCreditScore(100);
        user.setStatus(1);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getStudentId(), user.getId());
        return new LoginResponse(token, user.getId(), user.getStudentId(), user.getNickname());
    }

    // 密码登录（支持学号或手机号）
    public LoginResponse loginWithPassword(LoginRequest request) {
        Optional<User> userOpt;
        // 判断输入是学号还是手机号
        if (request.getAccount().matches("^1[3-9]\\d{9}$")) {
            // 手机号登录：需要加密后查询
            String encryptedPhone = aesUtil.encrypt(request.getAccount());
            userOpt = userRepository.findByPhone(encryptedPhone);
        } else {
            // 学号登录
            userOpt = userRepository.findByStudentId(request.getAccount());
        }

        User user = userOpt.orElseThrow(() -> new BusinessException("账号不存在"));
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        if (!bcryptUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        String token = jwtUtil.generateToken(user.getStudentId(), user.getId());
        return new LoginResponse(token, user.getId(), user.getStudentId(), user.getNickname());
    }

    // 手机号+验证码登录（自动注册或直接登录）
    @Transactional
    public LoginResponse loginWithSms(SmsLoginRequest request) {
        // 1. 校验验证码
        verificationCodeService.verifyCode(request.getPhone(), request.getCode(), "LOGIN");

        // 2. 加密手机号查询用户
        String encryptedPhone = aesUtil.encrypt(request.getPhone());
        Optional<User> userOpt = userRepository.findByPhone(encryptedPhone);
        User user;
        if (userOpt.isEmpty()) {
            // 自动注册（学号暂缺，需要用户后续完善）
            user = new User();
            user.setStudentId(""); // 空学号，稍后提示完善
            user.setPhone(encryptedPhone);
            user.setPassword(bcryptUtil.encode("")); // 空密码，只能用验证码登录
            user.setNickname("用户" + request.getPhone().substring(7));
            user.setCreditScore(100);
            user.setStatus(1);
            userRepository.save(user);
        } else {
            user = userOpt.get();
            if (user.getStatus() != 1) {
                throw new BusinessException("账号已被禁用");
            }
        }

        String token = jwtUtil.generateToken(user.getStudentId(), user.getId());
        LoginResponse response = new LoginResponse(token, user.getId(), user.getStudentId(), user.getNickname());
        response.setNeedBindStudentId(user.getStudentId().isEmpty());
        return response;
    }

    // 获取个人资料（解密手机号）
    public UserProfileDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        String plainPhone = aesUtil.decrypt(user.getPhone());
        return new UserProfileDTO(user.getId(), user.getStudentId(), plainPhone,
                user.getNickname(), user.getAvatar(), user.getCreditScore());
    }

    // 更新资料
    @Transactional
    public void updateProfile(Long userId, UserProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        // 如果学号为空且传入了学号，则更新
        if (user.getStudentId().isEmpty() && dto.getStudentId() != null && !dto.getStudentId().isEmpty()) {
            if (userRepository.existsByStudentId(dto.getStudentId())) {
                throw new BusinessException("学号已被其他用户绑定");
            }
            user.setStudentId(dto.getStudentId());
        }
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String studentId) throws UsernameNotFoundException {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        return new org.springframework.security.core.userdetails.User(
                user.getStudentId(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}