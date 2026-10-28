package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BabyEntity
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedPatientsDialog(
  viewModel: MainViewModel,
  onDismiss: () -> Unit
) {
  val isAr by viewModel.isArabic.collectAsState()
  val savedBabies by viewModel.savedBabies.collectAsState()
  val currentLabel by viewModel.patientLabel.collectAsState()
  val currentWeight by viewModel.weightKg.collectAsState()

  var isSavingNew by remember { mutableStateOf(false) }
  var newLabelText by remember { mutableStateOf(currentLabel) }
  var newNotesText by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.FolderShared,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
      )
    },
    title = {
      Text(
        text = if (isAr) "ملفات المرضى المحفوظة (NICU Records)" else "Saved Patient Records",
        style = MaterialTheme.typography.titleMedium
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .height(380.dp)
      ) {
        if (!isSavingNew) {
          // Action Buttons: Save Current or Start New
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { isSavingNew = true },
              modifier = Modifier
                .weight(1f)
                .testTag("save_current_patient_button")
            ) {
              Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isAr) "حفظ الحالي" else "Save Current",
                fontSize = 12.sp
              )
            }

            OutlinedButton(
              onClick = {
                viewModel.resetToDefaults()
                onDismiss()
              },
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isAr) "مريض جديد" else "New Patient",
                fontSize = 12.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
          Spacer(modifier = Modifier.height(8.dp))

          // Saved List
          if (savedBabies.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = if (isAr) "لا توجد ملفات محفوظة حالياً." else "No saved records yet.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = if (isAr) "احفظ حسابات الطفل للرجوع إليها لاحقاً." else "Save active baby calculations to recall them anytime.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              items(savedBabies, key = { it.id }) { baby ->
                SavedBabyItemRow(
                  baby = baby,
                  isArabic = isAr,
                  onSelect = {
                    viewModel.loadPatient(baby)
                    onDismiss()
                  },
                  onDelete = { viewModel.deletePatient(baby) }
                )
              }
            }
          }
        } else {
          // Save Form
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = if (isAr) "حفظ التقييم الحالي (${currentWeight} كجم):" else "Save current assessment (${currentWeight} kg):",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
              value = newLabelText,
              onValueChange = { newLabelText = it },
              label = { Text(if (isAr) "رقم السرير / الاسم" else "Bed / Patient Label") },
              modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
              value = newNotesText,
              onValueChange = { newNotesText = it },
              label = { Text(if (isAr) "ملاحظات إكلينيكية (اختياري)" else "Clinical Notes (Optional)") },
              modifier = Modifier.fillMaxWidth(),
              maxLines = 3
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              TextButton(onClick = { isSavingNew = false }) {
                Text(if (isAr) "إلغاء" else "Cancel")
              }
              Spacer(modifier = Modifier.width(8.dp))
              Button(
                onClick = {
                  viewModel.saveCurrentPatient(newLabelText, newNotesText)
                  isSavingNew = false
                  onDismiss()
                }
              ) {
                Text(if (isAr) "حفظ" else "Save")
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(if (isAr) "إغلاق" else "Close")
      }
    }
  )
}

@Composable
fun SavedBabyItemRow(
  baby: BabyEntity,
  isArabic: Boolean,
  onSelect: () -> Unit,
  onDelete: () -> Unit
) {
  val dateFormatted = remember(baby.updatedAtMillis) {
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    sdf.format(Date(baby.updatedAtMillis))
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
      .clickable(onClick = onSelect)
      .padding(horizontal = 10.dp, vertical = 8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = baby.nameOrBed,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = dateFormatted,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
          )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "${baby.weightKg} kg • GA ${baby.gestationalAgeWeeks}w • Age ${baby.ageHours}h" +
              (if (baby.tsb != null) " • TSB: ${baby.tsb}" else ""),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.primary,
          fontSize = 11.sp
        )
        if (baby.notes.isNotBlank()) {
          Text(
            text = baby.notes,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
        }
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete",
          tint = MaterialTheme.colorScheme.error,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}
