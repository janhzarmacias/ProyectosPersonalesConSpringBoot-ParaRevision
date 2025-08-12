package ecommerce.ecommercevaldani.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = true)
    @JsonIgnore
    private User user;

    private String addressLine;
    private String city;
    private String zipcode;
    private String country;
    private String phoneNumber;
    private boolean isPrimary;

    private LocalDateTime createdAt;

}

