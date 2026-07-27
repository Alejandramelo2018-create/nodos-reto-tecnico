package com.nodo.retotecnico.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nodo.retotecnico.model.ChallengeStatus;
import com.nodo.retotecnico.model.SubscriptionChallenge;
import com.nodo.retotecnico.model.User;
import com.nodo.retotecnico.repository.SubscriptionChallengeRepository;
import com.nodo.retotecnico.repository.UserRepository;
import com.nodo.retotecnico.service.SubscriptionChallengeService;

@Service
public class SubscriptionChallengeServiceImpl implements SubscriptionChallengeService {

    @Autowired
    private SubscriptionChallengeRepository subscriptionChallengeRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<SubscriptionChallenge> getAllSubscriptionChallenges() {
        return subscriptionChallengeRepository.findAll();
    }

    @Override
    public SubscriptionChallenge getSubscriptionChallengeById(Integer id) {
        return subscriptionChallengeRepository.findById(id).orElse(null);
    }

    @Override
    public Integer createSubscriptionChallenge(SubscriptionChallenge subscriptionChallenge) {
        return subscriptionChallengeRepository.save(subscriptionChallenge).getId();
    }

    @Override
    public SubscriptionChallenge updateSubscriptionChallenge(Integer id, SubscriptionChallenge subscriptionChallenge) {
        SubscriptionChallenge existing = subscriptionChallengeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubscriptionChallenge not found"));

        ChallengeStatus previousStatus = existing.getStatus();
        existing.setStatus(subscriptionChallenge.getStatus());

        SubscriptionChallenge saved = subscriptionChallengeRepository.save(existing);

        if (saved.getStatus() == ChallengeStatus.FINALIZADO && previousStatus != ChallengeStatus.FINALIZADO) {
            User user = saved.getUser();
            int current = user.getCompletedChallenges() == null ? 0 : user.getCompletedChallenges();
            user.setCompletedChallenges(current + 1);
            userRepository.save(user);
        }

        return saved;
    }

    @Override
    public void deleteSubscriptionChallenge(Integer id) {
        subscriptionChallengeRepository.deleteById(id);
    }

    @Override
    public List<SubscriptionChallenge> getChallengesByUser(Integer userId) {
        return subscriptionChallengeRepository.findByUserId(userId);
    }
}
