package ma.emsi.applicationweb.llm; // À ajuster

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.gemini.GeminiChatModel; // Pour Gemini
import dev.langchain4j.service.AiServices;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage; // Nécessaire pour ajouter un SystemMessage

import jakarta.enterprise.context.Dependent;
import java.io.Serializable;

/**
 * Gère l'interface avec l'API de Gemini (Modèle gemini-2.5-flash) via LangChain4j.
 * La portée @Dependent n'est plus nécessaire ici car ChatMemory gère l'état,
 * mais la portée 'view' du backing bean et la gestion de ChatMemory par LangChain4j
 * suffisent pour l'état de la conversation. L'annotation @Dependent est conservée
 * si elle était requise par votre configuration CDI.
 */
@Dependent
public class LlmClient implements Serializable {
    private static final String API_KEY_ENV_VAR = "GEMINI";
    private static final String MODEL_NAME = "gemini-2.5-flash"; // Modèle demandé

    // Rôle système actuel (conservé pour le setter)
    private String systemRole;

    // L'interface implémentée par LangChain4j
    private Assistant assistant;

    // Mémoire de conversation gérée par LangChain4j
    private final ChatMemory chatMemory;

    public LlmClient() {
        // 1. Récupère la clé secrète en utilisant la variable d'environnement
        String key = System.getenv(API_KEY_ENV_VAR);

        if (key == null || key.trim().isEmpty()) {
            throw new IllegalStateException("La variable d'environnement '" + API_KEY_ENV_VAR + "' (clé API) n'est pas définie ou est vide.");
        }

        // 2. Configuration de la mémoire : fenêtre de 10 messages
        this.chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // 3. Création du modèle de chat
        ChatLanguageModel model = GeminiChatModel.builder()
                .apiKey(key)
                .modelName(MODEL_NAME)
                .build();

        // 4. Création de l'Assistant (instance proxy) via AiServices
        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * Définit le rôle système pour l'assistant.
     * Vide la mémoire avant d'ajouter le nouveau rôle système pour un nouveau contexte.
     *
     * @param newSystemRole Le rôle à attribuer à l'assistant.
     */
    public void setSystemRole(String newSystemRole) {
        if (!newSystemRole.equals(this.systemRole)) {
            // Le rôle a changé : vider la mémoire et ajouter le nouveau rôle système
            this.chatMemory.clear();
            this.systemRole = newSystemRole;

            // Ajouter le rôle système comme SystemMessage à la mémoire
            // MessageWindowChatMemory retient toujours le SystemMessage
            this.chatMemory.add(SystemMessage.from(newSystemRole));
        }
        // Si le rôle est le même, ne rien faire.
    }

    /**
     * Envoie la question de l'utilisateur au LLM et reçoit la réponse.
     * LangChain4j ajoute automatiquement la question à la mémoire avant l'appel
     * et la réponse après.
     *
     * @param question La question de l'utilisateur.
     * @return La réponse générée par le LLM.
     */
    public String envoyerQuestion(String question) {
        // La méthode `chat` de l'interface `Assistant` gère tout :
        // 1. Ajout de la question à la ChatMemory.
        // 2. Envoi de l'historique (mémoire) + question au LLM.
        // 3. Retour de la réponse du LLM.
        // 4. Ajout de la réponse à la ChatMemory.
        return this.assistant.chat(question);
    }
}