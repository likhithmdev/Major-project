/*
  ESP32 LoRa Receiver for Smart Ambulance System
  Receives GPS data from ambulance via LoRa, calculates distance/bearing,
  sends commands to Arduino traffic controller via UART, and uploads to Firebase

  Hardware:
  - LoRa SX1278 (433MHz):
    * VCC: 3.3V
    * GND: GND
    * MISO: GPIO 19
    * MOSI: GPIO 23
    * SCK: GPIO 18
    * NSS (CS): GPIO 5
    * RST: GPIO 14
    * DIO0: GPIO 2
  - UART to Arduino (Serial2):
    * TX2 (GPIO 17): Connect to Arduino RX
    * RX2 (GPIO 16): Connect to Arduino TX
  - WiFi (built-in):
    * Connect to WiFi network for Firebase integration

  Commands sent to Arduino via UART:
  - "AMBULANCE_APPROACH,<signal_id>" - Ambulance approaching, preempt specified signal
  - "AMBULANCE_EXIT" - Ambulance cleared junction, restore normal traffic
  - "AMBULANCE_OUT_OF_RANGE" - Ambulance out of range, normal operations

  Firebase Integration:
  - Uploads LoRa telemetry to Firebase Realtime Database
  - Path: loraTelemetry/JNC001/{ambulanceId}
  - Rate limited to 2 seconds between uploads

  Configuration Required:
  - WIFI_SSID: Your WiFi network name
  - WIFI_PASSWORD: Your WiFi password
  - FIREBASE_HOST: Your Firebase project host
  - FIREBASE_AUTH: Your Firebase database secret

  Note: Expects JSON packets from ambulance: {"ambulanceId":"AMB001","tripId":"TRIP001","lat":12.975,"lng":77.5946,...}
*/

#include <SPI.h>
#include <LoRa.h>
#include <ArduinoJson.h>
#include <math.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>

// LoRa SPI Pins
#define LORA_SS    5
#define LORA_RST   14
#define LORA_DIO0  2

// UART to Arduino (Hardware Serial 2)
// ESP32 TX2 (GPIO 17) → Arduino RX (Pin 0)
// ESP32 RX2 (GPIO 16) → Arduino TX (Pin 1)
#define ARDUINO_UART Serial2
#define ARDUINO_BAUD 9600
#define ARDUINO_TX_PIN 17
#define ARDUINO_RX_PIN 16

// WiFi Configuration
// SECURITY NOTE: These credentials are for development only. For production,
// consider using environment variables or a separate secrets.h file.
const char* WIFI_SSID = "Kamlesh";
const char* WIFI_PASSWORD = "12345678";

// Firebase Configuration
// SECURITY NOTE: The database secret should be kept secure and rotated regularly.
const char* FIREBASE_HOST = "smart-ambulance-36f9d-default-rtdb.firebaseio.com";
const char* FIREBASE_AUTH = "GqHTgXInKc0BSkmZXhoLyyNYObY4Vtr1cywYdD1i";

// Junction Reference Coordinates
const float JUNCTION_LAT = 13.013123; 
const float JUNCTION_LON = 77.629112; 
const float TRIGGER_DISTANCE = 500.0; // Activation threshold in meters

// Ambulance tracking state
bool ambulanceInZone = false;
bool approachingSignal = false;
unsigned long lastPacketTime = 0;
const unsigned long PACKET_TIMEOUT_MS = 5000; // 5 seconds without packet = out of range

// LoRa data structure for Firebase
struct LoRaData {
  String ambulanceId;
  String tripId;
  double lat;
  double lng;
  float speedKmph;
  float headingDeg;
  bool gpsFix;
  float distanceMeters;
  float bearingToJunction;
  unsigned long timestamp;
};

// Firebase state
bool wifiConnected = false;
unsigned long lastFirebaseUpload = 0;
const unsigned long FIREBASE_UPLOAD_INTERVAL_MS = 2000; // Upload every 2 seconds

float toRadians(float deg) { return deg * PI / 180.0; }
float toDegrees(float rad) { return rad * 180.0 / PI; }

// Haversine Distance Formula
float getDistance(float lat1, float lon1, float lat2, float lon2) {
    float dLat = toRadians(lat2 - lat1);
    float dLon = toRadians(lon2 - lon1);
    float a = sin(dLat/2) * sin(dLat/2) + cos(toRadians(lat1)) * cos(toRadians(lat2)) * sin(dLon/2) * sin(dLon/2);
    return 6371000.0 * (2 * atan2(sqrt(a), sqrt(1-a)));
}

// Bearing Angle Heading Calculator
float getBearing(float lat1, float lon1, float lat2, float lon2) {
    float dLon = toRadians(lon2 - lon1);
    float y = sin(dLon) * cos(toRadians(lat2));
    float x = cos(toRadians(lat1)) * sin(toRadians(lat2)) - sin(toRadians(lat1)) * cos(toRadians(lat2)) * cos(dLon);
    float brng = toDegrees(atan2(y, x));
    return (brng < 0) ? brng + 360.0 : brng;
}

