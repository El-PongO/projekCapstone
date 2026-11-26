package com.example.projectcapstone

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// ==========================================
// THEME COLORS MATCHING MOCKUPS
// ==========================================
private val ScreenBgGray = Color(0xFFE5E5E5)
private val PrimaryBlue = Color(0xFF0048FF)
private val DarkBlueAccent = Color(0xFF002080)
private val GreenAccent = Color(0xFF1B8A2A)
private val RedAccent = Color(0xFFD92121)
private val OliveAccent = Color(0xFFA0A818)
private val InputFieldGray = Color(0xFFD9D9D9)
private val BadgeRed = Color(0xFFFF6E6E)
private val BadgeYellow = Color(0xFFEAFF42)
private val BadgeGreen = Color(0xFF42E831)
private val DirectDealRed = Color(0xFFFF3B30)

private fun formatCurrency(value: Double, useDotSeparator: Boolean = false): String {
    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }
    val formatted = nf.format(value)
    return if (useDotSeparator) "$${formatted.replace(',', '.')}" else "$$formatted"
}

private fun formatDateDmy(raw: String?, separator: String = "-"): String {
    if (raw.isNullOrBlank()) return "08${separator}11${separator}2025"
    val clean = raw.take(10)
    val parts = clean.split("-")
    return if (parts.size == 3) {
        val year = parts[0]
        val month = parts[1].trimStart('0').ifEmpty { "1" }
        val day = parts[2].trimStart('0').ifEmpty { "1" }
        if (separator == " - ") {
            "${parts[2]} - ${parts[1]} - $year"
        } else if (separator == "-") {
            "${parts[2]}-${parts[1]}-$year"
        } else {
            "$day-$month-$year"
        }
    } else {
        clean
    }
}

private fun daysSince(rawDate: String?): Long {
    if (rawDate.isNullOrBlank()) return 2L
    return try {
        val datePart = rawDate.take(10)
        val parsed = LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE)
        val now = LocalDate.now(ZoneId.systemDefault())
        ChronoUnit.DAYS.between(parsed, now).coerceAtLeast(1L)
    } catch (_: Exception) {
        try {
            val instant = Instant.parse(rawDate)
            val parsed = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            ChronoUnit.DAYS.between(parsed, LocalDate.now()).coerceAtLeast(1L)
        } catch (_: Exception) {
            2L
        }
    }
}

// ==========================================
// ROOT COORDINATOR FOR DOWNLINE ROLE
// ==========================================

enum class DownlinePage {
    HOME,
    PERFORMANCE,
    CRM,
    NEW_PROPOSAL,
    DEAL_DETAILS,
    PROFILE
}

