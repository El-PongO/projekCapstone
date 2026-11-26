package com.example.projectcapstone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val Ad2Background = Color(0xFFF4F7FD)
private val Ad2Navy = Color(0xFF172B55)
private val Ad2Blue = Color(0xFF115AF4)
private val Ad2Green = Color(0xFF12A873)
private val Ad2Amber = Color(0xFFF4A340)
private val Ad2Purple = Color(0xFF785BEA)
private val Ad2Muted = Color(0xFF7D8AA0)
private val Ad2Outline = Color(0xFFE8EDF5)

private fun adminV2Money(value: Double): String {
    return "$" + NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }.format(value)
}

private data class AdminV2Shortcut(
    val label: String,
    val icon: ImageVector,
    val page: AdminPage,
    val color: Color
)

private val adminV2Shortcuts = listOf(
    AdminV2Shortcut("Upline", Icons.Default.TrendingUp, AdminPage.UPLINE_LIST, Ad2Blue),
    AdminV2Shortcut("Downline", Icons.Default.Groups, AdminPage.DOWNLINE_LIST, Ad2Green),
    AdminV2Shortcut("Payments", Icons.Default.AttachMoney, AdminPage.MASTER_PAYMENTS, Ad2Purple),
    AdminV2Shortcut("Projects", Icons.Default.Folder, AdminPage.MASTER_PROJECT, Ad2Amber),
    AdminV2Shortcut("Master Item", Icons.Default.Inventory, AdminPage.MASTER_ITEM, Ad2Blue),
    AdminV2Shortcut("Deal Reports", Icons.Default.CheckCircle, AdminPage.DEALS_REPORTS, Ad2Green),
    AdminV2Shortcut("Export", Icons.Default.Download, AdminPage.EXPORT_REPORTS, Ad2Purple),
    AdminV2Shortcut("Broadcast", Icons.Default.Campaign, AdminPage.BROADCAST, Ad2Amber)
)

/**
 * Admin homepage baru. Dapat dipanggil langsung dari AdminHomeRoute tanpa
 * menghapus / mengganti AdminHomeScreenContent() yang sudah ada.
 * Semua angka dan daftar tetap berasal dari respons API Admin.
 */
