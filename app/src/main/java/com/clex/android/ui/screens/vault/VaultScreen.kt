package com.clex.android.ui.screens.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.clex.android.data.CreatedSecret
import com.clex.android.data.ClexDriveApi
import com.clex.android.data.ClexVaultApi
import com.clex.android.data.DriveAuthStore
import com.clex.android.data.DriveSession
import com.clex.android.data.DriveUploadItem
import com.clex.android.data.DriveUploadResult
import com.clex.android.data.SecretPolicy
import com.clex.android.data.SecretStatus
import com.clex.android.data.VaultLocalNote
import com.clex.android.data.VaultNotesStore
import com.clex.android.data.VaultPreferencesStore
import com.clex.android.data.VaultSyncStatus
import com.clex.android.ui.anim.*
import com.clex.android.ui.components.*
import com.clex.android.ui.effects.AuroraBackground
import com.clex.android.ui.effects.ParticleField
import com.clex.android.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ═══════════════════════════════════════════════════
//  CLEX — Vault Screen
//  Private encrypted workspace with 4 tabs:
//    Notes | Secret Share | Cloud Share | Settings
//  All states: default, loading, active, success,
//  error, empty, offline
// ═══════════════════════════════════════════════════

enum class VaultTab { NOTES, SECRET, CLOUD, SETTINGS }

@Composable
fun VaultScreen() {
    var currentTab by remember { mutableStateOf(VaultTab.NOTES) }
    val tabVisible = rememberEntryVisibility(currentTab)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CxTheme.colors.bgPrimary)
    ) {
        AuroraBackground(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.2f)
        )
        ParticleField(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.12f),
            particleCount = 22,
            connectDistance = 110f
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ──
            VaultTopBar()

            // ── Tab Selector ──
            VaultTabSelector(currentTab) { currentTab = it }

            // ── Content ──
            // Match the workspace tab transition shape (v1.9.10): directional
            // slides keyed by ordinal so vault tabs swap in the direction of
            // travel rather than soft-fading. Order: NOTES → SECRET → CLOUD →
            // SETTINGS.
            AnimatedContent(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                targetState = currentTab,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    val slideSpec = tween<IntOffset>(durationMillis = 260, easing = FastOutSlowInEasing)
                    val fadeSpec = tween<Float>(durationMillis = 200, easing = LinearEasing)
                    val enter = slideIntoContainer(
                        towards = if (forward) AnimatedContentTransitionScope.SlideDirection.Left
                        else AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = slideSpec,
                    ) + fadeIn(animationSpec = fadeSpec)
                    val exit = slideOutOfContainer(
                        towards = if (forward) AnimatedContentTransitionScope.SlideDirection.Left
                        else AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = slideSpec,
                    ) + fadeOut(animationSpec = fadeSpec)
                    enter togetherWith exit using SizeTransform(clip = false)
                },
                label = "vaultTab",
            ) { tab ->
                when (tab) {
                    VaultTab.NOTES -> NotesTab(tabVisible)
                    VaultTab.SECRET -> SecretShareTab(tabVisible)
                    VaultTab.CLOUD -> CloudShareTab(tabVisible)
                    VaultTab.SETTINGS -> VaultSettingsTab(tabVisible)
                }
            }
        }
    }
}

@Composable
private fun VaultTopBar() {
    val colors = CxTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(colors.bgPrimary)
            .padding(horizontal = CxSpacing.screenHorizontal, vertical = CxSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PageMark(glyph = "◈", title = "VAULT")
        TopBarStatusChip(text = "ENCRYPTED", accentColor = CxColors.success, showDot = true)
    }
}

