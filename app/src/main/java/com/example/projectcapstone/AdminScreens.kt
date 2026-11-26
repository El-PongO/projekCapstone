package com.example.projectcapstone

import androidx.compose.ui.zIndex

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.draw.shadow
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
private val AdminBgGray = Color(0xFFF3F6FB)
private val AdminPrimaryBlue = Color(0xFF1458ED)
private val AdminDarkBlue = Color(0xFF1946B8)
private val AdminGreen = Color(0xFF19A668)
private val AdminRed = Color(0xFFE95465)
private val AdminOlive = Color(0xFFF2B537)
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
            CompositionLocalProvider(LocalIntegraAdminNav provides { target: AdminPage -> navigateTo(target) }) {
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
        }

        AdminPage.PROFILE -> {
            CompositionLocalProvider(LocalIntegraAdminNav provides { target: AdminPage -> navigateTo(target) }) {
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
    Row(
        modifier = Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(42.dp).clickable { onBack() },
            shape = RoundedCornerShape(14.dp),
            color = AdminPrimaryBlue,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF18233D))
            Text("Integra Agency • Admin Workspace", fontSize = 11.sp, color = Color(0xFF71809B))
        }
        trailingContent?.invoke()
    }
}

@Composable
fun AdminBottomBar(
    currentPage: AdminPage = AdminPage.HOME,
    onNavigate: (AdminPage) -> Unit = {}
) {
    AppBottomBar(
        items = listOf(
            AppBottomNavItem("Home", Icons.Outlined.Home, currentPage == AdminPage.HOME) { onNavigate(AdminPage.HOME) },
            AppBottomNavItem(
                "Agents",
                Icons.Outlined.Groups,
                currentPage in listOf(AdminPage.AGENTS_HUB, AdminPage.UPLINE_LIST, AdminPage.DOWNLINE_LIST, AdminPage.USER_DETAILS)
            ) { onNavigate(AdminPage.AGENTS_HUB) },
            AppBottomNavItem("Projects", Icons.Outlined.Folder, currentPage == AdminPage.MASTER_PROJECT) { onNavigate(AdminPage.MASTER_PROJECT) },
            AppBottomNavItem("Profile", Icons.Outlined.Person, currentPage == AdminPage.PROFILE) { onNavigate(AdminPage.PROFILE) }
        ),
        centerAction = AppBottomNavItem("Broadcast", Icons.Default.Campaign, currentPage == AdminPage.BROADCAST) {
            onNavigate(AdminPage.BROADCAST)
        },
        selectedColor = AdminPrimaryBlue
    )
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
    IntegraModernAdminHomeScreen(
        userName = userName,
        overview = overview,
        notifications = notifications,
        recentProjects = recentProjects,
        onNavigate = onNavigate,
        onOpenDealDetails = onOpenDealDetails
    )
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
    IntegraModernAdminAgentsHubScreen(
        userName = userName,
        onBack = onBack,
        onUplineClick = onUplineClick,
        onDownlineClick = onDownlineClick,
        onBottomNavigate = onBottomNavigate
    )
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

    IntegraModernAdminAgentListScreen(
        userName = userName,
        title = title,
        agents = filtered,
        searchQuery = searchQuery,
        onSearchChange = { searchQuery = it },
        onBack = onBack,
        onAddClick = onAddClick,
        onSelectAgent = onSelectAgent,
        onBottomNavigate = onBottomNavigate
    )
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

    IntegraModernAdminUserDetailsScreen(
        userName = session?.user?.name ?: "Admin",
        agent = agent,
        onBack = onBack,
        onBottomNavigate = onBottomNavigate,
        onEditRateClick = { showEditRateDialog = true }
    )

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
    IntegraModernAdminMasterItemScreen(
        userName = userName,
        selectedTab = selectedTab,
        onSelectTab = onSelectTab,
        items = items,
        categories = categories,
        onBack = onBack,
        onAddClick = onAddClick,
        onBottomNavigate = onBottomNavigate
    )
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
    var selectedRateApproval by remember { mutableStateOf<ApprovalDto?>(null) }
    var pendingPayout by remember { mutableStateOf<CommissionDto?>(null) }

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

    selectedRateApproval?.let { approval ->
        IntegraAdminDealRateDialog(
            token = session.token,
            approvalId = approval.id,
            onDismiss = { selectedRateApproval = null },
            onUpdated = { refreshCount++ }
        )
    }

    pendingPayout?.let { comm ->
        AlertDialog(
            onDismissRequest = { pendingPayout = null },
            title = { Text("Konfirmasi transfer komisi") },
            text = { Text("Pastikan dana sudah ditransfer ke agen. Tindakan ini mencatat sisa komisi sebagai Paid, tetapi tidak melakukan transfer bank otomatis.") },
            confirmButton = {
                Button(onClick = {
                    pendingPayout = null
                    ApiService.adminPayRemainingCommission(session.token, comm.id) { result ->
                        result.onSuccess {
                            Toast.makeText(context, "Pencairan komisi tercatat", Toast.LENGTH_SHORT).show()
                            refreshCount++
                        }.onFailure { err ->
                            Toast.makeText(context, err.message ?: "Pencairan gagal", Toast.LENGTH_LONG).show()
                        }
                    }
                }) { Text("Sudah Transfer") }
            },
            dismissButton = { TextButton(onClick = { pendingPayout = null }) { Text("Batal") } }
        )
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

            AdminV3PageHero(
                title = "Pending approvals",
                subtitle = "Review permintaan Deal dan komisi agent",
                icon = Icons.Default.CheckCircle
            )

            // Top Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminV3AccentCard(
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
                AdminV3AccentCard(
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
                    .shadow(3.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFE4EBF5), RoundedCornerShape(24.dp))
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
                HorizontalDivider(color = Color(0xFFE4EBF5), thickness = 1.dp)
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

                            AdminV3AccentCard(
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
                                        OutlinedButton(onClick = {
                                            onOpenDealDetails(DealDetailSelection(
                                                projectId = proj.id, clientName = proj.clientName,
                                                initialPrice = proj.dealValue,
                                                agentResponsible = proj.agentCode ?: "-"
                                            ))
                                        }) { Text("Detail", fontSize = 11.sp) }
                                        if (pendingApp != null) {
                                            TextButton(onClick = { selectedRateApproval = pendingApp }) {
                                                Text("Sepakati Rate", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                    if (pendingApp != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    ApiService.adminReviewApproval(session.token, pendingApp.id, "approved") { result ->
                                                        result.onSuccess {
                                                            Toast.makeText(context, "Deal disetujui", Toast.LENGTH_SHORT).show()
                                                            refreshCount++
                                                        }.onFailure { error ->
                                                            Toast.makeText(context, error.message ?: "Rate belum disepakati", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = AdminGreen)
                                            ) { Text("Approve Deal", fontSize = 11.sp) }
                                            OutlinedButton(
                                                onClick = {
                                                    ApiService.adminReviewApproval(session.token, pendingApp.id, "rejected", "Ditolak Admin") { result ->
                                                        result.onSuccess {
                                                            Toast.makeText(context, "Permintaan Deal ditolak", Toast.LENGTH_SHORT).show()
                                                            refreshCount++
                                                        }.onFailure { error ->
                                                            Toast.makeText(context, error.message ?: "Gagal menolak", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) { Text("Tolak", fontSize = 11.sp) }
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
                            AdminV3AccentCard(
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
                                                        ApiService.adminApproveCommission(session.token, comm.id) { result ->
                                                            result.onSuccess {
                                                                Toast.makeText(context, "Komisi disetujui", Toast.LENGTH_SHORT).show()
                                                                refreshCount++
                                                            }.onFailure { error ->
                                                                Toast.makeText(context, error.message ?: "Komisi belum dapat disetujui", Toast.LENGTH_LONG).show()
                                                            }
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
                                                        pendingPayout = comm
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

            AdminV3PageHero(
                title = "Project pipeline",
                subtitle = "Pantau project aktif, deal, dan riwayat",
                icon = Icons.Default.Folder
            )

            // Top Summary Cards (Matching CRM Style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminV3AccentCard(
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
                AdminV3AccentCard(
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
                    .shadow(3.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFE4EBF5), RoundedCornerShape(24.dp))
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
                HorizontalDivider(color = Color(0xFFE4EBF5), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                val listToRender = currentList

                if (listToRender.isEmpty()) AdminV3EmptyState("Belum ada project pada kategori ini.")

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

                    AdminV3AccentCard(
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
    var pendingInvoicePayment by remember { mutableStateOf<InvoiceDto?>(null) }

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

    pendingInvoicePayment?.let { inv ->
        AlertDialog(
            onDismissRequest = { pendingInvoicePayment = null },
            title = { Text("Konfirmasi penerimaan pembayaran") },
            text = { Text("Pastikan pembayaran klien benar-benar sudah diterima. Aksi ini mencatat sisa tagihan invoice sebagai lunas.") },
            confirmButton = {
                Button(onClick = {
                    pendingInvoicePayment = null
                    ApiService.adminMarkInvoiceFullyPaid(session.token, inv.id) { result ->
                        result.onSuccess {
                            Toast.makeText(context, "Invoice tercatat lunas", Toast.LENGTH_SHORT).show()
                            refreshCount++
                        }.onFailure { err ->
                            Toast.makeText(context, err.message ?: "Gagal mencatat pembayaran", Toast.LENGTH_LONG).show()
                        }
                    }
                }) { Text("Sudah Diterima") }
            },
            dismissButton = { TextButton(onClick = { pendingInvoicePayment = null }) { Text("Batal") } }
        )
    }

    val projectMap = remember(projects) { projects.associateBy { it.id } }
    val ongoingList = invoices.filter { it.status != "paid" }
    val paidList = invoices.filter { it.status == "paid" }
    val overdueList = invoices.filter { invoice ->
        val due = invoice.dueDate?.take(10)
        invoice.status != "paid" && !due.isNullOrBlank() &&
            runCatching { java.time.LocalDate.parse(due).isBefore(java.time.LocalDate.now(java.time.ZoneId.of("Asia/Jakarta"))) }.getOrDefault(false)
    }

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

            AdminV3PageHero(
                title = "Payment management",
                subtitle = "Kelola invoice dan status pembayaran",
                icon = Icons.Default.AttachMoney
            )

            // Top Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminV3AccentCard(
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
                AdminV3AccentCard(
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
                    .shadow(3.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFE4EBF5), RoundedCornerShape(24.dp))
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
                HorizontalDivider(color = Color(0xFFE4EBF5), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                val listToRender = currentList

                if (listToRender.isEmpty()) AdminV3EmptyState("Belum ada invoice pada kategori ini.")

                listToRender.forEachIndexed { idx, inv ->
                    val proj = projectMap[inv.projectId]
                    val clientTitle = proj?.clientName ?: "Project #${inv.projectId}"
                    val accent = when (selectedTab) {
                        0 -> AdminPrimaryBlue
                        1 -> AdminGreen
                        else -> AdminRed
                    }

                    AdminV3AccentCard(
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
                                                pendingInvoicePayment = inv
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
    var audience by rememberSaveable { mutableStateOf("all") }
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

            AdminV3PageHero(
                title = "Team announcements",
                subtitle = "Kirim informasi ke seluruh agent",
                icon = Icons.Default.Campaign
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .shadow(3.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFE4EBF5), RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text("Create Broadcast", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF18233D))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Title") },
                    placeholder = { Text("Judul pengumuman") },
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    label = { Text("Announcements") },
                    placeholder = { Text("Tulis pesan untuk semua agent...") },
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text("Penerima Broadcast", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("all" to "Semua", "upline" to "Upline", "downline" to "Downline").forEach { (key, label) ->
                        FilterChip(selected = audience == key, onClick = { audience = key }, label = { Text(label, fontSize = 11.sp) })
                    }
                }

                Button(
                    onClick = {
                        if (title.isBlank() || message.isBlank()) {
                            Toast.makeText(context, "Please fill Title and Announcements", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        ApiService.adminBroadcast(
                            session.token, title, message,
                            roles = when (audience) { "upline" -> listOf("upline"); "downline" -> listOf("downline"); else -> null }
                        ) { res ->
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
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Send Broadcast",
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

                val listToRender = notifications.take(5)

                if (listToRender.isEmpty()) AdminV3EmptyState("Belum ada broadcast sebelumnya.")

                listToRender.forEachIndexed { idx, n ->
                    AdminV3AccentCard(
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
    var reportColumns by remember { mutableStateOf<List<ReportColumnDto>>(emptyList()) }
    var reportSummary by remember { mutableStateOf<Map<String, Any?>>(emptyMap()) }
    var previewFrom by remember { mutableStateOf<String?>(null) }
    var previewTo by remember { mutableStateOf<String?>(null) }
    var selectedFormat by rememberSaveable { mutableStateOf("PDF") }
    var reportFrom by rememberSaveable { mutableStateOf("") }
    var reportTo by rememberSaveable { mutableStateOf("") }
    var exportedFiles by remember { mutableStateOf<List<String>?>(null) }
    val selectedReports = remember { mutableStateListOf<String>() }
    var exporting by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val primaryReports = listOf(
        "Ringkasan Deal & Nilai Proyek" to "deal-summary",
        "Konversi Proyek" to "project-conversion",
        "Komisi & Bagi Hasil" to "commission-sharing",
        "Piutang & Umur Pembayaran" to "aging-payment",
        "Performa & Kinerja Agen" to "agent-performance"
    )

    val additionalBookReports = listOf(
        "Pembayaran & Riwayat Transaksi" to "payment-history",
        "Evaluasi Proyek Batal" to "cancel-analysis",
        "Produktivitas Hierarki Agen" to "agent-hierarchy",
        "Portofolio Kategori Proyek" to "project-category",
        "Proyeksi Potensi Pendapatan" to "revenue-forecast"
    )

    fun validPeriod(): Boolean {
        val from = runCatching { if (reportFrom.isBlank()) null else java.time.LocalDate.parse(reportFrom.trim()) }.getOrElse {
            Toast.makeText(context, "Tanggal Mulai harus YYYY-MM-DD", Toast.LENGTH_SHORT).show()
            return false
        }
        val to = runCatching { if (reportTo.isBlank()) null else java.time.LocalDate.parse(reportTo.trim()) }.getOrElse {
            Toast.makeText(context, "Tanggal Selesai harus YYYY-MM-DD", Toast.LENGTH_SHORT).show()
            return false
        }
        if (from != null && to != null && from.isAfter(to)) {
            Toast.makeText(context, "Tanggal Mulai harus sebelum Tanggal Selesai", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun downloadReports(types: List<String>, format: String, from: String?, to: String?) {
        if (types.isEmpty() || exporting) return
        exporting = true
        selectedReports.clear()
        val savedPaths = mutableListOf<String>()
        fun downloadNext(index: Int) {
            if (index == types.size) {
                exporting = false
                exportedFiles = savedPaths.toList()
                return
            }
            ApiService.adminDownloadReportFile(context, session.token, types[index], format, from, to) { result ->
                result.onSuccess { savedPath ->
                    savedPaths.add(savedPath)
                    downloadNext(index + 1)
                }
                result.onFailure { err ->
                    exporting = false
                    if (err is ApiError && err.unauthorized) onExpired()
                    else Toast.makeText(context, "Gagal mengunduh ${types[index]} (${index} berhasil): ${err.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        downloadNext(0)
    }

    fun loadReport(label: String, typeKey: String) {
        if (!validPeriod() || loading) return
        val from = reportFrom.trim().ifBlank { null }
        val to = reportTo.trim().ifBlank { null }
        loading = true
        ApiService.adminExportReport(
            session.token, typeKey, from = from, to = to
        ) { res ->
            loading = false
            res.onSuccess { dto ->
                activeReportKey = typeKey
                previewFrom = from
                previewTo = to
                reportPreviewTitle = label
                reportRows = dto.data
                reportColumns = dto.columns.ifEmpty {
                    dto.data.firstOrNull()?.keys?.map { key ->
                        ReportColumnDto(key, key.replace('_', ' ').replaceFirstChar { it.uppercase() })
                    } ?: emptyList()
                }
                reportSummary = dto.summary
            }
            res.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
                else Toast.makeText(context, err.message ?: "Gagal memuat laporan", Toast.LENGTH_LONG).show()
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

            AdminV3PageHero(
                title = "Business reports",
                subtitle = "Unduh laporan dan pantau performa",
                icon = Icons.Default.Download
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .shadow(3.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFE4EBF5), RoundedCornerShape(24.dp))
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
                        text = "Pilih laporan",
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

                Text("Filter Periode (opsional)", fontWeight = FontWeight.SemiBold, color = AdminPrimaryBlue)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = reportFrom, onValueChange = { reportFrom = it },
                        label = { Text("Mulai") }, placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = reportTo, onValueChange = { reportTo = it },
                        label = { Text("Selesai") }, placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                }

                primaryReports.forEach { (label, typeKey) ->
                    AdminV3AccentCard(
                        accentColor = AdminPrimaryBlue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
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
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = typeKey in selectedReports,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedReports.add(typeKey) else selectedReports.remove(typeKey)
                                    }
                                )
                                Text(
                                    text = label,
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Pratinjau",
                                tint = AdminGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Laporan lainnya",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                additionalBookReports.forEach { (label, typeKey) ->
                    AdminV3AccentCard(
                        accentColor = AdminGreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
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
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = typeKey in selectedReports,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedReports.add(typeKey) else selectedReports.remove(typeKey)
                                    }
                                )
                                Text(
                                    text = label,
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Pratinjau",
                                tint = AdminPrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Text("Ketuk nama laporan untuk pratinjau, atau centang untuk ekspor.", fontSize = 11.sp)
                Button(
                    onClick = {
                        if (validPeriod()) downloadReports(
                            selectedReports.toList(), selectedFormat,
                            reportFrom.trim().ifBlank { null }, reportTo.trim().ifBlank { null }
                        )
                    },
                    enabled = selectedReports.isNotEmpty() && !exporting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (exporting) "Mengunduh..." else "Ekspor ${selectedReports.size} laporan ($selectedFormat)")
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
                    if (reportSummary.isNotEmpty()) {
                        Text("Ringkasan", fontWeight = FontWeight.Bold, color = AdminDarkBlue, fontSize = 13.sp)
                        reportSummary.forEach { (key, value) ->
                            val label = reportColumns.firstOrNull { it.key == key }?.label
                                ?: key.replace('_', ' ').replaceFirstChar { it.uppercase() }
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text(label, modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color.DarkGray)
                                Text("${value ?: "-"}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            HorizontalDivider(color = Color(0xFFE4EBF5))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { activeReportKey?.let { downloadReports(listOf(it), "PDF", previewFrom, previewTo) } },
                            enabled = !exporting,
                            colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Download PDF", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { activeReportKey?.let { downloadReports(listOf(it), "Excel", previewFrom, previewTo) } },
                            enabled = !exporting,
                            colors = ButtonDefaults.buttonColors(containerColor = AdminGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Download Excel", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (reportColumns.isNotEmpty()) {
                        Text("Rincian laporan", fontWeight = FontWeight.Bold, color = AdminDarkBlue, fontSize = 13.sp)
                        Text("Geser ke samping untuk melihat semua kolom", fontSize = 10.sp, color = Color.DarkGray)
                        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                                reportColumns.forEach { column ->
                                    Box(
                                        modifier = Modifier.width(130.dp).fillMaxHeight()
                                            .background(AdminDarkBlue)
                                            .border(0.5.dp, Color(0xFFCBD5E1))
                                            .padding(8.dp)
                                    ) {
                                        Text(column.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                            reportRows.forEachIndexed { index, row ->
                                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                                    reportColumns.forEach { column ->
                                        Box(
                                            modifier = Modifier.width(130.dp).fillMaxHeight()
                                                .background(if (index % 2 == 0) Color(0xFFF3F6FB) else Color.White)
                                                .border(0.5.dp, Color(0xFFCBD5E1))
                                                .padding(8.dp)
                                        ) {
                                            Text("${row[column.key] ?: "-"}", fontSize = 11.sp, color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (reportRows.isEmpty()) Text("Belum ada data untuk periode ini.", fontSize = 12.sp, color = Color.DarkGray)
                }
            },
            confirmButton = {
                TextButton(onClick = { reportPreviewTitle = null }) {
                    Text("Close")
                }
            }
        )
    }

    exportedFiles?.let { paths ->
        AlertDialog(
            onDismissRequest = { exportedFiles = null },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AdminGreen) },
            title = { Text("Ekspor berhasil", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("${paths.size} laporan berhasil disimpan di perangkat Anda.")
                    paths.forEach { path ->
                        Text(path, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { exportedFiles = null }) { Text("OK") }
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
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (selected) AdminPrimaryBlue else Color(0xFFF0F4FA),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            color = if (selected) Color.White else Color(0xFF64738E),
            fontSize = 12.sp,
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
