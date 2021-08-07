#-dontobfuscate
#-dontoptimize
#-dontshrink

-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-repackageclasses ru.nikitenkogleb.tkvs
-verbose

#-printmapping 'mapping.txt'
#-printconfiguration 'configuration.txt'

#-optimizations !code/simplification/arithmetic
#-optimizations !code/simplification/cast
#-optimizations !code/allocation/variable
#-optimizations !field

-keepparameternames
#-renamesourcefileattribute SourceFile
-keepattributes *Annotation*, Signature
-keepattributes EnclosingMethod
#-keepattributes LineNumberTable, SourceFile
-keepattributes InnerClasses, Exceptions

-keep class androidx.appcompat.widget.FitWindowsFrameLayout { <init>(...); }
-keep class androidx.appcompat.widget.FitWindowsLinearLayout { <init>(...); }
-keep class androidx.appcompat.widget.ViewStubCompat { <init>(...); }
-keep class androidx.appcompat.widget.ContentFrameLayout { <init>(...); }
-keep class androidx.core.app.CoreComponentFactory { <init>(...); }
-keep class com.google.android.material.textfield.TextInputLayout { <init>(...); }
-keep class com.google.android.material.internal.CheckableImageButton { <init>(...); }

-keep class ru.nikitenkogleb.tkvs.Activity { <init>(...); }
-keep class ru.nikitenkogleb.tkvs.Input { <init>(...); }
-keep class ru.nikitenkogleb.tkvs.Application { <init>(...); }
