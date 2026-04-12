package tn.esprit.peakwell.services;

public interface IKeycloakService {
    void forgotPassword(String email);

    void updateUserNames(String userId, String firstName, String lastName);
}
