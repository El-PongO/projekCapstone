package com.example.projectcapstone.ui.upline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projectcapstone.R
import com.example.projectcapstone.data.model.Performance
import com.example.projectcapstone.data.repository.ApiException
import com.example.projectcapstone.data.repository.PerformanceRepository
import com.example.projectcapstone.data.session.SessionManager
import com.example.projectcapstone.ui.Navigator
import com.google.android.material.tabs.TabLayout
import java.util.Locale

class MyPerformanceActivity : AppCompatActivity() {

    private var performance: Performance? = null
    private lateinit var listContainer: LinearLayout
    private lateinit var emptyText: TextView
    private lateinit var tabs: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_performance)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.perf_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val session = SessionManager(this)
        val user = session.getUser()
        val token = session.getToken()
        if (user == null || token == null) {
            startActivity(Navigator.login(this))
            return
        }

        findViewById<TextView>(R.id.greeting).text = user.name
        listContainer = findViewById(R.id.list_container)
        emptyText = findViewById(R.id.empty_text)
        tabs = findViewById(R.id.tabs)

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<View>(R.id.nav_home).setOnClickListener { finish() }
        val soon = View.OnClickListener {
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        }
        listOf(R.id.btn_notifications, R.id.nav_earnings, R.id.nav_add, R.id.nav_profile)
            .forEach { findViewById<View>(it).setOnClickListener(soon) }

        tabs.addTab(tabs.newTab().setText(R.string.tab_sales_report))
        tabs.addTab(tabs.newTab().setText(R.string.tab_income))
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = render()
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        PerformanceRepository.load(token) { result ->
            result.onSuccess {
                performance = it
                findViewById<TextView>(R.id.stat_sales).text = rupiah(it.totalSales)
                findViewById<TextView>(R.id.stat_income).text = rupiah(it.totalIncome)
                render()
            }.onFailure { e ->
                if (e is ApiException && e.unauthorized) {
                    session.clear()
                    startActivity(Navigator.login(this))
                } else {
                    showEmpty(getString(R.string.error_load))
                }
            }
        }
    }

    private fun render() {
        val data = performance ?: return
        listContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        if (tabs.selectedTabPosition == 0) {
            data.sales.forEach { s ->
                val row = inflater.inflate(R.layout.item_performance_row, listContainer, false)
                row.findViewById<TextView>(R.id.row_title).text = s.projectCode
                row.findViewById<TextView>(R.id.row_subtitle).text = s.clientName
                row.findViewById<TextView>(R.id.row_detail).text = pretty(s.stage)
                row.findViewById<TextView>(R.id.row_date).text = s.date.take(10)
                row.findViewById<TextView>(R.id.row_amount).text = rupiah(s.dealValue)
                row.findViewById<TextView>(R.id.row_status).text = pretty(s.status)
                listContainer.addView(row)
            }
            showEmpty(if (data.sales.isEmpty()) getString(R.string.empty_sales) else null)
        } else {
            data.income.forEach { i ->
                val row = inflater.inflate(R.layout.item_performance_row, listContainer, false)
                row.findViewById<TextView>(R.id.row_title).text = i.projectCode
                row.findViewById<TextView>(R.id.row_subtitle).text = i.clientName
                row.findViewById<TextView>(R.id.row_detail).text =
                    getString(R.string.deal_value_label, rupiah(i.dealValue)) + " - " +
                        getString(R.string.commission_rate, trim(i.ratePercent))
                row.findViewById<TextView>(R.id.row_date).text = i.date.take(10)
                row.findViewById<TextView>(R.id.row_amount).text = rupiah(i.commission)
                row.findViewById<View>(R.id.row_status).visibility = View.GONE
                listContainer.addView(row)
            }
            showEmpty(if (data.income.isEmpty()) getString(R.string.empty_income) else null)
        }
    }

    private fun showEmpty(message: String?) {
        emptyText.text = message
        emptyText.visibility = if (message == null) View.GONE else View.VISIBLE
    }

    private fun rupiah(value: Double): String =
        "Rp " + String.format(Locale("in", "ID"), "%,.0f", value)

    private fun trim(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

    private fun pretty(raw: String): String =
        raw.replace('_', ' ').replaceFirstChar { it.uppercase() }
}
