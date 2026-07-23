package com.nodo.retotecnico.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nodo.retotecnico.model.Content;
import com.nodo.retotecnico.repository.ContentsRepository;
import com.nodo.retotecnico.service.ContentsService;

@Service
public class ContentsServiceImpl implements ContentsService{

    @Autowired
    private ContentsRepository contentsRepository;

    @Override
    public List<Content> getAllContents() {
        return contentsRepository.findAll();
    }

    @Override
    public Content getContentsById(Integer id){
        return contentsRepository.findById(id).orElse(null);
    }

    @Override
    public Integer createContent(Content content) {
        return contentsRepository.save(content).getId();
    }

    @Override
    public Content updateContent(Integer id, Content content){
        Content existingContent = contentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found"));
        existingContent.setTitle(content.getTitle());
        existingContent.setDescription(content.getDescription());
        return contentsRepository.save(existingContent);
    }
    @Override
    public void deleteContent(Integer id){
        if (!contentsRepository.existsById(id)){
            throw new RuntimeException("Content not found");
        }
        contentsRepository.deleteById(id);
    }
}

