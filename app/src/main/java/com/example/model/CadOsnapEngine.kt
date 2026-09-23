package com.example.model

import androidx.compose.ui.geometry.Offset
import kotlin.math.*

object CadOsnapEngine {

    fun findBestSnapPoint(
        cursorWorld: Offset,
        entities: List<CadEntity>,
        snapThresholdMm: Float = 25f,
        enabledSnapTypes: Set<OsnapType> = setOf(
            OsnapType.ENDPOINT,
            OsnapType.MIDPOINT,
            OsnapType.CENTER,
            OsnapType.QUADRANT,
            OsnapType.INTERSECTION,
            OsnapType.GRID
        ),
        isGridSnapEnabled: Boolean = false,
        gridSizeMm: Float = 10f
    ): OsnapResult? {
        var closestResult: OsnapResult? = null
        var minDistance = snapThresholdMm

        fun checkCandidate(point: Offset, type: OsnapType, desc: String) {
            if (!enabledSnapTypes.contains(type)) return
            val dx = cursorWorld.x - point.x
            val dy = cursorWorld.y - point.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < minDistance) {
                minDistance = dist
                closestResult = OsnapResult(point, type, desc)
            }
        }

        // 1. Inspect all entity features
        for (e in entities) {
            when (e) {
                is CadLine -> {
                    checkCandidate(Offset(e.x1, e.y1), OsnapType.ENDPOINT, "Line Start")
                    checkCandidate(Offset(e.x2, e.y2), OsnapType.ENDPOINT, "Line End")
                    checkCandidate(Offset((e.x1 + e.x2) * 0.5f, (e.y1 + e.y2) * 0.5f), OsnapType.MIDPOINT, "Line Midpoint")
                }
                is CadRect -> {
                    val p1 = Offset(e.x, e.y)
                    val p2 = Offset(e.x + e.width, e.y)
                    val p3 = Offset(e.x + e.width, e.y + e.height)
                    val p4 = Offset(e.x, e.y + e.height)

                    checkCandidate(p1, OsnapType.ENDPOINT, "Corner Top-Left")
                    checkCandidate(p2, OsnapType.ENDPOINT, "Corner Top-Right")
                    checkCandidate(p3, OsnapType.ENDPOINT, "Corner Bottom-Right")
                    checkCandidate(p4, OsnapType.ENDPOINT, "Corner Bottom-Left")

                    checkCandidate(Offset((p1.x + p2.x) * 0.5f, p1.y), OsnapType.MIDPOINT, "Top Edge Mid")
                    checkCandidate(Offset(p2.x, (p2.y + p3.y) * 0.5f), OsnapType.MIDPOINT, "Right Edge Mid")
                    checkCandidate(Offset((p3.x + p4.x) * 0.5f, p3.y), OsnapType.MIDPOINT, "Bottom Edge Mid")
                    checkCandidate(Offset(p1.x, (p1.y + p4.y) * 0.5f), OsnapType.MIDPOINT, "Left Edge Mid")
                    checkCandidate(Offset(e.x + e.width * 0.5f, e.y + e.height * 0.5f), OsnapType.CENTER, "Plate Center")
                }
                is CadCircle -> {
                    val c = Offset(e.cx, e.cy)
                    checkCandidate(c, OsnapType.CENTER, if (e.isPcd) "PCD Center" else "Circle Center")
                    checkCandidate(Offset(e.cx + e.radius, e.cy), OsnapType.QUADRANT, "0° Quadrant")
                    checkCandidate(Offset(e.cx, e.cy - e.radius), OsnapType.QUADRANT, "90° Quadrant")
                    checkCandidate(Offset(e.cx - e.radius, e.cy), OsnapType.QUADRANT, "180° Quadrant")
                    checkCandidate(Offset(e.cx, e.cy + e.radius), OsnapType.QUADRANT, "270° Quadrant")
                }
                is CadArc -> {
                    val c = Offset(e.cx, e.cy)
                    checkCandidate(c, OsnapType.CENTER, "Arc Center")
                    val radStart = Math.toRadians(e.startAngleDeg.toDouble())
                    val radEnd = Math.toRadians((e.startAngleDeg + e.sweepAngleDeg).toDouble())
                    val radMid = Math.toRadians((e.startAngleDeg + e.sweepAngleDeg * 0.5).toDouble())

                    val sp = Offset((e.cx + e.radius * cos(radStart)).toFloat(), (e.cy + e.radius * sin(radStart)).toFloat())
                    val ep = Offset((e.cx + e.radius * cos(radEnd)).toFloat(), (e.cy + e.radius * sin(radEnd)).toFloat())
                    val mp = Offset((e.cx + e.radius * cos(radMid)).toFloat(), (e.cy + e.radius * sin(radMid)).toFloat())

                    checkCandidate(sp, OsnapType.ENDPOINT, "Arc Start")
                    checkCandidate(ep, OsnapType.ENDPOINT, "Arc End")
                    checkCandidate(mp, OsnapType.MIDPOINT, "Arc Midpoint")
                }
                is CadHopperCone -> {
                    val tl = Offset(e.topX, e.topY)
                    val tr = Offset(e.topX + e.topWidth, e.topY)
                    val bl = Offset(e.topX + (e.topWidth - e.bottomWidth) * 0.5f, e.topY + e.height)
                    val br = Offset(e.topX + (e.topWidth + e.bottomWidth) * 0.5f, e.topY + e.height)

                    checkCandidate(tl, OsnapType.ENDPOINT, "Hopper Top-Left")
                    checkCandidate(tr, OsnapType.ENDPOINT, "Hopper Top-Right")
                    checkCandidate(bl, OsnapType.ENDPOINT, "Hopper Bottom-Left")
                    checkCandidate(br, OsnapType.ENDPOINT, "Hopper Bottom-Right")
                    checkCandidate(Offset(e.topX + e.topWidth * 0.5f, e.topY), OsnapType.MIDPOINT, "Top Inflow Center")
                    checkCandidate(Offset(e.topX + e.topWidth * 0.5f, e.topY + e.height), OsnapType.MIDPOINT, "Drain Center")
                }
                is CadNozzleComponent -> {
                    checkCandidate(Offset(e.x, e.y), OsnapType.CENTER, "Nozzle ${e.sizeNb}NB Base")
                }
                is CadDishedHead -> {
                    checkCandidate(Offset(e.cx, e.cy), OsnapType.CENTER, "Dish Center")
                    checkCandidate(Offset(e.cx - e.diameterMm * 0.5f, e.cy), OsnapType.ENDPOINT, "Dish Flange Left")
                    checkCandidate(Offset(e.cx + e.diameterMm * 0.5f, e.cy), OsnapType.ENDPOINT, "Dish Flange Right")
                }
                is CadSaddleSupport -> {
                    checkCandidate(Offset(e.x, e.y), OsnapType.CENTER, "Saddle Axis")
                    checkCandidate(Offset(e.x - e.baseWidthMm * 0.5f, e.y + e.saddleHeightMm), OsnapType.ENDPOINT, "Saddle Base Left")
                    checkCandidate(Offset(e.x + e.baseWidthMm * 0.5f, e.y + e.saddleHeightMm), OsnapType.ENDPOINT, "Saddle Base Right")
                }
                is CadLegSupport -> {
                    checkCandidate(Offset(e.x, e.y), OsnapType.ENDPOINT, "Leg Top")
                    checkCandidate(Offset(e.x, e.y + e.heightMm), OsnapType.ENDPOINT, "Foot Base")
                }
                is CadManway -> {
                    checkCandidate(Offset(e.x, e.y), OsnapType.CENTER, "Manway ${e.manwayNb}NB Axis")
                }
                is CadValve -> {
                    checkCandidate(Offset(e.x, e.y), OsnapType.CENTER, "Valve Axis")
                    if (e.isVertical) {
                        checkCandidate(Offset(e.x, e.y - e.lengthMm * 0.5f), OsnapType.ENDPOINT, "Valve Port 1")
                        checkCandidate(Offset(e.x, e.y + e.lengthMm * 0.5f), OsnapType.ENDPOINT, "Valve Port 2")
                    } else {
                        checkCandidate(Offset(e.x - e.lengthMm * 0.5f, e.y), OsnapType.ENDPOINT, "Valve Port 1")
                        checkCandidate(Offset(e.x + e.lengthMm * 0.5f, e.y), OsnapType.ENDPOINT, "Valve Port 2")
                    }
                }
                is CadDimension -> {
                    checkCandidate(Offset(e.x1, e.y1), OsnapType.ENDPOINT, "Dim Origin 1")
                    checkCandidate(Offset(e.x2, e.y2), OsnapType.ENDPOINT, "Dim Origin 2")
                }
                is CadLeader -> {
                    checkCandidate(Offset(e.targetX, e.targetY), OsnapType.ENDPOINT, "Leader Arrow Tip")
                }
                is CadBomItem -> {
                    checkCandidate(Offset(e.x, e.y), OsnapType.CENTER, "BOM #${e.itemNumber}")
                }
                else -> {}
            }
        }

