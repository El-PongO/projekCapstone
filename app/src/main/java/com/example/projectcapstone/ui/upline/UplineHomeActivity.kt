package com.example.projectcapstone.ui.upline

import android.content.Intent
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
import com.example.projectcapstone.data.session.SessionManager
import com.example.projectcapstone.ui.Navigator

class UplineHomeActivity : AppCompatActivity() {

    private data class DealCard(val title: String, val stage: String, val value: String)
    private data class ActivityCard(val agentCode: String, val event: String, val detail: String, val time: String)

    private val deals = listOf(
        DealCard("Inventory App - Client A", "Stage: Proposal sent", "Rp 25.000.000"),
        DealCard("Booking System - Client B", "Stage: Negotiation", "Rp 12.500.000")
    )
    private val activities = listOf(
        ActivityCard("AGT-0012", "Project marked as Deal", "Deal value: Rp 18.000.000", "2h ago"),
        ActivityCard("AGT-0007", "Stage updated", "Negotiation to Proposal sent", "Yesterday"),
        ActivityCard("AGT-0021", "Project cancelled", "Reason: Client budget", "2 days ago")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upline_home)

        val root = findViewById<View>(R.id.upline_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val name = intent.getStringExtra(EXTRA_NAME).orEmpty().ifBlank { "Upline" }
        findViewById<TextView>(R.id.greeting).text = getString(R.string.welcome, name)
        findViewById<TextView>(R.id.stat_deals_value).text = deals.size.toString()
        findViewById<TextView>(R.id.stat_commission_value).text = "Rp 13.000.000"

        val soon = View.OnClickListener { toast() }
        listOf(
            R.id.btn_notifications, R.id.action_downline,
            R.id.action_crm, R.id.nav_earnings, R.id.nav_add
        ).forEach { findViewById<View>(it).setOnClickListener(soon) }
        findViewById<View>(R.id.action_performance).setOnClickListener {
            startActivity(Intent(this, MyPerformanceActivity::class.java))
        }
        findViewById<View>(R.id.nav_profile).setOnClickListener {
            SessionManager(this).clear()
            startActivity(Navigator.login(this))
        }

        val dealsContainer = findViewById<LinearLayout>(R.id.deals_container)
        deals.forEach { d ->
            addCard(dealsContainer, d.title, d.stage, d.value, listOf(R.string.view, R.string.update, R.string.details))
        }
        val activityContainer = findViewById<LinearLayout>(R.id.activity_container)
        activities.forEach { a ->
            addCard(activityContainer, "${a.agentCode} - ${a.event}", a.detail, a.time, emptyList())
        }
    }

    private fun addCard(parent: LinearLayout, title: String, line1: String, line2: String, actions: List<Int>) {
        val card = LayoutInflater.from(this).inflate(R.layout.item_card, parent, false)
        card.findViewById<TextView>(R.id.card_title).text = title
        card.findViewById<TextView>(R.id.card_line1).text = line1
        card.findViewById<TextView>(R.id.card_line2).text = line2
        val actionRow = card.findViewById<LinearLayout>(R.id.card_actions)
        if (actions.isEmpty()) {
            actionRow.visibility = View.GONE
        } else {
            val density = resources.displayMetrics.density
            val margin = (6 * density).toInt()
            val padH = (14 * density).toInt()
            val padV = (6 * density).toInt()
            actions.forEach { label ->
                val pill = TextView(this).apply {
                    setText(label)
                    textSize = 12f
                    setTextColor(getColor(R.color.text_primary))
                    setBackgroundResource(R.drawable.bg_pill)
                    setPadding(padH, padV, padH, padV)
                    setOnClickListener { toast() }
                }
                actionRow.addView(pill, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginStart = margin })
            }
        }
        parent.addView(card)
    }

    private fun toast() = Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()

    companion object {
        const val EXTRA_NAME = "extra_name"
    }
}
