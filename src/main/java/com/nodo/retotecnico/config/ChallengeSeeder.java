package com.nodo.retotecnico.config;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.nodo.retotecnico.model.Challenge;
import com.nodo.retotecnico.repository.ChallengeRepository;

@Component
public class ChallengeSeeder implements CommandLineRunner {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Override
    public void run(String... args) {
        if (challengeRepository.count() > 0) {
            return;
        }
        challengeRepository.saveAll(List.of(
            challenge(
                "Reto: Jardín Encantado",
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 8, 15),
                "Construye el jardín más mágico posible: combina flora exótica, criaturas místicas y espacios de descanso al aire libre para tus Sims.",
                "https://placehold.co/600x800?text=Jardin+Encantado"
            ),
            challenge(
                "Reto: Dinastía Real",
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                "Funda un linaje real y llévalo a través de tres generaciones sin perder el trono: cuida el protocolo, las alianzas y el patrimonio familiar.",
                "https://placehold.co/600x800?text=Dinastia+Real"
            ),
            challenge(
                "Reto: Rancho Perfecto",
                LocalDate.of(2026, 7, 20),
                LocalDate.of(2026, 8, 10),
                "Levanta el rancho de caballos mejor cuidado de Chestnut Ridge: entrena, cría y compite con tus caballos hasta ganar tu primera copa.",
                "https://placehold.co/600x800?text=Rancho+Perfecto"
            ),
            challenge(
                "Reto: Vida de Pueblo",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                "Vive un mes entero de la tierra: cultiva, cría animales y participa en todas las ferias del pueblo sin comprar nada del supermercado.",
                "https://placehold.co/600x800?text=Vida+de+Pueblo"
            ),
            challenge(
                "Reto: Refugio de Mascotas",
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 9, 15),
                "Abre un refugio y llega a cuidar al mismo tiempo a la mayor cantidad de perros y gatos posible, todos felices y con buena salud.",
                "https://placehold.co/600x800?text=Refugio+de+Mascotas"
            ),
            challenge(
                "Reto: Emprendedor Simmer",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                "Arranca un negocio desde cero y conviértelo en un imperio rentable en menos de un mes de juego.",
                "https://placehold.co/600x800?text=Emprendedor+Simmer"
            )
        ));
    }

    private Challenge challenge(String name, LocalDate start, LocalDate end, String description, String imageURL) {
        Challenge challenge = new Challenge();
        challenge.setName(name);
        challenge.setStart(start);
        challenge.setEnd(end);
        challenge.setDescription(description);
        challenge.setImageURL(imageURL);
        return challenge;
    }
}
