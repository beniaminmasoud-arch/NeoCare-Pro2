package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.ClinicalMessage
import com.example.data.model.MessageSeverity
import com.example.ui.theme.NeoAmberWarn
import com.example.ui.theme.NeoAmberWarnDark
import com.example.ui.theme.NeoAmberWarnSoft
import com.example.ui.theme.NeoAmberWarnSoftDark
import com.example.ui.theme.NeoGreenSuccess
import com.example.ui.theme.NeoGreenSuccessDark
import com.example.ui.theme.NeoGreenSuccessSoft
import com.example.ui.theme.NeoGreenSuccessSoftDark
import com.example.ui.theme.NeoRedAlert
import com.example.ui.theme.NeoRedAlertDark
import com.example.ui.theme.NeoRedAlertSoft
import com.example.ui.theme.NeoRedAlertSoftDark
import com.example.ui.theme.NeoTealContainerDark
import com.example.ui.theme.NeoTealContainerLight
import com.example.ui.theme.NeoTealDark
import com.example.ui.theme.NeoTealPrimary

@Composable
fun ClinicalAlertBox(
  message: ClinicalMessage,
  modifier: Modifier = Modifier
) {
  ClinicalAlertBox(
    severity = message.severity,
    title = message.title,
    body = message.message,
    modifier = modifier
  )
}

@Composable
fun ClinicalAlertBox(
  severity: MessageSeverity,
  title: String,
  body: String,
  modifier: Modifier = Modifier
) {
  val isDark = isSystemInDarkTheme()

  val (bgColor, textColor, borderColor, icon) = when (severity) {
    MessageSeverity.OK -> listOf(
      if (isDark) NeoGreenSuccessSoftDark else NeoGreenSuccessSoft,
      if (isDark) NeoGreenSuccessDark else NeoGreenSuccess,
      if (isDark) NeoGreenSuccessDark.copy(alpha = 0.4f) else NeoGreenSuccess.copy(alpha = 0.3f),
      Icons.Default.CheckCircle
    )
    MessageSeverity.WARN -> listOf(
      if (isDark) NeoAmberWarnSoftDark else NeoAmberWarnSoft,
      if (isDark) NeoAmberWarnDark else NeoAmberWarn,
      if (isDark) NeoAmberWarnDark.copy(alpha = 0.4f) else NeoAmberWarn.copy(alpha = 0.3f),
      Icons.Default.Warning
    )
    MessageSeverity.BAD -> listOf(
      if (isDark) NeoRedAlertSoftDark else NeoRedAlertSoft,
      if (isDark) NeoRedAlertDark else NeoRedAlert,
      if (isDark) NeoRedAlertDark.copy(alpha = 0.4f) else NeoRedAlert.copy(alpha = 0.3f),
      Icons.Default.Error
    )
    MessageSeverity.INFO -> listOf(
      if (isDark) NeoTealContainerDark else NeoTealContainerLight,
      if (isDark) NeoTealDark else NeoTealPrimary,
      if (isDark) NeoTealDark.copy(alpha = 0.3f) else NeoTealPrimary.copy(alpha = 0.25f),
      Icons.Default.Info
    )
  }

  val bg = bgColor as Color
  val fg = textColor as Color
  val border = borderColor as Color
  val ic = icon as androidx.compose.ui.graphics.vector.ImageVector

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(bg)
      .border(1.dp, border, RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Row(verticalAlignment = Alignment.Top) {
      Icon(
        imageVector = ic,
        contentDescription = null,
        tint = fg,
        modifier = Modifier
          .size(20.dp)
          .padding(top = 2.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Column(modifier = Modifier.weight(1f)) {
        if (title.isNotBlank()) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = fg
          )
          Spacer(modifier = Modifier.height(2.dp))
        }
        Text(
          text = body,
          style = MaterialTheme.typography.bodyMedium,
          color = if (isDark) MaterialTheme.colorScheme.onSurface else fg
        )
      }
    }
  }
}
