package com.example.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import java.util.UUID
import kotlin.math.*

object CadGeometryEngine {
    fun snapToGrid(point: Offset, gridSize: Float = 10f): Offset {
        val x = (point.x / gridSize).roundToInt() * gridSize
        val y = (point.y / gridSize).roundToInt() * gridSize
        return Offset(x, y)
    }

    fun applyOrtho(start: Offset, current: Offset): Offset {
        val dx = abs(current.x - start.x)
        val dy = abs(current.y - start.y)
        return if (dx >= dy) {
            Offset(current.x, start.y)
        } else {
            Offset(start.x, current.y)
        }
    }

    fun distancePointToSegment(p: Offset, a: Offset, b: Offset): Float {
        val abx = b.x - a.x
        val aby = b.y - a.y
        val apx = p.x - a.x
        val apy = p.y - a.y
        val lenSq = abx * abx + aby * aby
        if (lenSq == 0f) return sqrt(apx * apx + apy * apy)

        val t = ((apx * abx + apy * aby) / lenSq).coerceIn(0f, 1f)
        val projX = a.x + t * abx
        val projY = a.y + t * aby
        val dx = p.x - projX
        val dy = p.y - projY
        return sqrt(dx * dx + dy * dy)
    }
}

object DxfExportEngine {
    fun generateDxf(project: CadProject): String {
        val sb = StringBuilder()
        sb.append("0\nSECTION\n2\nHEADER\n0\nENDSEC\n")
        sb.append("0\nSECTION\n2\nTABLES\n0\nTABLE\n2\nLAYER\n70\n${project.layers.size}\n")
        for (layer in project.layers) {
            sb.append("0\nLAYER\n2\n${layer.id}\n70\n0\n62\n7\n6\nCONTINUOUS\n")
        }
        sb.append("0\nENDTAB\n0\nENDSEC\n")

        sb.append("0\nSECTION\n2\nBLOCKS\n0\nENDSEC\n")
        sb.append("0\nSECTION\n2\nENTITIES\n")

        for (e in project.entities) {
            when (e) {
                is CadLine -> {
                    sb.append("0\nLINE\n8\n${e.layerId}\n10\n${e.x1}\n20\n${e.y1}\n30\n0.0\n11\n${e.x2}\n21\n${e.y2}\n31\n0.0\n")
                }
                is CadRect -> {
                    sb.append("0\nPOLYLINE\n8\n${e.layerId}\n66\n1\n70\n1\n")
                    val pts = listOf(
                        Offset(e.x, e.y),
                        Offset(e.x + e.width, e.y),
                        Offset(e.x + e.width, e.y + e.height),
                        Offset(e.x, e.y + e.height)
                    )
                    for (p in pts) {
                        sb.append("0\nVERTEX\n8\n${e.layerId}\n10\n${p.x}\n20\n${p.y}\n30\n0.0\n")
                    }
                    sb.append("0\nSEQEND\n")
                }
                is CadCircle -> {
                    sb.append("0\nCIRCLE\n8\n${e.layerId}\n10\n${e.cx}\n20\n${e.cy}\n30\n0.0\n40\n${e.radius}\n")
                }
                is CadArc -> {
                    sb.append("0\nARC\n8\n${e.layerId}\n10\n${e.cx}\n20\n${e.cy}\n30\n0.0\n40\n${e.radius}\n50\n${e.startAngleDeg}\n51\n${e.startAngleDeg + e.sweepAngleDeg}\n")
                }
                is CadText -> {
                    sb.append("0\nTEXT\n8\n${e.layerId}\n10\n${e.x}\n20\n${e.y}\n30\n0.0\n40\n${e.textHeightMm}\n1\n${e.text}\n")
                }
                is CadDimension -> {
                    sb.append("0\nLINE\n8\n${e.layerId}\n10\n${e.x1}\n20\n${e.y1}\n30\n0.0\n11\n${e.x2}\n21\n${e.y2}\n31\n0.0\n")
                }
                is CadLeader -> {
                    sb.append("0\nLINE\n8\n${e.layerId}\n10\n${e.targetX}\n20\n${e.targetY}\n30\n0.0\n11\n${e.elbowX}\n21\n${e.elbowY}\n31\n0.0\n")
                }
                is CadNozzleComponent -> {
                    sb.append("0\nCIRCLE\n8\n${e.layerId}\n10\n${e.x}\n20\n${e.y}\n30\n0.0\n40\n${e.flangeDiaMm * 0.5f}\n")
                }
                is CadHopperCone -> {
                    sb.append("0\nLINE\n8\n${e.layerId}\n10\n${e.topX}\n20\n${e.topY}\n30\n0.0\n11\n${e.topX + (e.topWidth - e.bottomWidth)*0.5f}\n21\n${e.topY + e.height}\n31\n0.0\n")
                }
                is CadDishedHead -> {
                    sb.append("0\nARC\n8\n${e.layerId}\n10\n${e.cx}\n20\n${e.cy}\n30\n0.0\n40\n${e.diameterMm * 0.5f}\n50\n0.0\n51\n180.0\n")
                }
                is CadSaddleSupport -> {
                    sb.append("0\nLINE\n8\n${e.layerId}\n10\n${e.x - e.baseWidthMm * 0.5f}\n20\n${e.y + e.saddleHeightMm}\n30\n0.0\n11\n${e.x + e.baseWidthMm * 0.5f}\n21\n${e.y + e.saddleHeightMm}\n31\n0.0\n")
                }
                is CadLegSupport -> {
                    sb.append("0\nLINE\n8\n${e.layerId}\n10\n${e.x}\n20\n${e.y}\n30\n0.0\n11\n${e.x}\n21\n${e.y + e.heightMm}\n31\n0.0\n")
                }
                is CadManway -> {
                    sb.append("0\nCIRCLE\n8\n${e.layerId}\n10\n${e.x}\n20\n${e.y}\n30\n0.0\n40\n${e.manwayNb * 0.5f}\n")
                }
                is CadValve -> {
                    sb.append("0\nCIRCLE\n8\n${e.layerId}\n10\n${e.x}\n20\n${e.y}\n30\n0.0\n40\n${e.lengthMm * 0.4f}\n")
                }
                is CadAgitator -> {
                    sb.append("0\nLINE\n8\n${e.layerId}\n10\n${e.topX}\n20\n${e.topY}\n30\n0.0\n11\n${e.topX}\n21\n${e.topY + e.shaftLengthMm}\n31\n0.0\n")
                }
                is CadBomItem -> {
                    sb.append("0\nTEXT\n8\n${e.layerId}\n10\n${e.x}\n20\n${e.y}\n30\n0.0\n40\n14.0\n1\n${e.itemNumber}\n")
                }
            }
        }

        sb.append("0\nENDSEC\n0\nEOF\n")
        return sb.toString()
    }
}

