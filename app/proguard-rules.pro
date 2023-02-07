# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\connectechconnect\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.otcengineering.apible.** { *; }
-dontwarn com.otcengineering.apible.**
-keep class org.simpleframework.xml.stream.** { *; }
-dontwarn org.simpleframework.xml.stream.**
-keep class okhttp3.internal.platform.** { *; }
-dontwarn okhttp3.internal.platform.**
-keep class com.google.** { *; }
-dontwarn com.google.**
-keep class com.amazonaws.** { *; }
-dontwarn com.amazonaws.**
-keep class com.otc.alice.** { *; }
-dontwarn com.otc.alice.**
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**
-keep class com.shockwave.**
-keep class org.simpleframework.** { *; }
-dontwarn org.simpleframework.**