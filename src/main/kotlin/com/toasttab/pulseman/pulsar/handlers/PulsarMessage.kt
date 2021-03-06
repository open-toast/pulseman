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

package com.toasttab.pulseman.pulsar.handlers

/**
 * Defines an interface for serializing and deserializing pulsar messages
 *
 * TODO make this interface more type safe
 */
interface PulsarMessage {
    /**
     * Serializes a class to a byte array
     *
     * @param cls the object that will be serialized.
     * @return A array of bytes containing the cls info
     */
    fun serialize(cls: Any): ByteArray

    /**
     * Deserializes a byte array to a class
     *
     * @param bytes the byte array that will be deserialized.
     * @return The deserialized byte array information
     */
    fun deserialize(bytes: ByteArray): Any

    /**
     * Prints the class in a custom format
     *
     * @param cls the class to be printed
     * @return The string version of the class
     */
    fun prettyPrint(cls: Any): String
}
