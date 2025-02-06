package cat.copernic.gamedex.logic;

import cat.copernic.gamedex.entity.Commentary;
import cat.copernic.gamedex.repository.CommentaryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentaryLogic {

    @Autowired
    private CommentaryRepository commentaryRepository;

    public Commentary createCommentary(Commentary commentary) {

        try {
            Optional<Commentary> oldCommentary = commentaryRepository.findById(commentary.getIdCommentary());
            if (oldCommentary.isPresent()) {
                throw new RuntimeException("Commentary already exists");
            }
            return commentaryRepository.save(commentary);
        } catch(RuntimeException e){
            throw e;
        } catch(Exception e){
            throw new RuntimeException("Unexpected error while creating commentary");
        }
    }
    
    public void deleteCommentary(String idCommentary){
        try{
            Optional<Commentary> commentary = commentaryRepository.findById(idCommentary);
            if (commentary.isPresent()) {
                commentaryRepository.deleteById(idCommentary);
            }else{
                throw new RuntimeException("Commentary not found");
            } 
        } catch(RuntimeException e){
            throw e;
        } catch(Exception e){
            throw new RuntimeException("Unexpected error while deleting commentary");
        }
        
    }
    
    public List<Commentary> getAllCommentaries(){
        try{
           return commentaryRepository.findAllByVideogame();
        }catch(Exception e){
            throw new RuntimeException("Unexpected error while listing commentaries");
        }
    }
}

