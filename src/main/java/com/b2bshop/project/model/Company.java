package com.b2bshop.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Entity
@Table(name = "company")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    private String name;
    String email;

//    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
//    @JoinTable(name = "users", joinColumns = @JoinColumn(name = "id"))
//    @Column(name = "user", nullable = false)
//    @JsonIgnore

    @OneToMany
    @JoinTable(
            name = "company_user",
            joinColumns = @JoinColumn(name = "tenant_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
//    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<User> users;
}
