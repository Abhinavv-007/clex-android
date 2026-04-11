package com.clex.android.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.clex.android.AppRelease
import com.clex.android.ChangelogEntry
import com.clex.android.ui.anim.RevealFromBottom
import com.clex.android.ui.anim.rememberEntryVisibility
import com.clex.android.ui.components.BodyText
import com.clex.android.ui.components.MonoText
import com.clex.android.ui.components.PageMark
import com.clex.android.ui.components.SectionLabel
import com.clex.android.ui.components.SectionTitle
import com.clex.android.ui.components.TopBarStatusChip
import com.clex.android.ui.effects.MeshGradientBackground
import com.clex.android.ui.effects.ParticleField
import com.clex.android.ui.theme.CxSpacing
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography

@Composable
fun ChangelogScreen(
    onBack: () -> Unit
) {
    val colors = CxTheme.colors
    val scrollState = rememberScrollState()
    val screenVisible = rememberEntryVisibility("changelog")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bgPrimary)
        ) {
            MeshGradientBackground(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.12f),
                accentStrength = 0.08f
            )
            ParticleField(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.08f),
                particleCount = 10,
                connectDistance = 82f,
                color = colors.accent
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = CxSpacing.screenHorizontal, vertical = CxSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MonoText(
                        text = "←",
                        fontSize = CxTypography.textXl,
                        color = colors.textSecondary,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onBack
                            )
                            .padding(end = CxSpacing.md)
                    )
                    PageMark(glyph = "⟳", title = "CHANGELOG")
                }
                TopBarStatusChip(
                    text = AppRelease.versionName,
                    accentColor = colors.accent,
                    showDot = true
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colors.borderSubtle)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = CxSpacing.screenHorizontal, vertical = CxSpacing.xl)
        ) {
            RevealFromBottom(visible = screenVisible, delayMs = 80L) {
                SectionLabel(text = "Release History")
            }
            Spacer(Modifier.height(CxSpacing.md))
            RevealFromBottom(visible = screenVisible, delayMs = 150L) {
                SectionTitle(text = "CLEX MOBILE\nCHANGELOG")
            }
            Spacer(Modifier.height(CxSpacing.sm))
            RevealFromBottom(visible = screenVisible, delayMs = 220L) {
                BodyText(
                    text = "${AppRelease.changelog.size} tracked builds from ${AppRelease.changelog.last().releasedOn} to ${AppRelease.changelog.first().releasedOn}.",
                    fontSize = CxTypography.textSm
                )
            }

            Spacer(Modifier.height(CxSpacing.xl))

            AppRelease.changelog.forEachIndexed { index, entry ->
                RevealFromBottom(
                    visible = screenVisible,
                    delayMs = 300L + index * 55L
                ) {
                    ChangelogCard(entry = entry, isCurrent = index == 0)
                }
                if (index < AppRelease.changelog.lastIndex) {
                    Spacer(Modifier.height(CxSpacing.md))
                }
            }

            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
private fun ChangelogCard(
    entry: ChangelogEntry,
    isCurrent: Boolean
) {
    val colors = CxTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.bgCard)
            .border(
                width = 1.dp,
                color = if (isCurrent) colors.accent.copy(alpha = 0.52f) else colors.borderSubtle,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(CxSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                MonoText(
                    text = entry.version,
                    fontSize = CxTypography.textLg,
                    fontWeight = CxTypography.weightBold,
                    color = if (isCurrent) colors.accent else colors.textPrimary
                )
                MonoText(
                    text = entry.releasedOn.uppercase(),
                    fontSize = CxTypography.textXs,
                    color = colors.textTertiary,
                    letterSpacing = CxTypography.textXs * 0.12f
                )
            }
            if (isCurrent) {
                TopBarStatusChip(
                    text = "CURRENT",
                    accentColor = colors.accent,
                    showDot = true
                )
            }
        }

        Spacer(Modifier.height(CxSpacing.md))

        entry.notes.forEachIndexed { index, note ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                MonoText(
                    text = "•",
                    fontSize = CxTypography.textSm,
                    color = colors.accent,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.width(CxSpacing.sm))
                BodyText(
                    text = note,
                    fontSize = CxTypography.textSm,
                    modifier = Modifier.weight(1f)
                )
            }
            if (index < entry.notes.lastIndex) {
                Spacer(Modifier.height(CxSpacing.sm))
            }
        }
    }
}
