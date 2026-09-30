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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
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

internal data class DragonBallCharacter(
    val name: String,
    val outfit: Color,
    val accent: Color,
    val skin: Color = Color(0xFFFFCB9A),
    val hair: Color = Color(0xFF18202D),
    val hairStyle: Int = 1
)

internal fun characterFor(id: String): DragonBallCharacter = when (id) {
    "squats" -> DragonBallCharacter("Vegeta", Color(0xFF2456B8), Color(0xFFF5F0DC), hairStyle = 2)
    "deep_squat" -> DragonBallCharacter("Gohan", Color(0xFF7845A5), Color(0xFFE9483D))
    "pullups" -> DragonBallCharacter("Piccolo", Color(0xFF7845A5), Color(0xFF52A8E0), skin = Color(0xFF7CC85B), hairStyle = 0)
    "dead_hang" -> DragonBallCharacter("Trunks", Color(0xFF526CB1), Color(0xFF313444), hair = Color(0xFFB8A0DC), hairStyle = 3)
    "reverse_lunges" -> DragonBallCharacter("Krillin", Color(0xFFF58428), Color(0xFF2255A2), hairStyle = 0)
    "hip_flexor" -> DragonBallCharacter("Tien", Color(0xFF388457), Color(0xFFE34E43), hairStyle = 0)
    else -> DragonBallCharacter("Goku", Color(0xFFF58428), Color(0xFF2255A2))
}

@Composable
fun MovementDiagram(exercise: Exercise) {
    val poses = posesFor(exercise.id)
    val character = characterFor(exercise.id)
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PoseCard("START", exercise.name, poses.first, character, Modifier.weight(1f))
        PoseCard("END", exercise.name, poses.second, character, Modifier.weight(1f))
    }
}

@Composable
private fun PoseCard(label: String, movement: String, pose: Pose, character: DragonBallCharacter, modifier: Modifier = Modifier) {
    val guideColor = MaterialTheme.colorScheme.outlineVariant
    ElevatedCard(
        modifier
            .height(208.dp)
            .semantics { contentDescription = "${character.name} demonstrating the $label position for $movement" }
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
                drawPose(pose, character, guideColor)
            }
        }
    }
}

