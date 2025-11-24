//file:noinspection GroovyAssignabilityCheck
//file:noinspection DependencyNotationArgument
//file:noinspection GroovyImplicitNullArgumentCall

plugins {
    `java-library`
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
dependencies {
    compileOnly(libs.guava)
    implementation(project(":multidexlib2-custom"))
    implementation(libs.jakarta.annotation.api)
}