package com.example.projectcapstone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RequestPage
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DlPrimary = Color(0xFF1155F5)
private val DlPrimaryDark = Color(0xFF0B3FC2)
private val DlBg = Color(0xFFF3F6FB)
private val DlCard = Color(0xFFFFFFFF)
private val DlTextMuted = Color(0xFF6B7280)
private val DlBorder = Color(0xFFE4E9F2)
private val DlGreen = Color(0xFF1FB96A)
private val DlYellow = Color(0xFFF1B81F)
private val DlRed = Color(0xFFEE5A5A)

@Composable
fun IntegraModernDownlineHome(
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
    Scaffold(
        containerColor = DlBg,
        bottomBar = {
            DownlineBottomBar(
                onHome = onHome,
                onCreateProposal = onCreateProposal,
                onProfile = onProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DlBg)
        ) {
            DownlineTopBar(userName = userName, onNotificationClick = onNotificationBellClick)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricStatCard(
                        title = "This Month's Cut",
                        value = monthlyCutFormatted,
                        icon = Icons.Default.AttachMoney,
                        accent = DlGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Projects Held",
                        value = projectsHeld.toString(),
                        icon = Icons.Default.Workspaces,
                        accent = DlPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(shape = RoundedCornerShape(24.dp), color = DlCard, shadowElevation = 4.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Menu Downline", fontWeight = FontWeight.SemiBold, color = Color.Black, fontSize = 13.sp)
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MenuShortcut(icon = Icons.Default.Star, label = "Income", onClick = onIncomeClick)
                            MenuShortcut(icon = Icons.Default.BusinessCenter, label = "My CRM", onClick = onCrmClick)
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(24.dp), color = DlCard, shadowElevation = 4.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Your Upline", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircleAvatar(label = uplineName)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(uplineName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("No: $uplineContactNo", color = DlTextMuted, fontSize = 11.sp)
                            }
                            PillButton(label = "Contact", filled = true, onClick = onContactUplineClick)
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(24.dp), color = DlCard, shadowElevation = 4.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Recent Notifications", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(10.dp))
                        if (loading) {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = DlPrimary)
                            }
                        } else if (notifications.isEmpty()) {
                            EmptyStateCard("Belum ada notifikasi terbaru.")
                        } else {
                            notifications.forEachIndexed { index, item ->
                                NotificationCardModern(item = item, onSeeDetails = { onSeeNotificationDetails(item) })
                                if (index != notifications.lastIndex) Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IntegraModernDownlinePerformance(
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
    Scaffold(
        containerColor = DlBg,
        bottomBar = {
            DownlineBottomBar(onHome = onHome, onCreateProposal = onCreateProposal, onProfile = onProfile)
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(DlBg)) {
            DownlineTopBar(userName = userName, onNotificationClick = onNotificationBellClick)
            SectionTopBar(title = "My Performance", onBack = onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(DlCard)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFFF8FAFF), tonalElevation = 1.dp, shadowElevation = 2.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Your Profits", color = DlTextMuted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        Text(profitsFormatted, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = DlPrimaryDark)
                    }
                }

                Text("My Sales Report", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DlTextMuted)
                HorizontalDivider(color = DlBorder)

                if (loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DlPrimary)
                    }
                } else if (reportItems.isEmpty()) {
                    EmptyStateCard("Belum ada sales report.")
                } else {
                    reportItems.forEach { item ->
                        SalesReportCard(item)
                    }
                }
            }
        }
    }
}

