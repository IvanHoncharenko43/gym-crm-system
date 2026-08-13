package org.example.user.service;

import lombok.RequiredArgsConstructor;
import org.example.trainee.repository.TraineeRepository;
import org.example.trainer.repository.TrainerRepository;
import org.example.user.controller.dto.UserRole;
import org.example.user.repository.UserEntity;
import org.example.user.repository.UserRepository;
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
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found in the database"));

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (trainerRepository.existsByUser((userEntity))){
            authorities.add(new SimpleGrantedAuthority(UserRole.TRAINER.getAuthority()));
        }
        else if (traineeRepository.existsByUser((userEntity))){
            authorities.add(new SimpleGrantedAuthority(UserRole.TRAINEE.getAuthority()));
        }
        return new User(
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getIsActive(),
                true,
                true,
                true,                     // Account non-locked (tie this to the Brute Force logic later)
                authorities
        );
    }
}
