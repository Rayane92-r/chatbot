
package org.ldv.chatbot.model.entity

import jakarta.persistence.*

@Entity
class ModeleIA(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,

    @Column(nullable = false)
    var nom: String,

    @Column(nullable = false)
    var identifiantTechnique: String,

    @Column(nullable = false)
    var urlApi: String,

    @Column(columnDefinition = "TEXT")
    var description: String,

    var cheminLogo: String,

    // Association ManyToOne avec FournisseurIA
    @ManyToOne
    @JoinColumn(name = "fournisseur_ia_id")
    var fournisseurIA: FournisseurIA? = null
)
