package com.clex.android.ui.screens.onboarding

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.clex.android.ui.components.CursiveAccent
import com.clex.android.ui.components.KickerChip
import com.clex.android.ui.components.LiquidGlassCard
import com.clex.android.ui.components.LiquidMeshBackground
import com.clex.android.ui.components.LiquidPillButton
import com.clex.android.ui.theme.CxColors
import com.clex.android.ui.theme.CxRadius
import com.clex.android.ui.theme.CxSpacing
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography
import kotlinx.coroutines.launch

private data class GlassSlide(
    val kicker: String,
    val title: String,
    val accent: String,
    val body: String,
    val bullets: List<Pair<String, String>>,
    val cta: String,
)

private val glassSlides = listOf(
    GlassSlide(
        kicker = "01 · drop",
        title = "Bring files in",
        accent = "instantly.",
        body = "Pull files from the picker or clipboard. Nothing leaves your phone until you say so.",
        bullets = listOf(
            "Picker" to "Files, photos, archives — any format.",
            "Clipboard" to "Links and snippets land in seconds.",
            "On-device" to "Workspace starts before any upload.",
        ),
        cta = "Continue",
    ),
    GlassSlide(
        kicker = "02 · prepare",
        title = "Polish without",
        accent = "leaving the app.",
        body = "Compress, merge, convert, chain — every step lives in one flow instead of jumping across tools.",
        bullets = listOf(
            "Image" to "Shrink, crop, convert.",
            "PDF" to "Merge, split, watermark.",
            "Chain" to "Route one action into the next.",
        ),
        cta = "Continue",
    ),
    GlassSlide(
        kicker = "03 · share",
        title = "Send by the",
        accent = "best route.",
        body = "Clex tries direct peer-to-peer first, then local network, then cloud as a fallback. Whichever lands fastest.",
        bullets = listOf(
            "Direct" to "Fastest when both devices are live.",
            "Local" to "Same Wi-Fi handoff when available.",
            "Cloud" to "Failsafe with end-to-end encryption.",
        ),
        cta = "Continue",
    ),
    GlassSlide(
        kicker = "04 · vault",
        title = "Hide secrets",
        accent = "behind a vault.",
        body = "Notes, keys, recovery codes — everything you tuck away is encrypted on-device with a passphrase only you know.",
        bullets = listOf(
            "Encrypted" to "AES-256 with PBKDF2.",
            "Local" to "Cipher never leaves the device.",
            "Stealth" to "Disguise screen for casual snoops.",
        ),
        cta = "Continue",
    ),
    GlassSlide(
        kicker = "05 · ready",
        title = "Welcome to",
        accent = "the calm flow.",
        body = "You're set. Hop in, drop a file, and feel how fast prepare → share gets when nothing fights you.",
        bullets = listOf(
            "Tip" to "Pull down anywhere to drop new files.",
            "Tip" to "Long-press any tile to peek the chain.",
            "Tip" to "Visit Settings to flip light or dark.",
        ),
        cta = "Open Clex",
    ),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val colors = CxTheme.colors
    val pagerState = rememberPagerState(pageCount = { glassSlides.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (colors.isDark) CxColors.bgPrimary else CxColors.cream),
    ) {
        LiquidMeshBackground(modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            // Top bar — skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                KickerChip(text = "Tour")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (colors.isDark) Color(0x33FFFFFF) else Color(0x14000000))
                        .clickable { onComplete() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Skip",
                        fontSize = CxTypography.textSm,
                        fontFamily = CxTypography.fontDisplay,
                        fontWeight = CxTypography.weightSemibold,
                        color = colors.textSecondary,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                pageSpacing = 12.dp,
            ) { page ->
                val slide = glassSlides[page]
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).let {
                    kotlin.math.abs(it)
                }
                val cardScale by animateFloatAsState(
                    targetValue = 1f - 0.05f * pageOffset.coerceIn(0f, 1f),
                    animationSpec = tween(0),
                    label = "cardScale",
                )

                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = cardScale
                            scaleY = cardScale
                            alpha = 1f - 0.3f * pageOffset.coerceIn(0f, 1f)
                        },
                    cornerRadius = CxRadius.lg,
                    padding = 26.dp,
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        KickerChip(text = slide.kicker)
                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = slide.title,
                            fontSize = CxTypography.text4xl,
                            fontFamily = CxTypography.fontDisplay,
                            fontWeight = CxTypography.weightExtrabold,
                            color = colors.textPrimary,
                            lineHeight = CxTypography.text4xl * 1.1,
                        )
                        Spacer(Modifier.height(2.dp))
                        CursiveAccent(
                            text = slide.accent,
                            fontSize = CxTypography.text4xl,
                            modifier = Modifier.align(Alignment.Start),
                        )

                        Spacer(Modifier.height(18.dp))

                        Text(
                            text = slide.body,
                            fontSize = CxTypography.textBase,
                            fontFamily = CxTypography.fontBody,
                            fontWeight = CxTypography.weightMedium,
                            color = colors.textSecondary,
                            lineHeight = CxTypography.textBase * 1.55,
                        )

                        Spacer(Modifier.height(24.dp))

                        slide.bullets.forEach { (k, v) ->
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(8.dp)
                                        .background(
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                listOf(CxColors.lavender, CxColors.peach2)
                                            ),
                                            shape = CircleShape,
                                        )
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = k,
                                        fontSize = CxTypography.textSm,
                                        fontFamily = CxTypography.fontDisplay,
                                        fontWeight = CxTypography.weightExtrabold,
                                        color = colors.textPrimary,
                                        letterSpacing = CxTypography.textSm * 0.04,
                                    )
                                    Text(
                                        text = v,
                                        fontSize = CxTypography.textSm,
                                        fontFamily = CxTypography.fontBody,
                                        fontWeight = CxTypography.weightMedium,
                                        color = colors.textTertiary,
                                        lineHeight = CxTypography.textSm * 1.5,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Dot pager
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(glassSlides.size) { i ->
                    val active = i == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(if (active) 24.dp else 8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (active) CxColors.lavender
                                else if (colors.isDark) Color(0x33FFFFFF) else Color(0x33000000)
                            )
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // CTA
            val isLast = pagerState.currentPage == glassSlides.lastIndex
            LiquidPillButton(
                text = glassSlides[pagerState.currentPage].cta,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isLast) onComplete()
                        else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
