# Clex Android — ProGuard / R8 keep rules.
#
# Release builds enable minify + resource shrinking. The libraries below all
# rely on either reflection or JNI, so R8 needs explicit keeps. Anything not
# listed here is fair game for shrinking.

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-dontwarn kotlinx.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**

# ── WebRTC (org.webrtc.*) ─────────────────────────────────────────────────
# Loaded from native code via JNI. The full org.webrtc package needs to stay
# intact along with all native methods. The Stream wrapper (io.getstream.*)
# also bridges to native through the same factory.
-keep class org.webrtc.** { *; }
-keep interface org.webrtc.** { *; }
-keep class io.getstream.webrtc.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# ── OkHttp / Okio ─────────────────────────────────────────────────────────
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ── PDFBox-Android (com.tom_roush.pdfbox) ────────────────────────────────
# Reflection on glyph + cmap tables; keep the entire package.
-keep class com.tom_roush.pdfbox.** { *; }
-keep class com.tom_roush.fontbox.** { *; }
-dontwarn com.tom_roush.**

# ── Apache POI / XMLBeans ────────────────────────────────────────────────
# Heavy reflection across schema types. Keep the public surface.
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class org.openxmlformats.** { *; }
-keep class schemasMicrosoftCom** { *; }
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.openxmlformats.**
-dontwarn schemasMicrosoftCom**
-dontwarn java.awt.**
-dontwarn javax.xml.**
# log4j2 + xmlbeans pull in OSGi / FindBugs / aQute build-tooling annotations
# that are never present at runtime on Android. Silence the missing-class
# errors instead of bundling those packages.
-dontwarn org.osgi.**
-dontwarn aQute.**
-dontwarn edu.umd.cs.findbugs.**

# ── ZXing barcode / camera ────────────────────────────────────────────────
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.barcodescanner.** { *; }
-dontwarn com.google.zxing.**

# ── Compose (most of the keep is in the default file already, this just
# silences warnings for tooling that ships in the BOM) ───────────────────
-dontwarn androidx.compose.ui.tooling.**

# ── App: keep the data layer's surface for JNI/JSON usage ─────────────────
# We do not use reflection-based JSON (only manual JSONObject), so the only
# thing that absolutely must stick around is the Application class (so the
# manifest can find it after rename).
-keep class com.clex.android.ClexApplication { *; }
