package com.example.ui.canvas

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import kotlin.math.*

@Composable
fun IsometricViewport3D(
    project: CadProject,
    modifier: Modifier = Modifier
) {
    var rotX by remember { mutableFloatStateOf(25f) }
    var rotY by remember { mutableFloatStateOf(-45f) }
    var zoomScale by remember { mutableFloatStateOf(1.1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var isAutoRotating by remember { mutableStateOf(false) }
    var renderModeShaded by remember { mutableStateOf(true) }
    var materialFinish by remember { mutableStateOf("SS 304 2B") }

    val mesh = remember(project.isometricModelType) {
        IsometricEquipmentMeshGenerator.generateMesh(project.isometricModelType)
    }

    LaunchedEffect(isAutoRotating) {
        if (isAutoRotating) {
            while (true) {
                withFrameNanos {
                    rotY = (rotY + 0.5f) % 360f
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B132B))
            .testTag("isometric_viewport_3d")
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (zoom != 1f || pan != Offset.Zero) {
                        zoomScale = (zoomScale * zoom).coerceIn(0.3f, 6f)
                        panOffset += pan
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    rotY = (rotY + dragAmount.x * 0.4f) % 360f
                    rotX = (rotX - dragAmount.y * 0.4f).coerceIn(-85f, 85f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width * 0.5f + panOffset.x
            val cy = size.height * 0.5f + panOffset.y

            draw3DGridPlanes(cx, cy, rotX, rotY, zoomScale)

            // Render Shaded Faces (Back to Front Depth Sort)
            if (renderModeShaded && mesh.faces.isNotEmpty()) {
                val sortedFaces = mesh.faces.map { face ->
                    val transformed = face.vertices.map { project3D(it, rotX, rotY, zoomScale, cx, cy) }
                    val avgZ = transformed.map { it.zDepth }.average().toFloat()
                    Triple(face, transformed, avgZ)
                }.sortedBy { it.third }

                for ((face, transformed, _) in sortedFaces) {
                    val path = Path().apply {
                        moveTo(transformed[0].x, transformed[0].y)
                        for (i in 1 until transformed.size) {
                            lineTo(transformed[i].x, transformed[i].y)
                        }
                        close()
                    }
                    val shadeColor = when (materialFinish) {
                        "SS 316L Mirror" -> face.color.copy(alpha = 0.9f)
                        "Titanium Gold" -> Color(0xFFD97706).copy(alpha = 0.85f)
                        "Bead Blasted" -> Color(0xFF64748B)
                        else -> face.color
                    }
                    drawPath(path, shadeColor)
                }
            }

            // Render 3D Wireframe / Feature Outlines
            for (line in mesh.lines) {
                val p1 = project3D(line.start, rotX, rotY, zoomScale, cx, cy)
                val p2 = project3D(line.end, rotX, rotY, zoomScale, cx, cy)
                drawLine(
                    color = if (renderModeShaded) line.color else Color(0xFF38BDF8),
                    start = Offset(p1.x, p1.y),
                    end = Offset(p2.x, p2.y),
                    strokeWidth = line.strokeWidth * zoomScale.coerceIn(0.8f, 2.5f)
                )
            }
        }

        // Top-Left Model Info Banner
        Surface(
            color = Color(0xDD0F172A),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "3D ISOMETRIC VESSEL ASSEMBLY",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Model: ${project.isometricModelType.displayName}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "RotX: %.0f°  RotY: %.0f°  Zoom: %.1fx".format(rotX, rotY, zoomScale),
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Camera Preset Views (Top Right)
        Surface(
            color = Color(0xDD0F172A),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
        ) {
            Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("CAMERA VIEWS", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = rotX == 25f && rotY == -45f,
                        onClick = { rotX = 25f; rotY = -45f; panOffset = Offset.Zero },
                        label = { Text("ISO-1", fontSize = 9.sp) },
                        modifier = Modifier.height(26.dp)
                    )
                    FilterChip(
                        selected = rotX == 25f && rotY == 135f,
                        onClick = { rotX = 25f; rotY = 135f; panOffset = Offset.Zero },
                        label = { Text("ISO-2", fontSize = 9.sp) },
                        modifier = Modifier.height(26.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = rotX == 0f && rotY == 0f,
                        onClick = { rotX = 0f; rotY = 0f; panOffset = Offset.Zero },
                        label = { Text("Front", fontSize = 9.sp) },
                        modifier = Modifier.height(26.dp)
                    )
                    FilterChip(
                        selected = rotX == 85f && rotY == 0f,
                        onClick = { rotX = 85f; rotY = 0f; panOffset = Offset.Zero },
                        label = { Text("Top", fontSize = 9.sp) },
                        modifier = Modifier.height(26.dp)
                    )
                    FilterChip(
                        selected = rotX == 0f && rotY == 90f,
                        onClick = { rotX = 0f; rotY = 90f; panOffset = Offset.Zero },
                        label = { Text("Right", fontSize = 9.sp) },
                        modifier = Modifier.height(26.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = Color(0xFF334155))

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { zoomScale = (zoomScale * 1.25f).coerceIn(0.3f, 6f) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { zoomScale = (zoomScale * 0.8f).coerceIn(0.3f, 6f) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = {
                            rotX = 25f
                            rotY = -45f
                            zoomScale = 1.1f
                            panOffset = Offset.Zero
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Bottom Controls Bar
        Surface(
            color = Color(0xDD0F172A),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Auto Rotate
                FilledIconToggleButton(
                    checked = isAutoRotating,
                    onCheckedChange = { isAutoRotating = it },
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        checkedContainerColor = Color(0xFF10B981),
                        containerColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isAutoRotating) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Auto Rotate",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Shaded vs Wireframe
                FilledIconToggleButton(
                    checked = renderModeShaded,
                    onCheckedChange = { renderModeShaded = it },
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        checkedContainerColor = Color(0xFF0284C7),
                        containerColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (renderModeShaded) Icons.Default.Visibility else Icons.Default.Layers,
                        contentDescription = "Toggle Shading",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Material selection
                listOf("SS 304 2B", "SS 316L Mirror", "Bead Blasted", "Titanium Gold").forEach { mat ->
                    FilterChip(
                        selected = materialFinish == mat,
                        onClick = { materialFinish = mat },
                        label = { Text(mat, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.height(30.dp)
                    )
                }
            }
        }
    }
}

private data class ProjectedPoint(val x: Float, val y: Float, val zDepth: Float)

private fun project3D(
    pt: Point3D,
    rotXDeg: Float,
    rotYDeg: Float,
    scale: Float,
    cx: Float,
    cy: Float
): ProjectedPoint {
    val radX = Math.toRadians(rotXDeg.toDouble())
    val radY = Math.toRadians(rotYDeg.toDouble())

    val cosY = cos(radY)
    val sinY = sin(radY)
    val x1 = pt.x * cosY + pt.z * sinY
    val y1 = pt.y.toDouble()
    val z1 = -pt.x * sinY + pt.z * cosY

    val cosX = cos(radX)
    val sinX = sin(radX)
    val x2 = x1
    val y2 = y1 * cosX - z1 * sinX
    val z2 = y1 * sinX + z1 * cosX

    val sx = (cx + x2 * scale).toFloat()
    val sy = (cy - y2 * scale).toFloat()
    return ProjectedPoint(sx, sy, z2.toFloat())
}

private fun DrawScope.draw3DGridPlanes(cx: Float, cy: Float, rotX: Float, rotY: Float, scale: Float) {
    val gridStep = 50f
    val gridExtent = 250f
    val floorColor = Color(0x18FFFFFF)

    var g = -gridExtent
    while (g <= gridExtent) {
        val p1 = project3D(Point3D(g, -150f, -gridExtent), rotX, rotY, scale, cx, cy)
        val p2 = project3D(Point3D(g, -150f, gridExtent), rotX, rotY, scale, cx, cy)
        drawLine(floorColor, Offset(p1.x, p1.y), Offset(p2.x, p2.y), strokeWidth = 1f)

        val p3 = project3D(Point3D(-gridExtent, -150f, g), rotX, rotY, scale, cx, cy)
        val p4 = project3D(Point3D(gridExtent, -150f, g), rotX, rotY, scale, cx, cy)
        drawLine(floorColor, Offset(p3.x, p3.y), Offset(p4.x, p4.y), strokeWidth = 1f)
        g += gridStep
    }

    // 3D Axis Indicator
    val origin = project3D(Point3D(0f, 0f, 0f), rotX, rotY, scale, cx, cy)
    val xAxis = project3D(Point3D(100f, 0f, 0f), rotX, rotY, scale, cx, cy)
    val yAxis = project3D(Point3D(0f, 100f, 0f), rotX, rotY, scale, cx, cy)
    val zAxis = project3D(Point3D(0f, 0f, 100f), rotX, rotY, scale, cx, cy)

    drawLine(Color(0xFFEF4444), Offset(origin.x, origin.y), Offset(xAxis.x, xAxis.y), strokeWidth = 3f) // X Red
    drawLine(Color(0xFF10B981), Offset(origin.x, origin.y), Offset(yAxis.x, yAxis.y), strokeWidth = 3f) // Y Green
    drawLine(Color(0xFF38BDF8), Offset(origin.x, origin.y), Offset(zAxis.x, zAxis.y), strokeWidth = 3f) // Z Blue
}
