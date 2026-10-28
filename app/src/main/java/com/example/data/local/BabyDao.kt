package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BabyDao {

  @Query("SELECT * FROM saved_babies ORDER BY updatedAtMillis DESC")
  fun getAllBabies(): Flow<List<BabyEntity>>

  @Query("SELECT * FROM saved_babies WHERE id = :id LIMIT 1")
  suspend fun getBabyById(id: Long): BabyEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBaby(baby: BabyEntity): Long

  @Update
  suspend fun updateBaby(baby: BabyEntity)

  @Delete
  suspend fun deleteBaby(baby: BabyEntity)

  @Query("DELETE FROM saved_babies WHERE id = :id")
  suspend fun deleteById(id: Long)
}
