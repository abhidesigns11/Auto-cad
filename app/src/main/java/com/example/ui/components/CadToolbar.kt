package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CadTool
import com.example.model.CanvasThemeMode

@Composable
fun CadRibbonToolbar(
    activeTool: CadTool,
    onToolSelected: (CadTool) -> Unit,
    onOpenUnfoldCalculator: () -> Unit = {},
    onOpenBomTable: () -> Unit = {},
    onOpenPartsCatalog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CadToolButton(
                tool = CadTool.SELECT,
                label = "Select / Edit",
                icon = Icons.Default.NearMe,
                isSelected = activeTool == CadTool.SELECT,
                onClick = { onToolSelected(CadTool.SELECT) },
                accentColor = Color(0xFFF59E0B)
            )

            CadToolButton(
                tool = CadTool.ROTATE,
                label = "Rotate",
                icon = Icons.Default.RotateRight,
                isSelected = activeTool == CadTool.ROTATE,
                onClick = { onToolSelected(CadTool.ROTATE) },
                accentColor = Color(0xFFF59E0B)
            )

            CadToolButton(
                tool = CadTool.PAN_ZOOM,
                label = "Pan/Zoom",
                icon = Icons.Default.PanTool,
                isSelected = activeTool == CadTool.PAN_ZOOM,
                onClick = { onToolSelected(CadTool.PAN_ZOOM) },
                accentColor = Color(0xFF0284C7)
            )

            CadToolButton(
                tool = CadTool.LINE,
                label = "Line",
                icon = Icons.Default.Timeline,
                isSelected = activeTool == CadTool.LINE,
                onClick = { onToolSelected(CadTool.LINE) }
            )

            CadToolButton(
                tool = CadTool.RECTANGLE,
                label = "Plate/Rect",
                icon = Icons.Default.CropSquare,
                isSelected = activeTool == CadTool.RECTANGLE,
                onClick = { onToolSelected(CadTool.RECTANGLE) }
            )

            CadToolButton(
                tool = CadTool.CIRCLE,
                label = "Circle/Flange",
                icon = Icons.Default.RadioButtonUnchecked,
                isSelected = activeTool == CadTool.CIRCLE,
                onClick = { onToolSelected(CadTool.CIRCLE) }
            )

            CadToolButton(
                tool = CadTool.DISHED_HEAD_STAMP,
                label = "Dished Head",
                icon = Icons.Default.FlipToBack,
                isSelected = activeTool == CadTool.DISHED_HEAD_STAMP,
                onClick = { onToolSelected(CadTool.DISHED_HEAD_STAMP) },
                accentColor = Color(0xFF38BDF8)
            )

            CadToolButton(
                tool = CadTool.SADDLE_SUPPORT,
                label = "Saddle",
                icon = Icons.Default.VerticalAlignBottom,
                isSelected = activeTool == CadTool.SADDLE_SUPPORT,
                onClick = { onToolSelected(CadTool.SADDLE_SUPPORT) }
            )

            CadToolButton(
                tool = CadTool.LEG_SUPPORT,
                label = "Leg Support",
                icon = Icons.Default.FormatLineSpacing,
                isSelected = activeTool == CadTool.LEG_SUPPORT,
                onClick = { onToolSelected(CadTool.LEG_SUPPORT) }
            )

            CadToolButton(
                tool = CadTool.NOZZLE_STAMP,
                label = "Nozzle",
                icon = Icons.Default.Adjust,
                isSelected = activeTool == CadTool.NOZZLE_STAMP,
                onClick = { onToolSelected(CadTool.NOZZLE_STAMP) }
            )

            CadToolButton(
                tool = CadTool.MANWAY_STAMP,
                label = "Manway",
                icon = Icons.Default.MeetingRoom,
                isSelected = activeTool == CadTool.MANWAY_STAMP,
                onClick = { onToolSelected(CadTool.MANWAY_STAMP) }
            )

            CadToolButton(
                tool = CadTool.VALVE_STAMP,
                label = "Valve",
                icon = Icons.Default.Grain,
                isSelected = activeTool == CadTool.VALVE_STAMP,
                onClick = { onToolSelected(CadTool.VALVE_STAMP) }
            )

            CadToolButton(
                tool = CadTool.AGITATOR_STAMP,
                label = "Agitator",
                icon = Icons.Default.Engineering,
                isSelected = activeTool == CadTool.AGITATOR_STAMP,
                onClick = { onToolSelected(CadTool.AGITATOR_STAMP) }
            )

            CadToolButton(
                tool = CadTool.CONE_HOPPER,
                label = "Hopper Cone",
                icon = Icons.Default.FilterAlt,
                isSelected = activeTool == CadTool.CONE_HOPPER,
                onClick = { onToolSelected(CadTool.CONE_HOPPER) }
            )

            CadToolButton(
                tool = CadTool.CENTERLINE,
                label = "Centerline",
                icon = Icons.Default.LinearScale,
                isSelected = activeTool == CadTool.CENTERLINE,
                onClick = { onToolSelected(CadTool.CENTERLINE) }
            )

            CadToolButton(
                tool = CadTool.DIMENSION,
                label = "Dimension",
                icon = Icons.Default.Straighten,
                isSelected = activeTool == CadTool.DIMENSION,
                onClick = { onToolSelected(CadTool.DIMENSION) },
                accentColor = Color(0xFFEF4444)
            )

            CadToolButton(
                tool = CadTool.BOM_ITEM_BUBBLE,
                label = "BOM Item #",
                icon = Icons.Default.Numbers,
                isSelected = activeTool == CadTool.BOM_ITEM_BUBBLE,
                onClick = { onToolSelected(CadTool.BOM_ITEM_BUBBLE) },
                accentColor = Color(0xFF0284C7)
            )

            CadToolButton(
                tool = CadTool.LEADER_NOTE,
                label = "Leader",
                icon = Icons.AutoMirrored.Filled.ArrowRightAlt,
                isSelected = activeTool == CadTool.LEADER_NOTE,
                onClick = { onToolSelected(CadTool.LEADER_NOTE) }
            )

            CadToolButton(
                tool = CadTool.TEXT_NOTE,
                label = "Text/Note",
                icon = Icons.Default.TextFields,
                isSelected = activeTool == CadTool.TEXT_NOTE,
                onClick = { onToolSelected(CadTool.TEXT_NOTE) }
            )

            CadToolButton(
                tool = CadTool.MEASURE_TAPE,
                label = "Measure",
                icon = Icons.Default.SquareFoot,
                isSelected = activeTool == CadTool.MEASURE_TAPE,
                onClick = { onToolSelected(CadTool.MEASURE_TAPE) }
            )

            CadToolButton(
                tool = CadTool.ERASE,
                label = "Erase",
                icon = Icons.Default.Delete,
                isSelected = activeTool == CadTool.ERASE,
                onClick = { onToolSelected(CadTool.ERASE) }
            )
        }
    }
}

