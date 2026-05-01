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
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.clex.android.ui.anim.RevealFromBottom
import com.clex.android.ui.anim.rememberEntryVisibility
import com.clex.android.ui.components.BrandLogoImage
import com.clex.android.ui.components.BodyText
import com.clex.android.ui.components.HeroTitle
import com.clex.android.ui.components.MonoText
import com.clex.android.ui.components.SectionLabel
import com.clex.android.ui.components.SectionTitle
import com.clex.android.ui.effects.MeshGradientBackground
import com.clex.android.ui.effects.ParticleField
import com.clex.android.ui.theme.CxSpacing
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography

private data class PrivacyHighlight(
    val icon: String,
    val title: String,
    val body: String,
)

private data class PrivacySection(
    val number: String,
    val title: String,
    val paragraphs: List<String>,
    val bullets: List<String> = emptyList(),
)

private val privacyHighlights = listOf(
    PrivacyHighlight(
        icon = "🔒",
        title = "NO P2P FILE STORAGE",
        body = "Direct and local transfers do not store file contents on Clex servers."
    ),
    PrivacyHighlight(
        icon = "👤",
        title = "LOCAL VAULT ENCRYPTION",
        body = "Vault notes stay encrypted on your device, with keys derived or stored locally."
    ),
    PrivacyHighlight(
        icon = "📊",
        title = "SCOPED DRIVE METADATA",
        body = "Temporary Drive-share metadata and auth signals are limited to what Vault needs to work."
    )
)

private val privacySections = listOf(
    PrivacySection(
        number = "SECTION 01",
        title = "OVERVIEW",
        paragraphs = listOf(
            "Clex is a workspace for preparing and sharing files, plus Vault for encrypted notes, secret links, and timed Drive-share links.",
            "The default product position is to keep content on your device whenever possible. Direct transfers, local transfers, browser-side file processing, and Vault notes are designed to minimize server involvement."
        )
    ),
    PrivacySection(
        number = "SECTION 02",
        title = "WHAT WE DON'T COLLECT",
        paragraphs = listOf("These boundaries are core to how Clex and Vault are built:"),
        bullets = listOf(
            "We do not store your files on any Clex server during direct P2P or local transfers.",
            "We do not read, scan, analyze, or inspect your file contents.",
            "We do not store Vault note plaintext or secret decryption keys on our servers.",
            "We do not require an account for core file preparation, direct transfer, or local transfer flows.",
            "We do not sell or share personal data for advertising.",
            "We do not publish Vault notes, secret-link content, or timed Drive-share file contents to the public Transfer Chain."
        )
    ),
    PrivacySection(
        number = "SECTION 03",
        title = "WHAT WE DO COLLECT",
        paragraphs = listOf("To operate the service, Clex may process a limited set of metadata:"),
        bullets = listOf(
            "Temporary signaling and routing data used to establish device-to-device sessions.",
            "Basic service analytics and error reports needed to keep the product stable.",
            "Google account identifiers such as UID, email, and display name when you explicitly use Google-backed flows.",
            "Temporary Drive-share metadata such as file name, size, MIME type, upload time, delete time, and Drive item ids.",
            "Secret-link status metadata such as encrypted payload, expiry, open state, and selected protection policy."
        )
    ),
    PrivacySection(
        number = "SECTION 04",
        title = "DIRECT TRANSFER PRIVACY",
        paragraphs = listOf(
            "Direct and local transfers use browser networking primitives such as WebRTC to create a direct connection between devices."
        ),
        bullets = listOf(
            "A lightweight signaling layer helps devices discover each other, but it does not carry file contents.",
            "Once the direct path is established, file data moves between devices rather than through a Clex content relay.",
            "Transfer traffic is encrypted in transit by the underlying protocols."
        )
    ),
    PrivacySection(
        number = "SECTION 05",
        title = "VAULT NOTES AND LOCAL ENCRYPTION",
        paragraphs = listOf(
            "Vault notes are designed to remain primarily on your device. Notes and related state may be written locally so Vault can work offline and reopen quickly."
        ),
        bullets = listOf(
            "Vault note content is encrypted before local persistence.",
            "Vault keys are generated, imported, or derived locally on the device.",
            "If you use Google-backed pairing or recovery flows, derivation happens locally and the raw encryption key is not uploaded by Clex."
        )
    ),
    PrivacySection(
        number = "SECTION 06",
        title = "VAULT SECRET SHARE",
        paragraphs = listOf(
            "Secret Share stores an encrypted payload plus expiry and policy metadata until the link expires or is consumed."
        ),
        bullets = listOf(
            "Clex stores the encrypted secret body, creation time, expiry, open state, and selected protection policy.",
            "The hash-fragment decryption key is intended to stay in the URL fragment rather than normal server requests.",
            "Client-side controls such as no-select or DevTools guard should be understood as best-effort safeguards."
        )
    ),
    PrivacySection(
        number = "SECTION 07",
        title = "VAULT CLOUD SHARE",
        paragraphs = listOf(
            "Vault Cloud Share is a temporary Drive-backed feature that writes uploads into the user's own Google Drive account under the Clex Share folder."
        ),
        bullets = listOf(
            "Google Drive sign-in is required so Vault can publish files to your Drive account and enforce quotas.",
            "Drive shares are limited to 1 GB per file and 10 GB per day per connected account.",
            "Drive sessions are scheduled for deletion after 24 hours."
        )
    ),
    PrivacySection(
        number = "SECTION 08",
        title = "GOOGLE AUTH AND LOCAL STORAGE",
        paragraphs = listOf(
            "Clex uses local device storage for preferences such as theme and Vault state. It does not use advertising cookies or behavioral trackers as part of the core product."
        ),
        bullets = listOf(
            "Google sign-in is used only when you choose features that require it, such as Drive fallback or Vault Cloud Share.",
            "Clex does not receive or store your Google password.",
            "Local device storage may retain preferences and local Vault state until you clear it."
        )
    ),
    PrivacySection(
        number = "SECTION 09",
        title = "CHAIN BOUNDARY",
        paragraphs = listOf(
            "The public Transfer Chain exists for workspace transfer-session metadata only.",
            "Vault notes, secret-link content, secret policies, and timed Drive-share file contents are not written to that public ledger."
        )
    ),
    PrivacySection(
        number = "SECTION 10",
        title = "CHANGES TO THIS POLICY",
        paragraphs = listOf(
            "Clex may update this policy over time. Changes are reflected in the app with an updated last-updated date and, when needed, surfaced through the product."
        )
    )
)

