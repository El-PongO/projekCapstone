package com.example.projectcapstone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val AdminModernBg = Color(0xFFF4F7FB)
private val AdminModernCard = Color.White
private val AdminModernBlue = Color(0xFF1C63F2)
private val AdminModernBlueDark = Color(0xFF1846D8)
private val AdminModernGreen = Color(0xFF1FA863)
private val AdminModernRed = Color(0xFFF05454)
private val AdminModernOrange = Color(0xFFF6A623)
private val AdminModernTextMuted = Color(0xFF7B8799)
private val AdminModernBorder = Color(0xFFE8EDF5)

private fun fmtMoney(value: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }
    return "$${nf.format(value)}"
}

@Composable
private fun ModernAdminPageTitle(title: String, onBack: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        onBack?.let {
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .clickable { it() },
                shape = CircleShape,
                color = AdminModernBlue,
                shadowElevation = 3.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        if (trailing != null) Box(modifier = Modifier.align(Alignment.CenterEnd)) { trailing() }
    }
}

@Composable
private fun ModernStatCard(title: String, value: String, accent: Color, icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = AdminModernCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, AdminModernBorder),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Text(title, color = AdminModernTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(value, color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ModernSectionCard(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = AdminModernCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, AdminModernBorder),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = {
            Text(title, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold, fontSize = 17.sp)
            if (!subtitle.isNullOrBlank()) Text(subtitle, color = AdminModernTextMuted, fontSize = 12.sp)
            content()
        })
    }
}

