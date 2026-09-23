package com.example.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class StrokeType {
    CONTINUOUS,
    CENTER_LINE,
    DASHED_HIDDEN
}

enum class DimensionType {
    LINEAR_HORIZONTAL,
    LINEAR_VERTICAL,
    ALIGNED,
    DIAMETER,
    RADIUS
}

enum class CadTool {
    SELECT,
    ROTATE,
    PAN_ZOOM,
    LINE,
    POLYLINE,
    RECTANGLE,
    CIRCLE,
    ARC,
    CENTERLINE,
    NOZZLE_STAMP,
    DISHED_HEAD_STAMP,
    SADDLE_SUPPORT,
    LEG_SUPPORT,
    MANWAY_STAMP,
    VALVE_STAMP,
    AGITATOR_STAMP,
    BOM_ITEM_BUBBLE,
    CONE_HOPPER,
    DIMENSION,
    LEADER_NOTE,
    TEXT_NOTE,
    OFFSET_WALL,
    MEASURE_TAPE,
    ERASE
}

enum class TouchNavigationMode {
    PAN_NAVIGATE,   // 1-finger effortlessly pans/navigates without drawing accidental lines
    DRAFT_TOOL,     // 1-finger creates/draws geometry with crosshairs and OSNAP
    SELECT_MODIFY   // 1-finger taps to select or drags to translate entities
}

enum class OsnapType(val glyphName: String, val label: String) {
    ENDPOINT("🟩", "Endpoint"),
    MIDPOINT("🔺", "Midpoint"),
    CENTER("⭕", "Center"),
    QUADRANT("🔶", "Quadrant"),
    INTERSECTION("❌", "Intersection"),
    PERPENDICULAR("⟂", "Perpendicular"),
    GRID("✛", "Grid")
}

data class OsnapResult(
    val point: Offset,
    val type: OsnapType,
    val description: String
)

enum class DishedHeadType(val displayName: String) {
    ELLIPSOIDAL_2_TO_1("2:1 Ellipsoidal Dished Head"),
    TORISPHERICAL("Torispherical (Klöpper) Head"),
    HEMISPHERICAL("Hemispherical Dished End"),
    FLAT_FLANGED("Flat Flanged Head")
}

enum class ValveType(val displayName: String) {
    GATE_VALVE("Gate Valve"),
    GLOBE_VALVE("Globe Valve"),
    BALL_VALVE("Ball Valve"),
    CHECK_VALVE("Non-Return Check Valve"),
    BUTTERFLY_VALVE("Butterfly Valve")
}

enum class CadViewMode {
    DRAWING_2D,
    ISOMETRIC_3D,
    BUILDER_3D_SOLID,
    PRINT_SHEET
}

enum class CanvasThemeMode {
    AUTOCAD_CLASSIC_BLACK,
    BLUEPRINT_CYAN,
    LIGHT_ENGINEERING_WHITE
}

enum class NozzleFacing {
    NORTH, SOUTH, EAST, WEST
}

data class CadLayer(
    val id: String,
    val name: String,
    val colorArgb: Long,
    val strokeWidthMm: Float = 0.5f,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val strokeType: StrokeType = StrokeType.CONTINUOUS
) {
    val composeColor: Color get() = Color(colorArgb)
}

sealed interface CadEntity {
    val id: String
    val layerId: String
}

