package com.tekravio.healthcare.bootstrap;

import com.tekravio.healthcare.user.AppUser;
import com.tekravio.healthcare.user.AppUserRepository;
import com.tekravio.healthcare.user.UserRole;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableConfigurationProperties(AdminBootstrapProperties.class)
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AdminBootstrapProperties properties;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(
            AdminBootstrapProperties properties,
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (isBlank(properties.adminEmail()) || isBlank(properties.adminPassword())) {
            return;
        }

        String email = properties.adminEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        userRepository.save(new AppUser(email, passwordEncoder.encode(properties.adminPassword()), UserRole.ADMIN));
        log.info("Bootstrap admin user created for {}", email);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

