# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/hemanths/Library/Android/sdk/tools/proguard/proguard-android.txt
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

-optimizationpasses 5                       # 指定代码的压缩级别
-dontusemixedcaseclassnames                 # 纯小写混合类名
-dontskipnonpubliclibraryclasses            # 不忽略非公共库的类
-dontskipnonpubliclibraryclassmembers       # 不忽略非公共库的类
-dontpreverify                              # 不做预校验
-verbose                                    # 混淆时记录日志
-allowaccessmodification                    # 优化时允许访问并修改有修饰符的类和类的成员
-mergeinterfacesaggressively                # 混淆时应用侵入式重载

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # 混淆时所采用的算法

-keepattributes *Annotation*                # 保护代码中的Annotation不被混淆
-keepattributes Signature                   # 避免混淆泛型
-renamesourcefileattribute ProGuard         # 重命名被混淆文件名
-keepattributes SourceFile,LineNumberTable  # 抛出异常时保留代码行号
-ignorewarnings

-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*

# RetroFit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-dontwarn javax.annotation.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

-keep class !android.support.v7.internal.view.menu.**,** {*;}

-dontwarn
-ignorewarnings

-keep public class android.support.design.widget.BottomNavigationView { *; }
-keep public class android.support.design.internal.BottomNavigationMenuView { *; }
-keep public class android.support.design.internal.BottomNavigationPresenter { *; }
-keep public class android.support.design.internal.BottomNavigationItemView { *; }

#-dontwarn android.support.v8.renderscript.*
#-keepclassmembers class android.support.v8.renderscript.RenderScript {
#  native *** rsn*(...);
#  native *** n*(...);
#}

#-keep class org.jaudiotagger.** { *; }

#For cast
-keep class code.name.monkey.retromusic.cast.CastOptionsProvider { *; }
-keep class android.support.** { *; }
-keep class com.google.** { *; }
-keep class java.nio.file.** { *; }

-obfuscationdictionary build/obfuscation-dictionary.txt
-classobfuscationdictionary build/class-dictionary.txt
-packageobfuscationdictionary build/package-dictionary.txt
