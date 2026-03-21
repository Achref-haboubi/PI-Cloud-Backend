package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.DietitianProfileRequest;
import tn.esprit.peakwell.entities.Dietitian;

public interface IDietitianService {

    Dietitian completeDietitianProfile(String token, DietitianProfileRequest request);
}
