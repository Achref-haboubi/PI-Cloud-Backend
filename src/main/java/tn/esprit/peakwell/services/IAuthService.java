package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.AuthResponse;
import tn.esprit.peakwell.dto.LoginRequest;

public interface IAuthService {

    AuthResponse login(LoginRequest request);

}
