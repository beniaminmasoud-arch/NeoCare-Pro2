package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeightPreset
import java.util.Locale

@Composable
fun BabyVitalsHeader(
  weightKg: Double,
  birthWeightG: Double,
  gaWeeks: Int,
  ageHours: Int,
  isUnderWarmerOrPhoto: Boolean,
  patientLabel: String,
  isArabic: Boolean,
  onWeightChange: (Double) -> Unit,
  onGaChange: (Int) -> Unit,
  onAgeChange: (Int) -> Unit,
  onWarmerPhotoToggle: (Boolean) -> Unit,
  onPresetSelected: (WeightPreset) -> Unit,
  onOpenSavedPatients: () -> Unit,
  onShareSummary: () -> Unit,
  onToggleLanguage: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showDirectEditDialog by remember { mutableStateOf(false) }

  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.background,
    shadowElevation = 2.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
      // Top row: App branding & quick actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Icon(
            imageVector = Icons.Default.ChildCare,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Neo Care ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
              )
              Text(
                text = "Pro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Text(
              text = if (isArabic) "مرجع NICU – للأطباء فقط" else "NICU Reference – Clinicians Only",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Actions: Patients List, Summary Share, Language
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onOpenSavedPatients,
            modifier = Modifier
              .size(36.dp)
              .testTag("saved_patients_button")
          ) {
            Icon(
              imageVector = Icons.Default.FolderShared,
              contentDescription = if (isArabic) "ملفات المرضى" else "Patient Records",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          IconButton(
            onClick = onShareSummary,
            modifier = Modifier
              .size(36.dp)
              .testTag("share_summary_button")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = if (isArabic) "مشاركة التقرير" else "Share Summary",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          IconButton(
            onClick = onToggleLanguage,
            modifier = Modifier
              .size(36.dp)
              .testTag("toggle_language_button")
          ) {
            Icon(
              imageVector = Icons.Default.Language,
              contentDescription = "Language",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Patient Active Bed / Label
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            .clickable { showDirectEditDialog = true }
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "👶 $patientLabel",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit",
              modifier = Modifier.size(12.dp),
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }

        // Warmer / Phototherapy +20 ml/kg toggle chip
        FilterChip(
          selected = isUnderWarmerOrPhoto,
          onClick = { onWarmerPhotoToggle(!isUnderWarmerOrPhoto) },
          label = {
            Text(
              text = if (isArabic) "مدفأة/ضوء (+20)" else "Warmer/Photo (+20)",
              style = MaterialTheme.typography.labelSmall
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.WbSunny,
              contentDescription = null,
              modifier = Modifier.size(14.dp)
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          ),
          modifier = Modifier
            .height(28.dp)
            .testTag("warmer_chip")
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Main Vitals 3-Card Grid
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // 1. Weight (kg)
        VitalMetricCard(
          label = if (isArabic) "الوزن (كجم)" else "Weight (kg)",
          valueDisplay = String.format(Locale.US, "%.2f", weightKg),
          subLabel = "${(weightKg * 1000).toInt()}g",
          onDecrement = { if (weightKg > 0.4) onWeightChange(Math.round((weightKg - 0.1) * 100.0) / 100.0) },
          onIncrement = { onWeightChange(Math.round((weightKg + 0.1) * 100.0) / 100.0) },
          onTapValue = { showDirectEditDialog = true },
          modifier = Modifier.weight(1f)
        )

        // 2. Gestational Age (GA)
        VitalMetricCard(
          label = if (isArabic) "عمر الحمل (GA)" else "GA (weeks)",
          valueDisplay = "$gaWeeks",
          subLabel = if (gaWeeks < 37) (if (isArabic) "خديج" else "Preterm") else (if (isArabic) "كامل" else "Term"),
          onDecrement = { if (gaWeeks > 23) onGaChange(gaWeeks - 1) },
          onIncrement = { if (gaWeeks < 43) onGaChange(gaWeeks + 1) },
          onTapValue = { showDirectEditDialog = true },
          modifier = Modifier.weight(1f)
        )

        // 3. Postnatal Age (hours)
        VitalMetricCard(
          label = if (isArabic) "العمر (ساعة)" else "Age (hours)",
          valueDisplay = "$ageHours",
          subLabel = "${ageHours / 24}d ${ageHours % 24}h",
          onDecrement = { if (ageHours > 0) onAgeChange(Math.max(0, ageHours - 6)) },
          onIncrement = { onAgeChange(ageHours + 6) },
          onTapValue = { showDirectEditDialog = true },
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Quick Weight Preset Chips Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        WeightPreset.values().forEach { preset ->
          AssistChip(
            onClick = { onPresetSelected(preset) },
            label = {
              Text(
                text = if (isArabic) preset.labelAr else preset.labelEn,
                fontSize = 11.sp
              )
            },
            colors = AssistChipDefaults.assistChipColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.height(28.dp)
          )
        }
      }
    }
  }

  // Direct Edit Dialog
  if (showDirectEditDialog) {
    DirectEditVitalsDialog(
      currentWeight = weightKg,
      currentGa = gaWeeks,
      currentAge = ageHours,
      currentLabel = patientLabel,
      isArabic = isArabic,
      onDismiss = { showDirectEditDialog = false },
      onConfirm = { w, ga, age, label ->
        onWeightChange(w)
        onGaChange(ga)
        onAgeChange(age)
        showDirectEditDialog = false
      }
    )
  }
}

@Composable
fun VitalMetricCard(
  label: String,
  valueDisplay: String,
  subLabel: String,
  onDecrement: () -> Unit,
  onIncrement: () -> Unit,
  onTapValue: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(MaterialTheme.colorScheme.surface)
      .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
      .padding(6.dp)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(2.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onDecrement),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "Decrease",
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurface
          )
        }

        Text(
          text = valueDisplay,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.clickable(onClick = onTapValue)
        )

        Box(
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onIncrement),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Increase",
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
      }
      Text(
        text = subLabel,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 10.sp
      )
    }
  }
}

@Composable
fun DirectEditVitalsDialog(
  currentWeight: Double,
  currentGa: Int,
  currentAge: Int,
  currentLabel: String,
  isArabic: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (weight: Double, ga: Int, age: Int, label: String) -> Unit
) {
  var weightText by remember { mutableStateOf(currentWeight.toString()) }
  var gaText by remember { mutableStateOf(currentGa.toString()) }
  var ageText by remember { mutableStateOf(currentAge.toString()) }
  var labelText by remember { mutableStateOf(currentLabel) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isArabic) "تعديل بيانات الطفل" else "Edit Patient Parameters",
        style = MaterialTheme.typography.titleMedium
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = labelText,
          onValueChange = { labelText = it },
          label = { Text(if (isArabic) "رقم السرير / الاسم" else "Bed / Patient Label") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = weightText,
          onValueChange = { weightText = it },
          label = { Text(if (isArabic) "الوزن (كجم)" else "Weight (kg)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = gaText,
          onValueChange = { gaText = it },
          label = { Text(if (isArabic) "عمر الحمل (أسابيع)" else "Gestational Age (weeks)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = ageText,
          onValueChange = { ageText = it },
          label = { Text(if (isArabic) "العمر بعد الولادة (ساعات)" else "Postnatal Age (hours)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val w = weightText.toDoubleOrNull() ?: currentWeight
          val g = gaText.toIntOrNull() ?: currentGa
          val a = ageText.toIntOrNull() ?: currentAge
          onConfirm(w, g, a, labelText)
        }
      ) {
        Text(if (isArabic) "حفظ وتطبيق" else "Apply")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (isArabic) "إلغاء" else "Cancel")
      }
    }
  )
}
