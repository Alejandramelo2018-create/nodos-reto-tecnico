package com.nodo.retotecnico.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "expansion_packs")
@NoArgsConstructor
@AllArgsConstructor
public class ExpansionPack {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String platforms;
    private double price;
    private String category;
    private String publicationDate;
    private String language;
    private Boolean deleted = false;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String URLImage;

    @JsonProperty("URLImage")
    public String getURLImage() {
        return URLImage;
    }

    @JsonProperty("URLImage")
    public void setURLImage(String URLImage) {
        this.URLImage = URLImage;
    }

    @ElementCollection
    @CollectionTable(name = "expansion_pack_characteristics", joinColumns = @JoinColumn(name = "expansion_pack_id"))
    @jakarta.persistence.Column(name = "characteristic")
    private List<String> characteristics = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "expansionPack", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartDetails> cartDetails;
}