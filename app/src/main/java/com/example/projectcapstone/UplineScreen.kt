package com.example.projectcapstone

// Sesuaikan package dengan package project kamu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Warna Integra Agency
private val PrimaryPurple = Color(0xFF3027B9)
private val LightPurple = Color(0xFF6258FF)
private val PrimaryBlue = Color(0xFF0065FF)
private val StatBlue = Color(0xFF0737DC)
private val StatGreen = Color(0xFF00C83B)

data class UplineDeal(
    val id: Int,
    val clientName: String,
    val amount: String
)

data class DownlineActivity(
    val agentCode: String,
    val description: String,
    val time: String
)

@Composable
fun UplineScreen(
    uplineName: String = "XXX",
    monthlyCut: String = "\$20,000",
    projectsHeld: Int = 8,
    deals: List<UplineDeal> = listOf(
        UplineDeal(1, "PT Surya Makmur", "\$10,000")
    ),
    activities: List<DownlineActivity> = listOf(
        DownlineActivity(
            "DNL-001",
            "Memperbarui status proyek menjadi Follow Up",
            "10 menit lalu"
        ),
        DownlineActivity(
            "DNL-002",
            "Menambahkan proyek baru",
            "30 menit lalu"
        )
    ),
    onNotificationClick: () -> Unit = {},
    onMenuClick: (String) -> Unit = {},
    onDealStatusClick: (Int, String) -> Unit = { _, _ -> },
    onBottomMenuClick: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Scaffold(
        containerColor = PrimaryPurple,
        bottomBar = {
            UplineBottomNavigation(onBottomMenuClick)
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PrimaryPurple)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp)
            ) {

                // Header
                UplineHeader(
                    name = uplineName,
                    onNotificationClick = onNotificationClick,
                    onLogout = onLogout
                )

                HorizontalDivider(
                    color = Color.Black.copy(alpha = 0.6f),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Statistik
                UplineStatistics(
                    monthlyCut = monthlyCut,
                    projectsHeld = projectsHeld
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Menu Upline
                UplineMainMenu(onMenuClick)

                Spacer(modifier = Modifier.height(20.dp))

                // Active Deals
                UplineActiveDeals(
                    deals = deals,
                    onStatusClick = onDealStatusClick
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Recent Downline Activity
                Text(
                    text = "Recent Downline Activity",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 7.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                activities.forEach { activity ->
                    DownlineActivityCard(activity)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// ==========================================
// HEADER
// ==========================================

@Composable
private fun UplineHeader(
    name: String,
    onNotificationClick: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(27.dp)
                    .rotate(45f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF00E5FF),
                                Color(0xFF003DFF),
                                Color(0xFFFF0099),
                                Color(0xFFFFC400)
                            )
                        )
                    )
            )

            Text(
                text = "◆",
                color = Color.White,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Welcome Upline, $name",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF168BFF))
                .clickable { onNotificationClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color.Black,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

// ==========================================
// STATISTICS
// ==========================================

@Composable
private fun UplineStatistics(
    monthlyCut: String,
    projectsHeld: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .height(70.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(StatGreen)
                .padding(11.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "This Month's Cut",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            Text(
                text = monthlyCut,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.End)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .height(70.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(StatBlue)
                .padding(11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Projects Held",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.Top)
            )

            Text(
                text = projectsHeld.toString(),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
    }
}

// ==========================================
// UPLINE MENU
// ==========================================

@Composable
private fun UplineMainMenu(
    onMenuClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(25.dp))
            .background(LightPurple)
    ) {

        Text(
            text = "my Upline menu",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(
                start = 22.dp,
                top = 9.dp,
                bottom = 8.dp
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(Color.White)
                .padding(horizontal = 9.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            UplineMenuItem(
                title = "My\nPerformance",
                iconType = "star",
                modifier = Modifier.weight(1f)
            ) {
                onMenuClick("performance")
            }

            UplineMenuItem(
                title = "Downline",
                iconType = "person",
                modifier = Modifier.weight(1f)
            ) {
                onMenuClick("downline")
            }

            UplineMenuItem(
                title = "My CRM",
                iconType = "crm",
                modifier = Modifier.weight(1f)
            ) {
                onMenuClick("crm")
            }
        }
    }
}

@Composable
private fun UplineMenuItem(
    title: String,
    iconType: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(35.dp)
                .clip(CircleShape)
                .background(Color(0xFF9855FF)),
            contentAlignment = Alignment.Center
        ) {
            when (iconType) {
                "star" -> Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(23.dp)
                )

                "person" -> Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(23.dp)
                )

                else -> Text(
                    text = "CRM",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            color = Color.Black,
            fontSize = 11.sp,
            lineHeight = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ==========================================
// ACTIVE DEALS
// ==========================================

@Composable
private fun UplineActiveDeals(
    deals: List<UplineDeal>,
    onStatusClick: (Int, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🤝",
                fontSize = 19.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Active deals",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (deals.isEmpty()) {
            Text(
                text = "Belum ada active deals",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        deals.forEach { deal ->

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightPurple)
                    .padding(9.dp)
            ) {

                Text(
                    text = deal.clientName,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Going for: ${deal.amount}",
                    color = Color.White,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {

                    DealStatusButton(
                        title = "no deal",
                        color = Color(0xFFED144E)
                    ) {
                        onStatusClick(deal.id, "no_deal")
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    DealStatusButton(
                        title = "Counter",
                        color = Color(0xFF2996F3)
                    ) {
                        onStatusClick(deal.id, "counter")
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    DealStatusButton(
                        title = "Deal",
                        color = Color(0xFF00E850)
                    ) {
                        onStatusClick(deal.id, "deal")
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun DealStatusButton(
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(55.dp)
            .height(21.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color.Black,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ==========================================
// DOWNLINE ACTIVITY
// ==========================================

@Composable
private fun DownlineActivityCard(
    activity: DownlineActivity
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFD2D2D2))
            .padding(11.dp),
        verticalAlignment = Alignment.Top
    ) {

        Box(
            modifier = Modifier
                .size(27.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(21.dp)
            )
        }

        Spacer(modifier = Modifier.width(9.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = activity.agentCode,
                color = Color.DarkGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = activity.description,
                color = Color.DarkGray,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = activity.time,
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

// ==========================================
// BOTTOM NAVIGATION
// ==========================================

@Composable
private fun UplineBottomNavigation(
    onMenuClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(PrimaryBlue)
            .padding(horizontal = 5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        BottomMenuItem(
            title = "Home",
            type = "home",
            modifier = Modifier.weight(1f)
        ) {
            onMenuClick("home")
        }

        BottomMenuItem(
            title = "addproject",
            type = "add",
            modifier = Modifier.weight(1f)
        ) {
            onMenuClick("add_project")
        }

        BottomMenuItem(
            title = "Earnings",
            type = "money",
            modifier = Modifier.weight(1f)
        ) {
            onMenuClick("earnings")
        }

        BottomMenuItem(
            title = "profile",
            type = "profile",
            modifier = Modifier.weight(1f)
        ) {
            onMenuClick("profile")
        }
    }
}

@Composable
private fun BottomMenuItem(
    title: String,
    type: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (type == "add") {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0048EE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Project",
                    tint = Color.White,
                    modifier = Modifier.size(27.dp)
                )
            }
        } else {
            when (type) {
                "home" -> Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )

                "profile" -> Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )

                "money" -> Text(
                    text = "$",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            color = Color.White,
            fontSize = 10.sp,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}