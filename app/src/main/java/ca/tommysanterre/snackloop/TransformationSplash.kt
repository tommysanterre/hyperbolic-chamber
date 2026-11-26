package ca.tommysanterre.snackloop

import android.graphics.BitmapFactory
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class TransformationCelebration(
    val templateId: String,
    val templateName: String,
    val completedSets: Int,
    val totalSets: Int,
    val durationMinutes: Long,
    val newlyUnlockedSuperSaiyan: Boolean
)

/**
 * Full-screen animated Super Saiyan transformation splash played at the end of a strength workout.
 * Uses the pixel-registered standing base and Super Saiyan artwork panels (Goku for Strength A,
 * Vegeta for Strength B) layered with a choreographed Compose Canvas golden ki aura, shockwave
 * burst, rising particles, and Super Saiyan lightning bolts.
 */
@Composable
fun TransformationSplash(
    celebration: TransformationCelebration,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val useVegeta = celebration.templateId == StrengthCatalog.STRENGTH_B_ID
    val baseRes = if (useVegeta) R.drawable.movement_squats else R.drawable.movement_deep_squat
    val ssjRes = if (useVegeta) R.drawable.movement_squats_ssj else R.drawable.movement_deep_squat_ssj

    val baseBitmap = remember(baseRes) {
        BitmapFactory.decodeResource(context.resources, baseRes).asImageBitmap()
    }
    val ssjBitmap = remember(ssjRes) {
        BitmapFactory.decodeResource(context.resources, ssjRes).asImageBitmap()
    }

    // Master transformation timeline: 0.0 -> 0.44 charge, 0.44 -> 0.56 flash burst, 0.56 -> 1.0 ascended
    val progress = remember(celebration) { Animatable(0f) }
    LaunchedEffect(celebration) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2700, easing = LinearEasing)
        )
    }

    val infinite = rememberInfiniteTransition(label = "ssj_aura_loop")
    val auraPulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aura_pulse"
    )
    val flicker by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 180, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_flicker"
    )

    val t = progress.value
    val isTransformed = t >= 0.48f
    val chargeIntensity = (t / 0.48f).coerceIn(0f, 1f)
    val flashAlpha = when {
        t < 0.42f -> 0f
        t < 0.50f -> ((t - 0.42f) / 0.08f).coerceIn(0f, 1f)
        t < 0.72f -> (1f - (t - 0.50f) / 0.22f).coerceIn(0f, 1f)
        else -> 0f
    }
    val shockwaveProgress = ((t - 0.46f) / 0.36f).coerceIn(0f, 1f)
    val revealAlpha = ((t - 0.56f) / 0.24f).coerceIn(0f, 1f)

    // Subtle kinetic tremor while charging up, settling smoothly after transformation
    val tremorAmplitude = if (!isTransformed) chargeIntensity * 9f else ((1f - revealAlpha) * 5f)
    val shakeX = sin(t * 140f) * tremorAmplitude
    val shakeY = cos(t * 115f) * tremorAmplitude * 0.5f
    val scaleBoost = if (!isTransformed) 1f + chargeIntensity * 0.04f else 1.05f - 0.03f * revealAlpha

    val goldBright = Color(0xFFFFF176)
    val goldCore = Color(0xFFFFD54F)
    val goldDeep = Color(0xFFFFA000)
    val electricCyan = Color(0xFF80D8FF)
    val chamberDark = Color(0xFF141108)

    Dialog(
        onDismissRequest = { if (t >= 0.65f) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1B1609),
                            chamberDark,
                            Color(0xFF221A08)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Background golden energy aura, shockwave rings, particles & lightning
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height * 0.43f)
                val maxRadius = maxOf(size.width, size.height) * 0.65f
                val activeIntensity = if (isTransformed) 1f else chargeIntensity * 0.6f

                // Radial golden ambient glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            goldBright.copy(alpha = 0.42f * activeIntensity * flicker.coerceAtMost(1.1f)),
                            goldCore.copy(alpha = 0.24f * activeIntensity),
                            goldDeep.copy(alpha = 0.10f * activeIntensity),
                            Color.Transparent
                        ),
                        center = center,
                        radius = maxRadius * (0.65f + 0.18f * activeIntensity)
                    ),
                    radius = maxRadius,
                    center = center
                )

                // Rising undulating ki flame ribbons behind the fighter
                val flameCount = 12
                for (i in 0 until flameCount) {
                    val angle = (i.toFloat() / flameCount) * 2f * PI.toFloat() + auraPulse * 0.4f
                    val spreadX = cos(angle) * size.width * (0.20f + 0.08f * activeIntensity)
                    val baseY = center.y + size.height * 0.16f
                    val flameHeight = size.height * (0.28f + 0.18f * activeIntensity) *
                        (0.8f + 0.2f * sin(auraPulse * 2f * PI.toFloat() + i * 1.1f))
                    val path = Path().apply {
                        moveTo(center.x + spreadX * 0.45f, baseY)
                        quadraticTo(
                            center.x + spreadX * 1.25f,
                            baseY - flameHeight * 0.45f,
                            center.x + spreadX * 0.2f,
                            baseY - flameHeight
                        )
                        quadraticTo(
                            center.x + spreadX * 0.8f,
                            baseY - flameHeight * 0.4f,
                            center.x + spreadX * 0.15f,
                            baseY
                        )
                        close()
                    }
                    drawPath(
                        path = path,
                        color = (if (i % 2 == 0) goldBright else goldCore).copy(alpha = 0.16f * activeIntensity)
                    )
                }

                // Expanding shockwave rings on ignition
                if (shockwaveProgress in 0.01f..0.99f) {
                    val ringRadius = maxRadius * shockwaveProgress * 1.1f
                    val ringAlpha = (1f - shockwaveProgress) * 0.85f
                    drawCircle(
                        color = goldBright.copy(alpha = ringAlpha),
                        radius = ringRadius,
                        center = center,
                        style = Stroke(width = 14f * (1f - shockwaveProgress) + 3f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = ringAlpha * 0.7f),
                        radius = ringRadius * 0.78f,
                        center = center,
                        style = Stroke(width = 6f * (1f - shockwaveProgress) + 2f)
                    )
                }

                // Rising golden ki sparks
                val sparkCount = 28
                for (i in 0 until sparkCount) {
                    val phase = (auraPulse + i * 0.137f) % 1f
                    val seedAngle = i * 2.39996f
                    val xOffset = sin(seedAngle) * size.width * 0.42f
                    val yPos = size.height * (0.88f - phase * 0.78f)
                    val sparkAlpha = sin(phase * PI.toFloat()).coerceIn(0f, 1f) * activeIntensity * 0.9f
                    val sparkLength = (14f + (i % 5) * 8f) * (0.5f + activeIntensity * 0.7f)
                    drawLine(
                        color = (if (i % 3 == 0) Color.White else goldBright).copy(alpha = sparkAlpha),
                        start = Offset(center.x + xOffset, yPos),
                        end = Offset(center.x + xOffset, yPos - sparkLength),
                        strokeWidth = if (i % 4 == 0) 4.5f else 2.8f,
                        cap = StrokeCap.Round
                    )
                }

                // Super Saiyan electric lightning arcs (during flash + periodically once ascended)
                val showLightning = (t in 0.38f..0.68f) || (isTransformed && ((auraPulse * 4f).toInt() % 2 == 0))
                if (showLightning) {
                    val boltAlpha = if (t in 0.42f..0.62f) 0.9f else 0.45f
                    val boltSeeds = listOf(-1f to -0.6f, 1f to -0.4f, -0.8f to 0.5f, 0.9f to 0.6f)
                    boltSeeds.forEachIndexed { idx, (dx, dy) ->
                        val start = Offset(
                            center.x + dx * size.width * 0.18f,
                            center.y + dy * size.height * 0.18f
                        )
                        val mid1 = Offset(
                            start.x + dx * 48f + sin(auraPulse * 18f + idx) * 24f,
                            start.y + dy * 36f - 28f
                        )
                        val mid2 = Offset(
                            mid1.x + dx * 36f - cos(auraPulse * 22f + idx) * 28f,
                            mid1.y + dy * 42f + 16f
                        )
                        val end = Offset(
                            mid2.x + dx * 52f,
                            mid2.y + dy * 38f - 18f
                        )
                        val boltPath = Path().apply {
                            moveTo(start.x, start.y)
                            lineTo(mid1.x, mid1.y)
                            lineTo(mid2.x, mid2.y)
                            lineTo(end.x, end.y)
                        }
                        drawPath(
                            path = boltPath,
                            color = electricCyan.copy(alpha = boltAlpha),
                            style = Stroke(width = 4.5f, cap = StrokeCap.Round)
                        )
                        drawPath(
                            path = boltPath,
                            color = Color.White.copy(alpha = boltAlpha * 0.85f),
                            style = Stroke(width = 2f, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // Foreground fighter card + completion readout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isTransformed) "SUPER SAIYAN!" else "POWERING UP...",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    ),
                    color = if (isTransformed) goldBright else goldCore.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))

                // Framed portrait drawing the left standing panel cleanly
                Card(
                    modifier = Modifier
                        .size(width = 252.dp, height = 320.dp)
                        .graphicsLayer {
                            translationX = shakeX
                            translationY = shakeY
                            scaleX = scaleBoost
                            scaleY = scaleBoost
                        },
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    val activeImage = if (isTransformed) ssjBitmap else baseBitmap
                    val split = activeImage.width / 2
                    Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                        val srcWidth = split - 3
                        val scale = minOf(size.width / srcWidth, size.height / activeImage.height)
                        val dstWidth = (srcWidth * scale).roundToInt().coerceAtLeast(1)
                        val dstHeight = (activeImage.height * scale).roundToInt().coerceAtLeast(1)
                        drawImage(
                            image = activeImage,
                            srcOffset = IntOffset(0, 0),
                            srcSize = IntSize(srcWidth, activeImage.height),
                            dstOffset = IntOffset(
                                ((size.width - dstWidth) / 2f).roundToInt(),
                                ((size.height - dstHeight) / 2f).roundToInt()
                            ),
                            dstSize = IntSize(dstWidth, dstHeight),
                            filterQuality = FilterQuality.High
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.graphicsLayer { alpha = revealAlpha }
                ) {
                    Text(
                        text = "${celebration.templateName} Complete",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${celebration.completedSets} of ${celebration.totalSets} sets logged · ~${celebration.durationMinutes} min",
                        style = MaterialTheme.typography.bodyLarge,
                        color = goldCore.copy(alpha = 0.92f),
                        textAlign = TextAlign.Center
                    )
                    if (celebration.newlyUnlockedSuperSaiyan) {
                        Text(
                            text = "Super Saiyan artwork unlocked across the app!",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = goldBright,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onDismiss,
                        enabled = t >= 0.65f,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = goldCore,
                            contentColor = Color(0xFF1A1400)
                        ),
                        modifier = Modifier.fillMaxWidth(0.7f).height(52.dp)
                    ) {
                        Text(
                            "Continue",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Blinding golden-white transformation flash overlay
            if (flashAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = flashAlpha),
                                    goldBright.copy(alpha = flashAlpha * 0.92f),
                                    goldCore.copy(alpha = flashAlpha * 0.72f)
                                )
                            )
                        )
                )
            }
        }
    }
}