@Composable
fun DownlineAppScreen(
    session: LoginSession,
    onLogout: () -> Unit,
    onExpired: () -> Unit
) {
    var currentPage by rememberSaveable { mutableStateOf(DownlinePage.HOME) }
    var previousPage by rememberSaveable { mutableStateOf(DownlinePage.HOME) }
    var selectedDeal by remember { mutableStateOf(DealDetailSelection()) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    when (currentPage) {
        DownlinePage.HOME -> {
            DownlineHomeRoute(
                session = session,
                refreshKey = refreshTrigger,
                onNavigatePerformance = { currentPage = DownlinePage.PERFORMANCE },
                onNavigateCrm = { currentPage = DownlinePage.CRM },
                onNavigateCreateProposal = { currentPage = DownlinePage.NEW_PROPOSAL },
                onOpenProfile = {
                    previousPage = DownlinePage.HOME
                    currentPage = DownlinePage.PROFILE
                },
                onOpenNotifications = { showNotificationsDialog = true },
                onExpired = onExpired
            )
        }

        DownlinePage.PERFORMANCE -> {
            DownlinePerformanceRoute(
                session = session,
                onBack = { currentPage = DownlinePage.HOME },
                onHome = { currentPage = DownlinePage.HOME },
                onCreateProposal = { currentPage = DownlinePage.NEW_PROPOSAL },
                onOpenProfile = {
                    previousPage = DownlinePage.PERFORMANCE
                    currentPage = DownlinePage.PROFILE
                },
                onOpenNotifications = { showNotificationsDialog = true },
                onExpired = onExpired
            )
        }

        DownlinePage.CRM -> {
            DownlineCrmRoute(
                session = session,
                refreshKey = refreshTrigger,
                onBack = { currentPage = DownlinePage.HOME },
                onHome = { currentPage = DownlinePage.HOME },
                onCreateProposal = { currentPage = DownlinePage.NEW_PROPOSAL },
                onOpenDealDetails = { card ->
                    selectedDeal = DealDetailSelection(
                        projectId = card.id,
                        clientName = card.clientName,
                        productType = card.productName,
                        dealType = if (card.status == "deal") "Direct Deal" else "Follow-Up",
                        initialPrice = card.dealValue,
                        agentResponsible = card.downlineInCharge
                    )
                    previousPage = DownlinePage.CRM
                    currentPage = DownlinePage.DEAL_DETAILS
                },
                onOpenProfile = {
                    previousPage = DownlinePage.CRM
                    currentPage = DownlinePage.PROFILE
                },
                onOpenNotifications = { showNotificationsDialog = true },
                onExpired = onExpired
            )
        }

        DownlinePage.NEW_PROPOSAL -> {
            DownlineNewProposalRoute(
                session = session,
                onBack = { currentPage = DownlinePage.HOME },
                onHome = { currentPage = DownlinePage.HOME },
                onDealCreated = {
                    refreshTrigger++
                    currentPage = DownlinePage.CRM
                },
                onOpenProfile = {
                    previousPage = DownlinePage.NEW_PROPOSAL
                    currentPage = DownlinePage.PROFILE
                },
                onOpenNotifications = { showNotificationsDialog = true },
                onExpired = onExpired
            )
        }

        DownlinePage.DEAL_DETAILS -> {
            GlobalDealsDetailsRoute(
                session = session,
                selection = selectedDeal,
                centerBarLabel = "Create Proposal",
                onBack = { currentPage = previousPage },
                onHome = { currentPage = DownlinePage.HOME },
                onCenterAction = { currentPage = DownlinePage.NEW_PROPOSAL },
                onProfile = { currentPage = DownlinePage.PROFILE },
                onNotificationBellClick = {},
                onExpired = onExpired
            )
        }

        DownlinePage.PROFILE -> {
            GlobalProfileRoute(
                session = session,
                centerBarLabel = "Create Proposal",
                onBack = { currentPage = previousPage },
                onHome = { currentPage = DownlinePage.HOME },
                onCenterAction = { currentPage = DownlinePage.NEW_PROPOSAL },
                onLogout = onLogout,
                onNotificationBellClick = {},
                onExpired = onExpired
            )
        }
    }
}

// ==========================================
// SHARED COMPONENTS: TOP BAR, BOTTOM BAR, ACCENT CARD
// ==========================================

@Composable
fun DownlineTopBar(
    userName: String,
    onNotificationClick: () -> Unit = {}
) {
    RoleTopBar(
        roleTitle = "Downline",
        userName = userName,
        onNotificationClick = onNotificationClick
    )
}

@Composable
fun DownlineBottomBar(
    onHome: () -> Unit,
    onCreateProposal: () -> Unit,
    onProfile: () -> Unit
) {
    AppBottomBar(
        items = listOf(
            AppBottomNavItem("Home", Icons.Outlined.Home, onClick = onHome),
            AppBottomNavItem("Profile", Icons.Outlined.Person, onClick = onProfile)
        ),
        centerAction = AppBottomNavItem("Create Proposal", Icons.Default.Add, onClick = onCreateProposal),
        selectedColor = PrimaryBlue
    )
}

/**
 * Reusable card with thin black border and thick left (or left+right) colored accent border,
 * matching the exact visual style of the screenshots.
 */
@Composable
fun LeftAccentCard(
    accentColor: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    leftBorderWidth: Dp = 4.dp,
    rightBorderWidth: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val outerShape = RoundedCornerShape(cornerRadius)
    val innerShape = RoundedCornerShape((cornerRadius - 2.dp).coerceAtLeast(4.dp))
    Box(
        modifier = modifier
            .clip(outerShape)
            .background(accentColor)
            .border(1.dp, Color.Black, outerShape)
            .padding(
                start = leftBorderWidth,
                end = rightBorderWidth,
                top = 1.dp,
                bottom = 1.dp
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(innerShape)
                .background(Color.White)
                .padding(contentPadding),
            content = content
        )
    }
}

// ==========================================
// 1. HOMEPAGE DOWNLINE ("Home")
// ==========================================

data class DownlineNotificationItemUi(
    val id: Long,
    val title: String,
    val message: String,
    val date: String,
    val isCheckIcon: Boolean
)

@Composable
private fun DownlineHomeRoute(
    session: LoginSession,
    refreshKey: Int,
    onNavigatePerformance: () -> Unit,
    onNavigateCrm: () -> Unit,
    onNavigateCreateProposal: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onExpired: () -> Unit
) {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var monthlyCut by remember { mutableDoubleStateOf(0.0) }
    var projectsHeld by remember { mutableIntStateOf(0) }
    var uplineName by remember { mutableStateOf("Bruce Wayne") }
    var uplinePhoneOrCode by remember { mutableStateOf("08XXXXXXXX") }
    var notifications by remember { mutableStateOf<List<DownlineNotificationItemUi>>(emptyList()) }
    var selectedNotification by remember { mutableStateOf<DownlineNotificationItemUi?>(null) }

    LaunchedEffect(session.token, refreshKey) {
        loading = true
        ApiService.downlineHome(session.token) { result ->
            loading = false
            result.onSuccess { payload ->
                monthlyCut = payload.monthlyCut
                projectsHeld = payload.dashboard.summary.totalProjects
                payload.upline?.let { up ->
                    uplineName = up.name.ifBlank { "Bruce Wayne" }
                    uplinePhoneOrCode = up.phoneNumber?.ifBlank { null } ?: up.agentCode.ifBlank { "08XXXXXXXX" }
                }

                val mappedNotifs = mutableListOf<DownlineNotificationItemUi>()
                payload.notifications.forEach { n ->
                    mappedNotifs.add(
                        DownlineNotificationItemUi(
                            id = n.id,
                            title = n.title.ifBlank { "Upline Sent a Notification" },
                            message = n.message.ifBlank { "Update your agency project activity." },
                            date = formatDateDmy(n.createdAt, "-"),
                            isCheckIcon = n.type == "project_status"
                        )
                    )
                }
                payload.dashboard.upcomingTasks.forEach { t ->
                    mappedNotifs.add(
                        DownlineNotificationItemUi(
                            id = -t.id,
                            title = t.title.ifBlank { "Client Wants to meet" },
                            message = "Project follow-up schedule.",
                            date = formatDateDmy(t.dueAt, "-"),
                            isCheckIcon = true
                        )
                    )
                }

                notifications = if (mappedNotifs.isNotEmpty()) {
                    mappedNotifs
                } else {
                    listOf(
                        DownlineNotificationItemUi(
                            id = 0L,
                            title = "Upline Sent a Notification",
                            message = "Lorem ipsum dolor sit amet, consecte...............",
                            date = "08-11-2025",
                            isCheckIcon = false
                        ),
                        DownlineNotificationItemUi(
                            id = 0L,
                            title = "Client Wants to meet",
                            message = "Lorem ipsum dolor sit amet, consecte...............",
                            date = "08-11-2025",
                            isCheckIcon = true
                        )
                    )
                }
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) {
                    onExpired()
                }
            }
        }
    }

    DownlineHomeScreenContent(
        userName = session.user.name,
        monthlyCutFormatted = formatCurrency(monthlyCut),
        projectsHeld = projectsHeld,
        uplineName = uplineName,
        uplineContactNo = uplinePhoneOrCode,
        notifications = notifications,
        loading = loading,
        onNotificationBellClick = onOpenNotifications,
        onIncomeClick = onNavigatePerformance,
        onCrmClick = onNavigateCrm,
        onContactUplineClick = {
            if (uplinePhoneOrCode.any { it.isDigit() } && !uplinePhoneOrCode.startsWith("UPL")) {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$uplinePhoneOrCode")))
                }.onFailure {
                    Toast.makeText(context, "Upline: $uplineName ($uplinePhoneOrCode)", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Contacting Upline: $uplineName ($uplinePhoneOrCode)", Toast.LENGTH_SHORT).show()
            }
        },
        onSeeNotificationDetails = { item ->
            selectedNotification = item
            if (item.id > 0) {
                ApiService.markNotificationRead(session.token, item.id)
            }
        },
        onHome = {},
        onCreateProposal = onNavigateCreateProposal,
        onProfile = onOpenProfile
    )

    selectedNotification?.let { notif ->
        AlertDialog(
            onDismissRequest = { selectedNotification = null },
            title = { Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text(notif.message, fontSize = 13.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Date: ${notif.date}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
fun DownlineHomeScreenContent(
    userName: String,
    monthlyCutFormatted: String,
    projectsHeld: Int,
    uplineName: String,
    uplineContactNo: String,
    notifications: List<DownlineNotificationItemUi>,
    loading: Boolean = false,
    onNotificationBellClick: () -> Unit = {},
    onIncomeClick: () -> Unit = {},
    onCrmClick: () -> Unit = {},
    onContactUplineClick: () -> Unit = {},
    onSeeNotificationDetails: (DownlineNotificationItemUi) -> Unit = {},
    onHome: () -> Unit = {},
    onCreateProposal: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    IntegraModernDownlineHome(
        userName = userName,
        monthlyCutFormatted = monthlyCutFormatted,
        projectsHeld = projectsHeld,
        uplineName = uplineName,
        uplineContactNo = uplineContactNo,
        notifications = notifications,
        loading = loading,
        onNotificationBellClick = onNotificationBellClick,
        onIncomeClick = onIncomeClick,
        onCrmClick = onCrmClick,
        onContactUplineClick = onContactUplineClick,
        onSeeNotificationDetails = onSeeNotificationDetails,
        onHome = onHome,
        onCreateProposal = onCreateProposal,
        onProfile = onProfile
    )
}

// ==========================================
// 2. MY PERFORMANCE ("my performance (income)")
// ==========================================

data class DownlineSalesReportItemUi(
    val clientName: String,
    val dealCutPercent: String,
    val dateFormatted: String,
    val amountFormatted: String
)

@Composable
private fun DownlinePerformanceRoute(
    session: LoginSession,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onCreateProposal: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onExpired: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var totalProfits by remember { mutableDoubleStateOf(0.0) }
    var salesReportItems by remember { mutableStateOf<List<DownlineSalesReportItemUi>>(emptyList()) }

    LaunchedEffect(session.token) {
        loading = true
        ApiService.performance(session.token) { result ->
            loading = false
            result.onSuccess { perf ->
                totalProfits = if (perf.totalIncome > 0) {
                    perf.totalIncome
                } else {
                    perf.income.sumOf { it.commission }
                }

                val rows = mutableListOf<DownlineSalesReportItemUi>()
                perf.income.forEach { inc ->
                    val rateStr = if (inc.rate % 1.0 == 0.0) {
                        "${inc.rate.toInt()}%"
                    } else {
                        "${inc.rate}%"
                    }
                    rows.add(
                        DownlineSalesReportItemUi(
                            clientName = inc.client,
                            dealCutPercent = rateStr,
                            dateFormatted = formatDateDmy(inc.date, " - "),
                            amountFormatted = formatCurrency(inc.commission)
                        )
                    )
                }

                // Also show non-cancelled sales if income records have not been generated yet
                val validSales = perf.sales.filter {
                    !it.status.equals("no_deal", ignoreCase = true) &&
                            !it.status.equals("cancelled", ignoreCase = true)
                }
                if (rows.isEmpty() && validSales.isNotEmpty()) {
                    validSales.forEach { sale ->
                        val estCut = sale.value * 0.05
                        rows.add(
                            DownlineSalesReportItemUi(
                                clientName = sale.client,
                                dealCutPercent = "5%",
                                dateFormatted = formatDateDmy(sale.date, " - "),
                                amountFormatted = formatCurrency(if (estCut > 0) estCut else sale.value)
                            )
                        )
                    }
                    if (totalProfits == 0.0) {
                        totalProfits = validSales.sumOf { it.value * 0.05 }
                    }
                }

                salesReportItems = rows
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    DownlinePerformanceScreenContent(
        userName = session.user.name,
        profitsFormatted = formatCurrency(totalProfits),
        reportItems = salesReportItems,
        loading = loading,
        onBack = onBack,
        onNotificationBellClick = onOpenNotifications,
        onHome = onHome,
        onCreateProposal = onCreateProposal,
        onProfile = onOpenProfile
    )
}

@Composable
fun DownlinePerformanceScreenContent(
    userName: String,
    profitsFormatted: String,
    reportItems: List<DownlineSalesReportItemUi>,
    loading: Boolean = false,
    onBack: () -> Unit = {},
    onNotificationBellClick: () -> Unit = {},
    onHome: () -> Unit = {},
    onCreateProposal: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    IntegraModernDownlinePerformance(
        userName = userName,
        profitsFormatted = profitsFormatted,
        reportItems = reportItems,
        loading = loading,
        onBack = onBack,
        onNotificationBellClick = onNotificationBellClick,
        onHome = onHome,
        onCreateProposal = onCreateProposal,
        onProfile = onProfile
    )
}

// ==========================================
// 3. DOWNLINE CRM ("My CRM" - Active, Deals, Dropped)
// ==========================================

data class DownlineCrmCardUi(
    val id: Long,
    val clientName: String,
    val productName: String,
    val sinceDate: String,
    val downlineInCharge: String,
    val daysElapsed: Long,
    val dealWorthFormatted: String,
    val status: String,
    val stage: String = "negotiation",
    val dealValue: Double = 0.0
)

@Composable
private fun DownlineCrmRoute(
    session: LoginSession,
    refreshKey: Int,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onCreateProposal: () -> Unit,
    onOpenDealDetails: (DownlineCrmCardUi) -> Unit = {},
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onExpired: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var localRefresh by remember { mutableIntStateOf(0) }

    var totalPotentialValue by remember { mutableDoubleStateOf(0.0) }
    var numberOfClients by remember { mutableIntStateOf(0) }
    var activeCards by remember { mutableStateOf<List<DownlineCrmCardUi>>(emptyList()) }
    var dealCards by remember { mutableStateOf<List<DownlineCrmCardUi>>(emptyList()) }
    var droppedCards by remember { mutableStateOf<List<DownlineCrmCardUi>>(emptyList()) }

    val downlineChargeName = session.user.name.substringBefore(" ").ifBlank { "Agus" }

    LaunchedEffect(session.token, refreshKey, localRefresh) {
        loading = true
        ApiService.downlineCrm(session.token) { result ->
            loading = false
            result.onSuccess { payload ->
                totalPotentialValue = payload.totalPotentialValue
                numberOfClients = payload.numberOfClients

                fun mapItem(item: ApiService.CrmProjectItem): DownlineCrmCardUi {
                    val p = item.project
                    return DownlineCrmCardUi(
                        id = p.id,
                        clientName = p.clientName,
                        productName = item.productName,
                        sinceDate = formatDateDmy(p.dealDate ?: p.createdAt, "short"),
                        downlineInCharge = downlineChargeName,
                        daysElapsed = daysSince(p.createdAt),
                        dealWorthFormatted = formatCurrency(p.dealValue, useDotSeparator = true),
                        status = p.status,
                        stage = p.stage,
                        dealValue = p.dealValue
                    )
                }

                activeCards = payload.activeProjects.map(::mapItem)
                dealCards = payload.dealProjects.map(::mapItem)
                droppedCards = payload.droppedProjects.map(::mapItem)
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    DownlineCrmScreenContent(
        userName = session.user.name,
        totalPotentialFormatted = formatCurrency(totalPotentialValue),
        numberOfClients = numberOfClients,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        activeCards = activeCards,
        dealCards = dealCards,
        droppedCards = droppedCards,
        loading = loading,
        onViewDetails = { card -> onOpenDealDetails(card) },
        onBack = onBack,
        onNotificationBellClick = onOpenNotifications,
        onHome = onHome,
        onCreateProposal = onCreateProposal,
        onProfile = onOpenProfile
    )
}

@Composable
fun DownlineCrmScreenContent(
    userName: String,
    totalPotentialFormatted: String,
    numberOfClients: Int,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    activeCards: List<DownlineCrmCardUi>,
    dealCards: List<DownlineCrmCardUi>,
    droppedCards: List<DownlineCrmCardUi>,
    loading: Boolean = false,
    onViewDetails: (DownlineCrmCardUi) -> Unit = {},
    onBack: () -> Unit = {},
    onNotificationBellClick: () -> Unit = {},
    onHome: () -> Unit = {},
    onCreateProposal: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    IntegraModernDownlineCrm(
        userName = userName,
        totalPotentialFormatted = totalPotentialFormatted,
        numberOfClients = numberOfClients,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        activeCards = activeCards,
        dealCards = dealCards,
        droppedCards = droppedCards,
        loading = loading,
        onViewDetails = onViewDetails,
        onBack = onBack,
        onNotificationBellClick = onNotificationBellClick,
        onHome = onHome,
        onCreateProposal = onCreateProposal,
        onProfile = onProfile
    )
}

@Composable
private fun CrmProjectCardItem(
    card: DownlineCrmCardUi,
    tabIndex: Int,
    onViewDetails: () -> Unit
) {
    val accentColor = when (tabIndex) {
        0 -> when {
            card.daysElapsed > 30 -> RedAccent
            card.daysElapsed >= 7 -> OliveAccent
            else -> GreenAccent
        }
        else -> Color.Black
    }

    val badgeBg = when {
        card.daysElapsed > 30 -> BadgeRed
        card.daysElapsed >= 7 -> BadgeYellow
        else -> BadgeGreen
    }

    val badgeLabel = when {
        card.daysElapsed > 30 -> "> 30 days"
        card.daysElapsed >= 7 -> "${card.daysElapsed} days"
        else -> "${card.daysElapsed} Days"
    }

    LeftAccentCard(
        accentColor = accentColor,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 14.dp,
        leftBorderWidth = if (tabIndex == 0) 3.dp else 1.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.clientName,
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Product: ${card.productName}",
                    color = Color(0xFFB5B5B5),
                    fontSize = 9.sp,
                    lineHeight = 11.sp
                )
                Text(
                    text = "Since: ${card.sinceDate}",
                    color = Color(0xFFB5B5B5),
                    fontSize = 9.sp,
                    lineHeight = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Downline in Charge: ${card.downlineInCharge}",
                    color = Color(0xFFB5B5B5),
                    fontSize = 9.sp,
                    lineHeight = 11.sp
                )
            }

            // Right Action & Status Column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.heightIn(min = 72.dp)
            ) {
                when (tabIndex) {
                    0 -> {
                        // Active tab: Colored age pill badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(badgeBg)
                                .padding(horizontal = 14.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badgeLabel,
                                color = Color.Black,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    1 -> {
                        // Deals tab: "Deals Worth" + "$50.000"
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Deals Worth",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                text = card.dealWorthFormatted,
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    else -> {
                        // Dropped tab: empty top-right space
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Gray "View Details" pill button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFD9D9D9))
                        .clickable { onViewDetails() }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "View Details",
                        color = Color.Black,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CrmProjectDetailDialog(
    session: LoginSession,
    card: DownlineCrmCardUi,
    onDismiss: () -> Unit,
    onProjectMutated: () -> Unit,
    onExpired: () -> Unit
) {
    var detail by remember { mutableStateOf<ProjectDetailResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var actionBusy by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(card.id) {
        if (card.id <= 0) {
            loading = false
            return@LaunchedEffect
        }
        loading = true
        ApiService.projectDetail(session.token, card.id) { result ->
            loading = false
            result.onSuccess { detail = it }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
                else errorMsg = err.message ?: "Failed to load details"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = card.clientName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Product: ${card.productName}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Since: ${card.sinceDate}", fontSize = 12.sp, color = Color.DarkGray)
                Text("Downline in Charge: ${card.downlineInCharge}", fontSize = 12.sp, color = Color.DarkGray)
                Text("Value: ${card.dealWorthFormatted}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Status: ${card.status.uppercase()} (${card.stage.replace('_', ' ')})", fontSize = 12.sp)

                detail?.project?.let { p ->
                    if (!p.clientPhone.isNullOrBlank()) {
                        Text("Phone: ${p.clientPhone}", fontSize = 12.sp)
                    }
                    if (!p.clientEmail.isNullOrBlank()) {
                        Text("Email: ${p.clientEmail}", fontSize = 12.sp)
                    }
                }

                if (loading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }

                val activities = detail?.activities.orEmpty()
                if (activities.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Recent Activity:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    activities.take(3).forEach { act ->
                        Text(
                            text = "• ${act.activityDescription}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                if (errorMsg.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMsg, color = Color.Red, fontSize = 11.sp)
                }

                if (card.status == "follow_up" && card.id > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                actionBusy = true
                                ApiService.requestDealApproval(session.token, card.id) { res ->
                                    actionBusy = false
                                    res.onSuccess { onProjectMutated() }
                                    res.onFailure { errorMsg = it.message ?: "Request failed" }
                                }
                            },
                            enabled = !actionBusy,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("Request Deal", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                actionBusy = true
                                ApiService.updateProjectStatus(
                                    session.token,
                                    card.id,
                                    "no_deal",
                                    "Dropped from CRM"
                                ) { res ->
                                    actionBusy = false
                                    res.onSuccess { onProjectMutated() }
                                    res.onFailure { errorMsg = it.message ?: "Update failed" }
                                }
                            },
                            enabled = !actionBusy,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("Drop Deal", fontSize = 11.sp, color = RedAccent)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// ==========================================
// 4. NEW PROPOSAL ("New Deals Proposal")
// ==========================================

@Composable
private fun DownlineNewProposalRoute(
    session: LoginSession,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onDealCreated: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onExpired: () -> Unit
) {
    val context = LocalContext.current
    var masterItems by remember { mutableStateOf<List<MasterItemDto>>(emptyList()) }
    var submitting by remember { mutableStateOf(false) }

    LaunchedEffect(session.token) {
        ApiService.getMasterItems(session.token) { result ->
            result.onSuccess { items ->
                masterItems = items
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    DownlineNewProposalScreenContent(
        userName = session.user.name,
        productItems = masterItems,
        submitting = submitting,
        onBack = onBack,
        onNotificationBellClick = onOpenNotifications,
        onHome = onHome,
        onProfile = onOpenProfile,
        onSubmitDeal = { clientName, clientAddress, clientEmail, clientPhone, selectedItem, productTypeLabel, isDirectDeal, firstOfferStr, productDetails ->
            val offerVal = firstOfferStr.replace(",", "").replace("$", "").trim().toDoubleOrNull() ?: 0.0
            when {
                clientName.isBlank() -> {
                    Toast.makeText(context, "Client name is required", Toast.LENGTH_SHORT).show()
                }
                offerVal <= 0.0 -> {
                    Toast.makeText(context, "The initial product offer must be greater than 0", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    submitting = true
                    ApiService.createNewProposalDeal(
                        token = session.token,
                        clientName = clientName,
                        clientAddress = clientAddress,
                        clientEmail = clientEmail,
                        clientPhone = clientPhone,
                        selectedItem = selectedItem,
                        productTypeName = productTypeLabel,
                        isDirectDeal = isDirectDeal,
                        firstOffer = offerVal,
                        productDetails = productDetails
                    ) { result ->
                        submitting = false
                        result.onSuccess {
                            Toast.makeText(
                                context,
                                if (isDirectDeal) "Direct Deal & Proposal Created!" else "Follow-Up Deal & Proposal Created!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onDealCreated()
                        }
                        result.onFailure { err ->
                            if (err is ApiError && err.unauthorized) {
                                onExpired()
                            } else {
                                Toast.makeText(
                                    context,
                                    err.message ?: "Failed to create deal proposal",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun DownlineNewProposalScreenContent(
    userName: String,
    productItems: List<MasterItemDto> = emptyList(),
    submitting: Boolean = false,
    onBack: () -> Unit = {},
    onNotificationBellClick: () -> Unit = {},
    onHome: () -> Unit = {},
    onProfile: () -> Unit = {},
    onSubmitDeal: (
        clientName: String,
        clientAddress: String,
        clientEmail: String,
        clientPhone: String,
        selectedItem: MasterItemDto?,
        productTypeLabel: String,
        isDirectDeal: Boolean,
        firstOffer: String,
        productDetails: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _ -> }
) {
    IntegraModernDownlineNewProposal(
        userName = userName,
        productItems = productItems,
        submitting = submitting,
        onBack = onBack,
        onNotificationBellClick = onNotificationBellClick,
        onHome = onHome,
        onProfile = onProfile,
        onSubmitDeal = onSubmitDeal
    )
}

@Composable
private fun ProposalFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = Color.Black,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(InputFieldGray)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = Color.Black, fontSize = 11.sp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ==========================================
// PROFILE & NOTIFICATIONS DIALOGS
// ==========================================

@Composable
private fun DownlineProfileDialog(
    session: LoginSession,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    onExpired: () -> Unit
) {
    var profile by remember { mutableStateOf<UserProfileDto?>(null) }
    var name by remember { mutableStateOf(session.user.name) }
    var phone by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(session.token) {
        ApiService.getProfile(session.token) { result ->
            result.onSuccess { p ->
                profile = p
                name = p.name
                phone = p.phoneNumber.orEmpty()
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Downline Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Agent Code: ${profile?.agentCode ?: session.user.agentCode}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Email: ${profile?.email ?: session.user.email}", fontSize = 12.sp, color = Color.DarkGray)
                profile?.ratePercent?.let { rate ->
                    Text("Commission Rate: $rate%", fontSize = 12.sp, color = GreenAccent, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.textButtonColors(contentColor = RedAccent)
                ) {
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        saving = true
                        ApiService.updateProfile(session.token, name, phone) { res ->
                            saving = false
                            res.onSuccess {
                                Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                            res.onFailure {
                                onDismiss()
                            }
                        }
                    },
                    enabled = !saving,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DownlineNotificationsDialog(
    session: LoginSession,
    onDismiss: () -> Unit,
    onExpired: () -> Unit
) {
    var list by remember { mutableStateOf<List<NotificationDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(session.token) {
        loading = true
        ApiService.downlineHome(session.token) { res ->
            loading = false
            res.onSuccess { list = it.notifications }
            res.onFailure { if (it is ApiError && it.unauthorized) onExpired() }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else if (list.isEmpty()) {
                    Text("No new notifications.", fontSize = 13.sp, color = Color.Gray)
                } else {
                    list.forEach { n ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Text(n.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(n.message, fontSize = 12.sp, color = Color.DarkGray)
                            Text(formatDateDmy(n.createdAt, "-"), fontSize = 10.sp, color = Color.Gray)
                            HorizontalDivider(modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// ==========================================
// COMPOSE PREVIEWS MATCHING THE 3 PICTURES
// ==========================================

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun DownlineHomePreview() {
    DownlineHomeScreenContent(
        userName = "XXX",
        monthlyCutFormatted = "$20,000",
        projectsHeld = 8,
        uplineName = "Bruce Wayne",
        uplineContactNo = "08XXXXXXXX",
        notifications = listOf(
            DownlineNotificationItemUi(
                id = 1,
                title = "Upline Sent a Notification",
                message = "Lorem ipsum dolor sit amet, consecte...............",
                date = "08-11-2025",
                isCheckIcon = false
            ),
            DownlineNotificationItemUi(
                id = 2,
                title = "Client Wants to meet",
                message = "Lorem ipsum dolor sit amet, consecte...............",
                date = "08-11-2025",
                isCheckIcon = true
            )
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun DownlinePerformancePreview() {
    val sampleSales = listOf(
        DownlineSalesReportItemUi("PT Awasuisi", "5%", "XX - XX - XXXX", "$500"),
        DownlineSalesReportItemUi("PT Awasuisi", "5%", "XX - XX - XXXX", "$500"),
        DownlineSalesReportItemUi("PT Awasuisi", "5%", "XX - XX - XXXX", "$500")
    )
    DownlinePerformanceScreenContent(
        userName = "XXX",
        profitsFormatted = "$1,500",
        reportItems = sampleSales
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun DownlineCrmActivePreview() {
    val sampleActive = listOf(
        DownlineCrmCardUi(1, "PT Hakumanatata", "Nursery Program", "18-2-2026", "Agus", 35, "$50.000", "follow_up"),
        DownlineCrmCardUi(2, "PT Hakumanatata", "Nursery Program", "18-2-2026", "Agus", 10, "$50.000", "follow_up"),
        DownlineCrmCardUi(3, "PT Hakumanatata", "Nursery Program", "18-2-2026", "Agus", 2, "$50.000", "follow_up")
    )
    DownlineCrmScreenContent(
        userName = "XXX",
        totalPotentialFormatted = "$20,000",
        numberOfClients = 8,
        selectedTab = 0,
        onTabSelected = {},
        activeCards = sampleActive,
        dealCards = emptyList(),
        droppedCards = emptyList()
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun DownlineCrmDealsPreview() {
    val sampleDeals = listOf(
        DownlineCrmCardUi(1, "PT Hakumanatata", "Nursery Program", "18-2-2026", "Agus", 10, "$50.000", "deal"),
        DownlineCrmCardUi(2, "PT Hakumanatata", "Nursery Program", "18-2-2026", "Agus", 10, "$50.000", "deal"),
        DownlineCrmCardUi(3, "PT Hakumanatata", "Nursery Program", "18-2-2026", "Agus", 10, "$50.000", "deal")
    )
    DownlineCrmScreenContent(
        userName = "XXX",
        totalPotentialFormatted = "$20,000",
        numberOfClients = 8,
        selectedTab = 1,
        onTabSelected = {},
        activeCards = emptyList(),
        dealCards = sampleDeals,
        droppedCards = emptyList()
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun DownlineCrmDroppedPreview() {
    val sampleDropped = listOf(
        DownlineCrmCardUi(1, "PT Hakumanatata", "Nursery Program", "18-2-2026", "Agus", 10, "$50.000", "no_deal"),
        DownlineCrmCardUi(2, "PT Hakumanatata", "Nursery Program", "18-2-2026", "Agus", 10, "$50.000", "no_deal"),
        DownlineCrmCardUi(3, "PT Hakumanatata", "Nursery Program", "18-2-2026", "Agus", 10, "$50.000", "no_deal")
    )
    DownlineCrmScreenContent(
        userName = "XXX",
        totalPotentialFormatted = "$20,000",
        numberOfClients = 8,
        selectedTab = 2,
        onTabSelected = {},
        activeCards = emptyList(),
        dealCards = emptyList(),
        droppedCards = sampleDropped
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun DownlineNewProposalPreview() {
    DownlineNewProposalScreenContent(
        userName = "XXX"
    )
}
