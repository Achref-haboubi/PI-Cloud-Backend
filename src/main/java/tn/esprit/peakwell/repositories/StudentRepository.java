package tn.esprit.peakwell.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.peakwell.entities.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
