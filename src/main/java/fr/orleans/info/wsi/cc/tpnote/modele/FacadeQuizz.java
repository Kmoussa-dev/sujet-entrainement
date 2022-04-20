package fr.orleans.info.wsi.cc.tpnote.modele;

import fr.orleans.info.wsi.cc.tpnote.modele.exceptions.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class FacadeQuizz {

    private HashMap<String, Utilisateur> mapUtilisateur;
    private Map<Integer, Collection<Question>> mapUtilisateurQuestions;
    private Map<String, Question> mapQuestions;


    public FacadeQuizz(){
        mapUtilisateur = new HashMap<>();
        mapUtilisateurQuestions = new HashMap<>();
        mapQuestions = new HashMap<>();
    }


    /**
     *
     * @param email : email valide
     * @param password : mot de passe utilisateur non vide et chiffré (lors de son intégration au web-service)
     * @return identifiant entier
     * @throws EmailDejaUtiliseException : email déjà utilisé
     * @throws EmailNonValideException : email n'est pas de la bonne forme
     * @throws MotDePasseObligatoireException : le mot de passe est Blank ou nul
     */
    public int creerUtilisateur(String email,String password) throws EmailNonValideException, EmailDejaUtiliseException, MotDePasseObligatoireException {
        if(!OutilsPourValidationEmail.patternMatches(email)) throw  new EmailNonValideException();
        if(mapUtilisateur.containsKey(email)) throw new EmailDejaUtiliseException();
        if(password == null ) throw new MotDePasseObligatoireException();
        if(password.isBlank()) throw new MotDePasseObligatoireException();
        Utilisateur utilisateur = new Utilisateur(email,password);
        mapUtilisateur.put(email,utilisateur);
        return utilisateur.getIdUtilisateur();
    }

    /**
     * Permet de récupérer l'identifiant int d'un utilisateur par son E-mail
     * @param email
     * @return identifiant int
     */

    public int getIdUserByEmail(String email) throws EmailInexistantException {
        Utilisateur utilisateur = mapUtilisateur.get(email);
        if(utilisateur== null) throw new EmailInexistantException();
        return utilisateur.getIdUtilisateur();
    }

    /**
     * Permet à un professeur de créer une question
     * @param idUser id du professeur (on suppose qu'uniquement les professeurs pourront accéder à cette fonctionnalité donc
     *               pas besoin de vérifier s'il s'agit d'un professeur ou s'il s'agit d'un utilisateur existant)
     * @param libelleQuestion : libellé de la question
     * @param libellesReponses : libellés des réponses possibles
     * @return identifiant aléatoire chaîne de caractère (UUID)
     * @throws AuMoinsDeuxReponsesException : au moins deux réponses sont attendues
     * @throws LibelleQuestionNonRenseigneException : le libellé doit être obligatoirement non vide (non blank)
     */

    public String creerQuestion(int idUser, String libelleQuestion, String... libellesReponses) throws AuMoinsDeuxReponsesException, LibelleQuestionNonRenseigneException {
        if(libelleQuestion == null ) throw  new LibelleQuestionNonRenseigneException();
        if(libelleQuestion.isBlank()) throw new LibelleQuestionNonRenseigneException();
        if(libellesReponses.length<2) throw new AuMoinsDeuxReponsesException();
        Question question = new Question(idUser,libelleQuestion,libellesReponses);
        if(mapUtilisateurQuestions.containsKey(idUser)){
            mapUtilisateurQuestions.get(idUser).add(question);
        }else {
            Collection<Question> questions = new ArrayList<>(); questions.add(question);
            mapUtilisateurQuestions.put(idUser, questions);
        }
        mapQuestions.put(question.getIdQuestion(),question);
        return question.getIdQuestion();
    }


    /**
     * Permet de récupérer une question par son identifiant
     * @param idQuestion : id de la question concernée
     * @return l'objet Question concerné
     * @throws QuestionInexistanteException : l'identifiant donné ne correspond à aucune question
     */

    public Question getQuestionById(String idQuestion) throws QuestionInexistanteException {
        Question q = mapQuestions.get(idQuestion);
        if(q==null) throw new QuestionInexistanteException();
        return q;
    }

    /**
     * Permet à un étudiant de voter pour une réponse
     * @param idUser : identifiant entier de l'étudiant en question (là encore on suppose que l'idUser est correct et que c'est bien un étudiant. Cette
     *               vérification est déléguée au contrôleur REST)
     * @param idQuestion : identifiant de la question concernée
     * @param numeroProposition : numéro de la proposition (les réponses possibles sont stockées dans un tableau donc le
     *                          numéro correspond à l'indice dans le tableau)
     * @throws ADejaVoteException : l'étudiant concerné a déjà voté à cette question (éventuellement pour une autre réponse)
     * @throws NumeroPropositionInexistantException : le numéro de la proposition n'est pas un indice correct du tableau des propositions
     * de la question
     * @throws QuestionInexistanteException : la question identifiée n'existe pas
     */

    public void voterReponse(int idUser,String idQuestion, int numeroProposition) throws QuestionInexistanteException, NumeroPropositionInexistantException, ADejaVoteException {
        Utilisateur u = mapUtilisateur.get(idUser);
        Question q = mapQuestions.get(idQuestion);
        if(q == null) throw new QuestionInexistanteException();
        q.voterPourUneReponse(idUser,numeroProposition);

    }


    /**
     * Vous devez dans la fonction ci-dessous vider toutes vos structures de données.
     * Pensez à remettre à 0 vos éventuels compteurs statiques (probablement dans la classe utilisateur)
     */

    public void reinitFacade(){
        mapUtilisateurQuestions = new HashMap<>();
        mapUtilisateur = new HashMap<>();
        mapQuestions = new HashMap<>();
        Utilisateur.resetCompteur();

    //TODO
    }


    /**
     * Permet de récupérer un utilisateur par son email
     * @param username
     * @return
     */
    public Utilisateur getUtilisateurByEmail(String username) throws UtilisateurInexistantException {
        if(!mapUtilisateur.containsKey(username)) throw new UtilisateurInexistantException();
        return mapUtilisateur.get(username);
    }


    /**
     * Permet de récupérer le résultat d'un vote à une question
     * @param idQuestion
     * @return
     * @throws QuestionInexistanteException
     */

    public ResultatVote[] getResultats(String idQuestion) throws QuestionInexistanteException {
        Question q = mapQuestions.get(idQuestion);
        if(q== null) throw new QuestionInexistanteException();
        return q.getResultats();
    }
}
