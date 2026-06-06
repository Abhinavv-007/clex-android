package com.clex.android.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clex.android.ui.anim.CxHaptics
import com.clex.android.ui.components.ClexMotion
import com.clex.android.ui.components.CxIcon
import com.clex.android.ui.components.CxIconType
import com.clex.android.ui.components.pressable
import com.clex.android.ui.theme.CxRadius
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography

// ═══════════════════════════════════════════════════
//  CxBottomNavBar — v1.12 flat-paper pill nav.
//  Solid paper card, 1px ink ring, soft drop shadow.
//  Active tab = filled ink capsule; inactive = icon-only.
//  No glass blur, no mesh gradient. Quiet by design.
// ═══════════════════════════════════════════════════

private val NAV_ITEMS = listOf(
    BottomNavItem.HOME to CxIconType.HOME,
    BottomNavItem.VAULT to CxIconType.VAULT,
    BottomNavItem.CHAIN to CxIconType.CHAIN,
    BottomNavItem.SETTINGS to CxIconType.SETTINGS,
)

@Composable
fun CxBottomNavBar(
    currentRoute: String?,
    onNavigate: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CxTheme.colors
    val view = LocalView.current

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(CxRadius.full), clip = false)
                    .clip(RoundedCornerShape(CxRadius.full))
                    .background(colors.bgCard)
                    .border(1.dp, colors.borderColor, RoundedCornerShape(CxRadius.full))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                NAV_ITEMS.forEach { (item, icon) ->
                    val active = currentRoute == item.route
                    NavPill(
                        active = active,
                        icon = icon,
                        label = item.label,
                        onClick = {
                            CxHaptics.snap(view)
                            onNavigate(item)
                        },
                    )
                }
            }
        }
        Spacer(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars),
        )
    }
}

@Composable
private fun NavPill(
    active: Boolean,
    icon: CxIconType,
    label: String,
    onClick: () -> Unit,
) {
    val colors = CxTheme.colors
    val expand by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = ClexMotion.defaultSpring(),
        label = "nav-expand",
    )
    val pillW = (44 + 64 * expand).dp
    val bg = if (active) colors.textPrimary else Color.Transparent
    val fg = if (active) colors.textInverse else colors.textSecondary
    Box(
        modifier = Modifier
            .height(44.dp)
            .width(pillW)
            .clip(RoundedCornerShape(CxRadius.full))
            .background(bg)
            .pressable(onClick = onClick, haptic = false),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 14.dp),
        ) {
            CxIcon(
                icon = icon,
                size = 20.dp,
                color = fg,
                strokeWidth = 1.6.dp,
            )
            if (active) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    color = fg,
                    fontSize = 13.sp,
                    fontFamily = CxTypography.fontDisplay,
                    fontWeight = FontWeight.W600,
                    letterSpacing = 0.2.sp,
                )
            }
        }
    }
}
