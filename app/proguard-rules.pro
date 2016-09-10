# JSoup
-keeppackagenames org.jsoup.nodes

# ACRA
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keep class org.acra.** { *; }
-dontwarn android.support.v4.app.NotificationCompat*