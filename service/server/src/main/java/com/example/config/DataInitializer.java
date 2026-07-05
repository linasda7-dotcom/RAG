package com.example.config;

import com.example.entity.User;
import com.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            String hashedPassword = hashPassword("admin123");
            User admin = new User("admin", hashedPassword, "管理员", "admin");
            userRepository.save(admin);
            log.info("默认管理员账号已创建: admin / admin123");
        }

        if (!userRepository.existsByUsername("demo")) {
            String hashedPassword = hashPassword("demo123");
            User demo = new User("demo", hashedPassword, "演示用户", "user");
            userRepository.save(demo);
            log.info("默认演示账号已创建: demo / demo123");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
}
