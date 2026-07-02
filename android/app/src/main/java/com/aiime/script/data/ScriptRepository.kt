package com.aiime.script.data

import kotlinx.coroutines.flow.Flow

/**
 * 话术分类枚举
 */
enum class ScriptCategory(val id: String, val displayName: String, val icon: String) {
    GREETING("greeting", "开场白", "👋"),
    PRODUCT("product", "产品介绍", "📦"),
    OBJECTION("objection", "异议处理", "💬"),
    CLOSING("closing", "促成成交", "🎯"),
    FOLLOW_UP("follow_up", "跟进", "📞"),
    SOCIAL("social", "朋友圈", "📱"),
    AFTER_SALES("after_sales", "售后", "🛠️"),
    OTHER("other", "其他", "📋");

    companion object {
        fun fromId(id: String): ScriptCategory =
            entries.find { it.id == id } ?: OTHER
    }
}

/**
 * 话术仓库 — 封装 DAO 操作
 */
class ScriptRepository(private val dao: ScriptDao) {

    fun observeAll(): Flow<List<ScriptEntity>> = dao.observeAll()

    fun observeByCategory(category: String): Flow<List<ScriptEntity>> =
        dao.observeByCategory(category)

    suspend fun getScripts(
        category: String? = null,
        favoriteOnly: Boolean = false,
        limit: Int = 20,
        offset: Int = 0,
    ): List<ScriptEntity> = dao.getScripts(category, favoriteOnly, limit, offset)

    suspend fun search(keyword: String, limit: Int = 20): List<ScriptEntity> =
        dao.search(keyword, limit)

    suspend fun getById(id: Long): ScriptEntity? = dao.getById(id)

    suspend fun count(): Int = dao.count()

    suspend fun insert(script: ScriptEntity): Long = dao.insert(script)

    suspend fun insertAll(scripts: List<ScriptEntity>) = dao.insertAll(scripts)

    suspend fun update(script: ScriptEntity) = dao.update(script)

    suspend fun incrementUsage(id: Long) = dao.incrementUsage(id)

    suspend fun toggleFavorite(id: Long) = dao.toggleFavorite(id)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    /** 获取分类列表（含计数） */
    suspend fun getCategoriesWithCount(): List<Pair<ScriptCategory, Int>> {
        val all = dao.getScripts(limit = 1000)
        val counts = all.groupBy { ScriptCategory.fromId(it.category) }
            .mapValues { it.value.size }
        return ScriptCategory.entries.map { it to (counts[it] ?: 0) }
    }
}
