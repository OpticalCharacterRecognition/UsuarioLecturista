UsuarioLecturista
=================

Proyecto para la junta de aguas de el usuario lecturista


OCR compile instructions:

OCR Test on windows

Need:
-Android Studio 0.8.9 +
-NDK android-ndk32-r10b-windows-x86_64.zip
http://dl.google.com/android/ndk/android-ndk32-r10b-windows-x86_64.zip

http://dl.google.com/android/ndk/android-ndk-r10c-darwin-x86_64.bin
-Ant


Original Tutorial
https://coderwall.com/p/eurvaq

Some code
https://github.com/rmtheis/android-ocr

Tess-Two library
https://github.com/rmtheis/tess-two

Instructions:

Set the NDK, Android SDK tools and platform tools, Ant/bin directories to the PATH

download tess library, then:

git clone git://github.com/rmtheis/tess-two tess
cd tess
cd tess-two
ndk-build
android update project --path . 
(this might not work so you can try 
android update project --path . --target android-19
)
ant release

Then in windows explorer make a libraries folder in the root of your project then copy tess library to it.
Delete 
project.properties, build.xml, .classpath, and .project

create build.gradle file in root of tess like this:
```
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:0.9.+'
    }
}

apply plugin: 'android-library'

android {
    compileSdkVersion 19
    buildToolsVersion "19.1.0"

    defaultConfig {
        minSdkVersion 8
        targetSdkVersion 19
    }

    sourceSets.main {
        manifest.srcFile 'AndroidManifest.xml'
        java.srcDirs = ['src']
        resources.srcDirs = ['src']
        res.srcDirs = ['res']
        jniLibs.srcDirs = ['libs']
    }
}
```
edit  settings.gradle in main project
add this line 
include ':libraries:tess-two'

sync in gradle

Then sync the project in Android Studio and add the new tess-two library as module dependency to you main project(after sync tess-two library should appear as a module, you can add it to your project from project settings in android studio)

Go to tess project in command window again and execute:
ndk-build

sync again run.


For mac:
first to modify the path is 

sudo nano /etc/paths

echo $PATH

/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:/Users/carlos/Developer/ndk/android-ndk-r10c:/Users/carlos/Developer/apache-ant-1.9.4/bin:/Users/carlos/Developer/android-tools/sdk/platform-tools:/Users/carlos/Developer/android-tools/sdk/tools


then try to put the sdk in an accesible place like all these
