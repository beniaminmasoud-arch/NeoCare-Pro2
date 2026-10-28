package com.example.data.model

import java.util.Locale

// ==================== Jaundice Models & Calculation ====================

data class JaundiceInputs(
  val tsb: Double? = 14.5,
  val dsb: Double? = 0.8,
  val previousTsb: Double? = null,
  val hoursBetweenMeasurements: Double? = null,
  val albumin: Double? = 3.4,
  val isDatPositive: Boolean = false,
  val phototherapyThreshold: Double? = 15.0,
  val exchangeTransfusionThreshold: Double? = 20.0,
  // Neurotoxicity Risk Factors
  val rfGaLessThan38: Boolean = false,
  val rfAlbuminLessThan3: Boolean = false,
  val rfHemolyticDisease: Boolean = false,
  val rfSepsis: Boolean = false,
  val rfClinicalInstability: Boolean = false
) {
  val hasNeurotoxicityRisk: Boolean
    get() = isDatPositive || rfGaLessThan38 || rfAlbuminLessThan3 ||
        rfHemolyticDisease || rfSepsis || rfClinicalInstability
}

data class BindInputs(
  val mentalStatus: Int = 0, // 0: Normal, 1: Sleepy but rouses, 2: Lethargic/irritable, 3: Semicoma/seizures
  val muscleTone: Int = 0,   // 0: Normal, 1: Hypotonia, 2: Fluctuating tone/hypertonia, 3: Opisthotonus
  val cry: Int = 0           // 0: Normal, 1: High-pitched, 2: Shrill, 3: Inconsolable/weak/absent
) {
  val totalScore: Int get() = mentalStatus + muscleTone + cry
}

enum class MessageSeverity {
  OK, INFO, WARN, BAD
}

data class ClinicalMessage(
  val severity: MessageSeverity,
  val title: String,
  val message: String
)

data class JaundiceAnalysisResult(
  val messages: List<ClinicalMessage>,
  val rateOfRise: Double?,
  val isRateHigh: Boolean,
  val dsbFractionPercent: Double?,
  val isCholestasisSuspected: Boolean,
  val diffFromPhoto: Double?,
  val diffFromExchange: Double?,
  val isEscalationOfCare: Boolean,
  val tsbAlbuminRatio: Double?,
  val tsbAlbuminThreshold: Double?,
  val isTsbAlbuminHigh: Boolean
)

object JaundiceCalculator {

