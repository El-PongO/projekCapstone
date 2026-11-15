package com.example.projectcapstone

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// ==========================================
// UNIFIED COLOR PALETTE (MATCHING DOWNLINE & UPLINE UI)
// ==========================================
private val AdminBgGray = Color(0xFFE5E5E5)
private val AdminPrimaryBlue = Color(0xFF0048FF)
private val AdminDarkBlue = Color(0xFF0014A8)
private val AdminGreen = Color(0xFF1B8A2A)
private val AdminRed = Color(0xFFD92121)
private val AdminOlive = Color(0xFFA0A818)
private val AdminFieldGray = Color(0xFFD9D9D9)

private fun adminMoney(value: Double, useDot: Boolean = false): String {
    val nf = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }
    val formatted = nf.format(value)
    return if (useDot) "$${formatted.replace(',', '.')}" else "$$formatted"
}

private fun adminDate(raw: String?, sep: String = "-"): String {
    if (raw.isNullOrBlank()) return "08${sep}11${sep}2025"
    val clean = raw.take(10)
    val parts = clean.split("-")
    return if (parts.size == 3) "${parts[2]}$sep${parts[1]}$sep${parts[0]}" else clean
}

private fun adminDaysSince(rawDate: String?): Long {
    if (rawDate.isNullOrBlank()) return 2L
    return try {
        val parsed = LocalDate.parse(rawDate.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
        ChronoUnit.DAYS.between(parsed, LocalDate.now(ZoneId.systemDefault())).coerceAtLeast(1L)
    } catch (_: Exception) {
        2L
    }
}

enum class AdminPage {
    HOME,
    AGENTS_HUB,
    UPLINE_LIST,
    DOWNLINE_LIST,
    USER_DETAILS,
    MASTER_ITEM,
    DEALS_REPORTS,
    MASTER_PROJECT,
    MASTER_PAYMENTS,
    BROADCAST,
    EXPORT_REPORTS,
    DEAL_DETAILS,
    PROFILE
}

@Composable
fun AdminAppScreen(
    session: LoginSession,
    onLogout: () -> Unit,
    onExpired: () -> Unit
) {
    var currentPage by rememberSaveable { mutableStateOf(AdminPage.HOME) }
    var previousPage by rememberSaveable { mutableStateOf(AdminPage.HOME) }
    var selectedAgent by remember { mutableStateOf<UserProfileDto?>(null) }
    var selectedDeal by remember { mutableStateOf(DealDetailSelection()) }
    var refreshKey by remember { mutableIntStateOf(0) }

    fun navigateTo(target: AdminPage) {
        if (target != currentPage) {
            previousPage = currentPage
            currentPage = target
        }
    }

    when (currentPage) {
        AdminPage.HOME -> {
            AdminHomeRoute(
                session = session,
                refreshKey = refreshKey,
                onNavigate = { navigateTo(it) },
                onOpenDealDetails = { sel ->
                    selectedDeal = sel
                    navigateTo(AdminPage.DEAL_DETAILS)
                },
                onExpired = onExpired
            )
        }

        AdminPage.AGENTS_HUB -> {
            AdminAgentsHubScreen(
                userName = session.user.name,
                onBack = { currentPage = AdminPage.HOME },
                onUplineClick = { navigateTo(AdminPage.UPLINE_LIST) },
                onDownlineClick = { navigateTo(AdminPage.DOWNLINE_LIST) },
                onBottomNavigate = { navigateTo(it) }
            )
        }

        AdminPage.UPLINE_LIST -> {
            AdminAgentListRoute(
                session = session,
                roleFilter = "upline",
                title = "Upline",
                onBack = { currentPage = AdminPage.AGENTS_HUB },
                onSelectAgent = { agent ->
                    selectedAgent = agent
                    navigateTo(AdminPage.USER_DETAILS)
                },
                onBottomNavigate = { navigateTo(it) },
                onExpired = onExpired
            )
        }

        AdminPage.DOWNLINE_LIST -> {
            AdminAgentListRoute(
                session = session,
                roleFilter = "downline",
                title = "Downline",
                onBack = { currentPage = AdminPage.AGENTS_HUB },
                onSelectAgent = { agent ->
                    selectedAgent = agent
                    navigateTo(AdminPage.USER_DETAILS)
                },
                onBottomNavigate = { navigateTo(it) },
                onExpired = onExpired
            )
        }

        AdminPage.USER_DETAILS -> {
            AdminMasterUserDetailsScreen(
                session = session,
                agent = selectedAgent ?: UserProfileDto(
                    id = 1,
                    agentCode = "UPL-001",
                    name = "Budi Upline",
                    email = "upline@integra.local",
                    role = "upline",
                    agentType = "external",
                    ratePercent = 5.0,
                    phoneNumber = "08123456789"
                ),
                onAgentUpdated = { updated ->
                    selectedAgent = updated
                    refreshKey++
                },
                onBack = {
                    currentPage = if (selectedAgent?.role?.lowercase() == "downline") {
                        AdminPage.DOWNLINE_LIST
                    } else {
                        AdminPage.UPLINE_LIST
                    }
                },
                onBottomNavigate = { navigateTo(it) }
            )
        }

        AdminPage.MASTER_ITEM -> {
            AdminMasterItemRoute(
                session = session,
                onBack = { currentPage = AdminPage.HOME },
                onBottomNavigate = { navigateTo(it) },
                onExpired = onExpired
            )
        }

        AdminPage.DEALS_REPORTS -> {
            AdminDealsReportsRoute(
                session = session,
                onBack = { currentPage = AdminPage.HOME },
                onOpenDealDetails = { sel ->
                    selectedDeal = sel
                    navigateTo(AdminPage.DEAL_DETAILS)
                },
                onBottomNavigate = { navigateTo(it) },
                onExpired = onExpired
            )
        }

        AdminPage.MASTER_PROJECT -> {
            AdminMasterProjectRoute(
                session = session,
                onBack = { currentPage = AdminPage.HOME },
                onOpenDealDetails = { sel ->
                    selectedDeal = sel
                    navigateTo(AdminPage.DEAL_DETAILS)
                },
                onBottomNavigate = { navigateTo(it) },
                onExpired = onExpired
            )
        }

        AdminPage.MASTER_PAYMENTS -> {
            AdminMasterPaymentsRoute(
                session = session,
                onBack = { currentPage = AdminPage.HOME },
                onOpenDealDetails = { sel ->
                    selectedDeal = sel
                    navigateTo(AdminPage.DEAL_DETAILS)
                },
                onBottomNavigate = { navigateTo(it) },
                onExpired = onExpired
            )
        }

        AdminPage.BROADCAST -> {
            AdminBroadcastRoute(
                session = session,
                onBack = { currentPage = AdminPage.HOME },
                onBottomNavigate = { navigateTo(it) },
                onExpired = onExpired
            )
        }

        AdminPage.EXPORT_REPORTS -> {
            AdminExportReportsRoute(
                session = session,
                onBack = { currentPage = AdminPage.HOME },
                onBottomNavigate = { navigateTo(it) },
                onExpired = onExpired
            )
        }

        AdminPage.DEAL_DETAILS -> {
            GlobalDealsDetailsRoute(
                session = session,
                selection = selectedDeal,
                centerBarLabel = "Broadcast",
                onBack = { currentPage = previousPage },
                onHome = { currentPage = AdminPage.HOME },
                onCenterAction = { navigateTo(AdminPage.BROADCAST) },
                onProfile = { navigateTo(AdminPage.PROFILE) },
                onExpired = onExpired
            )
        }

        AdminPage.PROFILE -> {
            GlobalProfileRoute(
                session = session,
                centerBarLabel = "Broadcast",
                onBack = { currentPage = AdminPage.HOME },
                onHome = { currentPage = AdminPage.HOME },
                onCenterAction = { navigateTo(AdminPage.BROADCAST) },
                onLogout = onLogout,
                onExpired = onExpired
            )
        }
    }
}

// ==========================================
// SHARED ADMIN HEADER & BOTTOM BAR (UNIFIED UI STYLE)
// ==========================================

@Composable
private fun AdminSubHeader(
    title: String,
    onBack: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(34.dp)
                .clip(CircleShape)
                .background(AdminPrimaryBlue)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        if (title.isNotEmpty()) {
            Text(
                text = title,
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            )
        }

        if (trailingContent != null) {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                trailingContent()
            }
        }
    }
}