private fun DrawScope.drawPose(pose: Pose, character: DragonBallCharacter, guideColor: Color) {
    fun Offset.scaled() = Offset(x * size.width, y * size.height)
    val stroke = size.minDimension * 0.045f
    val ink = Color(0xFF18202D)
    fun polygon(points: List<Offset>, color: Color, outline: Boolean = true) {
        val path = Path().apply {
            points.forEachIndexed { index, p ->
                if (index == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
            close()
        }
        drawPath(path, color)
        if (outline) drawPath(path, ink, style = Stroke(stroke * .18f))
    }
    drawCircle(
        Brush.radialGradient(listOf(character.accent.copy(alpha = .13f), Color.Transparent),
            center = Offset(size.width * .5f, size.height * .5f), radius = size.maxDimension * .55f),
        size.maxDimension * .55f, Offset(size.width * .5f, size.height * .5f)
    )
    drawOval(guideColor.copy(alpha = .28f), Offset(size.width * .16f, size.height * .91f), Size(size.width * .68f, size.height * .055f))
    fun segment(from: Offset, to: Offset, color: Color, width: Float) {
        val start = from.scaled()
        val end = to.scaled()
        val direction = end - start
        val length = direction.getDistance().coerceAtLeast(1f)
        val normal = Offset(-direction.y / length, direction.x / length)
        val path = Path().apply {
            val a = start + normal * width * .35f
            moveTo(a.x, a.y)
            val b = start + direction * .35f + normal * width * .65f
            val c = end + normal * width * .28f
            quadraticBezierTo(b.x, b.y, c.x, c.y)
            val d = end - normal * width * .28f
            lineTo(d.x, d.y)
            val e = start + direction * .35f - normal * width * .65f
            val f = start - normal * width * .35f
            quadraticBezierTo(e.x, e.y, f.x, f.y)
            close()
        }
        drawPath(path, Brush.linearGradient(listOf(lerp(color, Color.White, .25f), color, lerp(color, ink, .32f)), start - normal * width * .5f, start + normal * width * .5f))
        drawPath(path, ink, style = Stroke(stroke * .2f))
        drawLine(ink.copy(alpha = .3f), start + direction * .38f + normal * width * .15f,
            end - direction * .18f + normal * width * .1f, stroke * .12f, StrokeCap.Round)
    }
    fun limb(from: Offset, joint: Offset?, to: Offset, color: Color, width: Float) {
        val points = listOfNotNull(from, joint, to)
        points.zipWithNext().forEach { (start, end) ->
            segment(start, end, color, width)
        }
        val cuff = to + ((joint ?: from) - to) * .22f
        segment(cuff, to, character.accent, width * 1.05f)
    }

    if (pose.floor) {
        drawLine(guideColor, Offset(size.width * .08f, size.height * .92f), Offset(size.width * .92f, size.height * .92f), stroke / 2)
    }
    if (pose.bar) {
        drawLine(guideColor, Offset(size.width * .08f, size.height * .08f), Offset(size.width * .92f, size.height * .08f), stroke, StrokeCap.Round)
    }

    val trousers = if (character.name == "Trunks") character.accent else character.outfit
    limb(pose.hip, pose.leftKnee, pose.leftFoot, trousers, stroke * 2.9f)
    limb(pose.hip, pose.rightKnee, pose.rightFoot, trousers, stroke * 2.9f)
    for ((foot, side) in listOf(pose.leftFoot to -1f, pose.rightFoot to 1f)) {
        val p = foot.scaled()
        polygon(listOf(p + Offset(-stroke * .7f, -stroke), p + Offset(stroke * .7f, -stroke),
            p + Offset(stroke * (.7f + side * .5f), stroke * .35f),
            p + Offset(stroke * (-.7f + side * .5f), stroke * .35f)), character.accent)
        drawLine(ink, p + Offset(-stroke * .7f, stroke * .35f), p + Offset(stroke * .7f, stroke * .35f), stroke * .2f)
    }
    val torsoTop = pose.neck.scaled()
    val torsoVector = pose.hip.scaled() - torsoTop
    val torsoLength = torsoVector.getDistance().coerceAtLeast(1f)
    val torsoSide = Offset(torsoVector.y, -torsoVector.x) / torsoLength
    fun torso(x: Float, y: Float) = torsoTop + torsoVector * y + torsoSide * stroke * x
    fun garment(points: List<Pair<Float, Float>>, color: Color, outline: Boolean = true) =
        polygon(points.map { (x, y) -> torso(x, y) }, color, outline)
    val shirt = if (character.name == "Tien") character.skin else character.outfit
    garment(listOf(-1.8f to 0f, 1.8f to 0f, 2.2f to .2f, 1.35f to 1f, -1.35f to 1f, -2.2f to .2f), shirt)
    garment(listOf(1.5f to .05f, 2.2f to .2f, 1.35f to 1f, .6f to .94f, 1.25f to .5f), lerp(shirt, ink, .3f), false)
    garment(listOf(-1.6f to .08f, -.4f to .18f, -.7f to .7f, -1.3f to .83f), lerp(shirt, Color.White, .2f), false)
    if (character.name == "Vegeta") {
        garment(listOf(-1.65f to .05f, -.6f to .16f, .6f to .16f, 1.65f to .05f, 1.65f to .55f,
            1.1f to .86f, -1.1f to .86f, -1.65f to .55f), character.accent)
        garment(listOf(-1.2f to .58f, 1.2f to .58f, 1.1f to .86f, -1.1f to .86f), Color(0xFFD4B571))
        for (y in listOf(.65f, .73f, .81f)) drawLine(ink.copy(alpha = .5f), torso(-1.1f, y), torso(1.1f, y), stroke * .12f)
        drawLine(ink.copy(alpha = .4f), torso(0f, .23f), torso(0f, .53f), stroke * .13f)
    } else if (character.name == "Trunks") {
        garment(listOf(-.6f to 0f, .6f to 0f, .7f to .9f, -.7f to .9f), character.accent)
        for (side in listOf(-1f, 1f)) {
            garment(listOf(side * .6f to 0f, side * 1.2f to .12f, side * .8f to .4f), lerp(shirt, Color.White, .35f))
            drawLine(ink, torso(side * 1.2f, .45f), torso(side * 1.7f, .45f), stroke * .16f)
        }
    } else if (character.name != "Tien") {
        garment(listOf(-1f to 0f, 1f to 0f, 0f to .5f), character.accent)
        garment(listOf(-.6f to 0f, .6f to 0f, 0f to .23f), character.skin)
        drawLine(ink.copy(alpha = .5f), torso(.15f, .52f), torso(-.9f, .9f), stroke * .16f)
    } else {
        for (side in listOf(-1f, 1f)) {
            drawLine(ink.copy(alpha = .55f), torso(side * .25f, .33f), torso(side * 1.4f, .27f), stroke * .15f)
            drawLine(ink.copy(alpha = .35f), torso(side * .25f, .55f), torso(side * .8f, .6f), stroke * .13f)
        }
    }
    garment(listOf(-1.4f to .9f, 1.4f to .9f, 1.4f to 1.04f, -1.4f to 1.04f), character.accent)
    garment(listOf(.5f to .96f, 1f to 1f, 1.6f to 1.3f, .8f to 1.22f), character.accent)
    val armColor = when (character.name) {
        "Vegeta", "Trunks" -> character.outfit
        else -> character.skin
    }
    limb(pose.neck, pose.leftElbow, pose.leftHand, armColor, stroke * 1.9f)
    limb(pose.neck, pose.rightElbow, pose.rightHand, armColor, stroke * 1.9f)
    for (hand in listOf(pose.leftHand, pose.rightHand)) {
        val p = hand.scaled()
        val glove = if (character.name == "Vegeta") character.accent else character.skin
        drawCircle(ink, stroke * .8f, p)
        drawCircle(glove, stroke * .68f, p)
        for (finger in -1..1) drawLine(ink.copy(alpha = .45f), p + Offset(finger * stroke * .27f, 0f),
            p + Offset(finger * stroke * .27f, stroke * .4f), stroke * .09f)
    }
    if (character.name == "Piccolo") {
        for (elbow in listOfNotNull(pose.leftElbow, pose.rightElbow)) {
            val p = elbow.scaled()
            drawOval(Color(0xFFCE8994), p - Offset(stroke * .55f, stroke * .8f), Size(stroke * 1.1f, stroke * 1.6f))
            for (i in -1..1) drawLine(ink.copy(alpha = .55f), p + Offset(-stroke * .45f, i * stroke * .4f), p + Offset(stroke * .45f, i * stroke * .4f), stroke * .12f)
        }
    }

    val head = pose.head.scaled()
    val radius = size.minDimension * .078f
    fun point(x: Float, y: Float) = head + Offset(x * radius, y * radius)
    for (side in listOf(-1f, 1f)) {
        drawCircle(ink, radius * .27f, point(side * .92f, .1f))
        drawCircle(character.skin, radius * .21f, point(side * .92f, .1f))
    }
    polygon(listOf(point(-.85f, -.65f), point(0f, -1f), point(.85f, -.65f), point(.9f, .35f),
        point(.5f, .82f), point(0f, 1f), point(-.5f, .82f), point(-.9f, .35f)), character.skin)
    polygon(listOf(point(.6f, -.65f), point(.9f, .35f), point(.5f, .82f), point(0f, 1f), point(.25f, .55f)),
        lerp(character.skin, Color(0xFF8C4B36), .25f), false)
    if (character.hairStyle != 0) {
        val silhouette = when (character.hairStyle) {
            2 -> listOf(-1f to .1f, -1.2f to -1.1f, -.6f to -.8f, 0f to -2f, .6f to -.8f, 1.2f to -1.1f, 1f to .1f, .5f to -.6f, 0f to -.2f, -.5f to -.6f)
            3 -> listOf(-1.1f to .5f, -1.15f to -.6f, -.6f to -1.15f, .5f to -1.15f, 1.1f to -.5f, 1.1f to .5f, .4f to -.55f, 0f to -.8f, -.4f to -.55f)
            else -> listOf(-1f to .1f, -1.7f to -.5f, -1f to -.65f, -1.5f to -1.3f, -.6f to -1f, -.45f to -1.9f, .2f to -1.1f, 1.2f to -1.65f, .9f to -.7f, 1.5f to -.55f, .8f to .1f, .3f to -.5f, -.2f to -.2f, -.6f to -.5f)
        }
        drawPath(Path().apply {
            silhouette.forEachIndexed { index, (x, y) ->
                val p = point(x, y)
                if (index == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
            close()
        }, character.hair)
        if (character.hairStyle == 3) {
            for (side in listOf(-1f, 1f)) {
                drawLine(lerp(character.hair, Color.White, .45f), point(side * .15f, -.95f), point(side * .8f, -.05f), stroke * .18f, StrokeCap.Round)
                drawLine(ink.copy(alpha = .5f), point(side * .35f, -.95f), point(side * .95f, .2f), stroke * .12f)
            }
        } else {
            drawLine(Color(0xFF435065), point(-.35f, -1.35f), point(-.2f, -.65f), stroke * .19f, StrokeCap.Round)
            drawLine(Color(0xFF435065), point(.8f, -1.05f), point(.35f, -.55f), stroke * .16f, StrokeCap.Round)
        }
    }
    for (side in listOf(-1f, 1f)) {
        polygon(listOf(point(side * .15f, .09f), point(side * .65f, -.04f), point(side * .58f, .3f), point(side * .18f, .26f)), Color.White)
        drawCircle(ink, radius * .105f, point(side * .32f, .16f))
        drawLine(ink, point(side * .7f, -.14f), point(side * .13f, .02f), stroke * .27f)
    }
    drawLine(lerp(character.skin, ink, .5f), point(0f, .18f), point(-.09f, .43f), stroke * .12f)
    drawLine(ink, point(-.23f, .64f), point(.23f, .61f), stroke * .14f)
    when (character.name) {
        "Krillin" -> for (row in 0..2) for (column in 0..1) {
            drawCircle(ink, radius * .07f, point(-.2f + column * .4f, -.7f + row * .2f))
        }
        "Tien" -> {
            drawCircle(Color.White, radius * .22f, point(0f, -.5f))
            drawCircle(ink, radius * .1f, point(0f, -.5f))
        }
        "Piccolo" -> for (side in listOf(-1f, 1f)) {
            drawLine(character.skin, point(side * .45f, -.7f), point(side * .75f, -1.4f), stroke * .3f, StrokeCap.Round)
        }
    }
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
