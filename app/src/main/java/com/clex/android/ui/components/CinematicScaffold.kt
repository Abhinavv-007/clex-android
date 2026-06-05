package com.clex.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clex.android.ui.theme.CxColors
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography

// ═══════════════════════════════════════════════════
//  CinematicScaffold — shared liquid-glass shell.
//  Mesh-gradient floor + page header (kicker, big
//  Geist title, optional cursive accent + body line).
//  Drop-in for Settings/Vault/Chain/Workspace bodies.
// ═══════════════════════════════════════════════════

@Composable
fun CinematicScaffold(
    kicker: String,
    title: String,
    cursive: String? = null,
    body: String? = null,
    scrollable: Boolean = true,
    bottomInset: androidx.compose.ui.unit.Dp = 130.dp,
    headerTrailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CxTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (colors.isDark) CxColors.bgPrimary else CxColors.cream),
    ) {
        LiquidMeshBackground(modifier = Modifier.matchParentSize(), intensity = 0.85f)

        val outer = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp, bottom = bottomInset)

        if (scrollable) {
            Column(
                modifier = outer.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                CinematicHeader(kicker, title, cursive, body, headerTrailing)
                content()
            }
        } else {
            Column(
                modifier = outer,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                CinematicHeader(kicker, title, cursive, body, headerTrailing)
                content()
            }
        }
    }
}

@Composable
private fun CinematicHeader(
    kicker: String,
    title: String,
    cursive: String?,
    body: String?,
    trailing: @Composable (() -> Unit)?,
) {
    val colors = CxTheme.colors
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 22.dp,
    ) {
        Column {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KickerChip(text = kicker)
                if (trailing != null) trailing()
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = title,
                fontSize = CxTypography.text4xl,
                fontFamily = CxTypography.fontDisplay,
                fontWeight = CxTypography.weightExtrabold,
                color = colors.textPrimary,
                lineHeight = CxTypography.text4xl * 1.08,
            )
            if (cursive != null) {
                CursiveAccent(text = cursive, fontSize = CxTypography.text3xl)
            }
            if (body != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = body,
                    fontSize = CxTypography.textBase,
                    fontFamily = CxTypography.fontBody,
                    fontWeight = CxTypography.weightMedium,
                    color = colors.textSecondary,
                    lineHeight = CxTypography.textBase * 1.55,
                )
            }
        }
    }
}
