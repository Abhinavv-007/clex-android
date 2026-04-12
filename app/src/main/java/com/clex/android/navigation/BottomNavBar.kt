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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.clex.android.ui.anim.CxHaptics
import com.clex.android.ui.anim.CxSpringSpecs
import com.clex.android.ui.components.MonoText
import com.clex.android.ui.theme.*

// ═══════════════════════════════════════════════════
//  CxBottomNavBar
//  Neo-Brutalist bottom navigation:
//  - Heavy top border
//  - Text-only labels in monospace
//  - Active state: single sliding accent pill (3dp)
//  - No Material icons — raw typographic symbols
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

    // Animate the pill position as a fractional tab index
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = CxSpringSpecs.panel(),
        label = "navPill"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(colors.bgCard.copy(alpha = if (colors.isDark) 0.82f else 0.94f))
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(28.dp))
                .padding(horizontal = 10.dp, vertical = CxSpacing.sm)
                .drawBehind {
                    // Single sliding pill at the bottom of the row
                    val itemWidth = size.width / tabCount
                    val pillWidth = 28.dp.toPx()
                    val pillHeight = 3.dp.toPx()
                    val x = animatedIndex * itemWidth + (itemWidth - pillWidth) / 2f
                    val y = size.height - pillHeight
                    drawRect(
                        color = colors.accent,
                        topLeft = Offset(x, y),
                        size = Size(pillWidth, pillHeight)
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

        // Bottom safe area spacer
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

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = CxSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MonoText(
            text = item.icon,
            fontSize = CxTypography.textXl,
            color = if (isSelected) colors.accent else colors.textTertiary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        MonoText(
            text = item.label,
            fontSize = CxTypography.textXs,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) colors.accent else colors.textTertiary,
            letterSpacing = CxTypography.textXs * 0.15,
            textAlign = TextAlign.Center
        )
        // Space reserved so pill (drawn via drawBehind) has room below label
        Spacer(Modifier.height(7.dp))
    }
}
