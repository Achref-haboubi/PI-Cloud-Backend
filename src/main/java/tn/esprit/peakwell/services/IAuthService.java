package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.FaceLoginRequest;
import tn.esprit.peakwell.dto.LoginRequest;
import tn.esprit.peakwell.dto.RegisterRequest;
import tn.esprit.peakwell.entities.Role;

import org.springframework.http.ResponseEntity;

public interface IAuthService {

    ResponseEntity<?> login(LoginRequest request);

    ResponseEntity<?> register(RegisterRequest request);

    String getCurrentUserId();

    void forgotPassword(String email);

    String generateGoogleAuthUrl();

    ResponseEntity<?> handleGoogleLogin(String code, String flow);

    ResponseEntity<?> completeGoogleSignup(String accessToken, Role role);

    ResponseEntity<?> faceLogin(FaceLoginRequest request);
}
