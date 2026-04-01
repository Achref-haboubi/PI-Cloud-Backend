package tn.esprit.peakwell.services;

import org.springframework.web.multipart.MultipartFile;

import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.dto.UserProfile;
import tn.esprit.peakwell.entities.User;

public interface IUserService {

    void completeProfile(ProfileRequest request, MultipartFile image, MultipartFile certificate);
    User updateProfile(ProfileRequest request);
    UserProfile getCurrentUserProfile();
}
