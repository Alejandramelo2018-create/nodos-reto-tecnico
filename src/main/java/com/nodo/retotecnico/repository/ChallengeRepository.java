package com.nodo.retotecnico.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.retotecnico.model.Challenge;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Integer> {
}
