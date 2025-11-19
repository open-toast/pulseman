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

import com.toasttab.pulseman.entities.JarLoaderType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.URL

class RunTimeJarLoaderTest {

    @Test
    fun `Adding and removing a jar from RunTimeJarLoader is done correctly`() {
        val runTimeJarLoader = RunTimeJarLoader()
        runTimeJarLoader.addJar(url1)
        runTimeJarLoader.addJar(url2)

        val expectedURLs = mutableListOf(url1, url2)
        assertBaseUrls(runTimeJarLoader = runTimeJarLoader, expectedURLs = expectedURLs)
        assertGoogleUrls(runTimeJarLoader = runTimeJarLoader, expectedURLs = expectedURLs)
        assertProtoKTUrls(runTimeJarLoader = runTimeJarLoader, expectedURLs = expectedURLs)

        runTimeJarLoader.removeJar(url2)

        expectedURLs.remove(url2)
        assertBaseUrls(runTimeJarLoader = runTimeJarLoader, expectedURLs = expectedURLs)
        assertGoogleUrls(runTimeJarLoader = runTimeJarLoader, expectedURLs = expectedURLs)
        assertProtoKTUrls(runTimeJarLoader = runTimeJarLoader, expectedURLs = expectedURLs)
    }

    @Test
    fun `Removing a jar from a nested RunTimeJarLoader works as expected`() {
        val runTimeJarLoader1 = RunTimeJarLoader()
        runTimeJarLoader1.addJar(url1)

        val runTimeJarLoader2 = RunTimeJarLoader(dependentJarLoader = runTimeJarLoader1)
        runTimeJarLoader2.addJar(url2)

        val runTimeJarLoader3 = RunTimeJarLoader(dependentJarLoader = runTimeJarLoader2)
        runTimeJarLoader3.addJar(url3)

        val expectedURLs1 = listOf(url1)
        val expectedURLs2 = expectedURLs1 + url2
        val expectedURLs3 = expectedURLs2 + url3

        assertBaseUrls(runTimeJarLoader = runTimeJarLoader1, expectedURLs = expectedURLs1)
        assertGoogleUrls(runTimeJarLoader = runTimeJarLoader1, expectedURLs = expectedURLs1)
        assertProtoKTUrls(runTimeJarLoader = runTimeJarLoader1, expectedURLs = expectedURLs1)

        assertBaseUrls(runTimeJarLoader = runTimeJarLoader2, expectedURLs = expectedURLs2)
        assertGoogleUrls(runTimeJarLoader = runTimeJarLoader2, expectedURLs = expectedURLs2)
        assertProtoKTUrls(runTimeJarLoader = runTimeJarLoader2, expectedURLs = expectedURLs2)

        assertBaseUrls(runTimeJarLoader = runTimeJarLoader3, expectedURLs = expectedURLs3)
        assertGoogleUrls(runTimeJarLoader = runTimeJarLoader3, expectedURLs = expectedURLs3)
        assertProtoKTUrls(runTimeJarLoader = runTimeJarLoader3, expectedURLs = expectedURLs3)

        runTimeJarLoader1.removeJar(url1)

        val expectedURLs1AfterRemoval = emptyList<URL>()
        val expectedURLs2AfterRemoval = listOf(url2)
        val expectedURLs3AfterRemoval = expectedURLs2AfterRemoval + url3

        assertBaseUrls(runTimeJarLoader = runTimeJarLoader1, expectedURLs = expectedURLs1AfterRemoval)
        assertGoogleUrls(runTimeJarLoader = runTimeJarLoader1, expectedURLs = expectedURLs1AfterRemoval)
        assertProtoKTUrls(runTimeJarLoader = runTimeJarLoader1, expectedURLs = expectedURLs1AfterRemoval)

        assertBaseUrls(runTimeJarLoader = runTimeJarLoader2, expectedURLs = expectedURLs2AfterRemoval)
        assertGoogleUrls(runTimeJarLoader = runTimeJarLoader2, expectedURLs = expectedURLs2AfterRemoval)
        assertProtoKTUrls(runTimeJarLoader = runTimeJarLoader2, expectedURLs = expectedURLs2AfterRemoval)

        assertBaseUrls(runTimeJarLoader = runTimeJarLoader3, expectedURLs = expectedURLs3AfterRemoval)
        assertGoogleUrls(runTimeJarLoader = runTimeJarLoader3, expectedURLs = expectedURLs3AfterRemoval)
        assertProtoKTUrls(runTimeJarLoader = runTimeJarLoader3, expectedURLs = expectedURLs3AfterRemoval)
    }

    private fun assertBaseUrls(runTimeJarLoader: RunTimeJarLoader, expectedURLs: List<URL>) {
        val baseUrls = runTimeJarLoader.getJarLoader(jarLoaderType = JarLoaderType.BASE).urLs
        assertThat(baseUrls).hasSize(expectedURLs.size)
        assertContainsUrls(actualURLs = baseUrls, expectedURLs = expectedURLs)
    }

    private fun assertGoogleUrls(runTimeJarLoader: RunTimeJarLoader, expectedURLs: List<URL>) {
        val googleUrls = runTimeJarLoader.getJarLoader(jarLoaderType = JarLoaderType.GOOGLE_STANDARD).urLs
        assertThat(googleUrls).hasSize(expectedURLs.size + expectedGoogleURLs.size)
        assertContainsUrls(actualURLs = googleUrls, expectedURLs = expectedURLs)
        assertContainsUrlStrings(actualURLs = googleUrls, expectedURLs = expectedGoogleURLs)
    }

    private fun assertProtoKTUrls(runTimeJarLoader: RunTimeJarLoader, expectedURLs: List<URL>) {
        val protoKTUrls = runTimeJarLoader.getJarLoader(jarLoaderType = JarLoaderType.PROTOKT).urLs
        assertThat(protoKTUrls).hasSize(expectedURLs.size + expectedProtoKTURls.size)
        assertContainsUrls(actualURLs = protoKTUrls, expectedURLs = expectedURLs)
        assertContainsUrlStrings(actualURLs = protoKTUrls, expectedURLs = expectedProtoKTURls)
    }

    private fun assertContainsUrlStrings(actualURLs: Array<URL>, expectedURLs: List<String>) {
        expectedURLs.forEach { expectedUrl ->
            assertThat(actualURLs.singleOrNull { it.path.contains(expectedUrl) }).isNotNull()
        }
    }

    private fun assertContainsUrls(actualURLs: Array<URL>, expectedURLs: List<URL>) {
        expectedURLs.forEach { expectedUrl ->
            assertThat(actualURLs).contains(expectedUrl)
        }
    }

    companion object {
        private val url1 = URI("file:jar1").toURL()
        private val url2 = URI("file:jar2").toURL()
        private val url3 = URI("file:jar3").toURL()

        private const val GOOGLE_COMMON = "proto-google-common-protos-original.jar"
        private const val PROTOKT_COMMON = "proto-google-common-protos-protoKT.jar"
        private const val PROTOKT_COMMON_LITE = "proto-google-common-protos-lite-protoKT.jar"
        private const val PROTOKT_COMMON_EXTENSIONS_LITE = "proto-google-common-protos-extensions-lite-protoKT.jar"
        private val expectedGoogleURLs = listOf(GOOGLE_COMMON)
        private val expectedProtoKTURls = listOf(PROTOKT_COMMON, PROTOKT_COMMON_LITE, PROTOKT_COMMON_EXTENSIONS_LITE)
    }
}
