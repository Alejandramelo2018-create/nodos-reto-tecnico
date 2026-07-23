package com.nodo.retotecnico.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cart")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String status; // "activo", "inactivo", "comprado" 
    private Boolean deleted = false;

    private Double total = 0.0; // <-- nuevo campo persistente

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartDetails> details = new ArrayList<>();

    @OneToOne(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Buy buy;

    // MÉTODOS

    public void addExpansion(ExpansionPack expansion, Platform platform) {
        if (this.details == null) {
            this.details = new ArrayList<>();
        }
        boolean exists = this.details.stream()
                .anyMatch(detail -> detail.getExpansionPack().equals(expansion));
        
        if (!exists) {
            CartDetails cartDetail = new CartDetails();
            cartDetail.setCart(this);
            cartDetail.setExpansionPack(expansion);
            cartDetail.setPlatform(platform);
            cartDetail.setQuantity(1); // Set default quantity corresponding to NOT NULL DB constraint
            this.details.add(cartDetail);
        }
        recalculateTotal(); // recalcula al agregar
    }

    public void removeExpansion(ExpansionPack expansion) {
        if (this.details != null) {
            this.details.removeIf(detail -> detail.getExpansionPack().equals(expansion));
        }
        recalculateTotal(); // recalcula al quitar
    }

    public void clearCart() {
        if (this.details != null) {
            this.details.clear();
        }
        this.total = 0.0; // reinicia total
    }

    private void recalculateTotal() {
        this.total = (this.details == null || this.details.isEmpty()) ? 0.0 :
            this.details.stream()
                .mapToDouble(detail -> detail.getExpansionPack().getPrice())
                .sum();
    }
}