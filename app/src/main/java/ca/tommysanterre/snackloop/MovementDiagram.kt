package ca.tommysanterre.snackloop

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

internal fun artworkFor(id: String, superSaiyan: Boolean = false): Int = when {
    superSaiyan && id == "pushups" -> R.drawable.movement_pushups_ssj
    superSaiyan && id == "squats" -> R.drawable.movement_squats_ssj
    superSaiyan && id == "three_point_bridge" -> R.drawable.movement_three_point_bridge_ssj
    superSaiyan && id == "pullups" -> R.drawable.movement_pullups_ssj
    superSaiyan && id == "single_leg_rdl" -> R.drawable.movement_single_leg_rdl_ssj
    superSaiyan && id == "tuck_l_sit" -> R.drawable.movement_tuck_l_sit_ssj
    superSaiyan && id == "reverse_lunges" -> R.drawable.movement_reverse_lunges_ssj
    superSaiyan && id == "ring_rows" -> R.drawable.movement_ring_rows_ssj
    superSaiyan && id == "ring_hamstring_curls" -> R.drawable.movement_ring_hamstring_curls_ssj
    superSaiyan && id == "pike_pushups" -> R.drawable.movement_pike_pushups_ssj
    superSaiyan && id == "deep_squat" -> R.drawable.movement_deep_squat_ssj
    superSaiyan && id == "dead_hang" -> R.drawable.movement_dead_hang_ssj
    id == "pushups" -> R.drawable.movement_pushups
    id == "squats" -> R.drawable.movement_squats
    id == "deep_squat" -> R.drawable.movement_deep_squat
    id == "pullups" -> R.drawable.movement_pullups
    id == "dead_hang" -> R.drawable.movement_dead_hang
    id == "reverse_lunges" -> R.drawable.movement_reverse_lunges
    id == "ring_rows" -> R.drawable.movement_ring_rows
    id == "hip_flexor" -> R.drawable.movement_hip_flexor
    id == "three_point_bridge" -> R.drawable.movement_three_point_bridge
    id == "single_leg_rdl" -> R.drawable.movement_single_leg_rdl
    id == "tuck_l_sit" -> R.drawable.movement_tuck_l_sit
    id == "ring_hamstring_curls" -> R.drawable.movement_ring_hamstring_curls
    id == "pike_pushups" -> R.drawable.movement_pike_pushups
    else -> error("No movement artwork for $id")
}

@Composable
fun MovementDiagram(exercise: Exercise, superSaiyan: Boolean) {
    val context = LocalContext.current
    val artwork = artworkFor(exercise.id, superSaiyan)
    val image = remember(artwork) {
        BitmapFactory.decodeResource(context.resources, artwork).asImageBitmap()
    }
    // The deadlift illustration has a narrower start panel in both character forms.
    val split = if (exercise.id == "single_leg_rdl") image.width * 11 / 24 else image.width / 2
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PoseCard("START", exercise.name, image, 0, split, Modifier.weight(1f))
        PoseCard("END", exercise.name, image, 1, split, Modifier.weight(1f))
    }
}

@Composable
private fun PoseCard(label: String, movement: String, image: ImageBitmap, panel: Int, split: Int, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier.height(256.dp).semantics { contentDescription = "$label position for $movement" }
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Canvas(Modifier.fillMaxWidth().weight(1f)) { drawPanel(image, panel, split) }
        }
    }
}

private fun DrawScope.drawPanel(image: ImageBitmap, panel: Int, split: Int) {
    // Omit the separator itself so it does not appear at the edge of either card.
    val sourceLeft = if (panel == 0) 0 else split + 3
    val sourceWidth = if (panel == 0) split - 3 else image.width - sourceLeft
    val scale = minOf(size.width / sourceWidth, size.height / image.height)
    val width = (sourceWidth * scale).roundToInt().coerceAtLeast(1)
    val height = (image.height * scale).roundToInt().coerceAtLeast(1)
    drawImage(
        image = image,
        srcOffset = IntOffset(sourceLeft, 0),
        srcSize = IntSize(sourceWidth, image.height),
        dstOffset = IntOffset(((size.width - width) / 2).roundToInt(), ((size.height - height) / 2).roundToInt()),
        dstSize = IntSize(width, height),
        filterQuality = FilterQuality.High
    )
}
