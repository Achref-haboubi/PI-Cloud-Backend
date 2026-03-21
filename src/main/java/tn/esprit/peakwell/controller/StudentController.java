package tn.esprit.peakwell.controller;

import com.example.peakwellbackend.dto.StudentProfileRequest;
import com.example.peakwellbackend.entities.Role;
import com.example.peakwellbackend.entities.Student;
import com.example.peakwellbackend.entities.User;
import com.example.peakwellbackend.repository.userRepository;
import com.example.peakwellbackend.security.JwtUtils;
import com.example.peakwellbackend.services.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/student")
public class StudentController {
    @Autowired
    IStudentService studentService;

    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody StudentProfileRequest request) {

        try {
            String token = authHeader.substring(7);

            studentService.completeStudentProfile(token, request);

            return ResponseEntity.ok("Student profile completed");

        } catch (RuntimeException e) {

            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(401).body( Map.of("message", "Access denied", "details", e.getMessage()));
            }

            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.badRequest().body( Map.of("message", "already exists", "details", e.getMessage()));
            }

            return ResponseEntity.status(500).body(Map.of("message","Internal server error" ,
                    "details", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(500).body( Map.of("message",  "Internal server error",
                    "details", e.getMessage()));

        }
    }


}

