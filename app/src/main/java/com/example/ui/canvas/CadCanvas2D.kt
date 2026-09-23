package com.example.ui.canvas

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun CadCanvas2D(
    project: CadProject,
    activeTool: CadTool,
    activeLayerId: String,
    layers: List<CadLayer>,
    selectedEntityId: String?,
    isOrthoEnabled: Boolean,
    isSnapEnabled: Boolean,
    themeMode: CanvasThemeMode,
    scale: Float,
    panOffset: Offset,
    onScaleChange: (Float) -> Unit,
    onPanChange: (Offset) -> Unit,
    onPointerMoved: (worldX: Float, worldY: Float) -> Unit,
    onMeasuredDistance: (distMm: Float, angleDeg: Float) -> Unit,
    onEntitySelected: (CadEntity?) -> Unit,
    onAddEntity: (CadEntity) -> Unit,
    onDeleteEntity: (String) -> Unit,
    onUpdateEntity: (CadEntity) -> Unit,
    onDimensionPlacementRequest: (start: Offset, end: Offset) -> Unit,
    onToggleOrtho: () -> Unit,
    onToggleSnap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(IntSize(1000, 1000)) }

    // Touch Navigation Mode state
    var touchMode by remember {
        mutableStateOf(
            if (activeTool == CadTool.PAN_ZOOM) TouchNavigationMode.PAN_NAVIGATE
            else if (activeTool == CadTool.SELECT) TouchNavigationMode.SELECT_MODIFY
            else TouchNavigationMode.DRAFT_TOOL
        )
    }

    // Keep touch mode in sync when active tool changes
    LaunchedEffect(activeTool) {
        if (activeTool == CadTool.PAN_ZOOM) {
            touchMode = TouchNavigationMode.PAN_NAVIGATE
        } else if (activeTool == CadTool.SELECT) {
            touchMode = TouchNavigationMode.SELECT_MODIFY
        } else {
            touchMode = TouchNavigationMode.DRAFT_TOOL
        }
    }

    // Active gesture states
    var isInteracting by remember { mutableStateOf(false) }
    var interactionStartWorld by remember { mutableStateOf<Offset?>(null) }
    var interactionCurrentWorld by remember { mutableStateOf<Offset?>(null) }
    var dragMoveInitialEntity by remember { mutableStateOf<CadEntity?>(null) }
    var activeSnapResult by remember { mutableStateOf<OsnapResult?>(null) }
    var showJoystick by remember { mutableStateOf(false) }
    var showOsnapSettings by remember { mutableStateOf(false) }

    val enabledOsnapTypes = remember {
        mutableStateListOf(
            OsnapType.ENDPOINT,
            OsnapType.MIDPOINT,
            OsnapType.CENTER,
            OsnapType.QUADRANT,
            OsnapType.INTERSECTION,
            OsnapType.GRID
        )
    }

    fun screenToWorld(screenPos: Offset): Offset {
        val s = scale.coerceAtLeast(0.01f)
        return Offset(
            x = (screenPos.x - panOffset.x) / s,
            y = (screenPos.y - panOffset.y) / s
        )
    }

    fun worldToScreen(worldPos: Offset): Offset {
        return Offset(
            x = worldPos.x * scale + panOffset.x,
            y = worldPos.y * scale + panOffset.y
        )
    }

    fun fitAllEntitiesToScreen() {
        if (canvasSize.width > 50 && canvasSize.height > 50) {
            val (newScale, newPan) = CadTransformEngine.calculateFitTransform(
                entities = project.entities,
                viewportWidth = canvasSize.width.toFloat(),
                viewportHeight = canvasSize.height.toFloat(),
                paddingPx = 100f
            )
            onScaleChange(newScale)
            onPanChange(newPan)
        }
    }

    val bgColor = when (themeMode) {
        CanvasThemeMode.AUTOCAD_CLASSIC_BLACK -> Color(0xFF0D131F)
        CanvasThemeMode.BLUEPRINT_CYAN -> Color(0xFF071E3D)
        CanvasThemeMode.LIGHT_ENGINEERING_WHITE -> Color(0xFFF1F5F9)
    }

    val gridMajorColor = when (themeMode) {
        CanvasThemeMode.AUTOCAD_CLASSIC_BLACK -> Color(0x3338BDF8)
        CanvasThemeMode.BLUEPRINT_CYAN -> Color(0x4438BDF8)
        CanvasThemeMode.LIGHT_ENGINEERING_WHITE -> Color(0x280284C7)
    }

    val gridMinorColor = when (themeMode) {
        CanvasThemeMode.AUTOCAD_CLASSIC_BLACK -> Color(0x18FFFFFF)
        CanvasThemeMode.BLUEPRINT_CYAN -> Color(0x2038BDF8)
        CanvasThemeMode.LIGHT_ENGINEERING_WHITE -> Color(0x15000000)
    }

    val layerMap = remember(layers) { layers.associateBy { it.id } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .onSizeChanged { canvasSize = it }
            .testTag("cad_canvas_2d")
            .pointerInput(
                activeTool,
                touchMode,
                project.entities,
                selectedEntityId,
                scale,
                panOffset,
                isOrthoEnabled,
                isSnapEnabled,
                enabledOsnapTypes.toList()
            ) {
                // Unified, rock-solid gesture pipeline
                awaitEachGesture {
                    val firstDown = awaitFirstDown(requireUnconsumed = false)
                    var isMultiTouch = false
                    var prevPointers = listOf(firstDown.position)

                    var startRawWorld = screenToWorld(firstDown.position)
                    var effectiveStart = startRawWorld

                    // Initial snap calculation
                    if (isSnapEnabled && touchMode == TouchNavigationMode.DRAFT_TOOL) {
                        val snap = CadOsnapEngine.findBestSnapPoint(
                            cursorWorld = startRawWorld,
                            entities = project.entities,
                            snapThresholdMm = 30f / scale,
                            enabledSnapTypes = enabledOsnapTypes.toSet(),
                            isGridSnapEnabled = true,
                            gridSizeMm = 10f
                        )
                        activeSnapResult = snap
                        if (snap != null) {
                            effectiveStart = snap.point
                        }
                    } else {
                        activeSnapResult = null
                    }

                    interactionStartWorld = effectiveStart
                    interactionCurrentWorld = effectiveStart
                    isInteracting = true
                    onPointerMoved(effectiveStart.x, effectiveStart.y)

                    val hitEntity = findEntityAtWorldPoint(effectiveStart, project.entities, snapRadiusMm = 32f / scale)
                    if (touchMode == TouchNavigationMode.SELECT_MODIFY && hitEntity != null && hitEntity.id == selectedEntityId) {
                        dragMoveInitialEntity = hitEntity
                    } else {
                        dragMoveInitialEntity = null
                    }

                    var hasMoved = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressedPointers = event.changes.filter { it.pressed }

                        if (pressedPointers.isEmpty()) {
                            break
                        }

                        if (pressedPointers.size >= 2) {
                            // MULTI-TOUCH GESTURE (Pinch to Zoom & 2-Finger Pan)
                            isMultiTouch = true
                            isInteracting = false
                            dragMoveInitialEntity = null
                            activeSnapResult = null

                            val p1 = pressedPointers[0].position
                            val p2 = pressedPointers[1].position
                            val currentDist = (p1 - p2).getDistance()
                            val currentCenter = (p1 + p2) / 2f

                            if (prevPointers.size >= 2) {
                                val prevDist = (prevPointers[0] - prevPointers[1]).getDistance()
                                val prevCenter = (prevPointers[0] + prevPointers[1]) / 2f

                                if (prevDist > 10f && currentDist > 10f) {
                                    val zoomFactor = (currentDist / prevDist).coerceIn(0.7f, 1.4f)
                                    val newScale = (scale * zoomFactor).coerceIn(0.1f, 25f)
                                    val panDelta = currentCenter - prevCenter

                                    // Focal zoom around center
                                    val newPan = Offset(
                                        x = currentCenter.x - (currentCenter.x - panOffset.x) * (newScale / scale) + panDelta.x,
                                        y = currentCenter.y - (currentCenter.y - panOffset.y) * (newScale / scale) + panDelta.y
                                    )
                                    onScaleChange(newScale)
                                    onPanChange(newPan)
                                }
                            }

                            prevPointers = listOf(p1, p2)
                            pressedPointers.forEach { it.consume() }
                        } else if (!isMultiTouch && pressedPointers.size == 1) {
                            // SINGLE-TOUCH INTERACTION
                            val change = pressedPointers[0]
                            val curScreen = change.position
                            val dragDelta = curScreen - firstDown.position

                            if (dragDelta.getDistance() > 6f) {
                                hasMoved = true
                            }

                            if (touchMode == TouchNavigationMode.PAN_NAVIGATE || activeTool == CadTool.PAN_ZOOM) {
                                // Smooth 1-Finger Infinite Pan
                                val panDelta = change.position - change.previousPosition
                                onPanChange(panOffset + panDelta)
                                change.consume()
                            } else if (touchMode == TouchNavigationMode.SELECT_MODIFY && dragMoveInitialEntity != null && hasMoved) {
                                // Drag-to-Move Selected Entity
                                var curWorld = screenToWorld(curScreen)
                                if (isSnapEnabled) {
                                    val snap = CadOsnapEngine.findBestSnapPoint(
                                        cursorWorld = curWorld,
                                        entities = project.entities.filter { it.id != dragMoveInitialEntity!!.id },
                                        snapThresholdMm = 25f / scale,
                                        enabledSnapTypes = enabledOsnapTypes.toSet()
                                    )
                                    if (snap != null) curWorld = snap.point
                                }
                                val dx = curWorld.x - effectiveStart.x
                                val dy = curWorld.y - effectiveStart.y
                                val moved = CadTransformEngine.translateEntity(dragMoveInitialEntity!!, dx, dy)
                                onUpdateEntity(moved)
                                change.consume()
                            } else {
                                // Drafting & Measurement
                                var curWorld = screenToWorld(curScreen)

                                if (isSnapEnabled) {
                                    val snap = CadOsnapEngine.findBestSnapPoint(
                                        cursorWorld = curWorld,
                                        entities = project.entities,
                                        snapThresholdMm = 30f / scale,
                                        enabledSnapTypes = enabledOsnapTypes.toSet(),
                                        isGridSnapEnabled = true,
                                        gridSizeMm = 10f
                                    )
                                    activeSnapResult = snap
                                    if (snap != null) {
                                        curWorld = snap.point
                                    }
                                } else {
                                    activeSnapResult = null
                                }

                                if (isOrthoEnabled && activeSnapResult == null) {
                                    curWorld = CadGeometryEngine.applyOrtho(effectiveStart, curWorld)
                                }

                                interactionCurrentWorld = curWorld
                                onPointerMoved(curWorld.x, curWorld.y)

                                val dx = curWorld.x - effectiveStart.x
                                val dy = curWorld.y - effectiveStart.y
                                val dist = sqrt(dx * dx + dy * dy)
                                val angle = (atan2(dy, dx) * 180.0 / PI).let { if (it < 0) it + 360f else it }.toFloat()
                                onMeasuredDistance(dist, angle)
                            }
                        }
                    }

                    // Gesture Finished / Finger Lifted
                    val start = interactionStartWorld
                    val end = interactionCurrentWorld

                    if (!isMultiTouch && start != null && end != null) {
                        val totalDist = sqrt((end.x - start.x).pow(2) + (end.y - start.y).pow(2))

                        if (touchMode == TouchNavigationMode.SELECT_MODIFY || !hasMoved || totalDist < 4f) {
                            // Tap Action
                            when (activeTool) {
                                CadTool.SELECT, CadTool.ROTATE -> {
                                    onEntitySelected(hitEntity)
                                }
                                CadTool.ERASE -> {
                                    if (hitEntity != null) {
                                        onDeleteEntity(hitEntity.id)
                                    }
                                }
                                CadTool.NOZZLE_STAMP -> {
                                    val nozzle = CadNozzleComponent(
                                        x = end.x,
                                        y = end.y,
                                        sizeNb = 50,
                                        flangeRating = "150# RF",
                                        facing = NozzleFacing.NORTH,
                                        layerId = activeLayerId
                                    )
                                    onAddEntity(nozzle)
                                }
                                CadTool.DISHED_HEAD_STAMP -> {
                                    val dish = CadDishedHead(
                                        cx = end.x,
                                        cy = end.y,
                                        diameterMm = 600f,
                                        straightFaceMm = 40f,
                                        dishType = DishedHeadType.ELLIPSOIDAL_2_TO_1,
                                        layerId = activeLayerId
                                    )
                                    onAddEntity(dish)
                                }
                                CadTool.SADDLE_SUPPORT -> {
                                    val saddle = CadSaddleSupport(
                                        x = end.x,
                                        y = end.y,
                                        vesselDiaMm = 600f,
                                        saddleHeightMm = 250f,
                                        baseWidthMm = 400f,
                                        layerId = activeLayerId
                                    )
                                    onAddEntity(saddle)
                                }
                                CadTool.LEG_SUPPORT -> {
                                    val leg = CadLegSupport(
                                        x = end.x,
                                        y = end.y,
                                        legPipeNb = 50,
                                        heightMm = 300f,
                                        layerId = activeLayerId
                                    )
                                    onAddEntity(leg)
                                }
                                CadTool.MANWAY_STAMP -> {
                                    val manway = CadManway(
                                        x = end.x,
                                        y = end.y,
                                        manwayNb = 500,
                                        layerId = activeLayerId
                                    )
                                    onAddEntity(manway)
                                }
                                CadTool.VALVE_STAMP -> {
                                    val valve = CadValve(
                                        x = end.x,
                                        y = end.y,
                                        valveType = ValveType.GATE_VALVE,
                                        sizeNb = 50,
                                        layerId = activeLayerId
                                    )
                                    onAddEntity(valve)
                                }
                                CadTool.AGITATOR_STAMP -> {
                                    val agitator = CadAgitator(
                                        topX = end.x,
                                        topY = end.y,
                                        shaftLengthMm = 600f,
                                        impellerDiaMm = 300f,
                                        layerId = activeLayerId
                                    )
                                    onAddEntity(agitator)
                                }
                                CadTool.BOM_ITEM_BUBBLE -> {
                                    val count = project.entities.filterIsInstance<CadBomItem>().size + 1
                                    val bom = CadBomItem(
                                        x = end.x,
                                        y = end.y,
                                        itemNumber = count,
                                        description = "Fabrication Component #$count",
                                        qty = 1,
                                        material = "SS 304",
                                        layerId = activeLayerId
                                    )
                                    onAddEntity(bom)
                                }
                                CadTool.TEXT_NOTE -> {
                                    val text = CadText(
                                        x = end.x,
                                        y = end.y,
                                        text = "SS 304 THK 5MM",
                                        layerId = activeLayerId
                                    )
                                    onAddEntity(text)
                                }
                                CadTool.OFFSET_WALL -> {
                                    if (hitEntity != null) {
                                        val offset = CadTransformEngine.offsetEntity(hitEntity, 50f)
                                        if (offset != null) onAddEntity(offset)
                                    }
                                }
                                else -> {}
                            }
                        } else if (hasMoved && touchMode == TouchNavigationMode.DRAFT_TOOL) {
                            // Drag-to-Create Geometric Shapes
                            when (activeTool) {
                                CadTool.LINE -> {
                                    val line = CadLine(
                                        x1 = start.x,
                                        y1 = start.y,
                                        x2 = end.x,
                                        y2 = end.y,
                                        strokeType = StrokeType.CONTINUOUS,
                                        layerId = activeLayerId
                                    )
                                    onAddEntity(line)
                                }
                                CadTool.CENTERLINE -> {
                                    val cline = CadLine(
                                        x1 = start.x,
                                        y1 = start.y,
                                        x2 = end.x,
                                        y2 = end.y,
                                        strokeType = StrokeType.CENTER_LINE,
                                        layerId = "CENTER"
                                    )
                                    onAddEntity(cline)
                                }
                                CadTool.RECTANGLE -> {
                                    val rx = minOf(start.x, end.x)
                                    val ry = minOf(start.y, end.y)
                                    val rw = abs(end.x - start.x)
                                    val rh = abs(end.y - start.y)
                                    if (rw > 5f && rh > 5f) {
                                        val rect = CadRect(
                                            x = rx,
                                            y = ry,
                                            width = rw,
                                            height = rh,
                                            plateThicknessMm = 5f,
                                            layerId = activeLayerId
                                        )
                                        onAddEntity(rect)
                                    }
                                }
                                CadTool.CIRCLE -> {
                                    val radius = totalDist
                                    if (radius > 5f) {
                                        val circle = CadCircle(
                                            cx = start.x,
                                            cy = start.y,
                                            radius = radius,
                                            layerId = activeLayerId
                                        )
                                        onAddEntity(circle)
                                    }
                                }
                                CadTool.ARC -> {
                                    val radius = totalDist
                                    if (radius > 5f) {
                                        val arc = CadArc(
                                            cx = start.x,
                                            cy = start.y,
                                            radius = radius,
                                            startAngleDeg = 0f,
                                            sweepAngleDeg = 180f,
                                            layerId = activeLayerId
                                        )
                                        onAddEntity(arc)
                                    }
                                }
                                CadTool.CONE_HOPPER -> {
                                    val topW = abs(end.x - start.x).coerceAtLeast(100f)
                                    val height = abs(end.y - start.y).coerceAtLeast(100f)
                                    val hopper = CadHopperCone(
                                        topX = minOf(start.x, end.x),
                                        topY = minOf(start.y, end.y),
                                        topWidth = topW,
                                        bottomWidth = topW * 0.4f,
                                        height = height,
                                        layerId = activeLayerId
                                    )
                                    onAddEntity(hopper)
                                }
                                CadTool.DIMENSION -> {
                                    onDimensionPlacementRequest(start, end)
                                }
                                CadTool.LEADER_NOTE -> {
                                    val leader = CadLeader(
                                        targetX = start.x,
                                        targetY = start.y,
                                        elbowX = end.x,
                                        elbowY = end.y,
                                        text = "SS NOZZLE 50 NB",
                                        layerId = "TEXT"
                                    )
                                    onAddEntity(leader)
                                }
                                else -> {}
                            }
                        }
                    }

                    isInteracting = false
                    interactionStartWorld = null
                    interactionCurrentWorld = null
                    dragMoveInitialEntity = null
                    activeSnapResult = null
                }
            }
    ) {
        // Core 2D CAD Canvas Drawing Engine
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Draw AutoCAD Grid System
            drawEngineeringGrid(
                canvasW = canvasW,
                canvasH = canvasH,
                scale = scale,
                panOffset = panOffset,
                majorColor = gridMajorColor,
                minorColor = gridMinorColor
            )

            // 2. Draw World Coordinate Origin (0,0) Marker
            drawOriginMarker(scale, panOffset)

            // 3. Draw All CAD Entities
            for (entity in project.entities) {
                val layer = layerMap[entity.layerId] ?: CadLayer("0", "0", 0xFFE2E8F0L)
                if (!layer.isVisible) continue

                val isSelected = (entity.id == selectedEntityId)
                drawCadEntity(
                    entity = entity,
                    layer = layer,
                    scale = scale,
                    panOffset = panOffset,
                    isSelected = isSelected,
                    themeMode = themeMode
                )
            }

            // 4. Draw Live Interactive Preview (Line / Rect / Circle / Dim while dragging)
            if (isInteracting && interactionStartWorld != null && interactionCurrentWorld != null) {
                drawLiveInteractivePreview(
                    tool = activeTool,
                    startWorld = interactionStartWorld!!,
                    curWorld = interactionCurrentWorld!!,
                    scale = scale,
                    panOffset = panOffset,
                    themeMode = themeMode
                )
            }

            // 5. Draw AutoCAD OSNAP Marker & Snap Tag
            activeSnapResult?.let { snap ->
                drawOsnapMarker(snap, scale, panOffset)
            }

            // 6. Draw Full AutoCAD Crosshair & Dynamic Input HUD
            interactionCurrentWorld?.let { curWorld ->
                drawAutoCadCrosshairAndHud(
                    curWorld = curWorld,
                    startWorld = interactionStartWorld,
                    scale = scale,
                    panOffset = panOffset,
                    canvasW = canvasW,
                    canvasH = canvasH,
                    activeTool = activeTool,
                    touchMode = touchMode
                )
            }
        }

        // FLOATING PRECISION CAD NAVIGATION CONTROLS & HUD
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Top Right: Touch Mode Selector & Precision Zoom Bar
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .widthIn(max = 240.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Touch Navigation Mode Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    tonalElevation = 6.dp,
                    shadowElevation = 4.dp,
                    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 1-Finger Pan Mode Button
                        IconButton(
                            onClick = { touchMode = TouchNavigationMode.PAN_NAVIGATE },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (touchMode == TouchNavigationMode.PAN_NAVIGATE) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .testTag("touch_mode_pan")
                        ) {
                            Icon(
                                Icons.Default.PanTool,
                                contentDescription = "1-Finger Pan Mode",
                                tint = if (touchMode == TouchNavigationMode.PAN_NAVIGATE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Tool Draft Mode Button
                        IconButton(
                            onClick = { touchMode = TouchNavigationMode.DRAFT_TOOL },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (touchMode == TouchNavigationMode.DRAFT_TOOL) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .testTag("touch_mode_draft")
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Draft Mode",
                                tint = if (touchMode == TouchNavigationMode.DRAFT_TOOL) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Select / Move Mode Button
                        IconButton(
                            onClick = { touchMode = TouchNavigationMode.SELECT_MODIFY },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (touchMode == TouchNavigationMode.SELECT_MODIFY) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .testTag("touch_mode_select")
                        ) {
                            Icon(
                                Icons.Default.NearMe,
                                contentDescription = "Select & Move Mode",
                                tint = if (touchMode == TouchNavigationMode.SELECT_MODIFY) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Precision Zoom & Viewport Navigator HUD
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    tonalElevation = 6.dp,
                    shadowElevation = 4.dp,
                    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Zoom In (+)
                        IconButton(
                            onClick = { onScaleChange((scale * 1.3f).coerceAtMost(25f)) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }

                        // Zoom Out (-)
                        IconButton(
                            onClick = { onScaleChange((scale * 0.77f).coerceAtLeast(0.1f)) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }

                        HorizontalDivider(modifier = Modifier.width(36.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        // Fit Drawing / Zoom Extents (⊡)
                        IconButton(
                            onClick = { fitAllEntitiesToScreen() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.FitScreen, contentDescription = "Zoom Extents", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        }

                        // Center Coordinate Origin (0,0) (⌂)
                        IconButton(
                            onClick = {
                                onPanChange(Offset(canvasSize.width * 0.5f, canvasSize.height * 0.5f))
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = "Center Origin", tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                        }

                        // 8-Way Pan Disc Toggle
                        IconButton(
                            onClick = { showJoystick = !showJoystick },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.ControlCamera, contentDescription = "Pan D-Pad", tint = if (showJoystick) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Top Left: Mode Status Badge & Snap Toggles
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (touchMode) {
                        TouchNavigationMode.PAN_NAVIGATE -> Color(0xFF0284C7)
                        TouchNavigationMode.DRAFT_TOOL -> Color(0xFF10B981)
                        TouchNavigationMode.SELECT_MODIFY -> Color(0xFF8B5CF6)
                    },
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            when (touchMode) {
                                TouchNavigationMode.PAN_NAVIGATE -> Icons.Default.PanTool
                                TouchNavigationMode.DRAFT_TOOL -> Icons.Default.Edit
                                TouchNavigationMode.SELECT_MODIFY -> Icons.Default.NearMe
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            when (touchMode) {
                                TouchNavigationMode.PAN_NAVIGATE -> "1-FINGER PAN"
                                TouchNavigationMode.DRAFT_TOOL -> "DRAFTING (${activeTool.name})"
                                TouchNavigationMode.SELECT_MODIFY -> "SELECT / MOVE"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Snap Settings Toggle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier.clickable { showOsnapSettings = !showOsnapSettings }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "OSNAP 🧲",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSnapEnabled) Color(0xFF10B981) else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Bottom Right: 8-Way Pan D-Pad Joystick (if enabled)
            AnimatedVisibility(
                visible = showJoystick,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 60.dp, end = 12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(120.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // UP
                        IconButton(
                            onClick = { onPanChange(panOffset + Offset(0f, 60f)) },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Pan Up", tint = MaterialTheme.colorScheme.primary)
                        }
                        // DOWN
                        IconButton(
                            onClick = { onPanChange(panOffset + Offset(0f, -60f)) },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Pan Down", tint = MaterialTheme.colorScheme.primary)
                        }
                        // LEFT
                        IconButton(
                            onClick = { onPanChange(panOffset + Offset(60f, 0f)) },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Pan Left", tint = MaterialTheme.colorScheme.primary)
                        }
                        // RIGHT
                        IconButton(
                            onClick = { onPanChange(panOffset + Offset(-60f, 0f)) },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Pan Right", tint = MaterialTheme.colorScheme.primary)
                        }
                        // Center 1:1
                        Text(
                            "1:1",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clickable { onScaleChange(1.0f) }
                        )
                    }
                }
            }

            // OSNAP Quick Settings Drawer Dropdown
            AnimatedVisibility(
                visible = showOsnapSettings,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 40.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .width(260.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AutoCAD Object Snap", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = isSnapEnabled,
                                onCheckedChange = { onToggleSnap() },
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        OsnapType.values().forEach { osnap ->
                            val isChecked = enabledOsnapTypes.contains(osnap)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) enabledOsnapTypes.remove(osnap)
                                        else enabledOsnapTypes.add(osnap)
                                    }
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(osnap.glyphName, fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(osnap.label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { check ->
                                        if (check) enabledOsnapTypes.add(osnap)
                                        else enabledOsnapTypes.remove(osnap)
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DRAWING ENGINE IMPLEMENTATION DETAILS
// -----------------------------------------------------------------------------

private fun DrawScope.drawEngineeringGrid(
    canvasW: Float,
    canvasH: Float,
    scale: Float,
    panOffset: Offset,
    majorColor: Color,
    minorColor: Color
) {
    val baseGridSizeMm = 100f
    val fineGridSizeMm = 20f

    val stepMajorPx = baseGridSizeMm * scale
    val stepMinorPx = fineGridSizeMm * scale

    // Minor Grid (if visible)
    if (stepMinorPx > 12f) {
        val startX = (panOffset.x % stepMinorPx)
        val startY = (panOffset.y % stepMinorPx)

        var x = startX
        while (x < canvasW) {
            drawLine(minorColor, Offset(x, 0f), Offset(x, canvasH), strokeWidth = 0.6f)
            x += stepMinorPx
        }
        var y = startY
        while (y < canvasH) {
            drawLine(minorColor, Offset(0f, y), Offset(canvasW, y), strokeWidth = 0.6f)
            y += stepMinorPx
        }
    }

    // Major Grid
    if (stepMajorPx > 20f) {
        val startX = (panOffset.x % stepMajorPx)
        val startY = (panOffset.y % stepMajorPx)

        var x = startX
        while (x < canvasW) {
            drawLine(majorColor, Offset(x, 0f), Offset(x, canvasH), strokeWidth = 1.0f)
            x += stepMajorPx
        }
        var y = startY
        while (y < canvasH) {
            drawLine(majorColor, Offset(0f, y), Offset(canvasW, y), strokeWidth = 1.0f)
            y += stepMajorPx
        }
    }
}

private fun DrawScope.drawOriginMarker(scale: Float, panOffset: Offset) {
    val originX = panOffset.x
    val originY = panOffset.y

    // X Axis (Red arrow)
    drawLine(Color(0xFFEF4444), Offset(originX, originY), Offset(originX + 60f, originY), strokeWidth = 2f)
    // Y Axis (Green arrow)
    drawLine(Color(0xFF10B981), Offset(originX, originY), Offset(originX, originY + 60f), strokeWidth = 2f)
    // Center point
    drawCircle(Color.White, radius = 3f, center = Offset(originX, originY))
}

private fun DrawScope.drawCadEntity(
    entity: CadEntity,
    layer: CadLayer,
    scale: Float,
    panOffset: Offset,
    isSelected: Boolean,
    themeMode: CanvasThemeMode
) {
    val baseColor = if (isSelected) Color(0xFFFBBF24) else layer.composeColor
    val strokeWidthPx = (layer.strokeWidthMm * scale * 2.2f).coerceIn(1.2f, 8f)

    val pathEffect = when (layer.strokeType) {
        StrokeType.CONTINUOUS -> null
        StrokeType.CENTER_LINE -> PathEffect.dashPathEffect(floatArrayOf(24f, 8f, 6f, 8f), 0f)
        StrokeType.DASHED_HIDDEN -> PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    }

    when (entity) {
        is CadLine -> {
            val p1 = Offset(entity.x1 * scale + panOffset.x, entity.y1 * scale + panOffset.y)
            val p2 = Offset(entity.x2 * scale + panOffset.x, entity.y2 * scale + panOffset.y)
            val lineEffect = when (entity.strokeType) {
                StrokeType.CONTINUOUS -> pathEffect
                StrokeType.CENTER_LINE -> PathEffect.dashPathEffect(floatArrayOf(24f, 8f, 6f, 8f), 0f)
                StrokeType.DASHED_HIDDEN -> PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            }
            drawLine(
                color = if (entity.customColorArgb != null && !isSelected) Color(entity.customColorArgb) else baseColor,
                start = p1,
                end = p2,
                strokeWidth = strokeWidthPx,
                pathEffect = lineEffect
            )
        }
        is CadRect -> {
            val rx = entity.x * scale + panOffset.x
            val ry = entity.y * scale + panOffset.y
            val rw = entity.width * scale
            val rh = entity.height * scale

            if (entity.isFilled) {
                drawRect(baseColor.copy(alpha = 0.15f), Offset(rx, ry), Size(rw, rh))
            }
            drawRect(
                color = baseColor,
                topLeft = Offset(rx, ry),
                size = Size(rw, rh),
                style = Stroke(width = strokeWidthPx, pathEffect = pathEffect)
            )

            // Inner plate thickness line
            if (entity.plateThicknessMm > 0f) {
                val tPx = entity.plateThicknessMm * scale
                if (rw > tPx * 2 && rh > tPx * 2) {
                    drawRect(
                        color = baseColor.copy(alpha = 0.5f),
                        topLeft = Offset(rx + tPx, ry + tPx),
                        size = Size(rw - tPx * 2, rh - tPx * 2),
                        style = Stroke(width = strokeWidthPx * 0.6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
                    )
                }
            }
        }
        is CadCircle -> {
            val cx = entity.cx * scale + panOffset.x
            val cy = entity.cy * scale + panOffset.y
            val r = entity.radius * scale

            val cEffect = if (entity.isPcd) PathEffect.dashPathEffect(floatArrayOf(20f, 6f, 4f, 6f), 0f) else pathEffect
            val color = if (entity.isPcd) Color(0xFF10B981) else baseColor

            drawCircle(
                color = color,
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = strokeWidthPx, pathEffect = cEffect)
            )

            if (entity.isPcd) {
                // Draw 4 bolt holes on PCD
                for (i in 0 until 4) {
                    val angle = Math.toRadians((i * 90.0) + 45.0)
                    val bx = (cx + r * cos(angle)).toFloat()
                    val by = (cy + r * sin(angle)).toFloat()
                    drawCircle(Color(0xFF38BDF8), radius = 4f, center = Offset(bx, by))
                }
            }
        }
        is CadArc -> {
            val cx = entity.cx * scale + panOffset.x
            val cy = entity.cy * scale + panOffset.y
            val r = entity.radius * scale
            drawArc(
                color = baseColor,
                startAngle = entity.startAngleDeg,
                sweepAngle = entity.sweepAngleDeg,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r),
                size = Size(r * 2, r * 2),
                style = Stroke(width = strokeWidthPx, pathEffect = pathEffect)
            )
        }
        is CadDishedHead -> {
            val cx = entity.cx * scale + panOffset.x
            val cy = entity.cy * scale + panOffset.y
            val r = entity.diameterMm * 0.5f * scale
            val sf = entity.straightFaceMm * scale
            val crownDepth = when (entity.dishType) {
                DishedHeadType.ELLIPSOIDAL_2_TO_1 -> r * 0.5f
                DishedHeadType.TORISPHERICAL -> r * 0.38f
                DishedHeadType.HEMISPHERICAL -> r
                DishedHeadType.FLAT_FLANGED -> 0f
            }

            val path = Path().apply {
                val startX = cx - r
                val endX = cx + r
                if (entity.isFacingUp) {
                    moveTo(startX, cy)
                    lineTo(startX, cy - sf)
                    cubicTo(startX, cy - sf - crownDepth, endX, cy - sf - crownDepth, endX, cy - sf)
                    lineTo(endX, cy)
                } else {
                    moveTo(startX, cy)
                    lineTo(startX, cy + sf)
                    cubicTo(startX, cy + sf + crownDepth, endX, cy + sf + crownDepth, endX, cy + sf)
                    lineTo(endX, cy)
                }
            }
            drawPath(path, color = baseColor, style = Stroke(width = strokeWidthPx))
        }
        is CadSaddleSupport -> {
            val sx = entity.x * scale + panOffset.x
            val sy = entity.y * scale + panOffset.y
            val bw = entity.baseWidthMm * scale
            val sh = entity.saddleHeightMm * scale
            val r = entity.vesselDiaMm * 0.5f * scale

            val path = Path().apply {
                moveTo(sx - bw * 0.5f, sy + sh)
                lineTo(sx + bw * 0.5f, sy + sh)
                lineTo(sx + bw * 0.4f, sy + r * 0.5f)
                cubicTo(sx + r * 0.8f, sy, sx - r * 0.8f, sy, sx - bw * 0.4f, sy + r * 0.5f)
                close()
            }
            drawPath(path, color = baseColor, style = Stroke(width = strokeWidthPx))
            // Base plate thickness
            drawLine(baseColor, Offset(sx - bw * 0.5f, sy + sh + 6f), Offset(sx + bw * 0.5f, sy + sh + 6f), strokeWidth = strokeWidthPx * 1.5f)
        }
        is CadLegSupport -> {
            val lx = entity.x * scale + panOffset.x
            val ly = entity.y * scale + panOffset.y
            val lh = entity.heightMm * scale
            val pw = entity.basePadDiaMm * scale

            drawLine(baseColor, Offset(lx - 8f, ly), Offset(lx - 8f, ly + lh), strokeWidth = strokeWidthPx)
            drawLine(baseColor, Offset(lx + 8f, ly), Offset(lx + 8f, ly + lh), strokeWidth = strokeWidthPx)
            // Base foot pad
            drawLine(baseColor, Offset(lx - pw * 0.5f, ly + lh), Offset(lx + pw * 0.5f, ly + lh), strokeWidth = strokeWidthPx * 2f)
        }
        is CadManway -> {
            val mx = entity.x * scale + panOffset.x
            val my = entity.y * scale + panOffset.y
            val mr = entity.manwayNb * 0.25f * scale

            drawCircle(baseColor, radius = mr, center = Offset(mx, my), style = Stroke(width = strokeWidthPx))
            drawCircle(Color(0xFF10B981), radius = mr * 0.8f, center = Offset(mx, my), style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)))
            if (entity.hasDavitArm) {
                drawLine(Color(0xFFF59E0B), Offset(mx + mr, my), Offset(mx + mr + 35f, my - 20f), strokeWidth = 2.5f)
                drawCircle(Color(0xFFF59E0B), radius = 4f, center = Offset(mx + mr + 35f, my - 20f))
            }
        }
        is CadValve -> {
            val vx = entity.x * scale + panOffset.x
            val vy = entity.y * scale + panOffset.y
            val vl = entity.lengthMm * scale
            val vh = vl * 0.6f

            val path = Path().apply {
                if (entity.isVertical) {
                    moveTo(vx - vh * 0.5f, vy - vl * 0.5f)
                    lineTo(vx + vh * 0.5f, vy - vl * 0.5f)
                    lineTo(vx - vh * 0.5f, vy + vl * 0.5f)
                    lineTo(vx + vh * 0.5f, vy + vl * 0.5f)
                    close()
                } else {
                    moveTo(vx - vl * 0.5f, vy - vh * 0.5f)
                    lineTo(vx - vl * 0.5f, vy + vh * 0.5f)
                    lineTo(vx + vl * 0.5f, vy - vh * 0.5f)
                    lineTo(vx + vl * 0.5f, vy + vh * 0.5f)
                    close()
                }
            }
            drawPath(path, color = baseColor, style = Stroke(width = strokeWidthPx))
            // Valve handwheel stem
            drawLine(baseColor, Offset(vx, vy), Offset(vx, vy - vh), strokeWidth = strokeWidthPx)
            drawLine(baseColor, Offset(vx - 15f, vy - vh), Offset(vx + 15f, vy - vh), strokeWidth = strokeWidthPx * 1.5f)
        }
        is CadAgitator -> {
            val ax = entity.topX * scale + panOffset.x
            val ay = entity.topY * scale + panOffset.y
            val sl = entity.shaftLengthMm * scale
            val id = entity.impellerDiaMm * scale

            // Motor Top Box
            drawRect(Color(0xFF64748B), Offset(ax - 30f, ay - 60f), Size(60f, 60f))
            // Shaft
            drawLine(baseColor, Offset(ax, ay), Offset(ax, ay + sl), strokeWidth = strokeWidthPx * 1.5f)
            // Impeller Blades
            drawLine(Color(0xFF38BDF8), Offset(ax - id * 0.5f, ay + sl), Offset(ax + id * 0.5f, ay + sl), strokeWidth = strokeWidthPx * 2f)
        }
        is CadBomItem -> {
            val bx = entity.x * scale + panOffset.x
            val by = entity.y * scale + panOffset.y

            drawCircle(Color(0xFF0284C7), radius = 16f, center = Offset(bx, by))
            drawCircle(Color.White, radius = 16f, center = Offset(bx, by), style = Stroke(width = 1.5f))
            drawContext.canvas.nativeCanvas.apply {
                val paint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 20f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                }
                drawText(entity.itemNumber.toString(), bx, by + 7f, paint)
            }
        }
        is CadNozzleComponent -> {
            val nx = entity.x * scale + panOffset.x
            val ny = entity.y * scale + panOffset.y
            val fd = entity.flangeDiaMm * scale
            val nl = entity.neckLengthMm * scale

            val neckW = fd * 0.45f
            drawRect(baseColor, Offset(nx - neckW * 0.5f, ny - nl), Size(neckW, nl), style = Stroke(width = strokeWidthPx))
            // Flange Face
            drawRect(Color(0xFF38BDF8), Offset(nx - fd * 0.5f, ny - nl - 12f), Size(fd, 12f), style = Stroke(width = strokeWidthPx * 1.2f))
        }
        is CadHopperCone -> {
            val hx = entity.topX * scale + panOffset.x
            val hy = entity.topY * scale + panOffset.y
            val tw = entity.topWidth * scale
            val bw = entity.bottomWidth * scale
            val hh = entity.height * scale

            val path = Path().apply {
                moveTo(hx, hy)
                lineTo(hx + tw, hy)
                lineTo(hx + (tw + bw) * 0.5f, hy + hh)
                lineTo(hx + (tw - bw) * 0.5f, hy + hh)
                close()
            }
            drawPath(path, color = baseColor, style = Stroke(width = strokeWidthPx))
        }
        is CadDimension -> {
            drawCadDimension(entity, scale, panOffset, isSelected)
        }
        is CadLeader -> {
            drawCadLeader(entity, scale, panOffset, isSelected)
        }
        is CadText -> {
            val tx = entity.x * scale + panOffset.x
            val ty = entity.y * scale + panOffset.y
            val textPaint = Paint().apply {
                color = if (isSelected) android.graphics.Color.YELLOW else (entity.customColorArgb?.toInt() ?: android.graphics.Color.WHITE)
                textSize = (entity.textHeightMm * scale).coerceIn(12f, 48f)
                typeface = if (entity.isBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(entity.text, tx, ty, textPaint)
        }
    }
}

private fun DrawScope.drawCadDimension(
    dim: CadDimension,
    scale: Float,
    panOffset: Offset,
    isSelected: Boolean
) {
    val p1 = Offset(dim.x1 * scale + panOffset.x, dim.y1 * scale + panOffset.y)
    val p2 = Offset(dim.x2 * scale + panOffset.x, dim.y2 * scale + panOffset.y)
    val color = if (isSelected) Color(0xFFFBBF24) else Color(0xFFEF4444)
    val offPx = dim.offsetDistance * scale

    val dx = p2.x - p1.x
    val dy = p2.y - p1.y
    val len = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
    val nx = -dy / len
    val ny = dx / len

    val dp1 = Offset(p1.x + nx * offPx, p1.y + ny * offPx)
    val dp2 = Offset(p2.x + nx * offPx, p2.y + ny * offPx)

    // Extension witness lines
    drawLine(color.copy(alpha = 0.6f), p1, Offset(dp1.x + nx * 8f, dp1.y + ny * 8f), strokeWidth = 1f)
    drawLine(color.copy(alpha = 0.6f), p2, Offset(dp2.x + nx * 8f, dp2.y + ny * 8f), strokeWidth = 1f)

    // Dimension dimension line
    drawLine(color, dp1, dp2, strokeWidth = 1.6f)

    // AutoCAD 45-degree architectural slash ticks
    val tickSize = 7f
    drawLine(color, Offset(dp1.x - tickSize, dp1.y + tickSize), Offset(dp1.x + tickSize, dp1.y - tickSize), strokeWidth = 2f)
    drawLine(color, Offset(dp2.x - tickSize, dp2.y + tickSize), Offset(dp2.x + tickSize, dp2.y - tickSize), strokeWidth = 2f)

    // Dimension text
    val worldDistMm = sqrt((dim.x2 - dim.x1).pow(2) + (dim.y2 - dim.y1).pow(2))
    val displayText = dim.textOverride ?: "${dim.prefix}${worldDistMm.roundToInt()}${dim.suffix}"
    val mid = Offset((dp1.x + dp2.x) * 0.5f, (dp1.y + dp2.y) * 0.5f)

    drawContext.canvas.nativeCanvas.apply {
        val paint = Paint().apply {
            this.color = if (isSelected) android.graphics.Color.YELLOW else android.graphics.Color.RED
            textSize = (14f * scale).coerceIn(12f, 22f)
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
        }
        drawText(displayText, mid.x, mid.y - 8f, paint)
    }
}

private fun DrawScope.drawCadLeader(
    leader: CadLeader,
    scale: Float,
    panOffset: Offset,
    isSelected: Boolean
) {
    val tp = Offset(leader.targetX * scale + panOffset.x, leader.targetY * scale + panOffset.y)
    val ep = Offset(leader.elbowX * scale + panOffset.x, leader.elbowY * scale + panOffset.y)
    val color = if (isSelected) Color(0xFFFBBF24) else Color(0xFFF59E0B)

    // Slanted leader line
    drawLine(color, tp, ep, strokeWidth = 1.6f)
    // Horizontal landing
    val landingEnd = Offset(ep.x + (if (ep.x >= tp.x) 50f else -50f), ep.y)
    drawLine(color, ep, landingEnd, strokeWidth = 1.6f)

    // Arrow tip on target
    drawCircle(color, radius = 3.5f, center = tp)

    // Leader text
    drawContext.canvas.nativeCanvas.apply {
        val paint = Paint().apply {
            this.color = if (isSelected) android.graphics.Color.YELLOW else android.graphics.Color.WHITE
            textSize = (13f * scale).coerceIn(11f, 20f)
            typeface = Typeface.DEFAULT_BOLD
            textAlign = if (ep.x >= tp.x) Paint.Align.LEFT else Paint.Align.RIGHT
        }
        drawText(leader.text, if (ep.x >= tp.x) ep.x + 6f else ep.x - 6f, ep.y - 6f, paint)
    }
}

private fun DrawScope.drawLiveInteractivePreview(
    tool: CadTool,
    startWorld: Offset,
    curWorld: Offset,
    scale: Float,
    panOffset: Offset,
    themeMode: CanvasThemeMode
) {
    val p1 = Offset(startWorld.x * scale + panOffset.x, startWorld.y * scale + panOffset.y)
    val p2 = Offset(curWorld.x * scale + panOffset.x, curWorld.y * scale + panOffset.y)
    val previewColor = Color(0xFF38BDF8)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)

    when (tool) {
        CadTool.LINE, CadTool.CENTERLINE, CadTool.MEASURE_TAPE -> {
            drawLine(previewColor, p1, p2, strokeWidth = 2f, pathEffect = dashEffect)
        }
        CadTool.RECTANGLE -> {
            val rx = minOf(p1.x, p2.x)
            val ry = minOf(p1.y, p2.y)
            val rw = abs(p2.x - p1.x)
            val rh = abs(p2.y - p1.y)
            drawRect(previewColor, Offset(rx, ry), Size(rw, rh), style = Stroke(width = 2f, pathEffect = dashEffect))
        }
        CadTool.CIRCLE -> {
            val r = (p2 - p1).getDistance()
            drawCircle(previewColor, radius = r, center = p1, style = Stroke(width = 2f, pathEffect = dashEffect))
        }
        CadTool.CONE_HOPPER -> {
            val tw = abs(p2.x - p1.x)
            val hh = abs(p2.y - p1.y)
            val path = Path().apply {
                moveTo(p1.x, p1.y)
                lineTo(p1.x + tw, p1.y)
                lineTo(p1.x + tw * 0.7f, p1.y + hh)
                lineTo(p1.x + tw * 0.3f, p1.y + hh)
                close()
            }
            drawPath(path, previewColor, style = Stroke(width = 2f, pathEffect = dashEffect))
        }
        else -> {}
    }
}

private fun DrawScope.drawOsnapMarker(snap: OsnapResult, scale: Float, panOffset: Offset) {
    val sp = Offset(snap.point.x * scale + panOffset.x, snap.point.y * scale + panOffset.y)
    val snapColor = Color(0xFF10B981) // High-visibility AutoCAD Green

    when (snap.type) {
        OsnapType.ENDPOINT -> {
            // Square marker
            drawRect(snapColor, Offset(sp.x - 10f, sp.y - 10f), Size(20f, 20f), style = Stroke(width = 2.5f))
        }
        OsnapType.MIDPOINT -> {
            // Triangle marker
            val path = Path().apply {
                moveTo(sp.x, sp.y - 12f)
                lineTo(sp.x + 11f, sp.y + 8f)
                lineTo(sp.x - 11f, sp.y + 8f)
                close()
            }
            drawPath(path, snapColor, style = Stroke(width = 2.5f))
        }
        OsnapType.CENTER -> {
            // Circle with cross marker
            drawCircle(snapColor, radius = 10f, center = sp, style = Stroke(width = 2.5f))
            drawCircle(snapColor, radius = 2f, center = sp)
        }
        OsnapType.QUADRANT -> {
            // Diamond marker
            val path = Path().apply {
                moveTo(sp.x, sp.y - 11f)
                lineTo(sp.x + 11f, sp.y)
                lineTo(sp.x, sp.y + 11f)
                lineTo(sp.x - 11f, sp.y)
                close()
            }
            drawPath(path, snapColor, style = Stroke(width = 2.5f))
        }
        OsnapType.INTERSECTION -> {
            // X Cross marker
            drawLine(snapColor, Offset(sp.x - 9f, sp.y - 9f), Offset(sp.x + 9f, sp.y + 9f), strokeWidth = 2.5f)
            drawLine(snapColor, Offset(sp.x - 9f, sp.y + 9f), Offset(sp.x + 9f, sp.y - 9f), strokeWidth = 2.5f)
        }
        OsnapType.PERPENDICULAR -> {
            // Perpendicular symbol
            drawLine(snapColor, Offset(sp.x - 8f, sp.y + 8f), Offset(sp.x + 8f, sp.y + 8f), strokeWidth = 2.5f)
            drawLine(snapColor, Offset(sp.x, sp.y + 8f), Offset(sp.x, sp.y - 8f), strokeWidth = 2.5f)
        }
        OsnapType.GRID -> {
            drawCircle(Color(0xFF38BDF8), radius = 5f, center = sp, style = Stroke(width = 2f))
        }
    }

    // Snap Tooltip Tag
    drawContext.canvas.nativeCanvas.apply {
        val paint = Paint().apply {
            color = android.graphics.Color.GREEN
            textSize = 24f
            typeface = Typeface.MONOSPACE
            setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
        }
        drawText("${snap.type.label}: ${snap.description}", sp.x + 16f, sp.y - 12f, paint)
    }
}

private fun DrawScope.drawAutoCadCrosshairAndHud(
    curWorld: Offset,
    startWorld: Offset?,
    scale: Float,
    panOffset: Offset,
    canvasW: Float,
    canvasH: Float,
    activeTool: CadTool,
    touchMode: TouchNavigationMode
) {
    val cursorScreen = Offset(curWorld.x * scale + panOffset.x, curWorld.y * scale + panOffset.y)
    val crossColor = Color(0x77FFFFFF)

    // Full Screen Crosshair
    drawLine(crossColor, Offset(0f, cursorScreen.y), Offset(canvasW, cursorScreen.y), strokeWidth = 0.8f)
    drawLine(crossColor, Offset(cursorScreen.x, 0f), Offset(cursorScreen.x, canvasH), strokeWidth = 0.8f)

    // Aperture Pick Box at Center
    drawRect(Color.White, Offset(cursorScreen.x - 6f, cursorScreen.y - 6f), Size(12f, 12f), style = Stroke(width = 1.2f))

    // Dynamic Input Heads-Up Box (Next to Cursor)
    var hudText = "X: ${curWorld.x.roundToInt()}  Y: ${curWorld.y.roundToInt()} mm"
    if (startWorld != null && touchMode == TouchNavigationMode.DRAFT_TOOL) {
        val dx = curWorld.x - startWorld.x
        val dy = curWorld.y - startWorld.y
        val dist = sqrt(dx * dx + dy * dy)
        val angle = (atan2(dy, dx) * 180.0 / PI).let { if (it < 0) it + 360f else it }.toFloat()
        hudText = "Len: ${dist.roundToInt()} mm < ${angle.roundToInt()}° | X:${curWorld.x.roundToInt()} Y:${curWorld.y.roundToInt()}"
    }

    drawContext.canvas.nativeCanvas.apply {
        val bgPaint = Paint().apply {
            color = android.graphics.Color.argb(210, 15, 23, 42)
        }
        val textPaint = Paint().apply {
            color = android.graphics.Color.CYAN
            textSize = 24f
            typeface = Typeface.MONOSPACE
        }
        val textW = textPaint.measureText(hudText)
        val boxX = (cursorScreen.x + 18f).coerceAtMost(canvasW - textW - 30f)
        val boxY = (cursorScreen.y + 35f).coerceAtMost(canvasH - 25f)

        drawRoundRect(boxX - 6f, boxY - 22f, boxX + textW + 8f, boxY + 6f, 6f, 6f, bgPaint)
        drawText(hudText, boxX, boxY, textPaint)
    }
}

private fun findEntityAtWorldPoint(point: Offset, entities: List<CadEntity>, snapRadiusMm: Float): CadEntity? {
    for (entity in entities.reversed()) {
        when (entity) {
            is CadLine -> {
                val dist = CadGeometryEngine.distancePointToSegment(point, Offset(entity.x1, entity.y1), Offset(entity.x2, entity.y2))
                if (dist <= snapRadiusMm) return entity
            }
            is CadRect -> {
                val p1 = Offset(entity.x, entity.y)
                val p2 = Offset(entity.x + entity.width, entity.y)
                val p3 = Offset(entity.x + entity.width, entity.y + entity.height)
                val p4 = Offset(entity.x, entity.y + entity.height)
                val d1 = CadGeometryEngine.distancePointToSegment(point, p1, p2)
                val d2 = CadGeometryEngine.distancePointToSegment(point, p2, p3)
                val d3 = CadGeometryEngine.distancePointToSegment(point, p3, p4)
                val d4 = CadGeometryEngine.distancePointToSegment(point, p4, p1)
                if (minOf(d1, d2, d3, d4) <= snapRadiusMm) return entity
            }
            is CadCircle -> {
                val dist = sqrt((point.x - entity.cx).pow(2) + (point.y - entity.cy).pow(2))
                if (abs(dist - entity.radius) <= snapRadiusMm || dist <= entity.radius) return entity
            }
            is CadArc -> {
                val dist = sqrt((point.x - entity.cx).pow(2) + (point.y - entity.cy).pow(2))
                if (abs(dist - entity.radius) <= snapRadiusMm) return entity
            }
            is CadDishedHead -> {
                val dist = sqrt((point.x - entity.cx).pow(2) + (point.y - entity.cy).pow(2))
                if (dist <= entity.diameterMm * 0.5f + snapRadiusMm) return entity
            }
            is CadSaddleSupport -> {
                if (point.x in (entity.x - entity.baseWidthMm * 0.5f - snapRadiusMm)..(entity.x + entity.baseWidthMm * 0.5f + snapRadiusMm) &&
                    point.y in (entity.y - snapRadiusMm)..(entity.y + entity.saddleHeightMm + snapRadiusMm)
                ) return entity
            }
            is CadLegSupport -> {
                if (abs(point.x - entity.x) <= entity.basePadDiaMm * 0.5f + snapRadiusMm &&
                    point.y in (entity.y - snapRadiusMm)..(entity.y + entity.heightMm + snapRadiusMm)
                ) return entity
            }
            is CadManway -> {
                val dist = sqrt((point.x - entity.x).pow(2) + (point.y - entity.y).pow(2))
                if (dist <= entity.manwayNb * 0.25f + snapRadiusMm) return entity
            }
            is CadValve -> {
                val dist = sqrt((point.x - entity.x).pow(2) + (point.y - entity.y).pow(2))
                if (dist <= entity.lengthMm * 0.6f + snapRadiusMm) return entity
            }
            is CadAgitator -> {
                if (abs(point.x - entity.topX) <= entity.impellerDiaMm * 0.5f + snapRadiusMm &&
                    point.y in (entity.topY - 60f)..(entity.topY + entity.shaftLengthMm + snapRadiusMm)
                ) return entity
            }
            is CadBomItem -> {
                val dist = sqrt((point.x - entity.x).pow(2) + (point.y - entity.y).pow(2))
                if (dist <= 30f) return entity
            }
            is CadDimension -> {
                val dist = CadGeometryEngine.distancePointToSegment(point, Offset(entity.x1, entity.y1), Offset(entity.x2, entity.y2))
                if (dist <= snapRadiusMm + 40f) return entity
            }
            is CadLeader -> {
                val d1 = CadGeometryEngine.distancePointToSegment(point, Offset(entity.targetX, entity.targetY), Offset(entity.elbowX, entity.elbowY))
                if (d1 <= snapRadiusMm) return entity
            }
            is CadDishedHead -> {
                val dist = sqrt((point.x - entity.cx).pow(2) + (point.y - entity.cy).pow(2))
                if (dist <= entity.diameterMm * 0.5f + snapRadiusMm) return entity
            }
            is CadSaddleSupport -> {
                if (point.x in (entity.x - entity.baseWidthMm * 0.5f - snapRadiusMm)..(entity.x + entity.baseWidthMm * 0.5f + snapRadiusMm) &&
                    point.y in (entity.y - snapRadiusMm)..(entity.y + entity.saddleHeightMm + snapRadiusMm)
                ) return entity
            }
            is CadLegSupport -> {
                if (point.x in (entity.x - snapRadiusMm)..(entity.x + entity.basePadDiaMm + snapRadiusMm) &&
                    point.y in (entity.y - snapRadiusMm)..(entity.y + entity.heightMm + snapRadiusMm)
                ) return entity
            }
            is CadManway -> {
                val dist = sqrt((point.x - entity.x).pow(2) + (point.y - entity.y).pow(2))
                if (dist <= entity.manwayNb * 0.5f + snapRadiusMm) return entity
            }
            is CadValve -> {
                val dist = sqrt((point.x - entity.x).pow(2) + (point.y - entity.y).pow(2))
                if (dist <= entity.lengthMm + snapRadiusMm) return entity
            }
            is CadAgitator -> {
                if (point.x in (entity.topX - entity.impellerDiaMm * 0.5f - snapRadiusMm)..(entity.topX + entity.impellerDiaMm * 0.5f + snapRadiusMm) &&
                    point.y in (entity.topY - snapRadiusMm)..(entity.topY + entity.shaftLengthMm + snapRadiusMm)
                ) return entity
            }
            is CadText -> {
                if (point.x in (entity.x - snapRadiusMm)..(entity.x + 200f) &&
                    point.y in (entity.y - entity.textHeightMm - snapRadiusMm)..(entity.y + snapRadiusMm)
                ) return entity
            }
            is CadNozzleComponent -> {
                val dist = sqrt((point.x - entity.x).pow(2) + (point.y - entity.y).pow(2))
                if (dist <= entity.flangeDiaMm * 0.5f + snapRadiusMm) return entity
            }
            is CadHopperCone -> {
                if (point.x in (entity.topX - snapRadiusMm)..(entity.topX + entity.topWidth + snapRadiusMm) &&
                    point.y in (entity.topY - snapRadiusMm)..(entity.topY + entity.height + snapRadiusMm)
                ) return entity
            }
        }
    }
    return null
}
