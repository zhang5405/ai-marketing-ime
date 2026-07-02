# AI营销输入法 ProGuard 规则

# 保留 Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# 保留 Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# 保留 OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# 保留 Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# 保留 DeepSeek API 模型
-keep class com.aiime.** { *; }

# 保留 Miuix UI 库
-keep class top.yukonga.miuix.** { *; }
