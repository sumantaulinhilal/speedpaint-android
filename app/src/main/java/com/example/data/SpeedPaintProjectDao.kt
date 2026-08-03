package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedPaintProjectDao {

    @Query("SELECT * FROM speedpaint_projects ORDER BY dateCreated DESC")
    fun getAllProjects(): Flow<List<SpeedPaintProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: SpeedPaintProjectEntity)

    @Delete
    suspend fun deleteProject(project: SpeedPaintProjectEntity)

    @Query("SELECT * FROM speedpaint_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): SpeedPaintProjectEntity?
}
