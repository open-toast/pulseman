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

package com.toasttab.pulseman.testjar

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Message

/**
 * Do not delete, this class is used to generate a JAR used in the LoadedClassesTest tests
 *
 * The createTestJar gradle task creates the JAR file.
 */
class TestGeneratedMessageV3() : GeneratedMessageV3() {
    override fun getDefaultInstanceForType(): Message {
        TODO("Not yet implemented")
    }

    override fun newBuilderForType(parent: BuilderParent?): Message.Builder {
        TODO("Not yet implemented")
    }

    override fun newBuilderForType(): Message.Builder {
        TODO("Not yet implemented")
    }

    override fun toBuilder(): Message.Builder {
        TODO("Not yet implemented")
    }

    override fun internalGetFieldAccessorTable(): FieldAccessorTable {
        TODO("Not yet implemented")
    }
}
