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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke

// UI profile modern. Data & API tetap dikelola oleh GlobalProfileRoute().
private val ProfBlue = Color(0xFF0758F7)
private val ProfBlueDark = Color(0xFF073DB6)
private val ProfInk = Color(0xFF17233E)
private val ProfMuted = Color(0xFF718098)
private val ProfBackground = Color(0xFFF5F7FC)
private val ProfStroke = Color(0xFFE4EAF4)
private val ProfGreen = Color(0xFF168D53)
private val ProfRed = Color(0xFFE34352)
private val ProfRadius = RoundedCornerShape(22.dp)

@Composable
fun IntegraModernProfileScreen(
    roleTitle: String,
    headerUserName: String,
    profileName: String,
    userIdCode: String,
    phoneNumber: String,
    email: String,
    familySince: String,
    totalDeals: Int,
    performanceText: String,
    centerBarLabel: String,
    loading: Boolean,
    accountActive: Boolean,
    notificationToken: String?,
    loadingError: String?,
    onReload: () -> Unit,
    onBack: () -> Unit,
    onNotificationBellClick: () -> Unit,
    onHome: () -> Unit,
    onCenterClick: () -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showLeaderboard by remember { mutableStateOf(false) }
    var unreadCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(notificationToken, roleTitle, showNotifications) {
        // Tidak mengeksekusi API di Preview jika token belum tersedia.
        if (!notificationToken.isNullOrBlank()) {
            ApiService.fetchNotifications(token = notificationToken, roleHint = roleTitle) { result ->
                result.onSuccess { unreadCount = it.count { notification -> !notification.isRead } }
            }
        }
    }

    if (showLeaderboard) {
        IntegraLeaderboardDialog(token = notificationToken.orEmpty(), onDismiss = { showLeaderboard = false })
    }

    if (showNotifications) {
        RoleNotificationsDialog(
            roleTitle = roleTitle,
            token = notificationToken,
            onDismiss = { showNotifications = false },
            onUnreadCountChanged = { unreadCount = it }
        )
    }

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("Keluar dari akun?", fontWeight = FontWeight.Bold) },
            text = { Text("Kamu perlu login kembali untuk mengakses Integra Agency.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirmation = false
                    onLogout()
                }) { Text("Ya, Logout", color = ProfRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Batal", color = ProfMuted)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp)
        )
    }

    Scaffold(
        containerColor = ProfBackground,
        bottomBar = {
            val adminNavigation = LocalIntegraAdminNav.current
            if (roleTitle.equals("Admin", ignoreCase = true) && adminNavigation != null) {
                AdminBottomBar(currentPage = AdminPage.PROFILE, onNavigate = adminNavigation)
            } else {
                ProfileModernBottomBar(
                    centerBarLabel = centerBarLabel,
                    onHome = onHome,
                    onAddProject = onCenterClick
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ProfileModernTopBar(
                userName = headerUserName,
                unreadCount = unreadCount,
                onNotificationClick = {
                    showNotifications = true
                    onNotificationBellClick()
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                        .background(ProfBlue, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("My Profile", color = ProfInk, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Detail akun dan performa agent", color = ProfMuted, fontSize = 12.sp)
                }
            }

            Column(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileHeroCard(
                    name = profileName,
                    code = userIdCode,
                    role = roleTitle,
                    isActive = accountActive
                )

                OutlinedButton(
                    onClick = { showLeaderboard = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Lihat Peringkat Agen", color = ProfBlue, fontWeight = FontWeight.SemiBold)
                }

                if (loading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().clip(CircleShape),
                        color = ProfBlue,
                        trackColor = Color(0xFFDFE9FD)
                    )
                }

                if (!loadingError.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFFFFF3F3),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFD9DB))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Data profile terbaru belum dapat dimuat.",
                                modifier = Modifier.weight(1f),
                                color = ProfRed,
                                fontSize = 12.sp
                            )
                            TextButton(onClick = onReload) { Text("Coba lagi") }
                        }
                    }
                }

                ProfileSectionTitle("Informasi Akun", "Informasi yang terdaftar pada akun kamu")
                Surface(
                    shape = ProfRadius,
                    color = Color.White,
                    border = BorderStroke(1.dp, ProfStroke),
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)) {
                        ProfileInfoItem(Icons.Default.Phone, "Phone Number", phoneNumber)
                        HorizontalDivider(color = ProfStroke)
                        ProfileInfoItem(Icons.Default.Email, "Email Address", email)
                        HorizontalDivider(color = ProfStroke)
                        ProfileInfoItem(Icons.Default.DateRange, "Family Since", familySince)
                    }
                }

                ProfileSectionTitle("Performance Overview", "Ringkasan performa penjualan")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileStatCard(
                        title = "Total Deals",
                        value = totalDeals.toString(),
                        description = "Deals tercatat",
                        icon = Icons.Default.WorkOutline,
                        accent = ProfBlue,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        title = "Performance",
                        value = performanceText.substringBefore(' '),
                        description = performanceText.substringAfter(' ', "deals/month"),
                        icon = Icons.Default.TrendingUp,
                        accent = ProfGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    shape = ProfRadius,
                    color = Color.White,
                    border = BorderStroke(1.dp, ProfStroke)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null,
                            tint = ProfGreen, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Integra Agency", color = ProfInk,
                                fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Manajemen keagenan dalam satu aplikasi",
                                color = ProfMuted, fontSize = 11.sp)
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showLogoutConfirmation = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ProfRed,
                        containerColor = Color(0xFFFFF5F5)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(19.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Logout", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProfileModernTopBar(
    userName: String,
    unreadCount: Int,
    onNotificationClick: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().height(65.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.logo_ict),
                contentDescription = "Logo Integra Agency",
                modifier = Modifier.size(37.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Integra Agency", color = ProfInk,
                    fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Text("Halo, ${userName.substringBefore(' ').ifBlank { "Agent" }}!",
                    color = ProfMuted, fontSize = 11.sp, maxLines = 1)
            }
            Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.size(38.dp).background(ProfBackground, CircleShape)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifikasi",
                        tint = ProfInk, modifier = Modifier.size(21.dp))
                }
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd).size(17.dp)
                            .background(ProfRed, CircleShape)
                            .border(1.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (unreadCount > 9) "9+" else unreadCount.toString(),
                            color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeroCard(name: String, code: String, role: String, isActive: Boolean) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        shadowElevation = 4.dp,
        color = ProfBlueDark
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(
                    listOf(Color(0xFF0D65FF), ProfBlue, ProfBlueDark)
                ))
                .padding(22.dp)
        ) {
            Text("AGENT PROFILE", fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.75f),
                fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(17.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(74.dp)
                        .background(Color.White.copy(alpha = 0.18f), CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profileInitials(name),
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(15.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name.ifBlank { "Agent Integra" }, color = Color.White,
                        fontSize = 21.sp, fontWeight = FontWeight.Bold,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(5.dp))
                    Text("Kode Agent: ${code.ifBlank { "-" }}",
                        color = Color.White.copy(alpha = 0.84f), fontSize = 11.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileHeroChip(role.uppercase(), Color.White.copy(alpha = 0.15f))
                ProfileHeroChip(
                    if (isActive) "AKUN AKTIF" else "AKUN NONAKTIF",
                    if (isActive) Color(0xFF169B5B) else Color(0xFFD75B45)
                )
            }
        }
    }
}

