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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val GlobalBgGray = Color(0xFFE5E5E5)
private val GlobalPrimaryBlue = Color(0xFF0048FF)
private val GlobalDarkBlue = Color(0xFF0014A8)
private val GlobalGreen = Color(0xFF1B8A2A)
private val GlobalRed = Color(0xFFD92121)

data class DealDetailSelection(
    val projectId: Long = 0L,
    val clientName: String = "PT arunika Web",
    val productType: String = "Web Program",
    val dealType: String = "Follow-Up",
    val initialPrice: Double = 20000.0,
    val agentResponsible: String = "Tony Stark",
    val clientPhone: String = "08xxxxxxxx",
    val dealDetails: String = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
)

@Composable
fun RoleTopBar(
    roleTitle: String,
    userName: String,
    onNotificationClick: () -> Unit = {}
) {
    val displayName = userName.substringBefore(" ").ifBlank { "XXX" }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var unreadCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(roleTitle, userName, showNotificationsDialog) {
        ApiService.fetchNotifications(roleHint = roleTitle) { res ->
            res.onSuccess { list ->
                unreadCount = list.count { !it.isRead }
            }
        }
    }

    if (showNotificationsDialog) {
        RoleNotificationsDialog(
            roleTitle = roleTitle,
            onDismiss = { showNotificationsDialog = false },
            onUnreadCountChanged = { unreadCount = it }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlobalBgGray)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_ict),
                    contentDescription = "Integra Logo",
                    modifier = Modifier
                        .requiredWidth(115.dp)
                        .height(34.dp),
                    alignment = Alignment.CenterStart,
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Welcome $roleTitle, $displayName",
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier.size(38.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                        .clickable {
                            showNotificationsDialog = true
                            onNotificationClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(GlobalRed)
                            .border(1.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            color = Color.Black,
            thickness = 1.dp
        )
    }
}

@Composable
fun RoleNotificationsDialog(
    roleTitle: String,
    token: String? = null,
    onDismiss: () -> Unit,
    onUnreadCountChanged: (Int) -> Unit = {}
) {
    var notifications by remember { mutableStateOf<List<NotificationDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedNotif by remember { mutableStateOf<NotificationDto?>(null) }
    var refreshTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(roleTitle, token, refreshTick) {
        loading = true
        ApiService.fetchNotifications(token = token, roleHint = roleTitle) { res ->
            loading = false
            res.onSuccess { list ->
                notifications = list
                onUnreadCountChanged(list.count { !it.isRead })
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = GlobalPrimaryBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notifications",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.Black
                    )
                }
                val unread = notifications.count { !it.isRead }
                if (unread > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEAF0FF))
                            .clickable {
                                ApiService.markAllNotificationsRead(token = token, roleHint = roleTitle) {
                                    notifications = notifications.map { it.copy(isRead = true) }
                                    onUnreadCountChanged(0)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Mark all read ($unread)",
                            color = GlobalPrimaryBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = GlobalPrimaryBlue,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                } else if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notifications yet.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    notifications.forEach { n ->
                        val isBroadcast = n.type?.contains("broadcast", ignoreCase = true) == true
                        val accentColor = when {
                            !n.isRead && isBroadcast -> GlobalRed
                            !n.isRead -> GlobalPrimaryBlue
                            else -> GlobalGreen
                        }
                        val rawDate = n.createdAt?.take(10)?.let { d ->
                            val p = d.split("-")
                            if (p.size == 3) "${p[2]}-${p[1]}-${p[0]}" else d
                        } ?: "15-11-2026"

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor)
                                .border(0.8.dp, Color.Black, RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedNotif = n
                                    if (!n.isRead && n.id > 0) {
                                        ApiService.markNotificationRead(token = token, notificationId = n.id)
                                        val updated = notifications.map {
                                            if (it.id == n.id) it.copy(isRead = true) else it
                                        }
                                        notifications = updated
                                        onUnreadCountChanged(updated.count { !it.isRead })
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 5.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (!n.isRead) Color(0xFFF5F8FF) else Color.White)
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = n.title.ifBlank { "Notification" },
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = if (!n.isRead) FontWeight.Bold else FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!n.isRead) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(GlobalPrimaryBlue)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = n.message,
                                    color = Color(0xFF424242),
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Date: $rawDate",
                                        color = Color.Gray,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (n.isRead) "Read" else "Tap to read",
                                        color = if (n.isRead) GlobalGreen else GlobalPrimaryBlue,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { refreshTick++ }) {
                    Text("Refresh", color = GlobalPrimaryBlue, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GlobalPrimaryBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    )

    selectedNotif?.let { detail ->
        AlertDialog(
            onDismissRequest = { selectedNotif = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text(
                    text = detail.title.ifBlank { "Notification Detail" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = detail.message,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    val d = detail.createdAt?.take(10) ?: "15-11-2026"
                    Text(
                        text = "Date: $d",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedNotif = null },
                    colors = ButtonDefaults.buttonColors(containerColor = GlobalPrimaryBlue)
                ) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun RoleBottomBar(
    centerLabel: String = "add project",
    onHome: () -> Unit,
    onCenterClick: () -> Unit,
    onProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White)
                .border(width = 0.5.dp, color = Color(0xFFBDBDBD))
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .width(72.dp)
                    .clickable { onHome() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Home",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Home",
                    color = Color.Black,
                    fontSize = 10.sp
                )
            }

            Column(
                modifier = Modifier
                    .width(110.dp)
                    .clickable { onCenterClick() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Spacer(modifier = Modifier.height(26.dp))
                Text(
                    text = centerLabel,
                    color = Color.Black,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            Column(
                modifier = Modifier
                    .width(72.dp)
                    .clickable { onProfile() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "profile",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "profile",
                    color = Color.Black,
                    fontSize = 10.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 2.dp)
                .size(46.dp)
                .clip(CircleShape)
                .background(GlobalPrimaryBlue)
                .clickable { onCenterClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = centerLabel,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

// ==========================================
// 1. GLOBAL PROJECT / DEALS DETAILS SCREEN
// ==========================================

@Composable
fun GlobalDealsDetailsRoute(
    session: LoginSession,
    selection: DealDetailSelection,
    centerBarLabel: String = "add project",
    onBack: () -> Unit,
    onHome: () -> Unit,
    onCenterAction: () -> Unit,
    onProfile: () -> Unit,
    onNotificationBellClick: () -> Unit = {},
    onExpired: () -> Unit
) {
    var loading by remember { mutableStateOf(selection.projectId > 0) }
    var clientName by remember { mutableStateOf(selection.clientName) }
    var returningClient by remember { mutableStateOf("Yes") }
    var clientPhone by remember { mutableStateOf(selection.clientPhone) }
    var agentResponsible by remember { mutableStateOf(selection.agentResponsible) }
    var productType by remember { mutableStateOf(selection.productType) }
    var dealType by remember { mutableStateOf(selection.dealType) }
    var initialPrice by remember { mutableDoubleStateOf(selection.initialPrice) }
    var dealDetails by remember { mutableStateOf(selection.dealDetails) }

    LaunchedEffect(session.token, selection.projectId) {
        if (selection.projectId <= 0) {
            loading = false
            return@LaunchedEffect
        }
        loading = true
        ApiService.globalProjectDetails(
            token = session.token,
            projectId = selection.projectId,
            fallbackClientName = selection.clientName,
            fallbackProduct = selection.productType,
            fallbackPrice = selection.initialPrice,
            fallbackAgent = selection.agentResponsible
        ) { result ->
            loading = false
            result.onSuccess { payload ->
                clientName = payload.clientName
                returningClient = if (payload.isReturningClient) "Yes" else "No"
                clientPhone = payload.clientPhone
                agentResponsible = payload.agentResponsible
                productType = payload.productType
                dealType = payload.dealType
                initialPrice = payload.initialPrice
                dealDetails = payload.dealDetails
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) {
                    onExpired()
                }
            }
        }
    }

    val roleLabel = when (session.user.role.lowercase()) {
        "upline" -> "Upline"
        "downline" -> "Downline"
        else -> "Admin"
    }

    val priceFormatted = remember(initialPrice) {
        if (initialPrice <= 0) "Rp.XXX,XXX"
        else {
            val nf = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }
            "Rp.${nf.format(initialPrice)}"
        }
    }

    val context = LocalContext.current
    var generatingPdf by remember { mutableStateOf(false) }
    var showWorkspace by remember { mutableStateOf(false) }
    var showDealWorkflow by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
    GlobalDealsDetailsScreenContent(
        roleTitle = roleLabel,
        userName = session.user.name,
        clientName = clientName,
        returningClient = returningClient,
        clientNumber = clientPhone,
        agentResponsible = agentResponsible,
        productType = productType,
        dealType = dealType,
        initialPriceFormatted = priceFormatted,
        dealDetails = dealDetails,
        centerBarLabel = centerBarLabel,
        loading = loading,
        generatingPdf = generatingPdf,
        onGenerateProposalPdf = {
            if (selection.projectId <= 0 || generatingPdf) return@GlobalDealsDetailsScreenContent
            generatingPdf = true
            ApiService.generateOrGetProposal(
                token = session.token,
                projectId = selection.projectId,
                clientName = clientName,
                productName = productType,
                price = initialPrice,
                details = dealDetails
            ) { res ->
                generatingPdf = false
                res.onSuccess { prop ->
                    Toast.makeText(
                        context,
                        "Proposal PDF Ready: ${prop.proposalNumber} (${prop.pdfFilePath})",
                        Toast.LENGTH_LONG
                    ).show()
                }
                res.onFailure { err ->
                    Toast.makeText(
                        context,
                        err.message ?: "Proposal generated on server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        },
        onBack = onBack,
        onNotificationBellClick = onNotificationBellClick,
        onHome = onHome,
        onCenterClick = onCenterAction,
        onProfile = onProfile
    )
    if (selection.projectId > 0L && session.user.role != "admin") {
        ExtendedFloatingActionButton(
            onClick = { showDealWorkflow = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 160.dp),
            containerColor = Color(0xFF0E9F6E), contentColor = Color.White,
            text = { Text("Deal & Komisi") },
            icon = { Icon(Icons.Default.Add, contentDescription = null) }
        )
    }
    if (selection.projectId > 0L) {
        ExtendedFloatingActionButton(
            onClick = { showWorkspace = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 88.dp),
            containerColor = GlobalPrimaryBlue,
            contentColor = Color.White,
            text = { Text("Workspace") },
            icon = { Icon(Icons.Default.Add, contentDescription = null) }
        )
    }
    }
    if (showDealWorkflow && selection.projectId > 0L) {
        IntegraAgentDealWorkflowDialog(
            token = session.token,
            currentUserId = session.user.id.toLong(),
            projectId = selection.projectId,
            onDismiss = { showDealWorkflow = false }
        )
    }
    if (showWorkspace && selection.projectId > 0L) {
        IntegraProjectWorkspaceDialog(projectId = selection.projectId, token = session.token,
            onDismiss = { showWorkspace = false })
    }

}

@Composable
fun GlobalDealsDetailsScreenContent(
    roleTitle: String = "Upline",
    userName: String = "XXX",
    clientName: String = "PT arunika Web",
    returningClient: String = "Yes",
    clientNumber: String = "08xxxxxxxx",
    agentResponsible: String = "Tony Stark",
    productType: String = "Web Program",
    dealType: String = "Follow-Up",
    initialPriceFormatted: String = "Rp.XXX,XXX",
    dealDetails: String = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore",
    centerBarLabel: String = "add project",
    loading: Boolean = false,
    generatingPdf: Boolean = false,
    onGenerateProposalPdf: () -> Unit = {},
    onBack: () -> Unit = {},
    onNotificationBellClick: () -> Unit = {},
    onHome: () -> Unit = {},
    onCenterClick: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    IntegraModernDealsDetailsScreen(
        roleTitle = roleTitle,
        userName = userName,
        clientName = clientName,
        returningClient = returningClient,
        clientNumber = clientNumber,
        agentResponsible = agentResponsible,
        productType = productType,
        dealType = dealType,
        initialPriceFormatted = initialPriceFormatted,
        dealDetails = dealDetails,
        centerBarLabel = centerBarLabel,
        loading = loading,
        generatingPdf = generatingPdf,
        onGenerateProposalPdf = onGenerateProposalPdf,
        onBack = onBack,
        onNotificationBellClick = onNotificationBellClick,
        onHome = onHome,
        onCenterClick = onCenterClick,
        onProfile = onProfile
    )
}

@Composable
private fun DetailKeyValueRow(
    label: String,
    value: String,
    alignTop: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = if (alignTop) Alignment.Top else Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            color = Color.Black,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

// ==========================================
// 2. GLOBAL PROFILE PAGE ("My Profile") WITH LOGOUT
// ==========================================

@Composable
fun GlobalProfileRoute(
    session: LoginSession,
    centerBarLabel: String = "add project",
    onBack: () -> Unit,
    onHome: () -> Unit,
    onCenterAction: () -> Unit,
    onLogout: () -> Unit,
    onNotificationBellClick: () -> Unit = {},
    onExpired: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var fullName by remember { mutableStateOf(session.user.name) }
    var userIdCode by remember { mutableStateOf(session.user.agentCode) }
    var phone by remember { mutableStateOf("Belum diisi") }
    var email by remember { mutableStateOf(session.user.email) }
    val familySince = "Belum tersedia" // Tanggal bergabung belum ada dalam UserProfileDto.
    var totalDeals by remember { mutableIntStateOf(0) }
    var dealsPerMonth by remember { mutableDoubleStateOf(0.0) }
    var accountActive by remember { mutableStateOf(true) }
    var loadingError by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(session.token, reloadKey) {
        loading = true
        loadingError = null
        ApiService.globalProfile(session.token) { result ->
            loading = false
            result.onSuccess { payload ->
                fullName = payload.profile.name.ifBlank { session.user.name }
                userIdCode = payload.profile.agentCode.ifBlank { session.user.agentCode }
                phone = payload.profile.phoneNumber?.takeIf { it.isNotBlank() } ?: "Belum diisi"
                email = payload.profile.email.ifBlank { session.user.email }
                accountActive = payload.profile.isActive
                totalDeals = payload.totalDeals
                dealsPerMonth = payload.dealsPerMonth
            }
            result.onFailure { err ->
                if (err is ApiError && err.unauthorized) onExpired()
                else loadingError = err.message ?: "Gagal memuat profile"
            }
        }
    }

    val roleLabel = when (session.user.role.lowercase()) {
        "upline" -> "Upline"
        "downline" -> "Downline"
        else -> "Admin"
    }

    GlobalProfileScreenContent(
        roleTitle = roleLabel,
        headerUserName = session.user.name,
        profileName = fullName,
        userIdCode = userIdCode,
        phoneNumber = phone,
        email = email,
        familySince = familySince,
        totalDeals = totalDeals,
        performanceText = "$dealsPerMonth deals/month",
        centerBarLabel = centerBarLabel,
        loading = loading,
        accountActive = accountActive,
        notificationToken = session.token,
        loadingError = loadingError,
        onReload = { reloadKey++ },
        onBack = onBack,
        onNotificationBellClick = onNotificationBellClick,
        onHome = onHome,
        onCenterClick = onCenterAction,
        onLogout = onLogout
    )
}

@Composable
fun GlobalProfileScreenContent(
    roleTitle: String = "Upline",
    headerUserName: String = "XXX",
    profileName: String = "Agent Integra",
    userIdCode: String = "UPL-001",
    phoneNumber: String = "Belum diisi",
    email: String = "agent@example.com",
    familySince: String = "Belum tersedia",
    totalDeals: Int = 0,
    performanceText: String = "0.0 deals/month",
    centerBarLabel: String = "add project",
    loading: Boolean = false,
    accountActive: Boolean = true,
    notificationToken: String? = null,
    loadingError: String? = null,
    onReload: () -> Unit = {},
    onBack: () -> Unit = {},
    onNotificationBellClick: () -> Unit = {},
    onHome: () -> Unit = {},
    onCenterClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    IntegraModernProfileScreen(
        roleTitle = roleTitle,
        headerUserName = headerUserName,
        profileName = profileName,
        userIdCode = userIdCode,
        phoneNumber = phoneNumber,
        email = email,
        familySince = familySince,
        totalDeals = totalDeals,
        performanceText = performanceText,
        centerBarLabel = centerBarLabel,
        loading = loading,
        accountActive = accountActive,
        notificationToken = notificationToken,
        loadingError = loadingError,
        onReload = onReload,
        onBack = onBack,
        onNotificationBellClick = onNotificationBellClick,
        onHome = onHome,
        onCenterClick = onCenterClick,
        onLogout = onLogout
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun GlobalDealsDetailsPreview() {
    GlobalDealsDetailsScreenContent()
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun GlobalProfilePreview() {
    GlobalProfileScreenContent()
}
