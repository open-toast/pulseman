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

package com.toasttab.pulseman

import org.apache.pulsar.client.api.Message

/**
 * A simple implementation of Pulsar's Message interface for use in tests.
 */
class TestMessage(
    private val messageProperties: Map<String, String> = mapOf("environment" to "test"),
    private val messageData: ByteArray = ByteArray(10),
    private val messagePublishTime: Long = 1234567890000L,
) : Message<ByteArray> {
    override fun getProperties(): Map<String, String> = messageProperties
    override fun getData(): ByteArray = messageData
    override fun getPublishTime(): Long = messagePublishTime

    // All other methods throw UnsupportedOperationException since we don't use them in tests
    override fun getMessageId() = throw UnsupportedOperationException()
    override fun getTopicName() = throw UnsupportedOperationException()
    override fun getProducerName() = throw UnsupportedOperationException()
    override fun getEventTime() = throw UnsupportedOperationException()
    override fun getSequenceId() = throw UnsupportedOperationException()
    override fun getOrderingKey() = throw UnsupportedOperationException()
    override fun getKey() = throw UnsupportedOperationException()
    override fun hasKey() = throw UnsupportedOperationException()
    override fun hasOrderingKey() = throw UnsupportedOperationException()
    override fun getValue() = throw UnsupportedOperationException()
    override fun getReaderSchema() = throw UnsupportedOperationException()
    override fun hasBase64EncodedKey() = throw UnsupportedOperationException()
    override fun getKeyBytes() = throw UnsupportedOperationException()
    override fun getReplicatedFrom() = throw UnsupportedOperationException()
    override fun getBrokerPublishTime() = throw UnsupportedOperationException()
    override fun getIndex() = throw UnsupportedOperationException()
    override fun hasProperty(name: String) = throw UnsupportedOperationException()
    override fun getProperty(name: String) = throw UnsupportedOperationException()
    override fun getSchemaVersion() = throw UnsupportedOperationException()
    override fun size() = throw UnsupportedOperationException()
    override fun getEncryptionCtx() = throw UnsupportedOperationException()
    override fun getRedeliveryCount() = throw UnsupportedOperationException()
    override fun isReplicated() = throw UnsupportedOperationException()
    override fun release() = throw UnsupportedOperationException()
    override fun hasBrokerPublishTime() = throw UnsupportedOperationException()
    override fun hasIndex() = throw UnsupportedOperationException()
}
