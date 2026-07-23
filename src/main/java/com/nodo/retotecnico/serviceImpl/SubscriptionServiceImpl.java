package com.nodo.retotecnico.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nodo.retotecnico.model.Subscription;
import com.nodo.retotecnico.repository.ExpansionPacksRepository;
import com.nodo.retotecnico.repository.SubscriptionRepository;
import com.nodo.retotecnico.service.SubscriptionService;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ExpansionPacksRepository expansionPacksRepository;

    @Override
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findByDeletedFalse();
    }

    @Override
    public Subscription getSubscriptionById(Integer id) {
        return subscriptionRepository.findById(id)
                .filter(sub -> !sub.getDeleted())
                .orElse(null);
    }

    @Override
    public Integer createSubscription(Subscription subscription) {
        if (subscriptionRepository.existsByEmailAndSubscriptionTypeAndDeletedFalse(
                subscription.getEmail(), subscription.getSubscriptionType())) {
            throw new RuntimeException("El correo electrónico ya está registrado para este tipo de suscripción.");
        }
        return subscriptionRepository.save(subscription).getId();
    }

    @Override
    public Subscription updateSubscription(Integer id, Subscription subscription) {
        Subscription existing = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        
        existing.setEmail(subscription.getEmail());
        existing.setName(subscription.getName());
        existing.setSubscriptionType(subscription.getSubscriptionType());
        existing.setCountry(subscription.getCountry());
        existing.setConsentMarketing(subscription.getConsentMarketing());
        existing.setStatus(subscription.getStatus());

        return subscriptionRepository.save(existing);
    }

    @Override
    public void deleteSubscription(Integer id) {
        Subscription existing = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        existing.setDeleted(true);
        subscriptionRepository.save(existing);
    }
}
