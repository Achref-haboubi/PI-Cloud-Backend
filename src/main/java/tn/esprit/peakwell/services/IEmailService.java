package tn.esprit.peakwell.services;


public interface IEmailService {
    
    void sendSimpleEmail(String to, String subject, String content);

}
