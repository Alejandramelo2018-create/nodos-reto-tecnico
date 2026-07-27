package com.nodo.retotecnico.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.retotecnico.model.SubscriptionChallenge;

@Repository
public interface SubscriptionChallengeRepository extends JpaRepository<SubscriptionChallenge, Integer> {
    List<SubscriptionChallenge> findByUserId(Integer userId);
}
