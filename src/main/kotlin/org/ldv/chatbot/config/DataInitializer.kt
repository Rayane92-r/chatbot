
package org.ldv.chatbot.config

import org.ldv.chatbot.model.dao.FournisseurIADAO
import org.ldv.chatbot.model.dao.ModeleIADAO
import org.ldv.chatbot.model.dao.RoleDAO
import org.ldv.chatbot.model.dao.UtilisateurDAO
import org.ldv.chatbot.model.entity.FournisseurIA
import org.ldv.chatbot.model.entity.ModeleIA
import org.ldv.chatbot.model.entity.Role
import org.ldv.chatbot.model.entity.Utilisateur
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class DataInitializer(
    private val fournisseurIADAO: FournisseurIADAO,
    private val modeleIADAO: ModeleIADAO,
    private val roleDAO: RoleDAO,
    private val utilisateurDAO: UtilisateurDAO,
    private val passwordEncoder: PasswordEncoder
) {

    @Bean
    fun initData(): CommandLineRunner {
        return CommandLineRunner {

            // Création des fournisseurs

            val mistral = fournisseurIADAO.findAll()
                .firstOrNull { it.nom == "Mistral" }
                ?: fournisseurIADAO.save(
                    FournisseurIA(
                        nom = "Mistral",
                        cheminLogo = "/images/mistral.png",
                        description = "Fournisseur français de modèles d'intelligence artificielle."
                    )
                )

            val gemini = fournisseurIADAO.findAll()
                .firstOrNull { it.nom == "Gemini" }
                ?: fournisseurIADAO.save(
                    FournisseurIA(
                        nom = "Gemini",
                        cheminLogo = "/images/gemini.png",
                        description = "Modèles d'intelligence artificielle de Google."
                    )
                )

            // Liste des modèles à initialiser

            val modeles = listOf(

                ModeleIA(
                    nom = "gemini-3.8-flash",
                    identifiantTechnique = "gemini-3.8-flash",
                    urlApi = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent",
                    description = "Modèle rapide et polyvalent.",
                    cheminLogo = "/images/gemini.png",
                    fournisseurIA = gemini
                ),

                ModeleIA(
                    nom = "gemini-3.5-flash-lite",
                    identifiantTechnique = "gemini-3.5-flash-lite",
                    urlApi = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent",
                    description = "Modèle rapide pour les tâches simples.",
                    cheminLogo = "/images/gemini.png",
                    fournisseurIA = gemini
                ),

                ModeleIA(
                    nom = "mistral-small-latest",
                    identifiantTechnique = "mistral-small-latest",
                    urlApi = "https://api.mistral.ai/v1/chat/completions",
                    description = "Modèle léger pour les tâches courantes.",
                    cheminLogo = "/images/mistral.png",
                    fournisseurIA = mistral
                ),

                ModeleIA(
                    nom = "mistral-medium-latest",
                    identifiantTechnique = "mistral-medium-latest",
                    urlApi = "https://api.mistral.ai/v1/chat/completions",
                    description = "Modèle avancé pour les tâches complexes.",
                    cheminLogo = "/images/mistral.png",
                    fournisseurIA = mistral
                )
            )

            // Éviter les doublons de modèles

            val nomsExistants = modeleIADAO.findAll()
                .map { it.nom }
                .toMutableSet()

            modeles.forEach { modele ->
                if (nomsExistants.add(modele.nom)) {
                    modeleIADAO.save(modele)
                }
            }

            // Création des rôles

            val roleUser = roleDAO.findAll()
                .firstOrNull { it.nomLogique == "ROLE_USER" }
                ?: roleDAO.save(
                    Role(
                        nom = "Utilisateur",
                        nomLogique = "ROLE_USER"
                    )
                )

            val roleAdmin = roleDAO.findAll()
                .firstOrNull { it.nomLogique == "ROLE_ADMIN" }
                ?: roleDAO.save(
                    Role(
                        nom = "Administrateur",
                        nomLogique = "ROLE_ADMIN"
                    )
                )

            // Création des utilisateurs

            val utilisateurs = utilisateurDAO.findAll()

            if (utilisateurs.isEmpty()) {

                // Compte administrateur

                utilisateurDAO.save(
                    Utilisateur(
                        prenom = "Timothée",
                        nom = "Moulin",
                        email = "admin@email.com",
                        motDePasse = requireNotNull(
                            passwordEncoder.encode("admin123")
                        ),
                        role = roleAdmin
                    )
                )

                // Compte utilisateur

                utilisateurDAO.save(
                    Utilisateur(
                        nom = "Prane",
                        prenom = "Doli",
                        email = "doli.prane@email.com",
                        motDePasse = requireNotNull(
                            passwordEncoder.encode("user123")
                        ),
                        role = roleUser
                    )
                )
            }
        }
    }
}
