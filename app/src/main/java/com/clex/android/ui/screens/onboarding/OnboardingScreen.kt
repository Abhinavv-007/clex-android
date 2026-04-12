package com.clex.android.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.clex.android.ui.anim.RevealFromBottom
import com.clex.android.ui.anim.rememberEntryVisibility
import com.clex.android.ui.components.BodyText
import com.clex.android.ui.components.BrandLogoImage
import com.clex.android.ui.components.BrutalistBadge
import com.clex.android.ui.components.BrutalistButton
import com.clex.android.ui.components.ButtonSize
import com.clex.android.ui.components.ButtonVariant
import com.clex.android.ui.components.HeroTitle
import com.clex.android.ui.components.MonoText
import com.clex.android.ui.effects.MeshGradientBackground
import com.clex.android.ui.effects.ParticleField
import com.clex.android.ui.theme.CxBorders
import com.clex.android.ui.theme.CxColors
import com.clex.android.ui.theme.CxSpacing
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography
import kotlinx.coroutines.launch

private data class OnboardingCard(
    val title: String,
    val body: String,
)

private data class OnboardingSlide(
    val step: String,
    val title: String,
    val subtitle: String,
    val badges: List<String>,
    val accent: Color,
    val cards: List<OnboardingCard>,
)

private val onboardingSlides = listOf(
    OnboardingSlide(
        step = "01",
        title = "DROP FILES\nINSTANTLY",
        subtitle = "Bring files into Clex from the picker or clipboard. Nothing uploads first. The workspace starts on-device.",
        badges = listOf("FILE PICKER", "CLIPBOARD", "ANY FORMAT"),
        accent = CxColors.accent,
        cards = listOf(
            OnboardingCard("PICK", "Open the file picker and load what you need."),
            OnboardingCard("HOLD", "Files stay local while you prepare them."),
            OnboardingCard("QUEUE", "Start transfer only when you are ready."),
        )
    ),
    OnboardingSlide(
        step = "02",
        title = "PREPARE\nWITHOUT LEAVING",
        subtitle = "Compress, merge, convert, and chain actions inside the same flow instead of bouncing across tools.",
        badges = listOf("COMPRESS", "MERGE", "CONVERT", "ZIP"),
        accent = CxColors.accentTertiary,
        cards = listOf(
            OnboardingCard("IMAGE", "Shrink images before sending."),
            OnboardingCard("PDF", "Merge or split documents in one pass."),
            OnboardingCard("CHAIN", "Route one action straight into the next."),
        )
    ),
    OnboardingSlide(
        step = "03",
        title = "SHARE BY THE\nBEST ROUTE",
        subtitle = "Clex tries the best delivery path first: direct peer-to-peer, then local network, with cloud backup only when needed.",
        badges = listOf("DIRECT", "LOCAL", "FALLBACK"),
        accent = CxColors.success,
        cards = listOf(
            OnboardingCard("DIRECT", "Fastest path when both devices are live."),
            OnboardingCard("LOCAL", "Use same-network transfer when available."),
            OnboardingCard("BACKUP", "Cloud fallback only if the direct path fails."),
        )
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val colors = CxTheme.colors
    val pagerState = rememberPagerState(pageCount = { onboardingSlides.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingSlides.lastIndex
    val activeSlide = onboardingSlides[pagerState.currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
    ) {
        MeshGradientBackground(
            modifier = Modifier
                .matchParentSize()
                .alpha(if (colors.isDark) 0.15f else 0.08f),
            accentStrength = 0.08f
        )
        ParticleField(
            modifier = Modifier
                .matchParentSize()
                .alpha(if (colors.isDark) 0.10f else 0.04f),
            particleCount = 18,
            connectDistance = 90f,
            color = activeSlide.accent
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingSlidePage(
                slide = onboardingSlides[page],
                isActive = pagerState.currentPage == page
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = CxSpacing.screenHorizontal, vertical = CxSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm)
            ) {
                BrandLogoImage(size = 28.dp)
                MonoText(
                    text = "CLEX",
                    fontSize = CxTypography.textBase,
                    fontWeight = CxTypography.weightBlack,
                    color = colors.textPrimary,
                    letterSpacing = CxTypography.textXs * 0.18
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.bgCard)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(999.dp))
                    .clickable(onClick = onComplete)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                MonoText(
                    text = "SKIP",
                    fontSize = CxTypography.textXs,
                    color = colors.textSecondary,
                    letterSpacing = CxTypography.textXs * 0.14
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = CxSpacing.screenHorizontal)
                .padding(bottom = CxSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                onboardingSlides.forEachIndexed { index, slide ->
                    Box(
                        modifier = Modifier
                            .width(if (pagerState.currentPage == index) 30.dp else 12.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(
                                if (pagerState.currentPage == index) slide.accent
                                else colors.borderSubtle
                            )
                    )
                }
            }

            Spacer(Modifier.height(CxSpacing.lg))

            if (isLastPage) {
                BrutalistButton(
                    text = "OPEN WORKSPACE →",
                    onClick = onComplete,
                    variant = ButtonVariant.PRIMARY,
                    size = ButtonSize.LARGE,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                BrutalistButton(
                    text = "NEXT STEP →",
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    variant = ButtonVariant.PRIMARY,
                    size = ButtonSize.LARGE,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnboardingSlidePage(
    slide: OnboardingSlide,
    isActive: Boolean
) {
    val colors = CxTheme.colors
    val visible = rememberEntryVisibility("${slide.step}-$isActive")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(top = 96.dp, bottom = 220.dp)
    ) {
        RevealFromBottom(visible = visible, delayMs = 0) {
            OnboardingStepChip(step = slide.step, accent = slide.accent)
        }

        Spacer(Modifier.height(CxSpacing.xl))

        RevealFromBottom(visible = visible, delayMs = 80) {
            OnboardingGraphic(slide = slide)
        }

        Spacer(Modifier.height(CxSpacing.xl))

        RevealFromBottom(visible = visible, delayMs = 160) {
            HeroTitle(
                text = slide.title,
                color = colors.textPrimary,
                fontSize = CxTypography.text5xl
            )
        }

        Spacer(Modifier.height(CxSpacing.md))

        RevealFromBottom(visible = visible, delayMs = 240) {
            BodyText(
                text = slide.subtitle,
                color = colors.textSecondary,
                fontSize = CxTypography.textBase
            )
        }

        Spacer(Modifier.height(CxSpacing.lg))

        RevealFromBottom(visible = visible, delayMs = 320) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(CxSpacing.sm)
            ) {
                slide.badges.forEach { badge ->
                    BrutalistBadge(
                        text = badge,
                        accentColor = slide.accent
                    )
                }
            }
        }

        Spacer(Modifier.height(CxSpacing.xl))

        slide.cards.forEachIndexed { index, card ->
            RevealFromBottom(visible = visible, delayMs = 400L + index * 90L) {
                OnboardingInfoCard(
                    title = card.title,
                    body = card.body,
                    accent = slide.accent
                )
            }
            if (index < slide.cards.lastIndex) {
                Spacer(Modifier.height(CxSpacing.sm))
            }
        }

        Spacer(Modifier.height(CxSpacing.xl))
    }
}

@Composable
private fun OnboardingStepChip(
    step: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accent.copy(alpha = 0.14f))
            .border(1.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm)
    ) {
        MonoText(
            text = step,
            fontSize = CxTypography.textSm,
            fontWeight = CxTypography.weightBlack,
            color = accent
        )
        MonoText(
            text = "SETUP",
            fontSize = CxTypography.textXs,
            color = accent,
            letterSpacing = CxTypography.textXs * 0.14
        )
    }
}

@Composable
private fun OnboardingInfoCard(
    title: String,
    body: String,
    accent: Color
) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bgCard)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(20.dp))
            .padding(CxSpacing.md)
    ) {
        MonoText(
            text = title,
            fontSize = CxTypography.textSm,
            fontWeight = CxTypography.weightBold,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(CxSpacing.xs))
        BodyText(
            text = body,
            fontSize = CxTypography.textSm,
            color = colors.textSecondary
        )
        Spacer(Modifier.height(CxSpacing.sm))
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(accent)
        )
    }
}

