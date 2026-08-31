# Smart Ambulance 3-Device Hardware Setup Guide

## Architecture Overview

Your system now uses **3 separate devices**:

1. **Ambulance ESP32** (Transmitter) - LoRa + GPS + LED + Buzzer + Button
2. **ESP32 LoRa Receiver** - Receives LoRa, calculates GPS distance/bearing, sends UART commands
3. **Arduino Traffic Controller** - Controls traffic signals + RFID, receives UART commands

---

## Device 1: Ambulance ESP32 (Transmitter)

### Hardware Connections

**LoRa SX1278 (433MHz):**
- VCC → 3.3V
- GND → GND
- MISO → GPIO 19
- MOSI → GPIO 23
- SCK → GPIO 18
- NSS (CS) → GPIO 5
- RST → GPIO 14
- DIO0 → GPIO 2

**GPS Module (Neo-6M/M8N):**
- VCC → VIN/5V
- GND → GND
- TX → GPIO 16 (RX2)
- RX → GPIO 17 (TX2)

**Status LED:**
- Long leg (+) → GPIO 25 (with 220Ω resistor)
- Short leg (-) → GND

**Active Buzzer:**
- (+) Pin → GPIO 26
- (-) Pin → GND

**Emergency Button:**
- Pin A → GPIO 33
- Pin B → GND

**Power:**
- TP4056 Charger OUT+ → 3.3V
- TP4056 Charger OUT- → GND
- LiPo Battery → TP4056 input

### Firmware File
`firmware/ambulance_unit/ambulance_unit.ino`

---

## Device 2: ESP32 LoRa Receiver

### Hardware Connections

**LoRa SX1278 (433MHz):**
- VCC → 3.3V
- GND → GND
- MISO → GPIO 19
- MOSI → GPIO 23
- SCK → GPIO 18
- NSS (CS) → GPIO 5
- RST → GPIO 14
- DIO0 → GPIO 2

**UART to Arduino (Serial2):**
- TX2 (GPIO 17) → Arduino RX (Pin 0)
- RX2 (GPIO 16) → Arduino TX (Pin 1)
- GND → Arduino GND (common ground required)

**Power:**
- USB or external 5V supply

### Firmware File
`firmware/lora_receiver_esp32/lora_receiver_esp32.ino`

### UART Commands Sent to Arduino
- `AMBULANCE_APPROACH,<signal_id>` - Ambulance approaching, preempt specified signal (0-3)
- `AMBULANCE_OUT_OF_RANGE` - Ambulance out of range, restore normal traffic

---

## Device 3: Arduino Traffic Controller

### Hardware Connections

**RFID Entry Reader (Reader 2):**
- SDA (SS) → Pin 8
- RST → Pin 7
- SCK → Pin 13
- MOSI → Pin 11
- MISO → Pin 12
- 3.3V → 3.3V
- GND → GND

**RFID Exit Reader (Reader 1):**
- SDA (SS) → Pin 10
- RST → Pin 9
- SCK → Pin 13 (shared)
- MOSI → Pin 11 (shared)
- MISO → Pin 12 (shared)
- 3.3V → 3.3V
- GND → GND

**Traffic Signal LEDs (using digital pin numbers):**

**Signal 0:**
- RED → D2
- GREEN → D3
- YELLOW → D17 (A3 on Arduino Uno)

**Signal 1:**
- RED → D4
- GREEN → D5
- YELLOW → D15 (A1 on Arduino Uno)

**Signal 2:**
- RED → D6
- GREEN → D16 (A2 on Arduino Uno)
- YELLOW → D14 (A0 on Arduino Uno)

**Signal 3:**
- RED → D18 (A4 on Arduino Uno)
- GREEN → D19 (A5 on Arduino Uno)
- (No yellow for signal 3)

**Note:** For Arduino Uno/Nano: A0=14, A1=15, A2=16, A3=17, A4=18, A5=19
For Arduino Mega: A0=54, A1=55, A2=56, A3=57, A4=58, A5=59

**UART from ESP32 (Serial2):**
- RX (Pin 0) → ESP32 TX2 (GPIO 17)
- TX (Pin 1) → ESP32 RX2 (GPIO 16)
- GND → ESP32 GND (common ground required)

**Power:**
- USB or external 5V supply

### Firmware File
`firmware/arduino_traffic_controller/arduino_traffic_controller.ino`

---

## UART Commands Received from ESP32

The Arduino receives these commands via Serial (9600 baud):

1. **`AMBULANCE_APPROACH,<signal_id>`**
   - ESP32 detects ambulance within 500m
   - Arduino preempts specified signal (0=NORTH, 1=EAST, 2=SOUTH, 3=WEST)
   - Signal stays green until ambulance exit

2. **`AMBULANCE_OUT_OF_RANGE`**
   - ESP32 detects ambulance out of range or packet timeout
   - Arduino restores normal traffic cycle

3. **`AMBULANCE_EXIT`**
   - Manual command (can be sent if needed)
   - Arduino restores normal traffic cycle

