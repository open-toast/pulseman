/*
 * Copyright (c) 2021 Toast Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.toasttab.pulseman.jars

import com.google.protobuf.GeneratedMessageV3
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.pulseman.pulsar.filters.AuthClassFilter
import com.toasttab.pulseman.pulsar.filters.protobuf.GeneratedMessageV3Filter
import com.toasttab.pulseman.pulsar.filters.protobuf.KTMessageFilter
import com.toasttab.pulseman.testjar.TestKtMessage
import io.mockk.spyk
import io.mockk.verify
import org.apache.pulsar.client.api.Authentication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URI

// If running these tests through an IDE
// Run "./gradlew createTestJar" to generate test jars before running this test.
// "./gradlew test" will run the createTestJar task first automatically
class LoadedClassesTest {

    private val jarFile = File("build/libs/test-class1.jar")
    private val runTimeJarLoader = RunTimeJarLoader().also {
        it.addJar(jarFile.toURI().toURL())
    }

    private val ktMessageFilter = spyk(KTMessageFilter(runTimeJarLoader = runTimeJarLoader))
    private val generatedMessageV3Filter = GeneratedMessageV3Filter(runTimeJarLoader = runTimeJarLoader)
    private val authClassFilter = AuthClassFilter()

    private val loadedClassesKTMessageFilter = LoadedClasses(
        classFilters = listOf(ktMessageFilter),
        runTimeJarLoader = runTimeJarLoader
    )
    private val loadedClassesGeneratedMessageV3Filter = LoadedClasses(
        classFilters = listOf(generatedMessageV3Filter),
        runTimeJarLoader = runTimeJarLoader
    )
    private val loadedClassesAuthClassFilter = LoadedClasses(
        classFilters = listOf(authClassFilter),
        runTimeJarLoader = runTimeJarLoader
    )
    private val loadedClassesMessageFilters = LoadedClasses(
        classFilters = listOf(ktMessageFilter, generatedMessageV3Filter),
        runTimeJarLoader = runTimeJarLoader
    )

    @Test
    fun `confirm KTMessageFilter successfully finds an entry`() {
        val clsList = loadedClassesKTMessageFilter.filter("")
        assertThat(clsList).hasSize(1)
        assertThat(KtMessage::class.java.isAssignableFrom(clsList[0].cls)).isTrue
    }

    @Test
    fun `confirm GeneratedMessageV3Filter successfully finds an entry`() {
        val clsList = loadedClassesGeneratedMessageV3Filter.filter("")
        assertThat(clsList).hasSize(1)
        assertThat(GeneratedMessageV3::class.java.isAssignableFrom(clsList[0].cls)).isTrue
    }

    @Test
    fun `confirm loading 2 message filters finds entries for both`() {
        val clsList = loadedClassesMessageFilters.filter("")
        assertThat(clsList).hasSize(2)
        val entry1isKT = KtMessage::class.java.isAssignableFrom(clsList[0].cls)
        val entry2isKT = KtMessage::class.java.isAssignableFrom(clsList[1].cls)
        val entry1isV3 = GeneratedMessageV3::class.java.isAssignableFrom(clsList[0].cls)
        val entry2isV3 = GeneratedMessageV3::class.java.isAssignableFrom(clsList[1].cls)
        assertThat(entry1isKT.xor(entry2isKT)).isTrue
        assertThat(entry1isV3.xor(entry2isV3)).isTrue
    }

    @Test
    fun `confirm AuthClassFilter successfully finds an entry`() {
        val clsList = loadedClassesAuthClassFilter.filter("")
        assertThat(clsList).hasSize(1)
        assertThat(Authentication::class.java.isAssignableFrom(clsList[0].cls)).isTrue
    }

    @Test
    fun `confirm we dont reload all classes every time filter is called`() {
        val jarLoader = RunTimeJarLoader()
        jarLoader.addJar(jarFile.toURI().toURL())
        val loadedClasses = LoadedClasses(
            classFilters = listOf(ktMessageFilter),
            runTimeJarLoader = jarLoader
        )
        // Filter once confirm queried the classes only once
        loadedClasses.filter("")
        verify(exactly = 1) {
            ktMessageFilter.getClasses(any())
        }

        // Filter again confirm we don't query the classes again and use the cached map of classes
        loadedClasses.filter("")
        verify(exactly = 1) {
            ktMessageFilter.getClasses(any())
        }

        // Add a fake url, so it has a new url list and needs to rebuild the classes
        jarLoader.addJar(URI("file:jar1").toURL())
        loadedClasses.filter("")
        verify(exactly = 3) {
            ktMessageFilter.getClasses(any())
        }

        // Filter again confirm we don't query the classes again and use the cached map of classes
        loadedClasses.filter("")
        verify(exactly = 3) {
            ktMessageFilter.getClasses(any())
        }
    }

    @Test
    fun `filtering by the name of a class returns the wanted entries`() {
        val clsListFoundAll = loadedClassesMessageFilters.filter("")
        assertThat(clsListFoundAll).hasSize(2)

        val clsListFiltered = loadedClassesMessageFilters.filter("Test")
        assertThat(clsListFiltered).hasSize(2)

        val clsListFilter1 = loadedClassesMessageFilters.filter("TestK")
        assertThat(clsListFilter1).hasSize(1)

        val clsListFilterAll = loadedClassesMessageFilters.filter("TestKP")
        assertThat(clsListFilterAll).hasSize(0)
    }

    @Test
    fun `filtering works from any part of the class name`() {
        val clsListFilter1 = loadedClassesMessageFilters.filter("stK")
        assertThat(clsListFilter1).hasSize(1)
    }

    @Test
    fun `getClass returns the needed class`() {
        val className = TestKtMessage::class.qualifiedName!!
        val foundClass = loadedClassesKTMessageFilter.getClass(className)
        assertThat(foundClass?.cls?.name).isEqualTo(className)
    }
}
