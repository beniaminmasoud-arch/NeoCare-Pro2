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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JaundiceCalculator
import com.example.data.model.MessageSeverity
import com.example.ui.MainViewModel
import com.example.ui.components.ClinicalAlertBox
import com.example.ui.components.ClinicalCard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JaundiceScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val isAr by viewModel.isArabic.collectAsState()
  val weightKg by viewModel.weightKg.collectAsState()
  val gaWeeks by viewModel.gestationalAgeWeeks.collectAsState()
  val ageHours by viewModel.ageHours.collectAsState()

  val tsbText by viewModel.tsbText.collectAsState()
  val dsbText by viewModel.dsbText.collectAsState()
  val prevTsbText by viewModel.prevTsbText.collectAsState()
  val hoursBetweenText by viewModel.hoursBetweenText.collectAsState()
  val albuminText by viewModel.albuminText.collectAsState()
  val isDatPositive by viewModel.isDatPositive.collectAsState()
  val photoThresholdText by viewModel.photoThresholdText.collectAsState()
  val exchangeThresholdText by viewModel.exchangeThresholdText.collectAsState()

  val rfGaLessThan38 by viewModel.rfGaLessThan38.collectAsState()
  val rfAlbuminLessThan3 by viewModel.rfAlbuminLessThan3.collectAsState()
  val rfHemolyticDisease by viewModel.rfHemolyticDisease.collectAsState()
  val rfSepsis by viewModel.rfSepsis.collectAsState()
  val rfClinicalInstability by viewModel.rfClinicalInstability.collectAsState()

  val bindMental by viewModel.bindMentalStatus.collectAsState()
  val bindTone by viewModel.bindMuscleTone.collectAsState()
  val bindCry by viewModel.bindCry.collectAsState()

  val analysis = viewModel.getJaundiceAnalysis()
  val (bindInterpretation, bindSeverity) = viewModel.getBindInterpretation(isAr)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(bottom = 80.dp)
  ) {
    // 1. AAP 2022 Main Card
    ClinicalCard(
      title = if (isAr) "الصفراء الوليدية – AAP 2022" else "Neonatal Hyperbilirubinemia (AAP 2022)",
      icon = Icons.Default.WbSunny,
      testTag = "jaundice_main_card"
    ) {
      // 2-Column Grid for TSB and DSB
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = tsbText,
          onValueChange = { viewModel.tsbText.value = it },
          label = { Text("TSB (mg/dL)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier
            .weight(1f)
            .testTag("tsb_input")
        )
        OutlinedTextField(
          value = dsbText,
          onValueChange = { viewModel.dsbText.value = it },
          label = { Text("DSB (mg/dL)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier
            .weight(1f)
            .testTag("dsb_input")
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Previous TSB & Hours between measurements
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = prevTsbText,
          onValueChange = { viewModel.prevTsbText.value = it },
          label = { Text(if (isAr) "TSB السابق (mg/dL)" else "Previous TSB") },
          placeholder = { Text("اختياري") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
          value = hoursBetweenText,
          onValueChange = { viewModel.hoursBetweenText.value = it },
          label = { Text(if (isAr) "الساعات بين القياسين" else "Hours Elapsed") },
          placeholder = { Text("ساعة") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Albumin & DAT
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = albuminText,
          onValueChange = { viewModel.albuminText.value = it },
          label = { Text("Albumin (g/dL)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.weight(1f)
        )

        // DAT Switch/Toggle
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { viewModel.isDatPositive.value = !isDatPositive }
            .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column {
              Text(
                text = "DAT / Coombs",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (isDatPositive) (if (isAr) "موجب (+)" else "Positive (+)")
                else (if (isAr) "سلبي / غير معروف" else "Negative / Unknown"),
                style = MaterialTheme.typography.labelSmall,
                color = if (isDatPositive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = isDatPositive,
              onCheckedChange = { viewModel.isDatPositive.value = it },
              modifier = Modifier.testTag("dat_switch")
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Phototherapy & Exchange Thresholds
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = photoThresholdText,
          onValueChange = { viewModel.photoThresholdText.value = it },
          label = { Text(if (isAr) "عتبة الضوء (من الشارت)" else "Photo Threshold") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier
            .weight(1f)
            .testTag("photo_threshold_input")
        )
        OutlinedTextField(
          value = exchangeThresholdText,
          onValueChange = { viewModel.exchangeThresholdText.value = it },
          label = { Text(if (isAr) "عتبة التبديل (من الشارت)" else "Exchange Threshold") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier
            .weight(1f)
            .testTag("exchange_threshold_input")
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = if (isAr)
          "ℹ️ العتبات في AAP 2022 تتغير بالساعة وعمر الحمل وعوامل الخطر. ادخلها من المنحنى أو BiliTool ويقوم التطبيق بحساب باقي القرار الإكلينيكي فوراً."
        else
          "ℹ️ AAP 2022 thresholds vary hour-by-hour by GA and risk factors. Enter from nomogram/BiliTool to evaluate escalation and decisions.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(10.dp))

      // Neurotoxicity Risk Factors
      Text(
        text = if (isAr) "عوامل خطر سمّية الدماغ (Hyperbilirubinemia Neurotoxicity Risk Factors):"
        else "Hyperbilirubinemia Neurotoxicity Risk Factors:",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
      )

      RiskCheckboxItem(
        label = if (isAr) "عمر الحمل < 38 أسبوع (Gestational Age < 38 w)" else "Gestational age < 38 weeks",
        checked = rfGaLessThan38,
        onCheckedChange = { viewModel.rfGaLessThan38.value = it }
      )
      RiskCheckboxItem(
        label = if (isAr) "الألبومين < 3.0 g/dL (Albumin < 3.0)" else "Albumin < 3.0 g/dL",
        checked = rfAlbuminLessThan3,
        onCheckedChange = { viewModel.rfAlbuminLessThan3.value = it }
      )
      RiskCheckboxItem(
        label = if (isAr) "مرض انحلال دم نشط (DAT موجب / G6PD / تكور كرات الدم)" else "Hemolytic disease (DAT+, G6PD, spherocytosis)",
        checked = rfHemolyticDisease,
        onCheckedChange = { viewModel.rfHemolyticDisease.value = it }
      )
      RiskCheckboxItem(
        label = if (isAr) "تعفن الدم (Clinical Sepsis)" else "Clinical sepsis",
        checked = rfSepsis,
        onCheckedChange = { viewModel.rfSepsis.value = it }
      )
      RiskCheckboxItem(
        label = if (isAr) "عدم استقرار سريري ملحوظ في آخر 24 ساعة" else "Clinical instability in last 24h",
        checked = rfClinicalInstability,
        onCheckedChange = { viewModel.rfClinicalInstability.value = it }
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Results Output
      analysis.messages.forEach { msg ->
        ClinicalAlertBox(message = msg)
      }
    }

    // 2. BIND Score Card
    ClinicalCard(
      title = if (isAr) "مؤشر BIND Score (اعتلال الدماغ الحاد بالصفراء)" else "BIND Score (Acute Bilirubin Encephalopathy)",
      icon = Icons.Default.Psychology,
      testTag = "bind_card"
    ) {
      Text(
        text = if (isAr)
          "تقييم الاعتلال العصبي الحاد بالبيليروبين (Bilirubin-Induced Neurological Dysfunction). كل فئة من 0 إلى 3 درجات:"
        else
          "Evaluates Acute Bilirubin Encephalopathy (0–3 points per clinical category):",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 1. Mental Status
      BindSelector(
        title = if (isAr) "1. الحالة العقلية (Mental Status)" else "1. Mental Status",
        options = if (isAr) listOf("0: طبيعي", "1: نعسان ويفيق بالتنبيه", "2: خمول / متهيج", "3: شبه غيبوبة / تشنجات")
        else listOf("0: Normal", "1: Sleepy but rouses", "2: Lethargic / Irritable", "3: Semicoma / Seizures"),
        selectedIndex = bindMental,
        onSelect = { viewModel.bindMentalStatus.value = it }
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 2. Muscle Tone
      BindSelector(
        title = if (isAr) "2. التوتر العضلي (Muscle Tone)" else "2. Muscle Tone",
        options = if (isAr) listOf("0: طبيعي", "1: نقص توتر عضلي مستمر", "2: فرط توتر متناوب مع رخاوة", "3: تشنج ظهري (Opisthotonus)")
        else listOf("0: Normal", "1: Persistent hypotonia", "2: Alternating hyper/hypotonia", "3: Opisthotonus / Retrocollis"),
        selectedIndex = bindTone,
        onSelect = { viewModel.bindMuscleTone.value = it }
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Cry
      BindSelector(
        title = if (isAr) "3. الصراخ والبكاء (Cry Characteristics)" else "3. Cry Characteristics",
        options = if (isAr) listOf("0: طبيعي", "1: صراخ حاد النبرة", "2: صراخ حاد مستمر (Shrill)", "3: بكاء ضعيف أو غائب / غير مهدأ")
        else listOf("0: Normal", "1: High pitched", "2: Shrill / Inconsolable", "3: Weak or absent / Semicomatose"),
        selectedIndex = bindCry,
        onSelect = { viewModel.bindCry.value = it }
      )

      Spacer(modifier = Modifier.height(10.dp))

      // BIND Interpretation Alert
      ClinicalAlertBox(
        severity = bindSeverity,
        title = if (isAr) "النتيجة: BIND = ${viewModel.getBindScore()} / 9" else "Result: BIND = ${viewModel.getBindScore()} / 9",
        body = bindInterpretation
      )
    }

    // 3. Preterm Infants Table (<35 weeks Maisels 2012)
    val pretermThresholds = JaundiceCalculator.getMaiselsPretermThresholds(gaWeeks)
    ClinicalCard(
      title = if (isAr) "عتبات الخدج < 35 أسبوع (Maisels 2012)" else "Preterm Guidelines < 35 Weeks (Maisels 2012)",
      icon = Icons.Default.TableChart
    ) {
      if (pretermThresholds != null) {
        ClinicalAlertBox(
          severity = MessageSeverity.INFO,
          title = if (isAr) "توصية خاصة لعمر حمل $gaWeeks أسبوع:" else "Recommendation for GA $gaWeeks weeks:",
          body = if (isAr)
            "بدء العلاج الضوئي عند TSB = ${pretermThresholds.first} mg/dL | تبديل الدم عند TSB = ${pretermThresholds.second} mg/dL."
          else
            "Initiate phototherapy at TSB = ${pretermThresholds.first} mg/dL | Exchange transfusion at TSB = ${pretermThresholds.second} mg/dL."
        )
        Spacer(modifier = Modifier.height(8.dp))
      }

      // Preterm reference table
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
            .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
          Text(
            text = "GA (Weeks)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
          )
          Text(
            text = if (isAr) "بدء الضوء (mg/dL)" else "Phototherapy",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
          )
          Text(
            text = if (isAr) "تبديل الدم (mg/dL)" else "Exchange",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
          )
        }

        val rows = listOf(
          Triple("< 28", "5 – 6", "11 – 14"),
          Triple("28 – 29+6", "6 – 8", "12 – 14"),
          Triple("30 – 31+6", "8 – 10", "13 – 16"),
          Triple("32 – 33+6", "10 – 12", "15 – 18"),
          Triple("34 – 34+6", "12 – 14", "17 – 19")
        )

        rows.forEachIndexed { idx, row ->
          val isCurrentGaRow = when (idx) {
            0 -> gaWeeks < 28
            1 -> gaWeeks in 28..29
            2 -> gaWeeks in 30..31
            3 -> gaWeeks in 32..33
            4 -> gaWeeks == 34
            else -> false
          }
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                if (isCurrentGaRow) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else if (idx % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
              )
              .padding(horizontal = 8.dp, vertical = 6.dp)
          ) {
            Text(
              text = row.first + if (isCurrentGaRow) " 👈" else "",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = if (isCurrentGaRow) FontWeight.Bold else FontWeight.Normal,
              color = if (isCurrentGaRow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f)
            )
            Text(
              text = row.second,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = if (isCurrentGaRow) FontWeight.Bold else FontWeight.Normal,
              modifier = Modifier.weight(1f)
            )
            Text(
              text = row.third,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = if (isCurrentGaRow) FontWeight.Bold else FontWeight.Normal,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // 4. Causes by Day of Life Card
    ClinicalCard(
      title = if (isAr) "أسباب الصفراء حسب يوم العمر (Differential Diagnosis)" else "Causes of Jaundice by Day of Life",
      icon = Icons.Default.Info
    ) {
      val causes = if (isAr) listOf(
        Pair("اليوم الأول (< 24 ساعة) [مرضية دائماً]", "انحلال دم (ABO, Rh, Minor antigens)، عدوى داخل الرحم TORCH، تعفن دم Sepsis، نقص إنزيم G6PD الحاد."),
        Pair("اليوم 2 – 3", "صفراء فسيولوجية، تعفن دم، كثرة كرات الدم الحمراء (Polycythemia)، نزيف داخلي مخفي (Cephalohematoma)."),
        Pair("اليوم 3 – 7", "تعفن دم، نقص G6PD، صفراء نقص الرضاعة الطبيعية (Breastfeeding jaundice)، صفراء فسيولوجية متأخرة."),
        Pair("أكثر من 14 يوم (21 للخدج) [مطولة]", "صفراء حليب الأم (Breast milk jaundice)، قصور الغدة الدرقية، متلازمة داون، متلازمات جيلبرت/كريجلر نجار، انسداد بواب المعدة، ركود صفراوي (افحص DSB دائماً).")
      ) else listOf(
        Pair("Day 1 (< 24h) [Always Pathologic]", "Hemolytic disease (Rh, ABO, minors), TORCH infection, neonatal sepsis, severe G6PD deficiency."),
        Pair("Days 2 – 3", "Physiologic jaundice, sepsis, polycythemia, internal hemorrhage (cephalohematoma/subgaleal)."),
        Pair("Days 3 – 7", "Sepsis, G6PD deficiency, breastfeeding jaundice (inadequate intake), resolving physiologic."),
        Pair("> 14 days (> 21 for preterm) [Prolonged]", "Breast milk jaundice, congenital hypothyroidism, Down syndrome, Gilbert/Crigler-Najjar, pyloric stenosis, cholestasis (always measure conjugated/direct bilirubin).")
      )

      causes.forEach { (timeframe, etiologies) ->
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ) {
          Text(
            text = timeframe,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = etiologies,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        }
      }

      Spacer(modifier = Modifier.height(4.dp))
      ClinicalAlertBox(
        severity = MessageSeverity.WARN,
        title = if (isAr) "توجيه هام بخصوص البيليروبين المباشر (DSB)" else "Clinical Caveat Regarding Direct Bilirubin",
        body = if (isAr)
          "لا تطرح البيليروبين المباشر (DSB) من الكلي (TSB) عند استخدام المنحنيات. افحص DSB دائماً لو استمرت الصفراء أكثر من 14 يوماً لكامل المدة أو 21 يوماً للخديج لاستبعاد رتق القناة الصفراوية (Biliary Atresia) مبكراً."
        else
          "Never subtract DSB from TSB when consulting guidelines. Always measure conjugated bilirubin if jaundice persists >14 days in term infants or >21 days in preterms to rule out biliary atresia."
      )
    }
  }
}

@Composable
fun RiskCheckboxItem(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCheckedChange(!checked) }
      .padding(vertical = 2.dp)
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = CheckboxDefaults.colors(
        checkedColor = MaterialTheme.colorScheme.primary
      )
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
fun BindSelector(
  title: String,
  options: List<String>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = title,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      options.forEachIndexed { index, opt ->
        val isSelected = selectedIndex == index
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(
              if (isSelected) MaterialTheme.colorScheme.primaryContainer
              else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
              width = if (isSelected) 1.5.dp else 0.5.dp,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
              shape = RoundedCornerShape(8.dp)
            )
            .clickable { onSelect(index) }
            .padding(vertical = 8.dp, horizontal = 4.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = opt,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            maxLines = 2
          )
        }
      }
    }
  }
}
