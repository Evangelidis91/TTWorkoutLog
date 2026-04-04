package com.evangelidisapps.ttworkoutlog.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evangelidisapps.ttworkoutlog.data.model.BodyMeasurement
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── Metric definitions ────────────────────────────────────────────────────────

enum class ChartMetric(val label: String, val unitSuffix: (String) -> String) {
    WEIGHT("Weight", { u -> u }),
    BODY_FAT("Body Fat", { "%" }),
    MUSCLE_MASS("Muscle", { u -> u }),
    BMI("BMI", { "" }),
    WAIST("Waist", { "cm" }),
    HIP("Hip", { "cm" }),
    CHEST("Chest", { "cm" }),
    ARM("Arm", { "cm" }),
    THIGH("Thigh", { "cm" })
}

fun BodyMeasurement.valueFor(metric: ChartMetric, weightUnit: String): Double? = when (metric) {
    ChartMetric.WEIGHT -> weightKg?.let { if (weightUnit == "lbs") it * 2.20462 else it }
    ChartMetric.BODY_FAT -> bodyFatPercent
    ChartMetric.MUSCLE_MASS -> muscleMassKg?.let { if (weightUnit == "lbs") it * 2.20462 else it }
    ChartMetric.BMI -> bmi
    ChartMetric.WAIST -> waistCm
    ChartMetric.HIP -> hipCm
    ChartMetric.CHEST -> chestCm
    ChartMetric.ARM -> armCm
    ChartMetric.THIGH -> thighCm
}

// ── Progress charts card ──────────────────────────────────────────────────────

