import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.20"
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

val ideSinceBuild = "241"
val ideUntilBuild = "262.*"

group = "com.sigmap"
version = "4.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.1")
        pluginVerifier()
        zipSigner()
    }

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.2.20")
}

intellijPlatform {
    pluginConfiguration {
        version = project.version.toString()
        ideaVersion {
            sinceBuild = ideSinceBuild
            untilBuild = ideUntilBuild
        }
        changeNotes = """
            <h3>4.1.0</h3>
            <ul>
              <li>IDE compatibility extended to 2026.2 (until-build 262.*).</li>
              <li>Windows: the status-bar health probe now uses the same cross-platform command resolution as Regenerate (previously fell back to a file-age grade).</li>
              <li>The health probe runs only when the context file changes (or every 10 minutes) instead of every 60 seconds, and is bounded by a 30-second timeout; regeneration is now cancellable with a 5-minute cap.</li>
              <li>The status bar refreshes immediately after a successful regeneration.</li>
            </ul>
            <h3>4.0.1</h3>
            <ul>
              <li>Fixed both Plugin Verifier warnings: the stale-context notification's Regenerate button now fires the action via <code>ActionManager.tryToExecute(...)</code> instead of calling <code>actionPerformed</code> directly with a deprecated <code>createFromDataContext</code> event.</li>
            </ul>
            <h3>4.0.0</h3>
            <ul>
              <li>First standalone release — health status bar, one-click regenerate, stale-context notification, auto-refresh.</li>
            </ul>
        """.trimIndent()
    }

    pluginVerification {
        ides {
            create(IntelliJPlatformType.IntellijIdeaCommunity, "2024.1")
            create(IntelliJPlatformType.IntellijIdeaCommunity, "2025.2")
        }
    }

    signing {
        certificateChain = System.getenv("CERTIFICATE_CHAIN")
        privateKey = System.getenv("PRIVATE_KEY")
        password = System.getenv("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = System.getenv("PUBLISH_TOKEN")
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
