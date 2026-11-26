package com.example.projectcapstone

import com.squareup.moshi.Json

// Request dan Response dipisah menjadi data class, seperti pada tutor.
data class LoginRequest(val identifier: String, val password: String)
data class LoginResponse(val token: String, val user: AgencyUser)
data class LoginSession(val token: String, val user: AgencyUser)

data class AgencyUser(
    val id: Int,
    val agentCode: String,
    val name: String,
    val email: String,
    val role: String
)

data class DashboardStats(
    val totalProjects: Int,
    val dealCount: Int,
    val followUpCount: Int,
    val dealValue: Double,
    val agentCount: Int,
    val myCommission: Double
)

data class ProjectSummary(
    val projectCode: String,
    val title: String,
    val status: String,
    val stage: String,
    val dealValue: Double
)

data class DashboardResponse(
    val role: String,
    val stats: DashboardStats,
    val recentProjects: List<ProjectSummary>
)

data class DashboardData(
    val stats: DashboardStats,
    val recentProjects: List<ProjectSummary>
)

data class SalesReport(
    @Json(name = "projectCode") val code: String,
    @Json(name = "clientName") val client: String,
    val status: String,
    val stage: String,
    @Json(name = "dealValue") val value: Double,
    val date: String
)

data class IncomeReport(
    @Json(name = "projectCode") val code: String,
    @Json(name = "clientName") val client: String,
    val commission: Double,
    @Json(name = "ratePercent") val rate: Double,
    val date: String
)

data class PerformanceStats(
    val totalSales: Double,
    val totalIncome: Double,
    val totalDealsMade: Int = 0,
    val monthlyCut: Double = 0.0
)
data class PerformanceResponse(
    val stats: PerformanceStats,
    val sales: List<SalesReport>,
    val income: List<IncomeReport>
)

data class PerformanceData(
    val totalSales: Double,
    val totalIncome: Double,
    val sales: List<SalesReport>,
    val income: List<IncomeReport>,
    val totalDealsMade: Int = 0,
    val monthlyCut: Double = 0.0
)

// ==========================================
// DOWNLINE & FULL 20-TABLE BACKEND MODELS
// ==========================================

data class DownlineSummary(
    val totalProjects: Int = 0,
    val dealCount: Int = 0,
    val followUpCount: Int = 0,
    val myCommission: Double = 0.0
)

data class UplineInfo(
    val agentCode: String = "",
    val name: String = "",
    val phoneNumber: String? = null
)

data class UplineInfoResponse(
    val upline: UplineInfo? = null
)

data class UpcomingTaskDto(
    val id: Long = 0,
    val title: String = "",
    val dueAt: String? = null,
    val status: String = "pending"
)

data class DownlineDashboardResponse(
    val agentCode: String = "",
    val summary: DownlineSummary = DownlineSummary(),
    val upline: UplineInfo? = null,
    val upcomingTasks: List<UpcomingTaskDto> = emptyList(),
    val recentProjects: List<ProjectSummary> = emptyList()
)

data class NotificationDto(
    val id: Long = 0,
    @Json(name = "user_id") val userId: Long? = null,
    val title: String = "",
    val message: String = "",
    val type: String = "system",
    @Json(name = "is_read") val isRead: Boolean = false,
    @Json(name = "created_at") val createdAt: String? = null
)

data class OkResponse(
    val ok: Boolean = true,
    val message: String? = null
)

data class MasterCategoryDto(
    val id: Long = 0,
    @Json(name = "category_name") val categoryName: String = "",
    val description: String? = null
)

data class MasterItemDto(
    val id: Long = 0,
    @Json(name = "category_id") val categoryId: Long = 0,
    @Json(name = "item_code") val itemCode: String = "",
    @Json(name = "item_name") val itemName: String = "",
    @Json(name = "base_price") val basePrice: Double = 0.0,
    val description: String? = null
)

data class ClientCreateRequest(
    val clientCode: String,
    val clientName: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val address: String? = null
)

data class ClientDto(
    val id: Long = 0,
    @Json(name = "client_code") val clientCode: String = "",
    @Json(name = "client_name") val clientName: String = "",
    @Json(name = "phone_number") val phoneNumber: String? = null,
    val email: String? = null,
    val address: String? = null
)