// Determine which signal to preempt based on angle
int getSignalFromAngle(float angle) {
    if (angle >= 315 || angle < 45) {
        return 0; // NORTH
    } else if (angle >= 45 && angle < 135) {
        return 1; // EAST
    } else if (angle >= 135 && angle < 225) {
        return 2; // SOUTH
    } else {
        return 3; // WEST
    }
}

void parseLaneDirection(float angle) {
    if (angle >= 315 || angle < 45) {
        Serial.println("🟢 SYSTEM DECISION: Clear NORTH Lane!");
    } else if (angle >= 45 && angle < 135) {
        Serial.println("🟢 SYSTEM DECISION: Clear EAST Lane!");
    } else if (angle >= 135 && angle < 225) {
        Serial.println("🟢 SYSTEM DECISION: Clear SOUTH Lane!");
    } else if (angle >= 225 && angle < 315) {
        Serial.println("🟢 SYSTEM DECISION: Clear WEST Lane!");
    }
}

void sendToArduino(const char* command) {
    ARDUINO_UART.println(command);
    Serial.print("Sent to Arduino: ");
    Serial.println(command);
}

void sendToFirebase(LoRaData data) {
    if (!wifiConnected) return;
    
    WiFiClientSecure client;
    client.setInsecure(); // For HTTPS (use certificate in production)
    HTTPClient http;
    
    // Upload to loraTelemetry path (junction-specific telemetry)
    String loraPath = "loraTelemetry/JNC001/" + data.ambulanceId;
    String loraUrl = "https://" + String(FIREBASE_HOST) + "/" + loraPath + ".json";
    
    if (http.begin(client, loraUrl)) {
        http.addHeader("Content-Type", "application/json");
        http.setTimeout(2000);
        
        StaticJsonDocument<512> doc;
        doc["ambulanceId"] = data.ambulanceId;
        doc["tripId"] = data.tripId;
        doc["lat"] = data.lat;
        doc["lng"] = data.lng;
        doc["speedKmph"] = data.speedKmph;
        doc["headingDeg"] = data.headingDeg;
        doc["gpsFix"] = data.gpsFix;
        doc["distanceMeters"] = data.distanceMeters;
        doc["bearingToJunctionDeg"] = data.bearingToJunction;
        doc["approaching"] = data.distanceMeters <= 500.0;
        doc["preemptionEligible"] = data.distanceMeters <= 500.0;
        doc["timestamp"] = data.timestamp;
        doc["updatedAt"] = millis();
        
        String payload;
        serializeJson(doc, payload);
        
        int httpResponseCode = http.PATCH(payload);
        if (httpResponseCode > 0) {
            Serial.print("Firebase LoRa response: ");
            Serial.println(httpResponseCode);
        } else {
            Serial.print("Firebase LoRa error: ");
            Serial.println(http.errorToString(httpResponseCode));
        }
        http.end();
    }
    
    // Upload to ambulances path (general ambulance location for mobile app)
    String ambulancePath = "ambulances/" + data.ambulanceId + "/lastLocation";
    String ambulanceUrl = "https://" + String(FIREBASE_HOST) + "/" + ambulancePath + ".json";
    
    if (http.begin(client, ambulanceUrl)) {
        http.addHeader("Content-Type", "application/json");
        http.setTimeout(2000);
        
        StaticJsonDocument<256> locationDoc;
        locationDoc["lat"] = data.lat;
        locationDoc["lng"] = data.lng;
        locationDoc["updatedAt"] = millis();
        
        String locationPayload;
        serializeJson(locationDoc, locationPayload);
        
        int httpResponseCode = http.PATCH(locationPayload);
        if (httpResponseCode > 0) {
            Serial.print("Firebase ambulance location response: ");
            Serial.println(httpResponseCode);
        } else {
            Serial.print("Firebase ambulance location error: ");
            Serial.println(http.errorToString(httpResponseCode));
        }
        http.end();
    }
}

void handleWiFi() {
    if (WiFi.status() != WL_CONNECTED) {
        wifiConnected = false;
        static unsigned long lastWifiAttempt = 0;
        if (millis() - lastWifiAttempt > 10000) {
            lastWifiAttempt = millis();
            Serial.println("Attempting to reconnect to WiFi...");
            WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
        }
    } else {
        if (!wifiConnected) {
            wifiConnected = true;
            Serial.println("WiFi connected!");
            Serial.print("IP address: ");
            Serial.println(WiFi.localIP());
        }
    }
}

