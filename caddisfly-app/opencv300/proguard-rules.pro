# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Nick\AppData\Local\Android\android-studio\sdk/tools/proguard/proguard-android.txt
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
-dontoptimize

-keep class org.opencv.** { *; }
-keep class org.opencv.core.** { *; }
-keep class org.opencv.core.CvType
-keep class org.opencv.core.Mat
-keep class org.opencv.core.MatOfPoint2f
-keep class org.opencv.core.Point
-keep class org.opencv.imgproc.Imgproc
-keep class org.opencv.core.Rect
-keep class org.opencv.core.Scalar
-keep class org.opencv.core.Size

-keepattributes InnerClasses

-keepclassmembers public class org.opencv.core.Core {
    public static class MinMaxLocResult;
}

