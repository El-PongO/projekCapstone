package com.example.projectcapstone

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DealBg = Color(0xFFF3F6FB)
private val DealBlue = Color(0xFF1155F5)
private val DealBlueDark = Color(0xFF0D3DB8)
private val DealTextMuted = Color(0xFF6C7384)
private val DealBorder = Color(0xFFE5EAF3)
private val DealCard = Color.White
private val DealGreen = Color(0xFF22B36B)
private val DealSoftBlue = Color(0xFFEFF4FF)

@Composable
fun IntegraModernDealsDetailsScreen(
    roleTitle: String,
    userName: String,
    clientName: String,
    returningClient: String,
    clientNumber: String,
    agentResponsible: String,
    productType: String,
    dealType: String,
    initialPriceFormatted: String,
    dealDetails: String,
    centerBarLabel: String,
    loading: Boolean,
    generatingPdf: Boolean,
    onGenerateProposalPdf: () -> Unit,
    onBack: () -> Unit,
    onNotificationBellClick: () -> Unit,
    onHome: () -> Unit,
    onCenterClick: () -> Unit,
    onProfile: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = DealBg,
        bottomBar = {
            val adminNavigation = LocalIntegraAdminNav.current
            if (roleTitle.equals("Admin", ignoreCase = true) && adminNavigation != null) {
                AdminBottomBar(currentPage = AdminPage.MASTER_PROJECT, onNavigate = adminNavigation)
            } else {
                RoleBottomBar(
                    centerLabel = centerBarLabel,
                    onHome = onHome,
                    onCenterClick = onCenterClick,
                    onProfile = onProfile
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DealBg)
        ) {
            RoleTopBar(roleTitle = roleTitle, userName = userName, onNotificationClick = onNotificationBellClick)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .clickable { onBack() },
                    shape = CircleShape,
                    color = DealBlue,
                    shadowElevation = 3.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Text("Deals Details", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(DealCard)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OverviewCard(productType = productType, dealType = dealType, price = initialPriceFormatted)

                if (loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DealBlue)
                    }
                } else {
                    DetailSectionCard(title = "Client Details") {
                        InfoRow(icon = Icons.Default.Person, label = "Client Name", value = clientName)
                        InfoRow(icon = Icons.Default.Star, label = "Returning Client", value = returningClient)
                        ClickableInfoRow(
                            icon = Icons.Default.Phone,
                            label = "Client Number",
                            value = clientNumber,
                            actionLabel = "Call"
                        ) {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clientNumber")))
                            }.onFailure {
                                Toast.makeText(context, clientNumber.ifBlank { "Nomor klien tidak tersedia" }, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    DetailSectionCard(title = "Project Details") {
                        InfoRow(icon = Icons.Default.Badge, label = "Agent Responsible", value = agentResponsible)
                        InfoRow(icon = Icons.Default.Storefront, label = "Product Type", value = productType)
                        InfoRow(icon = Icons.Default.BusinessCenter, label = "Deal Type", value = dealType)
                        InfoRow(icon = Icons.Default.Work, label = "Initial Price", value = initialPriceFormatted)
                    }

                    DetailSectionCard(title = "Deal Description") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = DealSoftBlue,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DealBorder)
                        ) {
                            Text(
                                text = dealDetails.ifBlank { "Belum ada detail deal." },
                                modifier = Modifier.padding(14.dp),
                                color = Color.Black,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Button(
                        onClick = onGenerateProposalPdf,
                        enabled = !generatingPdf,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DealBlue),
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (generatingPdf) "Generating PDF..." else "Generate Proposal PDF",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(productType: String, dealType: String, price: String) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        color = DealCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, DealBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF9FBFF), Color(0xFFF1F6FF))
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DealBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = DealBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Project Overview", fontSize = 12.sp, color = DealTextMuted, fontWeight = FontWeight.Medium)
                        Text(productType, fontSize = 20.sp, color = Color.Black, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DealChip(text = dealType, background = DealBlue.copy(alpha = 0.10f), foreground = DealBlueDark)
                }

                Surface(shape = RoundedCornerShape(18.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, DealBorder)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Initial Price", color = DealTextMuted, fontSize = 12.sp)
                            Text(price, color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(DealBlue)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text("Project", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DealChip(text: String, background: Color, foreground: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(text = text, color = foreground, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

@Composable
private fun DetailSectionCard(title: String, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 3.dp,
        color = DealCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, DealBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            HorizontalDivider(color = DealBorder)
            content()
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(DealBlue.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = DealBlue, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = DealTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(3.dp))
            Text(value.ifBlank { "-" }, color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun ClickableInfoRow(icon: ImageVector, label: String, value: String, actionLabel: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(DealBlue.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = DealBlue, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = DealTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(3.dp))
            Text(value.ifBlank { "-" }, color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Surface(
            modifier = Modifier.clickable { onClick() },
            shape = RoundedCornerShape(50),
            color = DealBlue.copy(alpha = 0.10f)
        ) {
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Call, contentDescription = actionLabel, tint = DealBlue, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(actionLabel, color = DealBlueDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
