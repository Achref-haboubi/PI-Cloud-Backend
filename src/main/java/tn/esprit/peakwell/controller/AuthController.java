package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.dto.LoginRequest;
import tn.esprit.peakwell.dto.SignupRequest;
import tn.esprit.peakwell.services.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    IAuthService authService;


    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public  ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

}
