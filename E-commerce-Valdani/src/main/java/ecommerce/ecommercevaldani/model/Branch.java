package ecommerce.ecommercevaldani.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;
    @Embedded
    private ContactInformation contactInformation;
    private String description;
    private String openingHours;

    @JsonIgnore
    @OneToMany(mappedBy = "branch")
    private List<Order> orders = new ArrayList<>();


    @ElementCollection
    @Column(length = 1000)
    private List<String> images = new ArrayList<>();
    private boolean isActive;
    private LocalDateTime createdAt;
}
