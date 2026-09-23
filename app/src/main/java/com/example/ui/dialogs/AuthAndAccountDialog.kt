package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MembershipTier
import com.example.model.UserProfile

@Composable
fun UserAccountDialog(
    userProfile: UserProfile,
    isLoggedIn: Boolean,
    onSignIn: (email: String, name: String) -> Unit,
    onSignOut: () -> Unit,
    onUpgradePlan: (MembershipTier) -> Unit,
    onDismiss: () -> Unit
) {
    var emailInput by remember { mutableStateOf("engineer@ssfabrication.com") }
    var nameInput by remember { mutableStateOf("Senior CAD Engineer") }
    var isSigningInMode by remember { mutableStateOf(!isLoggedIn) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFF0284C7),
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isLoggedIn) "AutoCAD Cloud Engineering Account" else "Sign In to AutoCAD 3D",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isLoggedIn) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = userProfile.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF166534)
                                )
                                Surface(
                                    color = Color(0xFF16A34A),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = userProfile.membershipPlan.badge,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(text = userProfile.email, fontSize = 11.sp, color = Color(0xFF475569))
                            Text(text = "Org: ${userProfile.organization}", fontSize = 10.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Plan: ${userProfile.membershipPlan.title} (${userProfile.membershipPlan.monthlyPrice})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    Text("Engineering Tier Upgrades & Multi-Seat CAD:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                    MembershipTier.values().forEach { tier ->
                        val isCurrent = tier == userProfile.membershipPlan
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCurrent) Color(0xFFF0F9FF) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCurrent) Color(0xFF0284C7) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpgradePlan(tier) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(tier.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(tier.monthlyPrice, fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color(0xFF0284C7))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                tier.features.forEach { feat ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(feat, fontSize = 9.5.sp, color = Color(0xFF475569))
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onSignOut,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        modifier = Modifier.fillMaxWidth().testTag("auth_signout_button")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign Out", fontSize = 11.sp)
                    }
                } else {
                    Text(
                        "Sign in to sync your 2D manufacturing blueprints and 3D Solid models across workstations.",
                        fontSize = 11.sp,
                        color = Color(0xFF475569)
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Engineer Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_name_input")
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Engineering Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                    )

                    Button(
                        onClick = {
                            if (emailInput.isNotBlank()) {
                                onSignIn(emailInput, nameInput.ifBlank { "CAD Engineer" })
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier.fillMaxWidth().testTag("auth_signin_button")
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign In & Connect Cloud CAD", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