void setup() {
    Serial.begin(115200);
    while (!Serial);

    // Initialize UART to Arduino (TX2=GPIO 17, RX2=GPIO 16)
    ARDUINO_UART.begin(ARDUINO_BAUD, SERIAL_8N1, ARDUINO_RX_PIN, ARDUINO_TX_PIN);

    Serial.println("--- ESP32 LoRa Receiver for Smart Ambulance ---");
    Serial.println("Initializing WiFi...");

    // Initialize WiFi
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print("Connecting to WiFi");
    int wifiAttempts = 0;
    while (WiFi.status() != WL_CONNECTED && wifiAttempts < 20) {
        delay(500);
        Serial.print(".");
        wifiAttempts++;
    }
    
    if (WiFi.status() == WL_CONNECTED) {
        wifiConnected = true;
        Serial.println(" connected!");
        Serial.print("IP address: ");
        Serial.println(WiFi.localIP());
    } else {
        Serial.println(" failed. Will retry later.");
    }

    Serial.println("Initializing LoRa Receiver Module...");

    // Initialize LoRa Receiver Module
    LoRa.setPins(LORA_SS, LORA_RST, LORA_DIO0);
    if (!LoRa.begin(433E6)) { 
        Serial.println("❌ LoRa Receiver Init Failed!"); 
        while(1); 
    }
    Serial.println("✅ LoRa Receiver Active. Listening for ambulance data...");
    Serial.println("✅ UART to Arduino ready at 9600 baud");
    Serial.println("✅ Firebase integration ready (when WiFi connected)");
}

void loop() {
    // Handle WiFi connection
    handleWiFi();
    
    int packetSize = LoRa.parsePacket();
    if (packetSize) {
        String packetData = "";
        while (LoRa.available()) { packetData += (char)LoRa.read(); }
        
        // Update last packet time
        lastPacketTime = millis();

        // Parse JSON packet format: {"ambulanceId":"AMB001","tripId":"TRIP001","lat":12.975,"lng":77.5946,...}
        StaticJsonDocument<256> doc;
        DeserializationError error = deserializeJson(doc, packetData);
        
        if (!error) {
            String ambulanceId = doc["ambulanceId"] | "";
            String tripId = doc["tripId"] | "";
            float ambLat = doc["lat"] | 0.0;
            float ambLon = doc["lng"] | 0.0;
            float ambSpeed = doc["speedKmph"] | 0.0;
            float headingDeg = doc["headingDeg"] | 0.0;
            bool gpsFix = doc["gpsFix"] | false;

            // Calculate current metrics
            float distance = getDistance(JUNCTION_LAT, JUNCTION_LON, ambLat, ambLon);
            float angle = getBearing(JUNCTION_LAT, JUNCTION_LON, ambLat, ambLon);
            
            Serial.println("\n📡 --- WIRELESS DATA PACKET RECOVERY ---");
            Serial.print("Ambulance ID: "); Serial.println(ambulanceId);
            Serial.print("Trip ID: "); Serial.println(tripId);
            Serial.print("Raw Data: "); Serial.println(packetData);
            Serial.print("GPS Fix: "); Serial.println(gpsFix ? "YES" : "NO");
            Serial.print("Calculated Distance: "); Serial.print(distance, 2); Serial.println(" meters");
            Serial.print("Calculated Angle   : "); Serial.print(angle, 2); Serial.println(" degrees");

            // Create LoRa data structure for Firebase
            LoRaData data;
            data.ambulanceId = ambulanceId;
            data.tripId = tripId;
            data.lat = ambLat;
            data.lng = ambLon;
            data.speedKmph = ambSpeed;
            data.headingDeg = headingDeg;
            data.gpsFix = gpsFix;
            data.distanceMeters = distance;
            data.bearingToJunction = angle;
            data.timestamp = millis();

            // Send to Firebase (with rate limiting)
            if (millis() - lastFirebaseUpload > FIREBASE_UPLOAD_INTERVAL_MS) {
                lastFirebaseUpload = millis();
                sendToFirebase(data);
            }

            // Evaluate if preemption threshold is breached
            if (distance <= TRIGGER_DISTANCE) {
                Serial.println("⚠️ AMBULANCE WITHIN TRIGGER ZONE!");
                parseLaneDirection(angle);

                int signalId = getSignalFromAngle(angle);
                
                // Send command to Arduino only if not already in zone
                if (!ambulanceInZone) {
                    ambulanceInZone = true;
                    approachingSignal = true;
                    char command[32];
                    snprintf(command, sizeof(command), "AMBULANCE_APPROACH,%d", signalId);
                    sendToArduino(command);
                }
            } else {
                Serial.println("ℹ️ Ambulance detected out of range. Normal operations.");
                
                // Send out of range command if was in zone
                if (ambulanceInZone) {
                    ambulanceInZone = false;
                    approachingSignal = false;
                    sendToArduino("AMBULANCE_OUT_OF_RANGE");
                }
            }
            Serial.println("---------------------------------------");
        } else {
            Serial.print("❌ JSON Parse Error: ");
            Serial.println(error.c_str());
            Serial.print("Raw packet: ");
            Serial.println(packetData);
        }
    }

    // Check for packet timeout (ambulance out of range)
    if (ambulanceInZone && millis() - lastPacketTime > PACKET_TIMEOUT_MS) {
        Serial.println("⏱️ Packet timeout - ambulance out of range");
        ambulanceInZone = false;
        approachingSignal = false;
        sendToArduino("AMBULANCE_OUT_OF_RANGE");
    }
}