@Composable
fun IntegraModernDownlineCrm(
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
    val visibleCards = when (selectedTab) {
        0 -> activeCards
        1 -> dealCards
        else -> droppedCards
    }

    Scaffold(
        containerColor = DlBg,
        bottomBar = { DownlineBottomBar(onHome = onHome, onCreateProposal = onCreateProposal, onProfile = onProfile) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(DlBg)) {
            DownlineTopBar(userName = userName, onNotificationClick = onNotificationBellClick)
            SectionTopBar(title = "My CRM", onBack = onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(DlCard)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricStatCard(
                        title = "Total Potential Value",
                        value = totalPotentialFormatted,
                        icon = Icons.Default.RequestPage,
                        accent = DlGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Number of Clients",
                        value = numberOfClients.toString(),
                        icon = Icons.Default.Person,
                        accent = DlPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFF7F9FD)) {
                    TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent, divider = {}, indicator = {}) {
                        listOf("Active", "Deals", "Dropped").forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Tab(
                                selected = isSelected,
                                onClick = { onTabSelected(index) },
                                text = {
                                    Text(
                                        title,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) DlPrimary else DlTextMuted
                                    )
                                }
                            )
                        }
                    }
                }

                if (loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DlPrimary)
                    }
                } else if (visibleCards.isEmpty()) {
                    EmptyStateCard("Belum ada data pada tab ini.")
                } else {
                    visibleCards.forEach { card ->
                        CrmCardModern(card = card, selectedTab = selectedTab, onViewDetails = { onViewDetails(card) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegraModernDownlineNewProposal(
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
    var clientName by rememberSaveable { mutableStateOf("") }
    var clientAddress by rememberSaveable { mutableStateOf("") }
    var clientEmail by rememberSaveable { mutableStateOf("") }
    var clientPhone by rememberSaveable { mutableStateOf("") }
    var firstOffer by rememberSaveable { mutableStateOf("") }
    var productDetails by rememberSaveable { mutableStateOf("") }
    var isDirectDeal by rememberSaveable { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    val fallbackOptions = remember {
        listOf(
            MasterItemDto(id = 0, itemName = "Nursery Program", basePrice = 20000.0),
            MasterItemDto(id = 0, itemName = "Core ERP Module System", basePrice = 75000.0),
            MasterItemDto(id = 0, itemName = "Standard LMS Platform", basePrice = 35000.0),
            MasterItemDto(id = 0, itemName = "Clinic Management System", basePrice = 25000.0)
        )
    }
    val availableProducts = if (productItems.isNotEmpty()) productItems else fallbackOptions
    val currentProduct = availableProducts.getOrNull(selectedItemIndex) ?: availableProducts.first()

    Scaffold(
        containerColor = DlBg,
        bottomBar = { DownlineBottomBar(onHome = onHome, onCreateProposal = {}, onProfile = onProfile) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(DlBg)) {
            DownlineTopBar(userName = userName, onNotificationClick = onNotificationBellClick)
            SectionTopBar(title = "New Proposal", onBack = onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(DlCard)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SectionLabel("Client Details")
                ModernField(value = clientName, onValueChange = { clientName = it }, label = "Client's Name", leadingIcon = Icons.Default.Person)
                ModernField(value = clientAddress, onValueChange = { clientAddress = it }, label = "Client's Address", leadingIcon = Icons.Default.Badge)
                ModernField(value = clientEmail, onValueChange = { clientEmail = it }, label = "Client's Email", leadingIcon = Icons.Default.Email, keyboardType = KeyboardType.Email)
                ModernField(value = clientPhone, onValueChange = { clientPhone = it }, label = "Client's Phone Number", leadingIcon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)

                SectionLabel("Project / Product Details")
                ExposedDropdownMenuBox(expanded = menuExpanded, onExpandedChange = { menuExpanded = !menuExpanded }) {
                    OutlinedTextField(
                        value = currentProduct.itemName,
                        onValueChange = {},
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        label = { Text("Product Type") },
                        shape = RoundedCornerShape(16.dp),
                        colors = modernFieldColors(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) }
                    )
                    ExposedDropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        availableProducts.forEachIndexed { index, item ->
                            DropdownMenuItem(text = { Text(item.itemName) }, onClick = {
                                selectedItemIndex = index
                                menuExpanded = false
                            })
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    ToggleModeChip(label = "Follow-Up", active = !isDirectDeal, onClick = { isDirectDeal = false }, modifier = Modifier.weight(1f))
                    ToggleModeChip(label = "Direct Deal", active = isDirectDeal, onClick = { isDirectDeal = true }, modifier = Modifier.weight(1f))
                }

                ModernField(
                    value = firstOffer,
                    onValueChange = { firstOffer = it },
                    label = "Product's First Offer",
                    leadingIcon = Icons.Default.AttachMoney,
                    keyboardType = KeyboardType.Number
                )

                OutlinedTextField(
                    value = productDetails,
                    onValueChange = { productDetails = it },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    label = { Text("Product's Details") },
                    placeholder = { Text("Describe client needs, proposal notes, or deal context...") },
                    shape = RoundedCornerShape(18.dp),
                    colors = modernFieldColors()
                )

                Spacer(Modifier.height(4.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clickable(enabled = !submitting) {
                            onSubmitDeal(
                                clientName,
                                clientAddress,
                                clientEmail,
                                clientPhone,
                                currentProduct,
                                currentProduct.itemName,
                                isDirectDeal,
                                firstOffer,
                                productDetails
                            )
                        },
                    shape = RoundedCornerShape(18.dp),
                    color = DlPrimary,
                    shadowElevation = 3.dp
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (submitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                        } else {
                            Text(
                                text = if (isDirectDeal) "Create Direct Deal" else "Create Proposal",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTopBar(title: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.align(Alignment.CenterStart).size(38.dp).clickable { onBack() },
            shape = CircleShape,
            color = DlPrimary,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}

@Composable
private fun MetricStatCard(title: String, value: String, icon: ImageVector, accent: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = DlCard, shadowElevation = 4.dp) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(title, color = DlTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 15.sp)
            }
            Spacer(Modifier.height(14.dp))
            Text(value, color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun MenuShortcut(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier.size(54.dp).clip(CircleShape).background(DlPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = DlPrimary, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CircleAvatar(label: String) {
    val initials = label.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
    Box(
        modifier = Modifier.size(52.dp).clip(CircleShape).background(DlPrimary.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Text(initials.ifBlank { "UP" }, color = DlPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun PillButton(label: String, filled: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (filled) DlPrimary else Color.White,
        border = if (filled) null else androidx.compose.foundation.BorderStroke(1.dp, DlPrimary),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = if (filled) Color.White else DlPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun NotificationCardModern(item: DownlineNotificationItemUi, onSeeDetails: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 3.dp) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape).background((if (item.isCheckIcon) DlGreen else DlPrimary).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isCheckIcon) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = if (item.isCheckIcon) DlGreen else DlPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(item.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(8.dp))
            Text(item.message, fontSize = 12.sp, color = DlTextMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Date: ${item.date}", fontSize = 11.sp, color = DlTextMuted, modifier = Modifier.weight(1f))
                PillButton(label = "See Details", filled = false, onClick = onSeeDetails)
            }
        }
    }
}

@Composable
private fun SalesReportCard(item: DownlineSalesReportItemUi) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 3.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.width(4.dp).height(56.dp).clip(RoundedCornerShape(6.dp)).background(DlGreen)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("deal cut: ${item.dealCutPercent}", fontSize = 11.sp, color = DlTextMuted)
                Text("date: ${item.dateFormatted}", fontSize = 11.sp, color = DlTextMuted)
            }
            Text(item.amountFormatted, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.Black)
        }
    }
}

@Composable
private fun CrmCardModern(card: DownlineCrmCardUi, selectedTab: Int, onViewDetails: () -> Unit) {
    val badgeColor = when {
        selectedTab == 1 -> DlPrimary
        selectedTab == 2 -> Color(0xFFB8BEC9)
        card.daysElapsed > 30 -> DlRed
        card.daysElapsed > 7 -> DlYellow
        else -> DlGreen
    }
    val badgeText = when {
        selectedTab == 1 -> "Deals Worth\n${card.dealWorthFormatted}"
        selectedTab == 2 -> "Dropped"
        card.daysElapsed > 30 -> ">${card.daysElapsed} days"
        else -> "${card.daysElapsed} days"
    }
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 3.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.width(4.dp).height(78.dp).clip(RoundedCornerShape(6.dp)).background(if (selectedTab == 2) Color(0xFFD5DAE3) else badgeColor))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(card.clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(2.dp))
                InfoLine(icon = Icons.Default.Inventory2, text = "Product: ${card.productName}")
                InfoLine(icon = Icons.Default.CalendarMonth, text = "Since: ${card.sinceDate}")
                InfoLine(icon = Icons.Default.Person, text = "Downline in Charge: ${card.downlineInCharge}")
                if (selectedTab == 1) {
                    Spacer(Modifier.height(6.dp))
                    Text("Stage: ${card.stage}", fontSize = 11.sp, color = DlTextMuted)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(shape = RoundedCornerShape(50), color = badgeColor.copy(alpha = if (selectedTab == 2) 0.6f else 0.16f)) {
                    Text(
                        badgeText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = if (selectedTab == 2) Color(0xFF5E6673) else badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 13.sp
                    )
                }
                Spacer(Modifier.height(14.dp))
                PillButton(label = "View Details", filled = false, onClick = onViewDetails)
            }
        }
    }
}

@Composable
private fun InfoLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = DlTextMuted, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 11.sp, color = DlTextMuted)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
}

@Composable
private fun ModernField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = modernFieldColors(),
        singleLine = true
    )
}

@Composable
private fun modernFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color(0xFFF8FAFD),
    unfocusedContainerColor = Color(0xFFF8FAFD),
    focusedIndicatorColor = DlPrimary,
    unfocusedIndicatorColor = DlBorder,
    focusedLeadingIconColor = DlPrimary,
    unfocusedLeadingIconColor = DlTextMuted,
    focusedLabelColor = DlPrimary,
    unfocusedLabelColor = DlTextMuted,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    focusedPlaceholderColor = DlTextMuted,
    unfocusedPlaceholderColor = DlTextMuted
)

@Composable
private fun ToggleModeChip(label: String, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (active) DlPrimary else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (active) DlPrimary else DlBorder),
        shadowElevation = if (active) 2.dp else 0.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(label, color = if (active) Color.White else DlPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFF8FAFD)) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(message, color = DlTextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}
