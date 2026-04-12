package com.clex.android.ui.screens.workspace

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clex.android.data.AppLinkStore
import com.clex.android.data.tools.WorkspaceToolId
import com.clex.android.data.tools.WorkspaceToolResult
import com.clex.android.data.tools.WorkspaceToolRunner
import com.clex.android.data.transfer.TransferMethod
import com.clex.android.data.transfer.TransferState
import com.clex.android.data.transfer.TransferUiState
import com.clex.android.data.transfer.WorkspaceReceiverController
import com.clex.android.data.transfer.WorkspaceSelectedFile
import com.clex.android.data.transfer.WorkspaceSenderController
import com.clex.android.data.transfer.buildReceiveLink
import com.clex.android.ui.anim.*
import com.clex.android.ui.components.*
import com.clex.android.ui.effects.MeshGradientBackground
import com.clex.android.ui.effects.ParticleField
import com.clex.android.ui.theme.*
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════
//  CLEX — Workspace Screen
//  Core app screen. Tabs: Send | Receive | Tools
//  States: Empty → Files dropped → Tools → Sharing → Success
// ═══════════════════════════════════════════════════

enum class WorkspaceTab { SEND, RECEIVE, TOOLS }

// Simulated workspace state (frontend-only, no backend)
enum class WorkspaceState { EMPTY, FILES_READY, PROCESSING, SHARING, CONNECTED, TRANSFERRING, SUCCESS, ERROR, OFFLINE }

