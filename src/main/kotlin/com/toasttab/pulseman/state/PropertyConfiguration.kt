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

package com.toasttab.pulseman.state

import com.toasttab.pulseman.entities.TabValuesV2
import com.toasttab.pulseman.thirdparty.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane

class PropertyConfiguration(
    onChange: () -> Unit,
    initialSettings: TabValuesV2?
) {
    // TODO allow comments in json dialogs
    private val defaultJsonParameters = "{\n}"

    private val textArea =
        RSyntaxTextArea.textArea(
            initialSettings?.propertyMap ?: defaultJsonParameters,
            SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS,
            onChange
        )

    val sp = RTextScrollPane(textArea)

    fun propertyMap(): String = textArea.text
}
