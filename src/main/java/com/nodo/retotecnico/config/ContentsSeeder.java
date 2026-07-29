package com.nodo.retotecnico.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nodo.retotecnico.model.Content;
import com.nodo.retotecnico.repository.ContentsRepository;

/**
 * Siembra los campos de formularios como filas de "contents", agrupadas por
 * "section". Cada fila = un campo del formulario (title = label del campo,
 * fieldName/fieldType/required/options/displayOrder describen cómo pedirlo
 * y de qué tipo de dato es), para que el front arme el formulario leyendo
 * de la base de datos en vez de tenerlo hardcodeado.
 */
@Component
@Order(2)
public class ContentsSeeder implements CommandLineRunner {

    private static final String SECTION_REGISTER = "register_form";
    private static final String SECTION_LOGIN = "login_form";

    @Autowired
    private ContentsRepository contentsRepository;

    @Override
    @Transactional
    public void run(String... args) {
        boolean registerSeeded = !contentsRepository
                .findBySectionAndDeletedFalseOrderByDisplayOrderAsc(SECTION_REGISTER)
                .isEmpty();

        boolean loginSeeded = !contentsRepository
                .findBySectionAndDeletedFalseOrderByDisplayOrderAsc(SECTION_LOGIN)
                .isEmpty();

        List<Content> fieldsToSave = new ArrayList<>();

        if (!registerSeeded) {
            fieldsToSave.addAll(List.of(
                field(SECTION_REGISTER, "Nombre", "firstName", "TEXT", true, "", 1),
                field(SECTION_REGISTER, "Apellido", "lastName", "TEXT", true, "", 2),
                field(SECTION_REGISTER, "Usuario", "username", "TEXT", true, "", 3),
                field(SECTION_REGISTER, "Correo electrónico", "email", "EMAIL", true, "", 4),
                field(SECTION_REGISTER, "País", "country", "SELECT", true,
                    "Colombia,México,Argentina,Chile,Perú,España", 5),
                field(SECTION_REGISTER, "Contraseña", "password", "PASSWORD", true, "", 6),
                field(SECTION_REGISTER, "Confirmar contraseña", "confirmPassword", "PASSWORD", true, "", 7)
            ));
        }

        if (!loginSeeded) {
            fieldsToSave.addAll(List.of(
                field(SECTION_LOGIN, "Usuario", "username", "TEXT", true, "", 1),
                field(SECTION_LOGIN, "Contraseña", "password", "PASSWORD", true, "", 2)
            ));
        }

        if (!fieldsToSave.isEmpty()) {
            contentsRepository.saveAll(fieldsToSave);
        }
    }

    private Content field(String section, String title, String fieldName, String fieldType,
                           boolean required, String options, int displayOrder) {
        Content content = new Content();
        content.setSection(section);
        content.setTitle(title);
        content.setFieldName(fieldName);
        content.setFieldType(fieldType);
        content.setRequired(required);
        content.setOptions(options);
        content.setDisplayOrder(displayOrder);
        return content;
    }
}