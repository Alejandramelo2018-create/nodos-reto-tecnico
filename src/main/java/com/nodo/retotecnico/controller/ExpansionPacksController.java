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

import com.nodo.retotecnico.model.ExpansionPack;
import com.nodo.retotecnico.service.ExpansionPacksService;

@RestController
@RequestMapping("/nodos/expansionpacks")
public class ExpansionPacksController {

    @Autowired
    private ExpansionPacksService expansionPacksService;

    @GetMapping
    public List<ExpansionPack> getAllExpansionPacks() {
        return expansionPacksService.getAllExpansionPacks();
    }

    @GetMapping ("/{id}")
    public ExpansionPack getExpansionPacksById(@PathVariable Integer id) {
        return expansionPacksService.getExpansionPacksById(id);
    }
    @PostMapping("/create")
    public Integer createExpansionPack(@RequestBody ExpansionPack expansionPack) {
        return expansionPacksService.createExpansionPack(expansionPack);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ExpansionPack> updateExpansionPack(@PathVariable Integer id, @RequestBody ExpansionPack expansionPack){
        return ResponseEntity.ok(expansionPacksService.updateExpansionPack(id, expansionPack));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExpansionPack(@PathVariable Integer id){
        expansionPacksService.deleteExpansionPack(id);
        return ResponseEntity.ok("Expansion Pack deleted successfully");
    }
}
