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

    @Column(unique = true, nullable = false)
    String email;

    @Column(nullable = false)
    String password;

    String firstName;
    String lastName;
    Integer age;

    @Enumerated(EnumType.STRING)
    Role role;

    boolean enabled;
    boolean profileCompleted;

    Date createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
        this.enabled = false;
        this.profileCompleted = false;
    }

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    Student student;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    Dietitian dietitian;
}