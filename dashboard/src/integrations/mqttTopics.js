export const mqttTopics = {
  ambulanceLoRaGps: (ambulanceId) => `smart-ambulance/ambulances/${ambulanceId}/lora-gps`,
  junctionApproach: (junctionId) => `smart-ambulance/junctions/${junctionId}/approach`,
  junctionEvents: (junctionId) => `smart-ambulance/junctions/${junctionId}/events`,
  junctionSignal: (junctionId) => `smart-ambulance/junctions/${junctionId}/signal`,
  ambulanceStatus: (ambulanceId) => `smart-ambulance/ambulances/${ambulanceId}/status`,
  tripEvents: (tripId) => `smart-ambulance/trips/${tripId}/events`,
};

export const mqttBrokerDefaults = {
  localWebSocketUrl: "ws://localhost:9001",
  publicTestWebSocketUrl: "wss://broker.hivemq.com:8884/mqtt",
};
