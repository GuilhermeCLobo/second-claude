package com.marketplace.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    default String usernameById(Long id) {
        return findById(id).map(User::getUsername).orElse(null);
    }

    default Map<Long, String> usernamesByIds(Collection<Long> ids) {
        return findAllById(ids).stream().collect(Collectors.toMap(User::getId, User::getUsername));
    }
}
