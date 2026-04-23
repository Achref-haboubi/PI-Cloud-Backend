package tn.esprit.peakwell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PeakwellApplication {

    public static void main(String[] args) {
        SpringApplication.run(PeakwellApplication.class, args);
    }

}
