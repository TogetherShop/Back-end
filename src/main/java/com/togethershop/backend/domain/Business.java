package com.togethershop.backend.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Business")
@Getter
@Setter
@NoArgsConstructor
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
