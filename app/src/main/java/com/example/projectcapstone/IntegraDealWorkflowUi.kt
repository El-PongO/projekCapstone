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
        } catch (e: Exception) { error = e.message ?: "Failed to load commission rate" }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deal Rate Agreement", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 470.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("The Admin sets the commission rate for this project. Each external commission recipient must accept it before the deal can be approved.")
                snapshot?.recipients?.forEach { recipient ->
                    key(recipient.agentId) {
                        HorizontalDivider()
                        Text("${recipient.agentCode} • ${recipient.role}", fontWeight = FontWeight.SemiBold)
                        if (recipient.agentType == "internal") {
                            Text("Internal agents: 0% commission (company revenue).")
                        } else {
                            var rateText by remember(recipient.agentId, recipient.offer?.ratePercent) {
                                mutableStateOf((recipient.offer?.ratePercent ?: recipient.defaultRate).toString())
                            }
                            Text("Status: ${recipient.offer?.status ?: "Not offered yet"}")
                            OutlinedTextField(
                                value = rateText, onValueChange = { rateText = it },
                                label = { Text("Commission rate (%)") }, singleLine = true,
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
                                        } catch (e: Exception) { error = e.message ?: "Failed to send commission rate" }
                                        finally { busy = false }
                                    }
                                }
                            ) { Text("Send offer") }
                        }
                    }
                }
                if (snapshot == null && error.isBlank()) CircularProgressIndicator()
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
                Text("Once all statuses are accepted, close this dialog and select Approve Deal.",
                    style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
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
        } catch (e: Exception) { error = e.message ?: "Unable to load project" }
    }
    val isOwner = project?.agentId == currentUserId

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deals & Commissions", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (project == null && error.isBlank()) CircularProgressIndicator()
                project?.let { p ->
                    Text("${p.projectCode} • Status ${p.status}")
                    Text("Project stage: ${p.stage}")
                    if (!isOwner) Text("You are viewing this project as its Upline. Only the project owner can mark it as complete.")
                }
                if (offers.isEmpty()) Text("There is no commission rate offer for your account on this project yet. If the deal has been submitted, wait for the Admin to send an offer.")
                offers.forEach { offer ->
                    HorizontalDivider()
                    Text("Commission offer: ${offer.ratePercent}%", fontWeight = FontWeight.SemiBold)
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
                                    } catch (e: Exception) { error = e.message ?: "Failed to accept commission rate" }
                                    finally { busy = false }
                                }
                            }) { Text("Accept rate") }
                            OutlinedButton(enabled = !busy, onClick = {
                                busy = true
                                scope.launch {
                                    try {
                                        App.retrofitService.respondDealRate("Bearer $token", offer.id,
                                            DealRateResponseRequest("rejected"))
                                        refresh++
                                    } catch (e: Exception) { error = e.message ?: "Failed to reject rate" }
                                    finally { busy = false }
                                }
                            }) { Text("Reject") }
                        }
                    }
                }
                if (isOwner && project?.status == "deal" && project?.stage != "completed") {
                    HorizontalDivider()
                    Text("When the work is fully complete, notify the Admin.")
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
                                    } catch (e: Exception) { error = e.message ?: "Failed to complete project" }
                                    finally { busy = false }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E9F6E))
                    ) { Text(if (confirmComplete) "Confirm completion and notify Admin" else "Complete project") }
                }
                if (project?.stage == "completed") {
                    Text("Project completed. The Admin will verify invoice payment and commission payout.",
                        color = Color(0xFF0E9F6E), fontWeight = FontWeight.Medium)
                }
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
