package com.example.projectcapstone

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

// Untuk ketiga role: hanya menampilkan kode agen, bukan nama atau kontak Downline.
@Composable
fun IntegraLeaderboardDialog(token: String, onDismiss: () -> Unit) {
    var entries by remember { mutableStateOf<List<LeaderboardEntryDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(token) {
        if (token.isBlank()) {
            loading = false
            error = "Please sign in again"
        } else {
            try {
                val payload = App.retrofitService.getLeaderboard("Bearer $token")
                entries = payload.ranking
            } catch (e: Exception) {
                error = e.message ?: "Failed to load the leaderboard"
            } finally {
                loading = false
            }
        }
    }
    val number = remember { NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agent Leaderboard", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Ranked by deal value. Downline identities are kept private.", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
                if (!loading && error == null && entries.isEmpty()) Text("No deal transactions yet.")
                LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
                    items(entries) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5FF))
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Text("#${entry.rank}", color = Color(0xFF1156D9), fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(entry.agentCode, fontWeight = FontWeight.Bold)
                                    Text("${entry.totalDeals} Deal · ${number.format(entry.totalDealValue)}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        containerColor = Color.White
    )
}
