package tn.esprit.peakwell.services;

import org.springframework.stereotype.Service;
import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.entities.Student;
import tn.esprit.peakwell.entities.User;

@Service
public class StudentService implements IStudentService {

    @Override
    public void createStudent(User user, ProfileRequest request) {

        Student student = user.getStudent();

        //  If student does not exist → create
        if (student == null) {
            student = new Student();
            student.setUser(user);
        }

        //  Update values (NOT create new)
        student.setHeight(request.getHeight());
        student.setWeight(request.getWeight());
        student.setActivityLevel(request.getActivityLevel());
        student.setGoal(request.getGoal());

        // BMI
        float heightMeters = request.getHeight() / 100;
        float bmi = (float) (request.getWeight() / (heightMeters * heightMeters));
        student.setBmi(Math.round(bmi * 100) / 100f);

        user.setStudent(student);
    }

}
