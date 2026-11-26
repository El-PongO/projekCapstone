package com.example.projectcapstone

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class ApiError(message: String, val unauthorized: Boolean = false) : IOException(message)

object ApiService {
    // Di sini hanya pemanggilan WebService; tidak membuat HttpURLConnection manual.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Client-side metadata cache so Upline can display real Client Names and act on Downline deals
    // without modifying the backend API.
    private val projectIdToClientName = java.util.concurrent.ConcurrentHashMap<Long, String>()
    private val projectCodeToClientName = java.util.concurrent.ConcurrentHashMap<String, String>()
    private val agentCodeToClientName = java.util.concurrent.ConcurrentHashMap<String, String>()
    private val roleTokens = java.util.concurrent.ConcurrentHashMap<String, String>()
    private val agentCodeTokens = java.util.concurrent.ConcurrentHashMap<String, String>()
    private val localStatusOverrides = java.util.concurrent.ConcurrentHashMap<Long, String>()
    private val localValueOverrides = java.util.concurrent.ConcurrentHashMap<Long, Double>()

    private fun metaPrefs() = App.appContext?.getSharedPreferences("integra_project_meta", Context.MODE_PRIVATE)

    private fun isAnonymizedOrCode(name: String?): Boolean {
        if (name.isNullOrBlank()) return true
        val trimmed = name.trim()
        if (trimmed.startsWith("Proyek ", ignoreCase = true)) return true
        if (trimmed.startsWith("Komisi Tim", ignoreCase = true)) return true
        if (trimmed.matches(Regex("^(DNL|UPL|ADM)-\\d+.*", RegexOption.IGNORE_CASE))) return true
        return false
    }

    // Token tidak boleh disimpan lintas akun/role untuk mengakses proyek orang lain.
    fun rememberSessionToken(role: String, agentCode: String, token: String) {
        // Hapus token lama milik akun lain dari versi aplikasi sebelumnya.
        val prefs = metaPrefs() ?: return
        val editor = prefs.edit()
        prefs.all.keys.filter { it.startsWith("tok_role_") || it.startsWith("tok_code_") ||
            it.startsWith("cid_") || it.startsWith("ccode_") || it.startsWith("cagent_") }
            .forEach { editor.remove(it) }
        editor.apply()
        roleTokens.clear()
        agentCodeTokens.clear()
        projectIdToClientName.clear()
        projectCodeToClientName.clear()
        agentCodeToClientName.clear()
    }

    // Tidak menyimpan identitas klien lintas akun; data hanya berasal dari endpoint berizin.
    fun rememberProjectClient(projectId: Long, projectCode: String? = null,
                              agentCode: String? = null, clientName: String?) = Unit

    fun resolveClientName(
        rawClientName: String?, projectId: Long = 0L,
        projectCode: String? = null, agentCode: String? = null
    ): String {
        // Jangan menggunakan cache dari akun yang login sebelumnya untuk memulihkan data privat.
        if (!rawClientName.isNullOrBlank()) return rawClientName
        return "Proyek ${projectCode ?: agentCode ?: projectId}"
    }

    private fun candidateTokens(primaryToken: String): List<String> = listOf(primaryToken)

    private fun errorMessage(error: Exception): ApiError {
        if (error is HttpException) {
            val body = runCatching { error.response()?.errorBody()?.string() }.getOrNull().orEmpty()
            val message = runCatching { JSONObject(body).optString("error") }.getOrNull()
                ?.ifBlank { null } ?: "HTTP ${error.code()}"
            return ApiError(message, unauthorized = error.code() == 401)
        }
        return ApiError(error.message ?: "Koneksi gagal")
    }

    private val sentFcmTokens = java.util.concurrent.ConcurrentHashMap<String, String>()

    fun registerFcmDeviceToken(token: String) {
        val ctx = App.appContext ?: return
        PushNotificationHelper.obtainDeviceFcmToken(ctx) { fcmToken ->
            sendFcmTokenToBackend(token, fcmToken)
        }
    }

    private fun sendFcmTokenToBackend(jwt: String, fcmToken: String) {
        if (fcmToken.isBlank() || sentFcmTokens[jwt] == fcmToken) return
        scope.launch {
            runCatching {
                App.retrofitService.registerDeviceToken(
                    "Bearer $jwt", RegisterDeviceTokenRequest(fcmToken = fcmToken)
                )
            }.onSuccess {
                sentFcmTokens[jwt] = fcmToken
                Log.d("IntegraFCM", "Token perangkat terdaftar pada backend")
            }.onFailure { error ->
                Log.e("IntegraFCM", "Gagal mendaftarkan token: ${error.message}")
            }
        }
    }

    /** Dipanggil saat FCM melakukan token rotation. */
    fun registerRefreshedFcmToken(fcmToken: String) {
        val ctx = App.appContext ?: return
        val session = SessionStore(ctx).restore() ?: return
        sendFcmTokenToBackend(session.token, fcmToken)
    }

    /** Best-effort: kosongkan mapping FCM di backend saat logout. */
    fun unregisterFcmDeviceToken(jwt: String) {
        sentFcmTokens.remove(jwt)
        val ctx = App.appContext ?: return
        scope.launch {
            runCatching {
                App.retrofitService.registerDeviceToken(
                    "Bearer $jwt", RegisterDeviceTokenRequest(fcmToken = "")
                )
            }.onFailure { error ->
                Log.w("IntegraFCM", "Gagal unregister perangkat: ${error.message}")
            }
            PushNotificationHelper.deleteLocalFcmToken(ctx)
        }
    }

