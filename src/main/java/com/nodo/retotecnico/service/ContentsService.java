package com.nodo.retotecnico.service;

import java.util.List;

import com.nodo.retotecnico.model.Content;

public interface ContentsService {

    List<Content> getAllContents();
    Content getContentsById(Integer id);
    List<Content> getContentsBySection(String section);
    Integer createContent(Content content);
    Content updateContent(Integer id, Content content);
    void deleteContent(Integer id);
}