

package org.ldv.chatbot.service

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Service

@Service
class MarkdownService {

    // Lecture du Markdown
    private val parser = Parser.builder().build()

    // Conversion du Markdown en HTML
    private val renderer = HtmlRenderer.builder()
        .escapeHtml(true)
        .build()

    // Nettoyage du HTML
    private val safelist = Safelist.basic()
        .addTags(
            "h1", "h2", "h3",
            "h4", "h5", "h6", "hr"
        )
        .addEnforcedAttribute(
            "a", "rel", "nofollow noopener noreferrer"
        )

    fun toHtml(markdown: String): String {

        // Étape 1 : convertir le Markdown en HTML
        val html = renderer.render(parser.parse(markdown))

        // Étape 2 : nettoyer le HTML contre les XSS
        return Jsoup.clean(html, safelist)
    }
}
