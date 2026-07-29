package com.nodo.retotecnico.config;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nodo.retotecnico.model.Content;
import com.nodo.retotecnico.repository.ContentsRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContentsSeederTest {

    @Mock
    private ContentsRepository contentsRepository;

    @InjectMocks
    private ContentsSeeder contentsSeeder;

    @Captor
    private ArgumentCaptor<List<Content>> contentCaptor;

    @Test
    public void whenNeitherSectionIsSeeded_thenSeedsBoth() {
        // Arrange
        when(contentsRepository.findBySectionAndDeletedFalseOrderByDisplayOrderAsc("register_form"))
                .thenReturn(Collections.emptyList());
        when(contentsRepository.findBySectionAndDeletedFalseOrderByDisplayOrderAsc("login_form"))
                .thenReturn(Collections.emptyList());

        // Act
        contentsSeeder.run();

        // Assert
        verify(contentsRepository, times(1)).saveAll(contentCaptor.capture());
        List<Content> saved = contentCaptor.getValue();
        
        // 7 register fields + 2 login fields = 9 fields total
        assertEquals(9, saved.size());
        
        long registerCount = saved.stream().filter(c -> "register_form".equals(c.getSection())).count();
        long loginCount = saved.stream().filter(c -> "login_form".equals(c.getSection())).count();
        
        assertEquals(7, registerCount);
        assertEquals(2, loginCount);
    }

    @Test
    public void whenRegisterIsSeededButLoginIsNot_thenSeedsOnlyLogin() {
        // Arrange
        Content registerField = new Content();
        registerField.setSection("register_form");
        
        when(contentsRepository.findBySectionAndDeletedFalseOrderByDisplayOrderAsc("register_form"))
                .thenReturn(List.of(registerField));
        when(contentsRepository.findBySectionAndDeletedFalseOrderByDisplayOrderAsc("login_form"))
                .thenReturn(Collections.emptyList());

        // Act
        contentsSeeder.run();

        // Assert
        verify(contentsRepository, times(1)).saveAll(contentCaptor.capture());
        List<Content> saved = contentCaptor.getValue();
        
        assertEquals(2, saved.size());
        assertTrue(saved.stream().allMatch(c -> "login_form".equals(c.getSection())));
    }

    @Test
    public void whenLoginIsSeededButRegisterIsNot_thenSeedsOnlyRegister() {
        // Arrange
        Content loginField = new Content();
        loginField.setSection("login_form");
        
        when(contentsRepository.findBySectionAndDeletedFalseOrderByDisplayOrderAsc("register_form"))
                .thenReturn(Collections.emptyList());
        when(contentsRepository.findBySectionAndDeletedFalseOrderByDisplayOrderAsc("login_form"))
                .thenReturn(List.of(loginField));

        // Act
        contentsSeeder.run();

        // Assert
        verify(contentsRepository, times(1)).saveAll(contentCaptor.capture());
        List<Content> saved = contentCaptor.getValue();
        
        assertEquals(7, saved.size());
        assertTrue(saved.stream().allMatch(c -> "register_form".equals(c.getSection())));
    }

    @Test
    public void whenBothSectionsAreSeeded_thenNoSeedingOccurs() {
        // Arrange
        Content registerField = new Content();
        registerField.setSection("register_form");
        Content loginField = new Content();
        loginField.setSection("login_form");
        
        when(contentsRepository.findBySectionAndDeletedFalseOrderByDisplayOrderAsc("register_form"))
                .thenReturn(List.of(registerField));
        when(contentsRepository.findBySectionAndDeletedFalseOrderByDisplayOrderAsc("login_form"))
                .thenReturn(List.of(loginField));

        // Act
        contentsSeeder.run();

        // Assert
        verify(contentsRepository, never()).saveAll(any());
    }
}