  fun analyze(
    inputs: JaundiceInputs,
    weightKg: Double,
    gaWeeks: Int,
    ageHours: Int
  ): JaundiceAnalysisResult {
    val messages = mutableListOf<ClinicalMessage>()
    val tsb = inputs.tsb
    val dsb = inputs.dsb
    val pt = inputs.phototherapyThreshold
    val et = inputs.exchangeTransfusionThreshold
    val pTsb = inputs.previousTsb
    val pHours = inputs.hoursBetweenMeasurements
    val alb = inputs.albumin
    val isDat = inputs.isDatPositive
    val hasRisk = inputs.hasNeurotoxicityRisk || (gaWeeks < 38) || (alb != null && alb < 3.0)

    if (tsb == null) {
      messages.add(
        ClinicalMessage(
          MessageSeverity.WARN,
          "تنبيه",
          "يرجى إدخال قيمة البيليروبين الكلي (TSB) لحساب التوصيات."
        )
      )
      return JaundiceAnalysisResult(
        messages = messages,
        rateOfRise = null,
        isRateHigh = false,
        dsbFractionPercent = null,
        isCholestasisSuspected = false,
        diffFromPhoto = null,
        diffFromExchange = null,
        isEscalationOfCare = false,
        tsbAlbuminRatio = null,
        tsbAlbuminThreshold = null,
        isTsbAlbuminHigh = false
      )
    }

    // 1. Age < 24 hours check
    if (ageHours < 24) {
      messages.add(
        ClinicalMessage(
          MessageSeverity.BAD,
          "صفراء مبكرة (أقل من 24 ساعة)",
          "صفراء قبل 24 ساعة من العمر تُعد مرضية دائمًا (Pathological): ابحث فورًا عن انحلال الدم (ABO/Rh)، Sepsis، TORCH، أو نقص G6PD، وقِس TSB بشكل متكرر."
        )
      )
    }

    // 2. Rate of Rise
    var rateOfRise: Double? = null
    var isRateHigh = false
    if (pTsb != null && pHours != null && pHours > 0) {
      val r = (tsb - pTsb) / pHours
      rateOfRise = r
      val limit = if (ageHours < 24) 0.3 else 0.2
      if (r >= limit) {
        isRateHigh = true
        messages.add(
          ClinicalMessage(
            MessageSeverity.BAD,
            "معدل ارتفاع سريع جداً",
            "معدل الارتفاع ${String.format(Locale.US, "%.2f", r)} mg/dL/ساعة (الحد: $limit). يوحي بقوة بوجود انحلال دم نشط (Hemolysis). يجب عمل اختبار Coombs (DAT) ومتابعة دقيقة."
          )
        )
      } else {
        messages.add(
          ClinicalMessage(
            MessageSeverity.INFO,
            "معدل الارتفاع",
            "معدل الارتفاع ${String.format(Locale.US, "%.2f", r)} mg/dL/ساعة (ضمن الحدود المقبولة)."
          )
        )
      }
    }

    // 3. Direct Bilirubin (DSB)
    var dsbFraction: Double? = null
    var isCholestasis = false
    if (dsb != null) {
      val frac = (dsb / tsb) * 100.0
      dsbFraction = frac
      if (frac > 20.0 || dsb >= 1.0) {
        isCholestasis = true
        messages.add(
          ClinicalMessage(
            MessageSeverity.WARN,
            "ارتفاع البيليروبين المباشر (DSB)",
            "DSB يمثل ${String.format(Locale.US, "%.1f", frac)}% من TSB ($dsb mg/dL). استخدم TSB كاملاً في قرارات الضوء والتبديل (لا تطرح DSB). فكّر في أسباب الركود الصفراوي (Cholestasis/Biliary Atresia/Neonatal Hepatitis)."
          )
        )
      } else {
        messages.add(
          ClinicalMessage(
            MessageSeverity.INFO,
            "البيليروبين المباشر (DSB)",
            "DSB = $dsb mg/dL (${String.format(Locale.US, "%.1f", frac)}% من TSB). استخدم TSB كاملاً في المنحنيات."
          )
        )
      }
    }

    // 4. Threshold Decisions
    var diffPhoto: Double? = null
    var diffExchange: Double? = null
    var escalation = false

    if (pt != null) {
      val dsc = pt - tsb
      diffPhoto = dsc

      if (et != null) {
        diffExchange = et - tsb
        if (tsb >= et) {
          messages.add(
            ClinicalMessage(
              MessageSeverity.BAD,
              "🚨 تبديل دم عاجل (Exchange Transfusion)",
              "TSB وصل أو تجاوز عتبة تبديل الدم ($et mg/dL)! المطلوب فوراً: بدء تبديل الدم عاجلاً + علاج ضوئي مكثف متعدد + ترطيب وريدي وفموي + تجهيز دم متوافق."
            )
          )
        } else if (tsb >= et - 2.0) {
          escalation = true
          val ivigNote = if (isDat) " • DAT إيجابي: أعطِ IVIG بمعدل 0.5–1 جم/كجم على ساعتين، ويمكن تكرارها بعد 12 ساعة." else ""
          messages.add(
            ClinicalMessage(
              MessageSeverity.BAD,
              "⚠️ تصعيد الرعاية (Escalation of Care)",
              "TSB وصل لمنطقة الخطر الشديد (ضمن 2 mg/dL من عتبة التبديل). الإجراءات الفورية: تحاليل طارئة STAT (TSB/DSB, CBC, Albumin, وظائف كلى وغازات, Type & Crossmatch) • إبلاغ بنك الدم فوراً • ضوء مكثف ثلاثي + سوائل PO/IV • قياس TSB كل ساعتين على الأكثر • تجهيز النقل لوحدة متقدمة للتبديل لو غير متوفر.$ivigNote"
            )
          )
        }
      }

      if (et == null || tsb < et - 2.0) {
        if (dsc <= 0) {
          messages.add(
            ClinicalMessage(
              MessageSeverity.WARN,
              "بدء العلاج الضوئي (Phototherapy)",
              "TSB أعلى من عتبة الضوء ($pt mg/dL) بمقدار ${String.format(Locale.US, "%.1f", -dsc)} mg/dL: ابدأ العلاج الضوئي المكثف الآن، وأعد قياس TSB خلال 4–8 ساعات."
            )
          )
        } else if (dsc < 2.0) {
          val interval = if (ageHours < 24) "4–8 ساعات مع تأجيل الخروج" else "4–24 ساعة حسب عوامل الخطر"
          messages.add(
            ClinicalMessage(
              MessageSeverity.WARN,
              "تحت عتبة الضوء بقليل",
              "TSB تحت عتبة الضوء بمقدار ${String.format(Locale.US, "%.1f", dsc)} mg/dL. يتطلب متابعة دقيقة: أعد الفحص بعد $interval."
            )
          )
        } else {
          messages.add(
            ClinicalMessage(
              MessageSeverity.OK,
              "تحت عتبة الضوء",
              "TSB أقل من عتبة الضوء بمقدار ${String.format(Locale.US, "%.1f", dsc)} mg/dL. استمر في المتابعة السريرية الروتينية حسب العمر وعوامل الخطر."
            )
          )
        }
      }

      messages.add(
        ClinicalMessage(
          MessageSeverity.INFO,
          "معايير إيقاف الضوء (AAP 2022)",
          "يمكن إيقاف العلاج الضوئي عند انخفاض TSB بمقدار لا يقل عن 2.0 mg/dL تحت عتبة بدء الضوء. يُنصح بتمديد فترة العلاج لو كان عمر الحمل < 38 أسبوع أو العمر عند البدء < 48 ساعة أو مع وجود مرض انحلالي."
        )
      )
    } else {
      messages.add(
        ClinicalMessage(
          MessageSeverity.INFO,
          "عتبة الضوء",
          "أدخل عتبة العلاج الضوئي من منحنى AAP 2022 للحصول على القرار الإكلينيكي الدقيق."
        )
      )
    }

    // 5. TSB / Albumin Ratio
    var bRatio: Double? = null
    var bThreshold: Double? = null
    var isBHigh = false
    if (alb != null && alb > 0) {
      val ratio = tsb / alb
      bRatio = ratio
      val threshold = if (gaWeeks >= 38) {
        if (hasRisk) 7.2 else 8.0
      } else {
        if (hasRisk) 6.8 else 7.2
      }
      bThreshold = threshold
      if (ratio >= threshold) {
        isBHigh = true
        messages.add(
          ClinicalMessage(
            MessageSeverity.BAD,
            "نسبة TSB / Albumin مرتفعة",
            "نسبة TSB/Albumin = ${String.format(Locale.US, "%.2f", ratio)} (عتبة التبديل ≥ $threshold). تدعم قرار النظر في تبديل الدم جنباً إلى جنب مع TSB."
          )
        )
      } else {
        messages.add(
          ClinicalMessage(
            MessageSeverity.INFO,
            "نسبة TSB / Albumin",
            "نسبة TSB/Albumin = ${String.format(Locale.US, "%.2f", ratio)} (الحد الحرج: $threshold)."
          )
        )
      }
    }

    // 6. Extreme Hyperbilirubinemia Warning
    if (tsb >= 25.0 || (et != null && tsb >= et)) {
      messages.add(
        ClinicalMessage(
          MessageSeverity.BAD,
          "تحذير سمّية الدماغ الحادة",
          "مستوى TSB حرج جداً (≥ 25 mg/dL). قم بتقييم مؤشر BIND Score فوراً للكشف عن علامات الاعتلال الدماغي الحاد (Acute Bilirubin Encephalopathy)."
        )
      )
    }

    return JaundiceAnalysisResult(
      messages = messages,
      rateOfRise = rateOfRise,
      isRateHigh = isRateHigh,
      dsbFractionPercent = dsbFraction,
      isCholestasisSuspected = isCholestasis,
      diffFromPhoto = diffPhoto,
      diffFromExchange = diffExchange,
      isEscalationOfCare = escalation,
      tsbAlbuminRatio = bRatio,
      tsbAlbuminThreshold = bThreshold,
      isTsbAlbuminHigh = isBHigh
    )
  }

