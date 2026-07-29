package com.nodo.retotecnico.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "contents")
@AllArgsConstructor
@NoArgsConstructor
public class Content {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String section;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String image;
    private Boolean deleted = false;

    
    private String fieldName;
    private String fieldType;
    private Boolean required = false;
    @Column(columnDefinition = "TEXT")
    private String options;
    private Integer displayOrder;

}