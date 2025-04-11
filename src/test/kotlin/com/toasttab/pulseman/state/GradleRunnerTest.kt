package com.toasttab.pulseman.state

import androidx.compose.runtime.mutableStateOf
import com.toasttab.pulseman.entities.ClassInfo
import com.toasttab.pulseman.entities.JarLoaderType
import com.toasttab.pulseman.files.FileManagement
import com.toasttab.pulseman.jars.JarManager
import com.toasttab.pulseman.jars.LoadedClasses
import com.toasttab.pulseman.jars.RunTimeJarLoader
import com.toasttab.pulseman.pulsar.filters.AuthClassFilter
import com.toasttab.pulseman.pulsar.filters.protobuf.GeneratedMessageV3Filter
import com.toasttab.pulseman.pulsar.filters.protobuf.KTMessageFilter
import com.toasttab.pulseman.pulsar.handlers.PulsarAuthHandler
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import com.toasttab.pulseman.scripting.GradleScripting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.URL

class GradleRunnerTest {
    private lateinit var fileManagement: FileManagement
    private lateinit var globalFeedback: GlobalFeedback
    private lateinit var commonLoadedJars: MutableList<URL>
    private lateinit var messageLoadedJars: MutableList<URL>
    private lateinit var authLoadedJars: MutableList<URL>
    private lateinit var commonJarLoader: RunTimeJarLoader
    private lateinit var messageJarLoader: RunTimeJarLoader
    private lateinit var authJarLoader: RunTimeJarLoader
    private lateinit var commonJars: JarManager<ClassInfo>
    private lateinit var messageJars: JarManager<PulsarMessageClassInfo>
    private lateinit var authJars: JarManager<PulsarAuthHandler>
    private lateinit var pulsarJarManagers: List<JarManager<out ClassInfo>>

    @Test
    fun `Confirm non pulsar files are all stored in the common jar loader when not filtering for pulsar`(@TempDir tempDir: File) {
        setup(tempDir = tempDir)

        val gradleRunner = gradleRunner(filterPulsar = false, tempDir = tempDir)
        gradleRunner.runGradleTask(
            gradleScript = scriptWithDependency(
                includePulsarAuthImport = false,
                includeProtoKtMessage = false
            )
        )

        assertJarLocations(
            tempDir = tempDir,
            expectedCommonFiles = sl4jCommonFiles,
            expectedAuthFiles = emptyList(),
            expectedMessageFiles = emptyList()
        )
    }

    @Test
    fun `Confirm non pulsar files are not stored at all when filtering for pulsar files`(@TempDir tempDir: File) {
        setup(tempDir = tempDir)

        val gradleRunner = gradleRunner(filterPulsar = true, tempDir = tempDir)
        gradleRunner.runGradleTask(
            gradleScript = scriptWithDependency(
                includePulsarAuthImport = false,
                includeProtoKtMessage = false
            )
        )

        assertJarLocations(
            tempDir = tempDir,
            expectedCommonFiles = emptyList(),
            expectedAuthFiles = emptyList(),
            expectedMessageFiles = emptyList()
        )
    }

    @Test
    fun `Confirm pulsar files are stored in the correct loaders when not filtering for pulsar files`(@TempDir tempDir: File) {
        setup(tempDir = tempDir)

        val gradleRunner = gradleRunner(filterPulsar = false, tempDir = tempDir)
        gradleRunner.runGradleTask(
            gradleScript = scriptWithDependency(
                includePulsarAuthImport = true,
                includeProtoKtMessage = true
            )
        )

        assertJarLocations(
            tempDir = tempDir,
            expectedCommonFiles = pulsarCommonFiles + sl4jCommonFiles,
            expectedAuthFiles = pulsarAuthFiles,
            expectedMessageFiles = protoktMessageFiles
        )
    }

    @Test
    fun `Confirm pulsar files are stored in the correct loaders when filtering`(@TempDir tempDir: File) {
        setup(tempDir = tempDir)

        val gradleRunner = gradleRunner(filterPulsar = true, tempDir = tempDir)
        gradleRunner.runGradleTask(
            gradleScript = scriptWithDependency(
                includePulsarAuthImport = true,
                includeProtoKtMessage = true
            )
        )

        assertJarLocations(
            tempDir = tempDir,
            expectedCommonFiles = emptyList(),
            expectedAuthFiles = pulsarAuthFiles,
            expectedMessageFiles = protoktMessageFiles
        )
    }

