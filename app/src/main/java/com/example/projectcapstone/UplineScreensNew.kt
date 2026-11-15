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
    var monthlyCut by remember { mutableDoubleStateOf(20000.0) }
    var projectsHeld by remember { mutableIntStateOf(8) }
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
                            title = "PT Integra Cipta Data accepted the offer",
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
                            title = "PT Integra Cipta Data accepted the offer",
                            clientSubtitle = resolvedClient,
                            detailsText = "Details: ${act.description}",
                            dateFormatted = uplineDate(act.time, "-")
                        )
                    }
                }

                activities = if (mappedActs.isNotEmpty()) {
                    mappedActs
                } else {
                    listOf(
                        UplineRecentActivityUi(
                            id = 0,
                            title = "PT Integra Cipta Data accepted the offer",
                            clientSubtitle = "PT Demo Sejahtera",
                            detailsText = "Details: Counter offer accepted",
                            dateFormatted = "08-11-2025"
                        ),
                        UplineRecentActivityUi(
                            id = 0,
                            title = "PT Integra Cipta Data accepted the offer",
                            clientSubtitle = "PT Surya Makmur",
                            detailsText = "Details: Counter offer accepted",
                            dateFormatted = "08-11-2025"
                        )
                    )
                }
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
                    ApiService.updateProjectStatus(session.token, deal.id, "no_deal", "No deal by Upline") {
                        Toast.makeText(context, "Marked as No Deal", Toast.LENGTH_SHORT).show()
                        localRefresh++
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
                            ApiService.counterOffer(session.token, deal.id, v, "Counter offer from Upline") {
                                counterOfferDeal = null
                                localRefresh++
                                Toast.makeText(context, "Counter offer submitted!", Toast.LENGTH_SHORT).show()
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
    Scaffold(
        containerColor = UplineBgGray,
        bottomBar = {
            RoleBottomBar(
                centerLabel = "add project",
                onHome = onHome,
                onCenterClick = onAddProject,
                onProfile = onProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(UplineBgGray)
        ) {
            RoleTopBar(
                roleTitle = "Upline",
                userName = userName
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // 1. Top Stats Container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LeftAccentCard(
                        accentColor = UplineGreen,
                        modifier = Modifier
                            .weight(1f)
                            .height(74.dp),
                        cornerRadius = 16.dp,
                        leftBorderWidth = 4.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "This Months Cut",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = monthlyCutFormatted,
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.End),
                                maxLines = 1
                            )
                        }
                    }

                    LeftAccentCard(
                        accentColor = UplineDarkBlue,
                        modifier = Modifier
                            .weight(1f)
                            .height(74.dp),
                        cornerRadius = 16.dp,
                        leftBorderWidth = 4.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Projects Held",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.TopStart)
                            )
                            Text(
                                text = projectsHeld.toString(),
                                color = Color.Black,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.align(Alignment.BottomEnd)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. my Upline menu Card (3 buttons)
                LeftAccentCard(
                    accentColor = UplineDarkBlue,
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    leftBorderWidth = 4.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "my Upline menu",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Top
                        ) {
                            // My Performance
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onPerformanceClick() },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(UplinePrimaryBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.StarBorder,
                                        contentDescription = "My Performance",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "My\nPerformance",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Downline
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onDownlineClick() },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(UplinePrimaryBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = "Downline",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Downline",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // My CRM
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onCrmClick() },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(UplinePrimaryBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "CRM",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "My CRM",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Active deals Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(text = "🤝", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Active deals",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val dealsToShow = activeDeals.ifEmpty {
                        listOf(
                            UplineActiveDealUi(
                                id = 0L,
                                clientName = "PT Surya Makmur",
                                amountFormatted = "$10,0000",
                                rawAmount = 10000.0
                            )
                        )
                    }

                    dealsToShow.forEach { deal ->
                        LeftAccentCard(
                            accentColor = UplineRed,
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 10.dp,
                            leftBorderWidth = 4.dp,
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = deal.clientName,
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Going for: ${deal.amountFormatted}",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // no deal pill
                                    UplineDealPill(
                                        text = "no deal",
                                        bgColor = Color(0xFFFFCDD2),
                                        borderColor = Color(0xFFE53935),
                                        textColor = Color(0xFFD32F2F)
                                    ) {
                                        onDealActionClick(deal, "no_deal")
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Counter pill
                                    UplineDealPill(
                                        text = "Counter",
                                        bgColor = Color(0xFFB3E5FC),
                                        borderColor = Color(0xFF039BE5),
                                        textColor = Color(0xFF0277BD)
                                    ) {
                                        onDealActionClick(deal, "counter")
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Deal pill
                                    UplineDealPill(
                                        text = "Deal",
                                        bgColor = Color(0xFFC8E6C9),
                                        borderColor = Color(0xFF43A047),
                                        textColor = Color(0xFF2E7D32)
                                    ) {
                                        onDealActionClick(deal, "deal")
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Recent downline Activity Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Recent downline Activity",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    activities.forEach { act ->
                        LeftAccentCard(
                            accentColor = UplineGreen,
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 10.dp,
                            leftBorderWidth = 4.dp,
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = act.title,
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = act.clientSubtitle,
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = act.detailsText,
                                    color = Color.Black,
                                    fontSize = 8.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = "Date : ${act.dateFormatted}",
                                        color = Color.Black,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(1.dp, Color.Black, RoundedCornerShape(6.dp))
                                            .background(Color.White)
                                            .clickable { onSeeActivityDetails(act) }
                                            .padding(horizontal = 14.dp, vertical = 3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "See Details",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
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
    var downlineCount by remember { mutableIntStateOf(3) }
    var totalDeals by remember { mutableIntStateOf(12) }
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
                        name = d.agentCode.ifBlank { "Travis Scott" },
                        joinedDate = uplineDate(d.joinedAt, "-"),
                        phoneNumber = "08XXXXXXXXX",
                        totalDeals = d.totalDeals,
                        performanceRatio = "${d.dealsPerMonth} deals/month",
                        hasNewProposal = idx == 0
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
    var search by rememberSaveable { mutableStateOf("") }

    Scaffold(
        containerColor = UplineBgGray,
        bottomBar = {
            RoleBottomBar(
                centerLabel = "add project",
                onHome = onHome,
                onCenterClick = onAddProject,
                onProfile = onProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(UplineBgGray)
        ) {
            RoleTopBar(roleTitle = "Upline", userName = userName)

            // Sub-header: Blue Back Button + "My Downlines"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(UplinePrimaryBlue)
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

                Text(
                    text = "My Downlines",
                    color = Color.Black,
                    fontSize = 19.sp
                )
            }

            // Main White Rounded Container with Black Border
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Team Performance Ratio Card
                LeftAccentCard(
                    accentColor = UplineDarkBlue,
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    leftBorderWidth = 4.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Team Performance\nRatio",
                            color = Color.Black,
                            fontSize = 14.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "$downlineCount Downline\n$totalTeamDeals deals",
                            color = Color.Black,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(UplineFieldGray)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (search.isEmpty()) {
                            Text(
                                text = "search downline",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                        BasicTextField(
                            value = search,
                            onValueChange = { search = it },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.Black, fontSize = 10.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Agent Directory",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                HorizontalDivider(color = Color(0xFFCCCCCC), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                val filtered = agents.filter {
                    it.name.contains(search, ignoreCase = true)
                }

                if (loading && filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = UplinePrimaryBlue)
                    }
                } else {
                    filtered.forEach { agent ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0E6EE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = Color(0xFF455A64),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = agent.name,
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "joined: ${agent.joinedDate}",
                                        color = Color.DarkGray,
                                        fontSize = 8.sp,
                                        lineHeight = 10.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Phone number: ${agent.phoneNumber}",
                                            color = Color.DarkGray,
                                            fontSize = 8.sp,
                                            lineHeight = 10.sp
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = Color.DarkGray,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }

                                if (agent.hasNewProposal) {
                                    Text(
                                        text = "1 new proposal!",
                                        color = UplineRed,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Two gray stat boxes: Total Deals & Performance
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE0E0E0))
                                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Total Deals",
                                        color = Color.Black,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.TopStart)
                                    )
                                    Text(
                                        text = agent.totalDeals.toString(),
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE0E0E0))
                                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Performance",
                                        color = Color.Black,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.TopStart)
                                    )
                                    Text(
                                        text = agent.performanceRatio,
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFD6D6D6))
                                    .clickable { onViewAgentDetails(agent) }
                                    .padding(horizontal = 18.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "View Details",
                                    color = Color.Black,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
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
    var monthlyCut by remember { mutableDoubleStateOf(20000.0) }
    var totalDealsMade by remember { mutableIntStateOf(10) }
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
                        productName = "Cashier Program",
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
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }

    Scaffold(
        containerColor = UplineBgGray,
        bottomBar = {
            RoleBottomBar(
                centerLabel = "add project",
                onHome = onHome,
                onCenterClick = onAddProject,
                onProfile = onProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(UplineBgGray)
        ) {
            RoleTopBar(roleTitle = "Upline", userName = userName)

            // Sub-header: Blue Back Button + "My Performance"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(UplinePrimaryBlue)
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

                Text(
                    text = "My Performance",
                    color = Color.Black,
                    fontSize = 19.sp
                )
            }

            // Main White Rounded Card Container
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Top Stat Cards: This Monts Cut & Total Deals Made
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LeftAccentCard(
                        accentColor = UplineGreen,
                        modifier = Modifier
                            .weight(1f)
                            .height(74.dp),
                        cornerRadius = 16.dp,
                        leftBorderWidth = 4.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "This Monts Cut",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = thisMonthsCutFormatted,
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }

                    LeftAccentCard(
                        accentColor = UplineDarkBlue,
                        modifier = Modifier
                            .weight(1f)
                            .height(74.dp),
                        cornerRadius = 16.dp,
                        leftBorderWidth = 4.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Total Deals Made",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.TopStart)
                            )
                            Text(
                                text = totalDealsMade.toString(),
                                color = Color.Black,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.align(Alignment.BottomEnd)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tabs Row: My Sales Report | My income
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf("My Sales Report", "My income")
                    tabs.forEachIndexed { idx, label ->
                        val active = selectedTab == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (active) Color(0xFFD6D6D6) else Color.Transparent)
                                .clickable { selectedTab = idx }
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFD0D0D0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = UplinePrimaryBlue)
                    }
                } else if (selectedTab == 0) {
                    // My Sales Report Tab
                    salesList.forEach { sale ->
                        LeftAccentCard(
                            accentColor = Color(0xFF0055D4),
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 14.dp,
                            leftBorderWidth = 4.dp,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sale.clientName,
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = sale.productName,
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Date: ${sale.dateFormatted}",
                                        color = Color.Black,
                                        fontSize = 8.sp
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Deals worth:",
                                        color = Color.Black,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 28.dp)
                                    )
                                    Text(
                                        text = sale.dealsWorthFormatted,
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFD6D6D6))
                                            .clickable { onViewSaleDetails(sale) }
                                            .padding(horizontal = 18.dp, vertical = 3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "View Details",
                                            color = Color.Black,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                } else {
                    // My income Tab
                    incomeList.forEach { inc ->
                        LeftAccentCard(
                            accentColor = UplineGreen,
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 14.dp,
                            leftBorderWidth = 4.dp,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = inc.clientName,
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "deal cut : ${inc.dealCutPercent}",
                                        color = Color.Black,
                                        fontSize = 8.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "date:${inc.dateFormatted}",
                                        color = Color.Black,
                                        fontSize = 8.sp
                                    )
                                }

                                Text(
                                    text = inc.amountFormatted,
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
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
                        downlineInCharge = p.agentCode ?: "Agus",
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
    Scaffold(
        containerColor = UplineBgGray,
        bottomBar = {
            RoleBottomBar(
                centerLabel = "add project",
                onHome = onHome,
                onCenterClick = onAddProject,
                onProfile = onProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(UplineBgGray)
        ) {
            RoleTopBar(roleTitle = "Upline", userName = userName)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(UplinePrimaryBlue)
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

                Text(
                    text = "My CRM",
                    color = Color.Black,
                    fontSize = 19.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LeftAccentCard(
                        accentColor = UplineGreen,
                        modifier = Modifier
                            .weight(1f)
                            .height(74.dp),
                        cornerRadius = 16.dp,
                        leftBorderWidth = 4.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Potential Value",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = totalPotentialFormatted,
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }

                    LeftAccentCard(
                        accentColor = UplineDarkBlue,
                        modifier = Modifier
                            .weight(1f)
                            .height(74.dp),
                        cornerRadius = 16.dp,
                        leftBorderWidth = 4.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Number of Clients",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.TopStart)
                            )
                            Text(
                                text = numberOfClients.toString(),
                                color = Color.Black,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.align(Alignment.BottomEnd)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabs = listOf("Active", "Deals", "Dropped")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) Color(0xFFD6D6D6) else Color.Transparent)
                                .clickable { onTabSelected(index) }
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFD0D0D0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = UplinePrimaryBlue)
                    }
                } else {
                    val currentList = when (selectedTab) {
                        0 -> activeCards
                        1 -> dealCards
                        else -> droppedCards
                    }

                    currentList.forEach { card ->
                        val accent = when (selectedTab) {
                            0 -> when {
                                card.daysElapsed > 30 -> UplineRed
                                card.daysElapsed >= 7 -> UplineOlive
                                else -> UplineGreen
                            }
                            else -> Color.Black
                        }
                        val pillBg = when {
                            card.daysElapsed > 30 -> Color(0xFFFF6E6E)
                            card.daysElapsed >= 7 -> Color(0xFFEAFF42)
                            else -> Color(0xFF42E831)
                        }
                        val pillText = when {
                            card.daysElapsed > 30 -> "> 30 days"
                            card.daysElapsed >= 7 -> "${card.daysElapsed} days"
                            else -> "${card.daysElapsed} Days"
                        }

                        LeftAccentCard(
                            accentColor = accent,
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 14.dp,
                            leftBorderWidth = if (selectedTab == 0) 3.dp else 1.dp,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
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

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.heightIn(min = 72.dp)
                                ) {
                                    when (selectedTab) {
                                        0 -> {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(pillBg)
                                                    .padding(horizontal = 14.dp, vertical = 3.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = pillText,
                                                    color = Color.Black,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        1 -> {
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
                                        else -> Spacer(modifier = Modifier.height(20.dp))
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFD9D9D9))
                                            .clickable { onViewDetails(card) }
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
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
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
                    Toast.makeText(context, "Client's Name wajib diisi", Toast.LENGTH_SHORT).show()
                }
                offerVal <= 0.0 -> {
                    Toast.makeText(context, "Product's First offer harus lebih dari 0", Toast.LENGTH_SHORT).show()
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
    var clientName by rememberSaveable { mutableStateOf("") }
    var clientAddress by rememberSaveable { mutableStateOf("") }
    var clientEmail by rememberSaveable { mutableStateOf("") }
    var clientPhone by rememberSaveable { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
    var isDirectDeal by rememberSaveable { mutableStateOf(false) }
    var firstOffer by rememberSaveable { mutableStateOf("") }
    var productDetails by rememberSaveable { mutableStateOf("") }

    val fallbackOptions = remember {
        listOf(
            MasterItemDto(id = 0, itemName = "Nursery Program", basePrice = 20000.0),
            MasterItemDto(id = 0, itemName = "Core ERP Module System", basePrice = 75000.0),
            MasterItemDto(id = 0, itemName = "Standard LMS Platform", basePrice = 35000.0)
        )
    }
    val availableProducts = if (productItems.isNotEmpty()) productItems else fallbackOptions
    val currentProduct = availableProducts.getOrNull(selectedItemIndex) ?: availableProducts.first()

    Scaffold(
        containerColor = UplineBgGray,
        bottomBar = {
            RoleBottomBar(
                centerLabel = "add project",
                onHome = onHome,
                onCenterClick = {},
                onProfile = onProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(UplineBgGray)
        ) {
            RoleTopBar(roleTitle = "Upline", userName = userName)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(UplinePrimaryBlue)
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

                Text(
                    text = "New Deals",
                    color = Color.Black,
                    fontSize = 19.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Client Details",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = Color(0xFFD0D0D0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                UplineDealTextField("Client's Name", clientName, { clientName = it })
                Spacer(modifier = Modifier.height(10.dp))
                UplineDealTextField("Client's Address", clientAddress, { clientAddress = it })
                Spacer(modifier = Modifier.height(10.dp))
                UplineDealTextField("Client's Email", clientEmail, { clientEmail = it }, KeyboardType.Email)
                Spacer(modifier = Modifier.height(10.dp))
                UplineDealTextField("Client's Phone Number", clientPhone, { clientPhone = it }, KeyboardType.Phone)

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Project/product Details",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = Color(0xFFD0D0D0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Product Type", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(3.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(UplineFieldGray)
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = currentProduct.itemName,
                            color = Color.Black,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(Icons.Default.ArrowDropDown, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        availableProducts.forEachIndexed { idx, item ->
                            DropdownMenuItem(
                                text = { Text(item.itemName, fontSize = 12.sp) },
                                onClick = {
                                    selectedItemIndex = idx
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val followUpSelected = !isDirectDeal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (followUpSelected) Color(0xFF0018FF) else Color.White)
                            .border(1.dp, Color(0xFF0018FF), RoundedCornerShape(10.dp))
                            .clickable { isDirectDeal = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Follow-Up",
                            color = if (followUpSelected) Color.White else Color(0xFF0018FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    val directSelected = isDirectDeal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (directSelected) Color(0xFFFF3B30) else Color.White)
                            .border(1.dp, Color(0xFFFF3B30), RoundedCornerShape(10.dp))
                            .clickable { isDirectDeal = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Direct Deal",
                            color = if (directSelected) Color.White else Color(0xFFFF3B30),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Product's First offer", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(UplineFieldGray)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(6.dp))
                    BasicTextField(
                        value = firstOffer,
                        onValueChange = { firstOffer = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.Black, fontSize = 11.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Product's Details", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(UplineFieldGray)
                        .padding(10.dp)
                ) {
                    BasicTextField(
                        value = productDetails,
                        onValueChange = { productDetails = it },
                        textStyle = TextStyle(color = Color.Black, fontSize = 11.sp),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(UplinePrimaryBlue)
                        .clickable(enabled = !submitting) {
                            onSubmitDeal(
                                clientName,
                                clientAddress,
                                clientEmail,
                                clientPhone,
                                currentProduct.takeIf { it.id > 0 },
                                currentProduct.itemName,
                                isDirectDeal,
                                firstOffer,
                                productDetails
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (submitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Create Deal", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
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
