package com.b2bshop.project.service;

import com.b2bshop.project.dto.CreateUserRequest;
import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> user = userRepository.findByUsername(username);
        return user.orElseThrow(EntityNotFoundException::new);
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(CreateUserRequest request) {
        // TODO sadece ADMIN
        User newUser = User.builder()
                .name(request.name())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .authorities(request.authorities())
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .isEnabled(true)
                .accountNonLocked(true)
                .build();

        return userRepository.save(newUser);
    }

    public User updateUserById(Long userId, User newUser) {
        // TODO User sadece kendisini görebilmeli
        // TODO Admin herkesi görebilmeli
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User oldUser = user.get();
            oldUser.setName(newUser.getName());
            oldUser.setUsername(newUser.getUsername());
            oldUser.setEmail(newUser.getEmail());
            oldUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
            oldUser.setAuthorities(newUser.getAuthorities());
            userRepository.save(oldUser);
            return oldUser;
        } else return null;
    }
}
