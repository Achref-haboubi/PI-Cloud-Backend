package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.DietitianProfileRequest;
import tn.esprit.peakwell.entities.Role;
import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.entities.Dietitian;
import tn.esprit.peakwell.repositories.ConsultationRatingRepository;
import tn.esprit.peakwell.repositories.DietitianRepository;
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
public class DietitianService implements IDietitianService {
  @Autowired
  DietitianRepository dietitianRepository;
  userRepository userRepository;
  private final JwtUtils jwtUtils;
  ConsultationRatingRepository ratingRepository;


  @Override
  public Dietitian completeDietitianProfile(String token, DietitianProfileRequest request) {

    //  Extract userId
    Long userId = jwtUtils.extractUserId(token);

    //  Get user
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found"));

    //  Check role
    if (user.getRole() != Role.DIETITIAN) {
      throw new RuntimeException("Access denied: not a dietitian");
    }

    //  Prevent duplicate
    if (dietitianRepository.existsById(userId)) {
      throw new RuntimeException("Dietitian profile already exists");
    }

    //  Create profile
    Dietitian dietitian = new Dietitian();
    dietitian.setUser(user);

    dietitian.setSpecialization(request.getSpecialization());
    dietitian.setCertification(request.getCertification());
    dietitian.setLinkUrl(request.getLinkUrl());
    dietitian.setExperienceYears(request.getExperienceYears());
    dietitian.setConsultationPrice(request.getConsultationPrice());
    dietitian.setBio(request.getBio());

    //  Save
    Dietitian savedDietitian = dietitianRepository.save(dietitian);

    //  Update user
    user.setProfileCompleted(true);
    userRepository.save(user);

    //  Return created profile
    return savedDietitian;
  }

  @Override
  public List<Map<String, Object>> getAllDietitians() {
    return dietitianRepository.findAll().stream().map(d -> {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", d.getId());
      String firstName = "", lastName = "";
      if (d.getUser() != null) {
        firstName = d.getUser().getFirstName() != null ? d.getUser().getFirstName() : "";
        lastName  = d.getUser().getLastName()  != null ? d.getUser().getLastName()  : "";
        m.put("firstName", firstName);
        m.put("lastName",  lastName);
        m.put("email",     d.getUser().getEmail());
      }
      m.put("specialization",    d.getSpecialization());
      m.put("certification",     d.getCertification());
      m.put("experienceYears",   d.getExperienceYears());
      m.put("consultationPrice", d.getConsultationPrice());
      m.put("linkUrl",           d.getLinkUrl());
      String fullName = (firstName + " " + lastName).trim();
      Double avg   = ratingRepository.findAverageRatingByDoctorName(fullName);
      Long   count = ratingRepository.countRatingsByDoctorName(fullName);
      m.put("averageRating", avg   != null ? Math.round(avg * 10.0) / 10.0 : null);
      m.put("totalRatings",  count != null ? count : 0L);
      return m;
    }).collect(Collectors.toList());
  }
}
