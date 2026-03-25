package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.entities.User;

public interface IUserService {

     void completeProfile(ProfileRequest request);
    User updateProfile(ProfileRequest request);
}
