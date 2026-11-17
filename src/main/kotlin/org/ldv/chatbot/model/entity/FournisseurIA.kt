
package org.ldv.chatbot.model.entity

import jakarta.persistence.*

@Entity
class FournisseurIA(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,

    @Column(nullable = false)
    var nom: String,

    @Column(columnDefinition = "TEXT")
    var description: String,

    var cheminLogo: String,

    // Association OneToMany avec ModeleIA
    @OneToMany(
        mappedBy = "fournisseurIA",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var modeles: MutableList<ModeleIA> = mutableListOf()
)
