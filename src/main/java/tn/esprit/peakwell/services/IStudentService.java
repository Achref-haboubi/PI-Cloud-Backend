package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.StudentProfileRequest;
import tn.esprit.peakwell.entities.Student;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface IStudentService {

  Student completeStudentProfile(String token, StudentProfileRequest request);
  List<Map<String, Object>> getAllStudents();
  Map<String, Object> getStudentById(Long id);
}
