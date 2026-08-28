/*
  Smart Ambulance GPS-LoRa transmitter

  Broadcasts the same JSON the junction firmware already parses.
  Works with a Neo-6M GPS module, or with Serial simulation if GPS is not wired yet.

  Libraries to install in Arduino IDE:
  - LoRa by Sandeep Mistry
  - TinyGPSPlus by Mikal Hart
  - ArduinoJson by Benoit Blanchon

  Serial commands (115200):
    EMERGENCY ON
    EMERGENCY OFF
    SIM 12.9750,77.5946,185,42
    GPS OFF
    GPS ON
    STATUS
    SET_AMB AMB002
    SET_TRIP TRIP002
*/

#include <Arduino.h>
#include <SPI.h>
#include <LoRa.h>
#include <TinyGPS++.h>
#include <ArduinoJson.h>

// Configurable ambulance and trip IDs (can be changed via Serial commands)
String AMBULANCE_ID = "AMB001";
String TRIP_ID = "TRIP001";

const int LORA_SS_PIN = 5;
const int LORA_RST_PIN = 14;
const int LORA_DIO0_PIN = 26;
const int EMERGENCY_BUTTON_PIN = 4;
const int GPS_RX_PIN = 16;
const int GPS_TX_PIN = 17;

const unsigned long BROADCAST_INTERVAL_MS = 1000;
const bool START_IN_EMERGENCY = true;

TinyGPSPlus gps;
HardwareSerial gpsSerial(2);

bool emergencyActive = START_IN_EMERGENCY;
bool allowGpsFix = true;
bool loraReady = false;
bool usingSimulatedFix = false;

double simLat = 12.9750;
double simLng = 77.5946;
float simHeadingDeg = 185.0;
float simSpeedKmph = 42.0;

unsigned long lastBroadcastAt = 0;
bool lastButtonState = true;

String buildPacket(bool gpsFix, double lat, double lng, float speedKmph, float headingDeg) {
  StaticJsonDocument<256> doc;
  doc["ambulanceId"] = AMBULANCE_ID;
  doc["tripId"] = TRIP_ID;
  doc["lat"] = lat;
  doc["lng"] = lng;
  doc["speedKmph"] = speedKmph;
  doc["headingDeg"] = headingDeg;
  doc["gpsFix"] = gpsFix;
  doc["timestamp"] = millis();

  String payload;
  serializeJson(doc, payload);
  return payload;
}

void broadcastPacket() {
  if (!emergencyActive) return;

  bool gpsFix = false;
  double lat = simLat;
  double lng = simLng;
  float speedKmph = simSpeedKmph;
  float headingDeg = simHeadingDeg;

  if (allowGpsFix && gps.location.isValid()) {
    gpsFix = true;
    usingSimulatedFix = false;
    lat = gps.location.lat();
    lng = gps.location.lng();
    speedKmph = gps.speed.isValid() ? gps.speed.kmph() : 0.0;
    headingDeg = gps.course.isValid() ? gps.course.deg() : headingDeg;
  } else if (allowGpsFix && usingSimulatedFix) {
    gpsFix = true;
  }

  String payload = buildPacket(gpsFix, lat, lng, speedKmph, headingDeg);

  if (loraReady) {
    LoRa.beginPacket();
    LoRa.print(payload);
    LoRa.endPacket();
  }

  Serial.print("[TX] ");
  Serial.println(payload);
}

void printStatus() {
  Serial.print("emergency=");
  Serial.print(emergencyActive ? "ON" : "OFF");
  Serial.print(" gpsAllowed=");
  Serial.print(allowGpsFix ? "yes" : "no");
  Serial.print(" simFix=");
  Serial.print(usingSimulatedFix ? "yes" : "no");
  Serial.print(" lora=");
  Serial.println(loraReady ? "ready" : "offline");
}

void handleSerial() {
  if (!Serial.available()) return;

  String line = Serial.readStringUntil('\n');
  line.trim();
  if (line.length() == 0) return;

  if (line.equalsIgnoreCase("EMERGENCY ON")) {
    emergencyActive = true;
    Serial.println("Emergency broadcast started.");
    return;
  }

  if (line.equalsIgnoreCase("EMERGENCY OFF")) {
    emergencyActive = false;
    Serial.println("Emergency broadcast stopped.");
    return;
  }

  if (line.equalsIgnoreCase("GPS OFF")) {
    allowGpsFix = false;
    usingSimulatedFix = false;
    Serial.println("GPS marked unhealthy. Junction can use RSSI fallback.");
    return;
  }

  if (line.equalsIgnoreCase("GPS ON")) {
    allowGpsFix = true;
    Serial.println("GPS enabled.");
    return;
  }

  if (line.equalsIgnoreCase("STATUS")) {
    printStatus();
    return;
  }

  if (line.startsWith("SIM ")) {
    float lat = simLat;
    float lng = simLng;
    float heading = simHeadingDeg;
    float speed = simSpeedKmph;
    int parsed = sscanf(line.c_str(), "SIM %f,%f,%f,%f", &lat, &lng, &heading, &speed);
    if (parsed >= 2) {
      simLat = lat;
      simLng = lng;
      if (parsed >= 3) simHeadingDeg = heading;
      if (parsed >= 4) simSpeedKmph = speed;
      usingSimulatedFix = true;
      allowGpsFix = true;
      emergencyActive = true;
      Serial.println("Simulated GPS loaded and emergency started.");
      broadcastPacket();
    } else {
      Serial.println("Usage: SIM lat,lng,heading,speed");
    }
  }

  if (line.startsWith("SET_AMB ")) {
    AMBULANCE_ID = line.substring(8);
    Serial.print("Ambulance ID set to ");
    Serial.println(AMBULANCE_ID);
    return;
  }

  if (line.startsWith("SET_TRIP ")) {
    TRIP_ID = line.substring(9);
    Serial.print("Trip ID set to ");
    Serial.println(TRIP_ID);
    return;
  }
}

void handleEmergencyButton() {
  bool pressed = digitalRead(EMERGENCY_BUTTON_PIN) == LOW;
  if (pressed && lastButtonState) {
    emergencyActive = !emergencyActive;
    Serial.print("Button toggled emergency=");
    Serial.println(emergencyActive ? "ON" : "OFF");
    delay(250);
  }
  lastButtonState = !pressed;
}

void setup() {
  Serial.begin(115200);
  pinMode(EMERGENCY_BUTTON_PIN, INPUT_PULLUP);
  gpsSerial.begin(9600, SERIAL_8N1, GPS_RX_PIN, GPS_TX_PIN);

  LoRa.setPins(LORA_SS_PIN, LORA_RST_PIN, LORA_DIO0_PIN);
  loraReady = LoRa.begin(433E6);
  if (!loraReady) {
    Serial.println("LoRa start failed. Serial SIM still works for bench tests.");
  } else {
    Serial.println("LoRa transmitter ready.");
  }

  Serial.println("Ambulance GPS-LoRa unit ready.");
  Serial.println("Commands: EMERGENCY ON | EMERGENCY OFF | SIM lat,lng,heading,speed | GPS OFF | GPS ON | STATUS");
  printStatus();
}

void loop() {
  while (gpsSerial.available() > 0) {
    gps.encode(gpsSerial.read());
  }

  handleSerial();
  handleEmergencyButton();

  unsigned long now = millis();
  if (now - lastBroadcastAt >= BROADCAST_INTERVAL_MS) {
    lastBroadcastAt = now;
    broadcastPacket();
  }
}
