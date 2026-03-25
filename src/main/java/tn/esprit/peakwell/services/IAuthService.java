package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.AuthResponse;
import tn.esprit.peakwell.dto.LoginRequest;
import tn.esprit.peakwell.dto.RegisterRequest;

public interface IAuthService {

    AuthResponse login(LoginRequest request);
    void register(RegisterRequest request);
    String getCurrentUserId();
    void forgotPassword(String email);
}
