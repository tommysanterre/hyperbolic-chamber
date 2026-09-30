package ca.tommysanterre.snackloop

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal data class Pose(
    val head: Offset,
    val neck: Offset,
    val hip: Offset,
    val leftHand: Offset,
    val rightHand: Offset,
    val leftFoot: Offset,
    val rightFoot: Offset,
    val leftElbow: Offset? = null,
    val rightElbow: Offset? = null,
    val leftKnee: Offset? = null,
    val rightKnee: Offset? = null,
    val bar: Boolean = false,
    val floor: Boolean = true
)

@Composable
fun MovementDiagram(exercise: Exercise) {
    val poses = posesFor(exercise.id)
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PoseCard("START", exercise.name, poses.first, Modifier.weight(1f))
        PoseCard("END", exercise.name, poses.second, Modifier.weight(1f))
    }
}

@Composable
private fun PoseCard(label: String, movement: String, pose: Pose, modifier: Modifier = Modifier) {
    val figureColor = MaterialTheme.colorScheme.primary
    val guideColor = MaterialTheme.colorScheme.outlineVariant
    ElevatedCard(
        modifier
            .height(176.dp)
            .semantics { contentDescription = "$label position for $movement" }
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Canvas(Modifier.fillMaxWidth().weight(1f)) {
                drawPose(pose, figureColor, guideColor)
            }
        }
    }
}

private fun DrawScope.drawPose(pose: Pose, figureColor: Color, guideColor: Color) {
    fun Offset.scaled() = Offset(x * size.width, y * size.height)
    val stroke = size.minDimension * 0.045f
    fun limb(from: Offset, joint: Offset?, to: Offset) {
        val points = listOfNotNull(from, joint, to)
        points.zipWithNext().forEach { (start, end) ->
            drawLine(figureColor, start.scaled(), end.scaled(), stroke, StrokeCap.Round)
        }
    }

    if (pose.floor) {
        drawLine(guideColor, Offset(size.width * .08f, size.height * .92f), Offset(size.width * .92f, size.height * .92f), stroke / 2)
    }
    if (pose.bar) {
        drawLine(guideColor, Offset(size.width * .08f, size.height * .08f), Offset(size.width * .92f, size.height * .08f), stroke, StrokeCap.Round)
    }

    drawCircle(figureColor, size.minDimension * .075f, pose.head.scaled())
    drawLine(figureColor, pose.neck.scaled(), pose.hip.scaled(), stroke, StrokeCap.Round)
    limb(pose.neck, pose.leftElbow, pose.leftHand)
    limb(pose.neck, pose.rightElbow, pose.rightHand)
    limb(pose.hip, pose.leftKnee, pose.leftFoot)
    limb(pose.hip, pose.rightKnee, pose.rightFoot)
}

internal fun posesFor(id: String): Pair<Pose, Pose> = when (id) {
    "pushups" -> Pair(
        pose(.18f, .42f, .29f, .47f, .63f, .53f, .36f, .88f, .43f, .88f, .86f, .88f, .92f, .88f,
            .35f, .64f, .42f, .65f),
        pose(.18f, .66f, .29f, .70f, .63f, .70f, .36f, .88f, .43f, .88f, .86f, .88f, .92f, .88f,
            .35f, .80f, .42f, .80f)
    )
    "squats" -> Pair(
        standing(),
        pose(.50f, .35f, .50f, .43f, .50f, .63f, .27f, .55f, .73f, .55f, .25f, .90f, .75f, .90f, .36f, .47f, .64f, .47f, .34f, .72f, .66f, .72f)
    )
    "deep_squat" -> Pair(
        standing(),
        pose(.50f, .37f, .50f, .45f, .50f, .73f, .30f, .61f, .70f, .61f, .20f, .90f, .80f, .90f, .38f, .52f, .62f, .52f, .30f, .75f, .70f, .75f)
    )
    "pullups" -> Pair(hanging(.34f, .65f), hanging(.20f, .48f))
    "dead_hang" -> Pair(reachingForBar(), hanging(.34f, .65f))
    "reverse_lunges" -> Pair(
        standing(),
        pose(.45f, .24f, .45f, .33f, .46f, .55f, .30f, .47f, .62f, .47f, .25f, .90f, .88f, .90f, .35f, .42f, .57f, .42f, .33f, .69f, .70f, .72f)
    )
    "hip_flexor" -> Pair(
        pose(.45f, .23f, .45f, .32f, .46f, .54f, .31f, .47f, .61f, .47f, .22f, .90f, .87f, .90f, .35f, .42f, .57f, .42f, .31f, .71f, .70f, .71f),
        pose(.52f, .23f, .52f, .32f, .55f, .54f, .38f, .47f, .68f, .47f, .28f, .90f, .91f, .90f, .42f, .42f, .64f, .42f, .38f, .71f, .73f, .71f)
    )
    else -> Pair(standing(), standing())
}

private fun standing() = pose(
    .50f, .20f, .50f, .30f, .50f, .56f, .28f, .52f, .72f, .52f, .34f, .90f, .66f, .90f,
    .36f, .40f, .64f, .40f, .42f, .72f, .58f, .72f
)

private fun hanging(headY: Float, hipY: Float) = pose(
    .50f, headY, .50f, headY + .09f, .50f, hipY, .30f, .09f, .70f, .09f, .38f, .90f, .62f, .90f,
    .38f, .18f, .62f, .18f, .44f, .76f, .56f, .76f, bar = true, floor = false
)

private fun reachingForBar() = pose(
    .50f, .28f, .50f, .37f, .50f, .62f, .30f, .09f, .70f, .09f, .38f, .90f, .62f, .90f,
    .38f, .19f, .62f, .19f, .44f, .76f, .56f, .76f, bar = true
)

@Suppress("LongParameterList")
private fun pose(
    headX: Float, headY: Float, neckX: Float, neckY: Float, hipX: Float, hipY: Float,
    leftHandX: Float, leftHandY: Float, rightHandX: Float, rightHandY: Float,
    leftFootX: Float, leftFootY: Float, rightFootX: Float, rightFootY: Float,
    leftElbowX: Float? = null, leftElbowY: Float? = null,
    rightElbowX: Float? = null, rightElbowY: Float? = null,
    leftKneeX: Float? = null, leftKneeY: Float? = null,
    rightKneeX: Float? = null, rightKneeY: Float? = null,
    bar: Boolean = false, floor: Boolean = true
) = Pose(
    Offset(headX, headY), Offset(neckX, neckY), Offset(hipX, hipY),
    Offset(leftHandX, leftHandY), Offset(rightHandX, rightHandY),
    Offset(leftFootX, leftFootY), Offset(rightFootX, rightFootY),
    leftElbowX?.let { Offset(it, leftElbowY!!) }, rightElbowX?.let { Offset(it, rightElbowY!!) },
    leftKneeX?.let { Offset(it, leftKneeY!!) }, rightKneeX?.let { Offset(it, rightKneeY!!) },
    bar, floor
)
