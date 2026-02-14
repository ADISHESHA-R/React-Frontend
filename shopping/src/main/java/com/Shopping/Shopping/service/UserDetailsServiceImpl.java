package com.Shopping.Shopping.service;

import com.Shopping.Shopping.model.User;
import com.Shopping.Shopping.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository repo) {
        this.userRepository = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=== UserDetailsServiceImpl: Loading user by username: " + username + " ===");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("=== UserDetailsServiceImpl: User NOT FOUND: " + username + " ===");
                    return new UsernameNotFoundException("User not found: " + username);
                });

        System.out.println("=== UserDetailsServiceImpl: User FOUND: " + user.getUsername() + " ===");
        System.out.println("=== UserDetailsServiceImpl: Password hash: " + (user.getPassword() != null ? user.getPassword().substring(0, Math.min(20, user.getPassword().length())) + "..." : "null") + " ===");

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}
