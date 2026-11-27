
package org.ldv.chatbot.controller

import org.ldv.chatbot.model.dao.UtilisateurDAO
import org.ldv.chatbot.model.entity.Utilisateur
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ChatBotController(
    private val utilisateurDAO: UtilisateurDAO
) {

    // Page d'accueil
    @GetMapping("/ancien-chat")
    fun accueil(): String {
        return "exemple"
    }

    // Page de connexion
    @GetMapping("/connexion")
    fun connexion(): String {
        return "authy/connexion"
    }

    // Récupération de l'utilisateur authentifié
    fun getUtilisateurConnecte(
        authentication: Authentication
    ): Utilisateur {

        val email = authentication.name

        val utilisateur = utilisateurDAO
            .findByEmailIgnoreCase(email)
            ?: throw UsernameNotFoundException(
                "Utilisateur introuvable"
            )

        return utilisateur
    }
}
