package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.StudentProfileRequest;
import tn.esprit.peakwell.entities.Student;
import org.springframework.stereotype.Service;

@Service
public interface IStudentService {

    Student completeStudentProfile(String token, StudentProfileRequest request);
}
