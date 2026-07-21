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
import com.nodo.retotecnico.model.Subscription;
import com.nodo.retotecnico.service.SubscriptionService;

@RestController
@RequestMapping("/nodos/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping
    public List<Subscription> getAllSubscriptions() {
        return subscriptionService.getAllSubscriptions();
    }

    @GetMapping("/{id}")
    public Subscription getSubscriptionById(@PathVariable Integer id) {
        return subscriptionService.getSubscriptionById(id);
    }

    @PostMapping("/create")
    public Integer createSubscription(@RequestBody Subscription subscription) {
        return subscriptionService.createSubscription(subscription);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subscription> updateSubscription(@PathVariable Integer id, @RequestBody Subscription subscription) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(id, subscription));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubscription(@PathVariable Integer id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.ok("Subscription deleted successfully");
    }
}
