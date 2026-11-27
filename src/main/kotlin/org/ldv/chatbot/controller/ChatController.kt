
package org.ldv.chatbot.controller

import org.springframework.security.core.Authentication
import org.ldv.chatbot.model.dao.ConversationDAO
import org.ldv.chatbot.model.dao.MessageDAO
import org.ldv.chatbot.model.dao.ModeleIADAO
import org.ldv.chatbot.model.dao.UtilisateurDAO
import org.ldv.chatbot.model.entity.Conversation
import org.ldv.chatbot.model.entity.Message
import org.ldv.chatbot.model.entity.TypeMessage
import org.ldv.chatbot.model.entity.Utilisateur
import org.ldv.chatbot.service.IAServiceFactory
import org.ldv.chatbot.service.MarkdownService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

// Objet contenant le message et son contenu HTML nettoyé
data class MessageAffiche(
    val message: Message,
    val contenuHtml: String
)

@Controller
class ChatController(
    private val iaServiceFactory: IAServiceFactory,
    private val conversationDAO: ConversationDAO,
    private val messageDAO: MessageDAO,
    private val modeleIADAO: ModeleIADAO,
    private val markdownService: MarkdownService,
    private val utilisateurDAO: UtilisateurDAO
) {

    // Récupération de l'utilisateur authentifié
    private fun getUtilisateurConnecte(
        authentication: Authentication
    ): Utilisateur {

        val email = authentication.name

        return utilisateurDAO.findByEmailIgnoreCase(email)
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Utilisateur introuvable"
            )
    }

    // Vérification du propriétaire d'une conversation
    private fun recupererConversation(
        id: Long,
        utilisateur: Utilisateur
    ): Conversation {

        return conversationDAO.findByIdAndUtilisateur(
            id,
            utilisateur
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Conversation introuvable"
        )
    }

    // Préparation des messages avec conversion Markdown
    private fun preparerMessages(
        messages: List<Message>
    ): List<MessageAffiche> {

        return messages
            .sortedBy { it.dateCreation }
            .map { message ->
                MessageAffiche(
                    message = message,
                    contenuHtml = markdownService.toHtml(message.contenu)
                )
            }
    }

    // Page d'accueil : historique de l'utilisateur connecté
    @GetMapping("/")
    fun accueil(
        model: Model,
        authentication: Authentication
    ): String {

        // Récupérer l'utilisateur de la session
        val utilisateur = getUtilisateurConnecte(authentication)

        // Ajouter l'utilisateur à la vue
        model.addAttribute("utilisateur", utilisateur)

        model.addAttribute("conversation", null)
        model.addAttribute("modeles", modeleIADAO.findAll())
        model.addAttribute("messages", emptyList<Message>())
        model.addAttribute("messagesAffiches", emptyList<MessageAffiche>())

        // Récupérer uniquement les conversations de cet utilisateur
        model.addAttribute(
            "conversations",
            conversationDAO.findAllByUtilisateurOrderByDateModificationDesc(
                utilisateur
            )
        )

        return "utilisateur/chat"
    }

    // Création d'une nouvelle conversation
    @PostMapping("/conversations")
    fun creerConversation(
        @RequestParam prompt: String,
        @RequestParam idModele: Long,
        authentication: Authentication
    ): String {

        // Récupérer l'utilisateur authentifié
        val utilisateur = getUtilisateurConnecte(authentication)

        // Récupération du modèle sélectionné
        val modele = modeleIADAO.findById(idModele)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Modèle IA introuvable"
                )
            }

        // Création de la conversation
        val maintenant = LocalDateTime.now()

        val conversation = Conversation(
            titre = prompt.take(50),
            dateCreation = maintenant,
            dateModification = maintenant,
            modeleIA = modele,
            utilisateur = utilisateur
        )

        val conversationEnregistree =
            conversationDAO.save(conversation)

        // Enregistrement du premier message utilisateur
        val messageUtilisateur = Message(
            contenu = prompt,
            dateCreation = LocalDateTime.now(),
            typeMessage = TypeMessage.UTILISATEUR,
            conversation = conversationEnregistree
        )

        messageDAO.save(messageUtilisateur)

        try {
            // Sélection automatique du service IA
            val iaService = iaServiceFactory.getService(modele)

            // Appel du fournisseur
            val reponse = iaService.envoyerPrompt(prompt, modele)

            // Enregistrement de la réponse
            val messageAssistant = Message(
                contenu = reponse,
                dateCreation = LocalDateTime.now(),
                typeMessage = TypeMessage.ASSISTANT,
                conversation = conversationEnregistree
            )

            messageDAO.save(messageAssistant)

        } catch (e: Exception) {
            println(
                "Erreur lors de l'appel au modèle ${modele.nom} : ${e.message}"
            )
        }

        // Actualisation de la date de modification
        conversationEnregistree.dateModification = LocalDateTime.now()
        conversationDAO.save(conversationEnregistree)

        return "redirect:/conversations/${conversationEnregistree.id}"
    }

    // Affichage d'une conversation spécifique
    @GetMapping("/conversations/{id}")
    fun afficherConversation(
        @PathVariable id: Long,
        model: Model,
        authentication: Authentication
    ): String {

        // Récupérer l'utilisateur authentifié
        val utilisateur = getUtilisateurConnecte(authentication)

        // Ajouter l'utilisateur à la vue
        model.addAttribute("utilisateur", utilisateur)

        // Vérifier que la conversation appartient à l'utilisateur
        val conversation = conversationDAO
            .findByIdAndUtilisateur(id, utilisateur)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Conversation introuvable"
            )

        // Récupération des messages
        val messages = conversation.messages
            .sortedBy { it.dateCreation }

        model.addAttribute("conversation", conversation)
        model.addAttribute("messages", messages)

        // Conversion Markdown et nettoyage HTML
        model.addAttribute(
            "messagesAffiches",
            preparerMessages(messages)
        )

        // Modèles disponibles
        model.addAttribute("modeles", modeleIADAO.findAll())

        // Historique de l'utilisateur connecté
        model.addAttribute(
            "conversations",
            conversationDAO.findAllByUtilisateurOrderByDateModificationDesc(
                utilisateur
            )
        )

        return "utilisateur/chat"
    }

    // Envoi d'un nouveau message dans une conversation existante
    @PostMapping("/conversations/{id}/messages")
    fun envoyerMessage(
        @PathVariable id: Long,
        @RequestParam prompt: String,
        authentication: Authentication
    ): String {

        // Récupérer l'utilisateur authentifié
        val utilisateur = getUtilisateurConnecte(authentication)

        // Vérifier que la conversation appartient à l'utilisateur
        val conversation = conversationDAO
            .findByIdAndUtilisateur(id, utilisateur)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Conversation introuvable"
            )

        // Enregistrement du message utilisateur
        val messageUtilisateur = Message(
            contenu = prompt,
            dateCreation = LocalDateTime.now(),
            typeMessage = TypeMessage.UTILISATEUR,
            conversation = conversation
        )

        messageDAO.save(messageUtilisateur)

        try {
            // Récupération du modèle associé à la conversation
            val modele = conversation.modeleIA

            // Sélection automatique du service
            val iaService = iaServiceFactory.getService(modele)

            // Appel du fournisseur
            val reponse = iaService.envoyerPrompt(prompt, modele)

            // Enregistrement de la réponse
            val messageAssistant = Message(
                contenu = reponse,
                dateCreation = LocalDateTime.now(),
                typeMessage = TypeMessage.ASSISTANT,
                conversation = conversation
            )

            messageDAO.save(messageAssistant)

        } catch (e: Exception) {
            println(
                "Erreur lors de l'appel à ${conversation.modeleIA.nom} : ${e.message}"
            )
        }

        // Actualisation de la date de modification
        conversation.dateModification = LocalDateTime.now()
        conversationDAO.save(conversation)

        return "redirect:/conversations/$id"
    }
}
