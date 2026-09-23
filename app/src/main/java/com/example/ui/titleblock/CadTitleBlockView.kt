package com.example.ui.titleblock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TitleBlockInfo

@Composable
fun CadTitleBlockCompose(
    info: TitleBlockInfo,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("cad_title_block_card")
            .border(2.dp, Color(0xFF1E293B)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Title Block",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MANUFACTURING FABRICATION TITLE BLOCK",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(24.dp).testTag("edit_title_block_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Title Block",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Row 1: Client
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFCBD5E1))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CLIENT :— ${info.clientName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "ADDRESS :— ${info.clientAddress}   |   CONSULTANT :— ${info.consultant}",
                        fontSize = 10.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            // Row 2: Signoff Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFCBD5E1))
            ) {
                SignoffCell(label = "DRAWN", value = info.drawnBy, modifier = Modifier.weight(1.3f))
                SignoffCell(label = "CHECKED", value = info.checkedBy, modifier = Modifier.weight(1f))
                SignoffCell(label = "APPROVED", value = info.approvedBy, modifier = Modifier.weight(1f))
                SignoffCell(label = "SCALE", value = info.scale, modifier = Modifier.weight(1f))
                SignoffCell(label = "JOB NO.", value = info.jobNo, modifier = Modifier.weight(1.2f))
            }

            // Row 3: Company Details & SSEC Logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFCBD5E1))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF0F172A))
                        .border(1.5.dp, Color(0xFF38BDF8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = info.companyName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = info.companyAddress,
                        fontSize = 8.5.sp,
                        color = Color(0xFF334155),
                        lineHeight = 11.sp
                    )
                    Text(
                        text = "E-MAIL : ${info.companyEmail}",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0284C7)
                    )
                }
            }

            // Row 4: Drawing Title & Technical Specs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFCBD5E1))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.weight(1.6f)) {
                    Text(
                        text = "TITLE : ${info.drawingTitle}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "M.O.C : ${info.materialOfConstruction}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFDC2626)
                    )
                }
                Column(modifier = Modifier.weight(1.1f)) {
                    Text(
                        text = "DRG No. : ${info.drawingNo}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "DRG Date : ${info.drawingDate}",
                        fontSize = 10.sp,
                        color = Color(0xFF475569)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PO No. : ${info.poNo}",
                        fontSize = 10.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "PO Date : ${info.poDate}",
                        fontSize = 10.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            // Row 5: Notes & Laser Etching Callout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF3C7))
                    .border(1.dp, Color(0xFFFDE68A))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = info.generalNotes,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF78350F)
                )
            }
        }
    }
}

@Composable
private fun SignoffCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(0.5.dp, Color(0xFFCBD5E1))
            .padding(vertical = 4.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 9.5.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
    }
}

fun drawEngineeringBorder(
    drawScope: DrawScope,
    canvasWidth: Float,
    canvasHeight: Float,
    isDarkTheme: Boolean = false
) {
    val borderColor = if (isDarkTheme) Color(0xFF475569) else Color(0xFF0F172A)

    val margin = 30f
    val fileMarginLeft = 50f

    val left = fileMarginLeft
    val top = margin
    val right = canvasWidth - margin
    val bottom = canvasHeight - margin

    drawScope.drawRect(
        color = borderColor,
        topLeft = Offset(left, top),
        size = Size(right - left, bottom - top),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
    )

    drawScope.drawRect(
        color = borderColor,
        topLeft = Offset(left + 15f, top + 15f),
        size = Size(right - left - 30f, bottom - top - 30f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f)
    )

    val numCols = 8
    val colW = (right - left - 30f) / numCols
    for (i in 1 until numCols) {
        val x = left + 15f + i * colW
        drawScope.drawLine(borderColor, Offset(x, top), Offset(x, top + 15f), strokeWidth = 1f)
        drawScope.drawLine(borderColor, Offset(x, bottom - 15f), Offset(x, bottom), strokeWidth = 1f)
    }

    val numRows = 5
    val rowH = (bottom - top - 30f) / numRows
    for (i in 1 until numRows) {
        val y = top + 15f + i * rowH
        drawScope.drawLine(borderColor, Offset(left, y), Offset(left + 15f, y), strokeWidth = 1f)
        drawScope.drawLine(borderColor, Offset(right - 15f, y), Offset(right, y), strokeWidth = 1f)
    }
}
