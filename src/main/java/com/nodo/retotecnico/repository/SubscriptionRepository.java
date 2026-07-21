package com.nodo.retotecnico.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.nodo.retotecnico.model.Subscription;
import com.nodo.retotecnico.model.SubscriptionType;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    List<Subscription> findByDeletedFalse();
    boolean existsByEmailAndSubscriptionTypeAndDeletedFalse(String email, SubscriptionType subscriptionType);
}
