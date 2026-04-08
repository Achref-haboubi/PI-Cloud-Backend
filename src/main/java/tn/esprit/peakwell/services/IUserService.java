package tn.esprit.peakwell.services;

import tn.esprit.peakwell.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface IUserService {

  User getUserById(Long id);
}
