package com.example.projectcapstone

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

// UI-only components. API, data, and navigation are still managed by UplineScreensNew.kt.
private val Ink = Color(0xFF121A32)
private val Muted = Color(0xFF738098)
private val Primary = Color(0xFF0758F7)
private val PrimarySoft = Color(0xFFEAF1FF)
private val PageBg = Color(0xFFF5F7FC)
private val Outline = Color(0xFFE3E9F3)
private val Positive = Color(0xFF18A75A)
private val PositiveSoft = Color(0xFFE9F9EF)
private val Negative = Color(0xFFE64C5B)
private val NegativeSoft = Color(0xFFFFECEE)
private val Amber = Color(0xFFD48B11)
private val AmberSoft = Color(0xFFFFF5D9)
private val CardRadius = RoundedCornerShape(20.dp)

@Composable
private fun ModernSurface(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardRadius,
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Outline)
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String? = null) {
    Column {
        Text(title, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        if (subtitle != null) {
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ModernHeader(userName: String, pageTitle: String?, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 18.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().height(62.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.logo_ict_textless),
                contentDescription = "Integra Agency",
                modifier = Modifier.size(34.dp)
            )
            Spacer(Modifier.width(9.dp))
            Text("Integra Agency", color = Ink, fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp, modifier = Modifier.weight(1f))
            Box(
                Modifier.size(37.dp).clip(CircleShape).background(PageBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications",
                    tint = Ink, modifier = Modifier.size(20.dp))
            }
        }
        if (pageTitle != null) {
            HorizontalDivider(color = Outline)
            Row(
                Modifier.fillMaxWidth().height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp).background(Primary, RoundedCornerShape(11.dp))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali",
                        tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(15.dp))
                Text(pageTitle, color = Ink, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun ModernNavBar(
    onHome: () -> Unit,
    onAddProject: () -> Unit,
    onProfile: () -> Unit,
    active: String = "home",
    centerEnabled: Boolean = true
) {
    AppBottomBar(
        items = listOf(
            AppBottomNavItem("Home", Icons.Default.Home, selected = active == "home", onClick = onHome),
            AppBottomNavItem("Profile", Icons.Default.Person, selected = active == "profile", onClick = onProfile)
        ),
        centerAction = AppBottomNavItem(
            "Add Project",
            Icons.Default.Add,
            selected = active == "add",
            onClick = { if (centerEnabled) onAddProject() }
        ),
        selectedColor = Primary,
        unselectedColor = Muted,
        centerColor = Primary
    )
}

@Composable
private fun ModernPage(
    userName: String,
    title: String? = null,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {},
    onAddProject: () -> Unit = {},
    onProfile: () -> Unit = {},
    addEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        containerColor = PageBg,
        bottomBar = {
            ModernNavBar(
                onHome, onAddProject, onProfile,
                active = if (title == null) "home" else if (!addEnabled) "add" else "none",
                centerEnabled = addEnabled
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ModernHeader(userName, title, onBack)
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 116.dp), shape = RoundedCornerShape(18.dp),
        color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Outline),
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(30.dp).background(accent.copy(alpha = .11f), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(7.dp))
            Text(label, color = Muted, fontSize = 11.sp, maxLines = 2)
            Text(value, color = Ink, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ActionChip(
    text: String,
    background: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick),
        color = background, shape = RoundedCornerShape(10.dp)) {
        Box(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), contentAlignment = Alignment.Center) {
            Text(text, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MessageState(message: String, icon: ImageVector = Icons.Default.Info) {
    ModernSurface {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Muted)
            Spacer(Modifier.width(12.dp))
            Text(message, color = Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Primary, modifier = Modifier.size(28.dp))
    }
}

// ===== UPLINE HOME =====
@Composable
fun IntegraModernUplineHome(
    userName: String,
    monthlyCutFormatted: String,
    projectsHeld: Int,
    activeDeals: List<UplineActiveDealUi>,
    activities: List<UplineRecentActivityUi>,
    onPerformanceClick: () -> Unit,
    onDownlineClick: () -> Unit,
    onCrmClick: () -> Unit,
    onDealActionClick: (UplineActiveDealUi, String) -> Unit,
    onSeeActivityDetails: (UplineRecentActivityUi) -> Unit,
    onHome: () -> Unit,
    onAddProject: () -> Unit,
    onProfile: () -> Unit
) {
    ModernPage(userName, onHome = onHome, onAddProject = onAddProject, onProfile = onProfile) {
        Column(
            Modifier.fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFFE6EFFF), Color(0xFFF3F7FF))), CardRadius)
                .padding(18.dp)
        ) {
            Text("Selamat datang,", color = Muted, fontSize = 13.sp)
            Text("${userName.substringBefore(' ')} 👋", color = Ink, fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text("Pantau performa tim dan proyek hari ini.", color = Muted, fontSize = 12.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile("This Month's Cut", monthlyCutFormatted, Icons.Default.TrendingUp,
                Positive, Modifier.weight(1f))
            MetricTile("Projects Held", projectsHeld.toString(), Icons.Default.Layers,
                Primary, Modifier.weight(1f))
        }
        ModernSurface {
            SectionHeading("Menu Integra", "Akses cepat untuk aktivitas utama")
            Spacer(Modifier.height(15.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ShortcutTile("My Performance", Icons.Default.BarChart, onPerformanceClick)
                ShortcutTile("Downlines", Icons.Default.Groups, onDownlineClick)
                ShortcutTile("My CRM", Icons.Default.BusinessCenter, onCrmClick)
            }
        }
        SectionHeading("Active Deals", "Kelola penawaran yang sedang berlangsung")
        if (activeDeals.isEmpty()) {
            MessageState("Belum ada active deals.")
        } else {
            activeDeals.forEach { deal ->
                ModernSurface {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(38.dp).background(NegativeSoft, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Handshake, null, tint = Negative, modifier = Modifier.size(21.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(deal.clientName, color = Ink, fontWeight = FontWeight.Bold,
                                fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Going for: ${deal.amountFormatted}", color = Muted, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionChip("No Deal", NegativeSoft, Negative,
                            { onDealActionClick(deal, "no_deal") }, Modifier.weight(1f))
                        ActionChip("Counter", PrimarySoft, Primary,
                            { onDealActionClick(deal, "counter") }, Modifier.weight(1f))
                        ActionChip("Deal", PositiveSoft, Positive,
                            { onDealActionClick(deal, "deal") }, Modifier.weight(1f))
                    }
                }
            }
        }
        SectionHeading("Recent Downline Activity")
        if (activities.isEmpty()) {
            MessageState("Belum ada aktivitas terbaru.")
        } else {
            activities.forEach { activity ->
                ModernSurface {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.CheckCircle, null, tint = Positive,
                            modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(activity.title, color = Ink, fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp)
                            Spacer(Modifier.height(5.dp))
                            Text(activity.clientSubtitle, color = Ink, fontSize = 12.sp)
                            Text(activity.detailsText, color = Muted, fontSize = 11.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()) {
                        Text(activity.dateFormatted, color = Muted, fontSize = 11.sp)
                        TextButton(onClick = { onSeeActivityDetails(activity) }) {
                            Text("See Details", color = Primary, fontSize = 12.sp)
                            Icon(Icons.Default.ChevronRight, null, tint = Primary,
                                modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortcutTile(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        Modifier.width(90.dp).clickable(onClick = onClick).padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(48.dp)
            .background(Brush.linearGradient(listOf(Primary, Color(0xFF328AFF))), CircleShape),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(23.dp))
        }
        Spacer(Modifier.height(7.dp))
        Text(label, color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center, lineHeight = 14.sp)
    }
}

// ===== MY DOWNLINES =====
@Composable
fun IntegraModernUplineDownlines(
    userName: String,
    downlineCount: Int,
    totalTeamDeals: Int,
    agents: List<UplineDownlineAgentUi>,
    loading: Boolean,
    onViewAgentDetails: (UplineDownlineAgentUi) -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onAddProject: () -> Unit,
    onProfile: () -> Unit
) {
    var search by rememberSaveable { mutableStateOf("") }
    val filtered = agents.filter { it.name.contains(search, true) || it.phoneNumber.contains(search, true) }
    ModernPage(userName, "My Downlines", onBack, onHome, onAddProject, onProfile) {
        ModernSurface {
            Text("Team Performance", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricMini("Downlines", downlineCount.toString(), Primary, Modifier.weight(1f))
                MetricMini("Total Deals", totalTeamDeals.toString(), Positive, Modifier.weight(1f))
            }
        }
        OutlinedTextField(
            value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
            placeholder = { Text("Search downline...", fontSize = 13.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedBorderColor = Primary, unfocusedBorderColor = Outline
            )
        )
        SectionHeading("Agent Directory", "${filtered.size} anggota ditemukan")
        if (loading) LoadingState()
        else if (filtered.isEmpty()) MessageState("Tidak ada downline ditemukan.")
        else filtered.forEach { agent ->
            ModernSurface {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(46.dp).background(PrimarySoft, CircleShape),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Primary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text(agent.name, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Joined: ${agent.joinedDate}", color = Muted, fontSize = 10.sp)
                        Text(agent.phoneNumber, color = Muted, fontSize = 10.sp)
                    }
                    if (agent.hasNewProposal) {
                        StatusBadge("New!", NegativeSoft, Negative)
                    }
                }
                Spacer(Modifier.height(13.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    MetricMini("Total Deals", agent.totalDeals.toString(), Primary, Modifier.weight(1f))
                    MetricMini("Performance", agent.performanceRatio, Positive, Modifier.weight(1f))
                }
                Spacer(Modifier.height(9.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onViewAgentDetails(agent) }) {
                        Text("View Details", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowForward, null, tint = Primary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricMini(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.background(accent.copy(alpha = .08f), RoundedCornerShape(12.dp))
            .padding(horizontal = 11.dp, vertical = 10.dp)
    ) {
        Text(label, color = Muted, fontSize = 10.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = accent, fontSize = 14.sp, fontWeight = FontWeight.Bold,
            maxLines = 2, lineHeight = 17.sp)
    }
}

@Composable
private fun StatusBadge(text: String, bg: Color, fg: Color) {
    Box(Modifier.background(bg, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(text, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ModernTabs(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Color(0xFFECF0F7), RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val isActive = selectedIndex == index
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(11.dp))
                    .background(if (isActive) Primary else Color.Transparent)
                    .clickable { onSelect(index) }.padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = if (isActive) Color.White else Muted,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}

// ===== MY PERFORMANCE =====
@Composable
fun IntegraModernUplinePerformance(
    userName: String,
    thisMonthsCutFormatted: String,
    totalDealsMade: Int,
    salesList: List<UplineSalesCardUi>,
    incomeList: List<DownlineSalesReportItemUi>,
    loading: Boolean,
    initialTab: Int,
    onViewSaleDetails: (UplineSalesCardUi) -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onAddProject: () -> Unit,
    onProfile: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }
    ModernPage(userName, "My Performance", onBack, onHome, onAddProject, onProfile) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile("This Month's Cut", thisMonthsCutFormatted, Icons.Default.TrendingUp,
                Positive, Modifier.weight(1f))
            MetricTile("Total Deals Made", totalDealsMade.toString(), Icons.Default.Assessment,
                Primary, Modifier.weight(1f))
        }
        ModernTabs(listOf("My Sales Report", "My Income"), selectedTab) { selectedTab = it }
        SectionHeading(if (selectedTab == 0) "Sales Report" else "Income History")
        if (loading) LoadingState()
        else if (selectedTab == 0) {
            if (salesList.isEmpty()) MessageState("Belum ada data penjualan.")
            salesList.forEach { sale ->
                ModernSurface {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(Modifier.size(36.dp).background(PositiveSoft, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ReceiptLong, null, tint = Positive, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(sale.clientName, color = Ink, fontSize = 13.sp,
                                fontWeight = FontWeight.Bold)
                            Text(sale.productName, color = Muted, fontSize = 11.sp)
                            Text(sale.dateFormatted, color = Muted, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Deals Worth", color = Muted, fontSize = 10.sp)
                            Text(sale.dealsWorthFormatted, color = Ink, fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = Outline)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(sale.projectCode, color = Muted, fontSize = 11.sp)
                        TextButton(onClick = { onViewSaleDetails(sale) }) {
                            Text("View Details", color = Primary, fontSize = 12.sp)
                            Icon(Icons.Default.ChevronRight, null, tint = Primary)
                        }
                    }
                }
            }
        } else {
            if (incomeList.isEmpty()) MessageState("Belum ada data komisi.")
            incomeList.forEach { income ->
                ModernSurface {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, null, tint = Positive, modifier = Modifier.size(29.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(income.clientName, color = Ink, fontWeight = FontWeight.Bold,
                                fontSize = 13.sp)
                            Text("Commission rate: ${income.dealCutPercent}", color = Muted,
                                fontSize = 11.sp)
                            Text(income.dateFormatted, color = Muted, fontSize = 10.sp)
                        }
                        Text(income.amountFormatted, color = Positive, fontWeight = FontWeight.Bold,
                            fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ===== MY CRM =====
@Composable
fun IntegraModernUplineCrm(
    userName: String,
    totalPotentialFormatted: String,
    numberOfClients: Int,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    activeCards: List<DownlineCrmCardUi>,
    dealCards: List<DownlineCrmCardUi>,
    droppedCards: List<DownlineCrmCardUi>,
    loading: Boolean,
    onViewDetails: (DownlineCrmCardUi) -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onAddProject: () -> Unit,
    onProfile: () -> Unit
) {
    val cards = when (selectedTab) { 0 -> activeCards; 1 -> dealCards; else -> droppedCards }
    ModernPage(userName, "My CRM", onBack, onHome, onAddProject, onProfile) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile("Total Potential Value", totalPotentialFormatted, Icons.Default.TrackChanges,
                Positive, Modifier.weight(1f))
            MetricTile("Number of Clients", numberOfClients.toString(), Icons.Default.Groups,
                Primary, Modifier.weight(1f))
        }
        ModernTabs(listOf("Active", "Deals", "Dropped"), selectedTab, onTabSelected)
        SectionHeading("Client Pipeline", "${cards.size} proyek dalam kategori ini")
        if (loading) LoadingState()
        else if (cards.isEmpty()) MessageState("Tidak ada proyek pada kategori ini.")
        else cards.forEach { card ->
            val accent = when (selectedTab) {
                0 -> when {
                    card.daysElapsed > 30 -> Negative
                    card.daysElapsed > 7 -> Amber
                    else -> Positive
                }
                1 -> Positive
                else -> Muted
            }
            ModernSurface {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(card.clientName, color = Ink, fontWeight = FontWeight.Bold,
                            fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(5.dp))
                        Text("Product: ${card.productName}", color = Muted, fontSize = 11.sp)
                        Text("Since: ${card.sinceDate}", color = Muted, fontSize = 11.sp)
                        Text("Downline: ${card.downlineInCharge}", color = Muted, fontSize = 11.sp)
                    }
                    if (selectedTab == 0) {
                        StatusBadge("${card.daysElapsed} days", accent.copy(alpha = .11f), accent)
                    } else if (selectedTab == 1) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Deals Worth", fontSize = 10.sp, color = Muted)
                            Text(card.dealWorthFormatted, color = Ink,
                                fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        StatusBadge("Dropped", Color(0xFFECEFF3), Muted)
                    }
                }
                Spacer(Modifier.height(11.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onViewDetails(card) }) {
                        Text("View Details", color = Primary, fontSize = 12.sp)
                        Icon(Icons.Default.ChevronRight, null, tint = Primary)
                    }
                }
            }
        }
    }
}

// ===== NEW DEALS FORM =====
@Composable
fun IntegraModernUplineNewDeal(
    userName: String,
    productItems: List<MasterItemDto>,
    submitting: Boolean,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onProfile: () -> Unit,
    onSubmitDeal: (
        clientName: String, clientAddress: String, clientEmail: String,
        clientPhone: String, selectedItem: MasterItemDto?, productTypeLabel: String,
        isDirectDeal: Boolean, firstOffer: String, productDetails: String
    ) -> Unit
) {
    var clientName by rememberSaveable { mutableStateOf("") }
    var clientAddress by rememberSaveable { mutableStateOf("") }
    var clientEmail by rememberSaveable { mutableStateOf("") }
    var clientPhone by rememberSaveable { mutableStateOf("") }
    var selectedProductIndex by rememberSaveable { mutableIntStateOf(-1) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var directDeal by rememberSaveable { mutableStateOf(false) }
    var firstOffer by rememberSaveable { mutableStateOf("") }
    var productDetails by rememberSaveable { mutableStateOf("") }
    var formError by rememberSaveable { mutableStateOf("") }
    val selectedProduct = productItems.getOrNull(selectedProductIndex)

    ModernPage(userName, "New Deals", onBack, onHome, onAddProject = {}, onProfile = onProfile,
        addEnabled = false) {
        ModernSurface {
            SectionHeading("Client Details", "Informasi calon klien")
            Spacer(Modifier.height(14.dp))
            ModernInput("Client's Name *", clientName, { clientName = it }, Icons.Default.Person)
            Spacer(Modifier.height(10.dp))
            ModernInput("Client's Address", clientAddress, { clientAddress = it }, Icons.Default.LocationOn)
            Spacer(Modifier.height(10.dp))
            ModernInput("Client's Email", clientEmail, { clientEmail = it }, Icons.Default.Email,
                KeyboardType.Email)
            Spacer(Modifier.height(10.dp))
            ModernInput("Client's Phone Number", clientPhone, { clientPhone = it }, Icons.Default.Phone,
                KeyboardType.Phone)
        }
        ModernSurface {
            SectionHeading("Project / Product Details", "Tentukan jenis dan nilai penawaran")
            Spacer(Modifier.height(14.dp))
            Text("Product Type *", fontSize = 12.sp, color = Ink, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { dropdownExpanded = true }, modifier = Modifier.fillMaxWidth()
                        .height(54.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Outline)
                ) {
                    Text(selectedProduct?.itemName ?: "Pilih product type",
                        modifier = Modifier.weight(1f), maxLines = 1,
                        overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Start)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih produk")
                }
                DropdownMenu(expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }) {
                    productItems.forEachIndexed { index, product ->
                        DropdownMenuItem(
                            text = { Text(product.itemName) },
                            onClick = {
                                selectedProductIndex = index
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
            if (productItems.isEmpty()) {
                Text("Daftar produk belum tersedia. Periksa koneksi master item.",
                    color = Negative, fontSize = 11.sp)
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip("Follow-Up", if (!directDeal) Primary else PrimarySoft,
                    if (!directDeal) Color.White else Primary,
                    { directDeal = false }, Modifier.weight(1f))
                ActionChip("Direct Deal", if (directDeal) Primary else PrimarySoft,
                    if (directDeal) Color.White else Primary,
                    { directDeal = true }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))
            ModernInput("Product's First Offer *", firstOffer,
                { firstOffer = it }, Icons.Default.AttachMoney, KeyboardType.Decimal)
            Spacer(Modifier.height(10.dp))
            Text("Product's Details", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = productDetails, onValueChange = { productDetails = it },
                placeholder = { Text("Detail produk, catatan, atau kebutuhan klien...", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                shape = RoundedCornerShape(12.dp), maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Outline, focusedBorderColor = Primary
                )
            )
        }
        if (formError.isNotEmpty()) Text(formError, color = Negative, fontSize = 12.sp)
        Button(
            onClick = {
                formError = when {
                    clientName.isBlank() -> "Nama klien wajib diisi."
                    selectedProduct == null -> "Silakan pilih product type."
                    (firstOffer.replace(",", "").replace("$", "").trim().toDoubleOrNull() ?: 0.0) <= 0 -> "First offer harus lebih dari 0."
                    else -> ""
                }
                if (formError.isEmpty() && selectedProduct != null) {
                    onSubmitDeal(clientName, clientAddress, clientEmail, clientPhone,
                        selectedProduct.takeIf { it.id > 0 }, selectedProduct.itemName,
                        directDeal, firstOffer, productDetails)
                }
            },
            enabled = !submitting && productItems.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            if (submitting) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp))
            else {
                Text("Create Deal", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(7.dp))
    }
}

@Composable
private fun ModernInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            placeholder = { Text(label.removeSuffix(" *"), fontSize = 12.sp, color = Muted) },
            leadingIcon = { Icon(icon, null, tint = Muted, modifier = Modifier.size(19.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Outline, focusedBorderColor = Primary
            )
        )
    }
}
