package com.nodo.retotecnico.service;

import java.util.List;

import com.nodo.retotecnico.model.SubscriptionChallenge;

public interface SubscriptionChallengeService {

    List<SubscriptionChallenge> getAllSubscriptionChallenges();
    SubscriptionChallenge getSubscriptionChallengeById(Integer id);
    Integer createSubscriptionChallenge(SubscriptionChallenge subscriptionChallenge);
    SubscriptionChallenge updateSubscriptionChallenge(Integer id, SubscriptionChallenge subscriptionChallenge);
    void deleteSubscriptionChallenge(Integer id);
    List<SubscriptionChallenge> getChallengesByUser(Integer userId);
}
