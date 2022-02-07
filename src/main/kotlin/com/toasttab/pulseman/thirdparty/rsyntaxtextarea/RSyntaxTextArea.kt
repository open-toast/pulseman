/*
 * Copyright (c) 2019, Robert Futrell
 * All rights reserved.
 *
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

package com.toasttab.pulseman.thirdparty.rsyntaxtextarea

import com.toasttab.pulseman.AppResources.RSYNTAX_THEME
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.Theme
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * Reusable UI components
 */
object RSyntaxTextArea {

    fun textArea(initialText: String, syntaxStyle: String, onChange: () -> Unit) =
        RSyntaxTextArea(initialText).let { textArea ->
            textArea.syntaxEditingStyle = syntaxStyle
            Theme.load(javaClass.getResourceAsStream(RSYNTAX_THEME))
                .apply { apply(textArea) }
            textArea.document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) {}
                override fun removeUpdate(e: DocumentEvent?) {}
                override fun changedUpdate(e: DocumentEvent?) {
                    onChange()
                }
            })
            textArea
        }
}
