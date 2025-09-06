package com.togethershop.backend.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Businesses")
@Getter
@Setter
@NoArgsConstructor
public class Businesses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB auto_increment 사용
    @Column(name = "business_id")
    private Long id;

    @Column(name = "business_name")
    private String name;
}
