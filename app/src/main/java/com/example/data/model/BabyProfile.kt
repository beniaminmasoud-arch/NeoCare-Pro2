package com.example.data.model

data class BabyProfile(
  val weightKg: Double = 3.2,
  val birthWeightG: Double = 3200.0,
  val gestationalAgeWeeks: Int = 38,
  val ageHours: Int = 48,
  val isUnderPhototherapyOrWarmer: Boolean = false,
  val label: String = "طفل جديد"
)

enum class WeightPreset(val labelAr: String, val labelEn: String, val weightKg: Double, val gaWeeks: Int) {
  ELBW("شديد الصغر (ELBW)", "ELBW (<1 kg)", 0.85, 26),
  VLBW("صغير جداً (VLBW)", "VLBW (<1.5 kg)", 1.25, 29),
  PRETERM("خديج (Preterm)", "Preterm (2.2 kg)", 2.2, 34),
  TERM("كامل المدة (Term)", "Term (3.2 kg)", 3.2, 38),
  POST_TERM("متأخر الولادة (Post)", "Post-term (3.8 kg)", 3.8, 41)
}
