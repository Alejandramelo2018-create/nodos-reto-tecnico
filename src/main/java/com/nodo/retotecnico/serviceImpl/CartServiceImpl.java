package com.nodo.retotecnico.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.nodo.retotecnico.model.Cart;
import com.nodo.retotecnico.model.ExpansionPack;
import com.nodo.retotecnico.model.Platform;
import com.nodo.retotecnico.model.User;
import com.nodo.retotecnico.repository.CartRepository;
import com.nodo.retotecnico.repository.ExpansionPacksRepository;
import com.nodo.retotecnico.repository.PlatformsRepository;
import com.nodo.retotecnico.repository.UserRepository;
import com.nodo.retotecnico.service.CartService;

@Service
public class CartServiceImpl implements CartService {

    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpansionPacksRepository expansionRepository;

    @Autowired
    private PlatformsRepository platformsRepository;

    @Override
    public Cart getCartByUser(Integer userId) {
        return cartRepository.findByUserIdAndStatus(userId, "activo")
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElseThrow();
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setStatus("activo");
                    newCart.setTotal(0.0);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public Cart addToCart(Integer userId, Integer expansionId, Integer platformId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        validateUserCanPurchase(user);

        Cart cart = getCartByUser(userId);

        ExpansionPack expansion = expansionRepository.findById(expansionId)
                .orElseThrow(() -> new RuntimeException("Expansion not found"));
        Platform platform = platformsRepository.findById(platformId)
                .orElseThrow(() -> new RuntimeException("Platform not found"));

        cart.addExpansion(expansion, platform); // recalcula dentro de Cart
        return cartRepository.save(cart);
    }

    private void validateUserCanPurchase(User user) {
        if (user.getRole() != null && ADMIN_ROLE.equalsIgnoreCase(user.getRole())) {
            throw new AccessDeniedException("Admin users cannot add items to cart");
        }
    }

    @Override
    public Cart removeFromCart(Integer userId, Integer expansionId) {
        Cart cart = getCartByUser(userId);

        ExpansionPack expansion = expansionRepository.findById(expansionId)
                .orElseThrow();

        cart.removeExpansion(expansion); // recalcula dentro de Cart
        return cartRepository.save(cart);
    }

    @Override
    public void clearCart(Integer userId) {
        Cart cart = getCartByUser(userId);
        cart.clearCart(); // limpia y pone total en 0
        cartRepository.save(cart);
    }
}
