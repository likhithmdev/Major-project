# Ambulance Unit Firmware

For the hybrid prototype, the ambulance has two identity/tracking parts:

- A passive RFID tag mounted under the chassis for exact stop-line clearance.
- An optional ESP32 GPS-LoRa unit that broadcasts local approach telemetry to each junction.

## Optional Hardware

- ESP32.
- Neo-6M GPS module.
- LoRa transmitter or transceiver.
- Wi-Fi or GSM module only if independent cloud upload is needed.
- Emergency status button.
- RFID tag mounted on vehicle.

## Responsibilities

- Read GPS coordinates.
- Broadcast GPS coordinates over LoRa.
- Include speed, heading, GPS fix status, and packet timestamp.
- Let the junction ground station read LoRa RSSI for fallback proximity mode.
- Upload location to cloud only if internet hardware is added.
- Maintain ambulance ID.
- Send emergency active/inactive status.

## Firmware

Flash `ambulance_unit.ino` to the vehicle ESP32.

Pin map:

- LoRa NSS 5, RST 14, DIO0 26, 433 MHz
- Neo-6M TX -> GPIO16, RX -> GPIO17
- Emergency button on GPIO4 to GND

Serial monitor at 115200:

```text
EMERGENCY ON
SIM 12.9750,77.5946,185,42
GPS OFF
STATUS
```

`SIM lat,lng,heading,speed` starts emergency and broadcasts even if the GPS module is not connected. `GPS OFF` sends `gpsFix: false` so the junction can test RSSI fallback.

The phone app still owns Firebase GPS. This unit only sends local LoRa packets to the junction.
