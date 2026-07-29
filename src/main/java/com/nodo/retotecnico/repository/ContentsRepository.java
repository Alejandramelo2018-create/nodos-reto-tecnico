package com.nodo.retotecnico.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.retotecnico.model.Content;

@Repository
public interface  ContentsRepository extends JpaRepository<Content,Integer>{

    List<Content> findBySectionAndDeletedFalseOrderByDisplayOrderAsc(String section);

}