@Composable
fun IntegraAdminHomeV2(
    userName: String = "Admin",
    overview: AdminOverviewResponse = AdminOverviewResponse(),
    notifications: List<NotificationDto> = emptyList(),
    recentProjects: List<ProjectDto> = emptyList(),
    onNavigate: (AdminPage) -> Unit = {},
    onOpenDealDetails: (DealDetailSelection) -> Unit = {}
) {
    var selectedNotification by remember { mutableStateOf<NotificationDto?>(null) }
    val userFirstName = userName.substringBefore(" ").ifBlank { "Admin" }

    Scaffold(
        containerColor = Ad2Background,
        bottomBar = {
            AdminBottomBar(
                currentPage = AdminPage.HOME,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Ad2Background)
                .padding(innerPadding)
        ) {
            // Pertahankan notification bell, dialog, dan FCM indicator dari header asli.
            RoleTopBar(
                roleTitle = "Admin",
                userName = userName
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        shadowElevation = 5.dp,
                        color = Ad2Navy
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Ad2Navy, Ad2Blue)
                                    )
                                )
                                .padding(horizontal = 20.dp, vertical = 22.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "ADMIN DASHBOARD",
                                    color = Color.White.copy(alpha = 0.73f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Halo, $userFirstName!",
                                    color = Color.White,
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    "Pantau performa tim dan aktivitas proyek dalam satu tempat.",
                                    color = Color.White.copy(alpha = 0.86f),
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Ad2SectionTitle("Business Overview", "Ringkasan aktivitas Integra Agency")
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                        Ad2MetricCard(
                            title = "Total Projects",
                            value = overview.projects.total.toString(),
                            icon = Icons.Default.Workspaces,
                            accent = Ad2Blue,
                            modifier = Modifier.weight(1f)
                        )
                        Ad2MetricCard(
                            title = "Deal Value",
                            value = adminV2Money(overview.projects.dealValue),
                            icon = Icons.Default.TrendingUp,
                            accent = Ad2Green,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(11.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                        Ad2MetricCard(
                            title = "Commissions",
                            value = adminV2Money(overview.commissions.total),
                            icon = Icons.Default.AttachMoney,
                            accent = Ad2Purple,
                            modifier = Modifier.weight(1f)
                        )
                        Ad2MetricCard(
                            title = "Total Agents",
                            value = (overview.agents.uplines + overview.agents.downlines).toString(),
                            icon = Icons.Default.Groups,
                            accent = Ad2Amber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Ad2Card {
                        Ad2SectionTitle("Quick Actions", "Akses semua modul Admin")
                        Spacer(Modifier.height(18.dp))
                        adminV2Shortcuts.chunked(4).forEachIndexed { index, rowShortcuts ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowShortcuts.forEach { shortcut ->
                                    Ad2ShortcutItem(shortcut = shortcut) {
                                        onNavigate(shortcut.page)
                                    }
                                }
                            }
                            if (index == 0) Spacer(Modifier.height(19.dp))
                        }
                    }
                }

                item {
                    Ad2SectionTitle("Recent Activity", "Notifikasi terbaru dari tim")
                    Spacer(Modifier.height(11.dp))
                    Ad2Card {
                        if (notifications.isEmpty()) {
                            Ad2EmptyState("Belum ada notifikasi terbaru.")
                        } else {
                            notifications.take(4).forEachIndexed { index, notification ->
                                Ad2NotificationRow(notification) {
                                    selectedNotification = notification
                                }
                                if (index < notifications.take(4).lastIndex) {
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }

                item {
                    Ad2SectionTitle("Recent Projects", "Project terbaru yang tercatat")
                    Spacer(Modifier.height(11.dp))
                    Ad2Card {
                        if (recentProjects.isEmpty()) {
                            Ad2EmptyState("Belum ada project terbaru.")
                        } else {
                            recentProjects.take(3).forEachIndexed { index, project ->
                                Ad2ProjectRow(project) {
                                    onOpenDealDetails(
                                        DealDetailSelection(
                                            projectId = project.id,
                                            clientName = project.clientName,
                                            productType = "Project",
                                            dealType = project.status,
                                            initialPrice = project.dealValue,
                                            agentResponsible = project.agentCode ?: "-",
                                            clientPhone = project.clientPhone.orEmpty(),
                                            dealDetails = ""
                                        )
                                    )
                                }
                                if (index < recentProjects.take(3).lastIndex) {
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedNotification?.let { notification ->
        AlertDialog(
            onDismissRequest = { selectedNotification = null },
            title = { Text(notification.title.ifBlank { "Notification" }, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(notification.message.ifBlank { "Tidak ada detail tambahan." })
                    notification.createdAt?.let { date ->
                        Text("Date: ${date.take(10)}", color = Ad2Muted, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedNotification = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun Ad2Card(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Ad2Outline)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun Ad2SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ad2Navy)
        Text(subtitle, fontSize = 12.sp, color = Ad2Muted)
    }
}

@Composable
private fun Ad2MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Ad2Outline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Text(title, fontSize = 12.sp, color = Ad2Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                value,
                fontSize = if (value.length > 12) 17.sp else if (value.length > 8) 19.sp else 23.sp,
                color = Ad2Navy,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun Ad2ShortcutItem(shortcut: AdminV2Shortcut, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(73.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(17.dp))
                .background(shortcut.color.copy(alpha = 0.11f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                shortcut.icon,
                contentDescription = shortcut.label,
                tint = shortcut.color,
                modifier = Modifier.size(23.dp)
            )
        }
        Spacer(Modifier.height(7.dp))
        Text(
            shortcut.label,
            color = Ad2Navy,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2
        )
    }
}

@Composable
private fun Ad2NotificationRow(notification: NotificationDto, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = Ad2Background
    ) {
        Row(modifier = Modifier.padding(13.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape)
                    .background(if (notification.isRead) Color(0xFFE5E9F2) else Ad2Blue.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (notification.isRead) Ad2Muted else Ad2Blue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notification.title.ifBlank { "Activity update" },
                    fontWeight = FontWeight.Bold,
                    color = Ad2Navy,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    notification.message,
                    color = Ad2Muted,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                notification.createdAt?.let {
                    Spacer(Modifier.height(7.dp))
                    Text(it.take(10), fontSize = 10.sp, color = Ad2Muted)
                }
            }
            if (!notification.isRead) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Ad2Blue))
            }
        }
    }
}

@Composable
private fun Ad2ProjectRow(project: ProjectDto, onClick: () -> Unit) {
    val statusColor = when (project.status.lowercase()) {
        "deal" -> Ad2Green
        "follow_up" -> Ad2Amber
        "no_deal", "cancelled" -> Color(0xFFE85E68)
        else -> Ad2Blue
    }
    val statusText = project.status.replace('_', ' ').replaceFirstChar { it.uppercase() }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Ad2Outline),
        color = Color.White
    ) {
        Row(modifier = Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))
                    .background(Ad2Blue.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Workspaces, contentDescription = null, tint = Ad2Blue)
            }
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    project.clientName.ifBlank { project.projectCode },
                    fontWeight = FontWeight.Bold,
                    color = Ad2Navy,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(project.projectCode, color = Ad2Muted, fontSize = 11.sp)
                Spacer(Modifier.height(5.dp))
                Text(
                    statusText,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
            Text(
                adminV2Money(project.dealValue),
                color = Ad2Navy,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun Ad2EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp), contentAlignment = Alignment.Center) {
        Text(message, color = Ad2Muted, fontSize = 13.sp)
    }
}