  fun getMaiselsPretermThresholds(gaWeeks: Int): Pair<String, String>? {
    return when {
      gaWeeks < 28 -> Pair("5–6", "11–14")
      gaWeeks in 28..29 -> Pair("6–8", "12–14")
      gaWeeks in 30..31 -> Pair("8–10", "13–16")
      gaWeeks in 32..33 -> Pair("10–12", "15–18")
      gaWeeks == 34 -> Pair("12–14", "17–19")
      else -> null
    }
  }
}

// ==================== Respiratory Distress Models ====================

enum class RespiratoryFactor(
  val id: String,
  val labelAr: String,
  val labelEn: String,
  val targetDiagnosisKey: String,
  val weight: Int
) {
  CS_ELECTIVE("cs", "ولادة قيصرية اختيارية قبل المخاض", "Elective C-section before labor", "ttn", 3),
  IDM("idm", "طفل لأم مصابة بالسكري (IDM)", "Infant of diabetic mother (IDM)", "ttn", 1),
  PRETERM("preterm", "خديج (عمر الحمل < 37 أسبوع)", "Preterm (<37 weeks)", "rds", 4),
  MATERNAL_INFECTION("infection", "حمى لدى الأم / نزول ماء الجنين > 18 ساعة / GBS إيجابي", "Maternal fever / prolonged PROM / GBS+", "pna", 3),
  MECONIUM("meconium", "سائل أمينوسي معكر بالميكونيوم / ولادة متأخرة", "Meconium stained fluid / Post-term", "mas", 4),
  SUDDEN_DETERIORATION("ptx", "تدهور مفاجئ أو نقص متمايز في دخول الهواء بجانب واحد", "Sudden deterioration or unilateral air entry reduction", "ptx", 4),
  REFRACTORY_CYANOSIS("pphn", "زرقة شديدة لا تتناسب مع الضيق / لا تتحسن بالأكسجين 100%", "Severe cyanosis refractory to 100% O2", "ppc", 4)
}

