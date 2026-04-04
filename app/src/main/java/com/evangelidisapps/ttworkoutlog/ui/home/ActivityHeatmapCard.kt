package com.evangelidisapps.ttworkoutlog.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

private const val WEEKS = 16          // how many weeks to show
private const val DAYS_IN_WEEK = 7

@Composable
fun ActivityHeatmapCard(streakData: StreakData) {
    val primary = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Streak badges ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Activity",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Last $WEEKS weeks",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StreakBadge(
                        label = "Current",
                        value = "${streakData.currentStreak}d",
                        highlight = streakData.currentStreak > 0
                    )
                    StreakBadge(
                        label = "Best",
                        value = "${streakData.longestStreak}d",
                        highlight = false
                    )
                    StreakBadge(
                        label = "This week",
                        value = "${streakData.thisWeekCount}",
                        highlight = streakData.thisWeekCount > 0
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Heatmap canvas ────────────────────────────────────────────────
            val labelStyle = TextStyle(fontSize = 9.sp, color = onSurfaceVariant)
            val monthStyle = TextStyle(fontSize = 9.sp, color = onSurfaceVariant, fontWeight = FontWeight.Medium)

            // Height: top pad for month labels + 7 rows + bottom pad
            val canvasHeight = (12 + 7 * 14 + 4).dp   // ~114dp

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(canvasHeight)
            ) {
                val topPad = 14.dp.toPx()       // space for month labels
                val leftPad = 18.dp.toPx()      // space for day-of-week labels
                val gap = 2.dp.toPx()
                val weeksF = WEEKS.toFloat()
                val cellSize = ((size.width - leftPad) - gap * (weeksF - 1)) / weeksF

                // The grid starts from the Monday WEEKS-1 weeks ago
                val today = LocalDate.now()
                val gridStart = today
                    .with(DayOfWeek.MONDAY)
                    .minusWeeks((WEEKS - 1).toLong())

                var lastMonth = -1

                for (w in 0 until WEEKS) {
                    val weekMonday = gridStart.plusWeeks(w.toLong())
                    val x = leftPad + w * (cellSize + gap)

                    // Month label at start of column if month changed
                    val month = weekMonday.monthValue
                    if (month != lastMonth) {
                        val abbr = weekMonday.month
                            .getDisplayName(JTextStyle.SHORT, Locale.getDefault())
                        val m = textMeasurer.measure(abbr, monthStyle)
                        drawText(textMeasurer, abbr, Offset(x, 0f), style = monthStyle)
                        lastMonth = month
                    }

                    for (d in 0 until DAYS_IN_WEEK) {
                        val date = weekMonday.plusDays(d.toLong())
                        if (date.isAfter(today)) continue  // don't draw future days

                        val y = topPad + d * (cellSize + gap)
                        val count = streakData.countByDate[date] ?: 0
                        val cellColor = when (count) {
                            0 -> surfaceVariant
                            1 -> primary.copy(alpha = 0.30f)
                            2 -> primary.copy(alpha = 0.60f)
                            else -> primary
                        }
                        drawRoundRect(
                            color = cellColor,
                            topLeft = Offset(x, y),
                            size = Size(cellSize, cellSize),
                            cornerRadius = CornerRadius(2.dp.toPx())
                        )
                    }
                }

                // Day-of-week labels: Mon(0), Wed(2), Fri(4) only
                val dayLabels = listOf(0 to "M", 2 to "W", 4 to "F")
                dayLabels.forEach { (d, label) ->
                    val y = topPad + d * (cellSize + gap) + cellSize / 2f
                    val m = textMeasurer.measure(label, labelStyle)
                    drawText(
                        textMeasurer, label,
                        Offset(leftPad - m.size.width - 3.dp.toPx(), y - m.size.height / 2f),
                        style = labelStyle
                    )
                }
            }

            // ── Legend ────────────────────────────────────────────────────────
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Less", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                listOf(
                    surfaceVariant,
                    primary.copy(alpha = 0.30f),
                    primary.copy(alpha = 0.60f),
                    primary
                ).forEach { color ->
                    LegendCell(color = color)
                    Spacer(Modifier.width(2.dp))
                }
                Text("More", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StreakBadge(label: String, value: String, highlight: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendCell(color: Color) {
    val c = color
    Canvas(modifier = Modifier
        .width(10.dp)
        .height(10.dp)
    ) {
        drawRoundRect(
            color = c,
            size = size,
            cornerRadius = CornerRadius(2.dp.toPx())
        )
    }
}
