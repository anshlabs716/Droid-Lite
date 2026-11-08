package com.machiav3lli.fdroid.ui.components.appsheet

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.components.ExpandableItemsBlock

// Minimal HTML → AnnotatedString: covers <b>, <i>, <u>, <a href>, <br>,
// <p>/<li> headings and strips unknown tags. Replaces the compose-html dependency.

@Composable
fun HtmlTextBlock(
    modifier: Modifier = Modifier,
    shortText: String,
    longText: String = "",
    onUriClick: (String) -> Unit = {},
) {
    val text = remember(shortText, longText) {
        buildAnnotatedString {
            var bold = false
            var italic = false
            var underline = false
            var link: String? = null
            val tagRegex = Regex("</?([a-zA-Z][a-zA-Z0-9]*)[^>]*/?>|&([a-z]+|#\\d+);")

            fun flush(plain: String) {
                if (plain.isEmpty()) return
                val style = SpanStyle(
                    fontWeight = if (bold) FontWeight.Bold else null,
                    fontStyle = if (italic) androidx.compose.ui.text.font.FontStyle.Italic else null,
                    textDecoration = if (underline) androidx.compose.ui.text.style.TextDecoration.Underline else null,
                )
                val url = link
                if (url != null) {
                    withLink(LinkAnnotation.Url(url) { link ->
                        (link as? LinkAnnotation.Url)?.let { onUriClick(it.url) }
                    }) {
                        withStyle(style) { append(plain) }
                    }
                } else {
                    withStyle(style) { append(plain) }
                }
            }

            var index = 0
            var plainStart = 0
            while (true) {
                val match = tagRegex.find(longText, index)
                if (match == null) {
                    flush(longText.substring(plainStart))
                    break
                }
                flush(longText.substring(plainStart, match.range.first))
                plainStart = match.range.last + 1
                index = plainStart

                val (tagToken, entity) = match.destructured
                if (entity.isNotEmpty()) {
                    when (entity) {
                        "amp"  -> flush("&")
                        "lt"   -> flush("<")
                        "gt"   -> flush(">")
                        "quot" -> flush("\"")
                        "apos" -> flush("'")
                        "nbsp" -> flush(" ")
                        else   -> {
                            if (entity.startsWith("#")) {
                                entity.drop(1).toIntOrNull()?.let {
                                    flush(String(Character.toChars(it)))
                                }
                            } else flush(entity)
                        }
                    }
                } else when (tagToken.lowercase()) {
                    "b", "strong"  -> bold = !tagToken.startsWith("/")
                    "i", "em"      -> italic = !tagToken.startsWith("/")
                    "u"            -> underline = !tagToken.startsWith("/")
                    "br"           -> flush("\n")
                    "p", "li", "h1", "h2", "h3", "h4" ->
                        if (tagToken.startsWith("/")) flush("\n")
                    "a"            -> if (!tagToken.startsWith("/")) {
                        link = Regex("href\\s*=\\s*[\"']([^\"']+)[\"']")
                            .find(match.value)?.groupValues?.get(1)
                    } else link = null
                    else           -> {}
                }
            }
        }
    }

    ExpandableItemsBlock(
        modifier = modifier.fillMaxWidth(),
        heading = shortText
    ) {
        Text(
            text,
            modifier = Modifier
                .animateContentSize()
                .padding(12.dp)
                .fillMaxWidth()
        )
    }
}
