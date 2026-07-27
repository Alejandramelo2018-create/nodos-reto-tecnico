package com.nodo.retotecnico.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nodo.retotecnico.model.Challenge;
import com.nodo.retotecnico.repository.ChallengeRepository;
import com.nodo.retotecnico.service.ChallengeService;

@Service
public class ChallengeServiceImpl implements ChallengeService {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Override
    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    @Override
    public Challenge getChallengeById(Integer id) {
        return challengeRepository.findById(id).orElse(null);
    }

    @Override
    public Integer createChallenge(Challenge challenge) {
        return challengeRepository.save(challenge).getId();
    }

    @Override
    public Challenge updateChallenge(Integer id, Challenge challenge) {
        Challenge existingChallenge = challengeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));
        existingChallenge.setName(challenge.getName());
        existingChallenge.setStart(challenge.getStart());
        existingChallenge.setEnd(challenge.getEnd());
        existingChallenge.setDescription(challenge.getDescription());
        existingChallenge.setImageURL(challenge.getImageURL());
        return challengeRepository.save(existingChallenge);
    }

    @Override
    public void deleteChallenge(Integer id) {
        challengeRepository.deleteById(id);
    }
}
