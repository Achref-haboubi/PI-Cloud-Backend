package tn.esprit.peakwell.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    //  Link with Keycloak
    @Column(unique = true, nullable = false)
    String keycloakId;

    @Column(unique = true, nullable = false)
    String email;

    String firstName;
    String lastName;

    //  Step after first login
    @Column(nullable = false)
    boolean profileCompleted = false;

    @Temporal(TemporalType.TIMESTAMP)
    Date createdAt;

    //  Automatically executed before INSERT
    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
    }

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    Student student;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    Dietitian dietitian;
}