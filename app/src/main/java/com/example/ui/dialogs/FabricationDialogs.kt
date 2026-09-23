package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*

@Composable
fun SheetMetalUnfoldDialog(
    onDismiss: () -> Unit,
    onStampPatternOntoDrawing: (List<CadEntity>) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Conical Hopper parameters
    var topDia by remember { mutableStateOf("800") }
    var bottomDia by remember { mutableStateOf("200") }
    var height by remember { mutableStateOf("600") }
    var thickness by remember { mutableStateOf("5") }

    // Cylindrical Shell parameters
    var shellInsideDia by remember { mutableStateOf("1000") }
    var shellHeight by remember { mutableStateOf("1200") }
    var shellThickness by remember { mutableStateOf("5") }

    // Dished Head parameters
    var dishDia by remember { mutableStateOf("1000") }
    var dishType by remember { mutableStateOf(DishedHeadType.ELLIPSOIDAL_2_TO_1) }
    var straightFlange by remember { mutableStateOf("40") }
    var dishThickness by remember { mutableStateOf("5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Architecture, contentDescription = null, tint = Color(0xFF38BDF8))
                Text("Fabrication Blank & Flat Pattern Unfolder", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color(0xFF38BDF8)
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Cone / Hopper", fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Shell Rolling", fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                    }
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                        Text("Dished Head", fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                    }
                }

                when (selectedTab) {
                    0 -> {
                        // Cone Flat Pattern
                        val topD = topDia.toFloatOrNull() ?: 800f
                        val botD = bottomDia.toFloatOrNull() ?: 200f
                        val h = height.toFloatOrNull() ?: 600f
                        val thk = thickness.toFloatOrNull() ?: 5f

                        val result = remember(topD, botD, h, thk) {
                            FabricationCalculator.calculateConeFlatPattern(topD, botD, h, thk)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = topDia,
                                onValueChange = { topDia = it },
                                label = { Text("Top Dia D1 (mm)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = bottomDia,
                                onValueChange = { bottomDia = it },
                                label = { Text("Bottom Dia D2 (mm)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = height,
                                onValueChange = { height = it },
                                label = { Text("Height H (mm)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = thickness,
                                onValueChange = { thickness = it },
                                label = { Text("Thickness (mm)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Surface(
                            color = Color(0xFF0B132B),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("FLAT DEVELOPMENT RESULTS:", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("• Slant Length (L): %.1f mm".format(result.slantHeightMm), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                Text("• Outer Radius (R2): %.1f mm".format(result.outerRadiusR2), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                Text("• Inner Radius (R1): %.1f mm".format(result.innerRadiusR1), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                Text("• Included Sector Angle (θ): %.1f°".format(result.sectorAngleDeg), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                                Text("• Blank Chord Span: %.1f mm".format(result.blankChordLengthMm), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                Text("• Raw Sheet Size Req: %.0f × %.0f mm".format(result.blankPlateWidthMm, result.blankPlateHeightMm), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                Text("• SS 304 Weight: %.2f kg".format(result.weightKg), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                val entities = mutableListOf<CadEntity>()
                                val baseX = 2000f
                                val baseY = 800f
                                // Draw unfolded sector arc
                                entities.add(CadArc(cx = baseX, cy = baseY, radius = result.outerRadiusR2, startAngleDeg = 0f, sweepAngleDeg = result.sectorAngleDeg, layerId = "0"))
                                entities.add(CadArc(cx = baseX, cy = baseY, radius = result.innerRadiusR1, startAngleDeg = 0f, sweepAngleDeg = result.sectorAngleDeg, layerId = "0"))
                                entities.add(CadText(x = baseX + 20f, y = baseY - 20f, text = "CONE FLAT DEVELOPMENT (θ = %.1f°)".format(result.sectorAngleDeg), textHeightMm = 20f, isBold = true))
                                entities.add(CadText(x = baseX + 20f, y = baseY + 20f, text = "SS 304 THK ${result.thicknessMm} MM | WT: %.2f KG".format(result.weightKg), textHeightMm = 14f))
                                onStampPatternOntoDrawing(entities)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Stamp Unfolded Layout to CAD Drawing")
                        }
                    }

                    1 -> {
                        // Shell Rolling
                        val id = shellInsideDia.toFloatOrNull() ?: 1000f
                        val h = shellHeight.toFloatOrNull() ?: 1200f
                        val thk = shellThickness.toFloatOrNull() ?: 5f
                        val result = remember(id, h, thk) {
                            FabricationCalculator.calculateShellRollPattern(id, h, thk)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = shellInsideDia,
                                onValueChange = { shellInsideDia = it },
                                label = { Text("Inside Dia (mm)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = shellHeight,
                                onValueChange = { shellHeight = it },
                                label = { Text("Shell Height (mm)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = shellThickness,
                            onValueChange = { shellThickness = it },
                            label = { Text("Plate Thickness (mm)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Surface(
                            color = Color(0xFF0B132B),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("SHELL PLATE ROLLING CUT SIZE:", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("• Mean Dia (MD = ID + T): %.1f mm".format(result.meanDiaMm), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                Text("• Cut Length (π × MD): %.1f mm".format(result.blankCutLengthMm), fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                                Text("• Cut Width (Shell Height): %.1f mm".format(result.blankCutWidthMm), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                Text("• Plate Surface Area: %.2f m²".format(result.blankAreaM2), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                Text("• SS 304 Plate Weight: %.2f kg".format(result.weightKg), fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                val entities = mutableListOf<CadEntity>()
                                val baseX = 2000f
                                val baseY = 1200f
                                entities.add(CadRect(x = baseX, y = baseY, width = result.blankCutLengthMm, height = result.blankCutWidthMm, plateThicknessMm = result.thicknessMm))
                                entities.add(CadDimension(x1 = baseX, y1 = baseY, x2 = baseX + result.blankCutLengthMm, y2 = baseY, offsetDistance = -40f, textOverride = "CUT LENGTH: %.0f".format(result.blankCutLengthMm)))
                                entities.add(CadDimension(x1 = baseX, y1 = baseY, x2 = baseX, y2 = baseY + result.blankCutWidthMm, offsetDistance = -40f, textOverride = "WIDTH: %.0f".format(result.blankCutWidthMm)))
                                entities.add(CadText(x = baseX + 20f, y = baseY + result.blankCutWidthMm + 40f, text = "VESSEL SHELL ROLLING BLANK (ID Ø${result.insideDiaMm.toInt()} MM)", textHeightMm = 20f, isBold = true))
                                onStampPatternOntoDrawing(entities)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Stamp Shell Blank to Drawing")
                        }
                    }

                    2 -> {
                        // Dished Head
                        val od = dishDia.toFloatOrNull() ?: 1000f
                        val sf = straightFlange.toFloatOrNull() ?: 40f
                        val thk = dishThickness.toFloatOrNull() ?: 5f
                        val result = remember(od, dishType, thk, sf) {
                            FabricationCalculator.calculateDishedHeadBlank(od, dishType, thk, sf)
                        }

                        OutlinedTextField(
                            value = dishDia,
                            onValueChange = { dishDia = it },
                            label = { Text("Outside Dia OD (mm)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = straightFlange,
                                onValueChange = { straightFlange = it },
                                label = { Text("Straight Flange SF (mm)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = dishThickness,
                                onValueChange = { dishThickness = it },
                                label = { Text("Thickness (mm)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Surface(
                            color = Color(0xFF0B132B),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("DISHED END ESTIMATED BLANK:", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("• Head Style: ${result.headType.displayName}", fontSize = 11.sp, color = Color.White)
                                Text("• Estimated Circle Blank Dia: Ø %.1f mm".format(result.estimatedBlankDiaMm), fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                                Text("• Blank Disc Weight: %.2f kg".format(result.blankWeightKg), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                Text("• Internal Dish Volume: %.1f Litres".format(result.internalVolumeLiters), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = {
                                val entities = mutableListOf<CadEntity>()
                                val baseX = 2000f
                                val baseY = 1600f
                                entities.add(CadDishedHead(cx = baseX, cy = baseY, diameterMm = result.outsideDiaMm, straightFaceMm = result.straightFlangeMm, dishType = result.headType, thicknessMm = result.thicknessMm))
                                entities.add(CadText(x = baseX - 200f, y = baseY + 80f, text = "${result.headType.displayName} (Ø${result.outsideDiaMm.toInt()} MM)", textHeightMm = 18f, isBold = true))
                                onStampPatternOntoDrawing(entities)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Stamp Dished Head to Drawing")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun BomManagerDialog(
    project: CadProject,
    onDismiss: () -> Unit,
    onStampBomTable: (List<CadEntity>) -> Unit
) {
    val bomItems = remember(project.entities) {
        val existing = project.entities.filterIsInstance<CadBomItem>()
        if (existing.isNotEmpty()) existing else listOf(
            CadBomItem(x = 0f, y = 0f, itemNumber = 1, description = "Main Shell Plate 5mm", qty = 1, material = "SS 304"),
            CadBomItem(x = 0f, y = 0f, itemNumber = 2, description = "Settling Hopper Cone 5mm", qty = 1, material = "SS 304"),
            CadBomItem(x = 0f, y = 0f, itemNumber = 3, description = "Inlet Flanged Nozzle 25 NB", qty = 1, material = "SS 304 / ANSI 150#"),
            CadBomItem(x = 0f, y = 0f, itemNumber = 4, description = "Outlet Nozzle 25 NB", qty = 1, material = "SS 304 / ANSI 150#"),
            CadBomItem(x = 0f, y = 0f, itemNumber = 5, description = "Bottom Drain Nozzle 25 NB", qty = 2, material = "SS 304 / ANSI 150#"),
            CadBomItem(x = 0f, y = 0f, itemNumber = 6, description = "Overflow Interconnecting Pipe", qty = 1, material = "SS 304 25 NB")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ListAlt, contentDescription = null, tint = Color(0xFF10B981))
                Text("Bill of Materials (BOM) Schedule", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("EQUIPMENT FABRICATION PARTS LIST", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(bomItems) { item ->
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF0284C7)) {
                                    Text(
                                        "#${item.itemNumber}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.description, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Mat: ${item.material} | Qty: ${item.qty} Nos", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        val entities = mutableListOf<CadEntity>()
                        val tableX = 1800f
                        val tableY = 200f
                        val rowH = 35f
                        val tableW = 550f

                        // Table header
                        entities.add(CadRect(x = tableX, y = tableY, width = tableW, height = (bomItems.size + 1) * rowH, layerId = "0"))
                        entities.add(CadText(x = tableX + 10f, y = tableY + 24f, text = "BILL OF MATERIALS / PARTS SCHEDULE", textHeightMm = 16f, isBold = true))

                        bomItems.forEachIndexed { idx, item ->
                            val y = tableY + (idx + 1) * rowH
                            entities.add(CadLine(x1 = tableX, y1 = y, x2 = tableX + tableW, y2 = y, strokeType = StrokeType.CONTINUOUS, layerId = "0"))
                            entities.add(CadText(x = tableX + 10f, y = y + 22f, text = "#${item.itemNumber} | ${item.description} | ${item.qty} NOS | ${item.material}", textHeightMm = 12f))
                        }

                        onStampBomTable(entities)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Stamp BOM Table on Drawing")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
