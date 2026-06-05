package com.clex.android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.clex.android.ui.anim.CxHaptics
import com.clex.android.ui.components.LiquidPillNavBar
import com.clex.android.ui.components.NavTab

// ═══════════════════════════════════════════════════
//  CxBottomNavBar — Liquid glass floating pill nav.
//  Matches iOS-style free-floating dock: rounded pill,
//  big blur body, pill-within-pill for active tab.
// ═══════════════════════════════════════════════════

@Composable
fun CxBottomNavBar(
    currentRoute: String?,
    onNavigate: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val tabs = BottomNavItem.entries.map { item ->
        NavTab(route = item.route, label = item.label, icon = item.icon)
    }

    Box(modifier = modifier.fillMaxWidth()) {
        LiquidPillNavBar(
            items = tabs,
            selectedRoute = currentRoute,
            onSelect = { tab ->
                val match = BottomNavItem.entries.firstOrNull { it.route == tab.route } ?: return@LiquidPillNavBar
                CxHaptics.snap(view)
                onNavigate(match)
            },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
        Spacer(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
        )
    }
}
