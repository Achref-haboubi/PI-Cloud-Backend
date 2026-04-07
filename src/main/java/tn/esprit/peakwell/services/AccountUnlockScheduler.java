package tn.esprit.peakwell.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.*;

import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class AccountUnlockScheduler {
    
    private final UserRepository userRepository;
    private final IEmailService emailService;

    @Scheduled(fixedRate = 60000) 
    public void unlockAccounts() {

        List<User> lockedUsers = userRepository.findByAccountLockedTrue();

        long ONE_HOUR = 60 * 60 * 1000;

        for (User user : lockedUsers) {

            long diff = new Date().getTime() - user.getLockTime().getTime();

            if (diff > ONE_HOUR) {

                user.setAccountLocked(false);
                user.setFailedAttempts(0);
                user.setLockTime(null);

                userRepository.save(user);

                try {
                    emailService.sendAccountUnlockedEmail(user);
                } catch (Exception e) {
                    System.out.println("Email failed: " + e.getMessage());
                }
            }
        }
    }
}