@Composable
fun IntegraModernAdminHomeScreen(
    userName: String,
    overview: AdminOverviewResponse,
    notifications: List<NotificationDto>,
    recentProjects: List<ProjectDto>,
    onNavigate: (AdminPage) -> Unit,
    onOpenDealDetails: (DealDetailSelection) -> Unit
) {
    Scaffold(containerColor = AdminModernBg, bottomBar = { AdminBottomBar(currentPage = AdminPage.HOME, onNavigate = onNavigate) }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(AdminModernBg)) {
            RoleTopBar(roleTitle = "Admin", userName = userName, onNotificationClick = { onNavigate(AdminPage.BROADCAST) })
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ModernStatCard("Clients in Contact", "${overview.agents.relations}", AdminModernBlue, Icons.Default.Groups)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ModernStatCard("Projects in Progress", "${overview.projects.total}", AdminModernOrange, Icons.Default.Workspaces)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ModernStatCard("Deal Success", "${overview.projects.deals}", AdminModernGreen, Icons.Default.CheckCircle)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ModernStatCard("Total Employees", "${overview.agents.uplines + overview.agents.downlines}", AdminModernRed, Icons.Default.Person)
                        }
                    }
                }
                item {
                    ModernSectionCard("My Admin Menu", "Quick access to the main modules") {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            listOf(
                                listOf(
                                    Triple("Upline", Icons.Default.TrendingUp, AdminPage.UPLINE_LIST),
                                    Triple("Downline", Icons.Default.Groups, AdminPage.DOWNLINE_LIST),
                                    Triple("Payments", Icons.Default.AttachMoney, AdminPage.MASTER_PAYMENTS)
                                ),
                                listOf(
                                    Triple("Projects", Icons.Default.Workspaces, AdminPage.MASTER_PROJECT),
                                    Triple("Master Item", Icons.Default.Inventory2, AdminPage.MASTER_ITEM),
                                    Triple("Reports", Icons.Default.Campaign, AdminPage.DEALS_REPORTS)
                                ),
                                listOf(
                                    Triple("Export Reports", Icons.Default.Download, AdminPage.EXPORT_REPORTS)
                                )
                            ).forEach { rowItems ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    rowItems.forEach { (label, icon, page) ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(92.dp).clickable { onNavigate(page) }) {
                                            Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(AdminModernBlue), contentAlignment = Alignment.Center) {
                                                Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    ModernSectionCard("Recent Employee Activity", "Latest approvals, clients, and follow-ups") {
                        val notifList = notifications.take(3)
                        if (notifList.isEmpty()) {
                            Text("No recent activity.", color = AdminModernTextMuted, fontSize = 13.sp)
                        } else {
                            notifList.forEach { notif ->
                                Surface(shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, AdminModernBorder), color = Color.White) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(AdminModernGreen.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AdminModernGreen, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(Modifier.width(10.dp))
                                            Text(notif.title.ifBlank { "Activity update" }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text(notif.message.ifBlank { "No notification details." }, fontSize = 12.sp, color = Color(0xFF475569), maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    ModernSectionCard("Recent Projects", "Tap a card to view deal details") {
                        val projectList = recentProjects.take(3)
                        if (projectList.isEmpty()) {
                            Text("No recent projects.", color = AdminModernTextMuted, fontSize = 13.sp)
                        } else {
                            projectList.forEach { project ->
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminModernBorder),
                                    color = Color.White,
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        onOpenDealDetails(
                                            DealDetailSelection(
                                                projectId = project.id,
                                                clientName = project.clientName,
                                                clientPhone = project.clientPhone ?: "-",
                                                productType = project.stage,
                                                dealType = project.status,
                                                initialPrice = project.dealValue,
                                                dealDetails = project.clientEmail ?: ""
                                            )
                                        )
                                    }
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(project.clientName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text("Code: ${project.projectCode}", fontSize = 12.sp, color = AdminModernTextMuted)
                                            Text(project.status.replaceFirstChar { it.uppercase() }, fontSize = 12.sp, color = AdminModernBlueDark, fontWeight = FontWeight.SemiBold)
                                        }
                                        Text(fmtMoney(project.dealValue), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = AdminModernGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IntegraModernAdminAgentsHubScreen(
    userName: String,
    onBack: () -> Unit,
    onUplineClick: () -> Unit,
    onDownlineClick: () -> Unit,
    onBottomNavigate: (AdminPage) -> Unit
) {
    Scaffold(containerColor = AdminModernBg, bottomBar = { AdminBottomBar(currentPage = AdminPage.AGENTS_HUB, onNavigate = onBottomNavigate) }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(AdminModernBg)) {
            RoleTopBar(roleTitle = "Admin", userName = userName)
            ModernAdminPageTitle(title = "Select an Agent to View", onBack = onBack)
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("Choose an agent hierarchy to view", fontSize = 13.sp, color = AdminModernTextMuted)
                ModernAgentHubCard(
                    title = "Upline",
                    subtitle = "View direct project managers",
                    countText = "Uplines",
                    count = 23,
                    accent = AdminModernBlue,
                    icon = Icons.Default.TrendingUp,
                    onClick = onUplineClick
                )
                ModernAgentHubCard(
                    title = "Downline",
                    subtitle = "View employees and recruits",
                    countText = "Downlines",
                    count = 123,
                    accent = AdminModernGreen,
                    icon = Icons.Default.Groups,
                    onClick = onDownlineClick
                )
            }
        }
    }
}

@Composable
private fun ModernAgentHubCard(title: String, subtitle: String, countText: String, count: Int, accent: Color, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, AdminModernBorder),
        shadowElevation = 4.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(84.dp).clip(RoundedCornerShape(22.dp)).background(accent.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Surface(shape = RoundedCornerShape(50), color = accent.copy(alpha = 0.12f)) {
                    Text("$countText • $count", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(subtitle, fontSize = 13.sp, color = AdminModernTextMuted)
            }
        }
    }
}

@Composable
fun IntegraModernAdminAgentListScreen(
    userName: String,
    title: String,
    agents: List<UserProfileDto>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onSelectAgent: (UserProfileDto) -> Unit,
    onBottomNavigate: (AdminPage) -> Unit
) {
    Scaffold(containerColor = AdminModernBg, bottomBar = { AdminBottomBar(currentPage = AdminPage.AGENTS_HUB, onNavigate = onBottomNavigate) }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(AdminModernBg)) {
            RoleTopBar(roleTitle = "Admin", userName = userName)
            ModernAdminPageTitle(title = title, onBack = onBack, trailing = {
                Surface(modifier = Modifier.size(40.dp).clickable { onAddClick() }, shape = CircleShape, color = AdminModernBlue) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White) }
                }
            })
            Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    placeholder = { Text("Search by name, code, or email") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(26.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, AdminModernBorder), shadowElevation = 4.dp, modifier = Modifier.fillMaxSize()) {
                    LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (agents.isEmpty()) {
                            item { Text("No agents match your search.", color = AdminModernTextMuted, fontSize = 13.sp) }
                        }
                        items(agents) { agent ->
                            Surface(modifier = Modifier.fillMaxWidth().clickable { onSelectAgent(agent) }, shape = RoundedCornerShape(20.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, AdminModernBorder)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(62.dp).clip(RoundedCornerShape(18.dp)).background(AdminModernBlue.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, contentDescription = agent.name, tint = AdminModernBlue, modifier = Modifier.size(30.dp))
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(agent.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                        Text("${agent.role.replaceFirstChar { it.uppercase() }} • ${agent.agentCode}", fontSize = 12.sp, color = AdminModernTextMuted)
                                        Text(agent.email, fontSize = 12.sp, color = Color(0xFF475569), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("Status: ${if (agent.isActive) "Active" else "Inactive"}", fontSize = 12.sp, color = if (agent.isActive) AdminModernGreen else AdminModernRed)
                                    }
                                    Surface(shape = RoundedCornerShape(50), color = AdminModernBlue) {
                                        Text("View details", modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IntegraModernAdminUserDetailsScreen(
    userName: String,
    agent: UserProfileDto,
    onBack: () -> Unit,
    onBottomNavigate: (AdminPage) -> Unit,
    onEditRateClick: () -> Unit
) {
    val isDownline = agent.role.equals("downline", ignoreCase = true)
    val performanceLabel = if (isDownline) "Clients Pulled" else "Deals Pulled"
    val assignedLabel = if (isDownline) "Upline Assigned" else "Downlines Assigned"
    val assignedCount = if (isDownline) 1 else 3
    Scaffold(containerColor = AdminModernBg, bottomBar = { AdminBottomBar(currentPage = AdminPage.USER_DETAILS, onNavigate = onBottomNavigate) }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(AdminModernBg)) {
            RoleTopBar(roleTitle = "Admin", userName = userName)
            ModernAdminPageTitle(title = "Master User Details", onBack = onBack)
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ModernSectionCard(title = agent.name, subtitle = "Agent code: ${agent.agentCode}") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(86.dp).clip(RoundedCornerShape(24.dp)).background(AdminModernBlue.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = AdminModernBlue, modifier = Modifier.size(42.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ModernBadge(agent.role.replaceFirstChar { it.uppercase() }, AdminModernBlue)
                                ModernBadge((agent.agentType ?: "external").replaceFirstChar { it.uppercase() }, AdminModernGreen)
                            }
                            Spacer(Modifier.height(8.dp))
                            ModernInfoLine(Icons.Default.Email, agent.email)
                            Spacer(Modifier.height(4.dp))
                            ModernInfoLine(Icons.Default.Phone, agent.phoneNumber ?: "-")
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.weight(1f)) { ModernStatCard("Commission Rate", "${agent.ratePercent}%", AdminModernGreen, Icons.Default.AttachMoney) }
                        Box(modifier = Modifier.weight(1f)) { ModernStatCard(performanceLabel, if (isDownline) "67" else "67", AdminModernBlue, Icons.Default.TrendingUp) }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onEditRateClick, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = AdminModernBlue)) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Update Commission / Scheme", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                ModernSectionCard(title = "$assignedLabel • $assignedCount") {
                    repeat(assignedCount) { idx ->
                        Surface(shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, AdminModernBorder), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(AdminModernBlue.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                                    Icon(if (isDownline) Icons.Default.TrendingUp else Icons.Default.Groups, contentDescription = null, tint = AdminModernBlue, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(if (isDownline) "Budi Upline" else "Emma Watson", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(if (isDownline) "UPL-001" else "DL-00${idx + 1}", fontSize = 12.sp, color = AdminModernTextMuted)
                                }
                                if (!isDownline && idx == 0) {
                                    Surface(shape = RoundedCornerShape(50), color = AdminModernBlue) {
                                        Text("Assign", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                if (isDownline) {
                    ModernSectionCard(title = "Active clients") {
                        repeat(2) {
                            Surface(shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, AdminModernBorder), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(AdminModernBlue.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Storefront, contentDescription = null, tint = AdminModernBlue, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("PT Arunika Musangking", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Follow up • $8,000", fontSize = 12.sp, color = AdminModernTextMuted)
                                    }
                                }
                            }
                        }
                    }
                }

                ModernSectionCard(title = "User's Biodata") {
                    ModernInfoPair("Date of birth", "18th Jan 2007")
                    ModernInfoPair("Married", "No")
                    ModernInfoPair("Phone Number", agent.phoneNumber ?: "0834 5678 9322")
                    ModernInfoPair("Email", agent.email)
                    ModernInfoPair("Linked-in", "https://www.linkedin.com/in/yourname")
                }
            }
        }
    }
}

@Composable
private fun ModernInfoLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AdminModernTextMuted, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = Color(0xFF334155), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ModernInfoPair(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = AdminModernTextMuted)
        Spacer(Modifier.width(8.dp))
        Text(value, fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ModernBadge(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.12f)) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun IntegraModernAdminMasterItemScreen(
    userName: String,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    items: List<MasterItemDto>,
    categories: List<MasterCategoryDto>,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onBottomNavigate: (AdminPage) -> Unit
) {
    val categoryMap = remember(categories) { categories.associateBy { it.id } }
    Scaffold(containerColor = AdminModernBg, bottomBar = { AdminBottomBar(currentPage = AdminPage.MASTER_ITEM, onNavigate = onBottomNavigate) }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(AdminModernBg)) {
            RoleTopBar(roleTitle = "Admin", userName = userName)
            ModernAdminPageTitle(title = "Master Item", onBack = onBack, trailing = {
                Surface(modifier = Modifier.size(42.dp).clickable { onAddClick() }, shape = CircleShape, color = AdminModernBlue) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White) }
                }
            })
            Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                Surface(shape = RoundedCornerShape(24.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, AdminModernBorder), shadowElevation = 4.dp, modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ModernToggleTab("All Items", selected = selectedTab == 0, onClick = { onSelectTab(0) })
                            ModernToggleTab("Categories", selected = selectedTab == 1, onClick = { onSelectTab(1) })
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = AdminModernBorder)
                        Spacer(Modifier.height(12.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                            if (selectedTab == 0) {
                                items(items) { item ->
                                    val category = categoryMap[item.categoryId]?.categoryName ?: "General"
                                    val accent = when (category.lowercase()) {
                                        "pharmacy" -> AdminModernOrange
                                        "retail" -> Color(0xFFA9C400)
                                        "payroll" -> AdminModernBlue
                                        else -> AdminModernGreen
                                    }
                                    Surface(shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, AdminModernBorder), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(item.itemName.ifBlank { item.itemCode }, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                                    Text("Client: ${item.description ?: "Master item"}", fontSize = 11.sp, color = AdminModernTextMuted)
                                                    Text("Created: 30th February 2002", fontSize = 11.sp, color = AdminModernTextMuted)
                                                }
                                                ModernBadge(category, accent)
                                            }
                                            Spacer(Modifier.height(12.dp))
                                            HorizontalDivider(color = AdminModernBorder)
                                            Spacer(Modifier.height(10.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Deal Valued At", fontSize = 12.sp, color = Color.Black)
                                                Text(fmtMoney(item.basePrice), fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = AdminModernGreen)
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(categories) { category ->
                                    Surface(shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, AdminModernBorder), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(AdminModernBlue.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Inventory2, contentDescription = null, tint = AdminModernBlue, modifier = Modifier.size(24.dp))
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(category.categoryName, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                                Text("Items: ${items.count { it.categoryId == category.id }}", fontSize = 12.sp, color = AdminModernTextMuted)
                                            }
                                            Surface(shape = RoundedCornerShape(50), color = AdminModernBlue) {
                                                Text("→", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernToggleTab(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(50),
        color = if (selected) AdminModernBlue else Color(0xFFF1F5F9)
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = if (selected) Color.White else Color.Black, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
