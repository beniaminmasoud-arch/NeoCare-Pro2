package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BabyEntity
import com.example.data.model.BindInputs
import com.example.data.model.ClinicalMessage
import com.example.data.model.DiagnosisDetails
import com.example.data.model.DrugCalculator
import com.example.data.model.DrugDoseItem
import com.example.data.model.FluidsCalculator
import com.example.data.model.FluidsResult
import com.example.data.model.JaundiceAnalysisResult
import com.example.data.model.JaundiceCalculator
import com.example.data.model.JaundiceInputs
import com.example.data.model.MessageSeverity
import com.example.data.model.RespiratoryEngine
import com.example.data.model.WeightPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val babyDao = AppDatabase.getDatabase(application).babyDao()

  val savedBabies: StateFlow<List<BabyEntity>> = babyDao.getAllBabies()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // App Settings
  private val _isArabic = MutableStateFlow(true)
  val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

  private val _selectedTab = MutableStateFlow(0)
  val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

  // Active Baby Vitals
  private val _weightKg = MutableStateFlow(3.2)
  val weightKg: StateFlow<Double> = _weightKg.asStateFlow()

  private val _birthWeightG = MutableStateFlow(3200.0)
  val birthWeightG: StateFlow<Double> = _birthWeightG.asStateFlow()

  private val _gestationalAgeWeeks = MutableStateFlow(38)
  val gestationalAgeWeeks: StateFlow<Int> = _gestationalAgeWeeks.asStateFlow()

  private val _ageHours = MutableStateFlow(48)
  val ageHours: StateFlow<Int> = _ageHours.asStateFlow()

  private val _isUnderWarmerOrPhoto = MutableStateFlow(false)
  val isUnderWarmerOrPhoto: StateFlow<Boolean> = _isUnderWarmerOrPhoto.asStateFlow()

  private val _patientLabel = MutableStateFlow("حاضنة 1 / سرير A")
  val patientLabel: StateFlow<String> = _patientLabel.asStateFlow()

  private val _activeBabyId = MutableStateFlow<Long?>(null)
  val activeBabyId: StateFlow<Long?> = _activeBabyId.asStateFlow()

  // Jaundice Inputs
  val tsbText = MutableStateFlow("14.5")
  val dsbText = MutableStateFlow("0.8")
  val prevTsbText = MutableStateFlow("")
  val hoursBetweenText = MutableStateFlow("")
  val albuminText = MutableStateFlow("3.4")
  val isDatPositive = MutableStateFlow(false)
  val photoThresholdText = MutableStateFlow("15.0")
  val exchangeThresholdText = MutableStateFlow("20.0")

  // Neurotoxicity Risk Factors
  val rfGaLessThan38 = MutableStateFlow(false)
  val rfAlbuminLessThan3 = MutableStateFlow(false)
  val rfHemolyticDisease = MutableStateFlow(false)
  val rfSepsis = MutableStateFlow(false)
  val rfClinicalInstability = MutableStateFlow(false)

  // BIND Score Inputs
  val bindMentalStatus = MutableStateFlow(0)
  val bindMuscleTone = MutableStateFlow(0)
  val bindCry = MutableStateFlow(0)

  // Respiratory Inputs (Set of checked factor IDs)
  val selectedRespiratoryFactors = MutableStateFlow<Set<String>>(emptySet())

  // Fluids Inputs
  val dextrosePercentText = MutableStateFlow("10.0")
  val reverseRateText = MutableStateFlow("13.3")
  val reverseDexText = MutableStateFlow("10.0")

  // ==================== Actions ====================

  fun toggleLanguage() {
    _isArabic.value = !_isArabic.value
  }

  fun setTab(index: Int) {
    _selectedTab.value = index
  }

  fun updateWeight(weight: Double) {
    if (weight > 0) {
      _weightKg.value = weight
      // If birth weight was closely aligned, update it proportionally or keep in sync
      if (_birthWeightG.value == 0.0 || Math.abs(_birthWeightG.value - _weightKg.value * 1000.0) < 100) {
        _birthWeightG.value = weight * 1000.0
      }
    }
  }

  fun updateBirthWeight(grams: Double) {
    if (grams > 0) {
      _birthWeightG.value = grams
    }
  }

  fun updateGestationalAge(weeks: Int) {
    if (weeks in 22..44) {
      _gestationalAgeWeeks.value = weeks
      // Auto-update GA < 38 risk factor
      rfGaLessThan38.value = (weeks < 38)
    }
  }

  fun updateAgeHours(hours: Int) {
    if (hours >= 0) {
      _ageHours.value = hours
    }
  }

  fun toggleWarmerOrPhoto(enabled: Boolean) {
    _isUnderWarmerOrPhoto.value = enabled
  }

  fun applyPreset(preset: WeightPreset) {
    _weightKg.value = preset.weightKg
    _birthWeightG.value = preset.weightKg * 1000.0
    _gestationalAgeWeeks.value = preset.gaWeeks
    rfGaLessThan38.value = (preset.gaWeeks < 38)
    _patientLabel.value = if (_isArabic.value) preset.labelAr else preset.labelEn
    _activeBabyId.value = null
  }

  fun resetToDefaults() {
    _weightKg.value = 3.2
    _birthWeightG.value = 3200.0
    _gestationalAgeWeeks.value = 38
    _ageHours.value = 48
    _isUnderWarmerOrPhoto.value = false
    _patientLabel.value = "طفل جديد (Newborn)"
    _activeBabyId.value = null

    tsbText.value = "14.5"
    dsbText.value = "0.8"
    prevTsbText.value = ""
    hoursBetweenText.value = ""
    albuminText.value = "3.4"
    isDatPositive.value = false
    photoThresholdText.value = "15.0"
    exchangeThresholdText.value = "20.0"

    rfGaLessThan38.value = false
    rfAlbuminLessThan3.value = false
    rfHemolyticDisease.value = false
    rfSepsis.value = false
    rfClinicalInstability.value = false

    bindMentalStatus.value = 0
    bindMuscleTone.value = 0
    bindCry.value = 0

    selectedRespiratoryFactors.value = emptySet()
    dextrosePercentText.value = "10.0"
  }

  // ==================== Calculated Results ====================

  fun getJaundiceAnalysis(): JaundiceAnalysisResult {
    val tsb = tsbText.value.toDoubleOrNull()
    val dsb = dsbText.value.toDoubleOrNull()
    val prevTsb = prevTsbText.value.toDoubleOrNull()
    val hours = hoursBetweenText.value.toDoubleOrNull()
    val alb = albuminText.value.toDoubleOrNull()
    val pt = photoThresholdText.value.toDoubleOrNull()
    val et = exchangeThresholdText.value.toDoubleOrNull()

    val inputs = JaundiceInputs(
      tsb = tsb,
      dsb = dsb,
      previousTsb = prevTsb,
      hoursBetweenMeasurements = hours,
      albumin = alb,
      isDatPositive = isDatPositive.value,
      phototherapyThreshold = pt,
      exchangeTransfusionThreshold = et,
      rfGaLessThan38 = rfGaLessThan38.value || (_gestationalAgeWeeks.value < 38),
      rfAlbuminLessThan3 = rfAlbuminLessThan3.value || (alb != null && alb < 3.0),
      rfHemolyticDisease = rfHemolyticDisease.value || isDatPositive.value,
      rfSepsis = rfSepsis.value,
      rfClinicalInstability = rfClinicalInstability.value
    )

    return JaundiceCalculator.analyze(
      inputs = inputs,
      weightKg = _weightKg.value,
      gaWeeks = _gestationalAgeWeeks.value,
      ageHours = _ageHours.value
    )
  }

  fun getBindScore(): Int {
    return bindMentalStatus.value + bindMuscleTone.value + bindCry.value
  }

  fun getBindInterpretation(isAr: Boolean): Pair<String, MessageSeverity> {
    val s = getBindScore()
    return when {
      s >= 7 -> Pair(
        if (isAr) "درجة $s: اعتلال دماغي حاد متقدم (Advanced ABE). تدخل إسعافي عاجل لتبديل الدم لمنع الكرنكترك وموت الخلايا العصبية."
        else "Score $s: Advanced ABE. Urgent exchange transfusion and intensive phototherapy indicated to prevent irreversible kernicterus.",
        MessageSeverity.BAD
      )
      s >= 4 -> Pair(
        if (isAr) "درجة $s: اعتلال دماغي حاد متوسط (Moderate ABE). تدخل عاجل قادر على عكس الأثر السمّي للبيليروبين."
        else "Score $s: Moderate ABE. Urgent intervention can reverse acute neurotoxicity.",
        MessageSeverity.BAD
      )
      s >= 1 -> Pair(
        if (isAr) "درجة $s: اعتلال دماغي خفيف (Mild ABE). علامات سمّية خفيفة، يجب علاج البيليروبين بحزم وإعادة التقييم باستمرار."
        else "Score $s: Mild ABE. Subtle signs of bilirubin encephalopathy. Aggressive phototherapy and frequent reassessment.",
        MessageSeverity.WARN
      )
      else -> Pair(
        if (isAr) "درجة 0: فحص عصبي سليم وطبيعي."
        else "Score 0: Normal neurological examination.",
        MessageSeverity.OK
      )
    }
  }

  fun toggleRespiratoryFactor(id: String) {
    val current = selectedRespiratoryFactors.value.toMutableSet()
    if (current.contains(id)) {
      current.remove(id)
    } else {
      current.add(id)
    }
    selectedRespiratoryFactors.value = current
  }

  fun getRespiratoryRanked(): List<Pair<DiagnosisDetails, Int>> {
    return RespiratoryEngine.rankDiagnoses(selectedRespiratoryFactors.value)
  }

  fun getFluidsResult(): FluidsResult {
    val dex = dextrosePercentText.value.toDoubleOrNull() ?: 10.0
    return FluidsCalculator.calculate(
      birthWeightG = _birthWeightG.value,
      currentWeightKg = _weightKg.value,
      ageHours = _ageHours.value,
      dextrosePercent = dex,
      isUnderPhototherapyOrWarmer = _isUnderWarmerOrPhoto.value
    )
  }

  fun getReverseGirValue(): Double {
    val rate = reverseRateText.value.toDoubleOrNull() ?: 0.0
    val dex = reverseDexText.value.toDoubleOrNull() ?: 10.0
    return FluidsCalculator.reverseGir(rate, dex, _weightKg.value)
  }

  fun getNrpEmergencyDoses(): List<DrugDoseItem> {
    return DrugCalculator.getNrpEmergencyDoses(_weightKg.value)
  }

  fun getRespiratoryAndNicDoses(): List<DrugDoseItem> {
    return DrugCalculator.getRespiratoryAndNicDoses(_weightKg.value)
  }

  fun getEttSpecifications(): DrugCalculator.EttSpecifications {
    return DrugCalculator.getEttSpecs(_weightKg.value)
  }

  // ==================== Saved Patients (Room) ====================

  fun saveCurrentPatient(label: String, notes: String = "") {
    viewModelScope.launch {
      val entity = BabyEntity(
        id = _activeBabyId.value ?: 0,
        nameOrBed = label.ifBlank { "Baby ${_weightKg.value}kg" },
        weightKg = _weightKg.value,
        birthWeightG = _birthWeightG.value,
        gestationalAgeWeeks = _gestationalAgeWeeks.value,
        ageHours = _ageHours.value,
        isUnderWarmerOrPhoto = _isUnderWarmerOrPhoto.value,
        tsb = tsbText.value.toDoubleOrNull(),
        dsb = dsbText.value.toDoubleOrNull(),
        dextrosePercent = dextrosePercentText.value.toDoubleOrNull() ?: 10.0,
        notes = notes,
        updatedAtMillis = System.currentTimeMillis()
      )
      val newId = babyDao.insertBaby(entity)
      _activeBabyId.value = if (entity.id == 0L) newId else entity.id
      _patientLabel.value = entity.nameOrBed
    }
  }

  fun loadPatient(baby: BabyEntity) {
    _activeBabyId.value = baby.id
    _patientLabel.value = baby.nameOrBed
    _weightKg.value = baby.weightKg
    _birthWeightG.value = baby.birthWeightG
    _gestationalAgeWeeks.value = baby.gestationalAgeWeeks
    _ageHours.value = baby.ageHours
    _isUnderWarmerOrPhoto.value = baby.isUnderWarmerOrPhoto
    baby.tsb?.let { tsbText.value = it.toString() }
    baby.dsb?.let { dsbText.value = it.toString() }
    dextrosePercentText.value = baby.dextrosePercent.toString()
    rfGaLessThan38.value = (baby.gestationalAgeWeeks < 38)
  }

  fun deletePatient(baby: BabyEntity) {
    viewModelScope.launch {
      babyDao.deleteBaby(baby)
      if (_activeBabyId.value == baby.id) {
        _activeBabyId.value = null
        _patientLabel.value = "طفل جديد"
      }
    }
  }

  // ==================== Clinical Summary Export ====================

  fun generateClinicalSummary(): String {
    val isAr = _isArabic.value
    val wt = _weightKg.value
    val ga = _gestationalAgeWeeks.value
    val age = _ageHours.value
    val ett = getEttSpecifications()
    val fluids = getFluidsResult()
    val jaun = getJaundiceAnalysis()
    val bind = getBindScore()

    val sb = StringBuilder()
    if (isAr) {
      sb.append("📋 تقرير سريري - تطبيق Neo Care Pro\n")
      sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
      sb.append("المريض: ${_patientLabel.value}\n")
      sb.append("• الوزن الحالي: $wt كجم (وزن الميلاد: ${_birthWeightG.value.toInt()} جم)\n")
      sb.append("• عمر الحمل: $ga أسبوع | العمر: $age ساعة\n")
      sb.append("• العناية الإضافية: ${if (_isUnderWarmerOrPhoto.value) "تحت ضوء / مدفأة (+20 مل/كجم)" else "عادية"}\n\n")

      sb.append("🟡 تقييم الصفراء (AAP 2022):\n")
      sb.append("• TSB: ${tsbText.value} mg/dL | DSB: ${dsbText.value} mg/dL\n")
      jaun.diffFromPhoto?.let { sb.append("• بعد TSB عن عتبة الضوء: ${String.format(Locale.US, "%.1f", it)} mg/dL\n") }
      sb.append("• مؤشر BIND العصبي: $bind/9\n\n")

      sb.append("💧 السوائل والـ GIR (Table 15-3):\n")
      sb.append("• مدى السوائل: ${fluids.minMlPerKgDay.toInt()}–${fluids.maxMlPerKgDay.toInt()} مل/كجم/يوم (${fluids.totalMlPerDayMin.toInt()}–${fluids.totalMlPerDayMax.toInt()} مل/يوم)\n")
      sb.append("• المعدل عند المتوسط: ${String.format(Locale.US, "%.1f", fluids.hourlyRateMidpoint)} مل/ساعة\n")
      sb.append("• معدل تسريب الجلوكوز GIR: ${String.format(Locale.US, "%.1f", fluids.gir)} مجم/كجم/دقيقة (سكر ${dextrosePercentText.value}%)\n\n")

      sb.append("🫁 الأنبوب الحنجري (ETT) والإنعاش:\n")
      sb.append("• قطر الأنبوب: ${ett.internalDiameterMm} | العمق عند الشفة: ${String.format(Locale.US, "%.1f", ett.lipDepthCm)} سم\n")
      sb.append("• شفرة المنظار: ${ett.bladeSize}\n")
      sb.append("• أدرينالين وريد 1:10,000: ${String.format(Locale.US, "%.2f", 0.1 * wt)}–${String.format(Locale.US, "%.2f", 0.3 * wt)} مل\n")
      sb.append("• محلول تعويض الحجم: ${String.format(Locale.US, "%.1f", 10.0 * wt)} مل\n")
      sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
      sb.append("المصدر: AAP 2022, Neonatal Resuscitation Program (NRP). أداة مساعدة للطبيب.")
    } else {
      sb.append("📋 CLINICAL ASSESSMENT - Neo Care Pro\n")
      sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
      sb.append("Patient: ${_patientLabel.value}\n")
      sb.append("• Current Weight: $wt kg (Birth Weight: ${_birthWeightG.value.toInt()} g)\n")
      sb.append("• Gestational Age: $ga weeks | Chronological Age: $age hours\n")
      sb.append("• Warmer/Phototherapy: ${if (_isUnderWarmerOrPhoto.value) "Yes (+20 ml/kg/day)" else "No"}\n\n")

      sb.append("🟡 Jaundice Evaluation (AAP 2022):\n")
      sb.append("• TSB: ${tsbText.value} mg/dL | DSB: ${dsbText.value} mg/dL\n")
      jaun.diffFromPhoto?.let { sb.append("• Distance to phototherapy threshold: ${String.format(Locale.US, "%.1f", it)} mg/dL\n") }
      sb.append("• BIND Neurological Score: $bind/9\n\n")

      sb.append("💧 Fluids & GIR (Table 15-3):\n")
      sb.append("• Requirement: ${fluids.minMlPerKgDay.toInt()}–${fluids.maxMlPerKgDay.toInt()} ml/kg/day (${fluids.totalMlPerDayMin.toInt()}–${fluids.totalMlPerDayMax.toInt()} ml/day)\n")
      sb.append("• Midpoint infusion rate: ${String.format(Locale.US, "%.1f", fluids.hourlyRateMidpoint)} ml/hr\n")
      sb.append("• GIR: ${String.format(Locale.US, "%.1f", fluids.gir)} mg/kg/min (D${dextrosePercentText.value}%)\n\n")

      sb.append("🫁 ETT Intubation & NRP Emergency:\n")
      sb.append("• ETT Size: ${ett.internalDiameterMm} | Lip Depth: ${String.format(Locale.US, "%.1f", ett.lipDepthCm)} cm\n")
      sb.append("• Laryngoscope Blade: ${ett.bladeSize}\n")
      sb.append("• Epinephrine 1:10,000 IV: ${String.format(Locale.US, "%.2f", 0.1 * wt)}–${String.format(Locale.US, "%.2f", 0.3 * wt)} ml\n")
      sb.append("• Volume expansion: ${String.format(Locale.US, "%.1f", 10.0 * wt)} ml\n")
      sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
      sb.append("Reference: AAP 2022, NRP. Decision support for healthcare professionals.")
    }
    return sb.toString()
  }
}
