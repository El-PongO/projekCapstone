package com.example.projectcapstone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

data class AppBottomNavItem(
    val label: String,
    val icon: ImageVector,
    val selected: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun AppBottomBar(
    items: List<AppBottomNavItem>,
    centerAction: AppBottomNavItem? = null,
    backgroundColor: Color = Color.White,
    selectedColor: Color = Color(0xFF1C63F2),
    unselectedColor: Color = Color(0xFF75849B),
    centerColor: Color = selectedColor
) {
    val slotCount = items.size + if (centerAction != null) 1 else 0
    val centerIndex = if (centerAction != null) items.size / 2 else -1

    Box(
        modifier = Modifier.fillMaxWidth().        height(64.dp).zIndex(1f)
    ) {
        Surface(
                    modifier = Modifier.fillMaxSize(),
            color = backgroundColor,
            shadowElevation = 5.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var itemIndex = 0
                repeat(slotCount) { slotIndex ->
                    if (slotIndex == centerIndex) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        val item = items[itemIndex++]
                        Column(
                            modifier = Modifier.weight(1f).fillMaxSize()
                                .clickable(onClick = item.onClick),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (item.selected) selectedColor.copy(alpha = 0.10f)
                                        else Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (item.selected) selectedColor else unselectedColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = item.label,
                                color = if (item.selected) selectedColor else unselectedColor,
                                fontSize = 10.sp,
                                fontWeight = if (item.selected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        centerAction?.let { action ->
            Column(
                modifier =                 Modifier.align(Alignment.TopCenter).offset(y = (-18).dp)
                                    .width(76.dp).height(82.dp)
                                    .clickable(onClick = action.onClick)
                                    .padding(bottom = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(centerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        tint = Color.White,
                        modifier = Modifier.size(23.dp)
                    )
                }
                Text(
                    text = action.label,
                    color = if (action.selected) selectedColor else unselectedColor,
                    fontSize = 10.sp,
                    fontWeight = if (action.selected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
