package com.clex.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clex.android.ui.theme.CxRadius
import com.clex.android.ui.theme.CxSpacing
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography

// ═══════════════════════════════════════════════════
//  Ledger primitives — v1.12 paper+ink layout system.
//  Edge-to-edge horizontal rules, numeric markers,
//  ledger rows, flat segmented tab bar, press cards.
// ═══════════════════════════════════════════════════

@Composable
private fun DisplayText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color,
    weight: FontWeight = FontWeight.W500,
    letterSpacing: androidx.compose.ui.unit.TextUnit = 0.sp,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = CxTypography.fontDisplay,
        fontWeight = weight,
        letterSpacing = letterSpacing,
        lineHeight = fontSize * 1.25,
        maxLines = maxLines,
    )
}

@Composable
private fun BodyText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color,
    weight: FontWeight = FontWeight.W400,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = CxTypography.fontBody,
        fontWeight = weight,
        lineHeight = fontSize * 1.5,
        maxLines = maxLines,
    )
}

@Composable
private fun MonoSmall(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color,
    weight: FontWeight = FontWeight.W500,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = CxTypography.fontMono,
        fontWeight = weight,
        letterSpacing = 0.sp,
    )
}

// ── EdgeHeader — 1-line title + 1-line caption + optional trailing icon button ──
@Composable
fun EdgeHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    caption: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    bottomRule: Boolean = true,
) {
    val colors = CxTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = CxSpacing.screenHorizontal,
                end = CxSpacing.screenHorizontal,
                top = CxSpacing.lg,
                bottom = if (bottomRule) CxSpacing.md else CxSpacing.sm,
            )
            .then(
                if (bottomRule) Modifier.drawBehind {
                    val y = size.height
                    drawLine(
                        color = colors.borderColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                    )
                } else Modifier,
            ),
    ) {
        if (eyebrow != null) {
            DisplayText(
                text = eyebrow.uppercase(),
                fontSize = 11.sp,
                color = colors.textTertiary,
                weight = FontWeight.W600,
                letterSpacing = 1.6.sp,
            )
            Spacer(Modifier.height(8.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            DisplayText(
                text = title,
                fontSize = 32.sp,
                color = colors.textPrimary,
                weight = FontWeight.W700,
                letterSpacing = (-0.5).sp,
            )
            trailing?.invoke()
        }
        if (caption != null) {
            Spacer(Modifier.height(6.dp))
            BodyText(
                text = caption,
                fontSize = 14.sp,
                color = colors.textSecondary,
                weight = FontWeight.W400,
            )
        }
    }
}

// ── HeaderIconButton — circular outlined button, 40dp ──
@Composable
fun HeaderIconButton(
    icon: CxIconType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "",
) {
    val colors = CxTheme.colors
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(1.dp, colors.borderColor, CircleShape)
            .pressable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        CxIcon(icon = icon, size = 20.dp, color = colors.textPrimary, strokeWidth = 1.5.dp)
    }
}

// ── SectionLabel — uppercase tracked label sitting above a section ──
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    number: String? = null,
) {
    val colors = CxTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CxSpacing.screenHorizontal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (number != null) {
            MonoSmall(
                text = number,
                fontSize = 12.sp,
                color = colors.textTertiary,
                weight = FontWeight.W500,
            )
            Spacer(Modifier.width(12.dp))
        }
        DisplayText(
            text = text.uppercase(),
            fontSize = 11.sp,
            color = colors.textSecondary,
            weight = FontWeight.W600,
            letterSpacing = 1.6.sp,
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .height(1.dp)
                .weight(1f)
                .background(colors.borderColor),
        )
    }
}

