

package org.ldv.chatbot.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class MainController {

    @GetMapping("/login")
    fun login(model: Model): String {

        // Indique que nous sommes sur la page de connexion
        model.addAttribute("pageConnexion", true)

        return "auth/login"
    }
}
