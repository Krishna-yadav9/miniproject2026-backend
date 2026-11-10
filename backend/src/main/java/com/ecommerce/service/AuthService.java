package com.ecommerce.service;

import com.ecommerce.config.JwtTokenProvider;
import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.admin.email:admin@apexstore.com}")
    private String seedAdminEmail;

    @Value("${app.admin.password:Admin@2026!}")
    private String seedAdminPassword;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostConstruct
    public void seedInitialAccounts() {
        try {
            String adminEmail = seedAdminEmail;
            if (!userRepository.existsByEmail(adminEmail)) {
                log.info("Seeding initial administrator account: {}", adminEmail);
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setName("Store Administrator");
                admin.setPassword(passwordEncoder.encode(seedAdminPassword));
                admin.setRole(Role.ROLE_ADMIN.name());
                admin.setCreatedAt(System.currentTimeMillis());
                userRepository.save(admin);
            }

            String customerEmail = "customer@example.com";
            if (!userRepository.existsByEmail(customerEmail)) {
                log.info("Seeding initial customer account: {}", customerEmail);
                User customer = new User();
                customer.setEmail(customerEmail);
                customer.setName("Demo Customer");
                customer.setPassword(passwordEncoder.encode("password123"));
                customer.setRole(Role.ROLE_USER.name());
                customer.setCreatedAt(System.currentTimeMillis());
                userRepository.save(customer);
            }
        } catch (Exception e) {
            log.warn("Could not check/seed default accounts on startup: {}", e.getMessage());
        }
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with email " + email + " already exists");
        }

        // Public registration always creates a normal customer account.
        String assignedRole = Role.ROLE_USER.name();

        User user = new User();
        user.setEmail(email);
        user.setName(request.getName().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(assignedRole);
        user.setCreatedAt(System.currentTimeMillis());

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateToken(savedUser.getEmail(), savedUser.getRole(), savedUser.getName());

        return new AuthResponse(token, savedUser.getId(), savedUser.getEmail(), savedUser.getName(), savedUser.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!matches) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = tokenProvider.generateToken(user.getEmail(), user.getRole(), user.getName());

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole());
    }

    public User getCurrentUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User profile not found for principal: " + email));
    }
}
