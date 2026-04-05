package tn.esprit.peakwell.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.dto.UpdateProfileRequest;
import tn.esprit.peakwell.dto.UserProfile;

public interface IUserService {

    void completeProfile(ProfileRequest request, MultipartFile image, MultipartFile certificate);
    void updateProfile( UpdateProfileRequest request, MultipartFile image,  MultipartFile certificate);
    UserProfile getCurrentUserProfile();
    List<UserProfile> getAllUsers();
    void toggleStatus(Long userId);
   
}
