package com.smartambulance.driver.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.UUID

class MqttManager(context: Context) {
    private val applicationContext = context.applicationContext
    private var mqttClient: MqttClient? = null
    private val clientId = "smart-ambulance-android-${UUID.randomUUID().toString().take(8)}"
    
    companion object {
        private const val TAG = "MqttManager"
        private const val BROKER_URL = "tcp://broker.hivemq.com:1883"
        private const val CONNECTION_TIMEOUT = 30
        private const val KEEP_ALIVE_INTERVAL = 60
    }
    
    private var onMessageReceived: ((topic: String, message: String) -> Unit)? = null
    private var onConnectionStatusChanged: ((connected: Boolean) -> Unit)? = null
    
    fun setOnMessageReceived(callback: (topic: String, message: String) -> Unit) {
        onMessageReceived = callback
    }
    
    fun setOnConnectionStatusChanged(callback: (connected: Boolean) -> Unit) {
        onConnectionStatusChanged = callback
    }
    
    fun connect() {
        try {
            val persistence = MemoryPersistence()
            mqttClient = MqttClient(BROKER_URL, clientId, persistence)
            
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = CONNECTION_TIMEOUT
                keepAliveInterval = KEEP_ALIVE_INTERVAL
                isAutomaticReconnect = true
            }
            
            mqttClient?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e(TAG, "MQTT connection lost", cause)
                    onConnectionStatusChanged?.invoke(false)
                }
                
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    topic?.let { t ->
                        message?.let { m ->
                            val payload = String(m.payload)
                            Log.d(TAG, "Message arrived on topic: $t, payload: $payload")
                            onMessageReceived?.invoke(t, payload)
                        }
                    }
                }
                
                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d(TAG, "Message delivery complete")
                }
            })
            
            mqttClient?.connect(options)
            Log.d(TAG, "MQTT connection initiated")
            onConnectionStatusChanged?.invoke(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to MQTT broker", e)
            onConnectionStatusChanged?.invoke(false)
        }
    }
    
    fun disconnect() {
        try {
            mqttClient?.disconnect()
            mqttClient?.close()
            Log.d(TAG, "MQTT disconnected")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting MQTT", e)
        }
    }
    
    fun subscribe(topic: String) {
        try {
            mqttClient?.subscribe(topic, 1)
            Log.d(TAG, "Subscribed to topic: $topic")
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to topic: $topic", e)
        }
    }
    
    fun publish(topic: String, message: String) {
        try {
            val mqttMessage = MqttMessage(message.toByteArray()).apply {
                qos = 1
                isRetained = false
            }
            
            mqttClient?.publish(topic, mqttMessage)
            Log.d(TAG, "Message published to topic: $topic")
        } catch (e: Exception) {
            Log.e(TAG, "Error publishing message to topic: $topic", e)
        }
    }
    
    fun subscribeToAmbulanceTopics(ambulanceId: String) {
        subscribe(MqttTopics.ambulanceLoRaGps(ambulanceId))
        subscribe(MqttTopics.ambulanceStatus(ambulanceId))
    }
    
    fun subscribeToJunctionTopics(junctionId: String) {
        subscribe(MqttTopics.junctionApproach(junctionId))
        subscribe(MqttTopics.junctionEvents(junctionId))
        subscribe(MqttTopics.junctionSignal(junctionId))
    }
    
    fun isConnected(): Boolean {
        return mqttClient?.isConnected == true
    }
}