// ── LedgerRow — left dot + label + value + chevron pattern ──
@Composable
fun LedgerRow(
    label: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    accent: Boolean = false,
    leadingIcon: CxIconType? = null,
    trailingIcon: CxIconType? = CxIconType.CHEVRON_RIGHT,
    onClick: (() -> Unit)? = null,
    description: String? = null,
) {
    val colors = CxTheme.colors
    val rowMod = if (onClick != null) {
        modifier.pressable(onClick = onClick)
    } else modifier
    Row(
        modifier = rowMod
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(
                horizontal = CxSpacing.screenHorizontal,
                vertical = 14.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.bgSecondary),
                contentAlignment = Alignment.Center,
            ) {
                CxIcon(
                    icon = leadingIcon,
                    size = 18.dp,
                    color = if (accent) colors.accent else colors.textPrimary,
                    strokeWidth = 1.5.dp,
                )
            }
            Spacer(Modifier.width(14.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (accent) colors.accent else colors.textTertiary),
            )
            Spacer(Modifier.width(14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            BodyText(
                text = label,
                fontSize = 16.sp,
                color = colors.textPrimary,
                weight = FontWeight.W500,
            )
            if (description != null) {
                Spacer(Modifier.height(2.dp))
                BodyText(
                    text = description,
                    fontSize = 13.sp,
                    color = colors.textTertiary,
                    weight = FontWeight.W400,
                )
            }
        }
        if (value != null) {
            MonoSmall(
                text = value,
                fontSize = 14.sp,
                color = colors.textSecondary,
                weight = FontWeight.W500,
            )
            Spacer(Modifier.width(8.dp))
        }
        if (trailingIcon != null) {
            CxIcon(
                icon = trailingIcon,
                size = 16.dp,
                color = colors.textTertiary,
                strokeWidth = 1.5.dp,
            )
        }
    }
}

// ── HRule — full-width 1px ink line, no padding ──
@Composable
fun HRule(modifier: Modifier = Modifier, padded: Boolean = true) {
    val colors = CxTheme.colors
    val m = if (padded) modifier.padding(horizontal = CxSpacing.screenHorizontal) else modifier
    Box(
        modifier = m
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.borderColor),
    )
}

// ── FlatTabBar — segmented tab pill, ink ring, accent fill on active ──
@Composable
fun FlatTabBar(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CxTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CxSpacing.screenHorizontal)
            .clip(RoundedCornerShape(CxRadius.full))
            .background(colors.bgSecondary)
            .border(1.dp, colors.borderColor, RoundedCornerShape(CxRadius.full))
            .padding(4.dp),
    ) {
        tabs.forEachIndexed { i, label ->
            val isActive = i == selected
            val bg = if (isActive) colors.textPrimary else Color.Transparent
            val fg = if (isActive) colors.textInverse else colors.textSecondary
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(CxRadius.full))
                    .background(bg)
                    .pressable(haptic = true, onClick = { onSelect(i) }),
                contentAlignment = Alignment.Center,
            ) {
                DisplayText(
                    text = label,
                    fontSize = 13.sp,
                    color = fg,
                    weight = if (isActive) FontWeight.W600 else FontWeight.W500,
                    letterSpacing = 0.3.sp,
                )
            }
        }
    }
}

// ── PressableCard — paper-card with 1px ink ring, press-spring + haptic ──
@Composable
fun PressableCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    radius: Dp = 18.dp,
    content: @Composable () -> Unit,
) {
    val colors = CxTheme.colors
    val base = modifier
        .clip(RoundedCornerShape(radius))
        .background(colors.bgCard)
        .border(1.dp, colors.borderColor, RoundedCornerShape(radius))
    val withClick = if (onClick != null) base.pressable(onClick = onClick) else base
    Box(modifier = withClick.padding(contentPadding)) {
        content()
    }
}

// ── MetricRow — compact stat row with big number + label ──
@Composable
fun MetricRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null,
) {
    val colors = CxTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        DisplayText(
            text = label.uppercase(),
            fontSize = 11.sp,
            color = colors.textTertiary,
            weight = FontWeight.W600,
            letterSpacing = 1.5.sp,
        )
        DisplayText(
            text = value,
            fontSize = 22.sp,
            color = valueColor ?: colors.textPrimary,
            weight = FontWeight.W700,
            letterSpacing = (-0.3).sp,
        )
    }
}

// ── StatusDot — tiny breathing dot for live state ──
@Composable
fun StatusDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 6.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
    )
}