data class DiagnosisDetails(
  val key: String,
  val nameAr: String,
  val nameEn: String,
  val managementAr: String,
  val managementEn: String,
  val severity: MessageSeverity
)

object RespiratoryEngine {

  val diagnoses = mapOf(
    "ttn" to DiagnosisDetails(
      key = "ttn",
      nameAr = "سرعة تنفس وليدية عابرة (TTN)",
      nameEn = "Transient Tachypnea of the Newborn (TTN)",
      managementAr = "علاج داعم فقط: أكسجين / قنية أنفية عالية التدفق (HFNC) أو CPAP خفيف. تقييد محسوب للسوائل والتغذية حسب شدة التنفس. تشخيص استبعاد يتحسن غالباً خلال 12–24 ساعة وحتى 72 ساعة.",
      managementEn = "Supportive care only: O2 / HFNC or mild CPAP. Fluid restriction and feeding adjusted to tachypnea. Diagnosis of exclusion, resolves in 12–72 hours.",
      severity = MessageSeverity.INFO
    ),
    "rds" to DiagnosisDetails(
      key = "rds",
      nameAr = "متلازمة الضائقة التنفسية (RDS)",
      nameEn = "Respiratory Distress Syndrome (RDS)",
      managementAr = "بدء CPAP أنفي مبكراً (PEEP 5–6 سم ماء وزِد حتى 7–8). إعطاء مادة السيرفكتانت (Curosurf) كعلاج إنقاذي خلال أول ساعتين من الولادة عند استمرار الاحتياج للأكسجين FiO2 > 0.3. ستيرويدات ما قبل الولادة وقائية.",
      managementEn = "Early nasal CPAP (PEEP 5-6 up to 7-8 cmH2O). Early rescue surfactant (Curosurf) within 2h if FiO2 > 0.3. Antenatal steroids for prevention.",
      severity = MessageSeverity.WARN
    ),
    "pna" to DiagnosisDetails(
      key = "pna",
      nameAr = "التهاب رئوي وليدي / تعفن الدم المبكر (PNA / Early Sepsis)",
      nameEn = "Neonatal Pneumonia / Early-Onset Sepsis",
      managementAr = "سحب مزرعة دم فورية وعمل صورة دم CBC وCRP. بدء المضادات الحيوية التجريبية واسعة المجال وريدياً فوراً (Ampicillin + Gentamicin أو Amikacin حسب بروتوكول الوحدة). دعم تنفسي وتدفئة وضغط دم.",
      managementEn = "Blood culture, CBC, CRP STAT. Prompt IV empiric broad-spectrum antibiotics (Ampicillin + Gentamicin/Amikacin). Respiratory support and hemodynamic stability.",
      severity = MessageSeverity.BAD
    ),
    "mas" to DiagnosisDetails(
      key = "mas",
      nameAr = "استنشاق العقي (Meconium Aspiration Syndrome)",
      nameEn = "Meconium Aspiration Syndrome (MAS)",
      managementAr = "دعم تنفسي مكثف مع تجنب الضغط المفرط لتجنب احتباس الهواء. مراقبة دقيقة جداً لحدوث استرواح الصدر (Pneumothorax) وارتفاع ضغط الشريان الرئوي (PPHN). التفكير في إعطاء سيرفكتانت لغسل/تعويض تثبيط السيرفكتانت.",
      managementEn = "Respiratory support avoiding barotrauma. Vigilance for pneumothorax and PPHN. Surfactant replacement may be indicated to overcome inactivation.",
      severity = MessageSeverity.BAD
    ),
    "ptx" to DiagnosisDetails(
      key = "ptx",
      nameAr = "استرواح الصدر (Pneumothorax)",
      nameEn = "Pneumothorax",
      managementAr = "فحص نفاذية الضوء (Transillumination) وصورة أشعة صدر عاجلة. تطبيق خوارزمية DOPE (Displacement, Obstruction, Pneumothorax, Equipment). استرواح بسيط غير متوتر: مراقبة وأكسجين. استرواح متوتر (Tension): تفريغ إسعافي فوري بإبرة (Needle decompression) ثم تركيب أنبوبة صدرية (Chest Tube).",
      managementEn = "Urgent transillumination & chest X-ray. Check DOPE algorithm. Simple: close observation. Tension pneumothorax: immediate needle decompression followed by chest tube placement.",
      severity = MessageSeverity.BAD
    ),
    "ppc" to DiagnosisDetails(
      key = "ppc",
      nameAr = "ارتفاع ضغط الشريان الرئوي (PPHN) أو مرض قلب خلقي زرقاوي",
      nameEn = "Persistent Pulmonary Hypertension (PPHN) or Cyanotic Heart Disease",
      managementAr = "إجراء اختبار فرط الأكسجة (Hyperoxia Test: 100% O2 لمدة 5–10 دقائق) وعمل إيكو قلب عاجل (Echo) لنفي العيوب الخلقية المعتمدة على القناة الشريانية. الحفاظ على دفء وتوازن حمضي طبيعي وتجنب الألم والتهيج. التجهيز لـ Inhaled Nitric Oxide (iNO) وموسعات الأوعية الرئوية.",
      managementEn = "Hyperoxia test (100% O2 for 5-10 min) and urgent echocardiogram. Maintain sedation, neutral pH, avoid acidosis. Prepare for iNO, prostacyclin, or PGE1 infusion if ductal-dependent CHD suspected.",
      severity = MessageSeverity.BAD
    )
  )