---

## System Operation Flow

### Normal Operation
1. Ambulance ESP32 broadcasts GPS data via LoRa every 1 second (when emergency active)
2. ESP32 LoRa Receiver receives LoRa packets
3. ESP32 calculates distance and bearing to junction
4. If ambulance within 500m, ESP32 sends `AMBULANCE_APPROACH,<signal_id>` to Arduino
5. Arduino preempts traffic signal (specified signal green, others red)
6. Ambulance crosses junction

### RFID Exit Detection
1. Ambulance crosses RFID exit reader
2. Arduino detects authorized RFID tag
3. Arduino restores normal traffic cycle
4. System returns to normal operation

### GPS Exit Detection
1. If ambulance moves out of range (>500m)
2. ESP32 sends `AMBULANCE_OUT_OF_RANGE` to Arduino
3. Arduino restores normal traffic cycle

### Manual RFID Entry (Legacy)
1. Ambulance crosses RFID entry reader
2. Arduino detects authorized RFID tag
3. Arduino preempts traffic signal (uses configured entrySignalID)
4. Ambulance crosses exit RFID to restore

---

## Configuration

### ESP32 LoRa Receiver Settings
Edit `firmware/lora_receiver_esp32/lora_receiver_esp32.ino`:

```cpp
const float JUNCTION_LAT = 13.013123;  // Your junction latitude
const float JUNCTION_LON = 77.629112;  // Your junction longitude
const float TRIGGER_DISTANCE = 500.0;  // Trigger distance in meters
```

### Arduino Traffic Controller Settings
Edit `firmware/arduino_traffic_controller/arduino_traffic_controller.ino`:

```cpp
String allowedUIDs[] = {
  "04C0F0F2021390"  // Add your authorized RFID tags
};

const int totalTags = 1;  // Update count if adding more tags

int entrySignalID = 0;  // Signal to preempt for manual RFID entry
```

### Ambulance ESP32 Settings
Edit `firmware/ambulance_unit/ambulance_unit.ino`:

```cpp
String AMBULANCE_ID = "AMB001";  // Your ambulance ID
String TRIP_ID = "TRIP001";      // Your trip ID
```

---

## Testing Procedure

### 1. Test Ambulance ESP32
- Upload `ambulance_unit.ino`
- Open Serial Monitor (115200 baud)
- Send command: `EMERGENCY ON`
- Should see GPS data being broadcast via LoRa
- Test button toggles emergency mode
- Check LED and buzzer functionality

### 2. Test ESP32 LoRa Receiver
- Upload `lora_receiver_esp32.ino`
- Open Serial Monitor (115200 baud)
- Should see "LoRa Receiver Active"
- When ambulance transmits, should see distance/bearing calculations
- Should see UART commands being sent to Arduino

### 3. Test Arduino Traffic Controller
- Upload `arduino_traffic_controller.ino`
- Open Serial Monitor (9600 baud)
- Should see "SYSTEM READY"
- Normal traffic cycle should run (signals cycle every 5 seconds)
- Test RFID entry/exit detection
- Test UART commands (manually send via Serial Monitor)

### 4. Integration Test
- Connect all 3 devices
- Start ambulance emergency mode
- Watch ESP32 receiver detect ambulance
- Watch Arduino preempt traffic signal
- Test RFID exit to restore normal traffic

---

## Troubleshooting

### LoRa Not Working
- Check 433MHz frequency matches on both devices
- Verify antenna connections
- Check power supply (3.3V only)
- Ensure proper ground connections

### UART Communication Issues
- Verify TX/RX cross-connection (ESP32 TX2/GPIO 17 → Arduino RX/Pin 0)
- Verify RX/TX cross-connection (ESP32 RX2/GPIO 16 → Arduino TX/Pin 1)
- Ensure common ground between ESP32 and Arduino
- Check baud rate (9600 for Arduino, 115200 for ESP32 debug)
- Use Serial Monitor at correct baud rate
- Note: ESP32 uses Serial2 for Arduino communication (pins 16/17)

### RFID Not Working
- Check SPI pin connections
- Verify 3.3V power supply
- Check RFID tag UID matches allowedUIDs
- Test with Serial Monitor to see scanned UIDs

### Traffic Signals Not Working
- Verify LED pin connections
- Check current limiting resistors
- Test LEDs with simple digitalWrite in setup()
- Verify Arduino power supply

---

## Security Notes

1. **RFID Security**: Add authorized RFID tags to `allowedUIDs` array
2. **Ambulance Authorization**: ESP32 receiver should verify ambulance ID in production
3. **Physical Security**: Enclose hardware in tamper-proof enclosures
4. **Power Backup**: Consider battery backup for traffic controller

---

## Future Enhancements

1. Add WiFi to ESP32 receiver for cloud integration (MQTT/Firebase)
2. Add multiple ambulance support
3. Add traffic flow sensors
4. Add emergency vehicle detection cameras
5. Add traffic analytics and reporting
