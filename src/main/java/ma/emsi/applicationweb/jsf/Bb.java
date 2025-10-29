package ma.emsi.applicationweb.jsf; // A modifier...

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.applicationweb.llm.LlmClient; // Importez votre client LLM
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;
// ... (autres imports)

@Named
@ViewScoped
public class Bb implements Serializable {

    // ... (variables d'instance existantes) ...

    /**
     * Client LLM injecté pour interagir avec l'API Gemini via LangChain4j.
     */
    @Inject
    private LlmClient llmClient; // <--- Nouvelle injection

    // ... (constructeur et autres getters/setters existants) ...

    /**
     * Envoie la question au LLM via LlmClient.
     *
     * @return null pour rester sur la même page.
     */
    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        // Si la conversation n'a pas encore commencé, initialiser le rôle système
        if (this.conversation.isEmpty()) {
            // Vider la mémoire et définir le rôle système dans LlmClient
            llmClient.setSystemRole(roleSysteme); // <--- Appel de la nouvelle méthode
            // Invalide le bouton pour changer le rôle système
            this.roleSystemeChangeable = false;
        }

        try {
            // Déléguer l'appel à l'API LLM au LlmClient
            this.reponse = llmClient.envoyerQuestion(question); // <--- Appel de la méthode
        } catch (Exception e) {
            this.reponse = "ERREUR LORS DE L'APPEL AU LLM : " + e.getMessage();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Erreur LLM", this.reponse);
            facesContext.addMessage(null, message);
        }

        // La conversation contient l'historique des questions-réponses depuis le début.
        afficherConversation();
        return null;
    }

    // ... (méthode nouveauChat(), afficherConversation(), getRolesSysteme() existantes) ...
}