private fun profileInitials(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> "IA"
        words.size == 1 -> words.first().take(2).uppercase()
        else -> "${words.first().first()}${words.last().first()}".uppercase()
    }
}

@Composable
private fun ProfileHeroChip(label: String, fill: Color) {
    Box(modifier = Modifier.background(fill, RoundedCornerShape(50.dp))
        .padding(horizontal = 12.dp, vertical = 7.dp)) {
        Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProfileSectionTitle(title: String, caption: String) {
    Column {
        Text(title, color = ProfInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(caption, color = ProfMuted, fontSize = 12.sp)
    }
}

@Composable
private fun ProfileInfoItem(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Color(0xFFEAF1FF), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = ProfBlue, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = ProfMuted, fontSize = 11.sp)
            Spacer(Modifier.height(3.dp))
            Text(value.ifBlank { "Belum tersedia" },
                color = ProfInk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    description: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 146.dp),
        shape = ProfRadius,
        color = Color.White,
        border = BorderStroke(1.dp, ProfStroke),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(35.dp)
                    .background(accent.copy(alpha = 0.10f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(title, color = ProfMuted, fontSize = 11.sp, maxLines = 1)
            Spacer(Modifier.height(3.dp))
            Text(value, color = ProfInk, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(description, color = accent, fontSize = 10.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ProfileModernBottomBar(
    centerBarLabel: String,
    onHome: () -> Unit,
    onAddProject: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 7.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(74.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f).clickable { onHome() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Home, contentDescription = "Home", tint = ProfMuted,
                    modifier = Modifier.size(23.dp))
                Text("Home", color = ProfMuted, fontSize = 11.sp)
            }
            Column(
                modifier = Modifier.weight(1f).clickable { onAddProject() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(47.dp)
                        .background(ProfBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Project",
                        tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.height(2.dp))
                Text(centerBarLabel.replaceFirstChar { it.uppercase() },
                    color = ProfInk, fontSize = 10.sp, maxLines = 1)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Person, contentDescription = "Profile", tint = ProfBlue,
                    modifier = Modifier.size(23.dp))
                Text("Profile", color = ProfBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
