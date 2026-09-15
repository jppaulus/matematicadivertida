package com.joaop.matematicadivertida

import android.Manifest
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.drawscope.Fill
import kotlin.random.Random
import android.util.Log
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.compose.material3.Switch
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdListener
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

// Minimal interactive visuals used by SolutionDialog (NumberLine & BlocksGrid)
@Composable
fun NumberLine(maxValue: Int, highlighted: Int, startOffset: Int = 0, modifier: Modifier = Modifier, onTickClick: ((Int) -> Unit)? = null) {
    if (maxValue < startOffset) return
    val ticks = (maxValue - startOffset + 1).coerceAtMost(24)
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        (startOffset..maxValue).take(ticks).forEach { value ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val color = if (value == highlighted) Color(0xFF4CAF50) else Color(0xFFBDBDBD)
                    drawCircle(color = color, radius = size.minDimension / 2, style = Fill)
                }
                Text(value.toString(), fontSize = 12.sp, textAlign = TextAlign.Center,
                    modifier = if (onTickClick != null) Modifier.testTag("numberLineTick_$value").clickable { onTickClick(value) } else Modifier)
            }
        }
    }
}

@Composable
fun BlocksGrid(rows: Int, cols: Int, highlightCols: Int, modifier: Modifier = Modifier, onColClick: ((Int) -> Unit)? = null) {
    if (rows <= 0 || cols <= 0) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) { Text("", fontSize = 12.sp) }
        return
    }
    val safeRows = rows.coerceIn(1, 12)
    val safeCols = cols.coerceIn(1, 12)
    val selectedCols = remember { mutableStateListOf<Boolean>().apply { for (i in 0 until safeCols) add(false) } }
    Column(modifier = modifier) {
        for (r in 0 until safeRows) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(2.dp)) {
                for (c in 0 until safeCols) {
                    val active = c < highlightCols
                    val selected = selectedCols.getOrElse(c) { false }
                    val bgColor = if (selected || active) Color(0xFF4CAF50) else Color(0xFFEEEEEE)
                    Box(
                        modifier = (
                            if (onColClick != null) Modifier.testTag("blocksGridCol_$c").clickable {
                                selectedCols[c] = !selectedCols[c]
                                val totalSelected = selectedCols.count { it }
                                try { onColClick(totalSelected) } catch (_: Exception) {}
                            } else Modifier
                        ).size(24.dp).background(bgColor, RoundedCornerShape(4.dp))
                    ) {}
                }
            }
        }
    }
}
