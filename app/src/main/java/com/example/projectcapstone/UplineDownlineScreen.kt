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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

private val TeamBlue = Color(0xFF0064FF)
private val TeamSkyBlue = Color(0xFF55A9FA)
private val TeamPurple = Color(0xFF4265FF)
private val TeamGray = Color(0xFFE0E0E0)

@Composable
fun UplineDownlinesScreen(
    session: LoginSession,
    onBack: () -> Unit,
    onAddProject: () -> Unit = {},
    onEarnings: () -> Unit = {},
    onProfile: () -> Unit = {},
    onExpired: () -> Unit = {}
) {
    var teamData by remember {
        mutableStateOf<TeamResponse?>(null)
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf("")
    }

    var search by remember {
        mutableStateOf("")
    }

    var selectedId by remember {
        mutableStateOf<Long?>(null)
    }

    var refresh by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(session.token, refresh) {
        loading = true
        error = ""

        UplineDownlineApi.service
            .getDownlines("Bearer ${session.token}")
            .enqueue(object : Callback<TeamResponse> {

                override fun onResponse(
                    call: Call<TeamResponse>,
                    response: Response<TeamResponse>
                ) {
                    loading = false

                    if (response.code() == 401) {
                        onExpired()
                        return
                    }

                    if (response.isSuccessful && response.body() != null) {
                        teamData = response.body()
                    } else {
                        error = "Gagal mengambil data Downline (${response.code()})"
                    }
                }

                override fun onFailure(
                    call: Call<TeamResponse>,
                    t: Throwable
                ) {
                    loading = false
                    error = t.message ?: "Server tidak dapat dihubungi"
                }
            })
    }

    Scaffold(
        containerColor = TeamSkyBlue,
        bottomBar = {
            TeamBottomBar(
                onHome = onBack,
                onAddProject = onAddProject,
                onEarnings = onEarnings,
                onProfile = onProfile
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TeamSkyBlue)
        ) {

            // Header Upline
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_ict),
                    contentDescription = "Integra Logo",
                    modifier = Modifier.size(42.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Welcome Upline, ${session.user.name.substringBefore(' ')}",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(TeamBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notification",
                        tint = Color.Black
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 18.dp),
                color = Color.Black
            )

            // Title + Back
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(horizontal = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TeamBlue)
                        .clickable {
                            if (selectedId == null) {
                                onBack()
                            } else {
                                selectedId = null
                            }
                        },
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
                    text = if (selectedId == null)
                        "My Downlines"
                    else
                        "Downline Details",
                    fontSize = 22.sp,
                    color = Color.Black
                )
            }

            // White main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp
                        )
                    )
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                if (selectedId != null) {
                    TeamDetailContent(
                        session = session,
                        downlineId = selectedId!!,
                        onExpired = onExpired
                    )
                } else {

                    // Team Performance
                    val summary = teamData?.summary

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(TeamPurple, Color(0xFF4366FF))
                                )
                            )
                            .padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Team Performance\nRatio",
                            color = Color.White,
                            fontSize = 17.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            "${summary?.downlineCount ?: 0} Downline\n" +
                                    "${summary?.totalDeals ?: 0} deals",
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                    }

                    Spacer(Modifier.height(13.dp))

                    // Search
                    TextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = {
                            Text(
                                "search downline",
                                fontSize = 12.sp
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(25.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = TeamGray,
                            unfocusedContainerColor = TeamGray,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(Modifier.height(24.dp))

                    Text(
                        "Agent Directory",
                        fontSize = 13.sp,
                        color = Color.Black
                    )

                    HorizontalDivider(color = Color.LightGray)

                    Spacer(Modifier.height(7.dp))

                    if (loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = TeamBlue)
                        }
                    } else if (error.isNotBlank()) {
                        Text(
                            error,
                            color = Color.Red,
                            fontSize = 12.sp
                        )

                        TextButton(onClick = { refresh++ }) {
                            Text("Coba lagi")
                        }
                    } else {

                        val filtered = teamData?.downlines
                            .orEmpty()
                            .filter {
                                it.agentCode.contains(
                                    search,
                                    ignoreCase = true
                                )
                            }

                        if (filtered.isEmpty()) {
                            Text(
                                text = "Tidak ada Downline ditemukan",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(20.dp)
                            )
                        }

                        filtered.forEach { downline ->
                            TeamAgentCard(
                                downline = downline,
                                onViewDetails = {
                                    selectedId = downline.id
                                }
                            )

                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

// =========================================
// AGENT CARD
// =========================================

@Composable
private fun TeamAgentCard(
    downline: TeamDownline,
    onViewDetails: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Color.Black,
                RoundedCornerShape(18.dp)
            )
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(37.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8E8E8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(9.dp))

            Column {
                Text(
                    text = "Downline ${downline.agentCode}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )

                Text(
                    text = "joined: ${downline.joinedAt?.take(10) ?: "-"}",
                    fontSize = 9.sp,
                    color = Color.DarkGray
                )

                Text(
                    text = "Agent code: ${downline.agentCode}",
                    fontSize = 9.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(Modifier.height(7.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TeamBlue)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Total Deals",
                    color = Color.White,
                    fontSize = 10.sp
                )
                Text(
                    "${downline.totalDeals}",
                    color = Color.White,
                    fontSize = 17.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "Performance",
                    color = Color.White,
                    fontSize = 10.sp
                )
                Text(
                    "${downline.dealsPerMonth} deals/month",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(5.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(TeamGray)
                    .clickable { onViewDetails() }
                    .padding(horizontal = 18.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "View Details",
                    fontSize = 10.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// =========================================
// DOWNLINE DETAILS
// =========================================

@Composable
private fun TeamDetailContent(
    session: LoginSession,
    downlineId: Long,
    onExpired: () -> Unit
) {
    var detail by remember(downlineId) {
        mutableStateOf<TeamDetailResponse?>(null)
    }

    var loading by remember(downlineId) {
        mutableStateOf(true)
    }

    var error by remember(downlineId) {
        mutableStateOf("")
    }

    var refresh by remember(downlineId) {
        mutableIntStateOf(0)
    }

    LaunchedEffect(session.token, downlineId, refresh) {
        loading = true
        error = ""

        UplineDownlineApi.service
            .getDownlineDetail(
                "Bearer ${session.token}",
                downlineId
            )
            .enqueue(object : Callback<TeamDetailResponse> {

                override fun onResponse(
                    call: Call<TeamDetailResponse>,
                    response: Response<TeamDetailResponse>
                ) {
                    loading = false

                    if (response.code() == 401) {
                        onExpired()
                        return
                    }

                    if (response.isSuccessful) {
                        detail = response.body()
                    } else {
                        error = "Gagal mengambil detail (${response.code()})"
                    }
                }

                override fun onFailure(
                    call: Call<TeamDetailResponse>,
                    t: Throwable
                ) {
                    loading = false
                    error = t.message ?: "Koneksi gagal"
                }
            })
    }

    if (loading) {
        CircularProgressIndicator(color = TeamBlue)
        return
    }

    if (error.isNotBlank()) {
        Text(error, color = Color.Red)
        TextButton(onClick = { refresh++ }) {
            Text("Coba lagi")
        }
        return
    }

    val data = detail ?: return
    val downline = data.downline

    Text(
        "Downline ${downline.agentCode}",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(Modifier.height(6.dp))

    Text(
        "Joined: ${downline.joinedAt?.take(10) ?: "-"}",
        fontSize = 12.sp,
        color = Color.Gray
    )

    Spacer(Modifier.height(15.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(TeamPurple)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Total Deals", color = Color.White)
            Text(
                "${downline.totalDeals}",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("Performance", color = Color.White)
            Text(
                "${downline.dealsPerMonth}/month",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    Spacer(Modifier.height(22.dp))

    Text(
        "Project History",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(Modifier.height(10.dp))

    if (data.projects.isEmpty()) {
        Text(
            "Belum ada proyek.",
            color = Color.Gray,
            fontSize = 13.sp
        )
    }

    data.projects.forEach { project ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 9.dp)
                .border(
                    1.dp,
                    Color.LightGray,
                    RoundedCornerShape(12.dp)
                )
                .padding(14.dp)
        ) {
            Text(
                project.code,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Text(
                "Status: ${project.status.replace('_', ' ')}",
                fontSize = 12.sp
            )

            Text(
                "Stage: ${project.stage.replace('_', ' ')}",
                fontSize = 12.sp
            )

            Text(
                NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("id-ID")
                ).format(project.dealValue),
                fontSize = 13.sp,
                color = TeamBlue,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// =========================================
// BOTTOM NAVIGATION
// =========================================

@Composable
private fun TeamBottomBar(
    onHome: () -> Unit,
    onAddProject: () -> Unit,
    onEarnings: () -> Unit,
    onProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(TeamBlue),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamBottomItem(
            label = "Home",
            onClick = onHome
        ) {
            Icon(Icons.Default.Home, null, tint = Color.White)
        }

        TeamBottomItem(
            label = "add project",
            onClick = onAddProject
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0046DD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    null,
                    tint = Color.White
                )
            }
        }

        TeamBottomItem(
            label = "Earnings",
            onClick = onEarnings
        ) {
            Icon(
                Icons.Default.AttachMoney,
                null,
                tint = Color.White
            )
        }

        TeamBottomItem(
            label = "profile",
            onClick = onProfile
        ) {
            Icon(
                Icons.Default.Person,
                null,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun RowScope.TeamBottomItem(
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()

        Text(
            label,
            color = Color.White,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}
