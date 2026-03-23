package tn.esprit.peakwell.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.peakwell.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