    private fun setup(tempDir: File) {
        fileManagement = FileManagement(homeDirectory = tempDir.absolutePath)
        globalFeedback = GlobalFeedback()
        commonLoadedJars = mutableListOf()
        messageLoadedJars = mutableListOf()
        authLoadedJars = mutableListOf()

        commonJarLoader = RunTimeJarLoader(loadedJars = commonLoadedJars)
        // This is used for auth operation, inherits the messageJarLoader and will be available in all tabs
        authJarLoader = RunTimeJarLoader(dependentJarLoader = commonJarLoader, loadedJars = authLoadedJars)
        // A jar loader for storing global pulsar message jars
        messageJarLoader = RunTimeJarLoader(dependentJarLoader = authJarLoader, loadedJars = messageLoadedJars)

        commonJars = JarManager(
            loadedClasses = LoadedClasses(
                classFilters = emptyList(),
                runTimeJarLoader = commonJarLoader
            ),
            jarFolderName = DEPENDENCY_JAR_FOLDER,
            globalFeedback = globalFeedback,
            runTimeJarLoader = commonJarLoader,
            originalJarFolderName = null,
            tabFileExtension = null,
            fileManagement = fileManagement
        )

        messageJars = JarManager(
            loadedClasses = LoadedClasses(
                classFilters = listOf(
                    GeneratedMessageV3Filter(runTimeJarLoader = messageJarLoader),
                    KTMessageFilter(runTimeJarLoader = messageJarLoader)
                ),
                runTimeJarLoader = messageJarLoader
            ),
            jarFolderName = GLOBAL_MESSAGE_JAR_FOLDER,
            globalFeedback = globalFeedback,
            runTimeJarLoader = messageJarLoader,
            originalJarFolderName = null,
            tabFileExtension = null,
            fileManagement = fileManagement
        )

        authJars = JarManager(
            loadedClasses = LoadedClasses(
                classFilters = listOf(
                    // Add all the pulsar auth classes supported here
                    AuthClassFilter()
                ),
                runTimeJarLoader = authJarLoader
            ),
            jarFolderName = AUTH_JAR_FOLDER,
            globalFeedback = globalFeedback,
            runTimeJarLoader = authJarLoader,
            originalJarFolderName = null,
            tabFileExtension = null,
            fileManagement = fileManagement
        )

        pulsarJarManagers = listOf(messageJars, authJars)
    }

    private fun gradleRunner(filterPulsar: Boolean, tempDir: File) = GradleRunner(
        setUserFeedback = { },
        commonJarManager = commonJars,
        pulsarJarManagers = pulsarJarManagers,
        onChange = { },
        taskName = TASK_NAME,
        filterPulsarJars = mutableStateOf(filterPulsar),
        projectDir = tempDir,
        fileManagement = fileManagement
    )

    private fun scriptWithDependency(includePulsarAuthImport: Boolean, includeProtoKtMessage: Boolean): String {
        return GradleScripting.gradleTemplate.replace(
            "dependencies {",
            """dependencies { 
                implementation("org.slf4j:slf4j-nop:1.7.30")
                ${if (includePulsarAuthImport) "implementation(\"org.apache.pulsar:pulsar-client-admin:2.8.0\")\n" else "\n"}
                ${if (includeProtoKtMessage) "implementation(\"com.toasttab.protokt.thirdparty:proto-google-common-protos:0.12.1\")\n" else "\n"}
                """
        )
    }

