package com.homeledger.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeledger.app.data.CategoryEntity
import java.text.NumberFormat
import java.util.Locale

fun formatMoney(amountMinor: Long, currency: String = "GHS"): String = "$currency ${NumberFormat.getNumberInstance(Locale.US).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }.format(amountMinor / 100.0)}"

fun iconFor(name: String): ImageVector = when (name) {
    "bolt" -> Icons.Outlined.Bolt
    "cart" -> Icons.Outlined.ShoppingCart
    "home" -> Icons.Outlined.Home
    "build" -> Icons.Outlined.Build
    "car" -> Icons.Outlined.DirectionsCar
    "health" -> Icons.Outlined.MedicalServices
    "school" -> Icons.Outlined.School
    "subscriptions" -> Icons.Outlined.Subscriptions
    else -> Icons.Outlined.Category
}

@Composable
fun ScreenTitle(title: String, supporting: String? = null) {
    Text(title, fontSize = 26.sp, lineHeight = 31.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
    if (supporting != null) Text(supporting, modifier = Modifier.padding(top = 4.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun CategoryIcon(category: CategoryEntity, modifier: Modifier = Modifier) {
    val tint = runCatching { android.graphics.Color.parseColor(category.tint) }.getOrDefault(android.graphics.Color.LTGRAY)
    androidx.compose.foundation.layout.Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(iconFor(category.icon), contentDescription = category.name, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun SectionHeader(title: String, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        trailing?.invoke()
    }
}