object CadTransformEngine {

    fun rotatePoint(point: Offset, pivot: Offset, angleDeg: Float): Offset {
        val rad = Math.toRadians(angleDeg.toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()
        val dx = point.x - pivot.x
        val dy = point.y - pivot.y
        return Offset(
            x = pivot.x + (dx * cosA - dy * sinA),
            y = pivot.y + (dx * sinA + dy * cosA)
        )
    }

    fun getEntityCenter(entity: CadEntity): Offset {
        return when (entity) {
            is CadLine -> Offset((entity.x1 + entity.x2) * 0.5f, (entity.y1 + entity.y2) * 0.5f)
            is CadRect -> Offset(entity.x + entity.width * 0.5f, entity.y + entity.height * 0.5f)
            is CadCircle -> Offset(entity.cx, entity.cy)
            is CadArc -> Offset(entity.cx, entity.cy)
            is CadDimension -> Offset((entity.x1 + entity.x2) * 0.5f, (entity.y1 + entity.y2) * 0.5f)
            is CadLeader -> Offset((entity.targetX + entity.elbowX) * 0.5f, (entity.targetY + entity.elbowY) * 0.5f)
            is CadText -> Offset(entity.x + 30f, entity.y - entity.textHeightMm * 0.5f)
            is CadNozzleComponent -> Offset(entity.x, entity.y)
            is CadHopperCone -> Offset(entity.topX + entity.topWidth * 0.5f, entity.topY + entity.height * 0.5f)
            is CadDishedHead -> Offset(entity.cx, entity.cy)
            is CadSaddleSupport -> Offset(entity.x, entity.y + entity.saddleHeightMm * 0.5f)
            is CadLegSupport -> Offset(entity.x, entity.y + entity.heightMm * 0.5f)
            is CadManway -> Offset(entity.x, entity.y)
            is CadValve -> Offset(entity.x, entity.y)
            is CadAgitator -> Offset(entity.topX, entity.topY + entity.shaftLengthMm * 0.5f)
            is CadBomItem -> Offset(entity.x, entity.y)
        }
    }

    fun rotateEntity(entity: CadEntity, angleDeg: Float, customPivot: Offset? = null): CadEntity {
        val pivot = customPivot ?: getEntityCenter(entity)

        return when (entity) {
            is CadLine -> {
                val p1 = rotatePoint(Offset(entity.x1, entity.y1), pivot, angleDeg)
                val p2 = rotatePoint(Offset(entity.x2, entity.y2), pivot, angleDeg)
                entity.copy(x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y)
            }
            is CadRect -> {
                val center = Offset(entity.x + entity.width * 0.5f, entity.y + entity.height * 0.5f)
                val newCenter = rotatePoint(center, pivot, angleDeg)
                val normAngle = ((angleDeg % 360f) + 360f) % 360f
                val swapDims = (normAngle in 45f..135f) || (normAngle in 225f..315f)
                val newW = if (swapDims) entity.height else entity.width
                val newH = if (swapDims) entity.width else entity.height
                entity.copy(
                    x = newCenter.x - newW * 0.5f,
                    y = newCenter.y - newH * 0.5f,
                    width = newW,
                    height = newH
                )
            }
            is CadCircle -> {
                val newCenter = rotatePoint(Offset(entity.cx, entity.cy), pivot, angleDeg)
                entity.copy(cx = newCenter.x, cy = newCenter.y)
            }
            is CadArc -> {
                val newCenter = rotatePoint(Offset(entity.cx, entity.cy), pivot, angleDeg)
                val newStart = (entity.startAngleDeg + angleDeg) % 360f
                entity.copy(cx = newCenter.x, cy = newCenter.y, startAngleDeg = newStart)
            }
            is CadDimension -> {
                val p1 = rotatePoint(Offset(entity.x1, entity.y1), pivot, angleDeg)
                val p2 = rotatePoint(Offset(entity.x2, entity.y2), pivot, angleDeg)
                val normAngle = ((angleDeg % 360f) + 360f) % 360f
                val newType = if (normAngle in 45f..135f || normAngle in 225f..315f) {
                    when (entity.type) {
                        DimensionType.LINEAR_HORIZONTAL -> DimensionType.LINEAR_VERTICAL
                        DimensionType.LINEAR_VERTICAL -> DimensionType.LINEAR_HORIZONTAL
                        else -> entity.type
                    }
                } else {
                    entity.type
                }
                entity.copy(x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y, type = newType)
            }
            is CadLeader -> {
                val t = rotatePoint(Offset(entity.targetX, entity.targetY), pivot, angleDeg)
                val e = rotatePoint(Offset(entity.elbowX, entity.elbowY), pivot, angleDeg)
                entity.copy(targetX = t.x, targetY = t.y, elbowX = e.x, elbowY = e.y)
            }
            is CadText -> {
                val p = rotatePoint(Offset(entity.x, entity.y), pivot, angleDeg)
                entity.copy(x = p.x, y = p.y)
            }
            is CadNozzleComponent -> {
                val c = rotatePoint(Offset(entity.x, entity.y), pivot, angleDeg)
                val newAngle = (entity.angleDeg + angleDeg) % 360f
                entity.copy(x = c.x, y = c.y, angleDeg = newAngle)
            }
            is CadHopperCone -> {
                val c = rotatePoint(Offset(entity.topX + entity.topWidth * 0.5f, entity.topY + entity.height * 0.5f), pivot, angleDeg)
                entity.copy(topX = c.x - entity.topWidth * 0.5f, topY = c.y - entity.height * 0.5f)
            }
            is CadDishedHead -> {
                val c = rotatePoint(Offset(entity.cx, entity.cy), pivot, angleDeg)
                entity.copy(cx = c.x, cy = c.y)
            }
            is CadSaddleSupport -> {
                val c = rotatePoint(Offset(entity.x, entity.y), pivot, angleDeg)
                entity.copy(x = c.x, y = c.y)
            }
            is CadLegSupport -> {
                val c = rotatePoint(Offset(entity.x, entity.y), pivot, angleDeg)
                entity.copy(x = c.x, y = c.y)
            }
            is CadManway -> {
                val c = rotatePoint(Offset(entity.x, entity.y), pivot, angleDeg)
                entity.copy(x = c.x, y = c.y)
            }
            is CadValve -> {
                val c = rotatePoint(Offset(entity.x, entity.y), pivot, angleDeg)
                entity.copy(x = c.x, y = c.y, isVertical = !entity.isVertical)
            }
            is CadAgitator -> {
                val c = rotatePoint(Offset(entity.topX, entity.topY), pivot, angleDeg)
                entity.copy(topX = c.x, topY = c.y)
            }
            is CadBomItem -> {
                val c = rotatePoint(Offset(entity.x, entity.y), pivot, angleDeg)
                entity.copy(x = c.x, y = c.y)
            }
        }
    }

    fun translateEntity(entity: CadEntity, dx: Float, dy: Float): CadEntity {
        return when (entity) {
            is CadLine -> entity.copy(x1 = entity.x1 + dx, y1 = entity.y1 + dy, x2 = entity.x2 + dx, y2 = entity.y2 + dy)
            is CadRect -> entity.copy(x = entity.x + dx, y = entity.y + dy)
            is CadCircle -> entity.copy(cx = entity.cx + dx, cy = entity.cy + dy)
            is CadArc -> entity.copy(cx = entity.cx + dx, cy = entity.cy + dy)
            is CadDimension -> entity.copy(x1 = entity.x1 + dx, y1 = entity.y1 + dy, x2 = entity.x2 + dx, y2 = entity.y2 + dy)
            is CadLeader -> entity.copy(targetX = entity.targetX + dx, targetY = entity.targetY + dy, elbowX = entity.elbowX + dx, elbowY = entity.elbowY + dy)
            is CadText -> entity.copy(x = entity.x + dx, y = entity.y + dy)
            is CadNozzleComponent -> entity.copy(x = entity.x + dx, y = entity.y + dy)
            is CadHopperCone -> entity.copy(topX = entity.topX + dx, topY = entity.topY + dy)
            is CadDishedHead -> entity.copy(cx = entity.cx + dx, cy = entity.cy + dy)
            is CadSaddleSupport -> entity.copy(x = entity.x + dx, y = entity.y + dy)
            is CadLegSupport -> entity.copy(x = entity.x + dx, y = entity.y + dy)
            is CadManway -> entity.copy(x = entity.x + dx, y = entity.y + dy)
            is CadValve -> entity.copy(x = entity.x + dx, y = entity.y + dy)
            is CadAgitator -> entity.copy(topX = entity.topX + dx, topY = entity.topY + dy)
            is CadBomItem -> entity.copy(x = entity.x + dx, y = entity.y + dy)
        }
    }

    fun duplicateEntity(entity: CadEntity, offset: Offset = Offset(30f, 30f)): CadEntity {
        val moved = translateEntity(entity, offset.x, offset.y)
        val newId = UUID.randomUUID().toString()
        return when (moved) {
            is CadLine -> moved.copy(id = newId)
            is CadRect -> moved.copy(id = newId)
            is CadCircle -> moved.copy(id = newId)
            is CadArc -> moved.copy(id = newId)
            is CadDimension -> moved.copy(id = newId)
            is CadLeader -> moved.copy(id = newId)
            is CadText -> moved.copy(id = newId)
            is CadNozzleComponent -> moved.copy(id = newId)
            is CadHopperCone -> moved.copy(id = newId)
            is CadDishedHead -> moved.copy(id = newId)
            is CadSaddleSupport -> moved.copy(id = newId)
            is CadLegSupport -> moved.copy(id = newId)
            is CadManway -> moved.copy(id = newId)
            is CadValve -> moved.copy(id = newId)
            is CadAgitator -> moved.copy(id = newId)
            is CadBomItem -> moved.copy(id = newId)
        }
    }

    fun offsetEntity(entity: CadEntity, offsetDistance: Float): CadEntity? {
        val newId = UUID.randomUUID().toString()
        return when (entity) {
            is CadLine -> {
                val dx = entity.x2 - entity.x1
                val dy = entity.y2 - entity.y1
                val len = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
                val nx = -dy / len * offsetDistance
                val ny = dx / len * offsetDistance
                entity.copy(
                    id = newId,
                    x1 = entity.x1 + nx,
                    y1 = entity.y1 + ny,
                    x2 = entity.x2 + nx,
                    y2 = entity.y2 + ny
                )
            }
            is CadRect -> {
                val nw = (entity.width + offsetDistance * 2f).coerceAtLeast(10f)
                val nh = (entity.height + offsetDistance * 2f).coerceAtLeast(10f)
                entity.copy(
                    id = newId,
                    x = entity.x - offsetDistance,
                    y = entity.y - offsetDistance,
                    width = nw,
                    height = nh
                )
            }
            is CadCircle -> {
                val nr = (entity.radius + offsetDistance).coerceAtLeast(5f)
                entity.copy(id = newId, radius = nr)
            }
            else -> null
        }
    }

    fun calculateProjectBounds(entities: List<CadEntity>): Rect {
        if (entities.isEmpty()) {
            return Rect(0f, 0f, 1000f, 800f)
        }
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE

        for (e in entities) {
            when (e) {
                is CadLine -> {
                    minX = minOf(minX, e.x1, e.x2)
                    minY = minOf(minY, e.y1, e.y2)
                    maxX = maxOf(maxX, e.x1, e.x2)
                    maxY = maxOf(maxY, e.y1, e.y2)
                }
                is CadRect -> {
                    minX = minOf(minX, e.x)
                    minY = minOf(minY, e.y)
                    maxX = maxOf(maxX, e.x + e.width)
                    maxY = maxOf(maxY, e.y + e.height)
                }
                is CadCircle -> {
                    minX = minOf(minX, e.cx - e.radius)
                    minY = minOf(minY, e.cy - e.radius)
                    maxX = maxOf(maxX, e.cx + e.radius)
                    maxY = maxOf(maxY, e.cy + e.radius)
                }
                is CadArc -> {
                    minX = minOf(minX, e.cx - e.radius)
                    minY = minOf(minY, e.cy - e.radius)
                    maxX = maxOf(maxX, e.cx + e.radius)
                    maxY = maxOf(maxY, e.cy + e.radius)
                }
                is CadDimension -> {
                    minX = minOf(minX, e.x1, e.x2)
                    minY = minOf(minY, e.y1, e.y2)
                    maxX = maxOf(maxX, e.x1, e.x2)
                    maxY = maxOf(maxY, e.y1, e.y2)
                }
                is CadLeader -> {
                    minX = minOf(minX, e.targetX, e.elbowX)
                    minY = minOf(minY, e.targetY, e.elbowY)
                    maxX = maxOf(maxX, e.targetX, e.elbowX + 150f)
                    maxY = maxOf(maxY, e.targetY, e.elbowY)
                }
                is CadText -> {
                    minX = minOf(minX, e.x)
                    minY = minOf(minY, e.y - e.textHeightMm)
                    maxX = maxOf(maxX, e.x + 200f)
                    maxY = maxOf(maxY, e.y + e.textHeightMm)
                }
                is CadNozzleComponent -> {
                    minX = minOf(minX, e.x - e.flangeDiaMm * 0.5f)
                    minY = minOf(minY, e.y - e.flangeDiaMm * 0.5f)
                    maxX = maxOf(maxX, e.x + e.flangeDiaMm * 0.5f)
                    maxY = maxOf(maxY, e.y + e.flangeDiaMm * 0.5f)
                }
                is CadHopperCone -> {
                    minX = minOf(minX, e.topX)
                    minY = minOf(minY, e.topY)
                    maxX = maxOf(maxX, e.topX + e.topWidth)
                    maxY = maxOf(maxY, e.topY + e.height)
                }
                is CadDishedHead -> {
                    minX = minOf(minX, e.cx - e.diameterMm * 0.5f)
                    minY = minOf(minY, e.cy - e.diameterMm * 0.3f)
                    maxX = maxOf(maxX, e.cx + e.diameterMm * 0.5f)
                    maxY = maxOf(maxY, e.cy + e.diameterMm * 0.3f)
                }
                is CadSaddleSupport -> {
                    minX = minOf(minX, e.x - e.baseWidthMm * 0.5f)
                    minY = minOf(minY, e.y)
                    maxX = maxOf(maxX, e.x + e.baseWidthMm * 0.5f)
                    maxY = maxOf(maxY, e.y + e.saddleHeightMm)
                }
                is CadLegSupport -> {
                    minX = minOf(minX, e.x - e.basePadDiaMm * 0.5f)
                    minY = minOf(minY, e.y)
                    maxX = maxOf(maxX, e.x + e.basePadDiaMm * 0.5f)
                    maxY = maxOf(maxY, e.y + e.heightMm)
                }
                is CadManway -> {
                    minX = minOf(minX, e.x - 100f)
                    minY = minOf(minY, e.y - 100f)
                    maxX = maxOf(maxX, e.x + 100f)
                    maxY = maxOf(maxY, e.y + 100f)
                }
                is CadValve -> {
                    minX = minOf(minX, e.x - e.lengthMm * 0.5f)
                    minY = minOf(minY, e.y - e.lengthMm * 0.5f)
                    maxX = maxOf(maxX, e.x + e.lengthMm * 0.5f)
                    maxY = maxOf(maxY, e.y + e.lengthMm * 0.5f)
                }
                is CadAgitator -> {
                    minX = minOf(minX, e.topX - e.impellerDiaMm * 0.5f)
                    minY = minOf(minY, e.topY)
                    maxX = maxOf(maxX, e.topX + e.impellerDiaMm * 0.5f)
                    maxY = maxOf(maxY, e.topY + e.shaftLengthMm + 50f)
                }
                is CadBomItem -> {
                    minX = minOf(minX, e.x - 20f)
                    minY = minOf(minY, e.y - 20f)
                    maxX = maxOf(maxX, e.x + 20f)
                    maxY = maxOf(maxY, e.y + 20f)
                }
            }
        }
        return Rect(minX, minY, maxX, maxY)
    }

    fun calculateFitTransform(
        entities: List<CadEntity>,
        viewportWidth: Float,
        viewportHeight: Float,
        paddingPx: Float = 80f
    ): Pair<Float, Offset> {
        val bounds = calculateProjectBounds(entities)
        val drawingWidth = (bounds.width).coerceAtLeast(100f)
        val drawingHeight = (bounds.height).coerceAtLeast(100f)

        val availW = (viewportWidth - paddingPx * 2).coerceAtLeast(100f)
        val availH = (viewportHeight - paddingPx * 2).coerceAtLeast(100f)

        val scaleX = availW / drawingWidth
        val scaleY = availH / drawingHeight
        val fitScale = minOf(scaleX, scaleY).coerceIn(0.15f, 4.0f)

        val drawingCenter = bounds.center
        val panX = (viewportWidth * 0.5f) - (drawingCenter.x * fitScale)
        val panY = (viewportHeight * 0.5f) - (drawingCenter.y * fitScale)

        return Pair(fitScale, Offset(panX, panY))
    }
}
