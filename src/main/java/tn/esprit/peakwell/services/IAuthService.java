package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.LoginRequest;
import tn.esprit.peakwell.dto.SignupRequest;
import org.springframework.http.ResponseEntity;

public interface IAuthService {

    ResponseEntity<?> signup(SignupRequest signupRequest);
    ResponseEntity<?> login(LoginRequest loginRequest);
    //ResponseEntity<?> login(LoginRequest request);
}
