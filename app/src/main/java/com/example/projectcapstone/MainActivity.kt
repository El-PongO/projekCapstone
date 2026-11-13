package com.example.projectcapstone

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val Navy = Color(0xFF1B2437)
private val NavyLight = Color(0xFF2C3E5E)
private val Blue = Color(0xFF3D6A9E)
private val Orange = Color(0xFFB4691F)
private val CanvasBg = Color(0xFFF0F3F8)
private val Ink = Color(0xFF1B2437)
private val Muted = Color(0xFF637188)
private val LightField = Color(0xFFF3F5F8)

private fun money(value: Double): String = NumberFormat.getCurrencyInstance(
    Locale.forLanguageTag("id-ID")
).format(value).replace(",00", "")

private fun roleLabel(role: String) = when (role) {
    "admin" -> "Admin"
    "upline" -> "Agen Upline"
    "downline" -> "Agen Downline"
    else -> "Pengguna"
}


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val store = SessionStore(this)

        setContent {

            var session by remember {
                mutableStateOf<LoginSession?>(null)
            }

            var showPerformance by remember {
                mutableStateOf(false)
            }

            // State untuk halaman My Downlines
            var showDownlines by remember {
                mutableStateOf(false)
            }

            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Blue,
                    secondary = Orange
                )
            ) {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CanvasBg
                ) {

                    val current = session

                    // =========================
                    // 1. HALAMAN LOGIN
                    // =========================
                    if (current == null) {

                        LoginScreen(
                            onLogin = { identifier, password, callback ->

                                ApiService.login(identifier, password) { result ->

                                    result.onSuccess { auth ->

                                        store.save(auth)
                                        session = auth

                                        showPerformance = false
                                        showDownlines = false
                                    }

                                    callback(result)
                                }
                            }
                        )
                    }

                    // =========================
                    // 2. HALAMAN PERFORMANCE
                    // =========================

                    else if (showPerformance) {

                        if (current.user.role.lowercase() == "upline") {

                            UplinePerformanceScreen(
                                session = current,

                                onBack = {
                                    showPerformance = false
                                },

                                onHome = {
                                    showPerformance = false
                                    showDownlines = false
                                },

                                onAddProject = {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Add Project belum dibuat",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },

                                onProfile = {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Profile belum dibuat",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },

                                onExpired = {
                                    store.clear()
                                    session = null
                                    showPerformance = false
                                    showDownlines = false
                                }
                            )

                        } else {

                            // Performance lama untuk Admin dan Downline
                            PerformanceScreen(
                                session = current,

                                onBack = {
                                    showPerformance = false
                                },

                                onExpired = {
                                    store.clear()
                                    session = null
                                    showPerformance = false
                                    showDownlines = false
                                }
                            )
                        }
                    }


                    // =========================
                    // 3. HALAMAN MY DOWNLINES
                    // =========================
                    else if (
                        current.user.role.lowercase() == "upline" &&
                        showDownlines
                    ) {

                        UplineDownlinesScreen(
                            session = current,

                            onBack = {
                                showDownlines = false
                            },

                            onAddProject = {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Add Project belum dibuat",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },

                            onEarnings = {
                                showPerformance = true
                            },

                            onProfile = {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Profile belum dibuat",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },

                            onExpired = {
                                store.clear()
                                session = null
                                showPerformance = false
                                showDownlines = false
                            }
                        )
                    }

                    // =========================
                    // 4. DASHBOARD UPLINE
                    // =========================
                    else if (
                        current.user.role.lowercase() == "upline"
                    ) {

                        UplineScreen(
                            uplineName = current.user.name,

                            onLogout = {
                                store.clear()
                                session = null
                                showPerformance = false
                                showDownlines = false
                            },

                            onMenuClick = { menu ->

                                when (menu) {

                                    "performance" -> {
                                        showPerformance = true
                                    }

                                    "downline" -> {
                                        showDownlines = true
                                    }

                                    "crm" -> {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "My CRM belum dibuat",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },

                            onBottomMenuClick = { menu ->

                                when (menu) {

                                    "home" -> {
                                        showDownlines = false
                                    }

                                    "add_project" -> {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Add Project belum dibuat",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    "earnings" -> {
                                        showPerformance = true
                                    }

                                    "profile" -> {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Profile belum dibuat",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        )
                    }

                    // =========================
                    // 5. ADMIN / DOWNLINE
                    // =========================
                    else {

                        DashboardScreen(
                            session = current,

                            onPerformance = {
                                showPerformance = true
                            },

                            onLogout = {
                                store.clear()
                                session = null
                                showPerformance = false
                                showDownlines = false
                            },

                            onExpired = {
                                store.clear()
                                session = null
                                showPerformance = false
                                showDownlines = false
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun LoginScreen(onLogin: (String, String, (Result<LoginSession>) -> Unit) -> Unit) {
    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val context = LocalContext.current

    fun submit() {
        error = when {
            identifier.isBlank() -> "Email atau kode agen wajib diisi."
            password.isBlank() -> "Password wajib diisi."
            else -> ""
        }
        if (error.isNotBlank() || loading) return
        loading = true
        onLogin(identifier.trim(), password) { result ->
            loading = false
            result.onFailure { e ->
                error = if (e is ApiError && e.unauthorized) {
                    "Email/kode agen atau password salah."
                } else {
                    "Tidak dapat terhubung ke server: ${e.message.orEmpty()}"
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF141B2B), NavyLight))
        )
    ) {
        Box(Modifier.align(Alignment.TopEnd).padding(top = 30.dp).size(190.dp)
            .background(Color(0x193D6A9E), CircleShape))
        Box(Modifier.align(Alignment.BottomStart).size(170.dp)
            .background(Color(0x19B4691F), CircleShape))
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(18.dp))
            Image(
                painter = painterResource(R.drawable.logo_ict),
                contentDescription = "Logo Integradata Cipta Tech",
                modifier = Modifier.width(250.dp).height(74.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(10.dp))
            Text("SISTEM KEAGENAN & PELACAKAN PROYEK", color = Color(0xFFCED7E3),
                fontSize = 11.sp, letterSpacing = 1.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 430.dp)
                    .shadow(14.dp, RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFC))
            ) {
                Column(Modifier.fillMaxWidth().padding(24.dp)) {
                    Text("Selamat datang!", color = Ink, fontSize = 24.sp,
                        fontWeight = FontWeight.Bold)
                    Text("Masuk untuk mengakses akun keagenan Anda.", color = Muted,
                        fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                    Spacer(Modifier.height(26.dp))
                    Text("Email / Kode Agen", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = identifier, onValueChange = { identifier = it; error = "" },
                        placeholder = { Text("id atau email", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
                    )
                    Spacer(Modifier.height(18.dp))
                    Text("Password", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password, onValueChange = { password = it; error = "" },
                        placeholder = { Text("Masukkan password", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (showPassword) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(onClick = { showPassword = !showPassword }) {
                                Text(if (showPassword) "Tutup" else "Lihat", fontSize = 12.sp)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() })
                    )
                    if (error.isNotBlank()) {
                        Text(error, color = Color(0xFFB42318), fontSize = 12.sp,
                            modifier = Modifier.padding(top = 12.dp))
                    }
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { submit() }, enabled = !loading,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue)
                    ) {
                        if (loading) CircularProgressIndicator(
                            modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White
                        ) else Text("MASUK", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp)
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Belum memiliki akun? ", color = Muted, fontSize = 12.sp)
                        Text("Hubungi Admin", color = Orange, fontSize = 12.sp,
                            fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_SENDTO,
                                        Uri.parse("mailto:admin@integraciptadata.com")))
                                } catch (_: ActivityNotFoundException) {
                                    Toast.makeText(context, "Tidak ada aplikasi email", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
            Text("© PT Integradata Cipta Tech • Capstone Project", fontSize = 11.sp,
                color = Color(0xFFA6B2C5), textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DashboardScreen(
    session: LoginSession,
    onPerformance: () -> Unit,
    onLogout: () -> Unit,
    onExpired: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var dashboard by remember { mutableStateOf<DashboardData?>(null) }
    var refreshNumber by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    LaunchedEffect(session.token, refreshNumber) {
        loading = true
        ApiService.dashboard(session.token) { result ->
            loading = false
            result.onSuccess { dashboard = it; error = "" }
            result.onFailure { failure ->
                if (failure is ApiError && failure.unauthorized) onExpired()
                else error = failure.message ?: "Server tidak dapat dihubungi"
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(CanvasBg).safeDrawingPadding(),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Box(Modifier.fillMaxWidth().background(
                Brush.verticalGradient(listOf(Navy, NavyLight))
            ).padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 30.dp)) {
                Column {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("INTEGRA AGENCY", color = Color(0xFFCAD6E7), fontSize = 11.sp,
                                fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                            Spacer(Modifier.height(10.dp))
                            Text("Halo, ${session.user.name.substringBefore(' ')}!",
                                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                            Spacer(Modifier.height(5.dp))
                            Text("Pantau aktivitas keagenan di sini.",
                                color = Color(0xFFCED9E9), fontSize = 13.sp)
                        }
                        TextButton(
                            onClick = onLogout,
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "Keluar",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(roleLabel(session.user.role).uppercase(), color = Color.White, fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0x664C7BA7)).padding(horizontal = 14.dp, vertical = 8.dp))
                }
            }
        }
        item {
            Text("Ringkasan Kinerja", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = Ink, modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 12.dp))
        }
        if (loading) {
            item { Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue)
            } }
        } else if (error.isNotBlank()) {
            item { ErrorPane(error) { refreshNumber++ } }
        } else {
            val stats = dashboard?.stats
            if (stats != null) {
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard("Total Proyek", "${stats.totalProjects}", Modifier.weight(1f))
                        MetricCard("Proyek Deal", "${stats.dealCount}", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard("Nilai Deal", money(stats.dealValue), Modifier.weight(1f))
                        val label = when (session.user.role) {
                            "admin" -> "Agen Aktif"
                            "upline" -> "Downline"
                            else -> "Komisi Saya"
                        }
                        val value = if (session.user.role == "downline")
                            money(stats.myCommission) else "${stats.agentCount}"
                        MetricCard(label, value, Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            Text("Menu Utama", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = Ink, modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 12.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionCard("My Performance", "Penjualan & komisi", Modifier.weight(1f), onPerformance)
                ActionCard("Proyek", "Dalam pengembangan", Modifier.weight(1f)) {
                    Toast.makeText(context, "Modul CRUD proyek berikutnya", Toast.LENGTH_SHORT).show()
                }
            }
        }
        item {
            Text("Proyek Terbaru", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = Ink, modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 12.dp))
        }
        val projects = dashboard?.recentProjects.orEmpty()
        if (!loading && error.isBlank() && projects.isEmpty()) {
            item { Text("Belum ada proyek yang tercatat.", color = Muted,
                modifier = Modifier.padding(horizontal = 22.dp)) }
        } else {
            items(projects) { project ->
                ProjectCard(project)
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(114.dp), shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            Text(value, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun ActionCard(title: String, caption: String, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.height(106.dp), shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.Center) {
            Text("↗", fontSize = 20.sp, color = Blue, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(caption, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ProjectCard(item: ProjectSummary) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 5.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(item.projectCode, color = Blue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f))
                StatusPill(item.status)
            }
            Spacer(Modifier.height(8.dp))
            Text(item.title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(5.dp))
            Text("${item.stage.replace('_', ' ')}  •  ${money(item.dealValue)}",
                color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun StatusPill(status: String) {
    val bg = when (status) {
        "deal" -> Color(0xFFE0F5E9)
        "follow_up" -> Color(0xFFE5F0FF)
        else -> Color(0xFFFCE8E8)
    }
    val fg = when (status) {
        "deal" -> Color(0xFF14743F)
        "follow_up" -> Color(0xFF265D9B)
        else -> Color(0xFFAC3B3B)
    }
    Text(status.replace('_', ' ').uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
        color = fg, modifier = Modifier.background(bg, RoundedCornerShape(30.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp))
}

@Composable
private fun ErrorPane(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, color = Color(0xFFB42318), textAlign = TextAlign.Center, fontSize = 13.sp)
        TextButton(onClick = onRetry) { Text("Coba lagi") }
    }
}

@Composable
private fun PerformanceScreen(session: LoginSession, onBack: () -> Unit, onExpired: () -> Unit) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var data by remember { mutableStateOf<PerformanceData?>(null) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var reload by remember { mutableIntStateOf(0) }

    LaunchedEffect(session.token, reload) {
        loading = true
        ApiService.performance(session.token) { result ->
            loading = false
            result.onSuccess { data = it; error = "" }
            result.onFailure {
                if (it is ApiError && it.unauthorized) onExpired()
                else error = it.message ?: "Gagal mengambil data"
            }
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(CanvasBg).safeDrawingPadding(),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().background(Navy).padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("‹ Kembali", color = Color.White) }
                Spacer(Modifier.width(8.dp))
                Text("My Performance", color = Color.White, fontSize = 20.sp,
                    fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
        }
        if (loading) {
            item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue)
            } }
        } else if (error.isNotBlank()) {
            item { ErrorPane(error) { reload++ } }
        } else {
            data?.let { perf ->
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard("Total Sales", money(perf.totalSales), Modifier.weight(1f))
                        MetricCard("Total Income", money(perf.totalIncome), Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(22.dp))
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        TextButton(onClick = { tab = 0 }, modifier = Modifier.weight(1f)) {
                            Text("Sales Report", color = if (tab == 0) Blue else Muted,
                                fontWeight = if (tab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                        TextButton(onClick = { tab = 1 }, modifier = Modifier.weight(1f)) {
                            Text("My Income", color = if (tab == 1) Blue else Muted,
                                fontWeight = if (tab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    HorizontalDivider(color = Color(0xFFD9DEE8), modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(Modifier.height(12.dp))
                }
                if (tab == 0) {
                    if (perf.sales.isEmpty()) {
                        item { Text("Belum ada penjualan.", color = Muted,
                            modifier = Modifier.padding(22.dp)) }
                    } else {
                        items(perf.sales) { sale ->
                            ProjectCard(ProjectSummary(sale.code, sale.client, sale.status, sale.stage, sale.value))
                        }
                    }
                } else {
                    if (perf.income.isEmpty()) {
                        item { Text("Belum ada komisi yang tercatat.", color = Muted,
                            modifier = Modifier.padding(22.dp)) }
                    } else {
                        items(perf.income) { income ->
                            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 5.dp),
                                shape = RoundedCornerShape(15.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(income.code, color = Blue, fontSize = 12.sp)
                                    Text(income.client, color = Ink, fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp)
                                    Spacer(Modifier.height(9.dp))
                                    Text(money(income.commission), color = Color(0xFF14743F),
                                        fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Text("Rate ${income.rate}%  •  ${income.date.take(10)}",
                                        color = Muted, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Integra Login")
@Composable
private fun LoginPreview() {
    MaterialTheme {
        LoginScreen(onLogin = { _, _, _ -> })
    }
}
