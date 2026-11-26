package com.example.projectcapstone

import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@Composable
fun IntegraProjectWorkspaceDialog(projectId: Long, token: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf<ProjectWorkspaceDto?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    var tab by remember { mutableIntStateOf(0) }
    var newTitle by remember { mutableStateOf("") }
    var taskWhen by remember { mutableStateOf("") }
    var scheduleType by remember { mutableStateOf("followup") }
    var documentType by remember { mutableStateOf("other") }
    fun message(text: String) = Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    LaunchedEffect(token, projectId, refresh) {
        busy = true
        try {
            data = App.retrofitService.getProjectWorkspace("Bearer $token", projectId)
            error = null
        } catch (e: Exception) { error = e.message ?: "Gagal memuat Workspace" }
        finally { busy = false }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            try {
                val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                val ext = when (mime) {
                    "application/pdf" -> "pdf"; "image/png" -> "png"; "image/jpeg" -> "jpg"
                    else -> throw IllegalArgumentException("File hanya PDF, PNG atau JPEG")
                }
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val out = java.io.ByteArrayOutputStream()
                        val buf = ByteArray(8192)
                        while (out.size() <= 5 * 1024 * 1024) {
                            val read = input.read(buf)
                            if (read < 0) break
                            out.write(buf, 0, read)
                        }
                        out.toByteArray()
                    } ?: throw IllegalArgumentException("Tidak dapat membuka dokumen")
                }
                if (bytes.size > 5 * 1024 * 1024) throw IllegalArgumentException("Ukuran maksimal 5MB")
                val part = MultipartBody.Part.createFormData("file", "lampiran_${System.currentTimeMillis()}.$ext", bytes.toRequestBody(mime.toMediaTypeOrNull()))
                App.retrofitService.uploadProjectDocument("Bearer $token", projectId, part,
                    documentType.toRequestBody("text/plain".toMediaTypeOrNull()))
                message("Dokumen berhasil diunggah")
                refresh++
            } catch (e: Exception) { message(e.message ?: "Upload gagal") }
            finally { busy = false }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Workspace Proyek #$projectId", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 530.dp).verticalScroll(rememberScrollState())) {
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
                data?.let { overview ->
                    Text("Progress ${overview.progressPercent}% · ${overview.completedMilestones}/${overview.totalMilestones} milestone selesai", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(progress = { overview.progressPercent / 100f }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    if (!overview.canEdit) {
                        Text("Ringkasan anonim. Catatan dan dokumen milik Downline tidak dapat dibuka.")
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Milestone", "Jadwal", "Dokumen").forEachIndexed { index, title ->
                                FilterChip(selected = tab == index, onClick = { tab = index }, label = { Text(title) })
                            }
                        }
                        when (tab) {
                            0 -> {
                                overview.milestones.forEach { item ->
                                    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                        Text(item.title, fontWeight = FontWeight.SemiBold)
                                        Text(item.status.replace('_', ' '), style = MaterialTheme.typography.bodySmall)
                                        if (item.status != "completed") TextButton(onClick = {
                                            scope.launch {
                                                try { App.retrofitService.updateProjectMilestone("Bearer $token", item.id, MilestoneStatusRequest()); refresh++ }
                                                catch (e: Exception) { message(e.message ?: "Update gagal") }
                                            }
                                        }) { Text("Tandai selesai") }
                                    }
                                    HorizontalDivider()
                                }
                                OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Milestone baru") }, singleLine = true)
                                TextButton(enabled = newTitle.isNotBlank(), onClick = {
                                    scope.launch {
                                        try { App.retrofitService.createProjectMilestone("Bearer $token", projectId, CreateMilestoneRequest(newTitle.trim())); newTitle = ""; refresh++ }
                                        catch (e: Exception) { message(e.message ?: "Gagal membuat milestone") }
                                    }
                                }) { Text("Tambah milestone") }
                            }
                            1 -> {
                                Text("Follow-up", fontWeight = FontWeight.SemiBold)
                                overview.tasks.forEach { Text("• ${it.title} · ${it.dueAt?.take(16) ?: "-"}") }
                                Text("Meeting", fontWeight = FontWeight.SemiBold)
                                overview.meetings.forEach { Text("• ${it.meetingTitle} · ${it.meetingAt?.take(16) ?: "-"}") }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(selected = scheduleType == "followup", onClick = { scheduleType = "followup" }, label = { Text("Follow-up") })
                                    FilterChip(selected = scheduleType == "meeting", onClick = { scheduleType = "meeting" }, label = { Text("Meeting") })
                                }
                                OutlinedTextField(value = newTitle, onValueChange = { newTitle = it },
                                    label = { Text(if (scheduleType == "meeting") "Judul meeting" else "Judul follow-up") })
                                OutlinedTextField(value = taskWhen, onValueChange = { taskWhen = it }, label = { Text("Jadwal ISO (2026-11-21T09:00:00+07:00)") })
                                TextButton(enabled = newTitle.isNotBlank() && taskWhen.isNotBlank(), onClick = {
                                    scope.launch {
                                        try {
                                            if (scheduleType == "meeting") App.retrofitService.createProjectMeeting("Bearer $token", projectId,
                                                CreateMeetingRequest(newTitle.trim(), taskWhen.trim()))
                                            else App.retrofitService.createProjectFollowUp("Bearer $token", projectId,
                                                CreateFollowUpRequest(newTitle.trim(), taskWhen.trim()))
                                            newTitle = ""; taskWhen = ""; refresh++
                                        } catch (e: Exception) { message(e.message ?: "Gagal menyimpan jadwal") }
                                    }
                                }) { Text(if (scheduleType == "meeting") "Tambah meeting" else "Tambah follow-up") }
                            }
                            else -> {
                                overview.documents.forEach { doc ->
                                    Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                                        Text(doc.originalFilename, fontWeight = FontWeight.SemiBold)
                                        Row {
                                            TextButton(onClick = {
                                                scope.launch {
                                                    try {
                                                        val body = App.retrofitService.downloadProjectDocument("Bearer $token", doc.id)
                                                        val folder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                                            ?: throw IllegalStateException("Penyimpanan tidak tersedia")
                                                        val safeName = doc.originalFilename.replace(Regex("[^A-Za-z0-9._-]"), "_")
                                                        val file = File(folder, "${doc.id}_$safeName")
                                                        withContext(Dispatchers.IO) { body.byteStream().use { input -> file.outputStream().use { output -> input.copyTo(output) } } }
                                                        message("Disimpan di folder Downloads aplikasi: ${file.name}")
                                                    } catch (e: Exception) { message(e.message ?: "Download gagal") }
                                                }
                                            }) { Text("Download") }
                                            TextButton(onClick = {
                                                scope.launch {
                                                    try { App.retrofitService.deleteProjectDocument("Bearer $token", doc.id); refresh++ }
                                                    catch (e: Exception) { message(e.message ?: "Hapus gagal") }
                                                }
                                            }) { Text("Hapus") }
                                        }
                                    }
                                    HorizontalDivider()
                                }
                                Text("Jenis dokumen", fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    listOf("other" to "Lain", "contract" to "Kontrak", "nda" to "NDA", "receipt" to "Bukti").forEach { (key, label) ->
                                        FilterChip(selected = documentType == key, onClick = { documentType = key }, label = { Text(label) })
                                    }
                                }
                                TextButton(onClick = { picker.launch(arrayOf("application/pdf", "image/png", "image/jpeg")) }) {
                                    Text("Upload PDF / PNG / JPG (maks. 5 MB)")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } },
        shape = RoundedCornerShape(22.dp)
    )
}
