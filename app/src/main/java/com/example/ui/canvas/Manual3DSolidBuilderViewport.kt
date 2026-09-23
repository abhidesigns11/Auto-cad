package com.example.ui.canvas

import androidx.compose.runtime.withFrameNanos
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
fun Manual3DSolidBuilderViewport(
    parts: List<Solid3DPart>,
    active3DTool: Cad3DTool,
    selectedPartId: String?,
    onSelectPart: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var rotX by remember { mutableFloatStateOf(25f) }
    var rotY by remember { mutableFloatStateOf(-40f) }
    var zoomScale by remember { mutableFloatStateOf(0.9f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var isAutoRotating by remember { mutableStateOf(false) }
    var renderModeShaded by remember { mutableStateOf(true) }

    val mesh = remember(parts) {
        Manual3DSolidEngine.generateMeshFromParts(parts)
    }

    LaunchedEffect(isAutoRotating) {
        if (isAutoRotating) {
            while (true) {
                withFrameNanos {
                    rotY = (rotY + 0.4f) % 360f
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .testTag("manual_3d_builder_viewport")
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (zoom != 1f || pan != Offset.Zero) {
                        zoomScale = (zoomScale * zoom).coerceIn(0.2f, 6f)
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

            draw3DGridFloor(cx, cy, rotX, rotY, zoomScale)

            if (renderModeShaded && mesh.faces.isNotEmpty()) {
                val sortedFaces = mesh.faces.map { face ->
                    val transformed = face.vertices.map { project3DPoint(it, rotX, rotY, zoomScale, cx, cy) }
                    val avgZ = transformed.map { it.zDepth }.average().toFloat()
                    Triple(face, transformed, avgZ)
                }.sortedBy { it.third }

                for ((face, transformed, _) in sortedFaces) {
                    val path = Path().apply {
                        if (transformed.isNotEmpty()) {
                            moveTo(transformed[0].x, transformed[0].y)
                            for (i in 1 until transformed.size) {
                                lineTo(transformed[i].x, transformed[i].y)
                            }
                            close()
                        }
                    }
                    drawPath(path, face.color)
                }
            }

            for (line in mesh.lines) {
                val p1 = project3DPoint(line.start, rotX, rotY, zoomScale, cx, cy)
                val p2 = project3DPoint(line.end, rotX, rotY, zoomScale, cx, cy)
                drawLine(
                    color = if (renderModeShaded) line.color else Color(0xFF38BDF8),
                    start = Offset(p1.x, p1.y),
                    end = Offset(p2.x, p2.y),
                    strokeWidth = line.strokeWidth * zoomScale.coerceIn(0.7f, 2.5f)
                )
            }
        }

        // Viewport Header Specs
        Surface(
            color = Color(0xDD0F172A),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Widgets, contentDescription = null, tint = Color(0xFF007AFF), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "3D SOLID CSG MODELER",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "${parts.size} Solid Components in Tree",
                    color = Color.White,
                    fontSize = 10.sp
                )
                Text(
                    text = "Rot: (%.0f°, %.0f°) Zoom: %.1fx".format(rotX, rotY, zoomScale),
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Camera Views Preset Bar (Top Right)
        Surface(
            color = Color(0xDD0F172A),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
        ) {
            Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { zoomScale = (zoomScale * 1.25f).coerceIn(0.2f, 6f) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = { zoomScale = (zoomScale * 0.8f).coerceIn(0.2f, 6f) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = {
                        rotX = 25f
                        rotY = -40f
                        zoomScale = 0.9f
                        panOffset = Offset.Zero
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                }
            }
        }

        // Action Floating Buttons (Bottom Right)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FloatingActionButton(
                onClick = { isAutoRotating = !isAutoRotating },
                containerColor = if (isAutoRotating) Color(0xFF10B981) else Color(0xFF1E293B),
                contentColor = Color.White,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (isAutoRotating) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Auto Rotate",
                    modifier = Modifier.size(18.dp)
                )
            }

            FloatingActionButton(
                onClick = { renderModeShaded = !renderModeShaded },
                containerColor = Color(0xFF007AFF),
                contentColor = Color.White,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (renderModeShaded) Icons.Default.Visibility else Icons.Default.Layers,
                    contentDescription = "Toggle Shading",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private data class ProjectedPoint3D(val x: Float, val y: Float, val zDepth: Float)

private fun project3DPoint(
    pt: Point3D,
    rotXDeg: Float,
    rotYDeg: Float,
    scale: Float,
    cx: Float,
    cy: Float
): ProjectedPoint3D {
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
    return ProjectedPoint3D(sx, sy, z2.toFloat())
}

private fun DrawScope.draw3DGridFloor(cx: Float, cy: Float, rotX: Float, rotY: Float, scale: Float) {
    val gridStep = 50f
    val gridExtent = 300f
    val floorColor = Color(0x14FFFFFF)

    var g = -gridExtent
    while (g <= gridExtent) {
        val p1 = project3DPoint(Point3D(g, -200f, -gridExtent), rotX, rotY, scale, cx, cy)
        val p2 = project3DPoint(Point3D(g, -200f, gridExtent), rotX, rotY, scale, cx, cy)
        drawLine(floorColor, Offset(p1.x, p1.y), Offset(p2.x, p2.y), strokeWidth = 1f)

        val p3 = project3DPoint(Point3D(-gridExtent, -200f, g), rotX, rotY, scale, cx, cy)
        val p4 = project3DPoint(Point3D(gridExtent, -200f, g), rotX, rotY, scale, cx, cy)
        drawLine(floorColor, Offset(p3.x, p3.y), Offset(p4.x, p4.y), strokeWidth = 1f)
        g += gridStep
    }

    val origin = project3DPoint(Point3D(0f, 0f, 0f), rotX, rotY, scale, cx, cy)
    val xAxis = project3DPoint(Point3D(80f, 0f, 0f), rotX, rotY, scale, cx, cy)
    val yAxis = project3DPoint(Point3D(0f, 80f, 0f), rotX, rotY, scale, cx, cy)
    val zAxis = project3DPoint(Point3D(0f, 0f, 80f), rotX, rotY, scale, cx, cy)

    drawLine(Color(0xFFEF4444), Offset(origin.x, origin.y), Offset(xAxis.x, xAxis.y), strokeWidth = 2.5f)
    drawLine(Color(0xFF10B981), Offset(origin.x, origin.y), Offset(yAxis.x, yAxis.y), strokeWidth = 2.5f)
    drawLine(Color(0xFF38BDF8), Offset(origin.x, origin.y), Offset(zAxis.x, zAxis.y), strokeWidth = 2.5f)
}