@Composable
private fun VaultTabSelector(
    currentTab: VaultTab,
    onSelect: (VaultTab) -> Unit
) {
    val colors = CxTheme.colors
    val hapticView = rememberHapticView()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CxSpacing.screenHorizontal)
            .clip(RoundedCornerShape(22.dp))
            .background(colors.bgCard.copy(alpha = if (colors.isDark) 0.72f else 0.92f))
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(22.dp))
    ) {
        VaultTab.entries.forEach { tab ->
            val isActive = tab == currentTab
            val label = when (tab) {
                VaultTab.NOTES -> "NOTES"
                VaultTab.SECRET -> "SECRET"
                VaultTab.CLOUD -> "CLOUD"
                VaultTab.SETTINGS -> "VAULT+"
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (isActive) colors.accent.copy(alpha = if (colors.isDark) 0.16f else 0.12f)
                        else Color.Transparent
                    )
                    .border(
                        width = if (isActive) 1.dp else 0.dp,
                        color = if (isActive) colors.accent.copy(alpha = 0.42f) else Color.Transparent,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (!isActive) CxHaptics.snap(hapticView)
                            onSelect(tab)
                        }
                    )
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MonoText(
                    text = label,
                    fontSize = CxTypography.textXs,
                    fontWeight = if (isActive) CxTypography.weightBlack else CxTypography.weightMedium,
                    color = if (isActive) colors.accent else colors.textTertiary,
                    letterSpacing = CxTypography.textXs * 0.08,
                    maxLines = 1
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════
//  NOTES TAB
// ═══════════════════════════════════════════════════

@Composable
private fun NotesTab(visible: Boolean) {
    val colors = CxTheme.colors
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val notesStore = remember(context) {
        VaultNotesStore.get(context.applicationContext)
    }
    val notes by notesStore.notes.collectAsState()
    val isLoading by notesStore.isLoading.collectAsState()
    val syncStatus by notesStore.syncState.collectAsState()
    var editingNoteId by rememberSaveable { mutableStateOf<String?>(null) }
    var noteTitle by rememberSaveable { mutableStateOf("") }
    var noteBody by rememberSaveable { mutableStateOf("") }

    if (editingNoteId != null) {
        NoteEditorPanel(
            title = noteTitle,
            body = noteBody,
            isExisting = editingNoteId?.isNotBlank() == true,
            onTitleChange = { noteTitle = it },
            onBodyChange = { noteBody = it },
            onSave = {
                notesStore.saveNote(
                    noteId = editingNoteId?.takeIf { it.isNotBlank() },
                    title = noteTitle,
                    body = noteBody,
                ) {
                    editingNoteId = null
                    noteTitle = ""
                    noteBody = ""
                }
            },
            onDelete = if (editingNoteId?.isNotBlank() == true) {
                {
                    notes.firstOrNull { it.id == editingNoteId }?.let { note ->
                        notesStore.deleteNote(note) {
                            editingNoteId = null
                            noteTitle = ""
                            noteBody = ""
                        }
                    }
                }
            } else {
                null
            },
            onBack = {
                editingNoteId = null
                noteTitle = ""
                noteBody = ""
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(top = CxSpacing.xl)
    ) {
        when {
            isLoading -> {
                Spacer(Modifier.height(CxSpacing.xxxxl))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MonoText(
                        text = "LOADING NOTES",
                        fontSize = CxTypography.textBase,
                        color = colors.accent,
                        letterSpacing = CxTypography.textXs * 0.2,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(CxSpacing.lg))
                    BrutalistProgressBar(progress = 0.35f)
                }
            }

            notes.isNotEmpty() -> {
            RevealFromBottom(visible = visible, delayMs = 100) {
                Column {
                    NotesHeaderRow(
                        noteCount = notes.size,
                        onCreate = {
                            editingNoteId = ""
                            noteTitle = ""
                            noteBody = ""
                        }
                    )
                    Spacer(Modifier.height(CxSpacing.sm))
                    MonoText(
                        text = syncStatus.toNotesStatusLine(),
                        fontSize = CxTypography.textXs,
                        color = if (syncStatus.error != null) CxColors.warning else colors.textTertiary,
                    )
                }
            }

            Spacer(Modifier.height(CxSpacing.lg))

            RevealFromBottom(visible = visible, delayMs = 200) {
                FolderRow(name = "LOCAL NOTES", count = notes.size)
            }

            Spacer(Modifier.height(CxSpacing.lg))

            notes.forEachIndexed { index, note ->
                RevealFromBottom(visible = visible, delayMs = 350L + index * CxAnim.staggerDelay) {
                    NoteCard(
                        note = note,
                        onOpen = {
                            editingNoteId = note.id
                            noteTitle = note.title
                            noteBody = note.body
                        },
                        onCopy = {
                            copyToClipboard(context, "Vault note", buildString {
                                append(note.title)
                                append("\n\n")
                                append(note.body)
                            })
                        },
                        onDelete = {
                            notesStore.deleteNote(note)
                        }
                    )
                }
                if (index < notes.lastIndex) {
                    Spacer(Modifier.height(CxSpacing.sm))
                }
            }
            }

            else -> {
            Spacer(Modifier.height(CxSpacing.xxxxl))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MonoText(
                    text = "✎",
                    fontSize = CxTypography.text6xl,
                    color = colors.textTertiary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(CxSpacing.lg))
                SectionTitle(text = "NO NOTES YET", color = colors.textTertiary)
                Spacer(Modifier.height(CxSpacing.md))
                BodyText(
                    text = "Create your first encrypted note. It stays local first, with optional encrypted cloud backup in Vault settings.",
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(CxSpacing.xl))
                BrutalistButton(
                    text = "CREATE NOTE",
                    onClick = {
                        editingNoteId = ""
                        noteTitle = ""
                        noteBody = ""
                    },
                    variant = ButtonVariant.PRIMARY,
                    size = ButtonSize.LARGE
                )
            }
        }
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun NotesHeaderRow(
    noteCount: Int,
    onCreate: () -> Unit
) {
    val colors = CxTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(colors.bgCard)
                .border(CxBorders.thin, colors.borderSubtle)
                .padding(horizontal = CxSpacing.md, vertical = CxSpacing.md),
            verticalArrangement = Arrangement.Center
        ) {
            MonoText(
                text = "MY NOTES",
                fontSize = CxTypography.textSm,
                fontWeight = CxTypography.weightBold,
                color = colors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            MonoText(
                text = "$noteCount SAVED",
                fontSize = CxTypography.textXs,
                color = colors.textTertiary
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(colors.accent.copy(alpha = if (colors.isDark) 0.16f else 0.14f))
                .border(CxBorders.thin, colors.accent.copy(alpha = 0.75f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCreate
                )
                .padding(horizontal = CxSpacing.md, vertical = CxSpacing.md),
            contentAlignment = Alignment.Center
        ) {
            MonoText(
                text = "+ NEW NOTE",
                fontSize = CxTypography.textSm,
                fontWeight = CxTypography.weightBold,
                color = colors.accent
            )
        }
    }
}

@Composable
private fun FolderRow(name: String, count: Int) {
    val colors = CxTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgCard)
            .clickable {}
            .border(1.dp, colors.borderSubtle)
            .padding(horizontal = CxSpacing.md, vertical = CxSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MonoText(text = "▸", fontSize = CxTypography.textLg, color = colors.accent)
            Spacer(Modifier.width(CxSpacing.sm))
            MonoText(
                text = name,
                fontSize = CxTypography.textSm,
                fontWeight = CxTypography.weightBold,
                color = colors.textPrimary
            )
        }
        MonoText(
            text = "$count",
            fontSize = CxTypography.textXs,
            color = colors.textTertiary
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(
    note: VaultLocalNote,
    onOpen: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = CxTheme.colors
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgCard)
            .border(1.dp, colors.borderSubtle)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onOpen,
                onLongClick = { showMenu = true }
            )
            .padding(CxSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MonoText(
                text = note.title,
                fontSize = CxTypography.textSm,
                fontWeight = CxTypography.weightBold,
                color = colors.textPrimary,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(CxSpacing.sm))
            MonoText(
                text = "⋯",
                fontSize = CxTypography.textBase,
                color = colors.accent,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showMenu = true }
                )
            )
        }
        Spacer(Modifier.height(CxSpacing.xs))
        BodyText(
            text = note.body.ifBlank { "Empty note" },
            fontSize = CxTypography.textXs,
            maxLines = 2
        )
        Spacer(Modifier.height(CxSpacing.xs))
        MonoText(
            text = formatVaultNoteTime(note.updatedAt),
            fontSize = CxTypography.textXs,
            color = colors.textTertiary,
            letterSpacing = CxTypography.textXs * 0.1
        )
    }

    if (showMenu) {
        NoteActionsDialog(
            onOpen = {
                showMenu = false
                onOpen()
            },
            onCopy = {
                showMenu = false
                onCopy()
            },
            onDelete = {
                showMenu = false
                onDelete()
            },
            onDismiss = { showMenu = false }
        )
    }
}

@Composable
private fun NoteEditorPanel(
    title: String,
    body: String,
    isExisting: Boolean,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?,
    onBack: () -> Unit,
) {
    val colors = CxTheme.colors
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(top = CxSpacing.xl)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(text = if (isExisting) "Edit Note" else "New Note")
            BrutalistButton(
                text = "BACK",
                onClick = onBack,
                variant = ButtonVariant.GHOST,
                size = ButtonSize.SMALL
            )
        }

        Spacer(Modifier.height(CxSpacing.lg))

        MonoText(
            text = "TITLE",
            fontSize = CxTypography.textXs,
            color = colors.textTertiary,
            letterSpacing = CxTypography.textXs * 0.15
        )
        Spacer(Modifier.height(CxSpacing.sm))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.borderColor)
                .background(colors.bgInput)
                .padding(horizontal = CxSpacing.md, vertical = CxSpacing.md)
        ) {
            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                singleLine = true,
                cursorBrush = SolidColor(colors.accent),
                textStyle = TextStyle(
                    color = colors.textPrimary,
                    fontFamily = CxTypography.fontMono,
                    fontSize = CxTypography.textBase
                ),
                decorationBox = { inner ->
                    if (title.isBlank()) {
                        MonoText(
                            text = "UNTITLED NOTE",
                            fontSize = CxTypography.textSm,
                            color = colors.textTertiary
                        )
                    }
                    inner()
                }
            )
        }

        Spacer(Modifier.height(CxSpacing.lg))

        MonoText(
            text = "BODY",
            fontSize = CxTypography.textXs,
            color = colors.textTertiary,
            letterSpacing = CxTypography.textXs * 0.15
        )
        Spacer(Modifier.height(CxSpacing.sm))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 400.dp)
                .border(1.dp, colors.borderColor)
                .background(colors.bgInput)
                .padding(horizontal = CxSpacing.md, vertical = CxSpacing.md)
        ) {
            BasicTextField(
                value = body,
                onValueChange = onBodyChange,
                cursorBrush = SolidColor(colors.accent),
                textStyle = TextStyle(
                    color = colors.textPrimary,
                    fontFamily = CxTypography.fontBody,
                    fontSize = CxTypography.textBase,
                    lineHeight = CxTypography.textBase * 1.5
                ),
                decorationBox = { inner ->
                    if (body.isBlank()) {
                        BodyText(
                            text = "Write your private note here. It stays encrypted in your vault and can sync through Vault+ backup.",
                            color = colors.textTertiary
                        )
                    }
                    inner()
                }
            )
        }

        Spacer(Modifier.height(CxSpacing.xl))

        BrutalistButton(
            text = "SAVE NOTE",
            onClick = onSave,
            variant = ButtonVariant.PRIMARY,
            size = ButtonSize.LARGE,
            modifier = Modifier.fillMaxWidth()
        )

        if (onDelete != null) {
            Spacer(Modifier.height(CxSpacing.md))
            BrutalistButton(
                text = "DELETE NOTE",
                onClick = onDelete,
                variant = ButtonVariant.GHOST,
                size = ButtonSize.MEDIUM,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(120.dp))
    }
}

private fun formatVaultNoteTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour

    return when {
        diff < minute -> "JUST NOW"
        diff < hour -> "${diff / minute} MIN AGO"
        diff < day -> "${diff / hour} HOUR AGO"
        diff < 2 * day -> "YESTERDAY"
        else -> {
            val formatter = DateTimeFormatter.ofPattern("dd MMM")
            formatter.format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())).uppercase()
        }
    }
}