@Composable
private fun OnboardingGraphic(slide: OnboardingSlide) {
    when (slide.step) {
        "01" -> DropGraphic(accent = slide.accent)
        "02" -> PrepareGraphic(accent = slide.accent)
        else -> ShareGraphic(accent = slide.accent)
    }
}

@Composable
private fun DropGraphic(accent: Color) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.bgSecondary)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(24.dp))
            .padding(CxSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MonoText(
            text = "↓",
            fontSize = CxTypography.text3xl,
            color = accent
        )
        Spacer(Modifier.height(CxSpacing.md))
        Row(horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm)) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.bgCard)
                        .border(
                            1.dp,
                            if (index == 1) accent.copy(alpha = 0.75f) else colors.borderSubtle,
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    MonoText(
                        text = listOf("PDF", "IMG", "DOC")[index],
                        fontSize = CxTypography.textSm,
                        color = if (index == 1) accent else colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun PrepareGraphic(accent: Color) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.bgSecondary)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(24.dp))
            .padding(CxSpacing.lg)
    ) {
        listOf("COMPRESS", "CONVERT", "ZIP") .forEachIndexed { index, label ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accent.copy(alpha = if (index == 1) 0.18f else 0.10f))
                        .border(1.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    MonoText(
                        text = String.format("%02d", index + 1),
                        fontSize = CxTypography.textXs,
                        color = accent
                    )
                }
                Spacer(Modifier.width(CxSpacing.md))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.bgCard)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                        .padding(horizontal = CxSpacing.md, vertical = 12.dp)
                ) {
                    MonoText(
                        text = label,
                        fontSize = CxTypography.textSm,
                        fontWeight = CxTypography.weightBold,
                        color = colors.textPrimary
                    )
                }
            }
            if (index < 2) {
                Spacer(Modifier.height(CxSpacing.sm))
                MonoText(
                    text = "↓",
                    fontSize = CxTypography.textLg,
                    color = accent,
                    modifier = Modifier.padding(start = 12.dp)
                )
                Spacer(Modifier.height(CxSpacing.sm))
            }
        }
    }
}

@Composable
private fun ShareGraphic(accent: Color) {
    val colors = CxTheme.colors
    val routes = listOf(
        Triple("DIRECT", "BEST", accent),
        Triple("LOCAL", "READY", CxColors.accentTertiary),
        Triple("BACKUP", "FALLBACK", CxColors.warning)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.bgSecondary)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(24.dp))
            .padding(CxSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(CxSpacing.sm)
    ) {
        routes.forEach { (title, state, color) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.bgCard)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                    .padding(horizontal = CxSpacing.md, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(color)
                    )
                    MonoText(
                        text = title,
                        fontSize = CxTypography.textSm,
                        fontWeight = CxTypography.weightBold,
                        color = colors.textPrimary
                    )
                }
                MonoText(
                    text = state,
                    fontSize = CxTypography.textXs,
                    color = color
                )
            }
        }
    }
}
