package tn.esprit.peakwell.repositories;

import tn.esprit.peakwell.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