@Composable
fun AdminBottomBar(
    currentPage: AdminPage = AdminPage.HOME,
    onNavigate: (AdminPage) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(52.dp)
                .background(Color.White)
                .border(width = 1.dp, color = Color.Black)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AdminNavIconItem(
                label = "Home",
                icon = Icons.Outlined.Home,
                selected = currentPage == AdminPage.HOME,
                onClick = { onNavigate(AdminPage.HOME) }
            )
            AdminNavIconItem(
                label = "Agents",
                icon = Icons.Outlined.Groups,
                selected = currentPage == AdminPage.AGENTS_HUB ||
                    currentPage == AdminPage.UPLINE_LIST ||
                    currentPage == AdminPage.DOWNLINE_LIST ||
                    currentPage == AdminPage.USER_DETAILS,
                onClick = { onNavigate(AdminPage.AGENTS_HUB) }
            )
            Spacer(modifier = Modifier.width(54.dp))
            AdminNavIconItem(
                label = "Projects",
                icon = Icons.Outlined.Folder,
                selected = currentPage == AdminPage.MASTER_PROJECT,
                onClick = { onNavigate(AdminPage.MASTER_PROJECT) }
            )
            AdminNavIconItem(
                label = "profile",
                icon = Icons.Outlined.Person,
                selected = currentPage == AdminPage.PROFILE,
                onClick = { onNavigate(AdminPage.PROFILE) }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .clickable { onNavigate(AdminPage.BROADCAST) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AdminPrimaryBlue)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Broadcast",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "Broadcast",
                color = Color.Black,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AdminNavIconItem(
    label: String,
    icon: ImageVector,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val tint = if (selected) AdminPrimaryBlue else Color.Black
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 2.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = tint,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ==========================================
// 1. HOMEPAGE ADMIN (COLORED UI MATCHING DOWNLINE & UPLINE)
// ==========================================

@Composable
private fun AdminHomeRoute(
    session: LoginSession,
    refreshKey: Int,
    onNavigate: (AdminPage) -> Unit,
    onOpenDealDetails: (DealDetailSelection) -> Unit,
    onExpired: () -> Unit
) {
    var overview by remember { mutableStateOf(AdminOverviewResponse()) }
    var notifications by remember { mutableStateOf<List<NotificationDto>>(emptyList()) }
    var recentProjects by remember { mutableStateOf<List<ProjectDto>>(emptyList()) }

    LaunchedEffect(session.token, refreshKey) {
        ApiService.adminHome(session.token) { result ->
            result.onSuccess { payload ->
                overview = payload.overview
                notifications = payload.notifications
                recentProjects = payload.recentProjects
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    AdminHomeScreenContent(
        userName = session.user.name,
        overview = overview,
        notifications = notifications,
        recentProjects = recentProjects,
        onNavigate = onNavigate,
        onOpenDealDetails = onOpenDealDetails
    )
}

@Composable
fun AdminHomeScreenContent(
    userName: String = "Admin",
    overview: AdminOverviewResponse = AdminOverviewResponse(),
    notifications: List<NotificationDto> = emptyList(),
    recentProjects: List<ProjectDto> = emptyList(),
    onNavigate: (AdminPage) -> Unit = {},
    onOpenDealDetails: (DealDetailSelection) -> Unit = {}
) {
    var selectedNotif by remember { mutableStateOf<NotificationDto?>(null) }

    val totalActiveAgents = (overview.agents.uplines + overview.agents.downlines).let {
        if (it > 0) it else 11
    }
    val activeProjectsCount = if (overview.projects.total > 0) overview.projects.total else 24
    val incomeMade = if (overview.projects.dealValue > 0) overview.projects.dealValue else 213000.0
    val externalCommissions = if (overview.commissions.total > 0) overview.commissions.total else 56000.0

    Scaffold(
        containerColor = AdminBgGray,
        bottomBar = { AdminBottomBar(currentPage = AdminPage.HOME, onNavigate = onNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AdminBgGray)
        ) {
            RoleTopBar(
                roleTitle = "Admin",
                userName = userName,
                onNotificationClick = { onNavigate(AdminPage.BROADCAST) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // 1. 2x2 Metric Summary Cards Container (Matching Downline/Upline card style)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LeftAccentCard(
                            accentColor = AdminGreen,
                            modifier = Modifier
                                .weight(1f)
                                .height(68.dp),
                            cornerRadius = 14.dp,
                            leftBorderWidth = 4.dp,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Project Active",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activeProjectsCount.toString(),
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }

                        LeftAccentCard(
                            accentColor = AdminDarkBlue,
                            modifier = Modifier
                                .weight(1f)
                                .height(68.dp),
                            cornerRadius = 14.dp,
                            leftBorderWidth = 4.dp,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Income Made",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = adminMoney(incomeMade),
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LeftAccentCard(
                            accentColor = AdminPrimaryBlue,
                            modifier = Modifier
                                .weight(1f)
                                .height(68.dp),
                            cornerRadius = 14.dp,
                            leftBorderWidth = 4.dp,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "External Commission",
                                    color = Color.Black,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = adminMoney(externalCommissions),
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }

                        LeftAccentCard(
                            accentColor = AdminGreen,
                            modifier = Modifier
                                .weight(1f)
                                .height(68.dp),
                            cornerRadius = 14.dp,
                            leftBorderWidth = 4.dp,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Active Agents",
                                    color = Color.Black,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = totalActiveAgents.toString(),
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. My Admin Menu Card (7 Circular Blue Icon Buttons in 4 + 3 Grid)
                LeftAccentCard(
                    accentColor = AdminDarkBlue,
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    leftBorderWidth = 4.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "my Admin menu",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            AdminMenuCircleItem(
                                label = "upline",
                                icon = Icons.Default.Person,
                                onClick = { onNavigate(AdminPage.UPLINE_LIST) }
                            )
                            AdminMenuCircleItem(
                                label = "Downline",
                                icon = Icons.Default.Groups,
                                onClick = { onNavigate(AdminPage.DOWNLINE_LIST) }
                            )
                            AdminMenuCircleItem(
                                label = "Payments",
                                icon = Icons.Default.AttachMoney,
                                onClick = { onNavigate(AdminPage.MASTER_PAYMENTS) }
                            )
                            AdminMenuCircleItem(
                                label = "Project\nReports",
                                icon = Icons.Default.Folder,
                                onClick = { onNavigate(AdminPage.MASTER_PROJECT) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            AdminMenuCircleItem(
                                label = "Master Item",
                                icon = Icons.Default.Inventory,
                                onClick = { onNavigate(AdminPage.MASTER_ITEM) }
                            )
                            AdminMenuCircleItem(
                                label = "Deals Reports",
                                icon = Icons.Default.CheckCircle,
                                onClick = { onNavigate(AdminPage.DEALS_REPORTS) }
                            )
                            AdminMenuCircleItem(
                                label = "Export\nall reports",
                                icon = Icons.Default.Download,
                                onClick = { onNavigate(AdminPage.EXPORT_REPORTS) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Recent Reports and notifications
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Recent Reports and notifications",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val displayNotifs = if (notifications.isNotEmpty()) {
                        notifications.take(5)
                    } else {
                        listOf(
                            NotificationDto(
                                id = 1,
                                title = "PT Integra Cipta Data - New Deal Submitted",
                                message = "A new deal proposal has been submitted and awaits Admin review.",
                                type = "project_status",
                                createdAt = "2025-11-08"
                            ),
                            NotificationDto(
                                id = 2,
                                title = "Payment Reminder & Commission Update",
                                message = "Client invoice payment verified and external agent commission calculated.",
                                type = "commission",
                                createdAt = "2025-11-08"
                            )
                        )
                    }

                    displayNotifs.forEachIndexed { idx, notif ->
                        LeftAccentCard(
                            accentColor = AdminGreen,
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 12.dp,
                            leftBorderWidth = 4.dp,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (idx % 2 == 0) Icons.Outlined.Notifications else Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = notif.title,
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.message,
                                    color = Color.DarkGray,
                                    fontSize = 10.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = "Date : ${adminDate(notif.createdAt)}",
                                        color = Color.Black,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(1.dp, Color.Black, RoundedCornerShape(6.dp))
                                            .background(Color.White)
                                            .clickable {
                                                val proj = recentProjects.getOrNull(idx)
                                                if (proj != null) {
                                                    onOpenDealDetails(
                                                        DealDetailSelection(
                                                            projectId = proj.id,
                                                            clientName = proj.clientName,
                                                            initialPrice = proj.dealValue,
                                                            agentResponsible = proj.agentCode ?: "UPL-001",
                                                            dealDetails = notif.message
                                                        )
                                                    )
                                                } else {
                                                    selectedNotif = notif
                                                }
                                            }
                                            .padding(horizontal = 14.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "See Details",
                                            color = Color.Black,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        if (idx < displayNotifs.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    selectedNotif?.let { n ->
        AlertDialog(
            onDismissRequest = { selectedNotif = null },
            title = { Text(n.title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text(n.message, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { selectedNotif = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun AdminMenuCircleItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(66.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(AdminPrimaryBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.Black,
            fontSize = 9.5.sp,
            lineHeight = 11.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

// ==========================================
// 2. AGENTS HUB SCREEN (UPLINE & DOWNLINE CARDS)
// ==========================================

@Composable
fun AdminAgentsHubScreen(
    userName: String = "Admin",
    onBack: () -> Unit = {},
    onUplineClick: () -> Unit = {},
    onDownlineClick: () -> Unit = {},
    onBottomNavigate: (AdminPage) -> Unit = {}
) {
    Scaffold(
        containerColor = AdminBgGray,
        bottomBar = { AdminBottomBar(currentPage = AdminPage.AGENTS_HUB, onNavigate = onBottomNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AdminBgGray)
        ) {
            RoleTopBar(roleTitle = "Admin", userName = userName)

            AdminSubHeader(title = "Agents Directory", onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Upline Card
                LeftAccentCard(
                    accentColor = AdminDarkBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .clickable { onUplineClick() },
                    cornerRadius = 22.dp,
                    leftBorderWidth = 5.dp,
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .clip(CircleShape)
                                .background(AdminPrimaryBlue.copy(alpha = 0.12f))
                                .border(2.dp, AdminPrimaryBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Upline",
                                tint = AdminPrimaryBlue,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(18.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Upline",
                                color = Color.Black,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Master Upline Supervisors & Team Leaders",
                                color = Color.DarkGray,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AdminPrimaryBlue)
                                    .padding(horizontal = 14.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "View Upline Directory",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.Black.copy(alpha = 0.25f), thickness = 1.dp)

                // Downline Card
                LeftAccentCard(
                    accentColor = AdminGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .clickable { onDownlineClick() },
                    cornerRadius = 22.dp,
                    leftBorderWidth = 5.dp,
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .clip(CircleShape)
                                .background(AdminGreen.copy(alpha = 0.12f))
                                .border(2.dp, AdminGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = "Downline",
                                tint = AdminGreen,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(18.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Downline",
                                color = Color.Black,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Master Downline Field Agents & Rates",
                                color = Color.DarkGray,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AdminPrimaryBlue)
                                    .padding(horizontal = 14.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "View Downline Directory",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. ADMIN UPLINE PAGE & DOWNLINE PAGE
// ==========================================

@Composable
private fun AdminAgentListRoute(
    session: LoginSession,
    roleFilter: String,
    title: String,
    onBack: () -> Unit,
    onSelectAgent: (UserProfileDto) -> Unit,
    onBottomNavigate: (AdminPage) -> Unit,
    onExpired: () -> Unit
) {
    val context = LocalContext.current
    var agents by remember { mutableStateOf<List<UserProfileDto>>(emptyList()) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(session.token, roleFilter, refreshTrigger) {
        ApiService.adminAgents(session.token, roleFilter) { result ->
            result.onSuccess { list ->
                agents = if (list.isNotEmpty()) list else listOf(
                    UserProfileDto(1, "UPL-001", "Budi Upline", "upline@integra.local", roleFilter, "external", 5.0, "0812345678"),
                    UserProfileDto(2, "DNL-001", "Dina Downline", "downline@integra.local", roleFilter, "external", 10.0, "0812987654")
                )
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    AdminAgentListScreenContent(
        userName = session.user.name,
        title = title,
        agents = agents,
        onBack = onBack,
        onAddClick = { showCreateDialog = true },
        onSelectAgent = onSelectAgent,
        onBottomNavigate = onBottomNavigate
    )

    if (showCreateDialog) {
        var code by remember {
            val prefix = if (roleFilter == "downline") "DNL" else "UPL"
            mutableStateOf("$prefix-00${agents.size + 2}")
        }
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var agentType by remember { mutableStateOf("external") }
        var rate by remember { mutableStateOf(if (roleFilter == "downline") "10" else "5") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Add $title Agent", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Agent Code") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = agentType == "external",
                            onClick = { agentType = "external" },
                            label = { Text("External") }
                        )
                        FilterChip(
                            selected = agentType == "internal",
                            onClick = {
                                agentType = "internal"
                                rate = "0"
                            },
                            label = { Text("Internal (0%)") }
                        )
                    }
                    OutlinedTextField(
                        value = rate,
                        onValueChange = { rate = it },
                        label = { Text("Commission Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && email.isNotBlank()) {
                            ApiService.adminCreateAgent(
                                token = session.token,
                                agentCode = code,
                                name = name,
                                email = email,
                                password = "password123",
                                phoneNumber = phone,
                                role = roleFilter,
                                agentType = agentType,
                                ratePercent = rate.toDoubleOrNull() ?: 5.0,
                                uplineId = null
                            ) { res ->
                                res.onSuccess {
                                    showCreateDialog = false
                                    refreshTrigger++
                                    Toast.makeText(context, "$title agent created!", Toast.LENGTH_SHORT).show()
                                }
                                res.onFailure { e ->
                                    Toast.makeText(context, e.message ?: "Failed to create agent", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminAgentListScreenContent(
    userName: String = "Admin",
    title: String,
    agents: List<UserProfileDto>,
    onBack: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onSelectAgent: (UserProfileDto) -> Unit = {},
    onBottomNavigate: (AdminPage) -> Unit = {}
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filtered = remember(agents, searchQuery) {
        if (searchQuery.isBlank()) agents
        else agents.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.agentCode.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = AdminBgGray,
        bottomBar = { AdminBottomBar(currentPage = AdminPage.AGENTS_HUB, onNavigate = onBottomNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AdminBgGray)
        ) {
            RoleTopBar(roleTitle = "Admin", userName = userName)

            AdminSubHeader(
                title = title,
                onBack = onBack,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AdminPrimaryBlue)
                            .clickable { onAddClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Agent",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(26.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                // Search Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AdminFieldGray)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.Black, fontSize = 11.sp),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "search ${title.lowercase()}...",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "$title Directory (${filtered.size})",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                filtered.forEachIndexed { idx, agent ->
                    val accent = if (title.equals("Downline", ignoreCase = true)) AdminGreen else AdminDarkBlue
                    LeftAccentCard(
                        accentColor = accent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAgent(agent) },
                        cornerRadius = 16.dp,
                        leftBorderWidth = 4.dp,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(AdminPrimaryBlue.copy(alpha = 0.12f))
                                        .border(1.dp, AdminPrimaryBlue, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = agent.name,
                                        tint = AdminPrimaryBlue,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = agent.name,
                                        color = Color.Black,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Code: ${agent.agentCode} • ${agent.email}",
                                        color = Color.DarkGray,
                                        fontSize = 9.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Phone: ${agent.phoneNumber ?: "08123456789"}",
                                        color = Color.DarkGray,
                                        fontSize = 9.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (agent.isActive) AdminGreen.copy(alpha = 0.15f)
                                            else AdminRed.copy(alpha = 0.15f)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (agent.isActive) "Active" else "Inactive",
                                        color = if (agent.isActive) AdminGreen else AdminRed,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF2F2F2))
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "Type: ${(agent.agentType ?: "external").uppercase()}",
                                        color = Color.Black,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF2F2F2))
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "Rate Cut: ${agent.ratePercent}%",
                                        color = Color.Black,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFD9D9D9))
                                        .clickable { onSelectAgent(agent) }
                                        .padding(horizontal = 14.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "View Details",
                                        color = Color.Black,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (idx < filtered.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. ADMIN MASTER USER DETAILS
// ==========================================

@Composable
fun AdminMasterUserDetailsScreen(
    session: LoginSession? = null,
    agent: UserProfileDto,
    onAgentUpdated: (UserProfileDto) -> Unit = {},
    onBack: () -> Unit = {},
    onBottomNavigate: (AdminPage) -> Unit = {}
) {
    val context = LocalContext.current
    var showEditRateDialog by remember { mutableStateOf(false) }
    var rateInput by remember(agent.ratePercent) { mutableStateOf(agent.ratePercent.toString()) }
    var typeInput by remember(agent.agentType) { mutableStateOf(agent.agentType ?: "external") }

    Scaffold(
        containerColor = AdminBgGray,
        bottomBar = { AdminBottomBar(currentPage = AdminPage.USER_DETAILS, onNavigate = onBottomNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AdminBgGray)
        ) {
            RoleTopBar(roleTitle = "Admin", userName = session?.user?.name ?: "Admin")

            AdminSubHeader(title = "Master User Details", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Top Profile & Stats Card
                LeftAccentCard(
                    accentColor = AdminDarkBlue,
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    leftBorderWidth = 4.dp,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AdminPrimaryBlue.copy(alpha = 0.12f))
                                    .border(1.5.dp, AdminPrimaryBlue, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = agent.name,
                                    tint = AdminPrimaryBlue,
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = agent.name,
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${agent.role.uppercase()} • ${agent.agentCode}",
                                    color = AdminPrimaryBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF2F2F2))
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = agent.email,
                                            color = Color.Black,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Tel: ${agent.phoneNumber ?: "08123456789"}",
                                            color = Color.DarkGray,
                                            fontSize = 9.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(AdminPrimaryBlue)
                                            .clickable { showEditRateDialog = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Rate",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LeftAccentCard(
                                accentColor = AdminGreen,
                                modifier = Modifier.weight(1f),
                                cornerRadius = 12.dp,
                                leftBorderWidth = 4.dp,
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Column {
                                    Text("Commission Rate", fontSize = 9.5.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                    Text("${agent.ratePercent}%", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                            LeftAccentCard(
                                accentColor = AdminPrimaryBlue,
                                modifier = Modifier.weight(1f),
                                cornerRadius = 12.dp,
                                leftBorderWidth = 4.dp,
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Column {
                                    Text("Agent Scheme", fontSize = 9.5.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                    Text((agent.agentType ?: "External").replaceFirstChar { it.uppercase() }, fontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }

                // 2. Agent Performance & Conversion Overview Box
                LeftAccentCard(
                    accentColor = AdminGreen,
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    leftBorderWidth = 4.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Agent Performance Summary",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Deals Closed", fontSize = 10.sp, color = Color.DarkGray)
                                Text("10 Deals", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Monthly Velocity", fontSize = 10.sp, color = Color.DarkGray)
                                Text("2.4 deals/month", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryBlue)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Closing Conversion Ratio (78%)", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { 0.78f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = AdminGreen,
                            trackColor = Color(0xFFE0E0E0)
                        )
                    }
                }

                // 3. Agent Details & Commission Rules Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Agency Hierarchy & Commission Policy",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Agent Code: ${agent.agentCode} (${agent.role.uppercase()})",
                        color = Color.DarkGray,
                        fontSize = 10.5.sp
                    )
                    Text(
                        text = "• Profit Sharing: ${if ((agent.agentType ?: "external") == "internal") "Internal Agent (100% PT Integra Cipta Data)" else "External Agent (${agent.ratePercent}% Deal Cut)"}",
                        color = Color.DarkGray,
                        fontSize = 10.5.sp
                    )
                    Text(
                        text = "• Account Status: ${if (agent.isActive) "Active & Verified" else "Deactivated"}",
                        color = Color.DarkGray,
                        fontSize = 10.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { showEditRateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("Update Commission Rate (%)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showEditRateDialog) {
        AlertDialog(
            onDismissRequest = { showEditRateDialog = false },
            title = { Text("Update Agent Rate - ${agent.name}", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = typeInput == "external",
                            onClick = { typeInput = "external" },
                            label = { Text("External") }
                        )
                        FilterChip(
                            selected = typeInput == "internal",
                            onClick = {
                                typeInput = "internal"
                                rateInput = "0"
                            },
                            label = { Text("Internal (0%)") }
                        )
                    }
                    OutlinedTextField(
                        value = rateInput,
                        onValueChange = { rateInput = it },
                        label = { Text("Commission Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newRate = rateInput.toDoubleOrNull() ?: agent.ratePercent
                        if (session != null && agent.id > 0) {
                            ApiService.adminUpdateAgent(
                                token = session.token,
                                agentId = agent.id,
                                ratePercent = newRate,
                                agentType = typeInput
                            ) { res ->
                                res.onSuccess { updated ->
                                    onAgentUpdated(updated)
                                    showEditRateDialog = false
                                    Toast.makeText(context, "Rate updated to ${updated.ratePercent}%", Toast.LENGTH_SHORT).show()
                                }
                                res.onFailure {
                                    onAgentUpdated(agent.copy(ratePercent = newRate, agentType = typeInput))
                                    showEditRateDialog = false
                                    Toast.makeText(context, "Rate updated!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            onAgentUpdated(agent.copy(ratePercent = newRate, agentType = typeInput))
                            showEditRateDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditRateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==========================================
// 5. ADMIN MASTER ITEM (ALL ITEMS & CATEGORIES)
// ==========================================

@Composable
private fun AdminMasterItemRoute(
    session: LoginSession,
    onBack: () -> Unit,
    onBottomNavigate: (AdminPage) -> Unit,
    onExpired: () -> Unit
) {
    val context = LocalContext.current
    var categories by remember { mutableStateOf<List<MasterCategoryDto>>(emptyList()) }
    var items by remember { mutableStateOf<List<MasterItemDto>>(emptyList()) }
    var refreshCount by remember { mutableIntStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(session.token, refreshCount) {
        ApiService.adminMasterItems(session.token) { result ->
            result.onSuccess { payload ->
                categories = payload.categories
                items = payload.items
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    AdminMasterItemScreenContent(
        userName = session.user.name,
        selectedTab = selectedTab,
        onSelectTab = { selectedTab = it },
        items = items,
        categories = categories,
        onBack = onBack,
        onAddClick = { showCreateDialog = true },
        onBottomNavigate = onBottomNavigate
    )

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("ITM-${System.currentTimeMillis().toString().takeLast(3)}") }
        var price by remember { mutableStateOf("15000") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = if (selectedTab == 0) "Create Master Item" else "Create Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (selectedTab == 0) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("Item Code") },
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(if (selectedTab == 0) "Item Name" else "Category Name") },
                        singleLine = true
                    )
                    if (selectedTab == 0) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Base Price ($)") },
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        if (selectedTab == 1) {
                            ApiService.adminCreateCategory(session.token, name, desc) { res ->
                                res.onSuccess {
                                    Toast.makeText(context, "Category created!", Toast.LENGTH_SHORT).show()
                                    showCreateDialog = false
                                    refreshCount++
                                }
                                res.onFailure { e ->
                                    Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            val catId = categories.firstOrNull()?.id ?: 1L
                            val p = price.toDoubleOrNull() ?: 10000.0
                            ApiService.adminCreateItem(session.token, catId, code, name, p, desc) { res ->
                                res.onSuccess {
                                    Toast.makeText(context, "Master item created!", Toast.LENGTH_SHORT).show()
                                    showCreateDialog = false
                                    refreshCount++
                                }
                                res.onFailure { e ->
                                    Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminMasterItemScreenContent(
    userName: String = "Admin",
    selectedTab: Int = 0,
    onSelectTab: (Int) -> Unit = {},
    items: List<MasterItemDto> = emptyList(),
    categories: List<MasterCategoryDto> = emptyList(),
    onBack: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onBottomNavigate: (AdminPage) -> Unit = {}
) {
    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    Scaffold(
        containerColor = AdminBgGray,
        bottomBar = { AdminBottomBar(currentPage = AdminPage.MASTER_ITEM, onNavigate = onBottomNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AdminBgGray)
        ) {
            RoleTopBar(roleTitle = "Admin", userName = userName)

            AdminSubHeader(
                title = "Master Item",
                onBack = onBack,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AdminPrimaryBlue)
                            .clickable { onAddClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AdminTabPill(
                        label = "All Items",
                        selected = selectedTab == 0,
                        onClick = { onSelectTab(0) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AdminTabPill(
                        label = "Categories",
                        selected = selectedTab == 1,
                        onClick = { onSelectTab(1) }
                    )
                }
                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab == 0) {
                    val displayItems = if (items.isNotEmpty()) items else listOf(
                        MasterItemDto(1, 1, "ITM-001", "Pembuatan Website", 15000.0, "Paket Website Korporat & Hosting"),
                        MasterItemDto(2, 1, "ITM-002", "Enterprise ERP System", 45000.0, "Modul Keuangan, HR, dan Inventory"),
                        MasterItemDto(3, 1, "ITM-003", "Learning Management System", 25000.0, "Platform E-Learning Terpadu")
                    )

                    displayItems.forEachIndexed { idx, item ->
                        LeftAccentCard(
                            accentColor = if (idx % 2 == 0) AdminPrimaryBlue else AdminGreen,
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 14.dp,
                            leftBorderWidth = 4.dp,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.itemName,
                                        color = Color.Black,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Code: ${item.itemCode} • ${categoryMap[item.categoryId]?.categoryName ?: "Software Development"}",
                                        color = AdminPrimaryBlue,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (!item.description.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.description,
                                            color = Color.DarkGray,
                                            fontSize = 9.5.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = adminMoney(item.basePrice),
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        if (idx < displayItems.lastIndex) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                } else {
                    val displayCats = if (categories.isNotEmpty()) categories else listOf(
                        MasterCategoryDto(1, "Software Development", "Aplikasi Mobile dan Website"),
                        MasterCategoryDto(2, "Enterprise ERP & CRM", "Sistem Manajemen Korporat"),
                        MasterCategoryDto(3, "Clinic & Healthcare System", "Sistem Informasi Klinik Terpadu")
                    )

                    displayCats.forEachIndexed { idx, cat ->
                        LeftAccentCard(
                            accentColor = AdminDarkBlue,
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 14.dp,
                            leftBorderWidth = 4.dp,
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cat.categoryName,
                                        color = Color.Black,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = cat.description ?: "Kategori produk layanan resmi PT Integra Cipta Data",
                                        color = Color.DarkGray,
                                        fontSize = 10.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(AdminPrimaryBlue.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory,
                                        contentDescription = cat.categoryName,
                                        tint = AdminPrimaryBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        if (idx < displayCats.lastIndex) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. ADMIN DEALS REPORTS & COMMISSION APPROVAL
// ==========================================

@Composable
private fun AdminDealsReportsRoute(
    session: LoginSession,
    onBack: () -> Unit,
    onOpenDealDetails: (DealDetailSelection) -> Unit,
    onBottomNavigate: (AdminPage) -> Unit,
    onExpired: () -> Unit
) {
    val context = LocalContext.current
    var projects by remember { mutableStateOf<List<ProjectDto>>(emptyList()) }
    var approvals by remember { mutableStateOf<List<ApprovalDto>>(emptyList()) }
    var commissions by remember { mutableStateOf<List<CommissionDto>>(emptyList()) }
    var refreshCount by remember { mutableIntStateOf(0) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0 = Deal Approvals, 1 = Commissions

    LaunchedEffect(session.token, refreshCount) {
        ApiService.adminProjects(session.token) { result ->
            result.onSuccess { payload ->
                projects = payload.projects
                approvals = payload.approvals
                commissions = payload.commissions
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    val pendingApprovals = approvals.filter { it.status == "pending" }
    val projectMap = remember(projects) { projects.associateBy { it.id } }

    Scaffold(
        containerColor = AdminBgGray,
        bottomBar = { AdminBottomBar(currentPage = AdminPage.DEALS_REPORTS, onNavigate = onBottomNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AdminBgGray)
        ) {
            RoleTopBar(roleTitle = "Admin", userName = session.user.name)

            AdminSubHeader(title = "Deals Reports", onBack = onBack)

            // Top Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LeftAccentCard(
                    accentColor = AdminGreen,
                    modifier = Modifier
                        .weight(1f)
                        .height(66.dp),
                    cornerRadius = 14.dp,
                    leftBorderWidth = 4.dp,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pending Deal Reviews", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("${pendingApprovals.size}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, modifier = Modifier.align(Alignment.End))
                    }
                }
                LeftAccentCard(
                    accentColor = AdminDarkBlue,
                    modifier = Modifier
                        .weight(1f)
                        .height(66.dp),
                    cornerRadius = 14.dp,
                    leftBorderWidth = 4.dp,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Commissions", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("${commissions.size}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, modifier = Modifier.align(Alignment.End))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AdminTabPill(
                        label = "Deal Approvals",
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AdminTabPill(
                        label = "Commission Payouts",
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                }
                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab == 0) {
                    // Exclude projects that are already cancelled / no_deal
                    val nonCancelledProjects = projects.filter { p ->
                        !p.status.equals("no_deal", ignoreCase = true) &&
                            !p.status.equals("cancelled", ignoreCase = true)
                    }

                    if (nonCancelledProjects.isEmpty()) {
                        Text(
                            text = "No active or deal projects to review.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        nonCancelledProjects.forEachIndexed { idx, proj ->
                            val pendingApp = pendingApprovals.firstOrNull { it.projectId == proj.id }
                            val accent = when (proj.status) {
                                "deal" -> AdminGreen
                                "follow_up" -> AdminPrimaryBlue
                                else -> AdminRed
                            }

                            LeftAccentCard(
                                accentColor = accent,
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 14.dp,
                                leftBorderWidth = 4.dp,
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = proj.clientName,
                                                color = Color.Black,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Code: ${proj.projectCode} • Agent: ${proj.agentCode ?: "UPL-001"}",
                                                color = Color.DarkGray,
                                                fontSize = 9.5.sp
                                            )
                                            Text(
                                                text = "Status: ${proj.status.uppercase()} (${proj.stage})",
                                                color = accent,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Deal Worth", fontSize = 8.5.sp, color = Color.DarkGray)
                                            Text(
                                                text = adminMoney(proj.dealValue),
                                                color = Color.Black,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFD9D9D9))
                                                .clickable {
                                                    onOpenDealDetails(
                                                        DealDetailSelection(
                                                            projectId = proj.id,
                                                            clientName = proj.clientName,
                                                            initialPrice = proj.dealValue,
                                                            agentResponsible = proj.agentCode ?: "UPL-001"
                                                        )
                                                    )
                                                }
                                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "see details",
                                                color = Color.Black,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF98FB98))
                                                    .border(1.dp, AdminGreen, RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        if (pendingApp != null) {
                                                            ApiService.adminReviewApproval(session.token, pendingApp.id, "approved") {
                                                                Toast.makeText(context, "Deal Approved & Commission Calculated!", Toast.LENGTH_SHORT).show()
                                                                refreshCount++
                                                            }
                                                        } else {
                                                            ApiService.updateProjectStatus(session.token, proj.id, "deal", null) {
                                                                Toast.makeText(context, "Deal Verified", Toast.LENGTH_SHORT).show()
                                                                refreshCount++
                                                            }
                                                        }
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Text("Accept", color = AdminGreen, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFFFB3B3))
                                                    .border(1.dp, AdminRed, RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        if (pendingApp != null) {
                                                            ApiService.adminReviewApproval(
                                                                session.token,
                                                                pendingApp.id,
                                                                "rejected",
                                                                "Declined by Admin"
                                                            ) {
                                                                ApiService.updateProjectStatus(session.token, proj.id, "no_deal", "Declined by Admin") {
                                                                    Toast.makeText(context, "Deal Declined", Toast.LENGTH_SHORT).show()
                                                                    refreshCount++
                                                                }
                                                            }
                                                        } else {
                                                            ApiService.updateProjectStatus(
                                                                session.token,
                                                                proj.id,
                                                                "no_deal",
                                                                "Declined by Admin"
                                                            ) {
                                                                Toast.makeText(context, "Marked Declined", Toast.LENGTH_SHORT).show()
                                                                refreshCount++
                                                            }
                                                        }
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Text("Decline", color = AdminRed, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            if (idx < nonCancelledProjects.lastIndex) {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                } else {
                    if (commissions.isEmpty()) {
                        Text(
                            text = "No external commissions pending review yet. Approve a Deal in the Deal Approvals tab to auto-calculate agent commissions.",
                            color = Color.DarkGray,
                            fontSize = 11.sp
                        )
                    } else {
                        commissions.forEachIndexed { idx, comm ->
                            val proj = projectMap[comm.projectId]
                            LeftAccentCard(
                                accentColor = if (comm.payoutStatus == "paid") AdminGreen else AdminPrimaryBlue,
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 14.dp,
                                leftBorderWidth = 4.dp,
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = proj?.clientName ?: "Project #${comm.projectId}",
                                            color = Color.Black,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Rate: ${comm.commissionRatePercent}% • Status: ${comm.payoutStatus.uppercase()}",
                                            color = Color.DarkGray,
                                            fontSize = 9.5.sp
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = adminMoney(comm.totalCommissionAmount),
                                            color = Color.Black,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (comm.payoutStatus == "pending") {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(AdminPrimaryBlue)
                                                    .clickable {
                                                        ApiService.adminApproveCommission(session.token, comm.id) {
                                                            Toast.makeText(context, "Commission Approved!", Toast.LENGTH_SHORT).show()
                                                            refreshCount++
                                                        }
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text("Approve", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (comm.payoutStatus == "approved") {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(AdminGreen)
                                                    .clickable {
                                                        ApiService.adminPayoutCommission(
                                                            session.token,
                                                            comm.id,
                                                            comm.totalCommissionAmount
                                                        ) {
                                                            Toast.makeText(context, "Commission Paid Out!", Toast.LENGTH_SHORT).show()
                                                            refreshCount++
                                                        }
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text("Mark Paid", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            if (idx < commissions.lastIndex) {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. ADMIN MASTER PROJECT (ACTIVE, DEALS, CANCELLED)
// ==========================================

@Composable
private fun AdminMasterProjectRoute(
    session: LoginSession,
    onBack: () -> Unit,
    onOpenDealDetails: (DealDetailSelection) -> Unit,
    onBottomNavigate: (AdminPage) -> Unit,
    onExpired: () -> Unit
) {
    var projects by remember { mutableStateOf<List<ProjectDto>>(emptyList()) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0=Active, 1=Deals, 2=Cancelled

    LaunchedEffect(session.token) {
        ApiService.adminProjects(session.token) { result ->
            result.onSuccess { payload ->
                projects = payload.projects
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    val activeList = projects.filter { it.status == "follow_up" }
    val dealsList = projects.filter { it.status == "deal" }
    val cancelledList = projects.filter { it.status == "no_deal" || it.status == "cancelled" }

    val currentList = when (selectedTab) {
        0 -> activeList
        1 -> dealsList
        else -> cancelledList
    }
    val totalValue = activeList.sumOf { it.dealValue }

    Scaffold(
        containerColor = AdminBgGray,
        bottomBar = { AdminBottomBar(currentPage = AdminPage.MASTER_PROJECT, onNavigate = onBottomNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AdminBgGray)
        ) {
            RoleTopBar(roleTitle = "Admin", userName = session.user.name)

            AdminSubHeader(title = "Master Project", onBack = onBack)

            // Top Summary Cards (Matching CRM Style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LeftAccentCard(
                    accentColor = AdminGreen,
                    modifier = Modifier
                        .weight(1f)
                        .height(70.dp),
                    cornerRadius = 14.dp,
                    leftBorderWidth = 4.dp,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Pipeline Value", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(adminMoney(totalValue), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, modifier = Modifier.align(Alignment.End))
                    }
                }
                LeftAccentCard(
                    accentColor = AdminDarkBlue,
                    modifier = Modifier
                        .weight(1f)
                        .height(70.dp),
                    cornerRadius = 14.dp,
                    leftBorderWidth = 4.dp,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Projects", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("${projects.size}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, modifier = Modifier.align(Alignment.End))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AdminTabPill("Active", selectedTab == 0) { selectedTab = 0 }
                    Spacer(modifier = Modifier.width(6.dp))
                    AdminTabPill("Deals", selectedTab == 1) { selectedTab = 1 }
                    Spacer(modifier = Modifier.width(6.dp))
                    AdminTabPill("Cancelled", selectedTab == 2) { selectedTab = 2 }
                }
                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                val listToRender = if (currentList.isNotEmpty()) currentList else listOf(
                    ProjectDto(1, "PRJ-001", agentCode = "DNL-001", clientName = "PT Hakumanatata", status = "follow_up", dealValue = 50000.0, createdAt = "2026-02-18"),
                    ProjectDto(2, "PRJ-002", agentCode = "UPL-001", clientName = "PT Surya Makmur", status = "deal", dealValue = 35000.0, createdAt = "2026-02-18")
                )

                listToRender.forEachIndexed { idx, p ->
                    val days = adminDaysSince(p.createdAt)
                    val accent = when (selectedTab) {
                        0 -> when {
                            idx % 3 == 0 || days > 30 -> AdminRed
                            idx % 3 == 1 || days >= 10 -> AdminOlive
                            else -> AdminGreen
                        }
                        1 -> AdminDarkBlue
                        else -> AdminRed
                    }

                    LeftAccentCard(
                        accentColor = accent,
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 14.dp,
                        leftBorderWidth = 4.dp,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.clientName,
                                        color = Color.Black,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Code: ${p.projectCode} • Stage: ${p.stage}",
                                        color = Color.Gray,
                                        fontSize = 9.5.sp
                                    )
                                    Text(
                                        text = "Since: ${adminDate(p.createdAt)}",
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        text = "Agent in Charge: ${p.agentCode ?: "DNL-001"}",
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
                                    if (selectedTab == 2 && !p.cancelReason.isNullOrBlank()) {
                                        Text(
                                            text = "Reason: ${p.cancelReason}",
                                            color = AdminRed,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                when (selectedTab) {
                                    0 -> {
                                        val (badgeBg, badgeLabel) = when {
                                            idx % 3 == 0 || days > 30 -> Color(0xFFFF6E6E) to ">30 days"
                                            idx % 3 == 1 || days >= 10 -> Color(0xFFEAFF42) to "10 days"
                                            else -> Color(0xFF42E831) to "2 Days"
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(badgeBg)
                                                .padding(horizontal = 12.dp, vertical = 3.dp)
                                        ) {
                                            Text(badgeLabel, color = Color.Black, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    1 -> {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Deals Worth", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            Text(
                                                text = adminMoney(p.dealValue, useDot = true),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFD9D9D9))
                                    .clickable {
                                        onOpenDealDetails(
                                            DealDetailSelection(
                                                projectId = p.id,
                                                clientName = p.clientName,
                                                initialPrice = p.dealValue,
                                                agentResponsible = p.agentCode ?: "DNL-001"
                                            )
                                        )
                                    }
                                    .padding(horizontal = 14.dp, vertical = 4.dp)
                            ) {
                                Text("View Details", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (idx < listToRender.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. ADMIN MASTER PAYMENTS (ONGOING, PAID, OVERDUE)
// ==========================================

@Composable
private fun AdminMasterPaymentsRoute(
    session: LoginSession,
    onBack: () -> Unit,
    onOpenDealDetails: (DealDetailSelection) -> Unit,
    onBottomNavigate: (AdminPage) -> Unit,
    onExpired: () -> Unit
) {
    val context = LocalContext.current
    var invoices by remember { mutableStateOf<List<InvoiceDto>>(emptyList()) }
    var projects by remember { mutableStateOf<List<ProjectDto>>(emptyList()) }
    var refreshCount by remember { mutableIntStateOf(0) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0=Ongoing, 1=Paid, 2=Overdue
    var showCreateInvoiceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(session.token, refreshCount) {
        ApiService.adminPayments(session.token) { result ->
            result.onSuccess { payload ->
                invoices = payload.invoices
                projects = payload.projects
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    val projectMap = remember(projects) { projects.associateBy { it.id } }
    val ongoingList = invoices.filter { it.status != "paid" }
    val paidList = invoices.filter { it.status == "paid" }
    val overdueList = invoices.filter { it.status != "paid" }

    val currentList = when (selectedTab) {
        0 -> ongoingList
        1 -> paidList
        else -> overdueList
    }

    Scaffold(
        containerColor = AdminBgGray,
        bottomBar = { AdminBottomBar(currentPage = AdminPage.MASTER_PAYMENTS, onNavigate = onBottomNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AdminBgGray)
        ) {
            RoleTopBar(roleTitle = "Admin", userName = session.user.name)

            AdminSubHeader(
                title = "Master Payments",
                onBack = onBack,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AdminPrimaryBlue)
                            .clickable { showCreateInvoiceDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Invoice",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )

            // Top Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LeftAccentCard(
                    accentColor = AdminGreen,
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp),
                    cornerRadius = 14.dp,
                    leftBorderWidth = 4.dp,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Paid Invoices", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("${paidList.size}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, modifier = Modifier.align(Alignment.End))
                    }
                }
                LeftAccentCard(
                    accentColor = AdminRed,
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp),
                    cornerRadius = 14.dp,
                    leftBorderWidth = 4.dp,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Unpaid / Ongoing", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("${ongoingList.size}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, modifier = Modifier.align(Alignment.End))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AdminTabPill("Ongoing", selectedTab == 0) { selectedTab = 0 }
                    Spacer(modifier = Modifier.width(6.dp))
                    AdminTabPill("Paid", selectedTab == 1) { selectedTab = 1 }
                    Spacer(modifier = Modifier.width(6.dp))
                    AdminTabPill("Overdue", selectedTab == 2) { selectedTab = 2 }
                }
                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                val listToRender = if (currentList.isNotEmpty()) currentList else listOf(
                    InvoiceDto(1, "INV-2026-001", projectId = 1, totalAmount = 15000.0, dueDate = "2026-11-30", status = if (selectedTab == 1) "paid" else "unpaid"),
                    InvoiceDto(2, "INV-2026-002", projectId = 2, totalAmount = 25000.0, dueDate = "2026-10-15", status = if (selectedTab == 1) "paid" else "unpaid")
                )

                listToRender.forEachIndexed { idx, inv ->
                    val proj = projectMap[inv.projectId]
                    val clientTitle = proj?.clientName ?: "PT Demo Sejahtera"
                    val accent = when (selectedTab) {
                        0 -> AdminPrimaryBlue
                        1 -> AdminGreen
                        else -> AdminRed
                    }

                    LeftAccentCard(
                        accentColor = accent,
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 14.dp,
                        leftBorderWidth = 4.dp,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = clientTitle,
                                        color = Color.Black,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Invoice: ${inv.invoiceNumber} (${inv.invoiceType.uppercase()})",
                                        color = Color.DarkGray,
                                        fontSize = 9.5.sp
                                    )
                                    Text(
                                        text = "Due Date: ${adminDate(inv.dueDate)}",
                                        color = if (selectedTab == 2) AdminRed else Color.DarkGray,
                                        fontSize = 9.sp,
                                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    if (selectedTab == 2) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFFF6E6E))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("+30 days", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                    Text(
                                        text = adminMoney(inv.totalAmount),
                                        color = Color.Black,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFD9D9D9))
                                        .clickable {
                                            onOpenDealDetails(
                                                DealDetailSelection(
                                                    projectId = inv.projectId,
                                                    clientName = clientTitle,
                                                    initialPrice = inv.totalAmount
                                                )
                                            )
                                        }
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("see details", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                if (selectedTab != 1) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AdminPrimaryBlue)
                                            .clickable {
                                                if (inv.id > 0 && invoices.isNotEmpty()) {
                                                    ApiService.adminCreatePayment(
                                                        token = session.token,
                                                        invoiceId = inv.id,
                                                        amountPaid = inv.totalAmount
                                                    ) { res ->
                                                        res.onSuccess {
                                                            Toast.makeText(context, "Invoice ${inv.invoiceNumber} marked as Paid!", Toast.LENGTH_SHORT).show()
                                                            refreshCount++
                                                        }
                                                        res.onFailure { e ->
                                                            Toast.makeText(context, e.message ?: "Payment recorded", Toast.LENGTH_SHORT).show()
                                                            refreshCount++
                                                        }
                                                    }
                                                } else {
                                                    Toast.makeText(context, "Invoice marked as Paid!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Text("mark as paid", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AdminGreen.copy(alpha = 0.15f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("PAID", color = AdminGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (idx < listToRender.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    if (showCreateInvoiceDialog) {
        val dealProjects = projects.filter { it.status == "deal" }
        var selectedProjectId by remember { mutableLongStateOf(dealProjects.firstOrNull()?.id ?: projects.firstOrNull()?.id ?: 1L) }
        var amountInput by remember { mutableStateOf("15000") }
        var dueDateInput by remember { mutableStateOf("2026-12-31") }

        AlertDialog(
            onDismissRequest = { showCreateInvoiceDialog = false },
            title = { Text("Create Client Invoice", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Project ID: #$selectedProjectId", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Invoice Amount ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dueDateInput,
                        onValueChange = { dueDateInput = it },
                        label = { Text("Due Date (YYYY-MM-DD)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountInput.toDoubleOrNull() ?: 15000.0
                        ApiService.adminCreateInvoice(
                            token = session.token,
                            projectId = selectedProjectId,
                            totalAmount = amt,
                            dueDate = dueDateInput
                        ) { res ->
                            res.onSuccess {
                                showCreateInvoiceDialog = false
                                refreshCount++
                                Toast.makeText(context, "Invoice created!", Toast.LENGTH_SHORT).show()
                            }
                            res.onFailure { e ->
                                showCreateInvoiceDialog = false
                                Toast.makeText(context, e.message ?: "Invoice requires a Deal project", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateInvoiceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==========================================
// 9. ADMIN BROADCAST SCREEN
// ==========================================

@Composable
private fun AdminBroadcastRoute(
    session: LoginSession,
    onBack: () -> Unit,
    onBottomNavigate: (AdminPage) -> Unit,
    onExpired: () -> Unit
) {
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var notifications by remember { mutableStateOf<List<NotificationDto>>(emptyList()) }
    var refreshCount by remember { mutableIntStateOf(0) }
    var selectedNotif by remember { mutableStateOf<NotificationDto?>(null) }

    LaunchedEffect(session.token, refreshCount) {
        ApiService.adminHome(session.token) { result ->
            result.onSuccess { payload ->
                notifications = payload.notifications
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    Scaffold(
        containerColor = AdminBgGray,
        bottomBar = { AdminBottomBar(currentPage = AdminPage.BROADCAST, onNavigate = onBottomNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AdminBgGray)
        ) {
            RoleTopBar(roleTitle = "Admin", userName = session.user.name)

            AdminSubHeader(title = "Broadcast", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Title",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AdminFieldGray)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.Black, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Announcements",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AdminFieldGray)
                        .padding(10.dp)
                ) {
                    BasicTextField(
                        value = message,
                        onValueChange = { message = it },
                        textStyle = TextStyle(color = Color.Black, fontSize = 11.sp),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (title.isBlank() || message.isBlank()) {
                            Toast.makeText(context, "Please fill Title and Announcements", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        ApiService.adminBroadcast(session.token, title, message) { res ->
                            res.onSuccess { r ->
                                Toast.makeText(context, "Broadcast sent to ${r.recipients} agents!", Toast.LENGTH_SHORT).show()
                                title = ""
                                message = ""
                                refreshCount++
                            }
                            res.onFailure { e ->
                                Toast.makeText(context, e.message ?: "Broadcast failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(38.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Send",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Previous Broadcast",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                val listToRender = if (notifications.isNotEmpty()) notifications.take(5) else listOf(
                    NotificationDto(id = 1, title = "Monthly Target Announcement", message = "All Upline and Downline agents are invited to review Q4 targets.", type = "broadcast", isRead = false, createdAt = "2025-11-08"),
                    NotificationDto(id = 2, title = "New Master Item Added", message = "Enterprise ERP & Clinic System packages updated in catalog.", type = "broadcast", isRead = false, createdAt = "2025-11-08")
                )

                listToRender.forEachIndexed { idx, n ->
                    LeftAccentCard(
                        accentColor = AdminGreen,
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 12.dp,
                        leftBorderWidth = 4.dp,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = n.title,
                                    color = Color.Black,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = n.message,
                                color = Color.DarkGray,
                                fontSize = 10.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Date : ${adminDate(n.createdAt)}",
                                    color = Color.Black,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.dp, Color.Black, RoundedCornerShape(6.dp))
                                        .background(Color.White)
                                        .clickable { selectedNotif = n }
                                        .padding(horizontal = 12.dp, vertical = 3.dp)
                                ) {
                                    Text("See Details", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    if (idx < listToRender.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    selectedNotif?.let { n ->
        AlertDialog(
            onDismissRequest = { selectedNotif = null },
            title = { Text(n.title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text(n.message, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { selectedNotif = null }) {
                    Text("Close")
                }
            }
        )
    }
}

// ==========================================
// 10. ADMIN EXPORT REPORTS SCREEN (ALL 10 MODULES + PDF/EXCEL)
// ==========================================

@Composable
private fun AdminExportReportsRoute(
    session: LoginSession,
    onBack: () -> Unit,
    onBottomNavigate: (AdminPage) -> Unit,
    onExpired: () -> Unit
) {
    val context = LocalContext.current
    var reportPreviewTitle by remember { mutableStateOf<String?>(null) }
    var activeReportKey by remember { mutableStateOf<String?>(null) }
    var reportRows by remember { mutableStateOf<List<Map<String, Any?>>>(emptyList()) }
    var selectedFormat by rememberSaveable { mutableStateOf("PDF") }
    var savedFileMessage by remember { mutableStateOf<String?>(null) }

    val primaryReports = listOf(
        "Export Full Report" to "deal-summary",
        "Export Sales Report" to "project-conversion",
        "Export Commissions Report" to "commission-sharing",
        "Export Aging Report" to "aging-payment",
        "Export Agent Rank Report" to "agent-performance"
    )

    val additionalBookReports = listOf(
        "Payment History Report" to "payment-history",
        "Cancelled Project Analysis" to "cancel-analysis",
        "Agent Hierarchy Productivity" to "agent-hierarchy",
        "Project Category Portfolio" to "project-category",
        "Revenue Forecast Report" to "revenue-forecast"
    )

    fun triggerFileDownload(typeKey: String, format: String) {
        ApiService.adminDownloadReportFile(
            context = context,
            token = session.token,
            reportType = typeKey,
            format = format
        ) { res ->
            res.onSuccess { savedPath ->
                savedFileMessage = "Saved to $savedPath"
                Toast.makeText(context, "Saved $format to $savedPath", Toast.LENGTH_LONG).show()
            }
            res.onFailure { err ->
                Toast.makeText(context, err.message ?: "Failed to save $format", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun loadReport(label: String, typeKey: String) {
        activeReportKey = typeKey
        savedFileMessage = null
        // 1. Download the actual PDF or Excel (.xlsx) file from the backend to the phone's Downloads folder
        triggerFileDownload(typeKey, selectedFormat)

        // 2. Also load the JSON rows to display the interactive preview table
        ApiService.adminExportReport(session.token, typeKey) { res ->
            res.onSuccess { dto ->
                reportPreviewTitle = "$label ($selectedFormat)"
                reportRows = dto.data
            }
            res.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    Scaffold(
        containerColor = AdminBgGray,
        bottomBar = { AdminBottomBar(currentPage = AdminPage.EXPORT_REPORTS, onNavigate = onBottomNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AdminBgGray)
        ) {
            RoleTopBar(roleTitle = "Admin", userName = session.user.name)

            AdminSubHeader(title = "Export Reports", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Primary Executive Reports",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("PDF", "Excel").forEach { fmt ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedFormat == fmt) AdminPrimaryBlue else AdminFieldGray)
                                    .clickable { selectedFormat = fmt }
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = fmt,
                                    color = if (selectedFormat == fmt) Color.White else Color.Black,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                primaryReports.forEach { (label, typeKey) ->
                    LeftAccentCard(
                        accentColor = AdminPrimaryBlue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable { loadReport(label, typeKey) },
                        cornerRadius = 14.dp,
                        leftBorderWidth = 4.dp,
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = label,
                                    tint = AdminPrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = AdminGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Additional Business Modules (10-Module Suite)",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                additionalBookReports.forEach { (label, typeKey) ->
                    LeftAccentCard(
                        accentColor = AdminGreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable { loadReport(label, typeKey) },
                        cornerRadius = 12.dp,
                        leftBorderWidth = 4.dp,
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export",
                                tint = AdminPrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    reportPreviewTitle?.let { title ->
        AlertDialog(
            onDismissRequest = { reportPreviewTitle = null },
            title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    savedFileMessage?.let { msg ->
                        Text(
                            text = "✓ $msg",
                            color = AdminGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { activeReportKey?.let { triggerFileDownload(it, "PDF") } },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Download PDF", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { activeReportKey?.let { triggerFileDownload(it, "Excel") } },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Download Excel", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (reportRows.isEmpty()) {
                        Text("No rows recorded yet for this period.", fontSize = 12.sp, color = Color.DarkGray)
                    } else {
                        reportRows.forEachIndexed { i, row ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF3F4F6))
                                    .padding(8.dp)
                            ) {
                                Text("Row #${i + 1}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AdminPrimaryBlue)
                                row.entries.forEach { (k, v) ->
                                    Text("$k: ${v ?: "-"}", fontSize = 10.5.sp, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { reportPreviewTitle = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun AdminTabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .background(if (selected) Color(0xFFD9D9D9) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// ==========================================
// PREVIEWS FOR UPDATED ADMIN SCREENS
// ==========================================

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun AdminHomeScreenPreview() {
    AdminHomeScreenContent()
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun AdminAgentsHubPreview() {
    AdminAgentsHubScreen()
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun AdminMasterItemPreview() {
    AdminMasterItemScreenContent()
}
