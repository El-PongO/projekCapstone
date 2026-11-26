package com.example.projectcapstone

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Context khusus Admin. Tidak mengubah navigation Upline/Downline.
val LocalIntegraAdminNav = compositionLocalOf<((AdminPage) -> Unit)?> { null }

private val V3Border = Color(0xFFE4EBF5)
private val V3Blue = Color(0xFF1458ED)

@Composable
fun AdminV3PageHero(
    title: String,
    subtitle: String,
    icon: ImageVector,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 4.dp,
        color = V3Blue
    ) {
        Row(
            modifier = Modifier
                .background(Brush.horizontalGradient(listOf(V3Blue, Color(0xFF4089FF))))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(15.dp), color = Color.White.copy(alpha = 0.16f)) {
                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(alpha = 0.88f), fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}

/**
 * Card Admin yang tidak memakai outline hitam dari mockup.
 * Signature sengaja kompatibel dengan LeftAccentCard untuk patch UI tanpa mengubah callback/API.
 */
@Composable
fun AdminV3AccentCard(
    accentColor: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    leftBorderWidth: Dp = 4.dp,
    rightBorderWidth: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius.coerceAtLeast(10.dp)),
        color = Color.White,
        border = BorderStroke(1.dp, V3Border),
        shadowElevation = 3.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        color = accentColor,
                        size = Size(leftBorderWidth.toPx().coerceAtMost(size.width), size.height)
                    )
                }
                .padding(start = leftBorderWidth, end = rightBorderWidth)
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun AdminV3EmptyState(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
        color = Color(0xFFF5F8FD),
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(
            message,
            modifier = Modifier.padding(20.dp),
            fontSize = 13.sp,
            color = Color(0xFF6D7C92)
        )
    }
}
