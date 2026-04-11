package com.clex.android.ui.components.microapps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clex.android.ui.anim.CxHaptics
import com.clex.android.ui.anim.SlamIn
import com.clex.android.ui.anim.floatingIdle
import com.clex.android.ui.anim.physicalPress
import com.clex.android.ui.anim.shakeEffect
import com.clex.android.ui.theme.CxBorders
import com.clex.android.ui.theme.CxColors
import kotlinx.coroutines.delay

@Composable
fun StealthVaultLock(
    modifier: Modifier = Modifier,
    onUnlock: () -> Unit
) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isUnlocked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(120)
        isVisible = true
    }

    SlamIn(visible = isVisible && !isUnlocked) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(CxBorders.thick, CxColors.borderBold)
                .background(CxColors.bgCard)
                .shakeEffect(isShaking = isError) {
                    isError = false
                    pin = ""
                }
                .floatingIdle(amplitude = 3f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VAULT LOCK",
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = if (isError) CxColors.error else CxColors.textPrimary,
                    letterSpacing = 4.sp
                )

                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(if (index < pin.length) CxColors.textPrimary else Color.Transparent)
                                .border(
                                    CxBorders.thin,
                                    CxColors.textPrimary,
                                    androidx.compose.foundation.shape.CircleShape
                                )
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("*", "0", "#")
                ).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { char ->
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(CxColors.bgPrimary)
                                    .border(CxBorders.thin, CxColors.borderColor)
                                    .physicalPress {
                                        if (pin.length < 4) {
                                            pin += char
                                        }
                                        if (pin.length == 4) {
                                            if (pin == "1234") {
                                                CxHaptics.success(context)
                                                isUnlocked = true
                                                onUnlock()
                                            } else {
                                                CxHaptics.error(context)
                                                isError = true
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CxColors.textPrimary
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    AnimatedVisibility(
        visible = isUnlocked,
        enter = expandVertically(animationSpec = tween(500)),
        exit = shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(CxColors.accentSecondary)
        ) {
            Text(
                text = "VAULT OPEN",
                fontWeight = FontWeight.Black,
                color = CxColors.textPrimary,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
