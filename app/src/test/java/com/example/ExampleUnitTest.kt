package com.example

import com.example.data.model.DrugCalculator
import com.example.data.model.FluidsCalculator
import com.example.data.model.JaundiceCalculator
import com.example.data.model.JaundiceInputs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testFluidsAndGirCalculation() {
    // 3.2 kg term infant > 48h with D10W
    val result = FluidsCalculator.calculate(
      birthWeightG = 3200.0,
      currentWeightKg = 3.2,
      ageHours = 50,
      dextrosePercent = 10.0,
      isUnderPhototherapyOrWarmer = false
    )
    // Table 15-3 for >2500g and >48h is 90–150 ml/kg/day, mid = 120 ml/kg/day
    assertEquals(90.0, result.minMlPerKgDay, 0.1)
    assertEquals(150.0, result.maxMlPerKgDay, 0.1)
    assertEquals(120.0, result.midMlPerKgDay, 0.1)
    // Hourly rate = 120 * 3.2 / 24 = 16.0 ml/hr
    assertEquals(16.0, result.hourlyRateMidpoint, 0.1)
    // GIR = 16.0 * 10 / (6 * 3.2) = 160 / 19.2 = 8.33
    assertEquals(8.33, result.gir, 0.1)

    // Reverse GIR test: Rate 16 ml/hr, Dex 10%, Wt 3.2 kg -> ~8.33
    val revGir = FluidsCalculator.reverseGir(16.0, 10.0, 3.2)
    assertEquals(8.33, revGir, 0.1)
  }

  @Test
  fun testEttSizingAndLipDepth() {
    // 3.2 kg term baby -> ID 3.5–4.0 mm, depth = 6 + 3.2 = 9.2 cm
    val ett = DrugCalculator.getEttSpecs(3.2)
    assertEquals(9.2, ett.lipDepthCm, 0.05)
    assertTrue(ett.internalDiameterMm.contains("3.5"))

    // 900g ELBW baby -> ID 2.5 mm, depth = 6 + 0.9 = 6.9 cm
    val elbwEtt = DrugCalculator.getEttSpecs(0.9)
    assertEquals(6.9, elbwEtt.lipDepthCm, 0.05)
    assertEquals("2.5 mm", elbwEtt.internalDiameterMm)
  }

  @Test
  fun testJaundiceEscalationOfCare() {
    // Exchange threshold 20.0, TSB 18.5 (>= 18.0) -> Escalation of care
    val inputs = JaundiceInputs(
      tsb = 18.5,
      dsb = 0.6,
      albumin = 3.2,
      phototherapyThreshold = 15.0,
      exchangeTransfusionThreshold = 20.0
    )
    val res = JaundiceCalculator.analyze(inputs, 3.2, 38, 48)
    assertTrue(res.isEscalationOfCare)
  }
}
