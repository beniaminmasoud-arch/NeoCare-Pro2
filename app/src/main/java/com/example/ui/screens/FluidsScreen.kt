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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageSeverity
import com.example.ui.MainViewModel
import com.example.ui.components.ClinicalAlertBox
import com.example.ui.components.ClinicalCard
import java.util.Locale

@Composable
fun FluidsScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val isAr by viewModel.isArabic.collectAsState()
  val weightKg by viewModel.weightKg.collectAsState()
  val birthWeightG by viewModel.birthWeightG.collectAsState()
  val ageHours by viewModel.ageHours.collectAsState()
  val isWarmerPhoto by viewModel.isUnderWarmerOrPhoto.collectAsState()

  val dexText by viewModel.dextrosePercentText.collectAsState()
  val reverseRateText by viewModel.reverseRateText.collectAsState()
  val reverseDexText by viewModel.reverseDexText.collectAsState()

  val fluidsResult = viewModel.getFluidsResult()
  val reverseGir = viewModel.getReverseGirValue()

  Column(
    modifier = modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(bottom = 80.dp)
  ) {
    // 1. Table 15-3 Fluid & GIR Calculator Card
    ClinicalCard(
      title = if (isAr) "السوائل و GIR (Table 15-3)" else "Fluid Requirements & GIR (Table 15-3)",
      icon = Icons.Default.WaterDrop,
      testTag = "fluids_card"
    ) {
      // Inputs Row: Birth weight, Dextrose %
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = "${birthWeightG.toInt()}",
          onValueChange = {
            val v = it.toDoubleOrNull()
            if (v != null) viewModel.updateBirthWeight(v)
          },
          label = { Text(if (isAr) "وزن الميلاد (جم)" else "Birth Wt (g)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier
            .weight(1f)
            .testTag("birth_weight_input")
        )

        OutlinedTextField(
          value = dexText,
          onValueChange = { viewModel.dextrosePercentText.value = it },
          label = { Text(if (isAr) "تركيز الدكستروز %" else "Dextrose %") },
          placeholder = { Text("10") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier
            .weight(1f)
            .testTag("dextrose_percent_input")
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Calculated Fluid Requirements Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
          .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
          .padding(14.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isAr) "المدى الموصى به للسوائل:" else "Recommended Daily Fluid Range:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "${fluidsResult.minMlPerKgDay.toInt()} – ${fluidsResult.maxMlPerKgDay.toInt()} مل/كجم/يوم",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = if (isAr)
              "الحجم الكلي اليومي: ${String.format(Locale.US, "%.0f", fluidsResult.totalMlPerDayMin)} – ${String.format(Locale.US, "%.0f", fluidsResult.totalMlPerDayMax)} مل/يوم"
            else
              "Total volume: ${String.format(Locale.US, "%.0f", fluidsResult.totalMlPerDayMin)} – ${String.format(Locale.US, "%.0f", fluidsResult.totalMlPerDayMax)} ml/day",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(8.dp))
          HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
          Spacer(modifier = Modifier.height(8.dp))

          // Midpoint Rate & GIR
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = if (isAr) "معدل المحلول عند المتوسط:" else "Midpoint Infusion Rate:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format(Locale.US, "%.1f", fluidsResult.hourlyRateMidpoint)} مل/ساعة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "(${String.format(Locale.US, "%.0f", fluidsResult.midMlPerKgDay)} مل/كجم/يوم)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "Glucose Infusion Rate (GIR):",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format(Locale.US, "%.1f", fluidsResult.gir)} mg/kg/min",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (fluidsResult.isGirLow || fluidsResult.isGirHigh) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
              )
              Text(
                text = if (fluidsResult.gir in 4.0..8.0) (if (isAr) "المدى المثالي (4–8)" else "Optimal (4–8)")
                else if (fluidsResult.isGirLow) (if (isAr) "منخفض (< 4)" else "Low (< 4)")
                else (if (isAr) "مرتفع (> 8)" else "High (> 8)"),
                style = MaterialTheme.typography.labelSmall,
                color = if (fluidsResult.isGirLow || fluidsResult.isGirHigh) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Safety Alerts for GIR
      if (fluidsResult.isGirLow) {
        ClinicalAlertBox(
          severity = MessageSeverity.WARN,
          title = if (isAr) "تنبيه: GIR منخفض (< 4 mg/kg/min)" else "Warning: Low GIR (< 4 mg/kg/min)",
          body = if (isAr)
            "معدل تسريب الجلوكوز أقل من 4 مجم/كجم/دقيقة غير كافٍ للحفاظ على توازن السكر عند حديثي الولادة. ارفع تركيز الدكستروز أو معدل السوائل."
          else
            "GIR is under 4 mg/kg/min, generally insufficient for neonatal baseline energy expenditure. Increase dextrose concentration or rate."
        )
      }

      if (fluidsResult.isPeripheralVeinLimitExceeded) {
        ClinicalAlertBox(
          severity = MessageSeverity.BAD,
          title = if (isAr) "تحذير: لا تتجاوز D12.5% في وريد طرفي" else "Warning: D12.5% Peripheral Limit",
          body = if (isAr)
            "تركيز الدكستروز أعلى من 12.5% ممنوع في الأوردة الطرفية لمنع التهاب الوريد الخثاري ونخر الأنسجة (Extravasation necrosis). يتطلب خط وريدي مركزي (UVC أو PICC)."
          else
            "Dextrose concentration > 12.5% requires a central venous line (UVC or PICC). Do not infuse via peripheral veins."
        )
      } else if (fluidsResult.isGirHigh) {
        ClinicalAlertBox(
          severity = MessageSeverity.WARN,
          title = if (isAr) "تنبيه: GIR مرتفع (> 12.5 mg/kg/min)" else "Notice: High GIR (> 12.5 mg/kg/min)",
          body = if (isAr)
            "GIR مرتفع قد يسبب ارتفاع السكر (Hyperglycemia) وإدرار البول التناضحي. راقب نسبة السكر والبول بانتظام."
          else
            "High GIR may precipitate hyperglycemia, glycosuria, and osmotic diuresis. Monitor blood glucose closely."
        )
      }
    }

    // 2. Reference Table 15-3 Card
    ClinicalCard(
      title = if (isAr) "جدول السوائل المرجعي (Table 15-3)" else "Fluid Requirement Reference (Table 15-3)",
      icon = Icons.Default.TableChart
    ) {
      Text(
        text = if (isAr)
          "القيم بـ (مل/كجم/يوم). يُضاف +20 مل/كجم/يوم لو كان الطفل تحت مدفأة إشعاعية أو علاج ضوئي:"
        else
          "Values in ml/kg/day. Add +20 ml/kg/day under radiant warmer or phototherapy:",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Table layout
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
          Text("BW (g)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
          Text("Dex%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f))
          Text("<24h", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
          Text("24–48h", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
          Text(">48h", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }

        val rows = listOf(
          Table15Row("< 1000", "5 – 7.5", "100–120", "120–140", "140–190", birthWeightG < 1000),
          Table15Row("1000–1500", "7.5 – 10", "80–100", "100–120", "120–160", birthWeightG in 1000.0..1499.0),
          Table15Row("1500–2500", "10", "60–80", "80–100", "100–150", birthWeightG in 1500.0..2500.0),
          Table15Row("> 2500", "10", "60–80", "80–100", "90–150", birthWeightG > 2500)
        )

        rows.forEachIndexed { idx, row ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                if (row.isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else if (idx % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
              )
              .padding(horizontal = 6.dp, vertical = 6.dp)
          ) {
            Text(
              text = row.bw + if (row.isCurrent) " 👈" else "",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = if (row.isCurrent) FontWeight.Bold else FontWeight.Normal,
              color = if (row.isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1.2f)
            )
            Text(row.dex, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.9f))
            Text(row.d1, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text(row.d2, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text(row.d3, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Hypoglycemia Acute Protocol Box
      ClinicalAlertBox(
        severity = MessageSeverity.WARN,
        title = if (isAr) "بروتوكول نقص السكر الحاد (Hypoglycemia Protocol)" else "Acute Hypoglycemia Management Protocol",
        body = if (isAr)
          "• عند ظهور أعراض نقص السكر (رجفان، خمول، تشنج، عرق) أو سكر < 40 mg/dL:\n" +
              "  1. أعطِ دفعة إسعافية: 2 مل/كجم من D10W (= 200 مجم/كجم) وريدياً على دقيقة.\n" +
              "  2. ابدأ فوراً تسريب وريدي مستمر بمعدل GIR = 6–8 مجم/كجم/دقيقة.\n" +
              "  3. أعد قياس السكر بعد 30 دقيقة: لو استمر < 45 زِد الـ GIR بمقدار 1–2 مجم/كجم/دقيقة كل مرة.\n" +
              "  4. الفطام التدريجي: عند استقرار السكر 12 ساعة، اخفض GIR تدريجياً، وأوقف المحلول عندما يصبح GIR < 4 مع ثبات الرضاعة."
        else
          "• Symptomatic hypoglycemia or BG < 40 mg/dL:\n" +
              "  1. IV bolus: 2 ml/kg of D10W (= 200 mg/kg) over 1 min.\n" +
              "  2. Follow immediately with continuous infusion at GIR 6–8 mg/kg/min.\n" +
              "  3. Recheck blood glucose in 30 min: if still < 45 mg/dL, increase GIR by 1–2 mg/kg/min.\n" +
              "  4. Wean gradually after 12h of euglycemia; discontinue when GIR < 4 with established enteral feeds."
      )
    }

    // 3. Reverse GIR Calculator Card
    ClinicalCard(
      title = if (isAr) "حساب الـ GIR العكسي (Reverse GIR)" else "Reverse GIR Calculator",
      icon = Icons.Default.Speed,
      testTag = "reverse_gir_card"
    ) {
      Text(
        text = if (isAr)
          "احسب معدل تسريب الجلوكوز اللحظي بناءً على معدل المضخة الحالي وتركيز المحلول:"
        else
          "Calculate current GIR from active infusion pump rate and dextrose percentage:",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = reverseRateText,
          onValueChange = { viewModel.reverseRateText.value = it },
          label = { Text(if (isAr) "معدل المضخة (مل/ساعة)" else "Rate (ml/hr)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
          value = reverseDexText,
          onValueChange = { viewModel.reverseDexText.value = it },
          label = { Text(if (isAr) "تركيز الدكستروز %" else "Dextrose %") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "GIR = (Rate × Dex%) ÷ (6 × Weight)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = if (isAr) "الهدف: 4–6 لكامل المدة و 4–8 للخدج" else "Target: 4–6 (Term) | 4–8 (Preterm)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.secondary,
              fontWeight = FontWeight.SemiBold
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.primaryContainer)
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text = "${String.format(Locale.US, "%.1f", reverseGir)} mg/kg/min",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }
  }
}

private data class Table15Row(
  val bw: String,
  val dex: String,
  val d1: String,
  val d2: String,
  val d3: String,
  val isCurrent: Boolean
)