    fun syncFcmAndPollNotifications(token: String, userKey: String) {
        val ctx = App.appContext ?: return
        // Registrasi token hanya bila belum berhasil untuk session ini.
        if (!sentFcmTokens.containsKey(token)) registerFcmDeviceToken(token)
        scope.launch {
            runCatching {
                val notifs = App.retrofitService.getNotifications("Bearer $token")
                PushNotificationHelper.dispatchUnreadNotifications(ctx, userKey, notifs)
            }
        }
    }

    fun login(identifier: String, password: String, done: (Result<LoginSession>) -> Unit) {
        scope.launch {
            if (BuildConfig.DEBUG) Log.d("IntegraLogin", "LOGIN_START")
            val result = try {
                val response = App.retrofitService.login(LoginRequest(identifier, password))
                rememberSessionToken(response.user.role, response.user.agentCode, response.token)
                registerFcmDeviceToken(response.token)
                if (BuildConfig.DEBUG) Log.d("IntegraLogin", "LOGIN_OK role=${response.user.role}")
                Result.success(LoginSession(response.token, response.user))
            } catch (error: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e("IntegraLogin", "LOGIN_FAILED ${error.javaClass.simpleName}: ${error.message}", error)
                }
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun dashboard(token: String, done: (Result<DashboardData>) -> Unit) {
        scope.launch {
            val result = try {
                val response = App.retrofitService.getDashboard("Bearer $token")
                Result.success(DashboardData(response.stats, response.recentProjects))
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun performance(token: String, done: (Result<PerformanceData>) -> Unit) {
        scope.launch {
            val result = try {
                val response = App.retrofitService.getPerformance("Bearer $token")
                Result.success(PerformanceData(
                    totalSales = response.stats.totalSales,
                    totalIncome = response.stats.totalIncome,
                    sales = response.sales,
                    income = response.income,
                    totalDealsMade = response.stats.totalDealsMade,
                    monthlyCut = response.stats.monthlyCut
                ))
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    data class DownlineHomePayload(
        val dashboard: DownlineDashboardResponse,
        val monthlyCut: Double,
        val totalIncome: Double,
        val upline: UplineInfo?,
        val notifications: List<NotificationDto>
    )

    fun downlineHome(token: String, done: (Result<DownlineHomePayload>) -> Unit) {
        scope.launch {
            val result = try {
                val bearer = "Bearer $token"
                val dash = App.retrofitService.getDownlineDashboard(bearer)
                val perf = runCatching { App.retrofitService.getPerformance(bearer) }.getOrNull()
                val notifs = runCatching { App.retrofitService.getNotifications(bearer) }.getOrDefault(emptyList())
                App.appContext?.let { PushNotificationHelper.dispatchUnreadNotifications(it, "downline_${token.takeLast(8)}", notifs) }
                val uplineExtra = if (dash.upline == null) {
                    runCatching { App.retrofitService.getMyUplineInfo(bearer).upline }.getOrNull()
                } else {
                    dash.upline
                }
                val cut = when {
                    perf != null && perf.stats.monthlyCut > 0 -> perf.stats.monthlyCut
                    dash.summary.myCommission > 0 -> dash.summary.myCommission
                    perf != null -> perf.stats.totalIncome
                    else -> 0.0
                }
                Result.success(
                    DownlineHomePayload(
                        dashboard = dash,
                        monthlyCut = cut,
                        totalIncome = perf?.stats?.totalIncome ?: dash.summary.myCommission,
                        upline = uplineExtra,
                        notifications = notifs
                    )
                )
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun resolveActiveToken(explicitToken: String? = null, roleHint: String? = null): String? {
        if (!explicitToken.isNullOrBlank()) return explicitToken
        return App.appContext?.let { SessionStore(it).restore()?.token }
    }

    fun fetchNotifications(
        token: String? = null,
        roleHint: String? = null,
        done: (Result<List<NotificationDto>>) -> Unit
    ) {
        scope.launch {
            val result = try {
                val activeTok = resolveActiveToken(token, roleHint)
                    ?: throw IOException("No active session token")
                val notifs = App.retrofitService.getNotifications("Bearer $activeTok")
                App.appContext?.let {
                    PushNotificationHelper.dispatchUnreadNotifications(
                        it,
                        "${roleHint ?: "user"}_${activeTok.takeLast(8)}",
                        notifs
                    )
                }
                Result.success(notifs)
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun markNotificationRead(token: String? = null, notificationId: Long, done: (Result<Boolean>) -> Unit = {}) {
        scope.launch {
            val result = try {
                val activeTok = resolveActiveToken(token)
                if (!activeTok.isNullOrBlank() && notificationId > 0) {
                    App.retrofitService.markNotificationRead("Bearer $activeTok", notificationId)
                }
                Result.success(true)
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun markAllNotificationsRead(
        token: String? = null,
        roleHint: String? = null,
        done: (Result<Boolean>) -> Unit = {}
    ) {
        scope.launch {
            val result = try {
                val activeTok = resolveActiveToken(token, roleHint)
                if (!activeTok.isNullOrBlank()) {
                    App.retrofitService.markAllNotificationsRead("Bearer $activeTok")
                }
                Result.success(true)
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    data class CrmProjectItem(
        val project: ProjectDto,
        val productName: String,
        val details: String? = null
    )

    data class DownlineCrmPayload(
        val totalPotentialValue: Double,
        val numberOfClients: Int,
        val activeProjects: List<CrmProjectItem>,
        val dealProjects: List<CrmProjectItem>,
        val droppedProjects: List<CrmProjectItem>,
        val masterItems: List<MasterItemDto>
    )

    fun downlineCrm(token: String, done: (Result<DownlineCrmPayload>) -> Unit) {
        scope.launch {
            val result = try {
                val bearer = "Bearer $token"
                val prof = runCatching { App.retrofitService.getProfile(bearer) }.getOrNull()
                val projects = App.retrofitService.getProjects(bearer)
                val masterItems = runCatching { App.retrofitService.getItems(bearer) }.getOrDefault(emptyList())
                val itemMap = masterItems.associateBy { it.id }

                val enriched = projects.map { proj ->
                    rememberProjectClient(
                        projectId = proj.id,
                        projectCode = proj.projectCode,
                        agentCode = proj.agentCode ?: prof?.agentCode,
                        clientName = proj.clientName
                    )
                    val detail = runCatching {
                        App.retrofitService.getProjectDetail(bearer, proj.id)
                    }.getOrNull()
                    val firstItemId = detail?.items?.firstOrNull()?.itemId
                    val prodName = when {
                        firstItemId != null && itemMap.containsKey(firstItemId) ->
                            itemMap[firstItemId]?.itemName ?: "General Program"
                        masterItems.isNotEmpty() ->
                            masterItems.first().itemName
                        else -> "Nursery Program"
                    }
                    val lastActivity = detail?.activities?.firstOrNull()?.activityDescription
                    val effectiveStatus = localStatusOverrides[proj.id] ?: proj.status
                    val effectiveVal = localValueOverrides[proj.id] ?: proj.dealValue
                    CrmProjectItem(
                        project = proj.copy(status = effectiveStatus, dealValue = effectiveVal),
                        productName = prodName,
                        details = lastActivity
                    )
                }

                val active = enriched.filter { it.project.status == "follow_up" }
                val deals = enriched.filter { it.project.status == "deal" }
                val dropped = enriched.filter { it.project.status == "no_deal" || it.project.status == "cancelled" }

                // Total Potential Value strictly sums ONLY the active (follow_up) deals
                val potentialValue = active.sumOf { it.project.dealValue }

                val uniqueClients = projects.map { p ->
                    p.clientId?.let { "id-$it" } ?: p.clientName.trim().lowercase()
                }.distinct().size

                Result.success(
                    DownlineCrmPayload(
                        totalPotentialValue = potentialValue,
                        numberOfClients = uniqueClients,
                        activeProjects = active,
                        dealProjects = deals,
                        droppedProjects = dropped,
                        masterItems = masterItems
                    )
                )
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun projectDetail(token: String, projectId: Long, done: (Result<ProjectDetailResponse>) -> Unit) {
        scope.launch {
            val result = try {
                val res = App.retrofitService.getProjectDetail("Bearer $token", projectId)
                Result.success(res)
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun updateProjectStatus(
        token: String, projectId: Long, status: String, reason: String?,
        done: (Result<ProjectDto>) -> Unit
    ) {
        scope.launch {
            val result = try {
                Result.success(App.retrofitService.updateProjectStatus(
                    "Bearer $token", projectId, UpdateStatusRequest(status = status, reason = reason)
                ))
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun counterOffer(
        token: String, projectId: Long, newDealValue: Double, notes: String,
        done: (Result<ProjectDto>) -> Unit
    ) {
        scope.launch {
            val result = try {
                Result.success(App.retrofitService.counterOffer(
                    "Bearer $token", projectId,
                    CounterOfferRequest(newDealValue = newDealValue, notes = notes)
                ))
            } catch (error: Exception) { Result.failure(errorMessage(error)) }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun requestDealApproval(token: String, projectId: Long, done: (Result<ApprovalDto>) -> Unit) {
        scope.launch {
            val result = try {
                Result.success(App.retrofitService.requestDealApproval("Bearer $token", projectId))
            } catch (error: Exception) { Result.failure(errorMessage(error)) }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun getMasterItems(token: String, done: (Result<List<MasterItemDto>>) -> Unit) {
        scope.launch {
            val result = try {
                val items = App.retrofitService.getItems("Bearer $token")
                Result.success(items)
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun createNewProposalDeal(
        token: String,
        clientName: String,
        clientAddress: String,
        clientEmail: String,
        clientPhone: String,
        selectedItem: MasterItemDto?,
        productTypeName: String,
        isDirectDeal: Boolean,
        firstOffer: Double,
        productDetails: String,
        done: (Result<ProjectDto>) -> Unit
    ) {
        scope.launch {
            val result = try {
                val bearer = "Bearer $token"
                val prof = runCatching { App.retrofitService.getProfile(bearer) }.getOrNull()
                val safePhone = clientPhone.trim().ifBlank { "0800000000" }.take(20)
                val safeEmail = clientEmail.trim().ifBlank { null }?.take(100)
                val safeAddress = clientAddress.trim().ifBlank { null }?.take(3000)

                // 1. Create Client in `clients` table (if supported)
                val createdClient = runCatching {
                    val clientCode = "CL-${System.currentTimeMillis().toString(36).uppercase()}"
                    App.retrofitService.createClient(
                        bearer,
                        ClientCreateRequest(
                            clientCode = clientCode,
                            clientName = clientName.trim(),
                            phoneNumber = safePhone,
                            email = safeEmail,
                            address = safeAddress
                        )
                    )
                }.getOrNull()

                // 2. Create Project in `projects` + `project_items` tables
                val projectItems = if (selectedItem != null && selectedItem.id > 0) {
                    listOf(ProjectItemInput(itemId = selectedItem.id, quantity = 1, price = firstOffer))
                } else {
                    emptyList()
                }

                val createdProject = App.retrofitService.createProject(
                    bearer,
                    CreateProjectRequest(
                        clientId = createdClient?.id,
                        clientName = clientName.trim(),
                        clientPhone = safePhone,
                        clientEmail = safeEmail,
                        dealValue = firstOffer,
                        items = projectItems
                    )
                )

                rememberProjectClient(
                    projectId = createdProject.id,
                    projectCode = createdProject.projectCode,
                    agentCode = createdProject.agentCode ?: prof?.agentCode,
                    clientName = clientName.trim()
                )

                // 3. Create Proposal PDF & Record in `proposals` + `proposal_items` tables
                val propItemName = productTypeName.ifBlank { selectedItem?.itemName ?: "Project Package" }
                runCatching {
                    App.retrofitService.createProposal(
                        bearer,
                        createdProject.id,
                        CreateProposalRequest(
                            title = "Proposal ${clientName.trim()}",
                            items = listOf(
                                ProposalItemInput(
                                    itemId = selectedItem?.id?.takeIf { it > 0 },
                                    itemName = propItemName,
                                    description = productDetails.trim().ifBlank { null },
                                    quantity = 1,
                                    unitPrice = firstOffer
                                )
                            )
                        )
                    )
                }

                // 4. If Direct Deal selected, advance stage & submit Deal Approval request
                if (isDirectDeal && firstOffer > 0) {
                    runCatching {
                        App.retrofitService.updateProjectStage(
                            bearer,
                            createdProject.id,
                            UpdateStageRequest(stage = "proposal_sent")
                        )
                    }
                    runCatching {
                        App.retrofitService.requestDealApproval(bearer, createdProject.id)
                    }
                }

                Result.success(createdProject)
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun getProfile(token: String, done: (Result<UserProfileDto>) -> Unit) {
        scope.launch {
            val result = try {
                Result.success(App.retrofitService.getProfile("Bearer $token"))
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun updateProfile(
        token: String,
        name: String,
        phoneNumber: String,
        done: (Result<UserProfileDto>) -> Unit
    ) {
        scope.launch {
            val result = try {
                Result.success(
                    App.retrofitService.updateProfile(
                        "Bearer $token",
                        UpdateProfileRequest(
                            name = name.trim().ifBlank { null },
                            phoneNumber = phoneNumber.trim().ifBlank { null }
                        )
                    )
                )
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    data class GlobalProfilePayload(
        val profile: UserProfileDto,
        val totalDeals: Int,
        val dealsPerMonth: Double
    )

    fun globalProfile(token: String, done: (Result<GlobalProfilePayload>) -> Unit) {
        scope.launch {
            val result = try {
                val bearer = "Bearer $token"
                val prof = App.retrofitService.getProfile(bearer)
                rememberSessionToken(prof.role, prof.agentCode, token)
                val perf = runCatching { App.retrofitService.getPerformance(bearer) }.getOrNull()
                val deals = perf?.stats?.totalDealsMade
                    ?: perf?.sales?.count { it.status.equals("deal", ignoreCase = true) }
                    ?: 0
                val ratio = if (deals > 0) kotlin.math.round(deals / 2.0 * 10) / 10.0 else 0.0
                Result.success(
                    GlobalProfilePayload(
                        profile = prof,
                        totalDeals = deals,
                        dealsPerMonth = ratio
                    )
                )
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    data class GlobalDealDetailsPayload(
        val projectId: Long,
        val clientName: String,
        val isReturningClient: Boolean,
        val clientPhone: String,
        val agentResponsible: String,
        val agentPhone: String,
        val productType: String,
        val dealType: String,
        val initialPrice: Double,
        val dealDetails: String
    )

    fun globalProjectDetails(
        token: String,
        projectId: Long,
        fallbackClientName: String = "PT Arunika Web",
        fallbackProduct: String = "Web Program",
        fallbackPrice: Double = 0.0,
        fallbackAgent: String = "Tony Stark",
        done: (Result<GlobalDealDetailsPayload>) -> Unit
    ) {
        scope.launch {
            val result = try {
                val bearer = "Bearer $token"
                val detail = App.retrofitService.getProjectDetail(bearer, projectId)
                val items = runCatching { App.retrofitService.getItems(bearer) }.getOrDefault(emptyList())
                val itemMap = items.associateBy { it.id }

                val p = detail.project
                val firstItemId = detail.items.firstOrNull()?.itemId
                val matchedItem = firstItemId?.let { itemMap[it] }
                val prodName = matchedItem?.itemName ?: fallbackProduct
                val detailDesc = detail.activities.firstOrNull()?.activityDescription
                    ?: matchedItem?.description
                    ?: "Implementasi dan pengembangan paket solusi proyek keagenan sesuai penawaran klien."

                val resolvedClient = resolveClientName(
                    rawClientName = p.clientName.ifBlank { fallbackClientName },
                    projectId = p.id,
                    projectCode = p.projectCode,
                    agentCode = p.agentCode
                )

                val allProjects = runCatching { App.retrofitService.getProjects(bearer) }.getOrDefault(emptyList())
                val sameClientCount = allProjects.count {
                    (p.clientId != null && it.clientId == p.clientId) ||
                            it.clientName.equals(p.clientName, ignoreCase = true)
                }

                Result.success(
                    GlobalDealDetailsPayload(
                        projectId = p.id,
                        clientName = resolvedClient,
                        isReturningClient = sameClientCount > 1,
                        clientPhone = p.clientPhone?.ifBlank { "08xxxxxxxx" } ?: "08xxxxxxxx",
                        agentResponsible = p.agentCode?.ifBlank { fallbackAgent } ?: fallbackAgent,
                        agentPhone = p.clientPhone ?: "08123456789",
                        productType = prodName,
                        dealType = if ((localStatusOverrides[p.id] ?: p.status) == "deal") "Direct Deal" else "Follow-Up",
                        initialPrice = localValueOverrides[p.id] ?: (if (p.dealValue > 0) p.dealValue else fallbackPrice),
                        dealDetails = detailDesc
                    )
                )
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    // ==========================================
    // UPLINE HELPERS
    // ==========================================

    data class UplineResolvedActivityItem(
        val projectId: Long,
        val headline: String,
        val clientName: String,
        val description: String,
        val time: String?
    )

    data class UplineHomePayload(
        val dashboard: UplineDashboardResponse,
        val notifications: List<NotificationDto>,
        val resolvedActivities: List<UplineResolvedActivityItem> = emptyList()
    )

    fun uplineHome(token: String, done: (Result<UplineHomePayload>) -> Unit) {
        scope.launch {
            val result = try {
                val bearer = "Bearer $token"
                val dash = App.retrofitService.getUplineDashboard(bearer)
                val crm = runCatching { App.retrofitService.getUplineCrm(bearer) }.getOrNull()
                val allProjects = runCatching { App.retrofitService.getProjects(bearer) }.getOrDefault(emptyList())
                val perf = runCatching { App.retrofitService.getPerformance(bearer) }.getOrNull()
                val notifs = runCatching { App.retrofitService.getNotifications(bearer) }.getOrDefault(emptyList())
                App.appContext?.let { PushNotificationHelper.dispatchUnreadNotifications(it, "upline_${token.takeLast(8)}", notifs) }

                // Data downline hanya dari endpoint role-aware, tidak memakai token pengguna lain.

                // Remember any non-anonymized projects from Upline's own list
                allProjects.forEach { p ->
                    rememberProjectClient(p.id, p.projectCode, p.agentCode, p.clientName)
                }
                crm?.projects?.forEach { p ->
                    rememberProjectClient(p.id, p.projectCode, p.agentCode, p.clientName)
                }

                // Build ALL active deals made by EITHER the Downlines OR the Upline
                val mergedActiveDealsMap = linkedMapOf<Long, UplineActiveDealDto>()

                // 1. From CRM projects (includes both Upline and Downline projects)
                crm?.projects?.forEach { p ->
                    val effectiveStatus = localStatusOverrides[p.id] ?: p.status
                    val effectiveValue = localValueOverrides[p.id] ?: p.dealValue
                    if (effectiveStatus == "follow_up") {
                        val cName = resolveClientName(p.clientName, p.id, p.projectCode, p.agentCode)
                        mergedActiveDealsMap[p.id] = UplineActiveDealDto(
                            id = p.id,
                            clientName = cName,
                            amount = effectiveValue
                        )
                    }
                }

                // 2. From /api/projects (includes both Upline and Downline projects)
                allProjects.forEach { p ->
                    val effectiveStatus = localStatusOverrides[p.id] ?: p.status
                    val effectiveValue = localValueOverrides[p.id] ?: p.dealValue
                    if (effectiveStatus == "follow_up" && !mergedActiveDealsMap.containsKey(p.id)) {
                        val cName = resolveClientName(p.clientName, p.id, p.projectCode, p.agentCode)
                        mergedActiveDealsMap[p.id] = UplineActiveDealDto(
                            id = p.id,
                            clientName = cName,
                            amount = effectiveValue
                        )
                    }
                }

                // 3. From dashboard.activeDeals
                dash.activeDeals.forEach { d ->
                    val effectiveStatus = localStatusOverrides[d.id] ?: "follow_up"
                    val effectiveValue = localValueOverrides[d.id] ?: d.amount
                    if (effectiveStatus == "follow_up" && !mergedActiveDealsMap.containsKey(d.id)) {
                        val cName = resolveClientName(d.clientName, d.id, null, null)
                        mergedActiveDealsMap[d.id] = UplineActiveDealDto(
                            id = d.id,
                            clientName = cName,
                            amount = effectiveValue
                        )
                    }
                }

                val allActiveDeals = mergedActiveDealsMap.values.toList()

                // Map Recent Downline Activities:
                // - Text next to checkmark (`✓`): "PT Integra Cipta Data accepted the offer"
                // - Name below: Name of the Client (resolved from Downline's projects)
                val downlineProjects = (crm?.projects?.filter {
                    it.agentCode != null && !it.agentCode.equals("My Project", ignoreCase = true)
                } ?: emptyList())

                val resolvedActs = dash.activities.mapIndexed { idx, act ->
                    val matchingProjects = downlineProjects.filter {
                        it.agentCode.equals(act.agentCode, ignoreCase = true)
                    }
                    val chosenProject = if (matchingProjects.isNotEmpty()) {
                        matchingProjects[idx % matchingProjects.size]
                    } else {
                        crm?.projects?.getOrNull(idx % (crm.projects.size.coerceAtLeast(1)))
                    }
                    val projId = chosenProject?.id ?: 0L
                    val cName = resolveClientName(
                        rawClientName = chosenProject?.clientName,
                        projectId = projId,
                        projectCode = chosenProject?.projectCode,
                        agentCode = act.agentCode
                    )
                    UplineResolvedActivityItem(
                        projectId = projId,
                        headline = "PT Integra Cipta Data accepted the offer",
                        clientName = cName,
                        description = act.description,
                        time = act.time
                    )
                }

                val effectiveCut = when {
                    dash.monthlyCut > 0 -> dash.monthlyCut
                    perf != null && perf.stats.totalIncome > 0 -> perf.stats.totalIncome
                    else -> 0.0
                }
                Result.success(
                    UplineHomePayload(
                        dashboard = dash.copy(
                            monthlyCut = effectiveCut,
                            activeDeals = allActiveDeals
                        ),
                        notifications = notifs,
                        resolvedActivities = resolvedActs
                    )
                )
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun uplineDownlines(token: String, done: (Result<TeamResponse>) -> Unit) {
        scope.launch {
            val result = try {
                val res = App.retrofitService.getUplineDownlines("Bearer $token")
                Result.success(res)
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun uplineCrm(token: String, done: (Result<DownlineCrmPayload>) -> Unit) {
        scope.launch {
            val result = try {
                val bearer = "Bearer $token"
                val crm = App.retrofitService.getUplineCrm(bearer)
                val masterItems = runCatching { App.retrofitService.getItems(bearer) }.getOrDefault(emptyList())
                val defaultProd = masterItems.firstOrNull()?.itemName ?: "Nursery Program"

                for (tok in candidateTokens(token)) {
                    if (tok != token) {
                        runCatching {
                            App.retrofitService.getProjects("Bearer $tok").forEach { dp ->
                                rememberProjectClient(dp.id, dp.projectCode, dp.agentCode, dp.clientName)
                            }
                        }
                    }
                }

                val enriched = crm.projects.map { p ->
                    val effectiveStatus = localStatusOverrides[p.id] ?: p.status
                    val effectiveValue = localValueOverrides[p.id] ?: p.dealValue
                    val cName = resolveClientName(p.clientName, p.id, p.projectCode, p.agentCode)
                    CrmProjectItem(
                        project = ProjectDto(
                            id = p.id,
                            projectCode = p.projectCode,
                            clientName = cName,
                            agentCode = p.agentCode,
                            status = effectiveStatus,
                            stage = p.stage,
                            dealValue = effectiveValue,
                            cancelReason = p.cancelReason,
                            createdAt = p.createdAt,
                            updatedAt = p.updatedAt
                        ),
                        productName = defaultProd
                    )
                }

                val activeProjects = enriched.filter { it.project.status == "follow_up" }
                val dealProjects = enriched.filter { it.project.status == "deal" }
                val droppedProjects = enriched.filter {
                    it.project.status == "no_deal" || it.project.status == "cancelled"
                }

                // Total Potential Value strictly sums ONLY the active (follow_up) deals
                val activePotentialValue = activeProjects.sumOf { it.project.dealValue }

                Result.success(
                    DownlineCrmPayload(
                        totalPotentialValue = activePotentialValue,
                        numberOfClients = crm.summary.numberOfClients,
                        activeProjects = activeProjects,
                        dealProjects = dealProjects,
                        droppedProjects = droppedProjects,
                        masterItems = masterItems
                    )
                )
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    // ==========================================
    // ADMIN HELPERS
    // ==========================================

    data class AdminHomePayload(
        val overview: AdminOverviewResponse,
        val notifications: List<NotificationDto>,
        val recentProjects: List<ProjectDto>
    )

    fun adminHome(token: String, done: (Result<AdminHomePayload>) -> Unit) {
        scope.launch {
            val result = try {
                val bearer = "Bearer $token"
                val overview = App.retrofitService.getAdminOverview(bearer)
                val notifs = runCatching { App.retrofitService.getNotifications(bearer) }.getOrDefault(emptyList())
                App.appContext?.let { PushNotificationHelper.dispatchUnreadNotifications(it, "admin_${token.takeLast(8)}", notifs) }
                val projects = runCatching { App.retrofitService.getProjects(bearer) }.getOrDefault(emptyList())
                projects.forEach { p ->
                    rememberProjectClient(p.id, p.projectCode, p.agentCode, p.clientName)
                }
                Result.success(AdminHomePayload(overview, notifs, projects.take(6)))
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun adminAgents(token: String, role: String?, done: (Result<List<UserProfileDto>>) -> Unit) {
        scope.launch {
            val result = try {
                Result.success(App.retrofitService.getAdminAgents("Bearer $token", role))
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun adminCreateAgent(
        token: String,
        agentCode: String,
        name: String,
        email: String,
        password: String,
        phoneNumber: String,
        role: String,
        agentType: String,
        ratePercent: Double,
        uplineId: Long?,
        done: (Result<UserProfileDto>) -> Unit
    ) {
        scope.launch {
            val result = try {
                Result.success(
                    App.retrofitService.createAgent(
                        "Bearer $token",
                        CreateAgentRequest(
                            agentCode = agentCode.trim().uppercase(),
                            name = name.trim(),
                            email = email.trim(),
                            password = password.ifBlank { "password123" },
                            phoneNumber = phoneNumber.trim().ifBlank { null },
                            role = role.lowercase(),
                            agentType = agentType.lowercase(),
                            ratePercent = ratePercent,
                            uplineId = uplineId
                        )
                    )
                )
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun adminUpdateAgent(
        token: String,
        agentId: Long,
        ratePercent: Double? = null,
        agentType: String? = null,
        isActive: Boolean? = null,
        done: (Result<UserProfileDto>) -> Unit
    ) {
        scope.launch {
            val result = try {
                Result.success(
                    App.retrofitService.updateAgent(
                        "Bearer $token",
                        agentId,
                        UpdateAgentRequest(
                            ratePercent = ratePercent,
                            agentType = agentType,
                            isActive = isActive
                        )
                    )
                )
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    data class AdminMasterItemPayload(
        val categories: List<MasterCategoryDto>,
        val items: List<MasterItemDto>
    )

    fun adminMasterItems(token: String, done: (Result<AdminMasterItemPayload>) -> Unit) {
        scope.launch {
            val result = try {
                val bearer = "Bearer $token"
                val cats = App.retrofitService.getCategories(bearer)
                val items = App.retrofitService.getItems(bearer)
                Result.success(AdminMasterItemPayload(cats, items))
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun adminCreateCategory(
        token: String,
        name: String,
        description: String,
        done: (Result<MasterCategoryDto>) -> Unit
    ) {
        scope.launch {
            val result = try {
                Result.success(
                    App.retrofitService.createCategory(
                        "Bearer $token",
                        CreateCategoryRequest(
                            categoryName = name.trim(),
                            description = description.trim().ifBlank { null }
                        )
                    )
                )
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun adminCreateItem(
        token: String,
        categoryId: Long,
        itemCode: String,
        itemName: String,
        basePrice: Double,
        description: String,
        done: (Result<MasterItemDto>) -> Unit
    ) {
        scope.launch {
            val result = try {
                Result.success(
                    App.retrofitService.createMasterItem(
                        "Bearer $token",
                        CreateMasterItemRequest(
                            categoryId = categoryId,
                            itemCode = itemCode.trim().uppercase(),
                            itemName = itemName.trim(),
                            basePrice = basePrice,
                            description = description.trim().ifBlank { null }
                        )
                    )
                )
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    data class AdminProjectsPayload(
        val projects: List<ProjectDto>,
        val approvals: List<ApprovalDto>,
        val commissions: List<CommissionDto> = emptyList()
    )

    fun adminProjects(token: String, done: (Result<AdminProjectsPayload>) -> Unit) {
        scope.launch {
            val result = try {
                val bearer = "Bearer $token"
                val rawProjects = App.retrofitService.getProjects(bearer)
                val projects = rawProjects.map { p ->
                    rememberProjectClient(p.id, p.projectCode, p.agentCode, p.clientName)
                    val effectiveStatus = localStatusOverrides[p.id] ?: p.status
                    val effectiveVal = localValueOverrides[p.id] ?: p.dealValue
                    p.copy(status = effectiveStatus, dealValue = effectiveVal)
                }
                val approvals = runCatching { App.retrofitService.getAdminApprovals(bearer) }.getOrDefault(emptyList())
                val commissions = runCatching { App.retrofitService.getCommissions(bearer) }.getOrDefault(emptyList())
                Result.success(AdminProjectsPayload(projects, approvals, commissions))
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun adminReviewApproval(
        token: String,
        approvalId: Long,
        decision: String,
        reason: String? = null,
        done: (Result<Boolean>) -> Unit
    ) {
        scope.launch {
            val result = try {
                App.retrofitService.reviewApproval(
                    "Bearer $token",
                    approvalId,
                    ApprovalReviewRequest(decision = decision, reason = reason)
                )
                Result.success(true)
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    data class AdminPaymentsPayload(
        val invoices: List<InvoiceDto>,
        val payments: List<PaymentDto>,
        val commissions: List<CommissionDto>,
        val projects: List<ProjectDto> = emptyList()
    )

    fun adminPayments(token: String, done: (Result<AdminPaymentsPayload>) -> Unit) {
        scope.launch {
            val result = try {
                val bearer = "Bearer $token"
                val invoices = runCatching { App.retrofitService.getInvoices(bearer) }.getOrDefault(emptyList())
                val payments = runCatching { App.retrofitService.getPayments(bearer) }.getOrDefault(emptyList())
                val commissions = runCatching { App.retrofitService.getCommissions(bearer) }.getOrDefault(emptyList())
                val projects = runCatching { App.retrofitService.getProjects(bearer) }.getOrDefault(emptyList())
                projects.forEach { p ->
                    rememberProjectClient(p.id, p.projectCode, p.agentCode, p.clientName)
                }
                Result.success(AdminPaymentsPayload(invoices, payments, commissions, projects))
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun adminCreatePayment(
        token: String,
        invoiceId: Long,
        amountPaid: Double,
        paymentMethod: String = "Transfer Bank",
        done: (Result<PaymentDto>) -> Unit
    ) {
        scope.launch {
            val result = try {
                Result.success(
                    App.retrofitService.createPayment(
                        "Bearer $token",
                        CreatePaymentRequest(
                            invoiceId = invoiceId,
                            amountPaid = amountPaid,
                            paymentMethod = paymentMethod
                        )
                    )
                )
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun adminCreateInvoice(
        token: String,
        projectId: Long,
        totalAmount: Double,
        dueDate: String,
        invoiceType: String = "full",
        notes: String? = null,
        done: (Result<InvoiceDto>) -> Unit
    ) {
        scope.launch {
            val result = try {
                Result.success(
                    App.retrofitService.createInvoice(
                        "Bearer $token",
                        CreateInvoiceRequest(
                            projectId = projectId,
                            totalAmount = totalAmount,
                            dueDate = dueDate,
                            invoiceType = invoiceType,
                            notes = notes
                        )
                    )
                )
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    // Catat sisa invoice saja: menghindari duplikasi pembayaran pada invoice partial.
    fun adminMarkInvoiceFullyPaid(token: String, invoiceId: Long, done: (Result<Boolean>) -> Unit) {
        scope.launch {
            val result = try {
                val detail = App.retrofitService.getInvoiceDetail("Bearer $token", invoiceId)
                val remaining = kotlin.math.round((detail.invoice.totalAmount - detail.paidAmount) * 100) / 100
                if (remaining <= 0.0) throw IllegalStateException("Invoice sudah lunas")
                App.retrofitService.createPayment("Bearer $token",
                    CreatePaymentRequest(invoiceId = invoiceId, amountPaid = remaining))
                Result.success(true)
            } catch (error: Exception) { Result.failure(errorMessage(error)) }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    // Mencatat sisa komisi saja, setelah Admin benar-benar melakukan transfer.
    fun adminPayRemainingCommission(token: String, commissionId: Long, done: (Result<Boolean>) -> Unit) {
        scope.launch {
            val result = try {
                val detail = App.retrofitService.getCommissionDetail("Bearer $token", commissionId)
                if (detail.commission.payoutStatus != "approved")
                    throw IllegalStateException("Komisi belum approved atau sudah paid")
                val remaining = kotlin.math.round((detail.commission.totalCommissionAmount - detail.paidAmount) * 100) / 100
                if (remaining <= 0.0) throw IllegalStateException("Komisi sudah lunas")
                App.retrofitService.createCommissionPayout("Bearer $token",
                    CreatePayoutRequest(commissionId = commissionId, amountPaid = remaining))
                Result.success(true)
            } catch (error: Exception) { Result.failure(errorMessage(error)) }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun adminApproveCommission(
        token: String,
        commissionId: Long,
        done: (Result<CommissionDto>) -> Unit
    ) {
        scope.launch {
            val result = try {
                Result.success(App.retrofitService.approveCommission("Bearer $token", commissionId))
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun adminPayoutCommission(
        token: String,
        commissionId: Long,
        amountPaid: Double,
        done: (Result<Boolean>) -> Unit
    ) {
        scope.launch {
            val result = try {
                App.retrofitService.createCommissionPayout(
                    "Bearer $token",
                    CreatePayoutRequest(
                        commissionId = commissionId,
                        amountPaid = amountPaid
                    )
                )
                Result.success(true)
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    private fun saveBytesToDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Failed to create file in Downloads")
            try {
                val stream = resolver.openOutputStream(uri) ?: throw IOException("Failed to open file in Downloads")
                stream.use { out -> out.write(bytes) }
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                if (resolver.update(uri, values, null, null) != 1) throw IOException("Failed to publish file in Downloads")
            } catch (error: Exception) {
                resolver.delete(uri, null, null)
                throw error
            }
            return "Downloads/$fileName"
        } else {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            file.writeBytes(bytes)
            return file.absolutePath
        }
    }

    fun generateOrGetProposal(
        token: String,
        projectId: Long,
        clientName: String,
        productName: String,
        price: Double,
        details: String,
        done: (Result<ProposalDto>) -> Unit
    ) {
        scope.launch {
            val result = try {
                val bearer = "Bearer $token"
                val existing = runCatching {
                    App.retrofitService.getProposals(bearer).firstOrNull { it.projectId == projectId }
                }.getOrNull()
                val proposal = existing ?: App.retrofitService.createProposal(
                    bearer,
                    projectId,
                    CreateProposalRequest(
                        title = "Proposal $clientName",
                        items = listOf(
                            ProposalItemInput(
                                itemName = productName.ifBlank { "Project Package" },
                                description = details.ifBlank { null },
                                quantity = 1,
                                unitPrice = if (price > 0) price else 10000.0
                            )
                        )
                    )
                ).proposal

                // Also download and save the generated PDF into the phone's Downloads folder
                val ctx = App.appContext
                if (ctx != null && proposal.id > 0) {
                    runCatching {
                        val body = App.retrofitService.downloadProposalPdf(bearer, proposal.id)
                        val bytes = body.bytes()
                        val safeNum = proposal.proposalNumber.ifBlank { "PRP-$projectId" }
                        saveBytesToDownloads(ctx, "$safeNum.pdf", "application/pdf", bytes)
                    }
                }
                Result.success(proposal)
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun adminBroadcast(
        token: String,
        title: String,
        message: String,
        roles: List<String>? = null,
        done: (Result<BroadcastResponse>) -> Unit
    ) {
        scope.launch {
            val result = try {
                val res = App.retrofitService.sendBroadcast(
                    "Bearer $token",
                    BroadcastRequest(title = title.trim(), message = message.trim(), roles = roles)
                )
                App.appContext?.let { ctx ->
                    PushNotificationHelper.showSystemNotification(
                        context = ctx,
                        notificationId = (System.currentTimeMillis() % 100000).toInt(),
                        title = "Broadcast: ${title.trim()}",
                        message = message.trim()
                    )
                }
                Result.success(res)
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun adminExportReport(
        token: String,
        reportType: String,
        from: String? = null,
        to: String? = null,
        done: (Result<ReportResponseDto>) -> Unit
    ) {
        scope.launch {
            val result = try {
                Result.success(
                    App.retrofitService.getReportData("Bearer $token", reportType, "json", from, to)
                )
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun adminDownloadReportFile(
        context: Context,
        token: String,
        reportType: String,
        format: String,
        from: String? = null,
        to: String? = null,
        done: (Result<String>) -> Unit
    ) {
        scope.launch {
            val result = try {
                val fmt = if (format.equals("excel", ignoreCase = true) || format.equals("xlsx", ignoreCase = true)) {
                    "excel"
                } else {
                    "pdf"
                }
                val ext = if (fmt == "excel") "xlsx" else "pdf"
                val mime = if (fmt == "excel") {
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                } else {
                    "application/pdf"
                }
                val body = App.retrofitService.downloadReportFile("Bearer $token", reportType, fmt, from, to)
                val bytes = body.bytes()
                val fileName = "Integra_${reportType}_${System.currentTimeMillis().toString().takeLast(5)}.$ext"
                val savedPath = saveBytesToDownloads(context, fileName, mime, bytes)
                Result.success(savedPath)
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }
}

class SessionStore(context: Context) {
    // Untuk demo capstone lokal. Gunakan encrypted storage pada aplikasi production.
    private val preferences = context.applicationContext
        .getSharedPreferences("integra_session", Context.MODE_PRIVATE)

    fun save(session: LoginSession) {
        val user = session.user
        ApiService.rememberSessionToken(user.role, user.agentCode, session.token)
        preferences.edit()
            .putString("token", session.token)
            .putInt("id", user.id)
            .putString("agentCode", user.agentCode)
            .putString("name", user.name)
            .putString("email", user.email)
            .putString("role", user.role)
            .apply()
    }

    fun restore(): LoginSession? {
        val token = preferences.getString("token", null) ?: return null
        val user = AgencyUser(
            id = preferences.getInt("id", -1),
            agentCode = preferences.getString("agentCode", "").orEmpty(),
            name = preferences.getString("name", "").orEmpty(),
            email = preferences.getString("email", "").orEmpty(),
            role = preferences.getString("role", "").orEmpty()
        )
        ApiService.rememberSessionToken(user.role, user.agentCode, token)
        return LoginSession(token, user)
    }

    fun clear() = preferences.edit().clear().apply()
}
