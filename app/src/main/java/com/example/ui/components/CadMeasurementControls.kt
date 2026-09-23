package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DimensionType
import com.example.model.NozzleFacing

@Composable
fun LineMeasurementDock(
    currentStartX: Float,
    currentStartY: Float,
    onAddLine: (lengthMm: Float, angleDeg: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var lengthText by remember { mutableStateOf("300") }
    var selectedDirectionAngle by remember { mutableFloatStateOf(0f) }
    var customAngleText by remember { mutableStateOf("0") }

    val presetLengths = listOf(50f, 100f, 150f, 200f, 300f, 400f, 500f, 600f, 800f, 1000f)
    val presetAngles = listOf(0f, 30f, 45f, 60f, 90f, 135f, 180f, 270f)

    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Straighten,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PRECISION LINE DRAFTING",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "Pos: (%.0f, %.0f) mm".format(currentStartX, currentStartY),
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = lengthText,
                    onValueChange = { lengthText = it },
                    label = { Text("Length (mm)", fontSize = 10.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF38BDF8),
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier
                        .width(115.dp)
                        .height(50.dp)
                        .testTag("line_length_input")
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetLengths.forEach { p ->
                        FilterChip(
                            selected = lengthText == p.toInt().toString(),
                            onClick = { lengthText = p.toInt().toString() },
                            label = { Text("%.0f".format(p), fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customAngleText,
                    onValueChange = {
                        customAngleText = it
                        it.toFloatOrNull()?.let { ang -> selectedDirectionAngle = ang }
                    },
                    label = { Text("Angle (°)", fontSize = 10.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFFF59E0B),
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier
                        .width(95.dp)
                        .height(50.dp)
                        .testTag("line_angle_input")
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetAngles.forEach { a ->
                        FilterChip(
                            selected = selectedDirectionAngle == a,
                            onClick = {
                                selectedDirectionAngle = a
                                customAngleText = a.toInt().toString()
                            },
                            label = { Text("${a.toInt()}°", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD97706),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        val len = lengthText.toFloatOrNull() ?: 300f
                        val ang = customAngleText.toFloatOrNull() ?: selectedDirectionAngle
                        onAddLine(len, ang)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp).testTag("confirm_add_line_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Line", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RectangleMeasurementDock(
    currentX: Float,
    currentY: Float,
    onAddRect: (width: Float, height: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var widthText by remember { mutableStateOf("600") }
    var heightText by remember { mutableStateOf("400") }

    val presetSizes = listOf(Pair(300f, 200f), Pair(400f, 300f), Pair(600f, 400f), Pair(800f, 500f), Pair(1000f, 600f))

    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RECTANGLE / PLATE DRAFTING",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Corner: (%.0f, %.0f) mm".format(currentX, currentY),
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = widthText,
                    onValueChange = { widthText = it },
                    label = { Text("Width mm", fontSize = 10.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier.width(105.dp).height(50.dp)
                )

                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it },
                    label = { Text("Height mm", fontSize = 10.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier.width(105.dp).height(50.dp)
                )

                Row(
                    modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetSizes.forEach { (w, h) ->
                        FilterChip(
                            selected = widthText == w.toInt().toString() && heightText == h.toInt().toString(),
                            onClick = {
                                widthText = w.toInt().toString()
                                heightText = h.toInt().toString()
                            },
                            label = { Text("${w.toInt()}x${h.toInt()}", fontSize = 9.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        val w = widthText.toFloatOrNull() ?: 600f
                        val h = heightText.toFloatOrNull() ?: 400f
                        onAddRect(w, h)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text("Add Plate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CircleMeasurementDock(
    currentX: Float,
    currentY: Float,
    onAddCircle: (diameterMm: Float, isPcd: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var diameterText by remember { mutableStateOf("400") }
    var isPcd by remember { mutableStateOf(false) }

    val presetDias = listOf(100f, 150f, 200f, 250f, 300f, 400f, 500f, 600f, 800f, 1000f)

    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "CIRCLE / FLANGE PCD DRAFTING",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Center: (%.0f, %.0f) mm".format(currentX, currentY),
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = diameterText,
                    onValueChange = { diameterText = it },
                    label = { Text("Dia Ø (mm)", fontSize = 10.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier.width(115.dp).height(50.dp)
                )

                FilterChip(
                    selected = isPcd,
                    onClick = { isPcd = !isPcd },
                    label = { Text("PCD Hole Line", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF10B981),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF34D399)
                    ),
                    modifier = Modifier.height(32.dp)
                )

                Row(
                    modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetDias.forEach { d ->
                        FilterChip(
                            selected = diameterText == d.toInt().toString(),
                            onClick = { diameterText = d.toInt().toString() },
                            label = { Text("Ø${d.toInt()}", fontSize = 9.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        val d = diameterText.toFloatOrNull() ?: 400f
                        onAddCircle(d, isPcd)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text("Add Circle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun NozzleStampDock(
    currentX: Float,
    currentY: Float,
    onAddNozzle: (sizeNb: Int, rating: String, facing: NozzleFacing) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedNb by remember { mutableIntStateOf(50) }
    var selectedRating by remember { mutableStateOf("150# RF") }
    var selectedFacing by remember { mutableStateOf(NozzleFacing.NORTH) }

    val nbOptions = listOf(15, 20, 25, 40, 50, 65, 80, 100, 150, 200, 250, 300)
    val ratings = listOf("150# RF", "300# RF", "PN 16", "Table D")

    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7)),
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "NOZZLE & FLANGE STAMP (ANSI/ASME B16.5)",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Loc: (%.0f, %.0f) mm".format(currentX, currentY),
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                nbOptions.forEach { nb ->
                    FilterChip(
                        selected = selectedNb == nb,
                        onClick = { selectedNb = nb },
                        label = { Text("${nb} NB", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF38BDF8)
                        ),
                        modifier = Modifier.height(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ratings.forEach { r ->
                        FilterChip(
                            selected = selectedRating == r,
                            onClick = { selectedRating = r },
                            label = { Text(r, fontSize = 9.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                Button(
                    onClick = { onAddNozzle(selectedNb, selectedRating, selectedFacing) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stamp Nozzle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DimensionMeasurementDock(
    startPoint: androidx.compose.ui.geometry.Offset?,
    endPoint: androidx.compose.ui.geometry.Offset?,
    onPlaceDimension: (type: DimensionType, offsetDistance: Float, prefix: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var dimType by remember { mutableStateOf(DimensionType.LINEAR_VERTICAL) }
    var prefixText by remember { mutableStateOf("") }
    var offsetDist by remember { mutableFloatStateOf(-60f) }

    val dist = if (startPoint != null && endPoint != null) {
        val dx = endPoint.x - startPoint.x
        val dy = endPoint.y - startPoint.y
        when (dimType) {
            DimensionType.LINEAR_VERTICAL -> kotlin.math.abs(dy)
            DimensionType.LINEAR_HORIZONTAL -> kotlin.math.abs(dx)
            else -> kotlin.math.sqrt(dx * dx + dy * dy)
        }
    } else 0f

    LaunchedEffect(startPoint, endPoint) {
        if (startPoint != null && endPoint != null) {
            val dx = kotlin.math.abs(endPoint.x - startPoint.x)
            val dy = kotlin.math.abs(endPoint.y - startPoint.y)
            dimType = if (dy >= dx) DimensionType.LINEAR_VERTICAL else DimensionType.LINEAR_HORIZONTAL
        }
    }

    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Straighten,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BLUEPRINT DIMENSION / SHOW SIZE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (dist > 0f) {
                    Surface(
                        color = Color(0xFFB91C1C),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "$prefixText%.0f mm".format(dist),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Click/drag from point A to B",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = dimType == DimensionType.LINEAR_VERTICAL,
                        onClick = { dimType = DimensionType.LINEAR_VERTICAL },
                        label = { Text("↕ Vertical", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF4444),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.height(30.dp)
                    )
                    FilterChip(
                        selected = dimType == DimensionType.LINEAR_HORIZONTAL,
                        onClick = { dimType = DimensionType.LINEAR_HORIZONTAL },
                        label = { Text("↔ Horizontal", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF4444),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.height(30.dp)
                    )
                    FilterChip(
                        selected = dimType == DimensionType.ALIGNED,
                        onClick = { dimType = DimensionType.ALIGNED },
                        label = { Text("⤢ Aligned", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF4444),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.height(30.dp)
                    )
                }

                FilterChip(
                    selected = prefixText == "Ø",
                    onClick = { prefixText = if (prefixText == "Ø") "" else "Ø" },
                    label = { Text("Ø Dia", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF38BDF8)
                    ),
                    modifier = Modifier.height(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Offset:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    listOf(-40f, -80f, -120f, -170f, 40f).forEach { off ->
                        FilterChip(
                            selected = offsetDist == off,
                            onClick = { offsetDist = off },
                            label = { Text("%.0f".format(off), fontSize = 9.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFB91C1C),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        onPlaceDimension(dimType, offsetDist, prefixText)
                    },
                    enabled = dist > 2f,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444),
                        disabledContainerColor = Color(0xFF475569)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp).testTag("confirm_place_dimension_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Show Size", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DishedHeadStampDock(
    currentX: Float,
    currentY: Float,
    onAddDishedHead: (headType: com.example.model.DishedHeadType, diameterMm: Float, straightFaceMm: Float, thicknessMm: Float, isFacingUp: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var diaText by remember { mutableStateOf("1000") }
    var sfText by remember { mutableStateOf("40") }
    var thkText by remember { mutableStateOf("5") }
    var headType by remember { mutableStateOf(com.example.model.DishedHeadType.ELLIPSOIDAL_2_TO_1) }
    var isFacingUp by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("DISHED HEAD STAMP (ASME Sec VIII)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("Center: (%.0f, %.0f)".format(currentX, currentY), color = Color(0xFF94A3B8), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                com.example.model.DishedHeadType.values().forEach { type ->
                    FilterChip(
                        selected = headType == type,
                        onClick = { headType = type },
                        label = { Text(type.displayName.take(12), fontSize = 9.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White, containerColor = Color(0xFF1E293B), labelColor = Color(0xFF94A3B8)),
                        modifier = Modifier.height(26.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = diaText,
                    onValueChange = { diaText = it },
                    label = { Text("Dia OD (mm)", fontSize = 9.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(48.dp)
                )
                OutlinedTextField(
                    value = sfText,
                    onValueChange = { sfText = it },
                    label = { Text("SF (mm)", fontSize = 9.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(0.7f).height(48.dp)
                )
                OutlinedTextField(
                    value = thkText,
                    onValueChange = { thkText = it },
                    label = { Text("Thk (mm)", fontSize = 9.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(0.7f).height(48.dp)
                )
                FilterChip(
                    selected = isFacingUp,
                    onClick = { isFacingUp = !isFacingUp },
                    label = { Text(if (isFacingUp) "Top ↥" else "Bottom ↧", fontSize = 10.sp) },
                    modifier = Modifier.height(30.dp)
                )
                Button(
                    onClick = {
                        val d = diaText.toFloatOrNull() ?: 1000f
                        val sf = sfText.toFloatOrNull() ?: 40f
                        val thk = thkText.toFloatOrNull() ?: 5f
                        onAddDishedHead(headType, d, sf, thk, isFacingUp)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Stamp Head", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SaddleSupportStampDock(
    currentX: Float,
    currentY: Float,
    onAddSaddle: (vesselDiaMm: Float, saddleHeightMm: Float, basePlateWidthMm: Float, webThicknessMm: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var diaText by remember { mutableStateOf("1000") }
    var heightText by remember { mutableStateOf("350") }
    var baseWidthText by remember { mutableStateOf("300") }
    var webThkText by remember { mutableStateOf("10") }

    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text("SADDLE SUPPORT (HORIZONTAL VESSEL)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = diaText,
                    onValueChange = { diaText = it },
                    label = { Text("Vessel OD", fontSize = 9.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(48.dp)
                )
                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it },
                    label = { Text("Height", fontSize = 9.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(48.dp)
                )
                OutlinedTextField(
                    value = baseWidthText,
                    onValueChange = { baseWidthText = it },
                    label = { Text("Base Width", fontSize = 9.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(48.dp)
                )
                Button(
                    onClick = {
                        val d = diaText.toFloatOrNull() ?: 1000f
                        val h = heightText.toFloatOrNull() ?: 350f
                        val b = baseWidthText.toFloatOrNull() ?: 300f
                        val w = webThkText.toFloatOrNull() ?: 10f
                        onAddSaddle(d, h, b, w)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Add Saddle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LegSupportStampDock(
    currentX: Float,
    currentY: Float,
    onAddLeg: (legPipeNb: Int, heightMm: Float, basePadDiaMm: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var pipeNb by remember { mutableIntStateOf(50) }
    var heightText by remember { mutableStateOf("300") }
    var basePadText by remember { mutableStateOf("120") }

    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text("VERTICAL TANK LEG SUPPORT (PIPE / ISMC CHANNEL)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(40, 50, 65, 80, 100).forEach { sz ->
                    FilterChip(
                        selected = pipeNb == sz,
                        onClick = { pipeNb = sz },
                        label = { Text("${sz} NB", fontSize = 10.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        val h = heightText.toFloatOrNull() ?: 300f
                        val bp = basePadText.toFloatOrNull() ?: 120f
                        onAddLeg(pipeNb, h, bp)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Add Leg Support", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ManwayStampDock(
    currentX: Float,
    currentY: Float,
    onAddManway: (manwayNb: Int, projectionMm: Float, hasDavitArm: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var manwayNb by remember { mutableIntStateOf(500) }
    var neckText by remember { mutableStateOf("150") }
    var withDavit by remember { mutableStateOf(true) }

    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text("VESSEL MANWAY (INSPECTION PORT / DAVIT)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(400, 450, 500, 600).forEach { nb ->
                    FilterChip(
                        selected = manwayNb == nb,
                        onClick = { manwayNb = nb },
                        label = { Text("${nb} NB", fontSize = 10.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }
                FilterChip(
                    selected = withDavit,
                    onClick = { withDavit = !withDavit },
                    label = { Text(if (withDavit) "+ Davit Arm" else "No Davit", fontSize = 10.sp) },
                    modifier = Modifier.height(30.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        val n = neckText.toFloatOrNull() ?: 150f
                        onAddManway(manwayNb, n, withDavit)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Add Manway", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ValveStampDock(
    currentX: Float,
    currentY: Float,
    onAddValve: (valveType: com.example.model.ValveType, sizeNb: Int, lengthMm: Float, isVertical: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var valveType by remember { mutableStateOf(com.example.model.ValveType.BALL_VALVE) }
    var sizeNb by remember { mutableIntStateOf(50) }
    var isVertical by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text("PIPING VALVE SYMBOL (GATE / GLOBE / BALL / BUTTERFLY)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                com.example.model.ValveType.values().forEach { vt ->
                    FilterChip(
                        selected = valveType == vt,
                        onClick = { valveType = vt },
                        label = { Text(vt.displayName, fontSize = 9.sp) },
                        modifier = Modifier.height(26.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(25, 40, 50, 80, 100, 150).forEach { sz ->
                    FilterChip(
                        selected = sizeNb == sz,
                        onClick = { sizeNb = sz },
                        label = { Text("${sz} NB", fontSize = 10.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }
                FilterChip(
                    selected = isVertical,
                    onClick = { isVertical = !isVertical },
                    label = { Text(if (isVertical) "Vert" else "Horiz", fontSize = 10.sp) },
                    modifier = Modifier.height(28.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { onAddValve(valveType, sizeNb, 80f, isVertical) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Add Valve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AgitatorStampDock(
    currentX: Float,
    currentY: Float,
    onAddAgitator: (motorKw: Float, shaftLengthMm: Float, impellerDiaMm: Float, rpm: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var motorKwText by remember { mutableStateOf("1.5") }
    var shaftLenText by remember { mutableStateOf("500") }
    var impellerSpanText by remember { mutableStateOf("250") }
    var rpmText by remember { mutableStateOf("1440") }

    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text("REACTOR AGITATOR DRIVE & IMPELLER", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = motorKwText,
                    onValueChange = { motorKwText = it },
                    label = { Text("Motor kW", fontSize = 9.sp) },
                    modifier = Modifier.weight(1f).height(48.dp)
                )
                OutlinedTextField(
                    value = shaftLenText,
                    onValueChange = { shaftLenText = it },
                    label = { Text("Shaft L", fontSize = 9.sp) },
                    modifier = Modifier.weight(1f).height(48.dp)
                )
                OutlinedTextField(
                    value = impellerSpanText,
                    onValueChange = { impellerSpanText = it },
                    label = { Text("Impeller Ø", fontSize = 9.sp) },
                    modifier = Modifier.weight(1f).height(48.dp)
                )
                Button(
                    onClick = {
                        val kw = motorKwText.toFloatOrNull() ?: 1.5f
                        val sl = shaftLenText.toFloatOrNull() ?: 500f
                        val ispan = impellerSpanText.toFloatOrNull() ?: 250f
                        val r = rpmText.toIntOrNull() ?: 1440
                        onAddAgitator(kw, sl, ispan, r)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Add Agitator", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BomItemBubbleDock(
    currentX: Float,
    currentY: Float,
    onAddBomItem: (itemNo: Int, desc: String, qty: Int, material: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var itemNoText by remember { mutableStateOf("1") }
    var descText by remember { mutableStateOf("Main Shell Plate") }
    var qtyText by remember { mutableStateOf("1") }
    var materialText by remember { mutableStateOf("SS 304") }

    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7)),
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text("BILL OF MATERIALS (BOM) BALLOON #", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = itemNoText,
                    onValueChange = { itemNoText = it },
                    label = { Text("Item #", fontSize = 9.sp) },
                    modifier = Modifier.width(70.dp).height(48.dp)
                )
                OutlinedTextField(
                    value = descText,
                    onValueChange = { descText = it },
                    label = { Text("Description", fontSize = 9.sp) },
                    modifier = Modifier.weight(1f).height(48.dp)
                )
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Qty", fontSize = 9.sp) },
                    modifier = Modifier.width(60.dp).height(48.dp)
                )
                Button(
                    onClick = {
                        val no = itemNoText.toIntOrNull() ?: 1
                        val q = qtyText.toIntOrNull() ?: 1
                        onAddBomItem(no, descText, q, materialText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Place Balloon", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