@Composable
fun ProgressChartsCard(
    measurements: List<BodyMeasurement>,  // newest-first order
    weightUnit: String,
    dateFormat: String
) {
    // Sort oldest→newest for charting
    val sorted = remember(measurements) { measurements.sortedBy { it.date } }

    // Find which metrics have ≥ 2 data points
    val availableMetrics = remember(sorted) {
        ChartMetric.entries.filter { metric ->
            sorted.count { it.valueFor(metric, weightUnit) != null } >= 2
        }
    }

    if (availableMetrics.isEmpty()) return

    var selected by remember(availableMetrics) { mutableStateOf(availableMetrics.first()) }

    // Ensure selected metric is still available after measurements change
    LaunchedEffect(availableMetrics) {
        if (selected !in availableMetrics) selected = availableMetrics.first()
    }

    val points = remember(sorted, selected, weightUnit) {
        sorted.mapNotNull { m ->
            val v = m.valueFor(selected, weightUnit) ?: return@mapNotNull null
            m.date to v.toFloat()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Progress",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (points.size >= 2) {
                    val first = points.first().second
                    val last = points.last().second
                    val delta = last - first
                    val sign = if (delta >= 0) "+" else ""
                    val unit = selected.unitSuffix(weightUnit)
                    Text(
                        "$sign${"%.1f".format(delta)} $unit".trim(),
                        style = MaterialTheme.typography.labelMedium,
                        color = when (selected) {
                            ChartMetric.WEIGHT, ChartMetric.BODY_FAT, ChartMetric.BMI,
                            ChartMetric.WAIST, ChartMetric.HIP ->
                                if (delta <= 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            else ->
                                if (delta >= 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Metric selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableMetrics.forEach { metric ->
                    FilterChip(
                        selected = metric == selected,
                        onClick = { selected = metric },
                        label = { Text(metric.label, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Chart
            LineChart(
                points = points,
                unit = selected.unitSuffix(weightUnit),
                dateFormat = dateFormat,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    }
}

// ── Canvas line chart ─────────────────────────────────────────────────────────

@Composable
fun LineChart(
    points: List<Pair<String, Float>>,
    unit: String,
    dateFormat: String,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) return

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(800))
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    val labelStyle = TextStyle(
        fontSize = 9.sp,
        color = onSurfaceVariantColor
    )

    val values = points.map { it.second }
    val minVal = values.min()
    val maxVal = values.max()
    val range = (maxVal - minVal).coerceAtLeast(0.01f)

    val dates = points.map { it.first }

    Canvas(modifier = modifier) {
        val leftPad = 42.dp.toPx()
        val rightPad = 8.dp.toPx()
        val topPad = 8.dp.toPx()
        val bottomPad = 28.dp.toPx()

        val chartW = size.width - leftPad - rightPad
        val chartH = size.height - topPad - bottomPad

        // Gridlines + Y labels
        val gridCount = 4
        for (i in 0..gridCount) {
            val fraction = i.toFloat() / gridCount
            val y = topPad + chartH * (1f - fraction)
            val value = minVal + range * fraction
            drawLine(
                color = surfaceVariantColor,
                start = Offset(leftPad, y),
                end = Offset(leftPad + chartW, y),
                strokeWidth = 1.dp.toPx()
            )
            val yLabel = formatChartValue(value, unit)
            val measured = textMeasurer.measure(yLabel, labelStyle)
            drawText(
                textMeasurer,
                yLabel,
                Offset(leftPad - measured.size.width - 4.dp.toPx(), y - measured.size.height / 2f),
                style = labelStyle
            )
        }

        // Build animated path
        val xStep = if (points.size > 1) chartW / (points.size - 1).toFloat() else chartW
        val prog = animProgress.value

        val pathPoints = points.mapIndexed { i, (_, v) ->
            val x = leftPad + i * xStep
            val y = topPad + chartH * (1f - (v - minVal) / range)
            Offset(x, y)
        }

        // Clipped path up to animProgress
        val animatedCount = (prog * (pathPoints.size - 1)).toInt() + 1
        val animatedPts = pathPoints.take(animatedCount.coerceAtLeast(1))

        // Fill area
        if (animatedPts.size >= 2) {
            val fillPath = buildSmoothPath(animatedPts)
            fillPath.lineTo(animatedPts.last().x, topPad + chartH)
            fillPath.lineTo(animatedPts.first().x, topPad + chartH)
            fillPath.close()
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent),
                    startY = topPad,
                    endY = topPad + chartH
                )
            )
        }

        // Stroke line
        if (animatedPts.size >= 2) {
            drawPath(
                path = buildSmoothPath(animatedPts),
                color = primaryColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Dots
        animatedPts.forEach { pt ->
            drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = pt)
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = pt)
        }

        // X-axis date labels (first, middle, last)
        val labelIndices = when {
            points.size <= 3 -> points.indices.toList()
            else -> listOf(0, points.size / 2, points.size - 1)
        }
        labelIndices.forEach { i ->
            val x = leftPad + i * xStep
            val dateStr = formatDateShort(dates[i], dateFormat)
            val measured = textMeasurer.measure(dateStr, labelStyle)
            val labelX = (x - measured.size.width / 2f).coerceIn(0f, size.width - measured.size.width)
            drawText(
                textMeasurer,
                dateStr,
                Offset(labelX, topPad + chartH + 6.dp.toPx()),
                style = labelStyle
            )
        }
    }
}

private fun buildSmoothPath(pts: List<Offset>): Path {
    val path = Path()
    if (pts.isEmpty()) return path
    path.moveTo(pts[0].x, pts[0].y)
    for (i in 1 until pts.size) {
        val prev = pts[i - 1]
        val cur = pts[i]
        val cx1 = (prev.x + cur.x) / 2f
        path.cubicTo(cx1, prev.y, cx1, cur.y, cur.x, cur.y)
    }
    return path
}

private fun DrawScope.formatChartValue(value: Float, unit: String): String {
    return if (value == value.toLong().toFloat()) "${value.toLong()}$unit"
    else "${"%.1f".format(value)}$unit"
}

private fun formatDateShort(isoDate: String, dateFormat: String): String =
    runCatching {
        val date = LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE)
        // Always show as "d MMM" (e.g. "5 Apr") for chart clarity
        date.format(DateTimeFormatter.ofPattern("d MMM"))
    }.getOrElse { isoDate.takeLast(5) }
