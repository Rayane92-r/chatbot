package org.ldv.chatbot.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class AppelAPI(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var dateAppel: LocalDateTime,

    var tokensEntree: Int,

    var tokensSortie: Int,

    var cout: Double,

    var statut: String
)