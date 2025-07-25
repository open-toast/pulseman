package com.toasttab.pulseman.state

import androidx.compose.runtime.MutableState
import com.toasttab.pulseman.entities.ClassInfo
import com.toasttab.pulseman.files.FileManagement
import com.toasttab.pulseman.jars.JarManager
import com.toasttab.pulseman.scripting.GradleScripting
import java.io.File

class GradleRunner(
    private val setUserFeedback: (String) -> Unit,
    private val commonJarManager: JarManager<ClassInfo>?,
    private val pulsarJarManagers: List<JarManager<out ClassInfo>>,
    private val onChange: () -> Unit,
    private val taskName: String,
    private val filterPulsarJars: MutableState<Boolean>,
    private val fileManagement: FileManagement,
    private val projectDir: File = fileManagement.appFolder,
    private val javaHome: MutableState<String>
) {

    fun runGradleTask(gradleScript: String) {
        val urls = GradleScripting.downloadJars(
            projectDir = projectDir,
            taskName = taskName,
            gradleScript = gradleScript,
            javaHome = javaHome.value,
            setUserFeedback = setUserFeedback
        )
        // commonJarManager must be last in the list so that jars can be added to the filtered lists first
        val jarManagers = if (filterPulsarJars.value) {
            pulsarJarManagers
        } else {
            commonJarManager?.let { pulsarJarManagers + commonJarManager } ?: pulsarJarManagers
        }
        urls.forEach urlForeach@{ url ->
            jarManagers.forEach { jarManager ->
                if (jarManager.loadedClasses.doesJarContainValidClasses(url = url)) {
                    val file = File(url.toURI())
                    val newFile = jarManager.copyFile(file = file)
                    jarManager.addJar(
                        file = newFile,
                        setUserFeedback = setUserFeedback,
                        onChange = onChange
                    )
                    return@urlForeach
                }
            }
            setUserFeedback("Skipping jar, no pulsar classes. File:$url")
        }

        GradleScripting.cleanUp(projectDir = projectDir)
    }
}
