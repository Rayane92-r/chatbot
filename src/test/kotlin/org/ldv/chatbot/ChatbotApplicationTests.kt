
package org.ldv.chatbot

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import org.ldv.chatbot.controller.ChatBotController

@WebMvcTest(ChatBotController::class)
class ChatBotApplicationTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `tester la page accueil`() {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("exemple"))
    }

    @Test
    fun `tester la page connexion`() {
        mockMvc.perform(get("/connexion"))
            .andExpect(status().isOk)
            .andExpect(view().name("authy/connexion"))
    }
}
