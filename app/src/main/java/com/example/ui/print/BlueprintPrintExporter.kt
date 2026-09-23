package com.example.ui.print

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.model.*
import java.io.File
import java.io.FileOutputStream

object BlueprintPrintExporter {

    fun exportBlueprintToBitmap(
        project: CadProject,
        bitmapWidth: Int = 2400,
        bitmapHeight: Int = 1600
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(0xFFFFFFFF.toInt())

        val borderPaint = Paint().apply {
            color = 0xFF0F172A.toInt()
            strokeWidth = 4f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = 0xFF0F172A.toInt()
            textSize = 24f
            isAntiAlias = true
            isFakeBoldText = true
        }

        val margin = 50f
        val filingMarginLeft = 80f
        val left = filingMarginLeft
        val top = margin
        val right = bitmapWidth - margin
        val bottom = bitmapHeight - margin

        canvas.drawRect(left, top, right, bottom, borderPaint)
        borderPaint.strokeWidth = 2f
        canvas.drawRect(left + 25f, top + 25f, right - 25f, bottom - 25f, borderPaint)

        val numCols = 8
        val colW = (right - left - 50f) / numCols
        for (i in 1 until numCols) {
            val x = left + 25f + i * colW
            canvas.drawLine(x, top, x, top + 25f, borderPaint)
            canvas.drawLine(x, bottom - 25f, x, bottom, borderPaint)
            canvas.drawText("$i", x - 8f, top + 20f, textPaint)
        }

        val numRows = 5
        val letters = listOf("A", "B", "C", "D", "E")
        val rowH = (bottom - top - 50f) / numRows
        for (i in 1 until numRows) {
            val y = top + 25f + i * rowH
            canvas.drawLine(left, y, left + 25f, y, borderPaint)
            canvas.drawLine(right - 25f, y, right, y, borderPaint)
            canvas.drawText(letters.getOrElse(i - 1) { "" }, left + 6f, y - 8f, textPaint)
        }

        val entityPaint = Paint().apply {
            color = 0xFF0F172A.toInt()
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val dimPaint = Paint().apply {
            color = 0xFFDC2626.toInt()
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val dimTextPaint = Paint().apply {
            color = 0xFFDC2626.toInt()
            textSize = 20f
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val noteTextPaint = Paint().apply {
            color = 0xFF0F172A.toInt()
            textSize = 26f
            isAntiAlias = true
            isFakeBoldText = true
        }

        val scale = 0.9f
        val offsetX = 100f
        val offsetY = 100f

        for (e in project.entities) {
            when (e) {
                is CadLine -> {
                    val p = if (e.layerId == "CENTER") {
                        Paint(entityPaint).apply {
                            color = 0xFF059669.toInt()
                            strokeWidth = 2f
                        }
                    } else if (e.layerId == "NOZZLE") {
                        Paint(entityPaint).apply { color = 0xFF0284C7.toInt() }
                    } else entityPaint
                    canvas.drawLine(offsetX + e.x1 * scale, offsetY + e.y1 * scale, offsetX + e.x2 * scale, offsetY + e.y2 * scale, p)
                }
                is CadRect -> {
                    canvas.drawRect(offsetX + e.x * scale, offsetY + e.y * scale, offsetX + (e.x + e.width) * scale, offsetY + (e.y + e.height) * scale, entityPaint)
                }
                is CadCircle -> {
                    val p = if (e.layerId == "NOZZLE") Paint(entityPaint).apply { color = 0xFF0284C7.toInt() } else entityPaint
                    canvas.drawCircle(offsetX + e.cx * scale, offsetY + e.cy * scale, e.radius * scale, p)
                }
                is CadDimension -> {
                    val sx1 = offsetX + e.x1 * scale
                    val sy1 = offsetY + e.y1 * scale
                    val sx2 = offsetX + e.x2 * scale
                    val sy2 = offsetY + e.y2 * scale
                    val offPx = e.offsetDistance * scale

                    if (e.type == DimensionType.LINEAR_VERTICAL) {
                        val refX = if (e.offsetDistance < 0) minOf(sx1, sx2) else maxOf(sx1, sx2)
                        val sDimX = refX + offPx
                        val sMinY = minOf(sy1, sy2)
                        val sMaxY = maxOf(sy1, sy2)
                        val extOvershoot = if (e.offsetDistance < 0) -15f else 15f

                        canvas.drawLine(sx1, sy1, sDimX + extOvershoot, sy1, dimPaint)
                        canvas.drawLine(sx2, sy2, sDimX + extOvershoot, sy2, dimPaint)
                        canvas.drawLine(sDimX, sMinY, sDimX, sMaxY, dimPaint)

                        drawPrintArrow(canvas, sDimX, sMinY, sDimX, sMinY + 14f, dimPaint)
                        drawPrintArrow(canvas, sDimX, sMaxY, sDimX, sMaxY - 14f, dimPaint)

                        val txt = e.textOverride ?: "${e.prefix}%.0f${e.suffix}".format(kotlin.math.abs(e.y2 - e.y1))
                        canvas.save()
                        canvas.translate(sDimX - 8f, (sMinY + sMaxY) * 0.5f)
                        canvas.rotate(-90f)
                        canvas.drawText(txt, 0f, 0f, dimTextPaint)
                        canvas.restore()
                    } else {
                        val refY = if (e.offsetDistance < 0) minOf(sy1, sy2) else maxOf(sy1, sy2)
                        val sDimY = refY + offPx
                        val sMinX = minOf(sx1, sx2)
                        val sMaxX = maxOf(sx1, sx2)
                        val extOvershoot = if (e.offsetDistance < 0) -15f else 15f

                        canvas.drawLine(sx1, sy1, sx1, sDimY + extOvershoot, dimPaint)
                        canvas.drawLine(sx2, sy2, sx2, sDimY + extOvershoot, dimPaint)
                        canvas.drawLine(sMinX, sDimY, sMaxX, sDimY, dimPaint)

                        drawPrintArrow(canvas, sMinX, sDimY, sMinX + 14f, sDimY, dimPaint)
                        drawPrintArrow(canvas, sMaxX, sDimY, sMaxX - 14f, sDimY, dimPaint)

                        val txt = e.textOverride ?: "${e.prefix}%.0f${e.suffix}".format(kotlin.math.abs(e.x2 - e.x1))
                        canvas.drawText(txt, (sMinX + sMaxX) * 0.5f, sDimY - 8f, dimTextPaint)
                    }
                }
                is CadLeader -> {
                    val tx = offsetX + e.targetX * scale
                    val ty = offsetY + e.targetY * scale
                    val ex = offsetX + e.elbowX * scale
                    val ey = offsetY + e.elbowY * scale
                    canvas.drawLine(tx, ty, ex, ey, entityPaint)
                    canvas.drawLine(ex, ey, ex + 60f, ey, entityPaint)
                    canvas.drawText(e.text, ex + 65f, ey + 6f, noteTextPaint.apply { textSize = 18f })
                }
                is CadText -> {
                    canvas.drawText(e.text, offsetX + e.x * scale, offsetY + e.y * scale, noteTextPaint.apply { textSize = e.textHeightMm * 1.2f })
                }
                else -> {}
            }
        }

        // Title Block in Bottom Right Corner
        val tbW = 920f
        val tbH = 340f
        val tbRight = right - 25f
        val tbBottom = bottom - 25f
        val tbLeft = tbRight - tbW
        val tbTop = tbBottom - tbH

        drawTitleBlockOnCanvas(canvas, project.titleBlock, tbLeft, tbTop, tbRight, tbBottom)

        return bitmap
    }

    private fun drawTitleBlockOnCanvas(
        canvas: Canvas,
        info: TitleBlockInfo,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        val strokePaint = Paint().apply {
            color = 0xFF0F172A.toInt()
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val fillPaint = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.FILL
        }

        val headerFill = Paint().apply {
            color = 0xFF0F172A.toInt()
            style = Paint.Style.FILL
        }

        val textBold = Paint().apply {
            color = 0xFF0F172A.toInt()
            textSize = 20f
            isAntiAlias = true
            isFakeBoldText = true
        }

        val textSmall = Paint().apply {
            color = 0xFF334155.toInt()
            textSize = 14f
            isAntiAlias = true
        }

        val textRed = Paint().apply {
            color = 0xFFDC2626.toInt()
            textSize = 18f
            isAntiAlias = true
            isFakeBoldText = true
        }

        canvas.drawRect(left, top, right, bottom, fillPaint)
        canvas.drawRect(left, top, right, bottom, strokePaint)

        // Row 1: Client
        val r1Bottom = top + 55f
        canvas.drawLine(left, r1Bottom, right, r1Bottom, strokePaint)
        canvas.drawText("CLIENT :— ${info.clientName}", left + 15f, top + 28f, textBold)
        canvas.drawText("ADDRESS :— ${info.clientAddress}   |   CONSULTANT :— ${info.consultant}", left + 15f, top + 48f, textSmall)

        // Row 2: Signoff table
        val r2Bottom = r1Bottom + 45f
        canvas.drawLine(left, r2Bottom, right, r2Bottom, strokePaint)
        val colW = (right - left) / 6f
        val labels = listOf("DRAWN", "CHECKED", "APPROVED", "SCALE", "DATED", "JOB No.")
        val vals = listOf(info.drawnBy, info.checkedBy, info.approvedBy, info.scale, info.drawingDate, info.jobNo)
        for (i in 0 until 6) {
            val cx = left + i * colW
            if (i > 0) canvas.drawLine(cx, r1Bottom, cx, r2Bottom, strokePaint)
            canvas.drawText(labels[i], cx + 8f, r1Bottom + 18f, textSmall.apply { textSize = 11f; isFakeBoldText = true })
            canvas.drawText(vals[i], cx + 8f, r1Bottom + 38f, textBold.apply { textSize = 13f })
        }

        // Row 3: Company Name & Logo
        val r3Bottom = r2Bottom + 85f
        canvas.drawLine(left, r3Bottom, right, r3Bottom, strokePaint)
        canvas.drawRect(left + 15f, r2Bottom + 12f, left + 75f, r2Bottom + 72f, headerFill)
        canvas.drawText("S", left + 33f, r2Bottom + 52f, Paint().apply { color = 0xFFFFFFFF.toInt(); textSize = 38f; isFakeBoldText = true })

        canvas.drawText(info.companyName, left + 90f, r2Bottom + 32f, textBold.apply { textSize = 22f })
        canvas.drawText(info.companyAddress, left + 90f, r2Bottom + 54f, textSmall.apply { textSize = 11f })
        canvas.drawText("E-MAIL : ${info.companyEmail}", left + 90f, r2Bottom + 72f, textSmall.apply { textSize = 12f; color = 0xFF0284C7.toInt(); isFakeBoldText = true })

        // Row 4: Equipment specs
        val r4Bottom = r3Bottom + 95f
        canvas.drawLine(left, r4Bottom, right, r4Bottom, strokePaint)
        canvas.drawLine(left + (right - left) * 0.55f, r3Bottom, left + (right - left) * 0.55f, r4Bottom, strokePaint)

        val col2Left = left + (right - left) * 0.55f
        canvas.drawText("TITLE : ${info.drawingTitle}", left + 15f, r3Bottom + 30f, textBold.apply { textSize = 17f })
        canvas.drawText("M.O.C : ${info.materialOfConstruction}", left + 15f, r3Bottom + 60f, textRed)

        canvas.drawText("DRG No. : ${info.drawingNo}", col2Left + 15f, r3Bottom + 25f, textBold.apply { textSize = 15f })
        canvas.drawText("DRG Date : ${info.drawingDate}", col2Left + 15f, r3Bottom + 48f, textSmall)
        canvas.drawText("PO No. : ${info.poNo}  |  PO Date : ${info.poDate}", col2Left + 15f, r3Bottom + 72f, textSmall)

        // Row 5: Notes & Laser Etching
        canvas.drawRect(left, r4Bottom, right, bottom, Paint().apply { color = 0xFFFEF3C7.toInt(); style = Paint.Style.FILL })
        canvas.drawLine(left, r4Bottom, right, r4Bottom, strokePaint)
        canvas.drawText(info.generalNotes, left + 15f, r4Bottom + 25f, textSmall.apply { textSize = 12f; color = 0xFF78350F.toInt(); isFakeBoldText = true })
    }

    private fun drawPrintArrow(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, paint: Paint) {
        val dx = x2 - x1
        val dy = y2 - y1
        val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
        val uX = dx / len
        val uY = dy / len
        val arrowLen = 10f
        val arrowWidth = 4f
        val path = android.graphics.Path().apply {
            moveTo(x1, y1)
            lineTo(x1 + uX * arrowLen - uY * arrowWidth, y1 + uY * arrowLen + uX * arrowWidth)
            lineTo(x1 + uX * arrowLen + uY * arrowWidth, y1 + uY * arrowLen - uX * arrowWidth)
            close()
        }
        val fillP = Paint(paint).apply { style = Paint.Style.FILL }
        canvas.drawPath(path, fillP)
    }

    fun shareBlueprintBitmap(context: Context, bitmap: Bitmap, title: String) {
        try {
            val cachePath = File(context.cacheDir, "blueprints")
            cachePath.mkdirs()
            val file = File(cachePath, "SS_CAD_Blueprint_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Engineering Blueprint drawing: $title")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export / Share Blueprint"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
