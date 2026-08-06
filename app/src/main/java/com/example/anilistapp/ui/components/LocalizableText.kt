package com.example.anilistapp.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.util.Log
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

@Composable
fun LocalizableText(
    text: String,
    languages: Set<String>,
    randomize: Boolean,
    localizationManager: LocalizationManager,
    primaryLanguage: String = "ENGLISH",
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    style: TextStyle = LocalTextStyle.current
) {
    val displayText = if (randomize) {
        localizationManager.getRandomTranslation(text, languages)
    } else {
        if (primaryLanguage == "ENGLISH") {
            text
        } else {
            val translated = localizationManager.translate(text, primaryLanguage)
            if (translated == text && primaryLanguage != "ENGLISH") {
                Log.w("LocalizableText", "Translation failed for key: '$text' to $primaryLanguage")
            }
            translated
        }
    }

    Text(
        text = displayText,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        style = style
    )
}