  fun rankDiagnoses(selectedFactorIds: Set<String>): List<Pair<DiagnosisDetails, Int>> {
    val scores = mutableMapOf<String, Int>()
    RespiratoryFactor.values().forEach { factor ->
      if (selectedFactorIds.contains(factor.id)) {
        val curr = scores.getOrDefault(factor.targetDiagnosisKey, 0)
        scores[factor.targetDiagnosisKey] = curr + factor.weight
      }
    }
    return scores.entries
      .sortedByDescending { it.value }
      .mapNotNull { entry ->
        diagnoses[entry.key]?.let { Pair(it, entry.value) }
      }
  }
}

// ==================== Fluids & GIR Models ====================

data class FluidsResult(
  val minMlPerKgDay: Double,
  val maxMlPerKgDay: Double,
  val midMlPerKgDay: Double,
  val totalMlPerDayMin: Double,
  val totalMlPerDayMax: Double,
  val hourlyRateMidpoint: Double,
  val gir: Double,
  val isGirLow: Boolean,
  val isGirHigh: Boolean,
  val isPeripheralVeinLimitExceeded: Boolean
)

object FluidsCalculator {

  // Table 15-3 Fluid Guidelines
  fun calculate(
    birthWeightG: Double,
    currentWeightKg: Double,
    ageHours: Int,
    dextrosePercent: Double,
    isUnderPhototherapyOrWarmer: Boolean
  ): FluidsResult {
    val ageColumn = when {
      ageHours < 24 -> 0
      ageHours < 48 -> 1
      else -> 2
    }

    val (baseMin, baseMax) = when {
      birthWeightG < 1000 -> when (ageColumn) {
        0 -> Pair(100.0, 120.0)
        1 -> Pair(120.0, 140.0)
        else -> Pair(140.0, 190.0)
      }
      birthWeightG < 1500 -> when (ageColumn) {
        0 -> Pair(80.0, 100.0)
        1 -> Pair(100.0, 120.0)
        else -> Pair(120.0, 160.0)
      }
      birthWeightG <= 2500 -> when (ageColumn) {
        0 -> Pair(60.0, 80.0)
        1 -> Pair(80.0, 100.0)
        else -> Pair(100.0, 150.0)
      }
      else -> when (ageColumn) {
        0 -> Pair(60.0, 80.0)
        1 -> Pair(80.0, 100.0)
        else -> Pair(90.0, 150.0)
      }
    }

    val extra = if (isUnderPhototherapyOrWarmer) 20.0 else 0.0
    val finalMin = baseMin + extra
    val finalMax = baseMax + extra
    val finalMid = (finalMin + finalMax) / 2.0

    val totalDayMin = finalMin * currentWeightKg
    val totalDayMax = finalMax * currentWeightKg
    val hourlyRate = (finalMid * currentWeightKg) / 24.0

    // GIR = Rate (ml/hr) * Dex% / (6 * Wt_kg)
    val gir = if (currentWeightKg > 0) (hourlyRate * dextrosePercent) / (6.0 * currentWeightKg) else 0.0

    return FluidsResult(
      minMlPerKgDay = finalMin,
      maxMlPerKgDay = finalMax,
      midMlPerKgDay = finalMid,
      totalMlPerDayMin = totalDayMin,
      totalMlPerDayMax = totalDayMax,
      hourlyRateMidpoint = hourlyRate,
      gir = gir,
      isGirLow = gir < 4.0,
      isGirHigh = gir > 12.5,
      isPeripheralVeinLimitExceeded = dextrosePercent > 12.5
    )
  }

  // Reverse GIR: calculates GIR from known rate, dextrose %, and weight
  fun reverseGir(rateMlPerHour: Double, dextrosePercent: Double, weightKg: Double): Double {
    if (weightKg <= 0) return 0.0
    return (rateMlPerHour * dextrosePercent) / (6.0 * weightKg)
  }
}

// ==================== NRP & Neonatal Drugs Models ====================

data class DrugDoseItem(
  val drugNameAr: String,
  val drugNameEn: String,
  val calculatedDoseDisplay: String,
  val formulaAndInstructionsAr: String,
  val formulaAndInstructionsEn: String,
  val isEmergency: Boolean = false
)

