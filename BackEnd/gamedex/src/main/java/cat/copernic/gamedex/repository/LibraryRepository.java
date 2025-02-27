package cat.copernic.gamedex.repository;

import cat.copernic.gamedex.entity.Library;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface LibraryRepository extends MongoRepository<Library, String> {

    public List<Library> findAllByVideogame();

    @Query("SELECT l FROM Library l WHERE l.user.username = :username AND l.videogame.gameId = :gameId")
    Optional<Library> findByUserIdAndVideogameId(@Param("username") String username,
            @Param("gameId") String gameId);

}
