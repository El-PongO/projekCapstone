
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val PerformanceBlue = Color(0xFF0065FF)
private val PerformanceSky = Color(0xFF55A9F9)
private val PerformanceGreen = Color(0xFF00C83B)
private val PerformanceDarkBlue = Color(0xFF0738DF)

private fun formatPerformanceMoney(value: Double): String {
    return NumberFormat.getCurrencyInstance(
        Locale.forLanguageTag("id-ID")
    ).format(value).replace(",00", "")
}

@Composable
fun UplinePerformanceScreen(
    session: LoginSession,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onAddProject: () -> Unit,
    onProfile: () -> Unit,
    onExpired: () -> Unit
) {

    var data by remember {
        mutableStateOf<PerformanceData?>(null)
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf("")
    }

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    var refresh by remember {
        mutableIntStateOf(0)
    }

    var detailTitle by remember {
        mutableStateOf<String?>(null)
    }

    var detailText by remember {
        mutableStateOf("")
    }

    // Mengambil data melalui Backend
    LaunchedEffect(session.token, refresh) {
        loading = true
        error = ""

        ApiService.performance(session.token) { result ->
            loading = false

            result.onSuccess {
                data = it
                error = ""
            }

            result.onFailure {
                if (it is ApiError && it.unauthorized) {
                    onExpired()
                } else {
                    error = it.message ?: "Gagal mengambil data"
                }
            }
        }
    }

    Scaffold(
        containerColor = PerformanceSky,
        bottomBar = {
            PerformanceBottomBar(
                onHome = onHome,
                onAddProject = onAddProject,
                onEarnings = { selectedTab = 1 },
                onProfile = onProfile
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PerformanceSky)
        ) {

            // ======================
            // HEADER
            // ======================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    painter = painterResource(R.drawable.logo_ict),
                    contentDescription = "Logo Integra",
                    modifier = Modifier.size(39.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Welcome Upline, ${session.user.name.substringBefore(' ')}",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(PerformanceBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notification",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 18.dp),
                color = Color.Black
            )

            // ======================
            // BACK DAN JUDUL
            // ======================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PerformanceBlue)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(44.dp))

                Text(
                    text = "My Performance",
                    fontSize = 21.sp,
                    color = Color.Black
                )
            }

            // ======================
            // KONTEN PUTIH
            // ======================

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 26.dp,
                            topEnd = 26.dp
                        )
                    )
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {

                if (loading) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PerformanceBlue
                        )
                    }

                } else if (error.isNotBlank()) {

                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 12.sp
                    )

                    TextButton(onClick = { refresh++ }) {
                        Text("Coba lagi")
                    }

                } else {

                    val performance = data

                    if (performance != null) {

                        // ======================
                        // METRIC CARDS
                        // ======================

                        val totalDeals = performance.sales.count {
                            it.status.equals(
                                "deal",
                                ignoreCase = true
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            PerformanceMetricCard(
                                title = "Total Income",
                                value = formatPerformanceMoney(
                                    performance.totalIncome
                                ),
                                color = PerformanceGreen,
                                modifier = Modifier.weight(1f)
                            )

                            PerformanceMetricCard(
                                title = "Total Deals Made",
                                value = totalDeals.toString(),
                                color = PerformanceDarkBlue,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(28.dp))

                        // ======================
                        // TAB SALES / INCOME
                        // ======================

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(18.dp)
                        ) {

                            Text(
                                text = "My Sales Report",
                                color = if (selectedTab == 0)
                                    PerformanceBlue
                                else Color.DarkGray,
                                fontWeight = if (selectedTab == 0)
                                    FontWeight.Bold
                                else FontWeight.Normal,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable {
                                    selectedTab = 0
                                }
                            )

                            Text(
                                text = "My Income",
                                color = if (selectedTab == 1)
                                    PerformanceBlue
                                else Color.DarkGray,
                                fontWeight = if (selectedTab == 1)
                                    FontWeight.Bold
                                else FontWeight.Normal,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable {
                                    selectedTab = 1
                                }
                            )
                        }

                        Spacer(Modifier.height(5.dp))

                        HorizontalDivider(
                            color = Color.LightGray
                        )

                        Spacer(Modifier.height(12.dp))

                        // ======================
                        // SALES REPORT
                        // ======================

                        if (selectedTab == 0) {

                            if (performance.sales.isEmpty()) {
                                Text(
                                    "Belum ada penjualan.",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }

                            performance.sales.forEach { sale ->

                                PerformanceRecordCard(
                                    title = sale.client,
                                    subtitle = sale.code,
                                    amountLabel = "Deal Value",
                                    amount = formatPerformanceMoney(
                                        sale.value
                                    ),
                                    onViewDetails = {
                                        detailTitle = sale.client
                                        detailText =
                                            "Project: ${sale.code}\n" +
                                                    "Status: ${sale.status}\n" +
                                                    "Stage: ${sale.stage}\n" +
                                                    "Deal Value: ${
                                                        formatPerformanceMoney(
                                                            sale.value
                                                        )
                                                    }"
                                    }
                                )

                                Spacer(Modifier.height(7.dp))
                            }

                        } else {

                            // ======================
                            // MY INCOME
                            // ======================

                            if (performance.income.isEmpty()) {
                                Text(
                                    "Belum ada komisi.",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }

                            performance.income.forEach { income ->

                                PerformanceRecordCard(
                                    title = income.client,
                                    subtitle = income.code,
                                    amountLabel = "Commission",
                                    amount = formatPerformanceMoney(
                                        income.commission
                                    ),
                                    onViewDetails = {
                                        detailTitle = income.client
                                        detailText =
                                            "Project: ${income.code}\n" +
                                                    "Commission: ${
                                                        formatPerformanceMoney(
                                                            income.commission
                                                        )
                                                    }\n" +
                                                    "Rate: ${income.rate}%\n" +
                                                    "Date: ${income.date.take(10)}"
                                    }
                                )

                                Spacer(Modifier.height(7.dp))
                            }
                        }
                    }
                }
            }

            // Dekorasi bawah
            PerformanceDecoration()
        }
    }

    // ======================
    // DIALOG VIEW DETAILS
    // ======================

    detailTitle?.let { title ->

        AlertDialog(
            onDismissRequest = {
                detailTitle = null
            },

            title = {
                Text(title)
            },

            text = {
                Text(detailText)
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        detailTitle = null
                    }
                ) {
                    Text("Tutup")
                }
            }
        )
    }
}

