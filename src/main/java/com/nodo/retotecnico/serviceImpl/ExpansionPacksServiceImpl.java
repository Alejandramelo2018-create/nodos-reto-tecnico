package com.nodo.retotecnico.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nodo.retotecnico.model.ExpansionPack;
import com.nodo.retotecnico.repository.ExpansionPacksRepository;
import com.nodo.retotecnico.service.ExpansionPacksService;


@Service
public class ExpansionPacksServiceImpl implements ExpansionPacksService{

    @Autowired
    private ExpansionPacksRepository expansionPacksRepository;

    @Override
    public List<ExpansionPack> getAllExpansionPacks() {
        return expansionPacksRepository.findAll();
    }

    @Override
    public ExpansionPack getExpansionPacksById(Integer id) {
        return expansionPacksRepository.findById(id).orElse(null);
    }
    @Override
    public Integer createExpansionPack(ExpansionPack expansionPack) {
        return expansionPacksRepository.save(expansionPack).getId();
    }
    @Override
    public ExpansionPack updateExpansionPack(Integer id, ExpansionPack expansionPack){
        ExpansionPack existingExpansionPack = expansionPacksRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ExpansionPack not found"));
        existingExpansionPack.setName(expansionPack.getName());
        existingExpansionPack.setDescription(expansionPack.getDescription());
        existingExpansionPack.setDistributor(expansionPack.getDistributor());
        existingExpansionPack.setPrice(expansionPack.getPrice());
        existingExpansionPack.setCategory(expansionPack.getCategory());
        existingExpansionPack.setPublicationDate(expansionPack.getPublicationDate());
        existingExpansionPack.setLanguage(expansionPack.getLanguage());
        existingExpansionPack.setURLImage(expansionPack.getURLImage());
        existingExpansionPack.setCharacteristics(expansionPack.getCharacteristics());
        return expansionPacksRepository.save(existingExpansionPack);
    }

    @Override
    public void deleteExpansionPack(Integer id){
        ExpansionPack existingExpansionPack = expansionPacksRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ExpansionPack not found"));
        expansionPacksRepository.deleteById(id);
    }
}

