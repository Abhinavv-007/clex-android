package com.clex.android.ui.screens.help

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.clex.android.ui.anim.*
import com.clex.android.ui.components.*
import com.clex.android.ui.theme.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════
//  CLEX — Help / FAQ Screen
//  Accordion-based FAQ with categories
//  Matches the web FAQ page content
// ═══════════════════════════════════════════════════

data class FaqCategory(
    val title: String,
    val questions: List<FaqItem>
)

data class FaqItem(
    val question: String,
    val answer: String
)

private val faqCategories = listOf(
    FaqCategory(
        title = "Transfer Methods",
        questions = listOf(
            FaqItem(
                "How does P2P transfer work?",
                "Clex uses WebRTC to establish a direct browser-to-browser connection. Files stream directly from your device to the recipient's device. No server stores or relays the actual file data."
            ),
            FaqItem(
                "What is local network transfer?",
                "When both devices are on the same Wi-Fi network, Clex detects this and transfers files over the local network for LAN-speed transfers — ideal for large files."
            ),
            FaqItem(
                "When does Google Drive get used?",
                "Google Drive is a fallback when direct P2P or local network transfer isn't possible. The file uploads to your Google Drive account, not to Clex's servers."
            ),
            FaqItem(
                "Do both devices need to be open for P2P?",
                "Yes. For P2P and local network transfers, both sender and receiver need to keep the app open until the transfer completes. For async transfers, use Google Drive fallback."
            )
        )
    ),
    FaqCategory(
        title = "Privacy & Security",
        questions = listOf(
            FaqItem(
                "Are my files stored on Clex servers?",
                "No. During P2P and local network transfers, your files are never stored on any Clex server. The only server involvement is a lightweight signaling server that helps devices find each other."
            ),
            FaqItem(
                "Do I need an account?",
                "No account is required for P2P or local network transfers. The only time authentication is needed is for Google Drive fallback."
            ),
            FaqItem(
                "Is the transfer encrypted?",
                "WebRTC connections are encrypted by default using DTLS. P2P transfers are encrypted end-to-end between devices. Google Drive transfers use Google's TLS encryption."
            )
        )
    ),
    FaqCategory(
        title = "Vault",
        questions = listOf(
            FaqItem(
                "Where are Vault notes stored?",
                "Vault notes are stored locally on your device and encrypted before being written to the local database. Vault can sync between paired devices peer-to-peer when available."
            ),
            FaqItem(
                "Are secret links always one-time?",
                "No. Secret Share only becomes one-time when you enable View Once. If you leave that off, the recipient can reopen the link until the chosen expiry."
            ),
            FaqItem(
                "What do the secret protections do?",
                "You choose: View once, 60s viewing window, No select, Tab switch lock, and DevTools guard. Only the protections you turn on are enforced. Memory only is always guaranteed."
            ),
            FaqItem(
                "What are the Cloud Share limits?",
                "Cloud Share allows files up to 1 GB each, enforces a 10 GB per day upload budget, and deletes the Drive session automatically after 24 hours."
            ),
            FaqItem(
                "Do Vault items appear on Chain?",
                "No. The public Transfer Chain is for workspace transfer metadata only. Vault notes, secret-link content, and Drive-backed timed shares stay outside the public ledger."
            )
        )
    ),
    FaqCategory(
        title = "Speed & Performance",
        questions = listOf(
            FaqItem(
                "How fast are transfers?",
                "P2P transfers run at the speed of your internet connection. Local network transfers use your Wi-Fi's LAN speed (often 100+ Mbps). Google Drive speed depends on your upload bandwidth."
            ),
            FaqItem(
                "Is there a file size limit?",
                "For P2P and local transfers, the limit is your device's available RAM. For Google Drive, limits depend on your Drive storage quota (15 GB free tier)."
            )
        )
    ),
    FaqCategory(
        title = "Offline Usage",
        questions = listOf(
            FaqItem(
                "Can I use Clex offline?",
                "Partially. The preparation tools work offline — compress images, merge PDFs, convert documents, and bundle ZIPs without internet. Sharing requires a connection."
            )
        )
    ),
    FaqCategory(
        title = "File Handling",
        questions = listOf(
            FaqItem(
                "What file types can I process?",
                "Clex supports image compression/conversion (JPEG, PNG, WebP), PDF operations (merge, split, extract, export), DOCX to PDF conversion, and ZIP bundling for any file types."
            ),
            FaqItem(
                "What happens after transfer?",
                "For P2P and local transfers: nothing. Files existed only in memory. When you close the app, they're gone. For Google Drive transfers, the file stays in your Drive."
            ),
            FaqItem(
                "Can I process without sharing?",
                "Absolutely. You can use Clex purely as a file preparation tool. Compress, merge, convert, and download — all without touching the sharing features."
            )
        )
    )
)

