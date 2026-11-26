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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val UplineBgGray = Color(0xFFE5E5E5)
private val UplinePrimaryBlue = Color(0xFF0048FF)
private val UplineDarkBlue = Color(0xFF0014A8)
private val UplineGreen = Color(0xFF1B8A2A)
private val UplineRed = Color(0xFFD92121)
private val UplineOlive = Color(0xFFA0A818)
private val UplineFieldGray = Color(0xFFD9D9D9)

private fun uplineMoney(value: Double, useDot: Boolean = false): String {
    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }
    val formatted = nf.format(value)
    return if (useDot) "$${formatted.replace(',', '.')}" else "$$formatted"
}

private fun uplineDate(raw: String?, sep: String = "-"): String {
    if (raw.isNullOrBlank()) return "08${sep}11${sep}2025"
    val clean = raw.take(10)
    val parts = clean.split("-")
    return if (parts.size == 3) {
        if (sep == "short") {
            "${parts[2].trimStart('0').ifEmpty { "1" }}-${parts[1].trimStart('0').ifEmpty { "1" }}-${parts[0]}"
        } else {
            "${parts[2]}$sep${parts[1]}$sep${parts[0]}"
        }
    } else clean
}

private fun uplineDaysSince(rawDate: String?): Long {
    if (rawDate.isNullOrBlank()) return 2L
    return try {
        val parsed = LocalDate.parse(rawDate.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
        ChronoUnit.DAYS.between(parsed, LocalDate.now(ZoneId.systemDefault())).coerceAtLeast(1L)
    } catch (_: Exception) {
        try {
            val parsed = Instant.parse(rawDate).atZone(ZoneId.systemDefault()).toLocalDate()
            ChronoUnit.DAYS.between(parsed, LocalDate.now()).coerceAtLeast(1L)
        } catch (_: Exception) {
            2L
        }
    }
}

enum class UplinePage {
    HOME,
    PERFORMANCE,
    DOWNLINES,
    CRM,
    NEW_DEAL,
    DEAL_DETAILS,
    PROFILE
}

@Composable
fun UplineAppScreen(
    session: LoginSession,
    onLogout: () -> Unit,
    onExpired: () -> Unit
) {
    var currentPage by rememberSaveable { mutableStateOf(UplinePage.HOME) }
    var previousPage by rememberSaveable { mutableStateOf(UplinePage.HOME) }
    var selectedDeal by remember { mutableStateOf(DealDetailSelection()) }
    var refreshKey by remember { mutableIntStateOf(0) }

    when (currentPage) {
        UplinePage.HOME -> {
            UplineHomeRoute(
                session = session,
                refreshKey = refreshKey,
                onNavigatePerformance = { currentPage = UplinePage.PERFORMANCE },
                onNavigateDownlines = { currentPage = UplinePage.DOWNLINES },
                onNavigateCrm = { currentPage = UplinePage.CRM },
                onNavigateAddProject = { currentPage = UplinePage.NEW_DEAL },
                onOpenDealDetails = { sel ->
                    selectedDeal = sel
                    previousPage = UplinePage.HOME
                    currentPage = UplinePage.DEAL_DETAILS
                },
                onOpenProfile = {
                    previousPage = UplinePage.HOME
                    currentPage = UplinePage.PROFILE
                },
                onExpired = onExpired
            )
        }

        UplinePage.PERFORMANCE -> {
            UplinePerformanceRouteNew(
                session = session,
                onBack = { currentPage = UplinePage.HOME },
                onHome = { currentPage = UplinePage.HOME },
                onAddProject = { currentPage = UplinePage.NEW_DEAL },
                onOpenDealDetails = { sel ->
                    selectedDeal = sel
                    previousPage = UplinePage.PERFORMANCE
                    currentPage = UplinePage.DEAL_DETAILS
                },
                onOpenProfile = {
                    previousPage = UplinePage.PERFORMANCE
                    currentPage = UplinePage.PROFILE
                },
                onExpired = onExpired
            )
        }

        UplinePage.DOWNLINES -> {
            UplineDownlinesRouteNew(
                session = session,
                onBack = { currentPage = UplinePage.HOME },
                onHome = { currentPage = UplinePage.HOME },
                onAddProject = { currentPage = UplinePage.NEW_DEAL },
                onOpenProfile = {
                    previousPage = UplinePage.DOWNLINES
                    currentPage = UplinePage.PROFILE
                },
                onExpired = onExpired
            )
        }

        UplinePage.CRM -> {
            UplineCrmRouteNew(
                session = session,
                refreshKey = refreshKey,
                onBack = { currentPage = UplinePage.HOME },
                onHome = { currentPage = UplinePage.HOME },
                onAddProject = { currentPage = UplinePage.NEW_DEAL },
                onOpenDealDetails = { sel ->
                    selectedDeal = sel
                    previousPage = UplinePage.CRM
                    currentPage = UplinePage.DEAL_DETAILS
                },
                onOpenProfile = {
                    previousPage = UplinePage.CRM
                    currentPage = UplinePage.PROFILE
                },
                onExpired = onExpired
            )
        }

        UplinePage.NEW_DEAL -> {
            UplineNewDealsRoute(
                session = session,
                onBack = { currentPage = UplinePage.HOME },
                onHome = { currentPage = UplinePage.HOME },
                onDealCreated = {
                    refreshKey++
                    currentPage = UplinePage.CRM
                },
                onOpenProfile = {
                    previousPage = UplinePage.NEW_DEAL
                    currentPage = UplinePage.PROFILE
                },
                onExpired = onExpired
            )
        }

        UplinePage.DEAL_DETAILS -> {
            GlobalDealsDetailsRoute(
                session = session,
                selection = selectedDeal,
                centerBarLabel = "add project",
                onBack = { currentPage = previousPage },
                onHome = { currentPage = UplinePage.HOME },
                onCenterAction = { currentPage = UplinePage.NEW_DEAL },
                onProfile = { currentPage = UplinePage.PROFILE },
                onExpired = onExpired
            )
        }

        UplinePage.PROFILE -> {
            GlobalProfileRoute(
                session = session,
                centerBarLabel = "add project",
                onBack = { currentPage = previousPage },
                onHome = { currentPage = UplinePage.HOME },
                onCenterAction = { currentPage = UplinePage.NEW_DEAL },
                onLogout = onLogout,
                onExpired = onExpired
            )
        }
    }
}

// ==========================================
// 1. UPLINE HOME SCREEN (Image 4)
// ==========================================

data class UplineActiveDealUi(
    val id: Long,
    val clientName: String,
    val amountFormatted: String,
    val rawAmount: Double
)

data class UplineRecentActivityUi(
    val id: Long,
    val title: String,
    val clientSubtitle: String,
    val detailsText: String,
    val dateFormatted: String
)

@Composable
private fun UplineHomeRoute(
    session: LoginSession,
    refreshKey: Int,
    onNavigatePerformance: () -> Unit,
    onNavigateDownlines: () -> Unit,
    onNavigateCrm: () -> Unit,
    onNavigateAddProject: () -> Unit,
    onOpenDealDetails: (DealDetailSelection) -> Unit,
    onOpenProfile: () -> Unit,
    onExpired: () -> Unit
) {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var localRefresh by remember { mutableIntStateOf(0) }
    var monthlyCut by remember { mutableDoubleStateOf(0.0) }
    var projectsHeld by remember { mutableIntStateOf(0) }
    var activeDeals by remember { mutableStateOf<List<UplineActiveDealUi>>(emptyList()) }
    var activities by remember { mutableStateOf<List<UplineRecentActivityUi>>(emptyList()) }
    var counterOfferDeal by remember { mutableStateOf<UplineActiveDealUi?>(null) }
    var counterOfferInput by remember { mutableStateOf("") }

    LaunchedEffect(session.token, refreshKey, localRefresh) {
        loading = true
        ApiService.uplineHome(session.token) { result ->
            loading = false
            result.onSuccess { payload ->
                monthlyCut = payload.dashboard.monthlyCut
                projectsHeld = payload.dashboard.projectsHeld
                activeDeals = payload.dashboard.activeDeals.map { d ->
                    UplineActiveDealUi(
                        id = d.id,
                        clientName = d.clientName,
                        amountFormatted = uplineMoney(d.amount),
                        rawAmount = d.amount
                    )
                }

                val mappedActs = if (payload.resolvedActivities.isNotEmpty()) {
                    payload.resolvedActivities.map { act ->
                        UplineRecentActivityUi(
                            id = act.projectId,
                            title = "Activity update",
                            clientSubtitle = act.clientName,
                            detailsText = "Details: ${act.description}",
                            dateFormatted = uplineDate(act.time, "-")
                        )
                    }
                } else {
                    payload.dashboard.activities.mapIndexed { idx, act ->
                        val resolvedClient = ApiService.resolveClientName(
                            rawClientName = null,
                            projectId = idx.toLong(),
                            agentCode = act.agentCode
                        )
                        UplineRecentActivityUi(
                            id = idx.toLong(),
                            title = "Activity update",
                            clientSubtitle = resolvedClient,
                            detailsText = "Details: ${act.description}",
                            dateFormatted = uplineDate(act.time, "-")
                        )
                    }
                }

                activities = mappedActs
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    UplineHomeScreenContentNew(
        userName = session.user.name,
        monthlyCutFormatted = uplineMoney(monthlyCut),
        projectsHeld = projectsHeld,
        activeDeals = activeDeals,
        activities = activities,
        onPerformanceClick = onNavigatePerformance,
        onDownlineClick = onNavigateDownlines,
        onCrmClick = onNavigateCrm,
        onDealActionClick = { deal, action ->
            if (deal.id <= 0) return@UplineHomeScreenContentNew
            when (action) {
                "no_deal" -> {
                    ApiService.updateProjectStatus(session.token, deal.id, "no_deal", "No deal by Upline") { result ->
                        result.onSuccess {
                            Toast.makeText(context, "Marked as No Deal", Toast.LENGTH_SHORT).show()
                            localRefresh++
                        }.onFailure { error ->
                            Toast.makeText(context, error.message ?: "Failed to update status", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                "counter" -> {
                    counterOfferDeal = deal
                    counterOfferInput = deal.rawAmount.toLong().toString()
                }
                "deal" -> {
                    ApiService.requestDealApproval(session.token, deal.id) { res ->
                        res.onSuccess {
                            Toast.makeText(context, "Deal approval requested!", Toast.LENGTH_SHORT).show()
                            localRefresh++
                        }
                        res.onFailure { e ->
                            Toast.makeText(context, e.message ?: "Request failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        },
        onSeeActivityDetails = { act ->
            onOpenDealDetails(
                DealDetailSelection(
                    projectId = act.id,
                    clientName = act.clientSubtitle,
                    dealDetails = act.detailsText
                )
            )
        },
        onHome = {},
        onAddProject = onNavigateAddProject,
        onProfile = onOpenProfile
    )

    counterOfferDeal?.let { deal ->
        AlertDialog(
            onDismissRequest = { counterOfferDeal = null },
            title = { Text("Counter Offer - ${deal.clientName}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = counterOfferInput,
                    onValueChange = { counterOfferInput = it },
                    label = { Text("New Deal Value ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val v = counterOfferInput.toDoubleOrNull() ?: 0.0
                        if (v > 0 && deal.id > 0) {
                            ApiService.counterOffer(session.token, deal.id, v, "Counter offer from Upline") { result ->
                                result.onSuccess {
                                    counterOfferDeal = null
                                    localRefresh++
                                    Toast.makeText(context, "Counter offer submitted!", Toast.LENGTH_SHORT).show()
                                }.onFailure { error ->
                                    Toast.makeText(context, error.message ?: "Counteroffer failed", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { counterOfferDeal = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UplineHomeScreenContentNew(
    userName: String,
    monthlyCutFormatted: String,
    projectsHeld: Int,
    activeDeals: List<UplineActiveDealUi>,
    activities: List<UplineRecentActivityUi>,
    onPerformanceClick: () -> Unit = {},
    onDownlineClick: () -> Unit = {},
    onCrmClick: () -> Unit = {},
    onDealActionClick: (UplineActiveDealUi, String) -> Unit = { _, _ -> },
    onSeeActivityDetails: (UplineRecentActivityUi) -> Unit = {},
    onHome: () -> Unit = {},
    onAddProject: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    IntegraModernUplineHome(
        userName, monthlyCutFormatted, projectsHeld, activeDeals, activities,
        onPerformanceClick, onDownlineClick, onCrmClick, onDealActionClick,
        onSeeActivityDetails, onHome, onAddProject, onProfile
    )
}

@Composable
private fun UplineDealPill(
    text: String,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ==========================================
// 2. UPLINE'S MY DOWNLINES SCREEN (Image 1)
// ==========================================

data class UplineDownlineAgentUi(
    val id: Long,
    val name: String,
    val joinedDate: String,
    val phoneNumber: String,
    val totalDeals: Int,
    val performanceRatio: String,
    val hasNewProposal: Boolean = false
)

@Composable
private fun UplineDownlinesRouteNew(
    session: LoginSession,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onAddProject: () -> Unit,
    onOpenProfile: () -> Unit,
    onExpired: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var downlineCount by remember { mutableIntStateOf(0) }
    var totalDeals by remember { mutableIntStateOf(0) }
    var agents by remember { mutableStateOf<List<UplineDownlineAgentUi>>(emptyList()) }
    var selectedAgent by remember { mutableStateOf<UplineDownlineAgentUi?>(null) }

    LaunchedEffect(session.token) {
        loading = true
        ApiService.uplineDownlines(session.token) { result ->
            loading = false
            result.onSuccess { res ->
                downlineCount = res.summary.downlineCount
                totalDeals = res.summary.totalDeals
                agents = res.downlines.mapIndexed { idx, d ->
                    UplineDownlineAgentUi(
                        id = d.id,
                        name = d.agentCode.ifBlank { "Unknown agent" },
                        joinedDate = uplineDate(d.joinedAt, "-"),
                        phoneNumber = "Agent Code: ${d.agentCode}",
                        totalDeals = d.totalDeals,
                        performanceRatio = "${d.dealsPerMonth} deals/month",
                        hasNewProposal = false
                    )
                }
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    UplineDownlinesScreenContentNew(
        userName = session.user.name,
        downlineCount = downlineCount,
        totalTeamDeals = totalDeals,
        agents = agents,
        loading = loading,
        onViewAgentDetails = { selectedAgent = it },
        onBack = onBack,
        onHome = onHome,
        onAddProject = onAddProject,
        onProfile = onOpenProfile
    )

    selectedAgent?.let { agent ->
        AlertDialog(
            onDismissRequest = { selectedAgent = null },
            title = { Text(agent.name, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Joined: ${agent.joinedDate}", fontSize = 13.sp)
                    Text("Phone: ${agent.phoneNumber}", fontSize = 13.sp)
                    Text("Total Deals: ${agent.totalDeals}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Performance: ${agent.performanceRatio}", fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAgent = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun UplineDownlinesScreenContentNew(
    userName: String,
    downlineCount: Int,
    totalTeamDeals: Int,
    agents: List<UplineDownlineAgentUi>,
    loading: Boolean = false,
    onViewAgentDetails: (UplineDownlineAgentUi) -> Unit = {},
    onBack: () -> Unit = {},
    onHome: () -> Unit = {},
    onAddProject: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    IntegraModernUplineDownlines(
        userName, downlineCount, totalTeamDeals, agents, loading,
        onViewAgentDetails, onBack, onHome, onAddProject, onProfile
    )
}

// ==========================================
// 3. UPLINE MY PERFORMANCE SCREEN (Image 2)
// ==========================================

data class UplineSalesCardUi(
    val projectCode: String,
    val clientName: String,
    val productName: String,
    val dateFormatted: String,
    val dealsWorthFormatted: String,
    val rawValue: Double
)

@Composable
private fun UplinePerformanceRouteNew(
    session: LoginSession,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onAddProject: () -> Unit,
    onOpenDealDetails: (DealDetailSelection) -> Unit,
    onOpenProfile: () -> Unit,
    onExpired: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var monthlyCut by remember { mutableDoubleStateOf(0.0) }
    var totalDealsMade by remember { mutableIntStateOf(0) }
    var salesCards by remember { mutableStateOf<List<UplineSalesCardUi>>(emptyList()) }
    var incomeCards by remember { mutableStateOf<List<DownlineSalesReportItemUi>>(emptyList()) }

    LaunchedEffect(session.token) {
        loading = true
        ApiService.performance(session.token) { result ->
            loading = false
            result.onSuccess { perf ->
                monthlyCut = if (perf.monthlyCut > 0) perf.monthlyCut else perf.totalIncome
                totalDealsMade = if (perf.totalDealsMade > 0) {
                    perf.totalDealsMade
                } else {
                    perf.sales.count { it.status.equals("deal", ignoreCase = true) }
                }

                salesCards = perf.sales.filter {
                    !it.status.equals("no_deal", ignoreCase = true) &&
                            !it.status.equals("cancelled", ignoreCase = true)
                }.map { s ->
                    UplineSalesCardUi(
                        projectCode = s.code,
                        clientName = s.client,
                        productName = "Sales project",
                        dateFormatted = uplineDate(s.date, "-"),
                        dealsWorthFormatted = uplineMoney(s.value),
                        rawValue = s.value
                    )
                }

                incomeCards = perf.income.map { inc ->
                    val rateStr = if (inc.rate % 1.0 == 0.0) "${inc.rate.toInt()}%" else "${inc.rate}%"
                    DownlineSalesReportItemUi(
                        clientName = inc.client,
                        dealCutPercent = rateStr,
                        dateFormatted = uplineDate(inc.date, " - "),
                        amountFormatted = uplineMoney(inc.commission)
                    )
                }
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    UplinePerformanceScreenContentNew(
        userName = session.user.name,
        thisMonthsCutFormatted = uplineMoney(monthlyCut),
        totalDealsMade = totalDealsMade,
        salesList = salesCards,
        incomeList = incomeCards,
        loading = loading,
        onViewSaleDetails = { sale ->
            onOpenDealDetails(
                DealDetailSelection(
                    projectId = 0L,
                    clientName = sale.clientName,
                    productType = sale.productName,
                    initialPrice = sale.rawValue,
                    agentResponsible = session.user.name
                )
            )
        },
        onBack = onBack,
        onHome = onHome,
        onAddProject = onAddProject,
        onProfile = onOpenProfile
    )
}

@Composable
fun UplinePerformanceScreenContentNew(
    userName: String,
    thisMonthsCutFormatted: String,
    totalDealsMade: Int,
    salesList: List<UplineSalesCardUi>,
    incomeList: List<DownlineSalesReportItemUi>,
    loading: Boolean = false,
    initialTab: Int = 0,
    onViewSaleDetails: (UplineSalesCardUi) -> Unit = {},
    onBack: () -> Unit = {},
    onHome: () -> Unit = {},
    onAddProject: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    IntegraModernUplinePerformance(
        userName, thisMonthsCutFormatted, totalDealsMade, salesList, incomeList,
        loading, initialTab, onViewSaleDetails, onBack, onHome, onAddProject, onProfile
    )
}

// ==========================================
// 4. UPLINE CRM SCREEN (Image 3)
// ==========================================

@Composable
private fun UplineCrmRouteNew(
    session: LoginSession,
    refreshKey: Int,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onAddProject: () -> Unit,
    onOpenDealDetails: (DealDetailSelection) -> Unit,
    onOpenProfile: () -> Unit,
    onExpired: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var totalPotentialValue by remember { mutableDoubleStateOf(0.0) }
    var numberOfClients by remember { mutableIntStateOf(0) }
    var activeCards by remember { mutableStateOf<List<DownlineCrmCardUi>>(emptyList()) }
    var dealCards by remember { mutableStateOf<List<DownlineCrmCardUi>>(emptyList()) }
    var droppedCards by remember { mutableStateOf<List<DownlineCrmCardUi>>(emptyList()) }

    LaunchedEffect(session.token, refreshKey) {
        loading = true
        ApiService.uplineCrm(session.token) { result ->
            loading = false
            result.onSuccess { payload ->
                totalPotentialValue = payload.totalPotentialValue
                numberOfClients = payload.numberOfClients

                fun mapProject(item: ApiService.CrmProjectItem): DownlineCrmCardUi {
                    val p = item.project
                    return DownlineCrmCardUi(
                        id = p.id,
                        clientName = p.clientName,
                        productName = item.productName,
                        sinceDate = uplineDate(p.createdAt, "short"),
                        downlineInCharge = p.agentCode ?: "-",
                        daysElapsed = uplineDaysSince(p.createdAt),
                        dealWorthFormatted = uplineMoney(p.dealValue, useDot = true),
                        status = p.status,
                        stage = p.stage,
                        dealValue = p.dealValue
                    )
                }

                activeCards = payload.activeProjects.map(::mapProject)
                dealCards = payload.dealProjects.map(::mapProject)
                droppedCards = payload.droppedProjects.map(::mapProject)
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
            }
        }
    }

    UplineCrmScreenContentNew(
        userName = session.user.name,
        totalPotentialFormatted = uplineMoney(totalPotentialValue),
        numberOfClients = numberOfClients,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        activeCards = activeCards,
        dealCards = dealCards,
        droppedCards = droppedCards,
        loading = loading,
        onViewDetails = { card ->
            onOpenDealDetails(
                DealDetailSelection(
                    projectId = card.id,
                    clientName = card.clientName,
                    productType = card.productName,
                    dealType = if (card.status == "deal") "Direct Deal" else "Follow-Up",
                    initialPrice = card.dealValue,
                    agentResponsible = card.downlineInCharge
                )
            )
        },
        onBack = onBack,
        onHome = onHome,
        onAddProject = onAddProject,
        onProfile = onOpenProfile
    )
}

@Composable
fun UplineCrmScreenContentNew(
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
    onHome: () -> Unit = {},
    onAddProject: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    IntegraModernUplineCrm(
        userName, totalPotentialFormatted, numberOfClients, selectedTab,
        onTabSelected, activeCards, dealCards, droppedCards, loading,
        onViewDetails, onBack, onHome, onAddProject, onProfile
    )
}

// ==========================================
// 5. UPLINE NEW DEALS SCREEN (Image 5)
// ==========================================

@Composable
private fun UplineNewDealsRoute(
    session: LoginSession,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onDealCreated: () -> Unit,
    onOpenProfile: () -> Unit,
    onExpired: () -> Unit
) {
    val context = LocalContext.current
    var masterItems by remember { mutableStateOf<List<MasterItemDto>>(emptyList()) }
    var submitting by remember { mutableStateOf(false) }

    LaunchedEffect(session.token) {
        ApiService.getMasterItems(session.token) { result ->
            result.onSuccess { masterItems = it }
            result.onFailure { if (it is ApiError && it.unauthorized) onExpired() }
        }
    }

    UplineNewDealsScreenContent(
        userName = session.user.name,
        productItems = masterItems,
        submitting = submitting,
        onBack = onBack,
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
                            Toast.makeText(context, "New Deal Created!", Toast.LENGTH_SHORT).show()
                            onDealCreated()
                        }
                        result.onFailure { err ->
                            if (err is ApiError && err.unauthorized) onExpired()
                            else Toast.makeText(context, err.message ?: "Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun UplineNewDealsScreenContent(
    userName: String,
    productItems: List<MasterItemDto> = emptyList(),
    submitting: Boolean = false,
    onBack: () -> Unit = {},
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
    IntegraModernUplineNewDeal(
        userName, productItems, submitting, onBack, onHome, onProfile, onSubmitDeal
    )
}

@Composable
private fun UplineDealTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(UplineFieldGray)
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
// PREVIEWS FOR ALL 5 UPLINE SCREENS
// ==========================================

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun UplineHomeNewPreview() {
    UplineHomeScreenContentNew(
        userName = "XXX",
        monthlyCutFormatted = "$20,000",
        projectsHeld = 8,
        activeDeals = listOf(UplineActiveDealUi(1, "PT Surya Makmur", "$10,0000", 10000.0)),
        activities = listOf(
            UplineRecentActivityUi(1, "PT Integra Cipta Data accepted the offer", "PT Demo Sejahtera", "Details: Counter offer accepted", "08-11-2025"),
            UplineRecentActivityUi(2, "PT Integra Cipta Data accepted the offer", "PT Surya Makmur", "Details: Counter offer accepted", "08-11-2025")
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun UplineDownlinesNewPreview() {
    val sample = listOf(
        UplineDownlineAgentUi(1, "Travis Scott", "01-05-2016", "08XXXXXXXXX", 10, "2.4 deals/month", true),
        UplineDownlineAgentUi(2, "Travis Scott", "01-05-2016", "08XXXXXXXXX", 10, "2.4 deals/month", false),
        UplineDownlineAgentUi(3, "Travis Scott", "01-05-2016", "08XXXXXXXXX", 10, "2.4 deals/month", false)
    )
    UplineDownlinesScreenContentNew(
        userName = "XXX",
        downlineCount = 3,
        totalTeamDeals = 12,
        agents = sample
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun UplinePerformanceSalesNewPreview() {
    val sales = listOf(
        UplineSalesCardUi("PRJ-1", "PT Arunika Data", "Cashier Program", "XX- XX-XXXX", "$20,050", 20050.0),
        UplineSalesCardUi("PRJ-2", "PT Arunika Data", "Cashier Program", "XX- XX-XXXX", "$20,050", 20050.0),
        UplineSalesCardUi("PRJ-3", "PT Arunika Data", "Cashier Program", "XX- XX-XXXX", "$20,050", 20050.0)
    )
    UplinePerformanceScreenContentNew(
        userName = "XXX",
        thisMonthsCutFormatted = "$20,000",
        totalDealsMade = 10,
        salesList = sales,
        incomeList = emptyList(),
        initialTab = 0
    )
}