object DrugCalculator {

  fun getNrpEmergencyDoses(weightKg: Double): List<DrugDoseItem> {
    if (weightKg <= 0) return emptyList()
    val epivMin = 0.1 * weightKg
    val epivMax = 0.3 * weightKg
    val epiEttMin = 0.3 * weightKg
    val epiEttMax = 1.0 * weightKg
    val volumeNs = 10.0 * weightKg
    val d10Bolus = 2.0 * weightKg
    val bicarb = 2.0 * weightKg // 2 mEq/kg of 4.2%

    return listOf(
      DrugDoseItem(
        drugNameAr = "Epinephrine 1:10,000 وريدي (IV)",
        drugNameEn = "Epinephrine 1:10,000 IV/IO",
        calculatedDoseDisplay = "${String.format(Locale.US, "%.2f", epivMin)} – ${String.format(Locale.US, "%.2f", epivMax)} مل",
        formulaAndInstructionsAr = "الجرعة: 0.1–0.3 مل/كجم (0.01–0.03 مجم/كجم) سريعاً متبوعة بغسيل محلول ملحي. كرر كل 3–5 دقائق لو استمر النبض < 60.",
        formulaAndInstructionsEn = "0.1–0.3 ml/kg (0.01–0.03 mg/kg) rapid IV flush. Repeat q3–5 min if HR < 60.",
        isEmergency = true
      ),
      DrugDoseItem(
        drugNameAr = "Epinephrine عبر الأنبوب الحنجري (ETT)",
        drugNameEn = "Epinephrine via Endotracheal Tube",
        calculatedDoseDisplay = "${String.format(Locale.US, "%.2f", epiEttMin)} – ${String.format(Locale.US, "%.2f", epiEttMax)} مل",
        formulaAndInstructionsAr = "الجرعة: 0.3–1.0 مل/كجم فقط أثناء تجهيز الخط الوريدي. امتصاصه أقل ثباتاً من الوريد.",
        formulaAndInstructionsEn = "0.3–1.0 ml/kg via ETT while securing vascular access.",
        isEmergency = true
      ),
      DrugDoseItem(
        drugNameAr = "محلول تعويض الحجم (Volume Expansion)",
        drugNameEn = "Normal Saline / Ringer's / O-Neg Blood",
        calculatedDoseDisplay = "${String.format(Locale.US, "%.1f", volumeNs)} مل",
        formulaAndInstructionsAr = "10 مل/كجم محلول ملح 0.9% أو دم O سالب غير متطابق على مدار 5–10 دقائق لحالات الصدمة ونزيف الدم.",
        formulaAndInstructionsEn = "10 ml/kg Normal Saline or O-negative packed RBCs over 5–10 min for hypovolemia/shock.",
        isEmergency = true
      ),
      DrugDoseItem(
        drugNameAr = "دفعة جلوكوز مركز (Glucose D10W Bolus)",
        drugNameEn = "Dextrose 10% Bolus",
        calculatedDoseDisplay = "${String.format(Locale.US, "%.1f", d10Bolus)} مل",
        formulaAndInstructionsAr = "2 مل/كجم من محلول D10W (= 200 مجم/كجم) ببطء على مدار دقيقة واحدة لعلاج نقص السكر الحاد، ثم بدء تسريب GIR مستمر 6–8.",
        formulaAndInstructionsEn = "2 ml/kg of D10W (= 200 mg/kg) over 1 min for symptomatic hypoglycemia, followed by GIR 6–8.",
        isEmergency = false
      ),
      DrugDoseItem(
        drugNameAr = "بيكربونات الصوديوم (NaHCO3 4.2%)",
        drugNameEn = "Sodium Bicarbonate 4.2%",
        calculatedDoseDisplay = "${String.format(Locale.US, "%.1f", bicarb)} مل (2 mEq)",
        formulaAndInstructionsAr = "2 مل/كجم من تركيز 4.2% مخفف وريدياً ببطء شديد على مدار دقيقتين على الأقل (فقط في الحماض الاستقلابي الموثق والإنعاش المطول مع تأمين التهوية).",
        formulaAndInstructionsEn = "2 ml/kg of 4.2% (1-2 mEq/kg) slow IV over >= 2 min only in documented metabolic acidosis with effective ventilation.",
        isEmergency = true
      )
    )
  }

