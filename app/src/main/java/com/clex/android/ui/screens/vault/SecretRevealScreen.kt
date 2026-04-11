package com.clex.android.ui.screens.vault

import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.clex.android.data.AppLinkStore
import com.clex.android.data.ClexVaultApi
import com.clex.android.data.PendingSecretLink
import com.clex.android.data.VaultCryptoManager
import com.clex.android.ui.components.BodyText
import com.clex.android.ui.components.BrutalistBadge
import com.clex.android.ui.components.BrutalistButton
import com.clex.android.ui.components.BrutalistCard
import com.clex.android.ui.components.BrutalistProgressBar
import com.clex.android.ui.components.CardTitle
import com.clex.android.ui.components.HeroTitle
import com.clex.android.ui.components.MonoText
import com.clex.android.ui.components.SectionLabel
import com.clex.android.ui.theme.CxBorders
import com.clex.android.ui.theme.CxColors
import com.clex.android.ui.theme.CxSpacing
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class SecretRevealPhase {
    ENTRY,
    LOADING,
    CONFIRM,
    VIEWING,
    DESTROYED,
    ERROR,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SecretRevealScreen(
    onBack: () -> Unit,
) {
    val colors = CxTheme.colors
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var accessInput by remember { mutableStateOf("") }
    var pendingSecret by remember { mutableStateOf<PendingSecretLink?>(null) }
    var phase by remember { mutableStateOf(SecretRevealPhase.LOADING) }
    var secretText by remember { mutableStateOf<String?>(null) }
    var secretTitle by remember { mutableStateOf<String?>(null) }
    var policy by remember { mutableStateOf<com.clex.android.data.SecretPolicy?>(null) }
    var countdown by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun destroySecret(reason: String? = null) {
        secretText = null
        secretTitle = null
        countdown = 0
        errorMessage = reason
        phase = if (reason != null) SecretRevealPhase.DESTROYED else SecretRevealPhase.ENTRY
    }

    suspend fun loadSecretStatus(link: PendingSecretLink) {
        runCatching {
            ClexVaultApi.fetchSecretStatus(link.secretId)
        }.onSuccess { status ->
            policy = status.policy
            phase = when {
                !status.exists -> {
                    errorMessage = "This secret is no longer available."
                    SecretRevealPhase.ERROR
                }
                status.alreadyOpened && status.policy.viewOnce -> {
                    errorMessage = "This secret has already been opened and destroyed."
                    SecretRevealPhase.ERROR
                }
                else -> SecretRevealPhase.CONFIRM
            }
        }.onFailure { error ->
            errorMessage = error.message ?: "Could not verify this secret."
            phase = SecretRevealPhase.ERROR
        }
    }

    suspend fun revealSecret(link: PendingSecretLink) {
        phase = SecretRevealPhase.LOADING
        runCatching {
            val revealed = ClexVaultApi.revealSecret(link.secretId)
            val decrypted = VaultCryptoManager.decryptSharedSecret(
                encryptedPayloadB64 = revealed.encryptedPayload,
                ivB64 = revealed.iv,
                keyB64 = link.keyB64,
            )
            Triple(revealed, decrypted, parseRevealTitle(decrypted))
        }.onSuccess { (revealed, decrypted, title) ->
            policy = revealed.policy
            secretText = decrypted
            secretTitle = title
            countdown = if (revealed.policy.timedView) revealed.policy.viewWindowSeconds else 0
            errorMessage = null
            phase = SecretRevealPhase.VIEWING
        }.onFailure { error ->
            errorMessage = error.message ?: "Failed to open this secret."
            phase = SecretRevealPhase.ERROR
        }
    }

    LaunchedEffect(Unit) {
        val incoming = AppLinkStore.consumeSecretLink()
        if (incoming == null) {
            phase = SecretRevealPhase.ENTRY
        } else {
            pendingSecret = incoming
            loadSecretStatus(incoming)
        }
    }

    LaunchedEffect(phase, countdown, policy?.timedView) {
        if (phase != SecretRevealPhase.VIEWING || policy?.timedView != true || countdown <= 0) return@LaunchedEffect
        delay(1_000)
        if (countdown <= 1) {
            destroySecret("Viewing window ended.")
        } else {
            countdown -= 1
        }
    }

    DisposableEffect(phase, policy?.screenshotGuard) {
        if (phase == SecretRevealPhase.VIEWING && policy?.screenshotGuard == true) {
            activity?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE,
            )
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    DisposableEffect(lifecycleOwner, phase, policy?.tabSwitchLock) {
        val observer = LifecycleEventObserver { _, event ->
            if (
                phase == SecretRevealPhase.VIEWING &&
                policy?.tabSwitchLock == true &&
                event == Lifecycle.Event.ON_STOP
            ) {
                destroySecret("App moved to background. Secret view closed for safety.")
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
            .verticalScroll(scrollState)
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(top = CxSpacing.xl)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(text = "Secret Reveal")
            MonoText(
                text = "BACK",
                fontSize = CxTypography.textXs,
                color = colors.textTertiary,
                modifier = Modifier.clickable(onClick = onBack),
            )
        }

        Spacer(Modifier.height(CxSpacing.lg))

        when (phase) {
            SecretRevealPhase.ENTRY -> {
                HeroTitle(text = "OPEN\nSECRET", fontSize = CxTypography.text4xl)
                Spacer(Modifier.height(CxSpacing.md))
                BodyText(
                    text = "Paste the full Clex secret link or the reveal code.",
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(CxSpacing.xl))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                        .border(CxBorders.thin, colors.borderColor)
                        .background(colors.bgInput)
                        .padding(CxSpacing.md)
                ) {
                    BasicTextField(
                        value = accessInput,
                        onValueChange = { accessInput = it },
                        cursorBrush = SolidColor(colors.accent),
                        textStyle = TextStyle(
                            color = colors.textPrimary,
                            fontFamily = CxTypography.fontMono,
                            fontSize = CxTypography.textBase,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (accessInput.isBlank()) {
                                MonoText(
                                    text = "PASTE SECRET LINK OR CODE",
                                    fontSize = CxTypography.textSm,
                                    color = colors.textTertiary,
                                )
                            }
                            inner()
                        },
                    )
                }
                Spacer(Modifier.height(CxSpacing.lg))
                BrutalistButton(
                    text = "VERIFY SECRET →",
                    onClick = {
                        val parsed = parseSecretInput(accessInput)
                        if (parsed == null) {
                            errorMessage = "Paste the full secret link or the reveal code."
                            phase = SecretRevealPhase.ERROR
                        } else {
                            pendingSecret = parsed
                            phase = SecretRevealPhase.LOADING
                        }
                    },
                    variant = com.clex.android.ui.components.ButtonVariant.PRIMARY,
                    size = com.clex.android.ui.components.ButtonSize.LARGE,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            SecretRevealPhase.LOADING -> {
                Spacer(Modifier.height(120.dp))
                MonoText(
                    text = "VERIFYING SECRET…",
                    fontSize = CxTypography.textBase,
                    color = colors.accent,
                )
                Spacer(Modifier.height(CxSpacing.md))
                BrutalistProgressBar(progress = 0.55f)

                LaunchedEffect(pendingSecret?.secretId, phase) {
                    val link = pendingSecret ?: return@LaunchedEffect
                    loadSecretStatus(link)
                }
            }

            SecretRevealPhase.CONFIRM -> {
                val currentPolicy = policy
                HeroTitle(text = "READY TO\nREVEAL", fontSize = CxTypography.text4xl)
                Spacer(Modifier.height(CxSpacing.md))
                BodyText(
                    text = currentPolicy?.toSecretRuleSummary() ?: "This secret is ready to open.",
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(CxSpacing.lg))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(CxSpacing.sm),
                ) {
                    currentPolicy?.toBadges().orEmpty().forEach { badge ->
                        BrutalistBadge(text = badge, accentColor = colors.accent)
                    }
                }
                Spacer(Modifier.height(CxSpacing.xl))
                BrutalistButton(
                    text = "REVEAL SECRET",
                    onClick = {
                        val link = pendingSecret ?: return@BrutalistButton
                        errorMessage = null
                        phase = SecretRevealPhase.LOADING
                        scope.launch {
                            revealSecret(link)
                        }
                    },
                    variant = com.clex.android.ui.components.ButtonVariant.PRIMARY,
                    size = com.clex.android.ui.components.ButtonSize.LARGE,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            SecretRevealPhase.VIEWING -> {
                if (policy?.timedView == true && countdown > 0) {
                    MonoText(
                        text = "WINDOW: ${countdown}s",
                        fontSize = CxTypography.textSm,
                        color = if (countdown <= 10) CxColors.warning else colors.accent,
                    )
                    Spacer(Modifier.height(CxSpacing.sm))
                    BrutalistProgressBar(
                        progress = (countdown.toFloat() / (policy?.viewWindowSeconds?.coerceAtLeast(1) ?: 1)),
                        accentColor = if (countdown <= 10) CxColors.warning else colors.accent,
                    )
                    Spacer(Modifier.height(CxSpacing.lg))
                }

                secretTitle?.takeIf { it.isNotBlank() }?.let { title ->
                    MonoText(
                        text = title,
                        fontSize = CxTypography.textLg,
                        fontWeight = CxTypography.weightBold,
                        color = colors.accent,
                    )
                    Spacer(Modifier.height(CxSpacing.md))
                }

                BrutalistCard(accentBorder = true) {
                    SelectionHost(secretText.orEmpty())
                }
                Spacer(Modifier.height(CxSpacing.lg))
                BodyText(
                    text = "Close this screen after copying the value into your own password manager or notes app.",
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(CxSpacing.lg))
                BrutalistButton(
                    text = "DESTROY VIEW",
                    onClick = { destroySecret("Secret view closed.") },
                    variant = com.clex.android.ui.components.ButtonVariant.GHOST,
                    size = com.clex.android.ui.components.ButtonSize.MEDIUM,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            SecretRevealPhase.DESTROYED,
            SecretRevealPhase.ERROR -> {
                BrutalistCard {
                    CardTitle(
                        text = if (phase == SecretRevealPhase.ERROR) "SECRET UNAVAILABLE" else "SECRET CLOSED",
                        fontSize = CxTypography.textBase,
                        color = if (phase == SecretRevealPhase.ERROR) CxColors.error else colors.accent,
                    )
                    Spacer(Modifier.height(CxSpacing.sm))
                    BodyText(
                        text = errorMessage ?: "This secret is no longer available.",
                        color = colors.textSecondary,
                    )
                }
                Spacer(Modifier.height(CxSpacing.lg))
                BrutalistButton(
                    text = "TRY ANOTHER LINK",
                    onClick = {
                        accessInput = ""
                        pendingSecret = null
                        secretText = null
                        errorMessage = null
                        phase = SecretRevealPhase.ENTRY
                    },
                    variant = com.clex.android.ui.components.ButtonVariant.PRIMARY,
                    size = com.clex.android.ui.components.ButtonSize.MEDIUM,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun SelectionHost(value: String) {
    val colors = CxTheme.colors
    androidx.compose.foundation.text.selection.SelectionContainer {
        BodyText(
            text = value.ifBlank { "Empty secret" },
            color = colors.textPrimary,
            fontSize = CxTypography.textBase,
        )
    }
}

private fun parseRevealTitle(text: String): String? {
    val firstLine = text.lineSequence().firstOrNull()?.trim().orEmpty()
    return firstLine.takeIf { it.length in 1..80 }
}

private fun parseSecretInput(raw: String): PendingSecretLink? {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return null

    decodeSecretAccessCode(trimmed)?.let { return it }

    if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("/")) {
        val uri = runCatching { android.net.Uri.parse(trimmed) }.getOrNull() ?: return null
        val id = uri.getQueryParameter("id")
            ?: uri.pathSegments.takeIf { it.size >= 3 && it[0] == "vault" && it[1] == "secret" }?.get(2)
            ?: return null
        val fragment = uri.fragment.orEmpty()
        val key = fragment.substringAfter("key=", "").takeIf { it.isNotBlank() }
            ?: uri.getQueryParameter("key")
            ?: return null
        return PendingSecretLink(id.lowercase(), android.net.Uri.decode(key))
    }

    return null
}

private fun decodeSecretAccessCode(raw: String): PendingSecretLink? {
    val parts = raw.trim().split('.')
    if (parts.size != 2) return null
    val id = parts[0].lowercase().takeIf { it.matches(Regex("^[a-f0-9]+$")) } ?: return null
    val urlSafe = parts[1].replace('-', '+').replace('_', '/')
    val padded = urlSafe.padEnd(((urlSafe.length + 3) / 4) * 4, '=')
    return PendingSecretLink(id, padded)
}

private fun com.clex.android.data.SecretPolicy.toBadges(): List<String> {
    val badges = mutableListOf<String>()
    if (viewOnce) badges += "VIEW ONCE"
    if (timedView) badges += "${viewWindowSeconds}s WINDOW"
    if (tabSwitchLock) badges += "BACKGROUND LOCK"
    if (screenshotGuard) badges += "SCREENSHOT GUARD"
    if (noSelect) badges += "NO SELECT"
    if (devtoolsGuard) badges += "DESKTOP DEVTOOLS GUARD"
    badges += "MEMORY ONLY"
    return badges
}

private fun com.clex.android.data.SecretPolicy.toSecretRuleSummary(): String {
    return toBadges().joinToString(" · ")
}
