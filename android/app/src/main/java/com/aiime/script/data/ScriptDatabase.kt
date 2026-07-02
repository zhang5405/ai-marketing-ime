package com.aiime.script.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 话术实体
 */
@Entity(
    tableName = "scripts",
    indices = [
        Index("category"),
        Index("is_favorite"),
    ]
)
data class ScriptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "category")
    val category: String = "other",  // greeting/product/objection/closing/follow_up/social/after_sales/other

    @ColumnInfo(name = "tags")
    val tags: String = "",  // 逗号分隔

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "usage_count")
    val usageCount: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * 话术数据访问对象
 */
@Dao
interface ScriptDao {

    /** 按分类+收藏排序分页查询 */
    @Query("""
        SELECT * FROM scripts
        WHERE (:category IS NULL OR category = :category)
          AND (:favoriteOnly = 0 OR is_favorite = 1)
        ORDER BY is_favorite DESC, usage_count DESC, created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getScripts(
        category: String? = null,
        favoriteOnly: Boolean = false,
        limit: Int = 20,
        offset: Int = 0,
    ): List<ScriptEntity>

    /** 搜索话术 */
    @Query("""
        SELECT * FROM scripts
        WHERE title LIKE '%' || :keyword || '%'
           OR content LIKE '%' || :keyword || '%'
           OR tags LIKE '%' || :keyword || '%'
        ORDER BY is_favorite DESC, usage_count DESC
        LIMIT :limit
    """)
    suspend fun search(keyword: String, limit: Int = 20): List<ScriptEntity>

    /** 获取所有话术 Flow */
    @Query("""
        SELECT * FROM scripts
        ORDER BY is_favorite DESC, updated_at DESC
    """)
    fun observeAll(): Flow<List<ScriptEntity>>

    /** 按分类获取 Flow */
    @Query("""
        SELECT * FROM scripts
        WHERE category = :category
        ORDER BY is_favorite DESC, updated_at DESC
    """)
    fun observeByCategory(category: String): Flow<List<ScriptEntity>>

    /** 获取单条 */
    @Query("SELECT * FROM scripts WHERE id = :id")
    suspend fun getById(id: Long): ScriptEntity?

    /** 获取数量 */
    @Query("SELECT COUNT(*) FROM scripts")
    suspend fun count(): Int

    /** 插入 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(script: ScriptEntity): Long

    /** 批量插入 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(scripts: List<ScriptEntity>)

    /** 更新 */
    @Update
    suspend fun update(script: ScriptEntity)

    /** 增加使用次数 */
    @Query("UPDATE scripts SET usage_count = usage_count + 1, updated_at = :now WHERE id = :id")
    suspend fun incrementUsage(id: Long, now: Long = System.currentTimeMillis())

    /** 切换收藏 */
    @Query("UPDATE scripts SET is_favorite = NOT is_favorite, updated_at = :now WHERE id = :id")
    suspend fun toggleFavorite(id: Long, now: Long = System.currentTimeMillis())

    /** 删除 */
    @Delete
    suspend fun delete(script: ScriptEntity)

    /** 按 ID 删除 */
    @Query("DELETE FROM scripts WHERE id = :id")
    suspend fun deleteById(id: Long)
}

/**
 * 话术数据库
 */
@Database(
    entities = [ScriptEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ScriptDatabase : RoomDatabase() {
    abstract fun scriptDao(): ScriptDao

    companion object {
        const val DATABASE_NAME = "scripts.db"
    }
}
