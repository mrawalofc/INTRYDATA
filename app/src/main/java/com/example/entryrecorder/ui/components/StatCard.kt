package com.example.entryrecorder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.entryrecorder.ui.DashboardStats
import com.example.entryrecorder.ui.theme.*
import java.util.Locale

import androidx.compose.ui.platform.testTag

private val TealDark = Color(0xFF0D9488)
private val TealLight = Color(0xFFCCFBF1)
private val EmeraldDark = Color(0xFF15803D)
private val EmeraldLight = Color(0xFFDCFCE7)

@Composable
fun DashboardStatsGrid(
    stats: DashboardStats,
    canViewStats: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (canViewStats) {
            // Admin or Permitted user: Total Records & Total Amount
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Total Records",
                    value = stats.totalCount.toString(),
                    icon = Icons.Default.FormatListNumbered,
                    iconColor = PrimaryBlue,
                    bgColor = PrimaryLightBlue,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_total_records")
                )
                StatCard(
                    title = "Total Amount",
                    value = String.format(Locale.US, "$%.2f", stats.totalAmount),
                    icon = Icons.Default.AttachMoney,
                    iconColor = SuccessGreen,
                    bgColor = SuccessLightGreen,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_total_amount")
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Today's Entries",
                    value = stats.todayCount.toString(),
                    icon = Icons.Default.CalendarToday,
                    iconColor = AdminPurple,
                    bgColor = AdminLightPurple,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_today")
                )
                StatCard(
                    title = "Today's Amount",
                    value = String.format(Locale.US, "$%.2f", stats.todayAmount),
                    icon = Icons.Default.AttachMoney,
                    iconColor = EmeraldDark,
                    bgColor = EmeraldLight,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_today_amount")
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "This Month",
                    value = stats.monthCount.toString(),
                    icon = Icons.Default.DateRange,
                    iconColor = TealDark,
                    bgColor = TealLight,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_month")
                )
                StatCard(
                    title = "Last Entry",
                    value = stats.lastEntryName,
                    icon = Icons.Default.Person,
                    iconColor = WarningAmber,
                    bgColor = WarningLightAmber,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_last_entry")
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Company",
                    value = "Bin Mishal Travels",
                    icon = Icons.Default.FlightTakeoff,
                    iconColor = PrimaryDarkBlue,
                    bgColor = PrimaryLightBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stat_card_company")
                )
            }
        } else {
            // Non-admin standard user: Total Amount is strictly hidden
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Total Records",
                    value = stats.totalCount.toString(),
                    icon = Icons.Default.FormatListNumbered,
                    iconColor = PrimaryBlue,
                    bgColor = PrimaryLightBlue,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_total_records")
                )
                StatCard(
                    title = "This Month",
                    value = stats.monthCount.toString(),
                    icon = Icons.Default.DateRange,
                    iconColor = TealDark,
                    bgColor = TealLight,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_month")
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Today's Entries",
                    value = stats.todayCount.toString(),
                    icon = Icons.Default.CalendarToday,
                    iconColor = AdminPurple,
                    bgColor = AdminLightPurple,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_today")
                )
                StatCard(
                    title = "Last Entry",
                    value = stats.lastEntryName,
                    icon = Icons.Default.Person,
                    iconColor = WarningAmber,
                    bgColor = WarningLightAmber,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_last_entry")
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Company",
                    value = "Bin Mishal Travels",
                    icon = Icons.Default.FlightTakeoff,
                    iconColor = PrimaryDarkBlue,
                    bgColor = PrimaryLightBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stat_card_company")
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(bgColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate500,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Slate900,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
