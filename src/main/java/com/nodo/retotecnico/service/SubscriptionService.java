package com.nodo.retotecnico.service;

import java.util.List;
import com.nodo.retotecnico.model.Subscription;

public interface SubscriptionService {
    List<Subscription> getAllSubscriptions();
    Subscription getSubscriptionById(Integer id);
    Integer createSubscription(Subscription subscription);
    Subscription updateSubscription(Integer id, Subscription subscription);
    void deleteSubscription(Integer id);
}
