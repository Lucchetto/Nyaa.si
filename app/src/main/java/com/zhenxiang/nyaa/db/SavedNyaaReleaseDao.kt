package com.zhenxiang.nyaa.db

import androidx.room.*
import com.zhenxiang.nyaa.api.ApiDataSource
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedNyaaReleaseDao {

    // Order from most recent
    @Query("SELECT * FROM savednyaarelease ORDER BY savedTimestamp DESC")
    fun getAll(): Flow<List<SavedNyaaRelease>>

    // Order from most recent
    @Query("SELECT * From savednyaarelease INNER JOIN nyaareleasepreview ON number=parent_number AND dataSource=parent_dataSource ORDER BY savedTimestamp DESC")
    fun getAllWithDetails(): Flow<List<SavedNyaaReleaseWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(release: SavedNyaaRelease)

    @Query("SELECT * FROM savednyaarelease WHERE parent_number=:number AND parent_dataSource=:dataSource")
    fun getItemById(number: Int, dataSource: ApiDataSource): SavedNyaaRelease?

    @Delete
    fun delete(release: SavedNyaaRelease)
}