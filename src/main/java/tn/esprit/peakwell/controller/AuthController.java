package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.dto.LoginRequest;
import tn.esprit.peakwell.dto.SignupRequest;
import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.repositories.userRepository;
import tn.esprit.peakwell.security.JwtUtils;
import tn.esprit.peakwell.services.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

  @Autowired IAuthService authService;
  @Autowired JwtUtils     jwtUtils;
  @Autowired userRepository userRepo;

  @PostMapping("/signup")
  public ResponseEntity<?> register(@RequestBody SignupRequest request) {
    return authService.signup(request);
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    return authService.login(request);
  }

  /** GET /auth/me — returns the logged-in user's profile from the JWT */
  @GetMapping("/me")
  public ResponseEntity<?> getMe(
      @RequestHeader(value = "Authorization", required = false) String authHeader) {

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return ResponseEntity.status(401).body(Map.of("error", "No token provided"));
    }
    try {
      String token  = authHeader.substring(7);
      Long   userId = jwtUtils.extractUserId(token);
      User   user   = userRepo.findById(userId)
                              .orElseThrow(() -> new RuntimeException("User not found"));

      Map<String, Object> result = new LinkedHashMap<>();
      result.put("id",        user.getId());
      result.put("firstName", user.getFirstName());
      result.put("lastName",  user.getLastName());
      result.put("email",     user.getEmail());
      result.put("role",      user.getRole().name());
      return ResponseEntity.ok(result);

    } catch (Exception e) {
      return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
    }
  }
}
