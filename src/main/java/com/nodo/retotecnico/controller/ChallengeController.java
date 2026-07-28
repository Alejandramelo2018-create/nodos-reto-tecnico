package com.nodo.retotecnico.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nodo.retotecnico.model.Challenge;
import com.nodo.retotecnico.service.ChallengeService;

@RestController
@RequestMapping("/nodos/challenges")
public class ChallengeController {

    @Autowired
    private ChallengeService challengeService;

    @GetMapping
    public List<Challenge> getAllChallenges() {
        return challengeService.getAllChallenges();
    }

    @GetMapping("/{id}")
    public Challenge getChallengeById(@PathVariable Integer id) {
        return challengeService.getChallengeById(id);
    }

    @PostMapping("/create")
    public Integer createChallenge(@RequestBody Challenge challenge) {
        return challengeService.createChallenge(challenge);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Challenge> updateChallenge(@PathVariable Integer id, @RequestBody Challenge challenge) {
        return ResponseEntity.ok(challengeService.updateChallenge(id, challenge));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteChallenge(@PathVariable Integer id) {
        challengeService.deleteChallenge(id);
        return ResponseEntity.ok("Challenge deleted successfully");
    }
}
