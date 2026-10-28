package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageSeverity
import com.example.ui.MainViewModel
import com.example.ui.components.ClinicalAlertBox
import com.example.ui.components.ClinicalCard
import com.example.ui.components.DoseItemRow
import java.util.Locale

@Composable
fun DrugsScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val isAr by viewModel.isArabic.collectAsState()
  val weightKg by viewModel.weightKg.collectAsState()

  val nrpDoses = viewModel.getNrpEmergencyDoses()
  val respDoses = viewModel.getRespiratoryAndNicDoses()
  val ettSpecs = viewModel.getEttSpecifications()

  Column(
    modifier = modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(bottom = 80.dp)
  ) {
    // 1. Resuscitation (NRP) Emergency Doses Card
    ClinicalCard(
      title = if (isAr) "إنعاش الوليد (NRP) – الجرعات الإسعافية بالوزن" else "NRP Resuscitation Emergency Doses",
      icon = Icons.Default.Emergency,
      testTag = "nrp_card"
    ) {
      Text(
        text = if (isAr)
          "جرعات محسوبة بدقة للوزن الحالي (${String.format(Locale.US, "%.2f", weightKg)} كجم):"
        else
          "Calculated specifically for current patient weight (${String.format(Locale.US, "%.2f", weightKg)} kg):",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      nrpDoses.forEach { doseItem ->
        DoseItemRow(item = doseItem, isArabic = isAr)
      }

      Spacer(modifier = Modifier.height(6.dp))

      ClinicalAlertBox(
        severity = MessageSeverity.BAD,
        title = if (isAr) "خوارزمية الإنعاش القلبي الرئوي (NRP Algorithm)" else "NRP Decision Algorithm",
        body = if (isAr)
          "• إذا كان النبض < 60 بعد 30 ثانية من التهوية الفعالة بالضغط الإيجابي (PPV) + 30 ثانية من الضغطات الصدرية المنسقة (نسبة 3:1): أعطِ Epinephrine وريدياً.\n• إذا ارتفع النبض > 100: أوقف الضغطات واستمر في التهوية حتى التنفس التلقائي."
        else
          "• If HR < 60 bpm after 30s effective PPV + 30s coordinated chest compressions (3:1 ratio): administer IV Epinephrine.\n• If HR > 100 bpm: discontinue chest compressions and support ventilation as needed."
      )
    }

    // 2. Endotracheal Tube (ETT) & Intubation Specifications Card
    ClinicalCard(
      title = if (isAr) "الأنبوب الحنجري وأدوات التنبيب (ETT Sizing)" else "Endotracheal Tube (ETT) Specifications",
      icon = Icons.Default.MedicalServices,
      testTag = "ett_card"
    ) {
      // 2x2 Grid of Sizing parameters
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        EttParamCard(
          title = if (isAr) "قطر الأنبوب (ID)" else "Tube Size (ID)",
          value = ettSpecs.internalDiameterMm,
          sub = if (isAr) "جدول 4-2 بالوزن" else "Weight-based",
          modifier = Modifier.weight(1f)
        )
        EttParamCard(
          title = if (isAr) "العمق عند الشفة" else "Lip Depth",
          value = "${String.format(Locale.US, "%.1f", ettSpecs.lipDepthCm)} cm",
          sub = if (isAr) "قاعدة 6 + الوزن" else "Rule: 6 + Weight",
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        EttParamCard(
          title = if (isAr) "شفرة المنظار (Blade)" else "Laryngoscope Blade",
          value = ettSpecs.bladeSize,
          sub = ettSpecs.bladeType,
          modifier = Modifier.weight(1f)
        )
        EttParamCard(
          title = if (isAr) "قسطرة التشفيط" else "Suction Catheter",
          value = ettSpecs.suctionFr,
          sub = if (isAr) "مناسبة للقطر" else "Matching tube ID",
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      ClinicalAlertBox(
        severity = MessageSeverity.INFO,
        title = if (isAr) "تأكيد موضع الأنبوب" else "Position Confirmation",
        body = if (isAr)
          "قاعدة العمق عند الشفة: [الوزن بالكجم + 6 سم]. تأكد فوراً من سماع أصوات تنفس متناظرة على جانبي الصدر وحركة جدار الصدر والـ Colorimetric CO2 detector، ثم صورة أشعة للصدر ليكون رأس الأنبوب عند مستوى الفقرة الصدرية T2."
        else
          "Depth mark at upper lip: [Weight in kg + 6 cm]. Confirm position by bilateral symmetric breath sounds, chest expansion, end-tidal CO2 detection, and chest radiography targeting T2 vertebral level."
      )
    }

    // 3. Respiratory & NICU Medications Card
    ClinicalCard(
      title = if (isAr) "أدوية الرعاية المركزة الوليدية (NICU Drugs)" else "NICU Medications & Surfactant",
      icon = Icons.Default.Medication,
      testTag = "drugs_card"
    ) {
      Text(
        text = if (isAr)
          "جرعات السيرفكتانت، الكافيين، المضادات الحيوية، وأدوية الانحلال لوزن ${String.format(Locale.US, "%.2f", weightKg)} كجم:"
        else
          "Surfactant, caffeine, antibiotics, and hemolysis management for ${String.format(Locale.US, "%.2f", weightKg)} kg:",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      respDoses.forEach { doseItem ->
        DoseItemRow(item = doseItem, isArabic = isAr)
      }
    }

    // Disclaimer
    Text(
      text = if (isAr)
        "المصادر: AAP 2022، بروتوكولات وزارة الصحة المصرية، دليل أدوية حديثي الولادة (Neofax). أداة مساعدة مرجعية للطبيب ولا تغني عن الحكم الإكلينيكي المباشر والتحقق من الصيدلية السريرية."
      else
        "References: AAP 2022, NRP 8th Edition, Neofax. Clinical reference tool designed for healthcare professionals. Verify doses with clinical judgment and pharmacy.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      lineHeight = 16.sp,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
  }
}

@Composable
fun EttParamCard(
  title: String,
  value: String,
  sub: String,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
      .padding(10.dp)
  ) {
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = sub,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 10.sp
      )
    }
  }
}
