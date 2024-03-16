package com.meganov.passwordmanager.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SiteDao {
    @Query("SELECT * FROM site")
    fun getAll(): LiveData<List<Site>>

    @Query("SELECT * FROM site WHERE id IN (:siteIds)")
    fun loadAllByIds(siteIds: IntArray): LiveData<List<Site>>

    @Query("SELECT * FROM site WHERE login LIKE :name LIMIT 1")
    fun findByName(name: String): Site

    @Insert
    suspend fun insert(vararg sites: Site)

    @Update
    suspend fun update(site: Site)

    @Delete
    suspend fun delete(site: Site)
}