  fun getRespiratoryAndNicDoses(weightKg: Double): List<DrugDoseItem> {
    if (weightKg <= 0) return emptyList()

    val caffLoadMg = 20.0 * weightKg
    val caffLoadMl = caffLoadMg / 20.0 // 20 mg/ml
    val caffMaintMinMg = 5.0 * weightKg
    val caffMaintMaxMg = 8.0 * weightKg

    val theoLoadMg = 6.0 * weightKg
    val theoMaintMg = 2.0 * weightKg

    val curosurfMinMl = 1.25 * weightKg // 100 mg/kg at 80 mg/ml
    val curosurfMaxMl = 2.5 * weightKg  // 200 mg/kg at 80 mg/ml
    val curosurfTotalMaxMl = 5.0 * weightKg // max 400 mg/kg

    val ivigMinG = 0.5 * weightKg
    val ivigMaxG = 1.0 * weightKg

    val exchangeMinMl = 2.0 * 80.0 * weightKg
    val exchangeMaxMl = 2.0 * 120.0 * weightKg

    val ampicillinDose = 50.0 * weightKg // 50 mg/kg/dose
    val gentamicinDose = 4.0 * weightKg  // 4 mg/kg/dose

    return listOf(
      DrugDoseItem(
        drugNameAr = "Caffeine Citrate – جرعة التحميل (Loading)",
        drugNameEn = "Caffeine Citrate Loading Dose",
        calculatedDoseDisplay = "${String.format(Locale.US, "%.1f", caffLoadMg)} مجم = ${String.format(Locale.US, "%.2f", caffLoadMl)} مل",
        formulaAndInstructionsAr = "20 مجم/كجم كافيين سترات (تركيز 20 مجم/مل) وريدياً على 30 دقيقة أو عن طريق الفم لعلاج انقطاع النفس (Apnea of Prematurity).",
        formulaAndInstructionsEn = "20 mg/kg IV over 30 min or PO (concentration 20 mg/ml) for apnea of prematurity.",
        isEmergency = false
      ),
      DrugDoseItem(
        drugNameAr = "Caffeine Citrate – جرعة الصيانة (كل 24 ساعة)",
        drugNameEn = "Caffeine Citrate Maintenance (q24h)",
        calculatedDoseDisplay = "${String.format(Locale.US, "%.1f", caffMaintMinMg)} – ${String.format(Locale.US, "%.1f", caffMaintMaxMg)} مجم",
        formulaAndInstructionsAr = "5–8 مجم/كجم يومياً كل 24 ساعة، تبدأ بعد 24 ساعة من جرعة التحميل (المرجع الجيبي يذكر حتى 10 مجم/كجم).",
        formulaAndInstructionsEn = "5–8 mg/kg once daily starting 24h after loading dose (up to 10 mg/kg).",
        isEmergency = false
      ),
      DrugDoseItem(
        drugNameAr = "Theophylline – جرعة التحميل (Loading)",
        drugNameEn = "Theophylline Loading Dose",
        calculatedDoseDisplay = "${String.format(Locale.US, "%.1f", theoLoadMg)} مجم",
        formulaAndInstructionsAr = "6 مجم/كجم وريدياً ببطء على مدار 20 دقيقة (بديل الكافيين).",
        formulaAndInstructionsEn = "6 mg/kg IV slow infusion over 20 min.",
        isEmergency = false
      ),
      DrugDoseItem(
        drugNameAr = "Theophylline – جرعة الصيانة (كل 8 ساعات)",
        drugNameEn = "Theophylline Maintenance (q8h)",
        calculatedDoseDisplay = "${String.format(Locale.US, "%.1f", theoMaintMg)} مجم / جرعة",
        formulaAndInstructionsAr = "2 مجم/كجم كل 8 ساعات وريدياً. المستوى العلاجي المستهدف في الدم: 7–12 ميكروجرام/مل.",
        formulaAndInstructionsEn = "2 mg/kg IV q8h. Target therapeutic trough level: 7–12 mcg/ml.",
        isEmergency = false
      ),
      DrugDoseItem(
        drugNameAr = "Curosurf (سيرفكتانت طبيعي) – الجرعة الابتدائية",
        drugNameEn = "Curosurf (Poractant Alfa) Initial Dose",
        calculatedDoseDisplay = "${String.format(Locale.US, "%.2f", curosurfMinMl)} – ${String.format(Locale.US, "%.2f", curosurfMaxMl)} مل",
        formulaAndInstructionsAr = "100–200 مجم/كجم داخل الرغامي (التركيز: 80 مجم/مل). يُفضل إعطاء 200 مجم/كجم (2.5 مل/كجم) مبكراً خلال أول ساعتين لحالات RDS.",
        formulaAndInstructionsEn = "100–200 mg/kg intratracheally (80 mg/ml). 200 mg/kg (2.5 ml/kg) preferred as early rescue in RDS.",
        isEmergency = false
      ),
      DrugDoseItem(
        drugNameAr = "Curosurf – الجرعات الإضافية والحد الأقصى",
        drugNameEn = "Curosurf Subsequent & Max Doses",
        calculatedDoseDisplay = "التكرار: ${String.format(Locale.US, "%.2f", curosurfMinMl)} مل | الأقصى: ${String.format(Locale.US, "%.2f", curosurfTotalMaxMl)} مل",
        formulaAndInstructionsAr = "يمكن تكرار 100 مجم/كجم (1.25 مل/كجم) كل 12 ساعة حتى جرعتين إضافيتين لو استمرت الحاجة لـ FiO2 > 0.3. الحد الأقصى الكلي 300–400 مجم/كجم.",
        formulaAndInstructionsEn = "Repeat 100 mg/kg (1.25 ml/kg) q12h up to 2 repeat doses if FiO2 > 0.3. Max cumulative dose 300–400 mg/kg.",
        isEmergency = false
      ),
      DrugDoseItem(
        drugNameAr = "الأجسام المضادة الوريدية (IVIG)",
        drugNameEn = "Intravenous Immunoglobulin (IVIG)",
        calculatedDoseDisplay = "${String.format(Locale.US, "%.2f", ivigMinG)} – ${String.format(Locale.US, "%.2f", ivigMaxG)} جم",
        formulaAndInstructionsAr = "0.5–1.0 جم/كجم بالتنقيط الوريدي على مدار ساعتين في حالات مرض الانحلال الوليدي (Rh/ABO hemolytic disease) مع DAT إيجابي. يمكن تكرارها بعد 12 ساعة لو استمر التدهور.",
        formulaAndInstructionsEn = "0.5–1.0 g/kg IV over 2h in Rh/ABO hemolytic disease with positive DAT. Can repeat in 12h.",
        isEmergency = false
      ),
      DrugDoseItem(
        drugNameAr = "حجم دم التبديل المزدوج (Double Volume Exchange)",
        drugNameEn = "Double Volume Exchange Transfusion",
        calculatedDoseDisplay = "${String.format(Locale.US, "%.0f", exchangeMinMl)} – ${String.format(Locale.US, "%.0f", exchangeMaxMl)} مل",
        formulaAndInstructionsAr = "المعادلة: 2 × (80–120 مل/كجم) × الوزن بالكجم. خليط دم أحمر مغسول مع بلازما طازجة مجمدة للوصول لهيماتوكريت Hct ≈ 40–45%.",
        formulaAndInstructionsEn = "2 × (80–120 ml/kg) × weight. Reconstituted whole blood (washed PRBC + FFP) to target Hct 40–45%.",
        isEmergency = true
      ),
      DrugDoseItem(
        drugNameAr = "Ampicillin (مضاد حيوي تجريبي)",
        drugNameEn = "Ampicillin (Empiric Antibiotic)",
        calculatedDoseDisplay = "${String.format(Locale.US, "%.1f", ampicillinDose)} مجم / جرعة",
        formulaAndInstructionsAr = "50–100 مجم/كجم/جرعة وريدياً كل 12 ساعة (للأسبوع الأول من العمر) لتغطية Group B Strep و Listeria.",
        formulaAndInstructionsEn = "50–100 mg/kg/dose IV q12h (week 1 of life) covering GBS and Listeria.",
        isEmergency = false
      ),
      DrugDoseItem(
        drugNameAr = "Gentamicin (أمينوجليكوسايد)",
        drugNameEn = "Gentamicin (Aminoglycoside)",
        calculatedDoseDisplay = "${String.format(Locale.US, "%.1f", gentamicinDose)} مجم / جرعة",
        formulaAndInstructionsAr = "4–5 مجم/كجم/جرعة وريدياً على مدار 30 دقيقة كل 24 ساعة لكامل المدة، أو كل 36–48 ساعة للخدج حسب عمر الحمل ووظائف الكلى.",
        formulaAndInstructionsEn = "4–5 mg/kg/dose IV over 30 min q24h (term) or q36-48h (preterm) according to GA and renal function.",
        isEmergency = false
      )
    )
  }

  data class EttSpecifications(
    val internalDiameterMm: String,
    val lipDepthCm: Double,
    val bladeSize: String,
    val bladeType: String,
    val suctionFr: String
  )

  fun getEttSpecs(weightKg: Double): EttSpecifications {
    val grams = weightKg * 1000.0
    val id = when {
      grams < 1000 -> "2.5 mm"
      grams < 2000 -> "3.0 mm"
      grams < 3000 -> "3.5 mm"
      else -> "3.5 – 4.0 mm"
    }

    // NRP 7th & 8th Edition depth rule: 6 + Weight (kg) or gestational age estimate
    val depth = 6.0 + weightKg
    val blade = if (grams < 2000) "Miller 0 (مستقيمة للخديج)" else "Miller 1 (مستقيمة لكامل المدة)"
    val suction = when {
      grams < 1000 -> "5 – 6 Fr"
      grams < 2000 -> "6 – 8 Fr"
      else -> "8 – 10 Fr"
    }

    return EttSpecifications(
      internalDiameterMm = id,
      lipDepthCm = depth,
      bladeSize = blade,
      bladeType = "Miller (Straight)",
      suctionFr = suction
    )
  }
}
