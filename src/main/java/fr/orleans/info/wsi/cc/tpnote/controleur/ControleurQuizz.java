package fr.orleans.info.wsi.cc.tpnote.controleur;

import fr.orleans.info.wsi.cc.tpnote.modele.FacadeQuizz;
import fr.orleans.info.wsi.cc.tpnote.modele.ResultatVote;
import fr.orleans.info.wsi.cc.tpnote.modele.Utilisateur;
import fr.orleans.info.wsi.cc.tpnote.modele.exceptions.*;
import fr.orleans.info.wsi.cc.tpnote.modele.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.net.URI;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/quizz" , produces = MediaType.APPLICATION_JSON_VALUE)

public class ControleurQuizz {

    @Autowired
    FacadeQuizz facadeQuizz;
    String location = "/api/quizz";


    @PostMapping(value = "/utilisateur")
    public ResponseEntity creerUtilisateur(@RequestParam String pseudo, @RequestParam String password){
        try {
            int loca = facadeQuizz.creerUtilisateur(pseudo,password);
            return  ResponseEntity.created(URI.create(location+"/utilisateur/"+loca)).build();
        } catch (EmailNonValideException e) {
            return  ResponseEntity.status(406).build();
        } catch (EmailDejaUtiliseException e) {
            return ResponseEntity.status(409).build();
        } catch (MotDePasseObligatoireException e) {
            return ResponseEntity.status(406).build();
        }
    }

    @GetMapping(value = "/utilisateur/{id}")
    public ResponseEntity<Utilisateur> getUser(Principal principal , @PathVariable int id) throws UtilisateurInexistantException {
        String login = principal.getName();
        Utilisateur u = facadeQuizz.getUtilisateurByEmail(login);
        if(u.getIdUtilisateur() != id) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(u);
    }

    @PostMapping(value = "/question")
    public ResponseEntity poserQuestion(Authentication authentication,@RequestBody Question question) throws EmailInexistantException {
        List<String> roles = authentication.getAuthorities().stream()
                .map(r -> r.getAuthority()).collect(Collectors.toList());
        if(!roles.contains("ROLE_PROFESSEUR")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        try {
            String id = facadeQuizz.creerQuestion(facadeQuizz.getIdUserByEmail(authentication.getName()),question.getLibelleQuestion(), question.getReponsesPossibles());
            return ResponseEntity.created(URI.create(location+"/question/"+id)).build();
        } catch (AuMoinsDeuxReponsesException | LibelleQuestionNonRenseigneException e) {
            return ResponseEntity.status(406).build();
        }
    }

    @GetMapping(value = "/question/{idQuestion}")
    public ResponseEntity getQuestion(@PathVariable String idQuestion){
        try {
            return ResponseEntity.ok(facadeQuizz.getQuestionById(idQuestion));
        } catch (QuestionInexistanteException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(value = "/question/{idQuestion}/vote")
    public ResponseEntity voter(Authentication authentication, @PathVariable String idQuestion, @RequestParam int idReponse ) throws EmailInexistantException {
        List<String> roles = authentication.getAuthorities().stream()
                .map(r -> r.getAuthority()).collect(Collectors.toList());
        if(!roles.contains("ROLE_ETUDIANT")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        try {
            facadeQuizz.voterReponse(facadeQuizz.getIdUserByEmail(authentication.getName()),idQuestion, idReponse);
            return ResponseEntity.accepted().build();
        } catch (QuestionInexistanteException e) {
            return ResponseEntity.notFound().build();
        } catch (NumeroPropositionInexistantException e) {
            return ResponseEntity.status(406).build();
        } catch (ADejaVoteException e) {
            return ResponseEntity.status(409).build();
        }
    }
    @GetMapping(value = "/question/{idQuestion}/vote")
    public ResponseEntity<ResultatVote[]> getVotes(Authentication authentication, @PathVariable String idQuestion){
        List<String> roles = authentication.getAuthorities().stream()
                .map(r -> r.getAuthority()).collect(Collectors.toList());
        if(!roles.contains("ROLE_PROFESSEUR")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        try {
            return ResponseEntity.ok(facadeQuizz.getResultats(idQuestion));
        } catch (QuestionInexistanteException e) {
            return ResponseEntity.notFound().build();
        }

    }






}