@Composable
fun HelpFaqScreen(onBack: () -> Unit = {}) {
    val colors = CxTheme.colors
    val scrollState = rememberScrollState()
    var screenVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        screenVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(colors.bgPrimary)
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
                    text = "HELP & FAQ",
                    fontSize = CxTypography.textLg,
                    fontWeight = CxTypography.weightBold,
                    color = colors.textPrimary,
                    letterSpacing = CxTypography.textXs * 0.1
                )
            }
        }
        // Subtle separator
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
            // Hero
            Spacer(Modifier.height(CxSpacing.xxl))
            RevealFromBottom(visible = screenVisible, delayMs = 100) {
                SectionLabel(text = "Support")
            }
            Spacer(Modifier.height(CxSpacing.lg))
            RevealFromBottom(visible = screenVisible, delayMs = 200) {
                HeroTitle(
                    text = "FREQUENTLY\nASKED\nQUESTIONS",
                    fontSize = CxTypography.text4xl
                )
            }
            Spacer(Modifier.height(CxSpacing.md))
            RevealFromBottom(visible = screenVisible, delayMs = 300) {
                BodyText(
                    text = "Everything you need to know about how Clex handles your files, transfers, and privacy."
                )
            }

            Spacer(Modifier.height(CxSpacing.xxl))

            // FAQ Categories
            faqCategories.forEachIndexed { catIndex, category ->
                RevealFromBottom(
                    visible = screenVisible,
                    delayMs = 400L + catIndex * 150
                ) {
                    Column {
                        // Category title with accent underline
                        Column {
                            SectionTitle(
                                text = category.title.uppercase(),
                                fontSize = CxTypography.text2xl
                            )
                            Spacer(Modifier.height(CxSpacing.sm))
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(CxBorders.thick)
                                    .background(colors.accent)
                            )
                        }

                        Spacer(Modifier.height(CxSpacing.lg))

                        // Accordion items
                        category.questions.forEachIndexed { qIndex, faq ->
                            BrutalistAccordion(question = faq.question) {
                                BodyText(text = faq.answer, fontSize = CxTypography.textSm)
                            }
                            if (qIndex < category.questions.lastIndex) {
                                Spacer(Modifier.height(CxSpacing.xs))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(CxSpacing.xxl))
            }

            // Getting Started section
            RevealFromBottom(visible = screenVisible, delayMs = 1500) {
                Column {
                    SectionTitle(text = "GETTING\nSTARTED", fontSize = CxTypography.text3xl)
                    Spacer(Modifier.height(CxSpacing.lg))

                    val steps = listOf(
                        Triple("1", "OPEN WORKSPACE", "Tap 'Workspace' in the bottom nav. No account needed."),
                        Triple("2", "DROP FILES", "Select files from your device — images, PDFs, documents."),
                        Triple("3", "PREPARE", "Use built-in tools: compress, merge, convert, ZIP."),
                        Triple("4", "SHARE", "Generate a share code. Clex finds the fastest route."),
                        Triple("5", "RECEIVE", "Enter a share code or scan QR to receive files.")
                    )

                    steps.forEach { (num, title, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = CxSpacing.md),
                            verticalAlignment = Alignment.Top
                        ) {
                            AccentNumber(text = num, fontSize = CxTypography.text3xl)
                            Spacer(Modifier.width(CxSpacing.lg))
                            Column {
                                CardTitle(text = title, fontSize = CxTypography.textLg)
                                Spacer(Modifier.height(CxSpacing.xs))
                                BodyText(text = desc, fontSize = CxTypography.textSm)
                            }
                        }
                        BrutalistDivider(color = colors.borderColor, thickness = 1.dp)
                    }
                }
            }

            Spacer(Modifier.height(CxSpacing.xxl))

            // Tips
            RevealFromBottom(visible = screenVisible, delayMs = 1700) {
                Column {
                    SectionLabel(text = "Power User")
                    Spacer(Modifier.height(CxSpacing.lg))
                    SectionTitle(text = "TIPS &\nTRICKS", fontSize = CxTypography.text2xl)
                    Spacer(Modifier.height(CxSpacing.lg))

                    val tips = listOf(
                        "BATCH PROCESSING" to "Select multiple images and compress them all at once.",
                        "CHAIN EVERYTHING" to "After compressing, output flows to the next tool automatically.",
                        "OFFLINE MODE" to "Preparation tools work offline. Share when you're back online.",
                        "LOCAL SPEED" to "Same Wi-Fi? Clex uses local network for maximum transfer speed."
                    )

                    tips.forEach { (title, desc) ->
                        BrutalistCard {
                            CardTitle(text = title, fontSize = CxTypography.textBase)
                            Spacer(Modifier.height(CxSpacing.xs))
                            BodyText(text = desc, fontSize = CxTypography.textSm)
                        }
                        Spacer(Modifier.height(CxSpacing.md))
                    }
                }
            }

            // Contact CTA
            Spacer(Modifier.height(CxSpacing.xxl))
            RevealFromBottom(visible = screenVisible, delayMs = 1900) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.accent)
                        .border(CxBorders.thin, CxColors.pureBlack)
                        .padding(CxSpacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SectionTitle(
                        text = "STILL HAVE\nQUESTIONS?",
                        color = CxColors.pureBlack,
                        fontSize = CxTypography.text2xl
                    )
                    Spacer(Modifier.height(CxSpacing.md))
                    BodyText(
                        text = "Reach out and we'll get back to you.",
                        color = CxColors.pureBlack.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(CxSpacing.lg))
                    val ctx = LocalContext.current
                    BrutalistButton(
                        text = "CONTACT US →",
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:support@clex.in")
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Clex App Support")
                            }
                            runCatching { ctx.startActivity(intent) }
                        },
                        variant = ButtonVariant.SECONDARY,
                        size = ButtonSize.MEDIUM
                    )
                }
            }

            Spacer(Modifier.height(120.dp))
        }
    }
}
