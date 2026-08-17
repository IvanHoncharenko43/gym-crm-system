package org.example.user.service;

import lombok.RequiredArgsConstructor;
import org.example.config.AdminConfigurationProperties;
import org.example.security.service.BruteForceProtectionService;
import org.example.trainee.repository.TraineeRepository;
import org.example.trainer.repository.TrainerRepository;
import org.example.user.controller.dto.UserRole;
import org.example.user.repository.UserEntity;
import org.example.user.repository.UserRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminConfigurationProperties.class)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final AdminConfigurationProperties adminConfigurationProperties;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (adminConfigurationProperties.usernames().contains(username)) {
            authorities.add(new SimpleGrantedAuthority(UserRole.ADMIN.getAuthority()));
        } else if (trainerRepository.existsByUserId(userEntity.getId())) {
            authorities.add(new SimpleGrantedAuthority(UserRole.TRAINER.getAuthority()));
        } else if (traineeRepository.existsByUserId(userEntity.getId())) {
            authorities.add(new SimpleGrantedAuthority(UserRole.TRAINEE.getAuthority()));
        } else {
            throw new UsernameNotFoundException("User not found");
        }
        return new User(
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getIsActive(),
                true,
                true,
                !bruteForceProtectionService.isLocked(username),
                authorities
        );
    }
}
