package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.DietitianProfileRequest;
import tn.esprit.peakwell.entities.Dietitian;

import java.util.List;
import java.util.Map;

public interface IDietitianService {

  Dietitian completeDietitianProfile(String token, DietitianProfileRequest request);
  List<Map<String, Object>> getAllDietitians();
}