// ==========================================
// METRIC CARD
// ==========================================

@Composable
private fun PerformanceMetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(70.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.End),
            maxLines = 1
        )
    }
}

// ==========================================
// SALES / INCOME CARD
// ==========================================

@Composable
private fun PerformanceRecordCard(
    title: String,
    subtitle: String,
    amountLabel: String,
    amount: String,
    onViewDetails: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.Black,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = Color.DarkGray,
                fontSize = 10.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {

            Text(
                text = "$amountLabel:",
                fontSize = 10.sp,
                color = Color.DarkGray
            )

            Text(
                text = amount,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )

            Spacer(Modifier.height(5.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFD9D9D9))
                    .clickable { onViewDetails() }
                    .padding(
                        horizontal = 16.dp,
                        vertical = 5.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "View Details",
                    color = Color.Black,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ==========================================
// DEKORASI WARNA BAWAH
// ==========================================

@Composable
private fun PerformanceDecoration() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(Color(0xFF33C451))
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFF34C759))
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFFDDC623))
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFFE59A18))
        )
    }
}

// ==========================================
// BOTTOM NAVIGATION
// ==========================================

@Composable
private fun PerformanceBottomBar(
    onHome: () -> Unit,
    onAddProject: () -> Unit,
    onEarnings: () -> Unit,
    onProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(PerformanceBlue),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        PerformanceBottomItem(
            title = "Home",
            icon = Icons.Default.Home,
            modifier = Modifier.weight(1f),
            onClick = onHome
        )

        PerformanceBottomItem(
            title = "add project",
            icon = Icons.Default.Add,
            modifier = Modifier.weight(1f),
            onClick = onAddProject
        )

        PerformanceBottomItem(
            title = "Earnings",
            icon = Icons.Default.AttachMoney,
            modifier = Modifier.weight(1f),
            onClick = onEarnings
        )

        PerformanceBottomItem(
            title = "profile",
            icon = Icons.Default.Person,
            modifier = Modifier.weight(1f),
            onClick = onProfile
        )
    }
}

@Composable
private fun PerformanceBottomItem(
    title: String,
    icon: ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (title == "add project") {

            Box(
                modifier = Modifier
                    .size(43.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0045E8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(27.dp)
                )
            }

        } else {

            Icon(
                icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(23.dp)
            )
        }

        Spacer(Modifier.height(3.dp))

        Text(
            text = title,
            color = Color.White,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}
