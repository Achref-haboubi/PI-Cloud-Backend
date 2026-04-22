package tn.esprit.peakwell.services;

import jakarta.servlet.http.HttpServletRequest;
import tn.esprit.peakwell.entities.ActivityType;
import tn.esprit.peakwell.entities.User;

public interface IUserActivityService {
        void log(User user,  ActivityType action,  String description, String status, HttpServletRequest request);
}
