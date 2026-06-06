package com.clex.android.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clex.android.AppRelease
import com.clex.android.BuildConfig
import com.clex.android.data.VaultCryptoManager
import com.clex.android.ui.components.CxIcon
import com.clex.android.ui.components.CxIconType
import com.clex.android.ui.components.HRule
import com.clex.android.ui.components.HeaderIconButton
import com.clex.android.ui.components.LedgerRow
import com.clex.android.ui.components.SectionLabel
import com.clex.android.ui.components.pressable
import com.clex.android.ui.theme.CxSpacing
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography

// ═══════════════════════════════════════════════════
//  Developer — v1.13 ledger shell.
//  Surfaces app id, build channel, fingerprint, signaling
//  routes, links to author socials. No mesh, no glyphs.
// ═══════════════════════════════════════════════════

@Composable
fun DeveloperScreen(onBack: () -> Unit) {
    val colors = CxTheme.colors
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val fingerprint = remember(context) {
        VaultCryptoManager.getDeviceFingerprint(context.applicationContext).uppercase()
    }
    val deviceName = remember { VaultCryptoManager.detectDeviceName() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp),
        ) {
            // ── Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        horizontal = CxSpacing.screenHorizontal,
                        vertical = CxSpacing.md,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                HeaderIconButton(icon = CxIconType.CHEVRON_LEFT, onClick = onBack)
                Text(
                    text = "DEVELOPER",
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontFamily = CxTypography.fontDisplay,
                    fontWeight = FontWeight.W700,
                    letterSpacing = 1.6.sp,
                )
                Box(modifier = Modifier.size(40.dp))
            }

            HRule(padded = false)

            Spacer(Modifier.height(28.dp))

            // ── Section 01 — Build ──
            SectionLabel(text = "Build", number = "01")
            Spacer(Modifier.height(14.dp))
            ValueRow(label = "Application ID", value = "com.clex.android")
            HRule()
            ValueRow(label = "Version name", value = AppRelease.versionName)
            HRule()
            ValueRow(label = "Version code", value = AppRelease.versionCode.toString())
            HRule()
            ValueRow(label = "Build type", value = if (BuildConfig.DEBUG) "debug" else "release")
            HRule()
            ValueRow(label = "Channel", value = "Public")

            Spacer(Modifier.height(36.dp))

            // ── Section 02 — Device ──
            SectionLabel(text = "Device", number = "02")
            Spacer(Modifier.height(14.dp))
            ValueRow(label = "Device fingerprint", value = fingerprint, copy = true, context = context)
            HRule()
            ValueRow(label = "Device", value = deviceName)
            HRule()
            ValueRow(label = "Android", value = "${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}")
            HRule()
            ValueRow(label = "ABI", value = (Build.SUPPORTED_ABIS.firstOrNull() ?: "—"))

            Spacer(Modifier.height(36.dp))

            // ── Section 03 — Routes ──
            SectionLabel(text = "Routes", number = "03")
            Spacer(Modifier.height(14.dp))
            ValueRow(label = "Signaling", value = BuildConfig.SIGNALING_BASE_URL, copy = true, context = context)
            HRule()
            ValueRow(label = "Manufacturer ID", value = "0x" + BuildConfig.BLE_MANUFACTURER_ID.toString(16).uppercase())
            HRule()
            ValueRow(label = "API base", value = "https://api.clex.in")
            HRule()
            ValueRow(label = "Chain feed", value = "https://api.clex.in/chain/feed")

            Spacer(Modifier.height(36.dp))

            // ── Section 04 — Author ──
            SectionLabel(text = "Author", number = "04")
            Spacer(Modifier.height(14.dp))
            LedgerRow(
                label = "Repository",
                description = "github.com/Abhinavv-007/clex-android",
                leadingIcon = CxIconType.LINK,
                trailingIcon = CxIconType.ARROW_UP_RIGHT,
                onClick = { open(context, "https://github.com/Abhinavv-007/clex-android") },
            )
            HRule()
            LedgerRow(
                label = "Email",
                description = "hello@clex.in",
                leadingIcon = CxIconType.SHARE,
                trailingIcon = CxIconType.ARROW_UP_RIGHT,
                onClick = { open(context, "mailto:hello@clex.in") },
            )
            HRule()
            LedgerRow(
                label = "LinkedIn",
                description = "linkedin.com/in/abhnv07",
                leadingIcon = CxIconType.LINK,
                trailingIcon = CxIconType.ARROW_UP_RIGHT,
                onClick = { open(context, "https://www.linkedin.com/in/abhnv07/") },
            )
            HRule()
            LedgerRow(
                label = "Instagram",
                description = "@abhinavv.007",
                leadingIcon = CxIconType.LINK,
                trailingIcon = CxIconType.ARROW_UP_RIGHT,
                onClick = { open(context, "https://www.instagram.com/abhinavv.007/") },
            )
        }
    }
}

@Composable
private fun ValueRow(
    label: String,
    value: String,
    copy: Boolean = false,
    context: Context? = null,
) {
    val colors = CxTheme.colors
    val mod = if (copy && context != null) {
        Modifier.pressable(onClick = {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText(label, value))
            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
        })
    } else Modifier
    Row(
        modifier = mod
            .fillMaxWidth()
            .padding(horizontal = CxSpacing.screenHorizontal, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = colors.textTertiary,
            fontSize = 13.sp,
            fontFamily = CxTypography.fontBody,
            fontWeight = FontWeight.W400,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontFamily = CxTypography.fontMono,
                fontWeight = FontWeight.W500,
            )
            if (copy) {
                Spacer(Modifier.width(8.dp))
                CxIcon(
                    icon = CxIconType.COPY,
                    size = 14.dp,
                    color = colors.textTertiary,
                    strokeWidth = 1.4.dp,
                )
            }
        }
    }
}

private fun open(context: Context, uri: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri))) }
}
