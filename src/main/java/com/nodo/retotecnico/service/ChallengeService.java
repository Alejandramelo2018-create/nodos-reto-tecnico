package com.nodo.retotecnico.service;

import java.util.List;

import com.nodo.retotecnico.model.Challenge;

public interface ChallengeService {

    List<Challenge> getAllChallenges();
    Challenge getChallengeById(Integer id);
    Integer createChallenge(Challenge challenge);
    Challenge updateChallenge(Integer id, Challenge challenge);
    void deleteChallenge(Integer id);
}
