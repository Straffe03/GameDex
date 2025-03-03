package cat.copernic.gamedex.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import cat.copernic.gamedex.entity.Videogame;

@Repository
// Interfície que hereta de MongoRepository, que permet interactuar amb la base de dades
public interface VideogameRepository extends MongoRepository<Videogame, String> {
    
    // Mètode per buscar videojocs per nom 
    Optional<Videogame> findByNameGame(String nameGame);

    // Mètode per buscar videojocs per estat
    List<Videogame> findByState(boolean state);

    // Mètode per buscar videojocs per categoria
    List<Videogame> findByCategory(String category);

    List<Videogame> findByNameGameContaining(String nameGame);
}
