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

import com.nodo.retotecnico.model.Content;
import com.nodo.retotecnico.service.ContentsService;

@RestController
@RequestMapping("/nodos/contents")
public class ContentsController {

    @Autowired
    private ContentsService contentsService;

    @GetMapping
    public List<Content> getAllContents() {
        return contentsService.getAllContents();
    }

    @GetMapping ("/{id}")
    public Content getContentsById(@PathVariable Integer id) {
        return contentsService.getContentsById(id);
    }
    @PostMapping("/create")
    public Integer createContent(@RequestBody Content content) {
        try {
            return contentsService.createContent(content);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<Content> updateContent(@PathVariable Integer id, @RequestBody Content content){
        return ResponseEntity.ok(contentsService.updateContent(id, content));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContent(@PathVariable Integer id){
        contentsService.deleteContent(id);
        return ResponseEntity.ok("Content deleted successfully");
    }
}   
