package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.StudentProfileRequest;
import tn.esprit.peakwell.entities.Role;
import tn.esprit.peakwell.entities.Student;
import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.repositories.StudentRepository;
import tn.esprit.peakwell.repositories.userRepository;
import tn.esprit.peakwell.security.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StudentService implements IStudentService {

  @Autowired
  StudentRepository studentRepository;
  userRepository userRepository;
  private final JwtUtils jwtUtils;

  @Override
  public Student completeStudentProfile(String token, StudentProfileRequest request) {

    //  Extract userId
    Long userId = jwtUtils.extractUserId(token);

    //  Get user
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found"));

    //  Check role
    if (user.getRole() != Role.STUDENT) {
      throw new RuntimeException("Access denied: not a student");
    }

    //  Prevent duplicate
    if (studentRepository.existsById(userId)) {
      throw new RuntimeException("Student profile already exists");
    }

    //  Create profile
    Student student = new Student();
    student.setUser(user);

    student.setHeight(request.getHeight());
    student.setWeight(request.getWeight());

    // 👉 optional: calculate BMI automatically (better design 🔥)
    if (request.getHeight() != null && request.getWeight() != null) {
      double heightInMeters = request.getHeight() / 100.0;
      double bmi = request.getWeight() / (heightInMeters * heightInMeters);
      student.setBmi((float) bmi);
    }

    student.setActivityLevel(request.getActivityLevel());
    student.setGoal(request.getGoal());

    //  Save
    Student savedStudent = studentRepository.save(student);

    //  Update user
    user.setProfileCompleted(true);
    userRepository.save(user);

    //  Return created profile
    return savedStudent;
  }

  @Override
  public List<Map<String, Object>> getAllStudents() {
    return studentRepository.findAll().stream().map(this::toMap).collect(Collectors.toList());
  }

  @Override
  public Map<String, Object> getStudentById(Long id) {
    return studentRepository.findById(id).map(this::toMap).orElse(null);
  }

  private Map<String, Object> toMap(Student s) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", s.getId());
    if (s.getUser() != null) {
      User u = s.getUser();
      m.put("firstName", u.getFirstName());
      m.put("lastName",  u.getLastName());
      m.put("email",     u.getEmail());
      m.put("age",       u.getAge());
      m.put("enabled",   u.isEnabled());
      m.put("profileCompleted", u.isProfileCompleted());
    }
    m.put("height",        s.getHeight());
    m.put("weight",        s.getWeight());
    m.put("bmi",           s.getBmi());
    m.put("activityLevel", s.getActivityLevel());
    m.put("goal",          s.getGoal());
    return m;
  }
}