@Composable
fun WorkspaceScreen() {
    val colors = CxTheme.colors
    var currentTab by remember { mutableStateOf(WorkspaceTab.SEND) }
    val tabVisible = rememberEntryVisibility(currentTab)
    val context = LocalContext.current
    val senderController = remember(context) {
        WorkspaceSenderController(context.applicationContext)
    }
    val receiverController = remember(context) {
        WorkspaceReceiverController(context.applicationContext)
    }
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            senderController.addFiles(uris)
        }
    }

    DisposableEffect(senderController, receiverController) {
        onDispose {
            senderController.dispose()
            receiverController.dispose()
        }
    }

    LaunchedEffect(receiverController) {
        val pendingReceive = AppLinkStore.consumeReceiveLink() ?: return@LaunchedEffect
        currentTab = WorkspaceTab.RECEIVE
        receiverController.setMethod(pendingReceive.method)
        receiverController.connect(pendingReceive.roomCode)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
    ) {
        MeshGradientBackground(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.18f),
            accentStrength = 0.1f
        )
        ParticleField(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.12f),
            particleCount = 24,
            connectDistance = 105f
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ──
            WorkspaceTopBar()

            // ── Tab Selector ──
            TabSelector(currentTab) { currentTab = it }

            // ── Content ──
            Crossfade(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                targetState = currentTab,
                animationSpec = tween(220),
                label = "tabContent"
            ) { tab ->
                RevealFromBottom(visible = tabVisible) {
                    when (tab) {
                        WorkspaceTab.SEND -> LiveSendTab(
                            controller = senderController,
                            onPickFiles = { filePicker.launch(arrayOf("*/*")) }
                        )
                        WorkspaceTab.RECEIVE -> LiveReceiveTab(
                            controller = receiverController
                        )
                        WorkspaceTab.TOOLS -> ToolsTab()
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkspaceTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(CxTheme.colors.bgPrimary)
            .padding(horizontal = CxSpacing.screenHorizontal, vertical = CxSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        PageMark(glyph = "⇄", title = "WORKSPACE")
    }
}

@Composable
private fun TabSelector(
    currentTab: WorkspaceTab,
    onSelect: (WorkspaceTab) -> Unit
) {
    val colors = CxTheme.colors
    val tabs = WorkspaceTab.entries
    val tabCount = tabs.size
    val selectedIndex = tabs.indexOf(currentTab).coerceAtLeast(0)

    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = CxSpringSpecs.panel(),
        label = "tabPill"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgPrimary)
            .drawBehind {
                // Bottom border line
                drawRect(
                    color = colors.borderColor,
                    topLeft = Offset(0f, size.height - 2.dp.toPx()),
                    size = Size(size.width, 2.dp.toPx())
                )
                // Sliding accent pill
                val itemWidth = size.width / tabCount
                val pillWidth = 32.dp.toPx()
                val pillHeight = 3.dp.toPx()
                val x = animatedIndex * itemWidth + (itemWidth - pillWidth) / 2f
                val y = size.height - pillHeight
                drawRect(
                    color = colors.accent,
                    topLeft = Offset(x, y),
                    size = Size(pillWidth, pillHeight)
                )
            }
    ) {
        tabs.forEach { tab ->
            val isActive = tab == currentTab

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(tab) }
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MonoText(
                    text = tab.name,
                    fontSize = CxTypography.textSm,
                    fontWeight = if (isActive) CxTypography.weightBlack else CxTypography.weightMedium,
                    color = if (isActive) colors.accent else colors.textTertiary,
                    letterSpacing = CxTypography.textXs * 0.12
                )
                // Space so pill has room at bottom
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── SEND TAB ─────────────────────────────────────

@Composable
private fun SendTab(
    state: WorkspaceState,
    onStateChange: (WorkspaceState) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(top = CxSpacing.xl)
    ) {
        when (state) {
            WorkspaceState.EMPTY -> DropZone(onDrop = { onStateChange(WorkspaceState.FILES_READY) })
            WorkspaceState.FILES_READY -> FilesReadyState(
                onPrepare = { onStateChange(WorkspaceState.PROCESSING) },
                onShare = { onStateChange(WorkspaceState.SHARING) }
            )
            WorkspaceState.PROCESSING -> ProcessingState(
                onComplete = { onStateChange(WorkspaceState.FILES_READY) }
            )
            WorkspaceState.SHARING -> SharingState(
                onConnected = { onStateChange(WorkspaceState.CONNECTED) }
            )
            WorkspaceState.CONNECTED -> ConnectedState(
                onTransfer = { onStateChange(WorkspaceState.TRANSFERRING) }
            )
            WorkspaceState.TRANSFERRING -> TransferringState(
                onComplete = { onStateChange(WorkspaceState.SUCCESS) }
            )
            WorkspaceState.SUCCESS -> SuccessState(
                onReset = { onStateChange(WorkspaceState.EMPTY) }
            )
            WorkspaceState.ERROR -> ErrorState(
                onRetry = { onStateChange(WorkspaceState.SHARING) },
                onReset = { onStateChange(WorkspaceState.EMPTY) }
            )
            WorkspaceState.OFFLINE -> OfflineState()
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun LiveSendTab(
    controller: WorkspaceSenderController,
    onPickFiles: () -> Unit,
) {
    val context = LocalContext.current
    val files by controller.files.collectAsState()
    val transferState by controller.transferState.collectAsState()
    val scrollState = rememberScrollState()
    val receiveLink = remember(transferState.roomCode, transferState.method) {
        buildReceiveLink(transferState.roomCode, transferState.method)
    }
    var secondsRemaining by remember(transferState.shareExpiresAtMillis, transferState.state) {
        mutableIntStateOf(transferState.shareExpiresAtMillis.remainingShareSeconds())
    }

    LaunchedEffect(transferState.shareExpiresAtMillis, transferState.state, transferState.roomCode) {
        val expiresAt = transferState.shareExpiresAtMillis
        val waitingForPeer =
            transferState.state == TransferState.PREPARING || transferState.state == TransferState.WAITING_PEER

        if (!waitingForPeer || expiresAt == null) {
            secondsRemaining = 0
            return@LaunchedEffect
        }

        while (true) {
            val remaining = expiresAt.remainingShareSeconds()
            secondsRemaining = remaining
            if (remaining <= 0) {
                controller.expirePendingTransfer()
                break
            }
            delay(250)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(top = CxSpacing.xl)
    ) {
        when {
            files.isEmpty() -> {
                SectionLabel(text = if (transferState.method == TransferMethod.LOCAL) "Local Transfer" else "Direct Transfer")
                Spacer(Modifier.height(CxSpacing.lg))
                TransferRouteSelector(
                    selectedMethod = transferState.method,
                    onSelect = controller::setMethod
                )
                Spacer(Modifier.height(CxSpacing.lg))
                DropZone(onDrop = onPickFiles)
            }

            transferState.state == TransferState.IDLE -> {
                SectionLabel(text = "Files Loaded")
                Spacer(Modifier.height(CxSpacing.lg))
                TransferRouteSelector(
                    selectedMethod = transferState.method,
                    onSelect = controller::setMethod
                )
                Spacer(Modifier.height(CxSpacing.lg))
                files.forEach { file ->
                    RemovableFileRow(
                        file = file,
                        onRemove = { controller.removeFile(file.id) }
                    )
                    Spacer(Modifier.height(CxSpacing.sm))
                }

                Spacer(Modifier.height(CxSpacing.lg))
                TransferStatsRow(
                    firstValue = files.size.toString(),
                    firstLabel = "FILES",
                    secondValue = formatBytes(files.sumOf { it.size }),
                    secondLabel = "TOTAL",
                    thirdValue = transferState.roomCode,
                    thirdLabel = "CODE"
                )

                Spacer(Modifier.height(CxSpacing.xl))
                WorkspaceInfoPanel(
                    title = "Receiver Access",
                    body = receiveLink
                )

                Spacer(Modifier.height(CxSpacing.xl))
                BrutalistButton(
                    text = "START DIRECT TRANSFER →",
                    onClick = { controller.startTransfer() },
                    variant = ButtonVariant.PRIMARY,
                    size = ButtonSize.LARGE,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(CxSpacing.md))
                BrutalistButton(
                    text = "ADD MORE FILES",
                    onClick = onPickFiles,
                    variant = ButtonVariant.SECONDARY,
                    size = ButtonSize.MEDIUM,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(CxSpacing.sm))
                BrutalistButton(
                    text = "CLEAR FILES",
                    onClick = { controller.clearFiles() },
                    variant = ButtonVariant.GHOST,
                    size = ButtonSize.MEDIUM,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            transferState.state == TransferState.PREPARING || transferState.state == TransferState.WAITING_PEER -> {
                WaitingForReceiverPanel(
                    roomCode = transferState.roomCode,
                    receiveLink = receiveLink,
                    method = transferState.method,
                    secondsRemaining = secondsRemaining,
                    onCopyCode = { copyToClipboard(context, "Clex Code", transferState.roomCode) },
                    onCopyLink = { copyToClipboard(context, "Clex Receive Link", receiveLink) },
                    onShare = { shareText(context, receiveLink) },
                    onCancel = { controller.resetTransfer() }
                )
            }

            transferState.state == TransferState.CONNECTING -> {
                ConnectingPanel(
                    title = "ESTABLISHING CONNECTION",
                    subtitle = "Finding the best route between devices now. Keep both devices awake and on the same network for Local mode."
                )
            }

            transferState.state == TransferState.TRANSFERRING -> {
                ActiveTransferPanel(
                    state = transferState,
                    headline = "SENDING FILES",
                    tailLabel = if (transferState.nearby) "LOCAL" else "DIRECT"
                )
            }

            transferState.state == TransferState.COMPLETE -> {
                WorkspaceCompletePanel(
                    headline = "TRANSFER COMPLETE",
                    summary = "${files.size} file${if (files.size == 1) "" else "s"} delivered",
                    details = listOf(
                        "TOTAL" to formatBytes(files.sumOf { it.size }),
                        "ROUTE" to if (transferState.nearby) "LOCAL LAN" else "DIRECT P2P",
                        "CODE" to transferState.roomCode,
                    ),
                    primaryLabel = "NEW TRANSFER",
                    onPrimary = { controller.resetTransfer() },
                    secondaryLabel = "ADD MORE FILES",
                    onSecondary = onPickFiles
                )
            }

            transferState.state == TransferState.FAILED -> {
                WorkspaceErrorPanel(
                    message = transferState.error ?: "Transfer failed.",
                    onRetry = { controller.startTransfer() },
                    onReset = { controller.resetTransfer() }
                )
            }
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun LiveReceiveTab(
    controller: WorkspaceReceiverController,
) {
    val transferState by controller.transferState.collectAsState()
    val saveMessage by controller.saveMessage.collectAsState()
    var roomCodeInput by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val qrScanLauncher = rememberLauncherForActivityResult(
        contract = com.journeyapps.barcodescanner.ScanContract()
    ) { result ->
        val scannedContent = result.contents ?: return@rememberLauncherForActivityResult
        val code = extractRoomCodeFromScan(scannedContent)
        if (code != null) {
            roomCodeInput = code
            controller.connect(code)
        } else {
            roomCodeInput = normalizeRoomCodeInput(scannedContent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(top = CxSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (transferState.state) {
            TransferState.IDLE,
            TransferState.FAILED -> {
                SectionLabel(text = "Receive Files")
                Spacer(Modifier.height(CxSpacing.xl))
                TransferRouteSelector(
                    selectedMethod = transferState.method,
                    onSelect = controller::setMethod
                )
                Spacer(Modifier.height(CxSpacing.lg))
                CodeInputCard(
                    value = roomCodeInput,
                    onValueChange = { roomCodeInput = normalizeRoomCodeInput(it) }
                )
                Spacer(Modifier.height(CxSpacing.lg))
                BrutalistButton(
                    text = "CONNECT →",
                    onClick = { controller.connect(roomCodeInput) },
                    variant = ButtonVariant.PRIMARY,
                    size = ButtonSize.LARGE,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(CxSpacing.md))
                BrutalistButton(
                    text = "⊞  SCAN QR CODE",
                    onClick = {
                        val options = com.journeyapps.barcodescanner.ScanOptions().apply {
                            setDesiredBarcodeFormats(com.journeyapps.barcodescanner.ScanOptions.QR_CODE)
                            setPrompt("Scan the sender's QR code")
                            setBeepEnabled(false)
                            setBarcodeImageEnabled(false)
                            setOrientationLocked(false)
                            setCaptureActivity(com.clex.android.ui.scan.PortraitQrCaptureActivity::class.java)
                        }
                        qrScanLauncher.launch(options)
                    },
                    variant = ButtonVariant.SECONDARY,
                    size = ButtonSize.MEDIUM,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(CxSpacing.md))
                BodyText(
                    text = "Enter the 6-character code, scan the sender's QR code, or open the receive link on this device.",
                    textAlign = TextAlign.Center
                )
                if (transferState.error != null) {
                    Spacer(Modifier.height(CxSpacing.lg))
                    WorkspaceErrorPanel(
                        message = transferState.error.orEmpty(),
                        onRetry = { controller.connect(roomCodeInput) },
                        onReset = {
                            roomCodeInput = ""
                            controller.reset()
                        }
                    )
                }
            }

            TransferState.PREPARING,
            TransferState.WAITING_PEER -> {
                SectionLabel(text = "Receive Files")
                Spacer(Modifier.height(CxSpacing.xl))
                WaitingForSenderPanel(
                    roomCode = transferState.roomCode,
                    method = transferState.method
                ) {
                    roomCodeInput = ""
                    controller.reset()
                }
            }

            TransferState.CONNECTING -> {
                ConnectingPanel(
                    title = "ESTABLISHING CONNECTION",
                    subtitle = "Finding the best route between devices now. Keep both devices awake and on the same network for Local mode."
                )
            }

            TransferState.TRANSFERRING -> {
                ActiveTransferPanel(
                    state = transferState,
                    headline = "RECEIVING FILES",
                    tailLabel = if (transferState.nearby) "LOCAL" else "DIRECT"
                )
            }

            TransferState.COMPLETE -> {
                SectionLabel(text = "Received")
                Spacer(Modifier.height(CxSpacing.lg))
                WorkspaceCompletePanel(
                    headline = "FILES RECEIVED",
                    summary = saveMessage ?: "Received ${transferState.receivedFiles.size} file${if (transferState.receivedFiles.size == 1) "" else "s"}.",
                    details = listOf(
                        "FILES" to transferState.receivedFiles.size.toString(),
                        "TOTAL" to formatBytes(transferState.receivedFiles.sumOf { it.size }),
                        "ROUTE" to if (transferState.nearby) "LOCAL LAN" else "DIRECT P2P",
                    ),
                    primaryLabel = "RECEIVE ANOTHER",
                    onPrimary = {
                        roomCodeInput = ""
                        controller.reset()
                    },
                    secondaryLabel = "SAVE AGAIN",
                    onSecondary = { controller.saveAgain() }
                )

                Spacer(Modifier.height(CxSpacing.xl))
                transferState.receivedFiles.forEach { file ->
                    FileRow(name = file.name, size = formatBytes(file.size))
                    Spacer(Modifier.height(CxSpacing.sm))
                }
            }
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun RemovableFileRow(
    file: WorkspaceSelectedFile,
    onRemove: () -> Unit,
) {
    val colors = CxTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(CxBorders.thin, colors.borderColor)
            .background(colors.bgCard)
            .padding(horizontal = CxSpacing.md, vertical = CxSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CxSpacing.md)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            MonoText(
                text = file.name,
                fontSize = CxTypography.textSm,
                color = colors.textPrimary,
                maxLines = 1
            )
            Spacer(Modifier.height(CxSpacing.xs))
            MonoText(
                text = formatBytes(file.size),
                fontSize = CxTypography.textXs,
                color = colors.textTertiary
            )
        }
        MonoText(
            text = "REMOVE",
            fontSize = CxTypography.textXs,
            color = CxColors.error,
            modifier = Modifier.clickable { onRemove() }
        )
    }
}

@Composable
private fun TransferStatsRow(
    firstValue: String,
    firstLabel: String,
    secondValue: String,
    secondLabel: String,
    thirdValue: String,
    thirdLabel: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(firstValue, firstLabel)
        StatItem(secondValue, secondLabel)
        StatItem(thirdValue, thirdLabel)
    }
}

@Composable
private fun WorkspaceInfoPanel(
    title: String,
    body: String,
) {
    val colors = CxTheme.colors
    MicroAppPanel(title = title) {
        SelectionContainer {
            MonoText(
                text = body,
                fontSize = CxTypography.textXs,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun TransferRouteSelector(
    selectedMethod: TransferMethod,
    onSelect: (TransferMethod) -> Unit,
) {
    val colors = CxTheme.colors
    var showInfoDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(colors.bgSecondary)
            ) {
                listOf(
                    TransferMethod.WEBRTC to "DIRECT",
                    TransferMethod.LOCAL to "LOCAL",
                ).forEach { (method, label) ->
                    val isActive = method == selectedMethod
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelect(method) }
                            .background(
                                if (isActive) {
                                    colors.accent.copy(alpha = if (colors.isDark) 0.18f else 0.16f)
                                }
                                else colors.bgCard
                            )
                            .border(
                                width = CxBorders.medium,
                                color = if (isActive) colors.accent.copy(alpha = 0.82f) else colors.borderColor
                            )
                            .padding(vertical = CxSpacing.md),
                        contentAlignment = Alignment.Center
                    ) {
                        MonoText(
                            text = label,
                            fontSize = CxTypography.textXs,
                            fontWeight = CxTypography.weightBold,
                            color = if (isActive) {
                                colors.accent
                            } else colors.textTertiary,
                            letterSpacing = CxTypography.textXs * 0.12
                        )
                    }
                }
            }
            Spacer(Modifier.width(CxSpacing.sm))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(CxBorders.thin, colors.borderSubtle)
                    .background(colors.bgCard)
                    .clickable { showInfoDialog = true },
                contentAlignment = Alignment.Center
            ) {
                MonoText(
                    text = "i",
                    fontSize = CxTypography.textBase,
                    fontWeight = CxTypography.weightBold,
                    color = colors.accent
                )
            }
        }
    }

    if (showInfoDialog) {
        TransferModeInfoDialog(onDismiss = { showInfoDialog = false })
    }
}

@Composable
private fun TransferModeInfoDialog(onDismiss: () -> Unit) {
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
                text = "TRANSFER MODES",
                fontSize = CxTypography.textLg,
                fontWeight = CxTypography.weightBold,
                color = colors.accent
            )
            Spacer(Modifier.height(CxSpacing.lg))

            MonoText(
                text = "⚡ DIRECT",
                fontSize = CxTypography.textSm,
                fontWeight = CxTypography.weightBold,
                color = colors.textPrimary
            )
            Spacer(Modifier.height(CxSpacing.xs))
            BodyText(
                text = "Peer-to-peer transfer over the internet. Works anywhere — files are sent directly between devices using an encrypted WebRTC connection. No file size limits. No cloud storage.",
                fontSize = CxTypography.textSm
            )

            Spacer(Modifier.height(CxSpacing.lg))

            MonoText(
                text = "\uD83C\uDFE0 LOCAL",
                fontSize = CxTypography.textSm,
                fontWeight = CxTypography.weightBold,
                color = colors.textPrimary
            )
            Spacer(Modifier.height(CxSpacing.xs))
            BodyText(
                text = "Transfer over your local network (same Wi-Fi / office / home). Fastest possible speed. Data never leaves your network. Both devices must be on the same network.",
                fontSize = CxTypography.textSm
            )

            Spacer(Modifier.height(CxSpacing.xl))

            BrutalistButton(
                text = "GOT IT",
                onClick = onDismiss,
                variant = ButtonVariant.PRIMARY,
                size = ButtonSize.MEDIUM,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WaitingForReceiverPanel(
    roomCode: String,
    receiveLink: String,
    method: TransferMethod,
    secondsRemaining: Int,
    onCopyCode: () -> Unit,
    onCopyLink: () -> Unit,
    onShare: () -> Unit,
    onCancel: () -> Unit,
) {
    val colors = CxTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(CxSpacing.lg))
        SectionLabel(text = "Share Code")
        Spacer(Modifier.height(CxSpacing.md))
        CodeBoxesRow(
            code = roomCode,
            accentColor = colors.accent
        )
        Spacer(Modifier.height(CxSpacing.sm))
        BodyText(
            text = if (method == TransferMethod.LOCAL) {
                "Local mode expects both devices on the same Wi-Fi. Receiver can still open the link or enter the code here."
            } else {
                "Receiver can open the browser link or type this code inside the Android app."
            },
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(CxSpacing.sm))
        MonoText(
            text = "LINK EXPIRES IN ${secondsRemaining.coerceAtLeast(0).toMinuteSecondLabel()}",
            fontSize = CxTypography.textXs,
            color = colors.textTertiary,
            letterSpacing = CxTypography.textXs * 0.12,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(CxSpacing.lg))
        WorkspaceInfoPanel(
            title = "Receive Link",
            body = receiveLink
        )
        Spacer(Modifier.height(CxSpacing.lg))
        // QR code so receiver can scan link directly
        if (receiveLink.isNotBlank()) {
            FramedQRCode(
                content = receiveLink,
                label = "SCAN TO RECEIVE",
                size = 180.dp
            )
            Spacer(Modifier.height(CxSpacing.lg))
        }
        BrutalistButton(
            text = "COPY CODE",
            onClick = onCopyCode,
            variant = ButtonVariant.PRIMARY,
            size = ButtonSize.LARGE,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(CxSpacing.md))
        BrutalistButton(
            text = "COPY RECEIVE LINK",
            onClick = onCopyLink,
            variant = ButtonVariant.SECONDARY,
            size = ButtonSize.MEDIUM,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(CxSpacing.sm))
        BrutalistButton(
            text = "SHARE LINK",
            onClick = onShare,
            variant = ButtonVariant.SECONDARY,
            size = ButtonSize.MEDIUM,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(CxSpacing.sm))
        BrutalistButton(
            text = "CANCEL",
            onClick = onCancel,
            variant = ButtonVariant.GHOST,
            size = ButtonSize.MEDIUM,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun WaitingForSenderPanel(
    roomCode: String,
    method: TransferMethod,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CodeBoxesRow(
            code = roomCode,
            accentColor = CxTheme.colors.accent
        )
        Spacer(Modifier.height(CxSpacing.lg))
        MicroAppPanel(title = "Receiver Ready") {
            BodyText(
                text = if (method == TransferMethod.LOCAL) {
                    "Stay on this screen and keep both devices on the same Wi-Fi. The sender can begin once they start the local route."
                } else {
                    "Stay on this screen. The sender can start as soon as the room code is entered on their side."
                },
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(CxSpacing.xl))
        BrutalistButton(
            text = "CANCEL",
            onClick = onCancel,
            variant = ButtonVariant.GHOST,
            size = ButtonSize.MEDIUM,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ConnectingPanel(
    title: String,
    subtitle: String,
) {
    MicroAppPanel(title = "Connection") {
        MonoText(
            text = title,
            fontSize = CxTypography.textBase,
            fontWeight = CxTypography.weightBold,
            color = CxTheme.colors.accent
        )
        Spacer(Modifier.height(CxSpacing.md))
        BodyText(text = subtitle)
        Spacer(Modifier.height(CxSpacing.lg))
        BrutalistProgressBar(progress = 0.55f, segments = 12)
    }
}

@Composable
private fun ActiveTransferPanel(
    state: TransferUiState,
    headline: String,
    tailLabel: String,
) {
    val colors = CxTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(text = headline)
        Spacer(Modifier.height(CxSpacing.lg))
        MicroAppPanel(title = tailLabel) {
            BrutalistProgressBar(progress = state.progress / 100f, segments = 20)
            Spacer(Modifier.height(CxSpacing.lg))
            if (state.currentFile != null) {
                MonoText(
                    text = state.currentFile.name,
                    fontSize = CxTypography.textSm,
                    color = colors.textPrimary,
                    maxLines = 1
                )
                Spacer(Modifier.height(CxSpacing.sm))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonoText(text = "PROGRESS", fontSize = CxTypography.textXs, color = colors.textTertiary)
                MonoText(text = "${state.progress}%", fontSize = CxTypography.textSm, color = colors.accent)
            }
            Spacer(Modifier.height(CxSpacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonoText(text = "TRANSFERRED", fontSize = CxTypography.textXs, color = colors.textTertiary)
                MonoText(
                    text = "${formatBytes(state.bytesSent)} / ${formatBytes(state.bytesTotal)}",
                    fontSize = CxTypography.textSm,
                    color = colors.textSecondary
                )
            }
            Spacer(Modifier.height(CxSpacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonoText(text = "ROUTE", fontSize = CxTypography.textXs, color = colors.textTertiary)
                BrutalistBadge(
                    text = if (state.nearby) "LOCAL LAN" else "P2P DIRECT",
                    accentColor = if (state.nearby) CxColors.success else colors.accent,
                    filled = true
                )
            }
        }
    }
}

@Composable
private fun WorkspaceCompletePanel(
    headline: String,
    summary: String,
    details: List<Pair<String, String>>,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit,
) {
    val colors = CxTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedCheckmark()
        Spacer(Modifier.height(CxSpacing.lg))
        SectionTitle(text = headline, color = CxColors.success)
        Spacer(Modifier.height(CxSpacing.md))
        BodyText(text = summary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(CxSpacing.xl))
        details.forEachIndexed { index, (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonoText(text = label, fontSize = CxTypography.textXs, color = colors.textTertiary)
                MonoText(text = value, fontSize = CxTypography.textSm, color = colors.textPrimary)
            }
            if (index < details.lastIndex) {
                Spacer(Modifier.height(CxSpacing.xs))
            }
        }
        Spacer(Modifier.height(CxSpacing.xl))
        BrutalistButton(
            text = primaryLabel,
            onClick = onPrimary,
            variant = ButtonVariant.PRIMARY,
            size = ButtonSize.LARGE,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(CxSpacing.md))
        BrutalistButton(
            text = secondaryLabel,
            onClick = onSecondary,
            variant = ButtonVariant.SECONDARY,
            size = ButtonSize.MEDIUM,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun WorkspaceErrorPanel(
    message: String,
    onRetry: () -> Unit,
    onReset: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MonoText(
            text = "✕",
            fontSize = CxTypography.text6xl,
            color = CxColors.error
        )
        Spacer(Modifier.height(CxSpacing.md))
        BodyText(
            text = message,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(CxSpacing.lg))
        BrutalistButton(
            text = "RETRY",
            onClick = onRetry,
            variant = ButtonVariant.PRIMARY,
            size = ButtonSize.MEDIUM,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(CxSpacing.sm))
        BrutalistButton(
            text = "RESET",
            onClick = onReset,
            variant = ButtonVariant.GHOST,
            size = ButtonSize.MEDIUM,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CodeInputCard(
    value: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        cursorBrush = SolidColor(Color.Transparent),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Done
        ),
        textStyle = TextStyle(
            color = Color.Transparent,
            fontSize = 1.sp
        ),
        decorationBox = {
            CodeBoxesRow(
                code = value,
                accentColor = CxTheme.colors.accent,
                editableIndex = value.length.coerceAtMost(5)
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CodeBoxesRow(
    code: String,
    accentColor: Color,
    editableIndex: Int? = null,
    boxCount: Int = 6,
) {
    val colors = CxTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm)
    ) {
        repeat(boxCount) { index ->
            val char = code.getOrNull(index)?.toString().orEmpty()
            val isActive = editableIndex == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp)
                    .background(colors.bgInput)
                    .border(
                        CxBorders.medium,
                        when {
                            char.isNotBlank() -> accentColor.copy(alpha = 0.82f)
                            isActive -> colors.borderBold
                            else -> colors.borderColor
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                MonoText(
                    text = char.ifBlank { "·" },
                    fontSize = CxTypography.text2xl,
                    fontWeight = CxTypography.weightBold,
                    color = when {
                        char.isNotBlank() -> colors.textPrimary
                        isActive -> accentColor.copy(alpha = 0.55f)
                        else -> colors.textTertiary.copy(alpha = 0.45f)
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun normalizeRoomCodeInput(raw: String): String =
    raw.uppercase().filter { it.isLetterOrDigit() }.take(6)

private fun Long?.remainingShareSeconds(): Int {
    val deadline = this ?: return 0
    val diff = (deadline - System.currentTimeMillis()).coerceAtLeast(0L)
    return ((diff + 999L) / 1000L).toInt()
}

private fun Int.toMinuteSecondLabel(): String {
    val total = coerceAtLeast(0)
    val minutes = total / 60
    val seconds = total % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun extractRoomCodeFromScan(scanned: String): String? {
    // Try to extract code from a clex receive URL
    val uri = runCatching { android.net.Uri.parse(scanned) }.getOrNull()
    if (uri != null && uri.host == "clex.in") {
        val code = uri.getQueryParameter("code")?.trim()?.uppercase()
        if (code != null && com.clex.android.data.transfer.isValidRoomCode(code)) return code
    }
    // Try as a raw room code
    val raw = scanned.trim().uppercase()
    if (com.clex.android.data.transfer.isValidRoomCode(raw)) return raw
    return null
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = 0
    while (value >= 1024.0 && index < units.lastIndex) {
        value /= 1024.0
        index++
    }
    val decimals = if (value >= 100 || index == 0) 0 else 1
    return "%.${decimals}f %s".format(value, units[index])
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Clex receive link"))
}

// ── DROP ZONE (Empty State) ──────────────────────

@Composable
private fun DropZone(onDrop: () -> Unit) {
    val colors = CxTheme.colors

    val infiniteTransition = rememberInfiniteTransition(label = "dropFloat")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dropY"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dashed border drop area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .drawBehind {
                    drawRoundRect(
                        color = colors.borderColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(12.dp.toPx(), 8.dp.toPx()),
                                0f
                            )
                        )
                    )
                }
                .background(colors.bgSecondary)
                .clickable { onDrop() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = floatY.dp)
            ) {
                MonoText(
                    text = "↓",
                    fontSize = CxTypography.text5xl,
                    color = colors.textTertiary
                )
                Spacer(Modifier.height(CxSpacing.md))
                MonoText(
                    text = "DROP FILES HERE",
                    fontSize = CxTypography.textSm,
                    color = colors.textTertiary,
                    letterSpacing = CxTypography.textXs * 0.2
                )
                Spacer(Modifier.height(CxSpacing.xs))
                MonoText(
                    text = "OR TAP TO BROWSE",
                    fontSize = CxTypography.textXs,
                    color = colors.textTertiary,
                    letterSpacing = CxTypography.textXs * 0.15
                )
            }
        }

        Spacer(Modifier.height(CxSpacing.xl))

        // Quick stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("0", "FILES")
            StatItem("—", "SIZE")
            StatItem("IDLE", "STATUS")
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        MonoText(
            text = value,
            fontSize = CxTypography.text2xl,
            fontWeight = CxTypography.weightBold,
            color = CxTheme.colors.textPrimary
        )
        MonoText(
            text = label,
            fontSize = CxTypography.textXs,
            color = CxTheme.colors.textTertiary,
            letterSpacing = CxTypography.textXs * 0.15
        )
    }
}

// ── FILES READY ──────────────────────────────────

@Composable
private fun FilesReadyState(onPrepare: () -> Unit, onShare: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(text = "Files Loaded")
        Spacer(Modifier.height(CxSpacing.lg))

        // Simulated file list
        val files = listOf(
            "document.pdf" to "2.4 MB",
            "photo_001.jpg" to "3.8 MB",
            "report.docx" to "856 KB"
        )

        files.forEach { (name, size) ->
            FileRow(name = name, size = size)
            Spacer(Modifier.height(CxSpacing.sm))
        }

        Spacer(Modifier.height(CxSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("3", "FILES")
            StatItem("7.1 MB", "TOTAL")
            StatItem("READY", "STATUS")
        }

        Spacer(Modifier.height(CxSpacing.xl))

        // Actions
        BrutalistButton(
            text = "SHARE →",
            onClick = onShare,
            variant = ButtonVariant.PRIMARY,
            size = ButtonSize.LARGE,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(CxSpacing.md))
        BrutalistButton(
            text = "PREPARE WITH TOOLS",
            onClick = onPrepare,
            variant = ButtonVariant.SECONDARY,
            size = ButtonSize.MEDIUM,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FileRow(name: String, size: String) {
    val colors = CxTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(CxBorders.thin, colors.borderColor)
            .background(colors.bgCard)
            .padding(horizontal = CxSpacing.md, vertical = CxSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MonoText(
            text = name,
            fontSize = CxTypography.textSm,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        MonoText(
            text = size,
            fontSize = CxTypography.textXs,
            color = colors.textTertiary
        )
    }
}

// ── PROCESSING ───────────────────────────────────

@Composable
private fun ProcessingState(onComplete: () -> Unit) {
    val colors = CxTheme.colors
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(50)
            progress = (progress + 0.02f).coerceAtMost(1f)
        }
        delay(300)
        onComplete()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        MicroAppPanel(title = "Image Compression") {
            MonoText(
                text = "COMPRESSING 3 FILES...",
                fontSize = CxTypography.textSm,
                color = colors.accent
            )
            Spacer(Modifier.height(CxSpacing.lg))
            BrutalistProgressBar(progress = progress)
            Spacer(Modifier.height(CxSpacing.md))
            MonoText(
                text = "photo_001.jpg → 890 KB (−76%)",
                fontSize = CxTypography.textXs,
                color = colors.textTertiary
            )
        }
    }
}

// ── SHARING ──────────────────────────────────────

@Composable
private fun SharingState(onConnected: () -> Unit) {
    val colors = CxTheme.colors

    LaunchedEffect(Unit) {
        delay(3000)
        onConnected()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(CxSpacing.md))

        MonoText(
            text = "SHARE CODE: AX7-KR2-9PF",
            fontSize = CxTypography.textBase,
            fontWeight = CxTypography.weightBold,
            color = colors.accent,
            letterSpacing = CxTypography.textSm * 0.1,
            textAlign = TextAlign.Center
        )
    }
}

// ── CONNECTED ────────────────────────────────────

@Composable
private fun ConnectedState(onTransfer: () -> Unit) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MicroAppPanel(title = "Connection Established") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonoText(text = "ROUTE:", fontSize = CxTypography.textXs, color = colors.textTertiary)
                BrutalistBadge(text = "P2P DIRECT", accentColor = CxColors.success, filled = true)
            }
            Spacer(Modifier.height(CxSpacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonoText(text = "PEER:", fontSize = CxTypography.textXs, color = colors.textTertiary)
                MonoText(text = "CONNECTED ✓", fontSize = CxTypography.textSm, color = CxColors.success)
            }
            Spacer(Modifier.height(CxSpacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonoText(text = "ENCRYPTION:", fontSize = CxTypography.textXs, color = colors.textTertiary)
                MonoText(text = "DTLS ACTIVE", fontSize = CxTypography.textSm, color = CxColors.success)
            }
        }

        Spacer(Modifier.height(CxSpacing.xl))

        BrutalistButton(
            text = "START TRANSFER →",
            onClick = onTransfer,
            variant = ButtonVariant.PRIMARY,
            size = ButtonSize.LARGE,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

// ── TRANSFERRING ─────────────────────────────────

@Composable
private fun TransferringState(onComplete: () -> Unit) {
    val colors = CxTheme.colors
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(80)
            progress = (progress + 0.015f).coerceAtMost(1f)
        }
        delay(500)
        onComplete()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        MicroAppPanel(title = "Transferring — P2P Direct") {
            BrutalistProgressBar(
                progress = progress,
                segments = 20
            )
            Spacer(Modifier.height(CxSpacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonoText(text = "SPEED:", fontSize = CxTypography.textXs, color = colors.textTertiary)
                MonoText(text = "12.4 MB/S", fontSize = CxTypography.textSm, color = colors.accent)
            }
            Spacer(Modifier.height(CxSpacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonoText(text = "REMAINING:", fontSize = CxTypography.textXs, color = colors.textTertiary)
                MonoText(
                    text = "${((1f - progress) * 7.1f * 100).toInt() / 100f} MB",
                    fontSize = CxTypography.textSm,
                    color = colors.textSecondary
                )
            }
            Spacer(Modifier.height(CxSpacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonoText(text = "ENCRYPTION:", fontSize = CxTypography.textXs, color = colors.textTertiary)
                MonoText(text = "DTLS ✓", fontSize = CxTypography.textSm, color = CxColors.success)
            }
        }
    }
}

// ── SUCCESS ──────────────────────────────────────

@Composable
private fun SuccessState(onReset: () -> Unit) {
    val colors = CxTheme.colors
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SlamIn(visible = visible) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MonoText(
                    text = "✓",
                    fontSize = CxTypography.text7xl,
                    color = CxColors.success,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(CxSpacing.lg))
                SectionTitle(
                    text = "TRANSFER\nCOMPLETE",
                    color = CxColors.success
                )
            }
        }

        Spacer(Modifier.height(CxSpacing.xl))

        RevealFromBottom(visible = visible, delayMs = 400) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MonoText(text = "FILES:", fontSize = CxTypography.textXs, color = colors.textTertiary)
                    MonoText(text = "3", fontSize = CxTypography.textSm, color = colors.textPrimary)
                }
                Spacer(Modifier.height(CxSpacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MonoText(text = "TOTAL:", fontSize = CxTypography.textXs, color = colors.textTertiary)
                    MonoText(text = "7.1 MB", fontSize = CxTypography.textSm, color = colors.textPrimary)
                }
                Spacer(Modifier.height(CxSpacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MonoText(text = "ROUTE:", fontSize = CxTypography.textXs, color = colors.textTertiary)
                    BrutalistBadge(text = "P2P DIRECT", accentColor = CxColors.success)
                }
                Spacer(Modifier.height(CxSpacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MonoText(text = "DURATION:", fontSize = CxTypography.textXs, color = colors.textTertiary)
                    MonoText(text = "4.2S", fontSize = CxTypography.textSm, color = colors.textPrimary)
                }
            }
        }

        Spacer(Modifier.height(CxSpacing.xxl))

        RevealFromBottom(visible = visible, delayMs = 600) {
            BrutalistButton(
                text = "NEW TRANSFER",
                onClick = onReset,
                variant = ButtonVariant.SECONDARY,
                size = ButtonSize.LARGE,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── ERROR ────────────────────────────────────────

@Composable
private fun ErrorState(onRetry: () -> Unit, onReset: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MonoText(
            text = "✕",
            fontSize = CxTypography.text7xl,
            color = CxColors.error,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(CxSpacing.lg))
        SectionTitle(text = "TRANSFER\nFAILED", color = CxColors.error)
        Spacer(Modifier.height(CxSpacing.md))
        BodyText(
            text = "Connection dropped. The peer may have closed their browser tab.",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(CxSpacing.xl))

        BrutalistButton(
            text = "RETRY",
            onClick = onRetry,
            variant = ButtonVariant.PRIMARY,
            size = ButtonSize.LARGE,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(CxSpacing.md))
        BrutalistButton(
            text = "START OVER",
            onClick = onReset,
            variant = ButtonVariant.GHOST,
            size = ButtonSize.MEDIUM,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── OFFLINE ──────────────────────────────────────

@Composable
private fun OfflineState() {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MonoText(
            text = "⊘",
            fontSize = CxTypography.text7xl,
            color = colors.textTertiary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(CxSpacing.lg))
        SectionTitle(text = "OFFLINE", color = colors.textTertiary)
        Spacer(Modifier.height(CxSpacing.md))
        BodyText(
            text = "File tools work offline. Sharing requires a connection.",
            textAlign = TextAlign.Center
        )
    }
}

// ── TOOLS TAB ────────────────────────────────────

@Composable
private fun ToolsTab() {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val toolRunner = remember(context) { WorkspaceToolRunner(context.applicationContext) }
    val toolState by toolRunner.state.collectAsState()
    var pendingTool by remember { mutableStateOf<WorkspaceToolId?>(null) }
    var lastSavedMessage by remember { mutableStateOf<String?>(null) }

    val toolPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val tool = pendingTool
        pendingTool = null
        if (tool != null && !uris.isNullOrEmpty()) {
            lastSavedMessage = null
            toolRunner.runTool(tool, uris)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(top = CxSpacing.xl)
    ) {
        SectionLabel(text = "File Tools")
        Spacer(Modifier.height(CxSpacing.lg))
        SectionTitle(text = "TRANSFORM\nBEFORE SHARING")
        Spacer(Modifier.height(CxSpacing.xl))

        val tools = listOf(
            ToolListItem("⇩", "IMAGE COMPRESS", "JPEG, PNG, WebP — up to 90% smaller", WorkspaceToolId.IMAGE_COMPRESS),
            ToolListItem("⇄", "FORMAT CONVERT", "Convert images into WebP for lighter sharing", WorkspaceToolId.IMAGE_CONVERT),
            ToolListItem("⊕", "PDF MERGE", "Combine multiple PDFs into one", WorkspaceToolId.PDF_MERGE),
            ToolListItem("⊗", "PDF SPLIT", "Extract each PDF page into its own file", WorkspaceToolId.PDF_SPLIT),
            ToolListItem("◫", "PDF → IMAGE", "Export PDF pages as JPG images", WorkspaceToolId.PDF_TO_IMAGE),
            ToolListItem("⬡", "DOCX → PDF", "Word documents to PDF", WorkspaceToolId.WORD_TO_PDF),
            ToolListItem("⊞", "ZIP BUNDLE", "Package any files into ZIP", WorkspaceToolId.ZIP),
            ToolListItem("⟳", "SMART CHAIN", "Analyze the file and suggest the next steps", WorkspaceToolId.SMART_CHAIN)
        )

        if (toolState.isProcessing) {
            ToolProcessingPanel(
                title = toolState.activeTool?.name?.replace('_', ' ') ?: "PROCESSING",
                progress = toolState.progress
            )
            Spacer(Modifier.height(CxSpacing.lg))
        }

        toolState.result?.let { result ->
            ToolResultPanel(
                result = result,
                savedMessage = lastSavedMessage,
                onSave = {
                    lastSavedMessage = saveToolResultToDownloads(context, result)
                },
                onClear = {
                    lastSavedMessage = null
                    toolRunner.clearResult()
                }
            )
            Spacer(Modifier.height(CxSpacing.lg))
        }

        if (toolState.error != null) {
            WorkspaceErrorPanel(
                message = toolState.error.orEmpty(),
                onRetry = {
                    toolRunner.clearResult()
                },
                onReset = {
                    lastSavedMessage = null
                    toolRunner.clearResult()
                }
            )
            Spacer(Modifier.height(CxSpacing.lg))
        }

        tools.forEachIndexed { index, tool ->
            ToolRow(
                icon = tool.icon,
                title = tool.title,
                description = tool.description,
                onClick = {
                    pendingTool = tool.id
                    toolPicker.launch(toolMimeFilters(tool.id))
                }
            )
            if (index < tools.lastIndex) {
                Spacer(Modifier.height(CxSpacing.sm))
            }
        }
        
        Spacer(Modifier.height(CxSpacing.xxl))
        BodyText(
            text = "File tools run on-device. No uploads, no cloud.",
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(120.dp))
    }
}

private data class ToolListItem(
    val icon: String,
    val title: String,
    val description: String,
    val id: WorkspaceToolId,
)

@Composable
private fun ToolProcessingPanel(
    title: String,
    progress: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CxTheme.colors.bgCard)
            .border(CxBorders.thin, CxTheme.colors.borderSubtle)
            .padding(CxSpacing.md)
    ) {
        MonoText(
            text = title,
            fontSize = CxTypography.textSm,
            color = CxTheme.colors.accent
        )
        Spacer(Modifier.height(CxSpacing.md))
        BrutalistProgressBar(
            progress = progress / 100f,
            accentColor = CxTheme.colors.accent,
            showLabel = false
        )
        Spacer(Modifier.height(CxSpacing.sm))
        MonoText(
            text = "$progress%",
            fontSize = CxTypography.textXs,
            color = CxTheme.colors.textTertiary
        )
    }
}

@Composable
private fun ToolResultPanel(
    result: WorkspaceToolResult,
    savedMessage: String?,
    onSave: () -> Unit,
    onClear: () -> Unit,
) {
    val colors = CxTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgCard)
            .border(CxBorders.thin, colors.borderSubtle)
            .padding(CxSpacing.md)
    ) {
        MonoText(
            text = "TOOL COMPLETE",
            fontSize = CxTypography.textSm,
            color = colors.accent
        )
        Spacer(Modifier.height(CxSpacing.sm))
        result.outputName?.let {
            MonoText(
                text = it,
                fontSize = CxTypography.textBase,
                color = colors.textPrimary
            )
        }
        result.note?.let {
            if (result.outputName != null) Spacer(Modifier.height(CxSpacing.xs))
            BodyText(text = it, fontSize = CxTypography.textXs)
        }
        savedMessage?.let {
            Spacer(Modifier.height(CxSpacing.sm))
            BodyText(text = it, fontSize = CxTypography.textXs, color = colors.textTertiary)
        }
        if (result.suggestions.isNotEmpty()) {
            Spacer(Modifier.height(CxSpacing.md))
            MonoText(
                text = "NEXT BEST ACTIONS",
                fontSize = CxTypography.textXs,
                color = colors.textTertiary
            )
            Spacer(Modifier.height(CxSpacing.sm))
            result.suggestions.forEachIndexed { index, suggestion ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bgPrimary)
                        .border(CxBorders.thin, colors.borderSubtle)
                        .padding(CxSpacing.sm)
                ) {
                    MonoText(
                        text = suggestion.label,
                        fontSize = CxTypography.textSm,
                        color = colors.textPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    BodyText(text = suggestion.description, fontSize = CxTypography.textXs)
                }
                if (index < result.suggestions.lastIndex) {
                    Spacer(Modifier.height(CxSpacing.xs))
                }
            }
        }
        Spacer(Modifier.height(CxSpacing.md))
        if (result.outputBytes != null && result.outputName != null && result.outputType != null) {
            BrutalistButton(
                text = "SAVE OUTPUT",
                onClick = onSave,
                variant = ButtonVariant.PRIMARY,
                size = ButtonSize.MEDIUM,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(CxSpacing.sm))
        }
        BrutalistButton(
            text = "CLEAR RESULT",
            onClick = onClear,
            variant = ButtonVariant.SECONDARY,
            size = ButtonSize.MEDIUM,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun toolMimeFilters(toolId: WorkspaceToolId): Array<String> = when (toolId) {
    WorkspaceToolId.IMAGE_COMPRESS,
    WorkspaceToolId.IMAGE_CONVERT,
    WorkspaceToolId.IMAGE_TO_WEBP,
    WorkspaceToolId.IMAGE_TO_JPEG,
    WorkspaceToolId.IMAGE_TO_PNG -> arrayOf("image/*")
    WorkspaceToolId.PDF_MERGE,
    WorkspaceToolId.PDF_SPLIT,
    WorkspaceToolId.PDF_TO_IMAGE -> arrayOf("application/pdf")
    WorkspaceToolId.WORD_TO_PDF -> arrayOf(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/msword"
    )
    WorkspaceToolId.ZIP,
    WorkspaceToolId.SMART_CHAIN -> arrayOf("*/*")
}

private fun saveToolResultToDownloads(context: Context, result: WorkspaceToolResult): String {
    val outputBytes = result.outputBytes ?: return "No file output to save."
    val outputName = result.outputName ?: return "No file output to save."
    val outputType = result.outputType ?: "application/octet-stream"

    return runCatching {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, outputName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, outputType)
                put(
                    android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                    "${android.os.Environment.DIRECTORY_DOWNLOADS}/Clex Tools"
                )
                put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Could not create Downloads entry.")
            runCatching {
                resolver.openOutputStream(uri)?.use { it.write(outputBytes) }
                    ?: error("Could not open Downloads output stream.")
                resolver.update(
                    uri,
                    android.content.ContentValues().apply {
                        put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                    },
                    null,
                    null
                )
            }.getOrElse { error ->
                resolver.delete(uri, null, null)
                throw error
            }
        } else {
            val downloadsRoot = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                ?: java.io.File(context.filesDir, "downloads-fallback")
            val clexDir = java.io.File(downloadsRoot, "Clex Tools").also { it.mkdirs() }
            java.io.File(clexDir, outputName).writeBytes(outputBytes)
        }
        "Saved $outputName to Downloads/Clex Tools."
    }.getOrElse { error ->
        error.message ?: "Could not save the output."
    }
}

@Composable
private fun ToolRow(icon: String, title: String, description: String, onClick: () -> Unit) {
    val colors = CxTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.borderColor)
            .background(colors.bgCard)
            .clickable { onClick() }
            .padding(CxSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, colors.borderColor)
                .background(colors.bgSecondary),
            contentAlignment = Alignment.Center
        ) {
            MonoText(text = icon, fontSize = CxTypography.textXl, color = colors.accent)
        }
        Spacer(Modifier.width(CxSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            MonoText(
                text = title,
                fontSize = CxTypography.textSm,
                fontWeight = CxTypography.weightBold,
                color = colors.textPrimary
            )
            BodyText(text = description, fontSize = CxTypography.textXs)
        }
        MonoText(text = "→", fontSize = CxTypography.textXl, color = colors.accent)
    }
}
