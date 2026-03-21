package tn.esprit.peakwell.services;

import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.repositories.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService{

    @Autowired
    userRepository userRepository;

    public User getUserById (Long id ){
        return userRepository.getReferenceById(id);
    }

}
