package com.clex.android.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.clex.android.ui.anim.CxHaptics
import com.clex.android.ui.anim.CxSpringSpecs
import com.clex.android.ui.components.MonoText
import com.clex.android.ui.theme.*
import androidx.compose.material3.Text

// ═══════════════════════════════════════════════════
//  CxBottomNavBar
//  Pill nav matching clex.in:
//  - 999dp rounded pill, 1.5dp ink border, cream/dark fill
//  - Hard offset shadow + accent glow under selected tab
//  - Geist semi-bold labels, mixed case
// ═══════════════════════════════════════════════════

@Composable
fun CxBottomNavBar(
    currentRoute: String?,
    onNavigate: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CxTheme.colors
    val view = LocalView.current

    val items = BottomNavItem.entries
    val tabCount = items.size
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = CxSpringSpecs.panel(),
        label = "navPill"
    )

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .background(if (colors.isDark) colors.bgCard.copy(alpha = 0.92f) else CxColors.creamSoft)
                .border(1.5.dp, if (colors.isDark) colors.borderColor else colors.borderBold, RoundedCornerShape(999.dp))
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .drawBehind {
                    val itemWidth = size.width / tabCount
                    val centerX = animatedIndex * itemWidth + itemWidth / 2f
                    val centerY = size.height / 2f

                    // Active pill — gradient lavender→peach background
                    val pillW = (itemWidth - 8.dp.toPx()).coerceAtLeast(0f)
                    val pillH = (size.height - 12.dp.toPx()).coerceAtLeast(0f)
                    val pillX = centerX - pillW / 2f
                    val pillY = centerY - pillH / 2f
                    val r = pillH / 2f

                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = if (colors.isDark)
                                listOf(CxColors.lavender.copy(alpha = 0.45f), CxColors.accentSecondary.copy(alpha = 0.42f))
                            else
                                listOf(CxColors.lavender.copy(alpha = 0.6f), CxColors.peach.copy(alpha = 0.65f))
                        ),
                        topLeft = Offset(pillX, pillY),
                        size = Size(pillW, pillH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
                    )
                },
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                NavBarItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = {
                        CxHaptics.snap(view)
                        onNavigate(item)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
        )
    }
}

@Composable
private fun NavBarItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CxTheme.colors
    val activeColor = if (colors.isDark) colors.textPrimary else CxColors.ink
    val idleColor = colors.textTertiary

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = item.icon,
            fontSize = CxTypography.textLg,
            color = if (isSelected) activeColor else idleColor,
            textAlign = TextAlign.Center,
            fontFamily = CxTypography.fontDisplay,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.label,
            fontSize = CxTypography.textXs,
            fontFamily = CxTypography.fontDisplay,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (isSelected) activeColor else idleColor,
            letterSpacing = CxTypography.textXs * 0.06,
            textAlign = TextAlign.Center
        )
    }
}
