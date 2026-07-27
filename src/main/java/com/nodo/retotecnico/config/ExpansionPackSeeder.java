package com.nodo.retotecnico.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.nodo.retotecnico.model.ExpansionPack;
import com.nodo.retotecnico.repository.ExpansionPacksRepository;

@Component
public class ExpansionPackSeeder implements CommandLineRunner {

    @Autowired
    private ExpansionPacksRepository expansionPacksRepository;

    @Override
    public void run(String... args) {
        if (expansionPacksRepository.count() > 0) {
            return;
        }
        expansionPacksRepository.saveAll(List.of(
            pack(
                "The Sims™ 4: Naturaleza Encantada",
                "PACK DE EXPANSIÓN",
                79900.0,
                "PC / Mac / Consolas",
                "¡Conéctate con la magia del bosque y la vida silvestre! Descubre criaturas místicas, aprende herbalismo, construye refugios sostenibles y domina el arte de la botánica mística en entornos naturales impresionantes.",
                List.of(
                    "Nuevo mundo explorable: Arboleda de las Hadas.",
                    "Habilidad especial: Herbalismo y Magia Natural.",
                    "Criaturas mágicas y duendes de bosque interactivos.",
                    "Muebles de madera rústica y vestuario de espíritu libre."
                ),
                "https://res.cloudinary.com/w1jl4sa5/image/upload/v1784572920/TS4_Pack-Art_Enchanted-by-Nature_ES_iv5fev.avif"
            ),
            pack(
                "The Sims™ 4: Dinastías y Linajes",
                "PACK DE EXPANSIÓN",
                79900.0,
                "PC / Mac / Consolas",
                "Crea reinos prósperos, define la herencia de tus familias reales y domina el arte del protocolo o la intriga palaciega. En este pack podrás construir castillos majestuosos, organizar banquetes suntuosos y guiar el destino de tu linaje a través de las generaciones.",
                List.of(
                    "Mundo exclusivo: La Región de Ciudad Real.",
                    "Mecánicas de corte, títulos de nobleza y árboles genealógicos avanzados.",
                    "Muebles de construcción medieval y vestuario de gala suntuoso.",
                    "Nuevas aspiraciones: Monarca Justo o Maquiavélico."
                ),
                "https://res.cloudinary.com/w1jl4sa5/image/upload/v1784574803/TS4_Pack-Art_Royalty-and-Legacy_ES_zggtwv.avif"
            ),
            pack(
                "The Sims™ 4: Rancho de Caballos",
                "PACK DE EXPANSIÓN",
                89900.0,
                "PC / Mac / Consolas",
                "¡Forma parte de la comunidad de Chestnut Ridge y crea tu propio estilo de vida ecuestre! Cuida, entrena y entabla vínculos con majestuosos caballos, personaliza tu rancho y vive la experiencia del campo.",
                List.of(
                    "Mundo explorable: Chestnut Ridge.",
                    "Cuidado, entrenamiento y crianza de caballos.",
                    "Fabricación de néctar y baile en línea.",
                    "Nuevos animales: Mini cabras y mini ovejas."
                ),
                "https://res.cloudinary.com/w1jl4sa5/image/upload/v1784574889/TS4_Pack-Art_HorseRanch_ES_r2ddxj.avif"
            ),
            pack(
                "The Sims™ 4: Vida en el Pueblo",
                "PACK DE EXPANSIÓN",
                69900.0,
                "PC / Mac / Consolas",
                "Disfruta de la vida rural creando una granja, cuidando animales y explorando un encantador pueblo en Henford-on-Bagley. Cultiva productos frescos, cría animales y participa en las tradicionales ferias del pueblo.",
                List.of(
                    "Descubre la vida rural en Henford-on-Bagley.",
                    "Cultiva, cría animales, participa en ferias y disfruta de una comunidad llena de tradiciones.",
                    "Aleja a tus Sims del bullicio de la ciudad y llévalos al campo. ¡Construye una granja!",
                    "Completa actividades diarias y disfruta de las tradicionales ferias rurales."
                ),
                "https://res.cloudinary.com/w1jl4sa5/image/upload/v1784574983/ES_Sims4-cottage-living-1x1-Loc_rp5yll.avif"
            ),
            pack(
                "The Sims™ 4: Perros y Gatos",
                "PACK DE EXPANSIÓN",
                69900.0,
                "PC / Mac / Consolas",
                "Haz crecer tu familia con adorables mascotas y vive aventuras inolvidables junto a ellas en el hermoso mundo costero de Brindleton Bay.",
                List.of(
                    "Abre tu propia clínica veterinaria y dedica tu vida al cuidado de las mascotas.",
                    "Cuida, entrena y juega con perros y gatos mientras fortaleces el vínculo con tus Sims.",
                    "Personaliza mascotas únicas y acompaña a tus Sims en una aventura llena de cariño y compañía.",
                    "Adopta perros y gatos, cuídalos con cariño y crea un hogar lleno de amor y diversión."
                ),
                "https://res.cloudinary.com/w1jl4sa5/image/upload/v1784575067/ES_Sims4-cats-and-dogs-1x1-Loc_rw592n.avif"
            ),
            pack(
                "The Sims™ 4: ¡A Trabajar!",
                "PACK DE EXPANSIÓN",
                74900.0,
                "PC / Mac / Consolas",
                "Acompaña a tus Sims al trabajo y descubre profesiones llenas de desafíos, diversión y la oportunidad de crear tu propio negocio.",
                List.of(
                    "Controla cada jornada laboral y construye una carrera llena de éxito y nuevas oportunidades.",
                    "Explora nuevas profesiones y decide el futuro laboral de tus Sims en cada jornada.",
                    "Conviértete en médico, detective o científico y vive emocionantes aventuras profesionales.",
                    "¡Abre tu propio negocio y haz crecer un imperio con tus habilidades emprendedoras!"
                ),
                "https://res.cloudinary.com/w1jl4sa5/image/upload/v1784575123/ES_Sims4-get-to-work-1x1-Loc_saiuva.avif"
            )
        ));
    }

    private ExpansionPack pack(String name, String category, double price, String platforms,
                                String description, List<String> characteristics, String urlImage) {
        ExpansionPack expansionPack = new ExpansionPack();
        expansionPack.setName(name);
        expansionPack.setDescription(description);
        expansionPack.setPlatforms(platforms);
        expansionPack.setPrice(price);
        expansionPack.setCategory(category);
        expansionPack.setPublicationDate("Lanzamiento Reciente");
        expansionPack.setLanguage("es");
        expansionPack.setURLImage(urlImage);
        expansionPack.setCharacteristics(characteristics);
        return expansionPack;
    }
}
