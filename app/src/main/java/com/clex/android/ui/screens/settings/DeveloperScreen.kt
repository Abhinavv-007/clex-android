package com.clex.android.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.clex.android.AppRelease
import com.clex.android.ui.components.BodyText
import com.clex.android.ui.components.BrandLogoImage
import com.clex.android.ui.components.MonoText
import com.clex.android.ui.components.PageMark
import com.clex.android.ui.components.SectionLabel
import com.clex.android.ui.effects.MeshGradientBackground
import com.clex.android.ui.theme.CxSpacing
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography

private data class DeveloperLink(
    val label: String,
    val value: String,
    val uri: String,
)

@Composable
fun DeveloperScreen(onBack: () -> Unit) {
    val colors = CxTheme.colors
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val links = listOf(
        DeveloperLink("EMAIL", "hello@redlex.in", "mailto:hello@redlex.in"),
        DeveloperLink("EMAIL", "abhnv@redlex.in", "mailto:abhnv@redlex.in"),
        DeveloperLink("EMAIL", "abhnv@abhnv.in", "mailto:abhnv@abhnv.in"),
        DeveloperLink("LINKEDIN", "linkedin.com/in/abhnv07", "https://www.linkedin.com/in/abhnv07/"),
        DeveloperLink("WEBSITE", "abhnv.in", "https://abhnv.in"),
        DeveloperLink("WEBSITE", "abhnv.me", "https://abhnv.me"),
        DeveloperLink("WEBSITE", "clex.in", "https://clex.in"),
        DeveloperLink("WEBSITE", "lnch.in", "https://lnch.in"),
        DeveloperLink("WEBSITE", "modih.in", "https://modih.in"),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
    ) {
        MeshGradientBackground(modifier = Modifier.matchParentSize(), accentStrength = 0.08f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = CxSpacing.screenHorizontal, vertical = CxSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PageMark(glyph = "◉", title = "DEVELOPER")
                MonoText(
                    text = "V${AppRelease.versionName}",
                    fontSize = CxTypography.textXs,
                    color = colors.textTertiary,
                    letterSpacing = CxTypography.textXs * 0.15
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = CxSpacing.screenHorizontal, vertical = CxSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(CxSpacing.xl)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(CxSpacing.md)) {
                    SectionLabel(text = "Profile")
                    DeveloperCard(onClick = {}) {
                        BrandLogoImage(size = 44.dp)
                        Spacer(Modifier.height(CxSpacing.md))
                        MonoText(
                            text = "ABHINAV",
                            fontSize = CxTypography.text2xl,
                            fontWeight = CxTypography.weightBlack,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(CxSpacing.xs))
                        BodyText(
                            text = "Built Clex around direct transfer, local-first privacy, and public verification without turning the product into a normal cloud wrapper.",
                            textAlign = TextAlign.Start
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(CxSpacing.md)) {
                    SectionLabel(text = "Contact")
                    links.forEach { item ->
                        DeveloperCard(onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.uri)))
                        }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    MonoText(
                                        text = item.label,
                                        fontSize = CxTypography.textXs,
                                        color = colors.textTertiary
                                    )
                                    MonoText(
                                        text = item.value,
                                        fontSize = CxTypography.textSm,
                                        color = colors.textPrimary,
                                        fontWeight = CxTypography.weightBold
                                    )
                                }
                                MonoText(
                                    text = "↗",
                                    fontSize = CxTypography.textBase,
                                    color = colors.accent
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(CxSpacing.md)) {
                    SectionLabel(text = "Release")
                    DeveloperCard(onClick = onBack) {
                        MonoText(
                            text = "CLEX ANDROID ${AppRelease.versionName}",
                            fontSize = CxTypography.textBase,
                            color = colors.textPrimary,
                            fontWeight = CxTypography.weightBold
                        )
                        Spacer(Modifier.height(CxSpacing.xs))
                        BodyText(
                            text = "Transfer stability, portrait QR scanning, developer contact details, and release metadata alignment for the 1.9.1 pass.",
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }

            Spacer(Modifier.height(72.dp))
        }
    }
}

@Composable
private fun DeveloperCard(
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CxTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgCard)
            .clickable(onClick = onClick)
            .padding(CxSpacing.md),
        verticalArrangement = Arrangement.spacedBy(CxSpacing.sm),
        content = content
    )
}
