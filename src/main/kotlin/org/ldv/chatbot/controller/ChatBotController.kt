
package org.ldv.chatbot.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ChatBotController {

    // Page d'accueil
    @GetMapping("/")
    fun accueil(): String {
        return "exemple"
    }

    // Page de connexion
    @GetMapping("/connexion")
    fun connexion(): String {
        return "authy/connexion"
    }
}
