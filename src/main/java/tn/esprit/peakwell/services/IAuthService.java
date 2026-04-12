package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.LoginRequest;
import tn.esprit.peakwell.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface IAuthService {

  ResponseEntity<?> login(LoginRequest request);
  ResponseEntity<?> register(RegisterRequest request);
  String getCurrentUserId();
  void forgotPassword(String email);
}