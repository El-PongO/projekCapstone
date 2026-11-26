package com.example.projectcapstone

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** Admin menawarkan rate komisi per proyek. Rate wajib diterima penerima sebelum Deal approved. */
@Composable
fun IntegraAdminDealRateDialog(
    token: String,
    approvalId: Long,
    onDismiss: () -> Unit,
    onUpdated: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var snapshot by remember(approvalId) { mutableStateOf<DealRateReviewDto?>(null) }
    var error by remember(approvalId) { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }

    LaunchedEffect(approvalId, refresh) {
        try {
            snapshot = App.retrofitService.getDealRateReview("Bearer $token", approvalId)
            error = ""
        } catch (e: Exception) { error = e.message ?: "Gagal memuat rate" }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kesepakatan Rate Deal", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 470.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Admin menetapkan rate untuk proyek ini. Setiap penerima komisi eksternal harus menyetujuinya sebelum Deal di-approve.")
                snapshot?.recipients?.forEach { recipient ->
                    key(recipient.agentId) {
                        HorizontalDivider()
                        Text("${recipient.agentCode} • ${recipient.role}", fontWeight = FontWeight.SemiBold)
                        if (recipient.agentType == "internal") {
                            Text("Agen internal: 0% komisi (pendapatan perusahaan).")
                        } else {
                            var rateText by remember(recipient.agentId, recipient.offer?.ratePercent) {
                                mutableStateOf((recipient.offer?.ratePercent ?: recipient.defaultRate).toString())
                            }
                            Text("Status: ${recipient.offer?.status ?: "belum ditawarkan"}")
                            OutlinedTextField(
                                value = rateText, onValueChange = { rateText = it },
                                label = { Text("Rate komisi (%)") }, singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                enabled = !busy && rateText.toDoubleOrNull()?.let { it in 0.0..100.0 } == true,
                                onClick = {
                                    val rate = rateText.toDoubleOrNull() ?: return@Button
                                    busy = true
                                    scope.launch {
                                        try {
                                            App.retrofitService.proposeDealRate(
                                                "Bearer $token", approvalId, recipient.agentId,
                                                DealRateProposalRequest(rate)
                                            )
                                            refresh++
                                            onUpdated()
                                        } catch (e: Exception) { error = e.message ?: "Gagal mengirim rate" }
                                        finally { busy = false }
                                    }
                                }
                            ) { Text("Kirim penawaran") }
                        }
                    }
                }
                if (snapshot == null && error.isBlank()) CircularProgressIndicator()
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
                Text("Setelah semua status 'accepted', tutup dialog dan tekan Approve Deal.",
                    style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } }
    )
}

/** Agen hanya dapat menerima rate miliknya dan menyelesaikan proyek miliknya sendiri. */
@Composable
fun IntegraAgentDealWorkflowDialog(
    token: String,
    currentUserId: Long,
    projectId: Long,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var project by remember(projectId) { mutableStateOf<ProjectDto?>(null) }
    var offers by remember(projectId) { mutableStateOf<List<DealRateOfferDto>>(emptyList()) }
    var error by remember(projectId) { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var confirmComplete by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }

    LaunchedEffect(projectId, refresh) {
        try {
            project = App.retrofitService.getProjectDetail("Bearer $token", projectId).project
            offers = App.retrofitService.getMyDealRates("Bearer $token", projectId)
            error = ""
        } catch (e: Exception) { error = e.message ?: "Tidak dapat membaca proyek" }
    }
    val isOwner = project?.agentId == currentUserId

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deal & Komisi", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (project == null && error.isBlank()) CircularProgressIndicator()
                project?.let { p ->
                    Text("${p.projectCode} • Status ${p.status}")
                    Text("Tahap pengerjaan: ${p.stage}")
                    if (!isOwner) Text("Kamu melihat proyek ini sebagai Upline pengampu. Hanya pemilik proyek yang boleh menyelesaikannya.")
                }
                if (offers.isEmpty()) Text("Belum ada penawaran rate komisi untuk akunmu pada proyek ini. Jika Deal sedang diajukan, tunggu Admin mengirimkan rate.")
                offers.forEach { offer ->
                    HorizontalDivider()
                    Text("Penawaran komisi: ${offer.ratePercent}%", fontWeight = FontWeight.SemiBold)
                    Text("Status: ${offer.status}")
                    if (offer.status == "pending" && project?.status == "follow_up") {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(enabled = !busy, onClick = {
                                busy = true
                                scope.launch {
                                    try {
                                        App.retrofitService.respondDealRate("Bearer $token", offer.id,
                                            DealRateResponseRequest("accepted"))
                                        refresh++
                                    } catch (e: Exception) { error = e.message ?: "Gagal menyetujui rate" }
                                    finally { busy = false }
                                }
                            }) { Text("Setujui Rate") }
                            OutlinedButton(enabled = !busy, onClick = {
                                busy = true
                                scope.launch {
                                    try {
                                        App.retrofitService.respondDealRate("Bearer $token", offer.id,
                                            DealRateResponseRequest("rejected"))
                                        refresh++
                                    } catch (e: Exception) { error = e.message ?: "Gagal menolak rate" }
                                    finally { busy = false }
                                }
                            }) { Text("Tolak") }
                        }
                    }
                }
                if (isOwner && project?.status == "deal" && project?.stage != "completed") {
                    HorizontalDivider()
                    Text("Jika pekerjaan sudah benar-benar selesai, kirim pemberitahuan ke Admin.")
                    Button(
                        enabled = !busy,
                        onClick = {
                            if (!confirmComplete) {
                                confirmComplete = true
                            } else {
                                busy = true
                                scope.launch {
                                    try {
                                        App.retrofitService.updateProjectStage("Bearer $token", projectId,
                                            UpdateStageRequest("completed"))
                                        refresh++
                                        confirmComplete = false
                                    } catch (e: Exception) { error = e.message ?: "Gagal menyelesaikan proyek" }
                                    finally { busy = false }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E9F6E))
                    ) { Text(if (confirmComplete) "Konfirmasi: Selesai & Beri Tahu Admin" else "Selesaikan Proyek") }
                }
                if (project?.stage == "completed") {
                    Text("Proyek selesai. Admin akan memeriksa pelunasan invoice dan pencairan komisi.",
                        color = Color(0xFF0E9F6E), fontWeight = FontWeight.Medium)
                }
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } }
    )
}
