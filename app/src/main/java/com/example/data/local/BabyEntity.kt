package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_babies")
data class BabyEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val nameOrBed: String,
  val weightKg: Double,
  val birthWeightG: Double,
  val gestationalAgeWeeks: Int,
  val ageHours: Int,
  val isUnderWarmerOrPhoto: Boolean = false,
  val tsb: Double? = null,
  val dsb: Double? = null,
  val dextrosePercent: Double = 10.0,
  val notes: String = "",
  val updatedAtMillis: Long = System.currentTimeMillis()
)
