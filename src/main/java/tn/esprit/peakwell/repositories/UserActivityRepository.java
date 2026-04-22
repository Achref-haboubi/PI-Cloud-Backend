package tn.esprit.peakwell.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.esprit.peakwell.entities.UserActivity;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long>{
    
}
