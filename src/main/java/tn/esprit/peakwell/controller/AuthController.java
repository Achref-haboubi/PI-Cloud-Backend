package tn.esprit.peakwell.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import tn.esprit.peakwell.dto.ForgotPasswordRequest;
import tn.esprit.peakwell.dto.LoginRequest;
import tn.esprit.peakwell.dto.RegisterRequest;
import tn.esprit.peakwell.services.IAuthService;

import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {
  @Autowired
  IAuthService authService;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    return authService.login(request);
  }


  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody ForgotPasswordRequest request) {

    authService.forgotPassword(request.getEmail());

    Map<String, Object> response = Map.of(
            "status", HttpStatus.OK.value(),
            "message", "Reset password email sent successfully"
    );

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }


}