@Composable
fun PrivacyScreen(
    onBack: () -> Unit
) {
    val colors = CxTheme.colors
    val scrollState = rememberScrollState()
    val screenVisible = rememberEntryVisibility("privacy")
    val uriHandler = LocalUriHandler.current

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
                accentStrength = 0.07f
            )
            ParticleField(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.08f),
                particleCount = 10,
                connectDistance = 80f,
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
                    BrandLogoImage(size = 24.dp)
                    Spacer(Modifier.width(CxSpacing.sm))
                    MonoText(
                        text = "PRIVACY",
                        fontSize = CxTypography.textLg,
                        fontWeight = CxTypography.weightBold,
                        color = colors.textPrimary,
                        letterSpacing = CxTypography.textXs * 0.1
                    )
                }
                MonoText(
                    text = "APR 07 2026",
                    fontSize = CxTypography.textXs,
                    color = colors.textTertiary,
                    letterSpacing = CxTypography.textXs * 0.12
                )
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colors.borderSubtle)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = CxSpacing.screenHorizontal)
        ) {
            Spacer(Modifier.height(CxSpacing.xxl))
            RevealFromBottom(visible = screenVisible, delayMs = 100) {
                SectionLabel(text = "Legal")
            }
            Spacer(Modifier.height(CxSpacing.lg))
            RevealFromBottom(visible = screenVisible, delayMs = 200) {
                HeroTitle(
                    text = "PRIVACY\nPOLICY",
                    fontSize = CxTypography.text4xl
                )
            }
            Spacer(Modifier.height(CxSpacing.md))
            RevealFromBottom(visible = screenVisible, delayMs = 300) {
                BodyText(
                    text = "Privacy boundaries for Workspace, Vault, secret links, Drive-backed sharing, and the public Transfer Chain."
                )
            }

            Spacer(Modifier.height(CxSpacing.xxl))
            privacyHighlights.forEachIndexed { index, item ->
                RevealFromBottom(visible = screenVisible, delayMs = 420L + index * 110L) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.borderSubtle)
                            .background(colors.bgCard)
                            .padding(CxSpacing.md)
                    ) {
                        MonoText(
                            text = "${item.icon} ${item.title}",
                            fontSize = CxTypography.textSm,
                            fontWeight = CxTypography.weightBold,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(CxSpacing.xs))
                        BodyText(text = item.body, fontSize = CxTypography.textXs)
                    }
                }
                Spacer(Modifier.height(CxSpacing.sm))
            }

            Spacer(Modifier.height(CxSpacing.lg))

            privacySections.forEachIndexed { index, section ->
                RevealFromBottom(visible = screenVisible, delayMs = 780L + index * 110L) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.borderSubtle)
                            .background(colors.bgCard)
                            .padding(CxSpacing.md)
                    ) {
                        MonoText(
                            text = section.number,
                            fontSize = CxTypography.textXs,
                            color = colors.accent,
                            letterSpacing = CxTypography.textXs * 0.12
                        )
                        Spacer(Modifier.height(CxSpacing.sm))
                        SectionTitle(
                            text = section.title,
                            fontSize = CxTypography.text2xl
                        )
                        Spacer(Modifier.height(CxSpacing.sm))
                        section.paragraphs.forEachIndexed { paragraphIndex, paragraph ->
                            BodyText(
                                text = paragraph,
                                fontSize = CxTypography.textSm
                            )
                            if (paragraphIndex < section.paragraphs.lastIndex || section.bullets.isNotEmpty()) {
                                Spacer(Modifier.height(CxSpacing.sm))
                            }
                        }
                        section.bullets.forEachIndexed { bulletIndex, bullet ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                MonoText(
                                    text = "•",
                                    fontSize = CxTypography.textSm,
                                    color = colors.accent
                                )
                                Spacer(Modifier.width(CxSpacing.sm))
                                BodyText(
                                    text = bullet,
                                    fontSize = CxTypography.textXs,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (bulletIndex < section.bullets.lastIndex) {
                                Spacer(Modifier.height(CxSpacing.xs))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(CxSpacing.md))
            }

            RevealFromBottom(visible = screenVisible, delayMs = 2050) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.borderSubtle)
                        .background(colors.bgCard)
                        .padding(CxSpacing.md)
                ) {
                    SectionTitle(
                        text = "CONTACT",
                        fontSize = CxTypography.text2xl
                    )
                    Spacer(Modifier.height(CxSpacing.sm))
                    BodyText(
                        text = "Questions about this privacy policy? Contact hello@clex.in.",
                        fontSize = CxTypography.textSm
                    )
                    Spacer(Modifier.height(CxSpacing.md))
                    MonoText(
                        text = "EMAIL HELLO@CLEX.IN →",
                        fontSize = CxTypography.textXs,
                        color = colors.accent,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { uriHandler.openUri("mailto:hello@clex.in") }
                        ),
                        textAlign = TextAlign.Start
                    )
                }
            }

            Spacer(Modifier.height(120.dp))
        }
    }
}
