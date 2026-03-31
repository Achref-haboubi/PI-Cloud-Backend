package tn.esprit.peakwell.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.entities.Student;
import tn.esprit.peakwell.entities.User;

@Service
public class StudentService implements IStudentService {


    @Override
    public void createStudent(User user, ProfileRequest request) {

        Student student = user.getStudent();

        if (student == null) {
            student = new Student();
            student.setUser(user);
        }

        student.setHeight(request.getHeight());
        student.setWeight(request.getWeight());
        student.setActivityLevel(request.getActivityLevel());
        student.setGoal(request.getGoal());
        student.setImgUrl(request.getImgUrl());

        // BMI calculation
        float heightMeters = request.getHeight() / 100;
        float bmi = (float) (request.getWeight() / (heightMeters * heightMeters));
        student.setBmi(Math.round(bmi * 100) / 100f);

        student.setProfileCompleted(true);

        user.setStudent(student);
    }


    @Override
    public void updateStudent(User user, ProfileRequest request) {

        try {

            Student student = user.getStudent();

            if (student == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found");
            }

            if (request.getHeight() != null && request.getHeight() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid height");
            }

            if (request.getWeight() != null && request.getWeight() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid weight");
            }

            if (request.getHeight() != null) {
                student.setHeight(request.getHeight());
            }

            if (request.getWeight() != null) {
                student.setWeight(request.getWeight());
            }

            if (request.getActivityLevel() != null) {
                student.setActivityLevel(request.getActivityLevel());
            }

            if (request.getGoal() != null) {
                student.setGoal(request.getGoal());
            }

            //  BMI recalculation
            if (student.getHeight() != null && student.getWeight() != null) {
                float heightMeters = student.getHeight() / 100;
                float bmi = (float) (student.getWeight() / (heightMeters * heightMeters));
                student.setBmi(Math.round(bmi * 100) / 100f);
            }

        } catch (ResponseStatusException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal server error"
            );
        }
    }

}
