package com.example.employee.service;

import com.example.employee.entity.User;
import com.example.employee.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        System.out.println("LOGIN DEBUG:");
        System.out.println("Username: " + username);
        System.out.println("Stored password: " + user.getPassword());
        System.out.println("Approved: " + user.isApproved());
        System.out.println("Deleted: " + user.isDeleted());

        if (!user.isApproved()) {
            System.out.println("BLOCKED: User not approved");
            throw new UsernameNotFoundException("User not approved");
        }
        if (user.isDeleted()) {
            System.out.println("BLOCKED: User is deleted");
            throw new UsernameNotFoundException("User deleted");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