        // 2. Line Intersections
        if (enabledSnapTypes.contains(OsnapType.INTERSECTION)) {
            val lines = entities.filterIsInstance<CadLine>()
            for (i in 0 until lines.size) {
                for (j in (i + 1) until lines.size) {
                    val l1 = lines[i]
                    val l2 = lines[j]
                    val inter = computeLineIntersection(
                        Offset(l1.x1, l1.y1), Offset(l1.x2, l1.y2),
                        Offset(l2.x1, l2.y1), Offset(l2.x2, l2.y2)
                    )
                    if (inter != null) {
                        checkCandidate(inter, OsnapType.INTERSECTION, "Intersection")
                    }
                }
            }
        }

        // 3. Grid Snap (fallback or if closest)
        if (isGridSnapEnabled && enabledSnapTypes.contains(OsnapType.GRID)) {
            val gx = (cursorWorld.x / gridSizeMm).roundToInt() * gridSizeMm
            val gy = (cursorWorld.y / gridSizeMm).roundToInt() * gridSizeMm
            val gridPoint = Offset(gx, gy)
            checkCandidate(gridPoint, OsnapType.GRID, "Grid Snap (${gx.toInt()}, ${gy.toInt()})")
        }

        return closestResult
    }

    private fun computeLineIntersection(p1: Offset, p2: Offset, p3: Offset, p4: Offset): Offset? {
        val d = (p1.x - p2.x) * (p3.y - p4.y) - (p1.y - p2.y) * (p3.x - p4.x)
        if (abs(d) < 0.0001f) return null

        val t = ((p1.x - p3.x) * (p3.y - p4.y) - (p1.y - p3.y) * (p3.x - p4.x)) / d
        val u = -((p1.x - p2.x) * (p1.y - p3.y) - (p1.y - p2.y) * (p1.x - p3.x)) / d

        return if (t in 0.0..1.0 && u in 0.0..1.0) {
            Offset(
                p1.x + t * (p2.x - p1.x),
                p1.y + t * (p2.y - p1.y)
            )
        } else null
    }
}