@Composable
private fun CadToolButton(
    tool: CadTool,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color? = null
) {
    val bg = if (isSelected) {
        accentColor ?: Color(0xFF0284C7)
    } else {
        if (accentColor != null) Color(0xFF261820) else Color(0xFF1E293B)
    }
    val contentCol = if (isSelected) Color.White else (accentColor ?: Color(0xFFE2E8F0))

    Surface(
        onClick = onClick,
        color = bg,
        shape = MaterialTheme.shapes.extraSmall,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) (accentColor ?: Color(0xFF38BDF8)) else (accentColor?.copy(alpha = 0.5f) ?: Color(0xFF334155))
        ),
        modifier = Modifier.testTag("tool_btn_${tool.name}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = contentCol, modifier = Modifier.size(16.dp))
            Text(text = label, color = contentCol, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun AutoCadCommandLine(
    onExecuteCommand: (String) -> Unit,
    activeTool: CadTool,
    commandPromptMessage: String = "Type a command or tap shortcuts below",
    modifier: Modifier = Modifier
) {
    var commandText by remember { mutableStateOf("") }

    val quickCommands = listOf(
        "L" to "LINE",
        "REC" to "RECT",
        "C" to "CIRCLE",
        "DIM" to "DIMENSION",
        "DISH" to "DISHED HEAD",
        "SADDLE" to "SADDLE",
        "VALVE" to "VALVE",
        "BOM" to "BOM TABLE",
        "UNFOLD" to "UNFOLD PATTERN",
        "ZE" to "ZOOM EXTENTS",
        "PAN" to "PAN",
        "RO" to "ROTATE",
        "M" to "MOVE",
        "E" to "ERASE"
    )

    Surface(
        color = Color(0xFF0A0F1D),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
            // Command Line Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "CMD:",
                    color = Color(0xFF38BDF8),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                TextField(
                    value = commandText,
                    onValueChange = { commandText = it },
                    placeholder = {
                        Text(
                            commandPromptMessage,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (commandText.isNotBlank()) {
                            onExecuteCommand(commandText.trim())
                            commandText = ""
                        }
                    }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        cursorColor = Color(0xFF38BDF8),
                        focusedIndicatorColor = Color(0xFF38BDF8),
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("autocad_cmd_input")
                )

                IconButton(
                    onClick = {
                        if (commandText.isNotBlank()) {
                            onExecuteCommand(commandText.trim())
                            commandText = ""
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Execute Command",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Quick Auto-complete Chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                quickCommands.forEach { (cmd, label) ->
                    Surface(
                        onClick = { onExecuteCommand(cmd) },
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = "$cmd: $label",
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CadStatusBar(
    currentX: Float,
    currentY: Float,
    measuredDist: Float,
    measuredAngle: Float,
    isOrthoEnabled: Boolean,
    onToggleOrtho: () -> Unit,
    isSnapEnabled: Boolean,
    onToggleSnap: () -> Unit,
    canvasTheme: CanvasThemeMode,
    onCycleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0B132B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "X: %.1f mm  Y: %.1f mm".format(currentX, currentY),
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                if (measuredDist > 0f) {
                    Text(
                        text = "DIST: %.0f mm (%.0f°)".format(measuredDist, measuredAngle),
                        color = Color(0xFFF59E0B),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = isOrthoEnabled,
                    onClick = onToggleOrtho,
                    label = { Text("ORTHO", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF10B981),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.height(24.dp).testTag("ortho_toggle")
                )

                FilterChip(
                    selected = isSnapEnabled,
                    onClick = onToggleSnap,
                    label = { Text("OSNAP", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.height(24.dp).testTag("snap_toggle")
                )

                IconButton(
                    onClick = onCycleTheme,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Theme",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
