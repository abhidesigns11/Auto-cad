package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*

@Composable
fun EditTitleBlockDialog(
    currentInfo: TitleBlockInfo,
    onSave: (TitleBlockInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var clientName by remember { mutableStateOf(currentInfo.clientName) }
    var clientAddress by remember { mutableStateOf(currentInfo.clientAddress) }
    var consultant by remember { mutableStateOf(currentInfo.consultant) }
    var drawnBy by remember { mutableStateOf(currentInfo.drawnBy) }
    var checkedBy by remember { mutableStateOf(currentInfo.checkedBy) }
    var approvedBy by remember { mutableStateOf(currentInfo.approvedBy) }
    var scale by remember { mutableStateOf(currentInfo.scale) }
    var jobNo by remember { mutableStateOf(currentInfo.jobNo) }
    var companyName by remember { mutableStateOf(currentInfo.companyName) }
    var companyAddress by remember { mutableStateOf(currentInfo.companyAddress) }
    var companyEmail by remember { mutableStateOf(currentInfo.companyEmail) }
    var drawingTitle by remember { mutableStateOf(currentInfo.drawingTitle) }
    var materialOfConstruction by remember { mutableStateOf(currentInfo.materialOfConstruction) }
    var drawingNo by remember { mutableStateOf(currentInfo.drawingNo) }
    var drawingDate by remember { mutableStateOf(currentInfo.drawingDate) }
    var poNo by remember { mutableStateOf(currentInfo.poNo) }
    var poDate by remember { mutableStateOf(currentInfo.poDate) }
    var generalNotes by remember { mutableStateOf(currentInfo.generalNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Article, contentDescription = null, tint = Color(0xFF0284C7))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Manufacturing Title Block", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Client & Drawing Specs:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0284C7))

                OutlinedTextField(
                    value = drawingTitle,
                    onValueChange = { drawingTitle = it },
                    label = { Text("Drawing Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("tb_input_title")
                )

                OutlinedTextField(
                    value = materialOfConstruction,
                    onValueChange = { materialOfConstruction = it },
                    label = { Text("Material Of Construction (M.O.C)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("tb_input_moc")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = drawingNo,
                        onValueChange = { drawingNo = it },
                        label = { Text("DRG No.") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = drawingDate,
                        onValueChange = { drawingDate = it },
                        label = { Text("DRG Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = clientAddress,
                        onValueChange = { clientAddress = it },
                        label = { Text("Client Address / City") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = drawnBy,
                        onValueChange = { drawnBy = it },
                        label = { Text("Drawn By") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = checkedBy,
                        onValueChange = { checkedBy = it },
                        label = { Text("Checked By") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = scale,
                        onValueChange = { scale = it },
                        label = { Text("Scale") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = poNo,
                        onValueChange = { poNo = it },
                        label = { Text("P.O. Number") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = jobNo,
                        onValueChange = { jobNo = it },
                        label = { Text("Job No.") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = generalNotes,
                    onValueChange = { generalNotes = it },
                    label = { Text("Notes / Laser Marking Requirement") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Fabrication Engineering Company:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0284C7))

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = companyAddress,
                    onValueChange = { companyAddress = it },
                    label = { Text("Company Address") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = companyEmail,
                    onValueChange = { companyEmail = it },
                    label = { Text("Company Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        TitleBlockInfo(
                            clientName = clientName,
                            clientAddress = clientAddress,
                            consultant = consultant,
                            drawnBy = drawnBy,
                            checkedBy = checkedBy,
                            approvedBy = approvedBy,
                            scale = scale,
                            jobNo = jobNo,
                            companyName = companyName,
                            companyAddress = companyAddress,
                            companyEmail = companyEmail,
                            drawingTitle = drawingTitle,
                            materialOfConstruction = materialOfConstruction,
                            drawingNo = drawingNo,
                            drawingDate = drawingDate,
                            poNo = poNo,
                            poDate = poDate,
                            generalNotes = generalNotes
                        )
                    )
                },
                modifier = Modifier.testTag("save_title_block_button")
            ) {
                Text("Save Title Block")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ProjectsListDialog(
    projects: List<CadProject>,
    currentProject: CadProject,
    onSelectProject: (CadProject) -> Unit,
    onNewProject: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF0284C7))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Equipment CAD Blueprints", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onNewProject()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth().testTag("new_blank_project_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create New Blank Drawing Sheet")
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Text("Sample Manufacturing Drawings:", fontSize = 12.sp, color = Color(0xFF64748B))

                projects.forEach { p ->
                    val isSelected = p.name == currentProject.name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectProject(p)
                                onDismiss()
                            }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF0284C7) else Color(0xFFE2E8F0)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFF0F9FF) else Color.White
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = p.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) Color(0xFF0284C7) else Color(0xFF0F172A)
                            )
                            Text(
                                text = p.subtitle,
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "MOC: ${p.titleBlock.materialOfConstruction}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFDC2626)
                                )
                                Text(
                                    text = "${p.entities.size} CAD Entities",
                                    fontSize = 10.sp,
                                    color = Color(0xFF0284C7)
                                )
                            }
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
fun DxfExportDialog(
    project: CadProject,
    onDismiss: () -> Unit
) {
    val dxfText = remember(project) { DxfExportEngine.generateDxf(project) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF10B981))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export AutoCAD R12 / 2000 DXF", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Standard ASCII DXF format ready for AutoCAD, SolidWorks, Laser CNC & CAM software.",
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )

                Surface(
                    color = Color(0xFF0F172A),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                ) {
                    Text(
                        text = dxfText,
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState())
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
fun BillOfMaterialsDialog(
    project: CadProject,
    onDismiss: () -> Unit
) {
    val bomItems = remember(project) {
        val list = mutableListOf<BomItem>()
        var lineTotalLength = 0f
        var circlesCount = 0
        var nozzlesCount = 0
        var platesArea = 0f

        for (e in project.entities) {
            when (e) {
                is CadLine -> {
                    val dx = e.x2 - e.x1
                    val dy = e.y2 - e.y1
                    lineTotalLength += kotlin.math.sqrt(dx * dx + dy * dy)
                }
                is CadCircle -> circlesCount++
                is CadNozzleComponent -> nozzlesCount++
                is CadRect -> platesArea += (e.width * e.height) / 1_000_000f // sq meters
                else -> {}
            }
        }

        list.add(BomItem("1", "Main Shell / Body Plate", project.titleBlock.materialOfConstruction, "1 SET", "Laser cut, folded & seam welded"))
        if (platesArea > 0f) {
            list.add(BomItem("2", "SS Plate Consumption", "SS 304 2B Finish", "%.2f m²".format(platesArea * 1.2f), "Includes 20% nesting scrap margin"))
        }
        list.add(BomItem("3", "Welding Line Passivations", "TIG Argon AWS ER308L", "%.1f m".format(lineTotalLength / 1000f), "Ground flush & pickled"))
        list.add(BomItem("4", "Flanges / Nozzle Connections", "SS 304 Table D / ANSI 150#", "$nozzlesCount NOS", "Facing Ra 3.2 µm, RF Serrated"))
        list.add(BomItem("5", "Hardware Fasteners & Gaskets", "SS 304 A2-70 & PTFE/EPDM", "1 LOT", "Food/Pharma grade approved"))
        list
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ListAlt, contentDescription = null, tint = Color(0xFF0284C7))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bill of Materials (B.O.M)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Project: ${project.titleBlock.drawingTitle}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF0F172A)
                )

                bomItems.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.itemNo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.width(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.description, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(text = item.material, fontSize = 10.sp, color = Color(0xFFDC2626))
                                Text(text = item.remarks, fontSize = 9.sp, color = Color(0xFF64748B))
                            }
                            Text(
                                text = item.quantity,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

private data class BomItem(
    val itemNo: String,
    val description: String,
    val material: String,
    val quantity: String,
    val remarks: String
)
