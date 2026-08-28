package com.smartambulance.driver.mqtt

object MqttTopics {
    fun ambulanceLoRaGps(ambulanceId: String) = "smart-ambulance/ambulances/$ambulanceId/lora-gps"
    fun junctionApproach(junctionId: String) = "smart-ambulance/junctions/$junctionId/approach"
    fun ambulanceStatus(ambulanceId: String) = "smart-ambulance/ambulances/$ambulanceId/status"
    fun tripEvents(tripId: String) = "smart-ambulance/trips/$tripId/events"
    fun junctionEvents(junctionId: String) = "smart-ambulance/junctions/$junctionId/events"
    fun junctionSignal(junctionId: String) = "smart-ambulance/junctions/$junctionId/signal"
}
