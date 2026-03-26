package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.dto.StudentProfileRequest;
import tn.esprit.peakwell.entities.Role;
import tn.esprit.peakwell.entities.Student;
import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.repositories.userRepository;
import tn.esprit.peakwell.services.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RestController
@RequestMapping("/student")
@CrossOrigin(origins = "http://localhost:4200")
public class StudentController {
  @Autowired
  IStudentService studentService;

  @GetMapping("/all")
  public ResponseEntity<List<Map<String, Object>>> getAllStudents() {
    return ResponseEntity.ok(studentService.getAllStudents());
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getStudentById(@PathVariable Long id) {
    Map<String, Object> student = studentService.getStudentById(id);
    return student != null ? ResponseEntity.ok(student) : ResponseEntity.notFound().build();
  }

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
