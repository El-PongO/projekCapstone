package com.example.projectcapstone

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface WebService {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/dashboard")
    suspend fun getDashboard(@Header("Authorization") token: String): DashboardResponse

    @GET("api/performance")
    suspend fun getPerformance(@Header("Authorization") token: String): PerformanceResponse

    // Downline Dashboard & Upline Info
    @GET("api/downline/dashboard")
    suspend fun getDownlineDashboard(
        @Header("Authorization") token: String
    ): DownlineDashboardResponse

    @GET("api/agents/my-upline/info")
    suspend fun getMyUplineInfo(
        @Header("Authorization") token: String
    ): UplineInfoResponse

    // Workspace: seluruh akses tetap divalidasi di backend, termasuk privasi Downline.
    @GET("api/productivity/projects/{id}/overview")
    suspend fun getProjectWorkspace(
        @Header("Authorization") token: String,
        @Path("id") projectId: Long
    ): ProjectWorkspaceDto

    @POST("api/productivity/projects/{id}/milestones")
    suspend fun createProjectMilestone(
        @Header("Authorization") token: String,
        @Path("id") projectId: Long,
        @Body body: CreateMilestoneRequest
    ): ProjectWorkspaceMilestoneDto

    @PATCH("api/productivity/milestones/{id}")
    suspend fun updateProjectMilestone(
        @Header("Authorization") token: String,
        @Path("id") milestoneId: Long,
        @Body body: MilestoneStatusRequest
    ): ProjectWorkspaceMilestoneDto

    @POST("api/productivity/projects/{id}/tasks")
    suspend fun createProjectFollowUp(
        @Header("Authorization") token: String,
        @Path("id") projectId: Long,
        @Body body: CreateFollowUpRequest
    ): ProjectWorkspaceTaskDto

    @POST("api/productivity/projects/{id}/meetings")
    suspend fun createProjectMeeting(
        @Header("Authorization") token: String,
        @Path("id") projectId: Long,
        @Body body: CreateMeetingRequest
    ): ProjectWorkspaceMeetingDto

    @retrofit2.http.Multipart
    @POST("api/productivity/projects/{id}/documents")
    suspend fun uploadProjectDocument(
        @Header("Authorization") token: String,
        @Path("id") projectId: Long,
        @retrofit2.http.Part file: okhttp3.MultipartBody.Part,
        @retrofit2.http.Part("documentType") type: okhttp3.RequestBody
    ): ProjectWorkspaceDocumentDto

    @retrofit2.http.Streaming
    @GET("api/productivity/documents/{id}/download")
    suspend fun downloadProjectDocument(
        @Header("Authorization") token: String,
        @Path("id") documentId: Long
    ): okhttp3.ResponseBody

    @retrofit2.http.DELETE("api/productivity/documents/{id}")
    suspend fun deleteProjectDocument(
        @Header("Authorization") token: String,
        @Path("id") documentId: Long
    ): OkResponse

    // Leaderboard anonim untuk Admin, Upline dan Downline.
    @GET("api/leaderboard")
    suspend fun getLeaderboard(
        @Header("Authorization") token: String
    ): LeaderboardResponseDto

    // Notifications
    @GET("api/notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String
    ): List<NotificationDto>

    @PATCH("api/notifications/{id}/read")
    suspend fun markNotificationRead(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): OkResponse

    @PATCH("api/notifications/read-all/now")
    suspend fun markAllNotificationsRead(
        @Header("Authorization") token: String
    ): OkResponse

    @POST("api/notifications/device")
    suspend fun registerDeviceToken(
        @Header("Authorization") token: String,
        @Body body: RegisterDeviceTokenRequest
    ): OkResponse

    // Master Data (Categories & Items / Product Types)
    @GET("api/master/categories")
    suspend fun getCategories(
        @Header("Authorization") token: String
    ): List<MasterCategoryDto>

    @GET("api/master/items")
    suspend fun getItems(
        @Header("Authorization") token: String
    ): List<MasterItemDto>

    // Clients
    @GET("api/clients")
    suspend fun getClients(
        @Header("Authorization") token: String
    ): List<ClientDto>

    @POST("api/clients")
    suspend fun createClient(
        @Header("Authorization") token: String,
        @Body body: ClientCreateRequest
    ): ClientDto

    // Projects (CRM & Deals)
    @GET("api/projects")
    suspend fun getProjects(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null
    ): List<ProjectDto>

    @POST("api/projects")
    suspend fun createProject(
        @Header("Authorization") token: String,
        @Body body: CreateProjectRequest
    ): ProjectDto

    @GET("api/projects/{id}")
    suspend fun getProjectDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): ProjectDetailResponse

    @PATCH("api/projects/{id}/status")
    suspend fun updateProjectStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: UpdateStatusRequest
    ): ProjectDto

    @PATCH("api/projects/{id}/stage")
    suspend fun updateProjectStage(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: UpdateStageRequest
    ): ProjectDto

    @POST("api/projects/{id}/counter-offer")
    suspend fun counterOffer(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: CounterOfferRequest
    ): ProjectDto

    // Proposals
    @POST("api/proposals/projects/{projectId}")
    suspend fun createProposal(
        @Header("Authorization") token: String,
        @Path("projectId") projectId: Long,
        @Body body: CreateProposalRequest
    ): CreateProposalResponse

    // Deal Approvals
    @POST("api/approvals/projects/{projectId}/request")
    suspend fun requestDealApproval(
        @Header("Authorization") token: String,
        @Path("projectId") projectId: Long
    ): ApprovalDto

    // Profile
    @GET("api/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): UserProfileDto

    @PATCH("api/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body body: UpdateProfileRequest
    ): UserProfileDto

    // Upline Endpoints
    @GET("api/upline/dashboard")
    suspend fun getUplineDashboard(
        @Header("Authorization") token: String
    ): UplineDashboardResponse

    @GET("api/upline/crm")
    suspend fun getUplineCrm(
        @Header("Authorization") token: String
    ): UplineCrmResponse

    @GET("api/upline/downlines")
    suspend fun getUplineDownlines(
        @Header("Authorization") token: String
    ): TeamResponse

    @GET("api/upline/downlines/{id}")
    suspend fun getUplineDownlineDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): TeamDetailResponse

    // Admin Endpoints
    @GET("api/admin/overview")
    suspend fun getAdminOverview(
        @Header("Authorization") token: String
    ): AdminOverviewResponse

    @GET("api/agents")
    suspend fun getAdminAgents(
        @Header("Authorization") token: String,
        @Query("role") role: String? = null
    ): List<UserProfileDto>

    @POST("api/agents")
    suspend fun createAgent(
        @Header("Authorization") token: String,
        @Body body: CreateAgentRequest
    ): UserProfileDto

    @PATCH("api/agents/{id}")
    suspend fun updateAgent(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: UpdateAgentRequest
    ): UserProfileDto

    @GET("api/proposals")
    suspend fun getProposals(
        @Header("Authorization") token: String
    ): List<ProposalDto>

    @POST("api/notifications/broadcast")
    suspend fun sendBroadcast(
        @Header("Authorization") token: String,
        @Body body: BroadcastRequest
    ): BroadcastResponse

    @POST("api/master/categories")
    suspend fun createCategory(
        @Header("Authorization") token: String,
        @Body body: CreateCategoryRequest
    ): MasterCategoryDto

    @POST("api/master/items")
    suspend fun createMasterItem(
        @Header("Authorization") token: String,
        @Body body: CreateMasterItemRequest
    ): MasterItemDto

    @GET("api/approvals")
    suspend fun getAdminApprovals(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null
    ): List<ApprovalDto>

    @GET("api/approvals/{id}/rates")
    suspend fun getDealRateReview(
        @Header("Authorization") token: String,
        @Path("id") approvalId: Long
    ): DealRateReviewDto

    @PUT("api/approvals/{id}/rates/{agentId}")
    suspend fun proposeDealRate(
        @Header("Authorization") token: String,
        @Path("id") approvalId: Long,
        @Path("agentId") agentId: Long,
        @Body request: DealRateProposalRequest
    ): DealRateOfferInfoDto

    @GET("api/approvals/my-rate-offers")
    suspend fun getMyDealRates(@Header("Authorization") token: String, @Query("projectId") projectId: Long): List<DealRateOfferDto>

    @POST("api/approvals/rates/{id}/respond")
    suspend fun respondDealRate(
        @Header("Authorization") token: String,
        @Path("id") offerId: Long,
        @Body request: DealRateResponseRequest
    ): DealRateOfferInfoDto

    @POST("api/approvals/{id}/review")
    suspend fun reviewApproval(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: ApprovalReviewRequest
    ): Map<String, Any?>

    @GET("api/invoices/{id}")
    suspend fun getInvoiceDetail(
        @Header("Authorization") token: String, @Path("id") invoiceId: Long
    ): InvoiceDetailDto

    @GET("api/commissions/{id}")
    suspend fun getCommissionDetail(
        @Header("Authorization") token: String, @Path("id") commissionId: Long
    ): CommissionDetailDto

    @GET("api/invoices")
    suspend fun getInvoices(
        @Header("Authorization") token: String
    ): List<InvoiceDto>

    @POST("api/invoices")
    suspend fun createInvoice(
        @Header("Authorization") token: String,
        @Body body: CreateInvoiceRequest
    ): InvoiceDto

    @GET("api/payments")
    suspend fun getPayments(
        @Header("Authorization") token: String
    ): List<PaymentDto>

    @POST("api/payments")
    suspend fun createPayment(
        @Header("Authorization") token: String,
        @Body body: CreatePaymentRequest
    ): PaymentDto

    @GET("api/commissions")
    suspend fun getCommissions(
        @Header("Authorization") token: String
    ): List<CommissionDto>

    @POST("api/commissions/{id}/approve")
    suspend fun approveCommission(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): CommissionDto

    @POST("api/commission-payouts")
    suspend fun createCommissionPayout(
        @Header("Authorization") token: String,
        @Body body: CreatePayoutRequest
    ): Map<String, Any?>

    @GET("api/reports/types")
    suspend fun getReportTypes(
        @Header("Authorization") token: String
    ): List<String>

    @GET("api/reports/{type}")
    suspend fun getReportData(
        @Header("Authorization") token: String,
        @Path("type") type: String,
        @Query("format") format: String = "json",
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): ReportResponseDto

    @retrofit2.http.Streaming
    @GET("api/reports/{type}")
    suspend fun downloadReportFile(
        @Header("Authorization") token: String,
        @Path("type") type: String,
        @Query("format") format: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): okhttp3.ResponseBody

    @retrofit2.http.Streaming
    @GET("api/proposals/{id}/download")
    suspend fun downloadProposalPdf(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): okhttp3.ResponseBody
}
