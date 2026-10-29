package com.example.projectcapstone.data.model

data class SalesItem(
    val projectCode: String,
    val clientName: String,
    val status: String,
    val stage: String,
    val dealValue: Double,
    val date: String
)

data class IncomeItem(
    val projectCode: String,
    val clientName: String,
    val dealValue: Double,
    val commission: Double,
    val ratePercent: Double,
    val date: String
)

data class Performance(
    val totalSales: Double,
    val totalIncome: Double,
    val sales: List<SalesItem>,
    val income: List<IncomeItem>
)
