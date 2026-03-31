package tn.esprit.peakwell.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentUserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    
    private String role; 
    private boolean profileCompleted;
    private boolean enabled; 
}
