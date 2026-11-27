
package org.ldv.chatbot.model.dao

import org.ldv.chatbot.model.entity.Conversation
import org.ldv.chatbot.model.entity.Utilisateur
import org.springframework.data.jpa.repository.JpaRepository

interface ConversationDAO : JpaRepository<Conversation, Long> {

    // Récupérer les conversations d'un utilisateur
    // De la plus récente à la plus ancienne
    fun findAllByUtilisateurOrderByDateModificationDesc(
        utilisateur: Utilisateur
    ): List<Conversation>

    // Rechercher une conversation par son identifiant
    // et son propriétaire
    fun findByIdAndUtilisateur(
        id: Long,
        utilisateur: Utilisateur
    ): Conversation?
}