// ═══════════════════════════════════════════════════
//  SECRET SHARE TAB
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SecretShareTab(visible: Boolean) {
    val colors = CxTheme.colors
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val preferencesStore = remember(context) {
        VaultPreferencesStore.get(context.applicationContext)
    }
    val vaultPreferences by preferencesStore.settings.collectAsState()

    var secretContent by rememberSaveable { mutableStateOf("") }
    var selectedExpirySeconds by rememberSaveable { mutableIntStateOf(vaultPreferences.defaultSecretExpirySeconds) }
    var viewOnce by rememberSaveable { mutableStateOf(vaultPreferences.defaultViewOnce) }
    var timedView by rememberSaveable { mutableStateOf(vaultPreferences.defaultTimedView) }
    var noSelect by rememberSaveable { mutableStateOf(vaultPreferences.defaultNoSelect) }
    var tabSwitchLock by rememberSaveable { mutableStateOf(vaultPreferences.defaultTabSwitchLock) }
    var devtoolsGuard by rememberSaveable { mutableStateOf(vaultPreferences.defaultDevtoolsGuard) }
    var screenshotGuard by rememberSaveable { mutableStateOf(vaultPreferences.defaultScreenshotGuard) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var createdSecret by remember { mutableStateOf<CreatedSecret?>(null) }
    var secretStatus by remember { mutableStateOf<SecretStatus?>(null) }

    // FLAG_SECURE: prevent screenshots when a protected secret is visible
    val activity = context as? android.app.Activity
    DisposableEffect(createdSecret, screenshotGuard) {
        if (createdSecret != null && screenshotGuard && activity != null) {
            activity.window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        onDispose {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    val selectedPolicy = SecretPolicy(
        viewOnce = viewOnce,
        timedView = timedView,
        noSelect = noSelect,
        tabSwitchLock = tabSwitchLock,
        devtoolsGuard = devtoolsGuard,
        screenshotGuard = screenshotGuard,
        memoryOnly = true,
        viewWindowSeconds = if (timedView) 60 else 0
    )

    LaunchedEffect(createdSecret?.id) {
        val secretId = createdSecret?.id ?: return@LaunchedEffect
        while (isActive) {
            val latestStatus = runCatching { ClexVaultApi.fetchSecretStatus(secretId) }.getOrNull()
            if (latestStatus != null) {
                secretStatus = latestStatus
                if (!latestStatus.exists || (latestStatus.alreadyOpened && createdSecret?.policy?.viewOnce == true)) {
                    break
                }
            }
            delay(5_000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(top = CxSpacing.xl)
    ) {
        RevealFromBottom(visible = visible, delayMs = 100) {
            SectionLabel(text = "Secret Share")
        }
        Spacer(Modifier.height(CxSpacing.md))
        RevealFromBottom(visible = visible, delayMs = 200) {
            BodyText(text = "Paste sensitive text. Choose protections. Generate a self-destructing link.")
        }

        Spacer(Modifier.height(CxSpacing.xl))

        when {
            isSubmitting -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(CxSpacing.xxxxl))
                    MonoText(
                        text = "ENCRYPTING...",
                        fontSize = CxTypography.textLg,
                        color = colors.accent,
                        letterSpacing = CxTypography.textSm * 0.2,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(CxSpacing.xl))
                    BrutalistProgressBar(
                        progress = 0.7f,
                        accentColor = colors.accent
                    )
                }
            }

            createdSecret != null -> {
                val secret = createdSecret!!
                val statusText = when {
                    secretStatus == null -> "SYNCING STATUS"
                    secretStatus?.exists == false -> "NO LONGER AVAILABLE"
                    secretStatus?.alreadyOpened == true -> "OPENED"
                    else -> "WAITING FOR REVEAL"
                }
                val statusColor = when {
                    secretStatus == null -> colors.textTertiary
                    secretStatus?.exists == false -> CxColors.error
                    secretStatus?.alreadyOpened == true -> CxColors.warning
                    else -> CxColors.success
                }

                var linkVisible by remember { mutableStateOf(false) }
                LaunchedEffect(secret.id) {
                    delay(100)
                    linkVisible = true
                }

                SlamIn(visible = linkVisible) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MonoText(
                            text = "◈",
                            fontSize = CxTypography.text4xl,
                            color = CxColors.success,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(CxSpacing.md))
                        SectionTitle(text = "SECRET LINK READY", color = CxColors.success)
                    }
                }

                Spacer(Modifier.height(CxSpacing.xl))

                RevealFromBottom(visible = linkVisible, delayMs = 300) {
                    SecretValuePanel(
                        title = "LINK",
                        value = secret.linkUrl
                    )
                }

                // QR for quick handoff — scan to open secret on other device
                if (linkVisible && secret.linkUrl.isNotBlank()) {
                    Spacer(Modifier.height(CxSpacing.md))
                    FramedQRCode(
                        content = secret.linkUrl,
                        label = "SCAN TO OPEN SECRET",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(CxSpacing.md))

                RevealFromBottom(visible = linkVisible, delayMs = 360) {
                    SecretValuePanel(
                        title = "REVEAL CODE",
                        value = formatRevealCode(secret.accessCode)
                    )
                }

                Spacer(Modifier.height(CxSpacing.md))

                RevealFromBottom(visible = linkVisible, delayMs = 400) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CxSpacing.md)
                    ) {
                        BrutalistButton(
                            text = "COPY LINK",
                            onClick = {
                                copyToClipboard(context, "Secret link", secret.linkUrl)
                            },
                            variant = ButtonVariant.PRIMARY,
                            size = ButtonSize.MEDIUM,
                            modifier = Modifier.weight(1f)
                        )
                        BrutalistButton(
                            text = "COPY CODE",
                            onClick = {
                                copyToClipboard(context, "Reveal code", secret.accessCode)
                            },
                            variant = ButtonVariant.SECONDARY,
                            size = ButtonSize.MEDIUM,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(CxSpacing.lg))

                RevealFromBottom(visible = linkVisible, delayMs = 500) {
                    Column {
                        SecretMetaRow(
                            label = "STATUS:",
                            value = statusText,
                            valueColor = statusColor
                        )
                        Spacer(Modifier.height(CxSpacing.xs))
                        SecretMetaRow(
                            label = "PROTECTIONS:",
                            value = secret.policy.toProtectionSummary(),
                            valueColor = colors.accent
                        )
                        Spacer(Modifier.height(CxSpacing.xs))
                        SecretMetaRow(
                            label = "EXPIRES:",
                            value = formatExpiry(secret.expiresAt),
                            valueColor = colors.textSecondary
                        )
                        Spacer(Modifier.height(CxSpacing.xs))
                        SecretMetaRow(
                            label = "STORAGE:",
                            value = "MEMORY ONLY",
                            valueColor = CxColors.success
                        )
                    }
                }

                Spacer(Modifier.height(CxSpacing.lg))

                RevealFromBottom(visible = linkVisible, delayMs = 560) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(CxSpacing.sm)
                    ) {
                        secret.policy.toProtectionBadges().forEach { badge ->
                            BrutalistBadge(text = badge, accentColor = colors.accent)
                        }
                    }
                }

                Spacer(Modifier.height(CxSpacing.xxl))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CxSpacing.md)
                ) {
                    BrutalistButton(
                        text = "OPEN LINK",
                        onClick = { uriHandler.openUri(secret.linkUrl) },
                        variant = ButtonVariant.SECONDARY,
                        size = ButtonSize.MEDIUM,
                        modifier = Modifier.weight(1f)
                    )
                    BrutalistButton(
                        text = "NEW SECRET",
                        onClick = {
                            createdSecret = null
                            secretStatus = null
                            submitError = null
                            secretContent = ""
                        },
                        variant = ButtonVariant.GHOST,
                        size = ButtonSize.MEDIUM,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            else -> {
                RevealFromBottom(visible = visible, delayMs = 300) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .border(CxBorders.thick, colors.borderBold)
                            .background(colors.bgInput)
                            .padding(CxSpacing.md)
                    ) {
                        BasicTextField(
                            value = secretContent,
                            onValueChange = { nextValue ->
                                if (nextValue.length <= 10_000) {
                                    secretContent = nextValue
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            cursorBrush = SolidColor(colors.accent),
                            textStyle = TextStyle(
                                color = colors.textPrimary,
                                fontFamily = CxTypography.fontBody,
                                fontWeight = CxTypography.weightRegular,
                                fontSize = CxTypography.textBase,
                                lineHeight = CxTypography.textBase * 1.5
                            ),
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (secretContent.isBlank()) {
                                        MonoText(
                                            text = "PASTE SECRET TEXT HERE...",
                                            fontSize = CxTypography.textSm,
                                            color = colors.textTertiary
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(CxSpacing.xl))

                // Protection toggles
                RevealFromBottom(visible = visible, delayMs = 400) {
                    Column {
                        MonoText(
                            text = "PROTECTIONS",
                            fontSize = CxTypography.textXs,
                            fontWeight = CxTypography.weightBold,
                            color = colors.textTertiary,
                            letterSpacing = CxTypography.textXs * 0.2
                        )
                        Spacer(Modifier.height(CxSpacing.md))

                        val protections = listOf(
                            SecretToggleItem("VIEW ONCE", viewOnce) { viewOnce = !viewOnce },
                            SecretToggleItem("60S WINDOW", timedView) { timedView = !timedView },
                            SecretToggleItem("NO SELECT", noSelect) { noSelect = !noSelect },
                            SecretToggleItem("TAB LOCK", tabSwitchLock) { tabSwitchLock = !tabSwitchLock },
                            SecretToggleItem("DEVTOOLS GUARD", devtoolsGuard) { devtoolsGuard = !devtoolsGuard },
                            SecretToggleItem("SCREENSHOT GUARD", screenshotGuard) { screenshotGuard = !screenshotGuard }
                        )

                        protections.forEach { item ->
                            ProtectionToggle(
                                label = item.label,
                                enabled = item.enabled,
                                onToggle = item.onToggle
                            )
                            Spacer(Modifier.height(CxSpacing.sm))
                        }
                    }
                }

                Spacer(Modifier.height(CxSpacing.xl))

                // Expiry selector
                RevealFromBottom(visible = visible, delayMs = 500) {
                    var showCustomExpiry by remember { mutableStateOf(false) }
                    val presets = listOf(
                        "5M" to (5 * 60),
                        "30M" to (30 * 60),
                        "1H" to (60 * 60),
                        "24H" to (24 * 60 * 60),
                        "7D" to (7 * 24 * 60 * 60),
                    )
                    val isCustom = presets.none { it.second == selectedExpirySeconds }

                    Column {
                        MonoText(
                            text = "LINK EXPIRY",
                            fontSize = CxTypography.textXs,
                            fontWeight = CxTypography.weightBold,
                            color = colors.textTertiary,
                            letterSpacing = CxTypography.textXs * 0.2
                        )
                        Spacer(Modifier.height(CxSpacing.md))
                        Row(horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm)) {
                            presets.take(4).forEach { (label, ttlSeconds) ->
                                BrutalistBadge(
                                    text = label,
                                    filled = selectedExpirySeconds == ttlSeconds,
                                    accentColor = colors.accent,
                                    modifier = Modifier.clickable {
                                        selectedExpirySeconds = ttlSeconds
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(CxSpacing.sm))
                        Row(horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm)) {
                            presets.drop(4).forEach { (label, ttlSeconds) ->
                                BrutalistBadge(
                                    text = label,
                                    filled = selectedExpirySeconds == ttlSeconds,
                                    accentColor = colors.accent,
                                    modifier = Modifier.clickable {
                                        selectedExpirySeconds = ttlSeconds
                                    }
                                )
                            }
                            BrutalistBadge(
                                text = if (isCustom) "CUSTOM ✓" else "CUSTOM",
                                filled = isCustom,
                                accentColor = colors.accentSecondary,
                                modifier = Modifier.clickable { showCustomExpiry = true }
                            )
                        }
                    }

                    if (showCustomExpiry) {
                        CustomExpiryDialog(
                            currentSeconds = selectedExpirySeconds,
                            onConfirm = { seconds ->
                                selectedExpirySeconds = seconds
                                showCustomExpiry = false
                            },
                            onDismiss = { showCustomExpiry = false }
                        )
                    }
                }

                Spacer(Modifier.height(CxSpacing.xxl))

                RevealFromBottom(visible = visible, delayMs = 600) {
                    BrutalistButton(
                        text = "GENERATE SECRET LINK →",
                        onClick = {
                            if (secretContent.trim().isEmpty()) {
                                submitError = "Paste secret text before generating a link."
                            } else {
                                scope.launch {
                                    submitError = null
                                    secretStatus = null
                                    isSubmitting = true
                                    val result = runCatching {
                                        ClexVaultApi.createSecret(
                                            plaintext = secretContent.trim(),
                                            ttlSeconds = selectedExpirySeconds,
                                            policy = selectedPolicy
                                        )
                                    }
                                    result.onSuccess { secret ->
                                        createdSecret = secret
                                        secretStatus = SecretStatus(
                                            exists = true,
                                            alreadyOpened = false,
                                            openedAt = null,
                                            expiresAt = secret.expiresAt,
                                            policy = secret.policy
                                        )
                                        CxHaptics.success(context)
                                    }.onFailure { error ->
                                        submitError = error.message ?: "Vault could not create the secret link."
                                        CxHaptics.error(context)
                                    }
                                    isSubmitting = false
                                }
                            }
                        },
                        variant = ButtonVariant.PRIMARY,
                        size = ButtonSize.LARGE,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                submitError?.let { message ->
                    Spacer(Modifier.height(CxSpacing.md))
                    BrutalistCard {
                        CardTitle(
                            text = "CREATION FAILED",
                            fontSize = CxTypography.textBase,
                            color = CxColors.error
                        )
                        Spacer(Modifier.height(CxSpacing.sm))
                        BodyText(text = message, fontSize = CxTypography.textSm)
                    }
                }
            }
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun ProtectionToggle(
    label: String,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val colors = CxTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(CxBorders.thin, if (enabled) colors.accent else colors.borderColor)
            .background(if (enabled) colors.accentMuted else colors.bgCard)
            .clickable { onToggle() }
            .padding(horizontal = CxSpacing.md, vertical = CxSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MonoText(
            text = label,
            fontSize = CxTypography.textSm,
            fontWeight = CxTypography.weightBold,
            color = if (enabled) colors.accent else colors.textSecondary
        )
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 20.dp)
                .border(CxBorders.thin, if (enabled) colors.accent else colors.borderColor)
                .background(if (enabled) colors.accent else Color.Transparent),
            contentAlignment = if (enabled) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .padding(2.dp)
                    .background(if (enabled) CxColors.pureBlack else colors.textTertiary)
            )
        }
    }
}

@Composable
private fun SecretValuePanel(
    title: String,
    value: String
) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(CxBorders.thick, colors.accent)
            .background(colors.accentMuted)
            .padding(CxSpacing.md)
    ) {
        MonoText(
            text = title,
            fontSize = CxTypography.textXs,
            color = colors.textTertiary
        )
        Spacer(Modifier.height(CxSpacing.xs))
        BodyText(
            text = value,
            fontSize = CxTypography.textSm,
            color = colors.textPrimary
        )
    }
}

@Composable
private fun SecretMetaRow(
    label: String,
    value: String,
    valueColor: Color
) {
    val colors = CxTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MonoText(text = label, fontSize = CxTypography.textXs, color = colors.textTertiary)
        MonoText(
            text = value,
            fontSize = CxTypography.textXs,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

private fun SecretPolicy.toProtectionSummary(): String {
    val enabled = toProtectionBadges()
    return if (enabled.isEmpty()) "STANDARD" else enabled.joinToString(" + ")
}

private fun SecretPolicy.toProtectionBadges(): List<String> {
    val badges = mutableListOf<String>()
    if (viewOnce) badges += "VIEW ONCE"
    if (timedView) badges += "${viewWindowSeconds}s WINDOW"
    if (noSelect) badges += "NO SELECT"
    if (tabSwitchLock) badges += "TAB LOCK"
    if (devtoolsGuard) badges += "DEVTOOLS GUARD"
    if (screenshotGuard) badges += "SCREENSHOT GUARD"
    return badges
}

private fun formatRevealCode(raw: String): String {
    return raw.chunked(4).joinToString(" ")
}

@Composable
private fun CustomExpiryDialog(
    currentSeconds: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = CxTheme.colors
    var hoursText by remember { mutableStateOf((currentSeconds / 3600).coerceAtLeast(1).toString()) }
    var minutesText by remember { mutableStateOf(((currentSeconds % 3600) / 60).toString()) }
    val maxSeconds = 7 * 24 * 60 * 60

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(CxBorders.thin, colors.borderColor)
                .background(colors.bgPrimary)
                .padding(CxSpacing.cardPadding)
        ) {
            MonoText(
                text = "CUSTOM EXPIRY",
                fontSize = CxTypography.textLg,
                fontWeight = CxTypography.weightBold,
                color = colors.accent
            )
            Spacer(Modifier.height(CxSpacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CxSpacing.md)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    MonoText(
                        text = "HOURS",
                        fontSize = CxTypography.textXs,
                        color = colors.textTertiary
                    )
                    Spacer(Modifier.height(CxSpacing.xs))
                    androidx.compose.foundation.text.BasicTextField(
                        value = hoursText,
                        onValueChange = { hoursText = it.filter { c -> c.isDigit() }.take(4) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = colors.textPrimary,
                            fontFamily = CxTypography.fontMono,
                            fontWeight = CxTypography.weightBold,
                            fontSize = CxTypography.text2xl,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(CxBorders.medium, colors.borderBold)
                            .background(colors.bgInput)
                            .padding(CxSpacing.md)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    MonoText(
                        text = "MINUTES",
                        fontSize = CxTypography.textXs,
                        color = colors.textTertiary
                    )
                    Spacer(Modifier.height(CxSpacing.xs))
                    androidx.compose.foundation.text.BasicTextField(
                        value = minutesText,
                        onValueChange = { minutesText = it.filter { c -> c.isDigit() }.take(2) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = colors.textPrimary,
                            fontFamily = CxTypography.fontMono,
                            fontWeight = CxTypography.weightBold,
                            fontSize = CxTypography.text2xl,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(CxBorders.medium, colors.borderBold)
                            .background(colors.bgInput)
                            .padding(CxSpacing.md)
                    )
                }
            }

            Spacer(Modifier.height(CxSpacing.lg))
            val totalSeconds = ((hoursText.toIntOrNull() ?: 0) * 3600 + (minutesText.toIntOrNull() ?: 0) * 60)
                .coerceAtMost(maxSeconds)
            BodyText(
                text = if (totalSeconds > 0) {
                    val h = totalSeconds / 3600
                    val m = (totalSeconds % 3600) / 60
                    "Link will expire after ${if (h > 0) "${h}h " else ""}${if (m > 0) "${m}m" else ""}".trim()
                } else "Enter at least 1 minute (max 7 days)",
                fontSize = CxTypography.textXs
            )

            Spacer(Modifier.height(CxSpacing.lg))
            BrutalistButton(
                text = "SET EXPIRY",
                onClick = { if (totalSeconds >= 60) onConfirm(totalSeconds) },
                variant = ButtonVariant.PRIMARY,
                size = ButtonSize.MEDIUM,
                enabled = totalSeconds >= 60,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(CxSpacing.sm))
            BrutalistButton(
                text = "CANCEL",
                onClick = onDismiss,
                variant = ButtonVariant.GHOST,
                size = ButtonSize.MEDIUM,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatExpiry(expiresAt: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm")
    return formatter.format(
        Instant.ofEpochMilli(expiresAt).atZone(ZoneId.systemDefault())
    ).uppercase()
}

private fun copyToClipboard(
    context: android.content.Context,
    label: String,
    value: String
) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, value))
    Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
}

private data class SecretToggleItem(
    val label: String,
    val enabled: Boolean,
    val onToggle: () -> Unit
)

// ═══════════════════════════════════════════════════
//  CLOUD SHARE TAB
// ═══════════════════════════════════════════════════

@Composable
private fun CloudShareTab(visible: Boolean) {
    val colors = CxTheme.colors
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val driveAuthStore = remember(context) {
        DriveAuthStore.get(context.applicationContext)
    }
    val driveSession by driveAuthStore.session.collectAsState()
    var selectedFiles by remember { mutableStateOf<List<CloudDraftFile>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableIntStateOf(0) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var uploadResult by remember { mutableStateOf<DriveUploadResult?>(null) }
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            val nextFiles = uris.mapNotNull { uri -> context.toCloudDraftFile(uri) }
            if (nextFiles.isEmpty()) {
                uploadError = "The selected files could not be prepared for Drive upload."
            } else {
                selectedFiles = (selectedFiles + nextFiles).distinctBy { it.uri.toString() }
                uploadError = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(top = CxSpacing.xl)
    ) {
        RevealFromBottom(visible = visible, delayMs = 100) {
            SectionLabel(text = "Cloud Share")
        }
        Spacer(Modifier.height(CxSpacing.md))
        RevealFromBottom(visible = visible, delayMs = 200) {
            BodyText(
                text = "Upload files to your Google Drive. Generate timed share links. Auto-delete after 24 hours."
            )
        }

        Spacer(Modifier.height(CxSpacing.xl))

        // Google sign-in CTA
        RevealFromBottom(visible = visible, delayMs = 300) {
            BrutalistCard(accentBorder = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        MonoText(
                            text = "GOOGLE DRIVE",
                            fontSize = CxTypography.textBase,
                            fontWeight = CxTypography.weightBold,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(CxSpacing.xs))
                        MonoText(
                            text = driveSession.toDriveStatusLine(),
                            fontSize = CxTypography.textXs,
                            color = if (driveSession == null) colors.textTertiary else CxColors.success
                        )
                    }
                    if (driveSession == null) {
                        BrutalistButton(
                            text = "CONNECT",
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(ClexDriveApi.buildAndroidGoogleAuthUrl())
                                    )
                                )
                            },
                            variant = ButtonVariant.PRIMARY,
                            size = ButtonSize.SMALL
                        )
                    } else {
                        BrutalistButton(
                            text = "DISCONNECT",
                            onClick = {
                                driveAuthStore.clear()
                                selectedFiles = emptyList()
                                uploadResult = null
                                uploadError = null
                                uploadProgress = 0
                            },
                            variant = ButtonVariant.GHOST,
                            size = ButtonSize.SMALL
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(CxSpacing.xl))

        // Limits info
        RevealFromBottom(visible = visible, delayMs = 400) {
            Column {
                MonoText(
                    text = "LIMITS",
                    fontSize = CxTypography.textXs,
                    fontWeight = CxTypography.weightBold,
                    color = colors.textTertiary,
                    letterSpacing = CxTypography.textXs * 0.2
                )
                Spacer(Modifier.height(CxSpacing.md))

                listOf(
                    "MAX FILE SIZE" to "1 GB",
                    "DAILY BUDGET" to "10 GB",
                    "AUTO-DELETE" to "24 HOURS",
                    "STORAGE" to "YOUR GOOGLE DRIVE"
                ).forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = CxSpacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MonoText(text = label, fontSize = CxTypography.textXs, color = colors.textTertiary)
                        MonoText(text = value, fontSize = CxTypography.textXs, color = colors.accent)
                    }
                }
            }
        }

        Spacer(Modifier.height(CxSpacing.xl))

        when {
            uploadResult != null -> {
                val result = uploadResult!!
                RevealFromBottom(visible = visible, delayMs = 500) {
                    BrutalistCard(accentBorder = true) {
                        CardTitle(
                            text = "DRIVE LINK READY",
                            fontSize = CxTypography.textBase,
                            color = CxColors.success
                        )
                        Spacer(Modifier.height(CxSpacing.md))
                        BodyText(
                            text = result.folderName,
                            fontSize = CxTypography.textSm,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(CxSpacing.sm))
                        BodyText(
                            text = result.webViewLink,
                            fontSize = CxTypography.textXs,
                            color = colors.textSecondary
                        )
                        Spacer(Modifier.height(CxSpacing.lg))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(CxSpacing.md)
                        ) {
                            BrutalistButton(
                                text = "OPEN",
                                onClick = { uriHandler.openUri(result.webViewLink) },
                                variant = ButtonVariant.PRIMARY,
                                size = ButtonSize.MEDIUM,
                                modifier = Modifier.weight(1f)
                            )
                            BrutalistButton(
                                text = "COPY LINK",
                                onClick = { copyToClipboard(context, "Drive link", result.webViewLink) },
                                variant = ButtonVariant.SECONDARY,
                                size = ButtonSize.MEDIUM,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(CxSpacing.md))
                        BrutalistButton(
                            text = "UPLOAD MORE",
                            onClick = {
                                uploadResult = null
                                selectedFiles = emptyList()
                                uploadError = null
                                uploadProgress = 0
                            },
                            variant = ButtonVariant.GHOST,
                            size = ButtonSize.MEDIUM,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            driveSession == null -> {
                RevealFromBottom(visible = visible, delayMs = 500) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.borderColor)
                            .background(colors.bgSecondary)
                            .padding(CxSpacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MonoText(
                            text = "◈",
                            fontSize = CxTypography.text2xl,
                            color = colors.textTertiary
                        )
                        Spacer(Modifier.height(CxSpacing.sm))
                        BodyText(
                            text = "Connect your Google Drive above to start uploading files.",
                            fontSize = CxTypography.textXs,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            else -> {
                RevealFromBottom(visible = visible, delayMs = 500) {
                    Column {
                        BrutalistButton(
                            text = if (selectedFiles.isEmpty()) "SELECT FILES" else "ADD MORE FILES",
                            onClick = { filePicker.launch(arrayOf("*/*")) },
                            variant = ButtonVariant.SECONDARY,
                            size = ButtonSize.MEDIUM,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (selectedFiles.isNotEmpty()) {
                            Spacer(Modifier.height(CxSpacing.lg))
                            BrutalistCard {
                                CardTitle(
                                    text = "UPLOAD QUEUE",
                                    fontSize = CxTypography.textBase,
                                    color = colors.textPrimary
                                )
                                Spacer(Modifier.height(CxSpacing.sm))
                                selectedFiles.forEachIndexed { index, file ->
                                    CloudFileRow(
                                        file = file,
                                        onRemove = {
                                            selectedFiles = selectedFiles.filterNot { it.uri == file.uri }
                                        }
                                    )
                                    if (index < selectedFiles.lastIndex) {
                                        Spacer(Modifier.height(CxSpacing.sm))
                                    }
                                }
                                Spacer(Modifier.height(CxSpacing.md))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MonoText(
                                        text = "TOTAL",
                                        fontSize = CxTypography.textXs,
                                        color = colors.textTertiary
                                    )
                                    MonoText(
                                        text = formatCloudBytes(selectedFiles.sumOf { it.size }),
                                        fontSize = CxTypography.textXs,
                                        color = colors.accent
                                    )
                                }
                            }

                            Spacer(Modifier.height(CxSpacing.lg))
                            if (isUploading) {
                                MonoText(
                                    text = "UPLOADING TO DRIVE...",
                                    fontSize = CxTypography.textSm,
                                    color = colors.accent,
                                    letterSpacing = CxTypography.textXs * 0.15
                                )
                                Spacer(Modifier.height(CxSpacing.sm))
                                BrutalistProgressBar(progress = uploadProgress / 100f)
                                Spacer(Modifier.height(CxSpacing.xs))
                                MonoText(
                                    text = "$uploadProgress%",
                                    fontSize = CxTypography.textXs,
                                    color = colors.textTertiary
                                )
                            }

                            Spacer(Modifier.height(CxSpacing.lg))
                            BrutalistButton(
                                text = if (isUploading) "UPLOADING..." else "UPLOAD TO DRIVE →",
                                onClick = {
                                    val session = driveSession
                                    if (!isUploading && session != null) {
                                        scope.launch {
                                            isUploading = true
                                            uploadProgress = 4
                                            uploadError = null
                                            val uploadItems = withContext(Dispatchers.IO) {
                                                selectedFiles.mapNotNull { file ->
                                                    runCatching {
                                                        val bytes = context.contentResolver.openInputStream(file.uri)
                                                            ?.use { it.readBytes() }
                                                            ?: return@mapNotNull null
                                                        DriveUploadItem(
                                                            name = file.name,
                                                            mimeType = file.mimeType,
                                                            bytes = bytes,
                                                        )
                                                    }.getOrNull()
                                                }
                                            }

                                            if (uploadItems.size != selectedFiles.size) {
                                                uploadError = "One or more files could not be read for Drive upload."
                                                isUploading = false
                                                return@launch
                                            }

                                            runCatching {
                                                ClexDriveApi.uploadToDrive(uploadItems, session.token) { progress ->
                                                    uploadProgress = progress
                                                }
                                            }.onSuccess { result ->
                                                uploadResult = result
                                                selectedFiles = emptyList()
                                                CxHaptics.success(context)
                                            }.onFailure { error ->
                                                uploadError = error.message ?: "Drive upload failed."
                                                CxHaptics.error(context)
                                            }

                                            isUploading = false
                                        }
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
        }

        uploadError?.let { message ->
            Spacer(Modifier.height(CxSpacing.md))
            BrutalistCard {
                CardTitle(
                    text = "DRIVE ERROR",
                    fontSize = CxTypography.textBase,
                    color = CxColors.error
                )
                Spacer(Modifier.height(CxSpacing.sm))
                BodyText(text = message, fontSize = CxTypography.textSm)
            }
        }

        if (driveSession != null && selectedFiles.isEmpty() && uploadResult == null && !isUploading) {
            val activeSession = driveSession
            Spacer(Modifier.height(CxSpacing.md))
            BrutalistCard {
                BodyText(
                    text = "Connected as ${activeSession?.user?.email ?: activeSession?.user?.displayName ?: activeSession?.user?.sub}. Pick files here and the app will upload them into your Drive-backed Clex folder using the real backend auth session.",
                    fontSize = CxTypography.textSm,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(Modifier.height(120.dp))
    }
}

private data class CloudDraftFile(
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val size: Long,
)

@Composable
private fun CloudFileRow(
    file: CloudDraftFile,
    onRemove: () -> Unit,
) {
    val colors = CxTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.borderColor)
            .background(colors.bgCard)
            .padding(horizontal = CxSpacing.md, vertical = CxSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            MonoText(
                text = file.name,
                fontSize = CxTypography.textSm,
                fontWeight = CxTypography.weightBold,
                color = colors.textPrimary
            )
            Spacer(Modifier.height(CxSpacing.xs))
            MonoText(
                text = formatCloudBytes(file.size),
                fontSize = CxTypography.textXs,
                color = colors.textTertiary
            )
        }
        BrutalistButton(
            text = "REMOVE",
            onClick = onRemove,
            variant = ButtonVariant.GHOST,
            size = ButtonSize.SMALL
        )
    }
}

private fun DriveSession?.toDriveStatusLine(): String {
    return this?.user?.email
        ?: this?.user?.displayName
        ?: if (this != null) "CONNECTED" else "SIGN IN REQUIRED"
}

private fun android.content.Context.toCloudDraftFile(uri: Uri): CloudDraftFile? {
    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
    var name: String? = null
    var size = 0L

    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) {
                    name = cursor.getString(nameIndex)
                }
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }

    val resolvedName = name?.takeIf { it.isNotBlank() }
        ?: uri.lastPathSegment?.substringAfterLast('/')
        ?: return null

    return CloudDraftFile(
        uri = uri,
        name = resolvedName,
        mimeType = mimeType,
        size = size,
    )
}

private fun formatCloudBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = 0
    while (value >= 1024 && index < units.lastIndex) {
        value /= 1024.0
        index += 1
    }
    val decimals = if (value >= 10 || index == 0) 0 else 1
    return "%.${decimals}f %s".format(value, units[index])
}

private fun VaultSyncStatus.toBackupDescription(hasAccount: Boolean): String {
    return when {
        !hasAccount -> "Sign in to Google Drive to sync encrypted backups across devices."
        error != null -> error
        lastSyncedAt != null -> "Encrypted backup last synced ${formatSyncTimestamp(lastSyncedAt)}."
        else -> "Backup has not run yet on this device."
    }
}

private fun VaultSyncStatus.toNotesStatusLine(): String {
    return when {
        isSyncing -> "SYNCING ENCRYPTED BACKUP…"
        error != null -> error.uppercase()
        lastSyncedAt != null -> "BACKUP ${formatSyncTimestamp(lastSyncedAt).uppercase()}"
        else -> "LOCAL-FIRST ENCRYPTED STORAGE"
    }
}

private fun formatSecretExpiryLabel(seconds: Int): String {
    return when (seconds) {
        5 * 60 -> "5 MINUTES"
        30 * 60 -> "30 MINUTES"
        60 * 60 -> "1 HOUR"
        24 * 60 * 60 -> "24 HOURS"
        7 * 24 * 60 * 60 -> "7 DAYS"
        else -> {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            buildString {
                if (hours > 0) append("$hours H")
                if (minutes > 0) {
                    if (isNotEmpty()) append(" ")
                    append("$minutes M")
                }
            }.ifBlank { "CUSTOM" }
        }
    }
}

private fun formatSyncTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM HH:mm")
    return formatter.format(
        Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
    )
}

// ═══════════════════════════════════════════════════
//  VAULT SETTINGS TAB
// ═══════════════════════════════════════════════════

@Composable
private fun VaultSettingsTab(visible: Boolean) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val notesStore = remember(context) { VaultNotesStore.get(context.applicationContext) }
    val preferencesStore = remember(context) { VaultPreferencesStore.get(context.applicationContext) }
    val driveAuthStore = remember(context) { DriveAuthStore.get(context.applicationContext) }
    val syncStatus by notesStore.syncState.collectAsState()
    val accountDevices by notesStore.accountDevices.collectAsState()
    val driveSession by driveAuthStore.session.collectAsState()
    val vaultPreferences by preferencesStore.settings.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }
    var showCustomExpiry by remember { mutableStateOf(false) }
    var expandedSection by rememberSaveable { mutableStateOf("ACCOUNT") }
    val syncCode = syncStatus.roomId ?: syncStatus.keyFingerprint ?: ""
    val syncCodeDisplay = when {
        syncCode.isBlank() -> "NOT READY"
        syncCode.length > 18 -> "${syncCode.take(8)}…${syncCode.takeLast(6)}"
        else -> syncCode
    }

    fun updateDefaultProtections(
        viewOnce: Boolean = vaultPreferences.defaultViewOnce,
        timedView: Boolean = vaultPreferences.defaultTimedView,
        noSelect: Boolean = vaultPreferences.defaultNoSelect,
        tabSwitchLock: Boolean = vaultPreferences.defaultTabSwitchLock,
        devtoolsGuard: Boolean = vaultPreferences.defaultDevtoolsGuard,
        screenshotGuard: Boolean = vaultPreferences.defaultScreenshotGuard,
    ) {
        preferencesStore.setDefaultSecretProtections(
            viewOnce = viewOnce,
            timedView = timedView,
            noSelect = noSelect,
            tabSwitchLock = tabSwitchLock,
            devtoolsGuard = devtoolsGuard,
            screenshotGuard = screenshotGuard,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(top = CxSpacing.xl)
    ) {
        RevealFromBottom(visible = visible, delayMs = 100) {
            SectionLabel(text = "Vault Plus")
        }

        Spacer(Modifier.height(CxSpacing.md))

        RevealFromBottom(visible = visible, delayMs = 180) {
            SectionTitle(text = "SYNC, RECOVERY\nAND SECURITY")
        }
        Spacer(Modifier.height(CxSpacing.sm))
        BodyText(
            text = "Vault Plus controls real sync, recovery, default secret protection, and encrypted backup behavior for this device.",
            fontSize = CxTypography.textSm
        )

        Spacer(Modifier.height(CxSpacing.xl))

        val accountSummary = syncStatus.accountEmail ?: "LOCAL ONLY"
        val encryptionSummary =
            if (syncStatus.keySource == com.clex.android.data.VaultKeySource.SAME_ACCOUNT) {
                "SAME-ACCOUNT KEY"
            } else {
                "LOCAL DEVICE KEY"
            }

        listOf(
            Triple(
                "ACCOUNT",
                accountSummary,
                if (driveSession != null) {
                    "Active Google account used for encrypted backup sync and device registration."
                } else {
                    "Google Drive is not connected on this device yet."
                }
            ),
            Triple(
                "ENCRYPTION",
                encryptionSummary,
                if (syncStatus.keySource == com.clex.android.data.VaultKeySource.SAME_ACCOUNT) {
                    "Deterministic vault key derived from the signed-in account."
                } else {
                    "Local recovery key stored on this device."
                }
            ),
            Triple(
                "SYNC CODE",
                syncCodeDisplay,
                "Vault room namespace used for encrypted backup and recovery sync."
            ),
            Triple(
                "DEFAULT EXPIRY",
                formatSecretExpiryLabel(vaultPreferences.defaultSecretExpirySeconds),
                "Applied to new secret links across mobile flows."
            ),
            Triple(
                "CLOUD BACKUP",
                if (vaultPreferences.cloudBackupEnabled) "ENABLED" else "DISABLED",
                syncStatus.toBackupDescription(driveSession != null)
            ),
            Triple(
                "ACCOUNT DEVICES",
                accountDevices.size.toString(),
                "Devices currently registered against this vault identity."
            ),
            Triple(
                "DEFAULT PROTECTIONS",
                listOf(
                    vaultPreferences.defaultViewOnce,
                    vaultPreferences.defaultTimedView,
                    vaultPreferences.defaultNoSelect,
                    vaultPreferences.defaultTabSwitchLock,
                    vaultPreferences.defaultScreenshotGuard
                ).count { it }.toString(),
                "Default privacy rules automatically applied to new secrets."
            ),
            Triple(
                "VAULT DATA",
                "${syncStatus.deviceCount} DEVICES",
                "Export recovery material, export notes, or clear local encrypted data."
            )
        ).forEachIndexed { index, (title, value, description) ->
            RevealFromBottom(visible = visible, delayMs = 240L + index * 70L) {
                VaultSettingsSectionCard(
                    title = title,
                    value = value,
                    description = description,
                    expanded = expandedSection == title,
                    onToggle = {
                        expandedSection = if (expandedSection == title) "" else title
                    }
                ) {
                    when (title) {
                        "ACCOUNT" -> {
                            VaultSettingValueRow("DRIVE", driveSession?.toDriveStatusLine() ?: "NOT CONNECTED")
                            Spacer(Modifier.height(CxSpacing.sm))
                            VaultSettingValueRow("SYNC STATUS", syncStatus.toNotesStatusLine())
                            Spacer(Modifier.height(CxSpacing.md))
                            BodyText(
                                text = if (driveSession == null) {
                                    "Connect Google Drive in the Cloud tab to unlock same-account vault sync on Android."
                                } else {
                                    "This account is used for encrypted backup snapshots and account-device registration."
                                },
                                fontSize = CxTypography.textXs
                            )
                        }

                        "ENCRYPTION" -> {
                            VaultSettingValueRow("FINGERPRINT", syncStatus.keyFingerprint ?: "NOT READY")
                            Spacer(Modifier.height(CxSpacing.sm))
                            VaultSettingValueRow("SOURCE", encryptionSummary)
                            Spacer(Modifier.height(CxSpacing.md))
                            BrutalistButton(
                                text = if (vaultPreferences.sameAccountVaultEnabled) "USE LOCAL DEVICE KEY" else "USE SAME-ACCOUNT VAULT",
                                onClick = {
                                    if (!vaultPreferences.sameAccountVaultEnabled && driveSession == null) {
                                        Toast.makeText(context, "Connect Google Drive first to enable same-account vault.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        preferencesStore.setSameAccountVaultEnabled(!vaultPreferences.sameAccountVaultEnabled)
                                    }
                                },
                                variant = ButtonVariant.SECONDARY,
                                size = ButtonSize.MEDIUM,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(CxSpacing.sm))
                            BrutalistButton(
                                text = "EXPORT RECOVERY KEY",
                                onClick = {
                                    val keyJson = notesStore.exportRecoveryKey()
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_TEXT, keyJson)
                                        putExtra(Intent.EXTRA_SUBJECT, "Clex Vault Recovery Key")
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Export Recovery Key"))
                                },
                                variant = ButtonVariant.SECONDARY,
                                size = ButtonSize.MEDIUM,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (!vaultPreferences.sameAccountVaultEnabled) {
                                Spacer(Modifier.height(CxSpacing.sm))
                                BrutalistButton(
                                    text = "ROTATE LOCAL KEY",
                                    onClick = {
                                        val result = notesStore.rotateLocalKey()
                                        val message = result.fold(
                                            onSuccess = { "New key fingerprint: $it" },
                                            onFailure = { it.message ?: "Could not rotate the local key." }
                                        )
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    },
                                    variant = ButtonVariant.GHOST,
                                    size = ButtonSize.MEDIUM,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        "SYNC CODE" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CxTheme.colors.bgPrimary)
                                    .border(1.dp, CxTheme.colors.borderSubtle)
                                    .padding(CxSpacing.md)
                            ) {
                                MonoText(
                                    text = syncCode.ifBlank { "SYNC CODE IS NOT READY YET" },
                                    fontSize = CxTypography.textXs,
                                    color = CxTheme.colors.textPrimary,
                                    letterSpacing = CxTypography.textXs * 0.06f
                                )
                            }
                            Spacer(Modifier.height(CxSpacing.md))
                            BrutalistButton(
                                text = "COPY SYNC CODE",
                                onClick = {
                                    if (syncCode.isBlank()) {
                                        Toast.makeText(context, "Sync code is not ready yet.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        copyToClipboard(context, "Vault sync code", syncCode)
                                    }
                                },
                                variant = ButtonVariant.SECONDARY,
                                size = ButtonSize.MEDIUM,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        "DEFAULT EXPIRY" -> {
                            VaultSettingValueRow("CURRENT", formatSecretExpiryLabel(vaultPreferences.defaultSecretExpirySeconds))
                            Spacer(Modifier.height(CxSpacing.md))
                            BrutalistButton(
                                text = "CHANGE DEFAULT EXPIRY",
                                onClick = { showCustomExpiry = true },
                                variant = ButtonVariant.SECONDARY,
                                size = ButtonSize.MEDIUM,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        "CLOUD BACKUP" -> {
                            VaultSettingValueRow("BACKUP", if (vaultPreferences.cloudBackupEnabled) "ENABLED" else "DISABLED")
                            Spacer(Modifier.height(CxSpacing.sm))
                            VaultSettingValueRow("LAST SYNC", syncStatus.lastSyncedAt?.let(::formatSyncTimestamp) ?: "NOT YET")
                            Spacer(Modifier.height(CxSpacing.md))
                            BrutalistButton(
                                text = if (vaultPreferences.cloudBackupEnabled) "DISABLE CLOUD BACKUP" else "ENABLE CLOUD BACKUP",
                                onClick = {
                                    preferencesStore.setCloudBackupEnabled(!vaultPreferences.cloudBackupEnabled)
                                },
                                variant = ButtonVariant.SECONDARY,
                                size = ButtonSize.MEDIUM,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(CxSpacing.sm))
                            BrutalistButton(
                                text = "SYNC NOW",
                                onClick = { notesStore.syncNow() },
                                variant = ButtonVariant.PRIMARY,
                                size = ButtonSize.MEDIUM,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(CxSpacing.sm))
                            BrutalistButton(
                                text = "RESTORE CLOUD BACKUP",
                                onClick = { notesStore.restoreFromCloud() },
                                variant = ButtonVariant.SECONDARY,
                                size = ButtonSize.MEDIUM,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        "ACCOUNT DEVICES" -> {
                            if (accountDevices.isEmpty()) {
                                BodyText(
                                    text = "No signed-in devices are registered yet.",
                                    fontSize = CxTypography.textXs
                                )
                            } else {
                                accountDevices.forEachIndexed { deviceIndex, device ->
                                    VaultDeviceRow(device = device)
                                    if (deviceIndex < accountDevices.lastIndex) {
                                        Spacer(Modifier.height(CxSpacing.sm))
                                    }
                                }
                            }
                        }

                        "DEFAULT PROTECTIONS" -> {
                            VaultPreferenceToggleRow(
                                title = "VIEW ONCE",
                                subtitle = "New secret links self-destruct after the first successful reveal.",
                                enabled = vaultPreferences.defaultViewOnce,
                                onToggle = { updateDefaultProtections(viewOnce = !vaultPreferences.defaultViewOnce) }
                            )
                            Spacer(Modifier.height(CxSpacing.sm))
                            VaultPreferenceToggleRow(
                                title = "TIMED VIEW",
                                subtitle = "Limit the reveal screen to a 60-second viewing window by default.",
                                enabled = vaultPreferences.defaultTimedView,
                                onToggle = { updateDefaultProtections(timedView = !vaultPreferences.defaultTimedView) }
                            )
                            Spacer(Modifier.height(CxSpacing.sm))
                            VaultPreferenceToggleRow(
                                title = "NO SELECT",
                                subtitle = "Disable text selection on secret reveal screens where supported.",
                                enabled = vaultPreferences.defaultNoSelect,
                                onToggle = { updateDefaultProtections(noSelect = !vaultPreferences.defaultNoSelect) }
                            )
                            Spacer(Modifier.height(CxSpacing.sm))
                            VaultPreferenceToggleRow(
                                title = "TAB SWITCH LOCK",
                                subtitle = "Hide secret content when the app loses focus.",
                                enabled = vaultPreferences.defaultTabSwitchLock,
                                onToggle = { updateDefaultProtections(tabSwitchLock = !vaultPreferences.defaultTabSwitchLock) }
                            )
                            Spacer(Modifier.height(CxSpacing.sm))
                            VaultPreferenceToggleRow(
                                title = "SCREENSHOT GUARD",
                                subtitle = "Apply Android secure-window protection to new secret reveal sessions.",
                                enabled = vaultPreferences.defaultScreenshotGuard,
                                onToggle = { updateDefaultProtections(screenshotGuard = !vaultPreferences.defaultScreenshotGuard) }
                            )
                            Spacer(Modifier.height(CxSpacing.sm))
                            VaultPreferenceToggleRow(
                                title = "DEVTOOLS GUARD",
                                subtitle = "Keeps the matching protection flag enabled for parity with the web secret policy.",
                                enabled = vaultPreferences.defaultDevtoolsGuard,
                                onToggle = { updateDefaultProtections(devtoolsGuard = !vaultPreferences.defaultDevtoolsGuard) }
                            )
                        }

                        "VAULT DATA" -> {
                            BrutalistButton(
                                text = "EXPORT VAULT DATA",
                                onClick = {
                                    scope.launch {
                                        val exportJson = notesStore.exportNotesAsJson()
                                        if (exportJson == "[]") {
                                            Toast.makeText(context, "No notes to export", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_TEXT, exportJson)
                                            putExtra(Intent.EXTRA_SUBJECT, "Clex Vault Export")
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Export Vault"))
                                    }
                                },
                                variant = ButtonVariant.SECONDARY,
                                size = ButtonSize.MEDIUM,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(CxSpacing.sm))
                            BrutalistButton(
                                text = "CLEAR ALL VAULT DATA",
                                onClick = { showClearConfirm = true },
                                variant = ButtonVariant.GHOST,
                                size = ButtonSize.MEDIUM,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(CxSpacing.sm))
        }

        Spacer(Modifier.height(120.dp))
    }

    if (showCustomExpiry) {
        CustomExpiryDialog(
            currentSeconds = vaultPreferences.defaultSecretExpirySeconds,
            onConfirm = { seconds ->
                preferencesStore.setDefaultSecretExpirySeconds(seconds)
                showCustomExpiry = false
            },
            onDismiss = { showCustomExpiry = false }
        )
    }

    if (showClearConfirm) {
        ClearVaultConfirmDialog(
            onConfirm = {
                notesStore.clearAll {
                    Toast.makeText(context, "All vault data cleared", Toast.LENGTH_SHORT).show()
                }
                showClearConfirm = false
            },
            onDismiss = { showClearConfirm = false }
        )
    }
}

@Composable
private fun ClearVaultConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = CxTheme.colors
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(CxBorders.thick, colors.borderBold)
                .background(colors.bgPrimary)
                .padding(CxSpacing.cardPadding)
        ) {
            MonoText(
                text = "⚠ CLEAR ALL DATA",
                fontSize = CxTypography.textLg,
                fontWeight = CxTypography.weightBold,
                color = colors.error
            )
            Spacer(Modifier.height(CxSpacing.lg))
            BodyText(
                text = "This will permanently delete all local vault notes. Secret links and cloud uploads are stored server-side and won't be affected.",
                fontSize = CxTypography.textSm
            )
            Spacer(Modifier.height(CxSpacing.xl))
            BrutalistButton(
                text = "DELETE EVERYTHING",
                onClick = onConfirm,
                variant = ButtonVariant.PRIMARY,
                size = ButtonSize.MEDIUM,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(CxSpacing.sm))
            BrutalistButton(
                text = "CANCEL",
                onClick = onDismiss,
                variant = ButtonVariant.GHOST,
                size = ButtonSize.MEDIUM,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NoteActionsDialog(
    onOpen: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = CxTheme.colors
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CxColors.black.copy(alpha = 0.20f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(colors.bgCard)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(horizontal = CxSpacing.md, vertical = CxSpacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(42.dp)
                        .height(4.dp)
                        .background(colors.borderColor, RoundedCornerShape(999.dp))
                )
                Spacer(Modifier.height(CxSpacing.md))
                NoteActionButton(label = "OPEN", color = colors.textPrimary, onClick = onOpen)
                Spacer(Modifier.height(CxSpacing.xs))
                NoteActionButton(label = "COPY", color = colors.textPrimary, onClick = onCopy)
                Spacer(Modifier.height(CxSpacing.xs))
                NoteActionButton(label = "DELETE", color = CxColors.error, onClick = onDelete)
                Spacer(Modifier.height(CxSpacing.sm))
                NoteActionButton(label = "CANCEL", color = colors.textTertiary, onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun NoteActionButton(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    val colors = CxTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.bgPrimary)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = CxSpacing.md),
        contentAlignment = Alignment.Center
    ) {
        MonoText(
            text = label,
            fontSize = CxTypography.textSm,
            color = color
        )
    }
}

@Composable
private fun VaultSettingsSectionCard(
    title: String,
    value: String,
    description: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.bgCard)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(22.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle
                )
                .padding(CxSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MonoText(
                    text = title,
                    fontSize = CxTypography.textSm,
                    fontWeight = CxTypography.weightBold,
                    color = colors.textPrimary
                )
                MonoText(
                    text = value,
                    fontSize = CxTypography.textXs,
                    color = colors.accent
                )
                BodyText(
                    text = description,
                    fontSize = CxTypography.textXs
                )
            }
            Spacer(Modifier.width(CxSpacing.md))
            MonoText(
                text = if (expanded) "−" else "+",
                fontSize = CxTypography.textXl,
                color = colors.accent
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(180))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CxSpacing.md)
                    .padding(bottom = CxSpacing.md),
                verticalArrangement = Arrangement.spacedBy(CxSpacing.sm),
                content = content
            )
        }
    }
}

@Composable
private fun VaultSettingValueRow(
    label: String,
    value: String,
) {
    val colors = CxTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgPrimary)
            .border(1.dp, colors.borderSubtle)
            .padding(horizontal = CxSpacing.md, vertical = CxSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MonoText(
            text = label,
            fontSize = CxTypography.textXs,
            color = colors.textTertiary
        )
        Spacer(Modifier.width(CxSpacing.md))
        MonoText(
            text = value,
            fontSize = CxTypography.textXs,
            color = colors.textPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

data class SettingItem(val label: String, val value: String, val description: String)

@Composable
private fun SettingRow(item: SettingItem) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgCard)
            .border(1.dp, colors.borderSubtle)
            .padding(CxSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MonoText(
                text = item.label,
                fontSize = CxTypography.textSm,
                fontWeight = CxTypography.weightBold,
                color = colors.textPrimary
            )
            MonoText(
                text = item.value,
                fontSize = CxTypography.textXs,
                color = colors.accent
            )
        }
        Spacer(Modifier.height(CxSpacing.xs))
        BodyText(text = item.description, fontSize = CxTypography.textXs)
    }
}

@Composable
private fun VaultPreferenceToggleRow(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val colors = CxTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgCard)
            .border(1.dp, colors.borderSubtle)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .padding(CxSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MonoText(
                text = title,
                fontSize = CxTypography.textSm,
                fontWeight = CxTypography.weightBold,
                color = colors.textPrimary
            )
            BodyText(
                text = subtitle,
                fontSize = CxTypography.textXs
            )
        }
        Spacer(Modifier.width(CxSpacing.md))
        BrutalistBadge(
            text = if (enabled) "ON" else "OFF",
            accentColor = if (enabled) colors.accent else colors.textTertiary,
            filled = enabled
        )
    }
}

@Composable
private fun VaultDeviceRow(device: com.clex.android.data.VaultAccountDevice) {
    val colors = CxTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgCard)
            .border(1.dp, colors.borderSubtle)
            .padding(CxSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MonoText(
                text = device.name.ifBlank { "DEVICE" }.uppercase(),
                fontSize = CxTypography.textSm,
                fontWeight = CxTypography.weightBold,
                color = colors.textPrimary
            )
            MonoText(
                text = device.fingerprint.ifBlank { "UNKNOWN" },
                fontSize = CxTypography.textXs,
                color = colors.accent
            )
        }
        Spacer(Modifier.height(CxSpacing.xs))
        BodyText(
            text = "Last seen ${formatSyncTimestamp(device.lastSeen)}",
            fontSize = CxTypography.textXs
        )
    }
}
