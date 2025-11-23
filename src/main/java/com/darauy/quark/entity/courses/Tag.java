package com.darauy.quark.entity.courses;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tags",
        indexes = {
                @Index(name = "idx_tag_name", columnList = "name", unique = true)
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;
}