data class ProjectItemInput(
    val itemId: Long,
    val quantity: Int = 1,
    val price: Double? = null
)

data class CreateProjectRequest(
    val clientId: Long? = null,
    val clientName: String,
    val clientPhone: String? = null,
    val clientEmail: String? = null,
    val dealValue: Double = 0.0,
    val items: List<ProjectItemInput> = emptyList()
)

data class ProjectDto(
    val id: Long = 0,
    val projectCode: String = "",
    val agentId: Long = 0,
    val agentCode: String? = null,
    val clientId: Long? = null,
    val clientName: String = "",
    val clientPhone: String? = null,
    val clientEmail: String? = null,
    val status: String = "follow_up",
    val stage: String = "negotiation",
    val dealValue: Double = 0.0,
    val cancelReason: String? = null,
    val dealDate: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val isAnonymized: Boolean = false
)

data class ProjectItemDto(
    val id: Long = 0,
    @Json(name = "project_id") val projectId: Long = 0,
    @Json(name = "item_id") val itemId: Long = 0,
    val quantity: Int = 1,
    val price: Double = 0.0
)

data class ProjectActivityDto(
    val id: Long = 0,
    @Json(name = "project_id") val projectId: Long = 0,
    @Json(name = "user_id") val userId: Long = 0,
    @Json(name = "activity_description") val activityDescription: String = "",
    @Json(name = "previous_stage") val previousStage: String? = null,
    @Json(name = "current_stage") val currentStage: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

data class ProjectDetailResponse(
    val project: ProjectDto,
    val items: List<ProjectItemDto> = emptyList(),
    val activities: List<ProjectActivityDto> = emptyList()
)

data class ProposalItemInput(
    val itemId: Long? = null,
    val itemName: String,
    val description: String? = null,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0
)

data class CreateProposalRequest(
    val title: String,
    val validUntil: String? = null,
    val items: List<ProposalItemInput>
)

data class ProposalDto(
    val id: Long = 0,
    @Json(name = "proposal_number") val proposalNumber: String = "",
    @Json(name = "project_id") val projectId: Long = 0,
    @Json(name = "proposal_title") val proposalTitle: String = "",
    @Json(name = "pdf_file_path") val pdfFilePath: String? = null,
    @Json(name = "valid_until") val validUntil: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

data class CreateProposalResponse(
    val proposal: ProposalDto,
    val total: Double = 0.0
)

data class UpdateStatusRequest(
    val status: String,
    val reason: String? = null
)

data class UpdateStageRequest(
    val stage: String
)

data class CounterOfferRequest(
    val newDealValue: Double,
    val notes: String? = null
)

data class ApprovalDto(
    val id: Long = 0,
    @Json(name = "project_id") val projectId: Long = 0,
    @Json(name = "requested_by") val requestedBy: Long = 0,
    val status: String = "pending"
)

data class UserProfileDto(
    val id: Long = 0,
    val agentCode: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val agentType: String? = null,
    val ratePercent: Double = 0.0,
    val phoneNumber: String? = null,
    val isActive: Boolean = true
)

data class UpdateProfileRequest(
    val name: String? = null,
    val phoneNumber: String? = null
)

// ==========================================
// UPLINE ENDPOINT MODELS
// ==========================================

data class UplineActiveDealDto(
    val id: Long = 0,
    val clientName: String = "",
    val amount: Double = 0.0
)

data class UplineActivityDto(
    val agentCode: String = "",
    val description: String = "",
    val time: String? = null
)

data class UplineDashboardResponse(
    val monthlyCut: Double = 0.0,
    val projectsHeld: Int = 0,
    val downlineCount: Int = 0,
    val activeDeals: List<UplineActiveDealDto> = emptyList(),
    val activities: List<UplineActivityDto> = emptyList()
)

data class UplineCrmSummaryDto(
    val totalPotentialValue: Double = 0.0,
    val numberOfClients: Int = 0,
    val activeCount: Int = 0,
    val dealCount: Int = 0,
    val droppedCount: Int = 0
)

data class UplineCrmProjectDto(
    val id: Long = 0,
    val projectCode: String = "",
    val clientName: String = "",
    val agentCode: String? = null,
    val status: String = "follow_up",
    val stage: String = "negotiation",
    val dealValue: Double = 0.0,
    val cancelReason: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class UplineCrmResponse(
    val summary: UplineCrmSummaryDto = UplineCrmSummaryDto(),
    val projects: List<UplineCrmProjectDto> = emptyList()
)

// ==========================================
// ADMIN ENDPOINT MODELS
// ==========================================

data class AdminAgentsCountDto(
    val uplines: Int = 0,
    val downlines: Int = 0,
    val relations: Int = 0
)

data class AdminProjectsCountDto(
    val total: Int = 0,
    val deals: Int = 0,
    val dealValue: Double = 0.0
)

data class AdminApprovalsCountDto(
    val pending: Int = 0
)

data class AdminInvoicesCountDto(
    val open: Int = 0,
    val outstanding: Double = 0.0
)

data class AdminCommissionsCountDto(
    val total: Double = 0.0,
    val pending: Int = 0
)

data class AdminOverviewResponse(
    val agents: AdminAgentsCountDto = AdminAgentsCountDto(),
    val projects: AdminProjectsCountDto = AdminProjectsCountDto(),
    val approvals: AdminApprovalsCountDto = AdminApprovalsCountDto(),
    val invoices: AdminInvoicesCountDto = AdminInvoicesCountDto(),
    val commissions: AdminCommissionsCountDto = AdminCommissionsCountDto()
)

data class BroadcastRequest(
    val title: String,
    val message: String,
    val roles: List<String>? = null,
    val agentCodes: List<String>? = null
)

data class BroadcastResponse(
    val ok: Boolean = true,
    val recipients: Int = 0,
    val pushSent: Int = 0
)

data class CreateCategoryRequest(
    val categoryName: String,
    val description: String? = null
)

data class CreateMasterItemRequest(
    val categoryId: Long,
    val itemCode: String,
    val itemName: String,
    val basePrice: Double = 0.0,
    val description: String? = null
)

data class InvoiceDetailDto(
    val invoice: InvoiceDto = InvoiceDto(),
    val paidAmount: Double = 0.0
)
data class CommissionDetailDto(
    val commission: CommissionDto = CommissionDto(),
    val paidAmount: Double = 0.0
)

data class InvoiceDto(
    val id: Long = 0,
    @Json(name = "invoice_number") val invoiceNumber: String = "",
    @Json(name = "project_id") val projectId: Long = 0,
    @Json(name = "invoice_type") val invoiceType: String = "full",
    @Json(name = "total_amount") val totalAmount: Double = 0.0,
    @Json(name = "invoice_date") val invoiceDate: String? = null,
    @Json(name = "due_date") val dueDate: String? = null,
    val status: String = "unpaid",
    val notes: String? = null
)

data class PaymentDto(
    val id: Long = 0,
    @Json(name = "project_id") val projectId: Long = 0,
    @Json(name = "invoice_id") val invoiceId: Long? = null,
    @Json(name = "payment_receipt_no") val paymentReceiptNo: String = "",
    @Json(name = "amount_paid") val amountPaid: Double = 0.0,
    @Json(name = "payment_date") val paymentDate: String? = null,
    @Json(name = "payment_method") val paymentMethod: String = "",
    @Json(name = "payment_status") val paymentStatus: String = "paid",
    @Json(name = "due_date") val dueDate: String? = null
)

data class CommissionDto(
    val id: Long = 0,
    @Json(name = "project_id") val projectId: Long = 0,
    @Json(name = "agent_id") val agentId: Long = 0,
    @Json(name = "commission_rate_percent") val commissionRatePercent: Double = 0.0,
    @Json(name = "total_commission_amount") val totalCommissionAmount: Double = 0.0,
    @Json(name = "payout_status") val payoutStatus: String = "pending",
    @Json(name = "created_at") val createdAt: String? = null
)

// Kesepakatan rate berlaku per proyek, tidak mengubah rate global agen.
data class DealRateOfferDto(
    val id: Long = 0,
    val projectId: Long = 0,
    val projectCode: String = "",
    val ratePercent: Double = 0.0,
    val status: String = "pending",
    val projectStatus: String = "follow_up"
)
data class DealRateOfferInfoDto(
    val id: Long = 0,
    val ratePercent: Double = 0.0,
    val status: String = "pending"
)
data class DealRateRecipientDto(
    val agentId: Long = 0,
    val agentCode: String = "",
    val role: String = "",
    val agentType: String = "",
    val defaultRate: Double = 0.0,
    val offer: DealRateOfferInfoDto? = null
)
data class DealRateReviewDto(
    val approvalId: Long = 0,
    val projectId: Long = 0,
    val approvalStatus: String = "pending",
    val recipients: List<DealRateRecipientDto> = emptyList()
)
data class DealRateProposalRequest(val ratePercent: Double)
data class DealRateResponseRequest(val decision: String)

data class ApprovalReviewRequest(
    val decision: String,
    val reason: String? = null
)

data class ReportResponseDto(
    val type: String = "",
    val count: Int = 0,
    val data: List<Map<String, Any?>> = emptyList()
)

data class CreatePaymentRequest(
    val invoiceId: Long,
    val amountPaid: Double,
    val paymentMethod: String = "Transfer Bank",
    val paymentDate: String? = null,
    val receiptNumber: String? = null
)

data class CreateInvoiceRequest(
    val projectId: Long,
    val totalAmount: Double,
    val dueDate: String,
    val invoiceType: String = "full",
    val notes: String? = null
)

data class CreateAgentRequest(
    val agentCode: String,
    val name: String,
    val email: String,
    val password: String = "password123",
    val phoneNumber: String? = null,
    val role: String,
    val agentType: String = "external",
    val ratePercent: Double = 5.0,
    val uplineId: Long? = null
)

data class UpdateAgentRequest(
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val agentType: String? = null,
    val ratePercent: Double? = null,
    val isActive: Boolean? = null
)

data class CreatePayoutRequest(
    val commissionId: Long,
    val amountPaid: Double,
    val paymentMethod: String = "Transfer Bank"
)

data class RegisterDeviceTokenRequest(
    val fcmToken: String
)

// Privasi leaderboard: response tidak pernah mengirim nama, email, maupun nomor HP.
data class LeaderboardEntryDto(
    val rank: Int = 0,
    val agentCode: String = "",
    val totalDeals: Int = 0,
    val totalDealValue: Double = 0.0
)
data class LeaderboardResponseDto(
    val ranking: List<LeaderboardEntryDto> = emptyList()
)

// Modul Workspace Proyek (milestone, jadwal, lampiran).
data class ProjectWorkspaceDto(
    val projectId: Long = 0,
    val progressPercent: Int = 0,
    val completedMilestones: Int = 0,
    val totalMilestones: Int = 0,
    val canEdit: Boolean = false,
    val milestones: List<ProjectWorkspaceMilestoneDto> = emptyList(),
    val tasks: List<ProjectWorkspaceTaskDto> = emptyList(),
    val meetings: List<ProjectWorkspaceMeetingDto> = emptyList(),
    val documents: List<ProjectWorkspaceDocumentDto> = emptyList()
)
data class ProjectWorkspaceMilestoneDto(
    val id: Long = 0,
    val title: String = "",
    val status: String = "pending"
)
data class ProjectWorkspaceTaskDto(
    val id: Long = 0,
    val title: String = "",
    @Json(name = "due_at") val dueAt: String? = null
)
data class ProjectWorkspaceMeetingDto(
    val id: Long = 0,
    @Json(name = "meeting_title") val meetingTitle: String = "",
    @Json(name = "meeting_at") val meetingAt: String? = null
)
data class ProjectWorkspaceDocumentDto(
    val id: Long = 0,
    @Json(name = "original_filename") val originalFilename: String = ""
)
data class CreateMilestoneRequest(val title: String)
data class MilestoneStatusRequest(val status: String = "completed")
data class CreateFollowUpRequest(val title: String, val dueAt: String)
data class CreateMeetingRequest(val meetingTitle: String, val meetingAt: String)
