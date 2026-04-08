package tn.esprit.peakwell.repositories;

import tn.esprit.peakwell.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface userRepository extends JpaRepository<User, Long> {

  boolean existsByEmail(String email);

  User findByEmail (String email);
}
