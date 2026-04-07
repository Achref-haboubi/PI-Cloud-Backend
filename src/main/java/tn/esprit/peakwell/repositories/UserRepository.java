package tn.esprit.peakwell.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import tn.esprit.peakwell.entities.User;

import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKeycloakId(String keycloakId);
    User findByEmail(String email);
    @Query("""
    SELECT DISTINCT u FROM User u
    LEFT JOIN FETCH u.student
    LEFT JOIN FETCH u.dietitian
    """)
    List<User> findAllWithProfiles();
    List<User> findByAccountLockedTrue();

}
