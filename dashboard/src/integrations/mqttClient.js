import mqtt from "mqtt";
import { mqttBrokerDefaults } from "./mqttTopics";

const JUNCTION_TOPICS = [
  "smart-ambulance/junctions/+/events",
  "smart-ambulance/junctions/+/signal",
  "smart-ambulance/junctions/+/approach",
  "smart-ambulance/ambulances/+/lora-gps",
];

function parsePayload(payload) {
  try {
    return JSON.parse(payload.toString());
  } catch {
    return null;
  }
}

export function subscribeToMqtt({ onStatus, onEvent, onSignal, onTelemetry }) {
  const url = import.meta.env.VITE_MQTT_URL || mqttBrokerDefaults.publicTestWebSocketUrl;
  const client = mqtt.connect(url, {
    clientId: `smart-ambulance-dash-${Math.random().toString(16).slice(2, 10)}`,
    clean: true,
    reconnectPeriod: 4000,
    connectTimeout: 8000,
    protocolVersion: 4,
  });

  client.on("connect", () => {
    onStatus?.("live");
    JUNCTION_TOPICS.forEach((topic) => client.subscribe(topic));
  });
  client.on("reconnect", () => onStatus?.("connecting"));
  client.on("offline", () => onStatus?.("offline"));
  client.on("close", () => onStatus?.("offline"));
  client.on("error", () => onStatus?.("error"));

  client.on("message", (topic, payload) => {
    const data = parsePayload(payload);
    if (!data) return;

    if (topic.endsWith("/events")) onEvent?.(data, topic);
    else if (topic.endsWith("/signal")) onSignal?.(data, topic);
    else onTelemetry?.(data, topic);
  });

  return () => {
    client.end(true);
  };
}
