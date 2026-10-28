package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.RespiratoryFactor
import com.example.ui.MainViewModel
import com.example.ui.components.ClinicalAlertBox
import com.example.ui.components.ClinicalCard

@Composable
fun RespiratoryScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val isAr by viewModel.isArabic.collectAsState()
  val selectedFactors by viewModel.selectedRespiratoryFactors.collectAsState()
  val rankedResults = viewModel.getRespiratoryRanked()

  Column(
    modifier = modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(bottom = 80.dp)
  ) {
    // 1. Diagnostic Scoring Card
    ClinicalCard(
      title = if (isAr) "ضيق التنفس الوليدي – ترجيح التشخيص" else "Neonatal Respiratory Distress – Differential",
      icon = Icons.Default.Air,
      testTag = "respiratory_card",
      headerAction = {
        if (selectedFactors.isNotEmpty()) {
          OutlinedButton(
            onClick = { viewModel.selectedRespiratoryFactors.value = emptySet() },
            modifier = Modifier.height(30.dp)
          ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isAr) "إعادة ضبط" else "Reset", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    ) {
      Text(
        text = if (isAr)
          "حدد المعطيات السريرية وتاريخ الولادة لترجيح التشخيص التفريقي وخطط العلاج فوراً:"
        else
          "Select clinical indicators and delivery history to rank differential diagnoses:",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      RespiratoryFactor.values().forEach { factor ->
        val isChecked = selectedFactors.contains(factor.id)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.toggleRespiratoryFactor(factor.id) }
            .padding(vertical = 4.dp)
        ) {
          Checkbox(
            checked = isChecked,
            onCheckedChange = { viewModel.toggleRespiratoryFactor(factor.id) },
            colors = CheckboxDefaults.colors(
              checkedColor = MaterialTheme.colorScheme.primary
            )
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isAr) factor.labelAr else factor.labelEn,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (rankedResults.isEmpty()) {
        ClinicalAlertBox(
          severity = MessageSeverity.INFO,
          title = if (isAr) "في انتظار المعطيات" else "Select Indicators",
          body = if (isAr)
            "اختر علامة سريرية أو أكثر من الأعلى لترجيح التشخيص وعرض خطة التدخل المناسبة."
          else
            "Check one or more clinical findings above to calculate probability and management protocol."
        )
      } else {
        Text(
          text = if (isAr) "التشخيصات المرجحة مرتبة حسب الأولوية:" else "Ranked Differential Diagnoses:",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        rankedResults.forEachIndexed { index, (diag, score) ->
          val isTop = index == 0
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (isTop) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
              )
              .border(
                width = if (isTop) 1.5.dp else 0.5.dp,
                color = if (isTop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
              )
              .padding(12.dp)
          ) {
            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (isTop) {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text(
                        text = if (isAr) "الأرجح" else "Most Likely",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                  }
                  Text(
                    text = if (isAr) diag.nameAr else diag.nameEn,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }

                Text(
                  text = if (isAr) "نقاط: $score" else "Score: $score",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold
                )
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = if (isAr) diag.managementAr else diag.managementEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))
        ClinicalAlertBox(
          severity = MessageSeverity.INFO,
          title = if (isAr) "قاعدة الفحص والتشخيص" else "Diagnostic Gold Rule",
          body = if (isAr)
            "تأكد دائماً بصورة أشعة الصدر وغازات الدم الشرياني (ABG). للتفريق بين الأسباب الرئوية والعيوب القلبية الخلقية: أعطِ أكسجين 100% لمدة 5–10 دقائق (اختبار Hyperoxia) وراقب استجابة PaO2."
          else
            "Always corroborate with chest radiography and arterial blood gases. To differentiate pulmonary vs cyanotic cardiac defects, perform 100% hyperoxia test for 5–10 minutes."
        )
      }
    }

    // 2. Respiratory Support Protocol Card
    ClinicalCard(
      title = if (isAr) "بروتوكول الدعم التنفسي (Respiratory Protocols)" else "Respiratory Support Protocols",
      icon = Icons.Default.MedicalServices
    ) {
      // CPAP protocol
      ClinicalAlertBox(
        severity = MessageSeverity.INFO,
        title = if (isAr) "1. ضغط مجرى التنفس الإيجابي المستمر (Nasal CPAP)" else "1. Early Nasal CPAP",
        body = if (isAr)
          "ابدأ CPAP مبكراً لو FiO2 < 0.4 و PaCO2 < 55–60 mmHg مع تنفس تلقائي. ابدأ PEEP بـ 5–6 سم ماء وزِد تدريجياً حتى 7–8 cmH2O لتحقيق سعة متبقية وظيفية جيدة وتجنب الانخماص."
        else
          "Early CPAP indicated when FiO2 < 0.4 and PaCO2 < 55–60 mmHg with spontaneous effort. Start PEEP at 5–6 cmH2O and titrate up to 7–8 cmH2O to recruit lung volume and prevent atelectasis."
      )

      // Mechanical Ventilation
      ClinicalAlertBox(
        severity = MessageSeverity.BAD,
        title = if (isAr) "2. دواعي التهوية الميكانيكية التداخلية (Intubation Triggers)" else "2. Mechanical Ventilation Criteria",
        body = if (isAr)
          "يستدعي التنبيب والتهوية الميكانيكية: PaCO2 > 60 mmHg مع حموضة pH < 7.25، أو PaO2 < 50 mmHg أو SpO2 < 90% مع FiO2 > 0.5 على الـ CPAP، أو انقطاع نفس شديد متكرر غير مستجيب للكافيين."
        else
          "Intubation triggers: PaCO2 > 60 mmHg with pH < 7.25, PaO2 < 50 mmHg or SpO2 < 90% at FiO2 > 0.5 on CPAP, or severe recurrent apneas refractory to caffeine."
      )

      // Pneumothorax & DOPE
      ClinicalAlertBox(
        severity = MessageSeverity.WARN,
        title = if (isAr) "3. الاشتباه في استرواح الصدر وخوارزمية DOPE" else "3. Pneumothorax & DOPE Algorithm",
        body = if (isAr)
          "عند تدهور طفل موصول بجهاز تنفس فجأة، فكّر في DOPE:\n• D: Displacement (إزاحة الأنبوب)\n• O: Obstruction (انسداد الأنبوب بالمخاط)\n• P: Pneumothorax (استرواح الصدر - افحص بالضوء Transillumination)\n• E: Equipment (عطل الجهاز أو الدائرة).\nفي الاسترواح المتوتر: بزل فوري بالإبرة في الفراغ الضلعي الثاني خط منتصف الترقوة ثم تركيب أنبوبة صدرية."
        else
          "In acute decompensation of an intubated neonate, execute DOPE:\n• D: Tube displacement\n• O: Tube obstruction with secretions\n• P: Pneumothorax (transillumination)\n• E: Equipment failure.\nTension pneumothorax: emergent needle thoracostomy 2nd intercostal space midclavicular line then chest tube."
      )

      // TTN
      ClinicalAlertBox(
        severity = MessageSeverity.OK,
        title = if (isAr) "4. سرعة التنفس العابرة (TTN)" else "4. Transient Tachypnea (TTN)",
        body = if (isAr)
          "TTN هو تشخيص استبعاد (Diagnosis of Exclusion). يتحسن ذاتياً خلال 12–24 ساعة وحتى 72 ساعة بامتصاص سوائل الرئة. العلاج داعم فقط دون حاجة للتهوية التداخلية عادة، وتقييم المضادات الحيوية حسب عوامل خطورة العدوى."
        else
          "TTN is a benign diagnosis of exclusion resolving within 12–72h as lung fluid clears. Supportive care only; antibiotics guided by maternal/neonatal sepsis risk factors."
      )
    }
  }
}
