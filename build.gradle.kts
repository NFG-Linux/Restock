// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
   // alias(libs.plugins.android.application) apply false
   id("com.android.application") version "8.8.1" apply false
   //dependency for Google services Gradle plugin
   id("com.google.gms.google-services") version "4.4.2" apply false
   alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false
}