data class CadLine(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val strokeType: StrokeType = StrokeType.CONTINUOUS,
    val strokeWidthMm: Float = 0.5f,
    val customColorArgb: Long? = null,
    override val layerId: String = "0",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class CadRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val plateThicknessMm: Float = 0f,
    val isFilled: Boolean = false,
    val customColorArgb: Long? = null,
    override val layerId: String = "0",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class CadCircle(
    val cx: Float,
    val cy: Float,
    val radius: Float,
    val isPcd: Boolean = false,
    val customColorArgb: Long? = null,
    override val layerId: String = "0",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class CadArc(
    val cx: Float,
    val cy: Float,
    val radius: Float,
    val startAngleDeg: Float,
    val sweepAngleDeg: Float,
    val strokeType: StrokeType = StrokeType.CONTINUOUS,
    val customColorArgb: Long? = null,
    override val layerId: String = "0",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class CadDimension(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val type: DimensionType = DimensionType.ALIGNED,
    val offsetDistance: Float = -50f,
    val textOverride: String? = null,
    val prefix: String = "",
    val suffix: String = " mm",
    override val layerId: String = "DIM",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class CadLeader(
    val targetX: Float,
    val targetY: Float,
    val elbowX: Float,
    val elbowY: Float,
    val text: String,
    val nozzleSizeNb: String = "50 NB",
    override val layerId: String = "TEXT",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class CadText(
    val x: Float,
    val y: Float,
    val text: String,
    val textHeightMm: Float = 16f,
    val isBold: Boolean = false,
    val customColorArgb: Long? = null,
    override val layerId: String = "TEXT",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class CadNozzleComponent(
    val x: Float,
    val y: Float,
    val sizeNb: Int = 50,
    val flangeRating: String = "150# RF",
    val facing: NozzleFacing = NozzleFacing.NORTH,
    val flangeDiaMm: Float = 120f,
    val neckLengthMm: Float = 80f,
    val angleDeg: Float = 0f,
    val label: String = "NOZZLE",
    override val layerId: String = "NOZZLE",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class CadHopperCone(
    val topX: Float,
    val topY: Float,
    val topWidth: Float,
    val bottomWidth: Float,
    val height: Float,
    val wallThicknessMm: Float = 5f,
    override val layerId: String = "0",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class CadDishedHead(
    val cx: Float,
    val cy: Float,
    val diameterMm: Float = 600f,
    val straightFaceMm: Float = 40f,
    val dishType: DishedHeadType = DishedHeadType.ELLIPSOIDAL_2_TO_1,
    val isFacingUp: Boolean = true,
    val thicknessMm: Float = 5f,
    override val layerId: String = "0",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class CadSaddleSupport(
    val x: Float,
    val y: Float,
    val vesselDiaMm: Float = 600f,
    val saddleHeightMm: Float = 250f,
    val baseWidthMm: Float = 400f,
    val ribCount: Int = 2,
    override val layerId: String = "0",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class CadLegSupport(
    val x: Float,
    val y: Float,
    val legPipeNb: Int = 50,
    val heightMm: Float = 300f,
    val basePadDiaMm: Float = 120f,
    override val layerId: String = "0",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class CadManway(
    val x: Float,
    val y: Float,
    val manwayNb: Int = 500,
    val projectionMm: Float = 150f,
    val hasDavitArm: Boolean = true,
    val facing: NozzleFacing = NozzleFacing.EAST,
    override val layerId: String = "NOZZLE",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class CadValve(
    val x: Float,
    val y: Float,
    val valveType: ValveType = ValveType.GATE_VALVE,
    val sizeNb: Int = 50,
    val lengthMm: Float = 80f,
    val isVertical: Boolean = false,
    override val layerId: String = "NOZZLE",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class CadAgitator(
    val topX: Float,
    val topY: Float,
    val shaftLengthMm: Float = 500f,
    val impellerDiaMm: Float = 250f,
    val motorKw: Float = 1.5f,
    val rpm: Int = 1440,
    override val layerId: String = "0",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class CadBomItem(
    val x: Float,
    val y: Float,
    val itemNumber: Int,
    val description: String,
    val qty: Int = 1,
    val material: String = "SS 304",
    override val layerId: String = "TEXT",
    override val id: String = UUID.randomUUID().toString()
) : CadEntity

data class TitleBlockInfo(
    val clientName: String = "ARISTO PHARMA PVT LTD.",
    val clientAddress: String = "DAMAN",
    val consultant: String = "-",
    val drawnBy: String = "RIPEN KOLI",
    val checkedBy: String = "DUD",
    val approvedBy: String = "DUD",
    val scale: String = "N.T.S.",
    val jobNo: String = "JOB-SSEC-4491",
    val companyName: String = "SHREE SAI ENGINEERING COMPANY",
    val companyAddress: String = "A/16, GLOBAL INDUSTRIAL PARK, SURVEY NO. 26/2, NEAR NAHULI RAILWAY CROSSING, OFF N.H. No.8, VALVADA - VAPI, DIST. VALSAD - 396 105",
    val companyEmail: String = "shreesaiengineeringworks@gmail.com",
    val drawingTitle: String = "SS 304 EQUIPMENT FABRICATION BLUEPRINT",
    val materialOfConstruction: String = "SS 304 X 1.2 X 1MM THK MATT FINISH",
    val drawingNo: String = "SSEC/116",
    val drawingDate: String = "22/09/2026",
    val poNo: String = "PO/4491",
    val poDate: String = "15/09/2026",
    val generalNotes: String = "Note:- All Numbers is Laser Printing As Per Your Requirement.\nAll SS welds to be ground smooth, pickled and passivated. Ra < 0.6 µm."
)

enum class IsometricModelType(val displayName: String) {
    MBBR_STP_TANK("MBBR STP Tank & Settling Unit"),
    SS_CYCLONE_HOPPER("SS Cyclone Hopper Vessel"),
    SS_MOBILE_TRAY_RACK("SS Mobile Tray Trolley"),
    SS_DOUBLE_SINK_TABLE("SS Double Sink Table"),
    SS_LOCKER_CABINET("SS Shoes & Apron Locker"),
    CUSTOM_3D_GENERATED("Custom 3D Assembly")
}

data class CadProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val subtitle: String = "Stainless Steel Fabrication Drawing",
    val entities: List<CadEntity> = emptyList(),
    val layers: List<CadLayer> = defaultCadLayers(),
    val titleBlock: TitleBlockInfo = TitleBlockInfo(),
    val sheetWidthMm: Float = 2100f,
    val sheetHeightMm: Float = 1400f,
    val isometricModelType: IsometricModelType = IsometricModelType.MBBR_STP_TANK
)

fun defaultCadLayers(): List<CadLayer> = listOf(
    CadLayer("0", "0 (Outline/Plates)", 0xFFE2E8F0L, 0.6f),
    CadLayer("DIM", "Dimensions (Red)", 0xFFEF4444L, 0.3f),
    CadLayer("CENTER", "Centerlines (Green)", 0xFF10B981L, 0.35f, strokeType = StrokeType.CENTER_LINE),
    CadLayer("NOZZLE", "Nozzles & Fittings (Cyan)", 0xFF38BDF8L, 0.5f),
    CadLayer("TEXT", "Annotations / Text (Yellow)", 0xFFF59E0BL, 0.4f),
    CadLayer("HIDDEN", "Hidden Lines (Gray)", 0xFF64748BL, 0.3f, strokeType = StrokeType.DASHED_HIDDEN)
)
