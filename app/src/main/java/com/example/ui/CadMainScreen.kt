package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.canvas.CadCanvas2D
import com.example.ui.canvas.IsometricViewport3D
import com.example.ui.canvas.Manual3DSolidBuilderViewport
import com.example.ui.components.*
import com.example.ui.dialogs.*
import com.example.ui.print.BlueprintPrintLayoutView
import com.example.ui.titleblock.CadTitleBlockCompose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadMainScreen(
    initialProjects: List<CadProject> = SampleEquipmentDrawings.getAllProjects(),
    modifier: Modifier = Modifier
) {
    var allProjects by remember { mutableStateOf(initialProjects) }
    var currentProjectIndex by remember { mutableIntStateOf(0) }
    val currentProject = allProjects.getOrElse(currentProjectIndex) { initialProjects.first() }

    var currentViewMode by remember { mutableStateOf(CadViewMode.DRAWING_2D) }
    var activeTool by remember { mutableStateOf(CadTool.SELECT) }
    var active3DTool by remember { mutableStateOf(Cad3DTool.ORBIT_PAN) }
    var activeLayerId by remember { mutableStateOf("0") }
    var layers by remember {
        mutableStateOf(
            listOf(
                CadLayer("0", "0 (Continuous Visible)", 0xFFF8FAFC, 0.45f, strokeType = StrokeType.CONTINUOUS),
                CadLayer("CENTER", "CENTER (Axis & PCD)", 0xFF10B981, 0.25f, strokeType = StrokeType.CENTER_LINE),
                CadLayer("HIDDEN", "HIDDEN (Dashed Edge)", 0xFF94A3B8, 0.25f, strokeType = StrokeType.DASHED_HIDDEN),
                CadLayer("DIM", "DIMENSIONS (Red mm)", 0xFFEF4444, 0.30f),
                CadLayer("NOZZLE", "NOZZLE & FLANGES", 0xFF0284C7, 0.40f),
                CadLayer("TEXT", "ANNOTATIONS & SPECS", 0xFFF59E0B, 0.35f)
            )
        )
    }

    var selectedEntityId by remember { mutableStateOf<String?>(null) }
    var isOrthoEnabled by remember { mutableStateOf(false) }
    var isSnapEnabled by remember { mutableStateOf(true) }
    var themeMode by remember { mutableStateOf(CanvasThemeMode.AUTOCAD_CLASSIC_BLACK) }

    var scale by remember { mutableFloatStateOf(0.75f) }
    var panOffset by remember { mutableStateOf(Offset(200f, 150f)) }

    var pointerWorldX by remember { mutableFloatStateOf(0f) }
    var pointerWorldY by remember { mutableFloatStateOf(0f) }
    var measuredDist by remember { mutableFloatStateOf(0f) }
    var measuredAngle by remember { mutableFloatStateOf(0f) }

    // 3D Solid Model Manual Builder State
    var solid3DParts by remember {
        mutableStateOf(
            listOf(
                Solid3DPart(name = "Main Shell Plate", shapeType = Solid3DShapeType.CYLINDER, posX = 0f, posY = 0f, posZ = 0f, dimX = 260f, dimY = 320f, dimZ = 260f, color = Color(0xFF94A3B8)),
                Solid3DPart(name = "Hopper Discharge Cone", shapeType = Solid3DShapeType.CONE, posX = 0f, posY = -220f, posZ = 0f, dimX = 260f, dimY = 160f, dimZ = 50f, color = Color(0xFFCBD5E1)),
                Solid3DPart(name = "50 NB Top Nozzle Shell", shapeType = Solid3DShapeType.CYLINDER, posX = 0f, posY = 200f, posZ = 0f, dimX = 60f, dimY = 80f, dimZ = 60f, color = Color(0xFF38BDF8)),
                Solid3DPart(name = "Inspection Port Cutout", shapeType = Solid3DShapeType.BOX, booleanOp = Solid3DBooleanOp.SUBTRACT, posX = 80f, posY = 50f, posZ = 120f, dimX = 90f, dimY = 60f, dimZ = 40f, color = Color(0x99EF4444))
            )
        )
    }
    var selected3DPartId by remember { mutableStateOf<String?>(solid3DParts.firstOrNull()?.id) }

    // User Profile & Account
    var userProfile by remember {
        mutableStateOf(
            UserProfile(
                uid = "eng_4491",
                email = "engineer@ssengineering.com",
                displayName = "Ripen Koli (CAD Senior Drafter)",
                membershipPlan = MembershipTier.PRO_ENGINEER,
                organization = "Shree Sai Engineering Works",
                projectsCount = allProjects.size
            )
        )
    }
    var isLoggedIn by remember { mutableStateOf(true) }

    // Dialogs
    var showProjectsDialog by remember { mutableStateOf(false) }
    var showEditTitleBlockDialog by remember { mutableStateOf(false) }
    var showLayersDialog by remember { mutableStateOf(false) }
    var showDxfExportDialog by remember { mutableStateOf(false) }
    var showBomDialog by remember { mutableStateOf(false) }
    var showUnfoldDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }

    // Active Dimensioning Request State
    var pendingDimStart by remember { mutableStateOf<Offset?>(null) }
    var pendingDimEnd by remember { mutableStateOf<Offset?>(null) }

    val undoStack = remember { mutableStateListOf<List<CadEntity>>() }
    val redoStack = remember { mutableStateListOf<List<CadEntity>>() }

    fun pushUndo() {
        undoStack.add(currentProject.entities)
        redoStack.clear()
        if (undoStack.size > 25) undoStack.removeAt(0)
    }

    fun updateProjectEntities(newEntities: List<CadEntity>) {
        pushUndo()
        val updated = currentProject.copy(entities = newEntities)
        val list = allProjects.toMutableList()
        list[currentProjectIndex] = updated
        allProjects = list
    }

    fun updateTitleBlock(newTitleBlock: TitleBlockInfo) {
        val updated = currentProject.copy(titleBlock = newTitleBlock)
        val list = allProjects.toMutableList()
        list[currentProjectIndex] = updated
        allProjects = list
    }

    fun handleAutoCadCommand(rawCommand: String) {
        val cmd = rawCommand.trim().uppercase()
        when {
            cmd == "L" || cmd == "LINE" -> activeTool = CadTool.LINE
            cmd == "REC" || cmd == "RECT" || cmd == "RECTANGLE" -> activeTool = CadTool.RECTANGLE
            cmd == "C" || cmd == "CIRCLE" -> activeTool = CadTool.CIRCLE
            cmd == "ARC" || cmd == "A" -> activeTool = CadTool.ARC
            cmd == "DIM" || cmd == "DLI" || cmd == "DAL" -> activeTool = CadTool.DIMENSION
            cmd == "DISH" || cmd == "DISHED" -> activeTool = CadTool.DISHED_HEAD_STAMP
            cmd == "SADDLE" -> activeTool = CadTool.SADDLE_SUPPORT
            cmd == "LEG" -> activeTool = CadTool.LEG_SUPPORT
            cmd == "NOZZLE" || cmd == "NOZ" -> activeTool = CadTool.NOZZLE_STAMP
            cmd == "VALVE" -> activeTool = CadTool.VALVE_STAMP
            cmd == "MANWAY" || cmd == "MW" -> activeTool = CadTool.MANWAY_STAMP
            cmd == "AGITATOR" || cmd == "AGIT" -> activeTool = CadTool.AGITATOR_STAMP
            cmd == "CONE" || cmd == "HOPPER" -> activeTool = CadTool.CONE_HOPPER
            cmd == "BOM" -> showBomDialog = true
            cmd == "UNFOLD" || cmd == "FLAT" -> showUnfoldDialog = true
            cmd == "RO" || cmd == "ROTATE" -> activeTool = CadTool.ROTATE
            cmd == "M" || cmd == "MOVE" || cmd == "SEL" -> activeTool = CadTool.SELECT
            cmd == "PAN" || cmd == "P" -> activeTool = CadTool.PAN_ZOOM
            cmd == "E" || cmd == "ERASE" || cmd == "DEL" -> {
                if (selectedEntityId != null) {
                    updateProjectEntities(currentProject.entities.filterNot { it.id == selectedEntityId })
                    selectedEntityId = null
                } else {
                    activeTool = CadTool.ERASE
                }
            }
            cmd == "ZE" || cmd == "ZOOM EXTENTS" || cmd == "Z" -> {
                scale = 0.65f
                panOffset = Offset(200f, 150f)
            }
            cmd == "3D" || cmd == "ISO" -> currentViewMode = CadViewMode.ISOMETRIC_3D
            cmd == "2D" || cmd == "PLAN" -> currentViewMode = CadViewMode.DRAWING_2D
            cmd == "PRINT" || cmd == "PLOT" -> currentViewMode = CadViewMode.PRINT_SHEET
            cmd == "EXPORT" || cmd == "DXF" -> showDxfExportDialog = true
            cmd == "LAY" || cmd == "LAYER" -> showLayersDialog = true
            cmd == "ORTHO" || cmd == "F8" -> isOrthoEnabled = !isOrthoEnabled
            cmd == "SNAP" || cmd == "OSNAP" || cmd == "F9" -> isSnapEnabled = !isSnapEnabled
            cmd == "UNDO" || cmd == "U" -> {
                if (undoStack.isNotEmpty()) {
                    redoStack.add(currentProject.entities)
                    val prev = undoStack.removeAt(undoStack.lastIndex)
                    val updated = currentProject.copy(entities = prev)
                    val list = allProjects.toMutableList()
                    list[currentProjectIndex] = updated
                    allProjects = list
                }
            }
        }
    }

    val selectedEntity = remember(currentProject.entities, selectedEntityId) {
        currentProject.entities.find { it.id == selectedEntityId }
    }

    if (currentViewMode == CadViewMode.PRINT_SHEET) {
        BlueprintPrintLayoutView(
            project = currentProject,
            onBackToDrafting = { currentViewMode = CadViewMode.DRAWING_2D },
            onEditTitleBlock = { showEditTitleBlockDialog = true }
        )
    } else {
        Scaffold(
            topBar = {
                Surface(
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Column {
                        // Top row: App Name, Project switcher, View Mode Segmented Controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Project Switcher
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showProjectsDialog = true }
                                    .padding(end = 6.dp)
                                    .testTag("project_selector_btn")
                            ) {
                                Surface(
                                    color = Color(0xFF0284C7),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Engineering,
                                        contentDescription = "CAD",
                                        tint = Color.White,
                                        modifier = Modifier.padding(3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = currentProject.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "Select Project",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = "MOC: ${currentProject.titleBlock.materialOfConstruction}",
                                        fontSize = 9.sp,
                                        color = Color(0xFFDC2626),
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // View Mode Tabs
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                FilterChip(
                                    selected = currentViewMode == CadViewMode.DRAWING_2D,
                                    onClick = { currentViewMode = CadViewMode.DRAWING_2D },
                                    label = { Text("2D CAD", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF0284C7),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFF1E293B),
                                        labelColor = Color(0xFF94A3B8)
                                    ),
                                    modifier = Modifier.height(28.dp).testTag("tab_2d_blueprint")
                                )

                                FilterChip(
                                    selected = currentViewMode == CadViewMode.ISOMETRIC_3D,
                                    onClick = { currentViewMode = CadViewMode.ISOMETRIC_3D },
                                    label = { Text("3D Iso", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF0284C7),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFF1E293B),
                                        labelColor = Color(0xFF94A3B8)
                                    ),
                                    modifier = Modifier.height(28.dp).testTag("tab_3d_isometric")
                                )

                                FilterChip(
                                    selected = currentViewMode == CadViewMode.BUILDER_3D_SOLID,
                                    onClick = { currentViewMode = CadViewMode.BUILDER_3D_SOLID },
                                    label = { Text("3D Solid", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF007AFF),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFF1E293B),
                                        labelColor = Color(0xFF38BDF8)
                                    ),
                                    modifier = Modifier.height(28.dp).testTag("tab_3d_builder")
                                )

                                FilterChip(
                                    selected = false,
                                    onClick = { currentViewMode = CadViewMode.PRINT_SHEET },
                                    label = { Text("Plot", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color(0xFF065F46),
                                        labelColor = Color(0xFF6EE7B7)
                                    ),
                                    modifier = Modifier.height(28.dp).testTag("tab_print_sheet")
                                )

                                IconButton(
                                    onClick = { showAccountDialog = true },
                                    modifier = Modifier.size(28.dp).testTag("open_account_btn")
                                ) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = "Account", tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        // Second Header Row: Undo/Redo, Title Block, Unfolder, BOM, DXF, Layer Manager
                        // Horizontally scrollable so it never wraps or squishes text!
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0B132B))
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (undoStack.isNotEmpty()) {
                                        redoStack.add(currentProject.entities)
                                        val prev = undoStack.removeAt(undoStack.lastIndex)
                                        val updated = currentProject.copy(entities = prev)
                                        val list = allProjects.toMutableList()
                                        list[currentProjectIndex] = updated
                                        allProjects = list
                                    }
                                },
                                enabled = undoStack.isNotEmpty(),
                                modifier = Modifier.size(26.dp).testTag("undo_button")
                            ) {
                                Icon(Icons.Default.Undo, contentDescription = "Undo", tint = if (undoStack.isNotEmpty()) Color.White else Color(0xFF475569), modifier = Modifier.size(15.dp))
                            }

                            IconButton(
                                onClick = {
                                    if (redoStack.isNotEmpty()) {
                                        undoStack.add(currentProject.entities)
                                        val next = redoStack.removeAt(redoStack.lastIndex)
                                        val updated = currentProject.copy(entities = next)
                                        val list = allProjects.toMutableList()
                                        list[currentProjectIndex] = updated
                                        allProjects = list
                                    }
                                },
                                enabled = redoStack.isNotEmpty(),
                                modifier = Modifier.size(26.dp).testTag("redo_button")
                            ) {
                                Icon(Icons.Default.Redo, contentDescription = "Redo", tint = if (redoStack.isNotEmpty()) Color.White else Color(0xFF475569), modifier = Modifier.size(15.dp))
                            }

                            VerticalDivider(modifier = Modifier.height(16.dp).padding(horizontal = 2.dp))

                            Surface(
                                onClick = { showLayersDialog = true },
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.testTag("layers_dialog_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Layers, contentDescription = "Layers", tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Layers (${layers.size})", fontSize = 10.sp, color = Color.White)
                                }
                            }

                            Surface(
                                onClick = { showEditTitleBlockDialog = true },
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.testTag("title_block_dialog_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Article, contentDescription = "Title Block", tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Title Block", fontSize = 10.sp, color = Color.White)
                                }
                            }

                            Surface(
                                onClick = { showUnfoldDialog = true },
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.testTag("unfolder_dialog_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Architecture, contentDescription = "Unfold", tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Unfold Pattern", fontSize = 10.sp, color = Color.White)
                                }
                            }

                            Surface(
                                onClick = { showBomDialog = true },
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.testTag("bom_dialog_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ListAlt, contentDescription = "BOM", tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("BOM Schedule", fontSize = 10.sp, color = Color.White)
                                }
                            }

                            Surface(
                                onClick = { showDxfExportDialog = true },
                                color = Color(0xFF065F46),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.testTag("dxf_export_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = "DXF", tint = Color(0xFF6EE7B7), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DXF Export", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentViewMode) {
                    CadViewMode.DRAWING_2D -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            CadRibbonToolbar(
                                activeTool = activeTool,
                                onToolSelected = {
                                    activeTool = it
                                    if (it != CadTool.SELECT && it != CadTool.ROTATE) {
                                        selectedEntityId = null
                                    }
                                },
                                onOpenUnfoldCalculator = { showUnfoldDialog = true },
                                onOpenBomTable = { showBomDialog = true }
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                CadCanvas2D(
                                    project = currentProject,
                                    activeTool = activeTool,
                                    activeLayerId = activeLayerId,
                                    layers = layers,
                                    selectedEntityId = selectedEntityId,
                                    isOrthoEnabled = isOrthoEnabled,
                                    isSnapEnabled = isSnapEnabled,
                                    themeMode = themeMode,
                                    scale = scale,
                                    panOffset = panOffset,
                                    onScaleChange = { scale = it },
                                    onPanChange = { panOffset = it },
                                    onPointerMoved = { wx, wy ->
                                        pointerWorldX = wx
                                        pointerWorldY = wy
                                    },
                                    onMeasuredDistance = { d, a ->
                                        measuredDist = d
                                        measuredAngle = a
                                    },
                                    onEntitySelected = { entity ->
                                        selectedEntityId = entity?.id
                                    },
                                    onAddEntity = { newEntity ->
                                        updateProjectEntities(currentProject.entities + newEntity)
                                    },
                                    onDeleteEntity = { delId ->
                                        updateProjectEntities(currentProject.entities.filterNot { it.id == delId })
                                    },
                                    onUpdateEntity = { updated ->
                                        updateProjectEntities(currentProject.entities.map { if (it.id == updated.id) updated else it })
                                    },
                                    onDimensionPlacementRequest = { start, end ->
                                        pendingDimStart = start
                                        pendingDimEnd = end
                                    },
                                    onToggleOrtho = { isOrthoEnabled = !isOrthoEnabled },
                                    onToggleSnap = { isSnapEnabled = !isSnapEnabled }
                                )

                                // Floating Measurement / Direct Entry Docks:
                                if (selectedEntity != null) {
                                    CadEntityEditorDock(
                                        selectedEntity = selectedEntity,
                                        onEntityUpdated = { updated ->
                                            updateProjectEntities(currentProject.entities.map { if (it.id == updated.id) updated else it })
                                        },
                                        onEntityDeleted = { delId ->
                                            updateProjectEntities(currentProject.entities.filterNot { it.id == delId })
                                            selectedEntityId = null
                                        },
                                        onEntityDuplicated = { dup ->
                                            updateProjectEntities(currentProject.entities + dup)
                                            selectedEntityId = dup.id
                                        },
                                        onDeselect = { selectedEntityId = null },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                } else if (activeTool == CadTool.LINE) {
                                    LineMeasurementDock(
                                        currentStartX = pointerWorldX,
                                        currentStartY = pointerWorldY,
                                        onAddLine = { lengthMm, angleDeg ->
                                            val rad = Math.toRadians(angleDeg.toDouble())
                                            val endX = (pointerWorldX + lengthMm * kotlin.math.cos(rad)).toFloat()
                                            val endY = (pointerWorldY + lengthMm * kotlin.math.sin(rad)).toFloat()
                                            updateProjectEntities(
                                                currentProject.entities + CadLine(
                                                    x1 = pointerWorldX,
                                                    y1 = pointerWorldY,
                                                    x2 = endX,
                                                    y2 = endY,
                                                    layerId = activeLayerId
                                                )
                                            )
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                } else if (activeTool == CadTool.RECTANGLE) {
                                    RectangleMeasurementDock(
                                        currentX = pointerWorldX,
                                        currentY = pointerWorldY,
                                        onAddRect = { width, height ->
                                            updateProjectEntities(
                                                currentProject.entities + CadRect(
                                                    x = pointerWorldX,
                                                    y = pointerWorldY,
                                                    width = width,
                                                    height = height,
                                                    layerId = activeLayerId
                                                )
                                            )
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                } else if (activeTool == CadTool.CIRCLE) {
                                    CircleMeasurementDock(
                                        currentX = pointerWorldX,
                                        currentY = pointerWorldY,
                                        onAddCircle = { diameterMm, isPcd ->
                                            updateProjectEntities(
                                                currentProject.entities + CadCircle(
                                                    cx = pointerWorldX,
                                                    cy = pointerWorldY,
                                                    radius = diameterMm * 0.5f,
                                                    isPcd = isPcd,
                                                    layerId = if (isPcd) "CENTER" else activeLayerId
                                                )
                                            )
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                } else if (activeTool == CadTool.DISHED_HEAD_STAMP) {
                                    DishedHeadStampDock(
                                        currentX = pointerWorldX,
                                        currentY = pointerWorldY,
                                        onAddDishedHead = { headType, diameterMm, straightFaceMm, thicknessMm, isFacingUp ->
                                            updateProjectEntities(
                                                currentProject.entities + CadDishedHead(
                                                    cx = pointerWorldX,
                                                    cy = pointerWorldY,
                                                    diameterMm = diameterMm,
                                                    straightFaceMm = straightFaceMm,
                                                    dishType = headType,
                                                    thicknessMm = thicknessMm,
                                                    isFacingUp = isFacingUp,
                                                    layerId = activeLayerId
                                                )
                                            )
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                 } else if (activeTool == CadTool.SADDLE_SUPPORT) {
                                    SaddleSupportStampDock(
                                        currentX = pointerWorldX,
                                        currentY = pointerWorldY,
                                        onAddSaddle = { vesselDia, saddleH, baseW, webThk ->
                                            updateProjectEntities(
                                                currentProject.entities + CadSaddleSupport(
                                                    x = pointerWorldX,
                                                    y = pointerWorldY,
                                                    vesselDiaMm = vesselDia,
                                                    saddleHeightMm = saddleH,
                                                    baseWidthMm = baseW,
                                                    layerId = activeLayerId
                                                )
                                            )
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                } else if (activeTool == CadTool.LEG_SUPPORT) {
                                    LegSupportStampDock(
                                        currentX = pointerWorldX,
                                        currentY = pointerWorldY,
                                        onAddLeg = { pipeNb, h, bp ->
                                            updateProjectEntities(
                                                currentProject.entities + CadLegSupport(
                                                    x = pointerWorldX,
                                                    y = pointerWorldY,
                                                    legPipeNb = pipeNb,
                                                    heightMm = h,
                                                    basePadDiaMm = bp,
                                                    layerId = activeLayerId
                                                )
                                            )
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                } else if (activeTool == CadTool.MANWAY_STAMP) {
                                    ManwayStampDock(
                                        currentX = pointerWorldX,
                                        currentY = pointerWorldY,
                                        onAddManway = { manwayNb, proj, withDavit ->
                                            updateProjectEntities(
                                                currentProject.entities + CadManway(
                                                    x = pointerWorldX,
                                                    y = pointerWorldY,
                                                    manwayNb = manwayNb,
                                                    projectionMm = proj,
                                                    hasDavitArm = withDavit,
                                                    layerId = activeLayerId
                                                )
                                            )
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                } else if (activeTool == CadTool.VALVE_STAMP) {
                                    ValveStampDock(
                                        currentX = pointerWorldX,
                                        currentY = pointerWorldY,
                                        onAddValve = { vType, sizeNb, len, isVert ->
                                            updateProjectEntities(
                                                currentProject.entities + CadValve(
                                                    x = pointerWorldX,
                                                    y = pointerWorldY,
                                                    valveType = vType,
                                                    sizeNb = sizeNb,
                                                    lengthMm = len,
                                                    isVertical = isVert,
                                                    layerId = activeLayerId
                                                )
                                            )
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                } else if (activeTool == CadTool.AGITATOR_STAMP) {
                                    AgitatorStampDock(
                                        currentX = pointerWorldX,
                                        currentY = pointerWorldY,
                                        onAddAgitator = { kw, sl, span, rpm ->
                                            updateProjectEntities(
                                                currentProject.entities + CadAgitator(
                                                    topX = pointerWorldX,
                                                    topY = pointerWorldY,
                                                    motorKw = kw,
                                                    shaftLengthMm = sl,
                                                    impellerDiaMm = span,
                                                    rpm = rpm,
                                                    layerId = activeLayerId
                                                )
                                            )
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                } else if (activeTool == CadTool.BOM_ITEM_BUBBLE) {
                                    BomItemBubbleDock(
                                        currentX = pointerWorldX,
                                        currentY = pointerWorldY,
                                        onAddBomItem = { itemNo, desc, qty, mat ->
                                            updateProjectEntities(
                                                currentProject.entities + CadBomItem(
                                                    x = pointerWorldX,
                                                    y = pointerWorldY,
                                                    itemNumber = itemNo,
                                                    description = desc,
                                                    qty = qty,
                                                    material = mat,
                                                    layerId = activeLayerId
                                                )
                                            )
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                } else if (activeTool == CadTool.NOZZLE_STAMP) {
                                    NozzleStampDock(
                                        currentX = pointerWorldX,
                                        currentY = pointerWorldY,
                                        onAddNozzle = { sizeNb, rating, facing ->
                                            updateProjectEntities(
                                                currentProject.entities + CadNozzleComponent(
                                                    x = pointerWorldX,
                                                    y = pointerWorldY,
                                                    sizeNb = sizeNb,
                                                    flangeRating = rating,
                                                    facing = facing,
                                                    layerId = "NOZZLE"
                                                )
                                            )
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                } else if (activeTool == CadTool.DIMENSION) {
                                    DimensionMeasurementDock(
                                        startPoint = pendingDimStart,
                                        endPoint = pendingDimEnd,
                                        onPlaceDimension = { dimType, offsetDist, prefix ->
                                            val p1 = pendingDimStart ?: Offset(pointerWorldX, pointerWorldY)
                                            val p2 = pendingDimEnd ?: Offset(pointerWorldX + 200f, pointerWorldY)
                                            updateProjectEntities(
                                                currentProject.entities + CadDimension(
                                                    x1 = p1.x,
                                                    y1 = p1.y,
                                                    x2 = p2.x,
                                                    y2 = p2.y,
                                                    type = dimType,
                                                    offsetDistance = offsetDist,
                                                    prefix = prefix,
                                                    layerId = "DIM"
                                                )
                                            )
                                            pendingDimStart = null
                                            pendingDimEnd = null
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }

                            // AutoCAD Command Line Dock
                            AutoCadCommandLine(
                                onExecuteCommand = { cmd -> handleAutoCadCommand(cmd) },
                                activeTool = activeTool
                            )

                            CadStatusBar(
                                currentX = pointerWorldX,
                                currentY = pointerWorldY,
                                measuredDist = measuredDist,
                                measuredAngle = measuredAngle,
                                isOrthoEnabled = isOrthoEnabled,
                                onToggleOrtho = { isOrthoEnabled = !isOrthoEnabled },
                                isSnapEnabled = isSnapEnabled,
                                onToggleSnap = { isSnapEnabled = !isSnapEnabled },
                                canvasTheme = themeMode,
                                onCycleTheme = {
                                    themeMode = when (themeMode) {
                                        CanvasThemeMode.AUTOCAD_CLASSIC_BLACK -> CanvasThemeMode.BLUEPRINT_CYAN
                                        CanvasThemeMode.BLUEPRINT_CYAN -> CanvasThemeMode.LIGHT_ENGINEERING_WHITE
                                        CanvasThemeMode.LIGHT_ENGINEERING_WHITE -> CanvasThemeMode.AUTOCAD_CLASSIC_BLACK
                                    }
                                }
                            )
                        }
                    }

                    CadViewMode.ISOMETRIC_3D -> {
                        IsometricViewport3D(
                            project = currentProject,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    CadViewMode.BUILDER_3D_SOLID -> {
                        val activePart = remember(solid3DParts, selected3DPartId) {
                            solid3DParts.find { it.id == selected3DPartId }
                        }

                        Column(modifier = Modifier.fillMaxSize()) {
                            Cad3DToolbar(
                                activeTool = active3DTool,
                                onToolSelected = { active3DTool = it }
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                Manual3DSolidBuilderViewport(
                                    parts = solid3DParts,
                                    active3DTool = active3DTool,
                                    selectedPartId = selected3DPartId,
                                    onSelectPart = { selected3DPartId = it },
                                    modifier = Modifier.fillMaxSize()
                                )

                                Cad3DActiveToolInspector(
                                    activeTool = active3DTool,
                                    selectedPart = activePart,
                                    allParts = solid3DParts,
                                    onUpdatePart = { updated ->
                                        solid3DParts = solid3DParts.map { if (it.id == updated.id) updated else it }
                                    },
                                    onAddPrimitive = { shapeType ->
                                        val newPart = Solid3DPart(
                                            name = "${shapeType.label} ${solid3DParts.size + 1}",
                                            shapeType = shapeType,
                                            posX = 0f,
                                            posY = 0f,
                                            posZ = 0f,
                                            dimX = 200f,
                                            dimY = 200f,
                                            dimZ = 200f,
                                            color = Color(0xFFCBD5E1)
                                        )
                                        solid3DParts = solid3DParts + newPart
                                        selected3DPartId = newPart.id
                                    },
                                    onDeletePart = { delId ->
                                        solid3DParts = solid3DParts.filterNot { it.id == delId }
                                        selected3DPartId = solid3DParts.firstOrNull()?.id
                                    },
                                    onDuplicatePart = { part ->
                                        val dup = part.copy(
                                            id = java.util.UUID.randomUUID().toString(),
                                            name = "${part.name} (Copy)",
                                            posX = part.posX + 40f,
                                            posZ = part.posZ + 40f
                                        )
                                        solid3DParts = solid3DParts + dup
                                        selected3DPartId = dup.id
                                    },
                                    onDismissInspector = { active3DTool = Cad3DTool.ORBIT_PAN },
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )
                            }
                        }
                    }

                    CadViewMode.PRINT_SHEET -> {
                        // Handled above in root if-check
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (showProjectsDialog) {
        ProjectsListDialog(
            projects = allProjects,
            currentProject = currentProject,
            onSelectProject = { p ->
                val idx = allProjects.indexOf(p)
                if (idx >= 0) currentProjectIndex = idx
            },
            onNewProject = {
                val newPrj = CadProject(
                    name = "New Equipment Drawing #${allProjects.size + 1}",
                    subtitle = "Stainless Steel Fabrication Blueprint",
                    entities = emptyList(),
                    titleBlock = TitleBlockInfo(drawingTitle = "UNTITLED EQUIPMENT", drawingNo = "SSEC/${allProjects.size + 100}")
                )
                allProjects = allProjects + newPrj
                currentProjectIndex = allProjects.lastIndex
            },
            onDismiss = { showProjectsDialog = false }
        )
    }

    if (showEditTitleBlockDialog) {
        EditTitleBlockDialog(
            currentInfo = currentProject.titleBlock,
            onSave = { updated ->
                updateTitleBlock(updated)
                showEditTitleBlockDialog = false
            },
            onDismiss = { showEditTitleBlockDialog = false }
        )
    }

    if (showLayersDialog) {
        CadLayerDialog(
            layers = layers,
            activeLayerId = activeLayerId,
            onLayerSelected = { activeLayerId = it },
            onToggleVisibility = { lid ->
                layers = layers.map { if (it.id == lid) it.copy(isVisible = !it.isVisible) else it }
            },
            onDismiss = { showLayersDialog = false }
        )
    }

    if (showDxfExportDialog) {
        DxfExportDialog(
            project = currentProject,
            onDismiss = { showDxfExportDialog = false }
        )
    }

    if (showBomDialog) {
        BomManagerDialog(
            project = currentProject,
            onDismiss = { showBomDialog = false },
            onStampBomTable = { bomEntities ->
                updateProjectEntities(currentProject.entities + bomEntities)
            }
        )
    }

    if (showUnfoldDialog) {
        SheetMetalUnfoldDialog(
            onDismiss = { showUnfoldDialog = false },
            onStampPatternOntoDrawing = { patternEntities ->
                updateProjectEntities(currentProject.entities + patternEntities)
            }
        )
    }

    if (showAccountDialog) {
        UserAccountDialog(
            userProfile = userProfile,
            isLoggedIn = isLoggedIn,
            onSignIn = { email, name ->
                userProfile = userProfile.copy(email = email, displayName = name)
                isLoggedIn = true
                showAccountDialog = false
            },
            onSignOut = {
                isLoggedIn = false
                showAccountDialog = false
            },
            onUpgradePlan = { newTier ->
                userProfile = userProfile.copy(membershipPlan = newTier)
            },
            onDismiss = { showAccountDialog = false }
        )
    }
}