    private fun assertJarLocations(
        tempDir: File,
        expectedCommonFiles: List<String>,
        expectedAuthFiles: List<String>,
        expectedMessageFiles: List<String>
    ) {
        fun getUrls(jarLoader: RunTimeJarLoader) =
            jarLoader.getJarLoader(jarLoaderType = JarLoaderType.BASE).urLs.toList()

        fun getFileNames(urls: List<URL>) = urls.map { it.path.substringAfterLast('/') }
        fun assertUrlContents(urls: List<URL>, expectedFiles: List<String>) {
            val urlFileNames = getFileNames(urls = urls)
            assertThat(urls).hasSize(expectedFiles.size)
            expectedFiles.forEach {
                assertThat(urlFileNames).contains(it)
            }
        }

        fun assertFiles(files: Array<File>?, expectedFiles: List<String>) {
            assertThat(files).hasSize(expectedFiles.size)
            expectedFiles.forEach { file ->
                assertThat(files?.find { it.name == file }).exists()
            }
        }

        // Confirm jars are correctly loading in the jar loaders and that dependent loaders are correctly loading
        val commonUrls = getUrls(jarLoader = commonJarLoader)
        val authUrls = getUrls(jarLoader = authJarLoader)
        val messageUrls = getUrls(jarLoader = messageJarLoader)

        assertUrlContents(urls = commonUrls, expectedFiles = expectedCommonFiles)
        assertUrlContents(urls = authUrls, expectedFiles = expectedCommonFiles + expectedAuthFiles)
        assertUrlContents(
            urls = messageUrls,
            expectedFiles = expectedCommonFiles + expectedAuthFiles + expectedMessageFiles
        )

        // Confirm underlying jar lists only store their expected files and nothing else in the dependency tree
        assertUrlContents(urls = commonLoadedJars, expectedFiles = expectedCommonFiles)
        assertUrlContents(urls = authLoadedJars, expectedFiles = expectedAuthFiles)
        assertUrlContents(urls = messageLoadedJars, expectedFiles = expectedMessageFiles)

        val files = tempDir.listFiles()
        assertThat(files).hasSize(2)
        val pulsemanConfigFiles = files?.first { it.name == PULSEMAN_CONFIG_FOLDER }?.listFiles()
        val gradleFiles = files?.first { it.name == GRADLE_CONFIG_FOLDER }?.listFiles()
        assertThat(gradleFiles).hasSize(3)

        assertThat(pulsemanConfigFiles).hasSize(4)
        val authFiles = pulsemanConfigFiles?.first { it.name == AUTH_JAR_FOLDER }?.listFiles()
        val projectFiles = pulsemanConfigFiles?.first { it.name == PROJECT_FOLDER }?.listFiles()
        val messageFiles = pulsemanConfigFiles?.first { it.name == GLOBAL_MESSAGE_JAR_FOLDER }?.listFiles()
        val dependencyFiles = pulsemanConfigFiles?.first { it.name == DEPENDENCY_JAR_FOLDER }?.listFiles()

        // Confirm the correct files are copied to the correct folders after download
        assertFiles(files = authFiles, expectedFiles = expectedAuthFiles)
        assertFiles(files = dependencyFiles, expectedFiles = expectedCommonFiles)
        assertFiles(files = messageFiles, expectedFiles = expectedMessageFiles)
        assertThat(projectFiles).isEmpty()
    }

    companion object {
        private const val DEPENDENCY_JAR_FOLDER = "dependency_jars"
        private const val GLOBAL_MESSAGE_JAR_FOLDER = "global_message_jars"
        private const val AUTH_JAR_FOLDER = "auth_jars"
        private const val PROJECT_FOLDER = "projects"
        private const val PULSEMAN_CONFIG_FOLDER = "pulseman_config"
        private const val GRADLE_CONFIG_FOLDER = ".gradle"
        private const val TASK_NAME = "gradle_runner_test"

        private val pulsarCommonFiles = listOf(
            "jakarta.xml.bind-api-2.3.3.jar",
            "checker-qual-3.5.0.jar",
            "pulsar-transaction-common-2.8.0.jar",
            "gson-2.8.6.jar",
            "bcprov-jdk15on-1.68.jar",
            "auto-service-annotations-1.0.1.jar",
            "bcpkix-jdk15on-1.68.jar",
            "jackson-core-2.12.2.jar",
            "validation-api-1.1.0.Final.jar",
            "jakarta.ws.rs-api-2.1.6.jar",
            "failureaccess-1.0.1.jar",
            "pulsar-client-admin-api-2.8.0.jar",
            "jaxb-api-2.3.1.jar",
            "javax.activation-1.2.0.jar",
            "error_prone_annotations-2.3.4.jar",
            "commons-compress-1.20.jar",
            "kotlin-reflect-1.6.21.jar",
            "jul-to-slf4j-1.7.25.jar",
            "listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar",
            "bcprov-ext-jdk15on-1.68.jar",
            "jakarta.activation-api-1.2.2.jar",
            "protokt-extensions-api-0.12.1.jar",
            "guava-30.1-jre.jar",
            "protokt-runtime-0.12.1.jar",
            "proto-google-common-protos-0.12.1.jar",
            "j2objc-annotations-1.3.jar",
            "avro-1.10.2.jar",
            "aircompressor-0.16.jar",
            "bouncy-castle-bc-2.8.0-pkg.jar",
            "jcip-annotations-1.0.jar",
            "pulsar-package-core-2.8.0.jar",
            "jsr305-3.0.2.jar",
            "commons-lang3-3.11.jar",
            "protokt-core-0.12.1.jar",
            "jackson-databind-2.12.2.jar",
            "jackson-annotations-2.12.2.jar",
            "avro-protobuf-1.10.2.jar",
            "pulsar-client-api-2.8.0.jar"
        )

        private val sl4jCommonFiles = listOf(
            "slf4j-api-1.7.30.jar",
            "slf4j-nop-1.7.30.jar"
        )

        private val pulsarAuthFiles = listOf(
            "pulsar-client-admin-2.8.0.jar"
        )

        private val protoktMessageFiles = listOf(
            "proto-google-common-protos-lite-0.12.1.jar",
            "protokt-core-lite-0.12.1.jar"
        )
    }
}
