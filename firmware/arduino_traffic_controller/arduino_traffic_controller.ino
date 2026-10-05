/*
  Arduino Traffic Controller for Smart Ambulance System
  Controls traffic signals and RFID, receives commands from ESP32 LoRa Receiver via UART

  Hardware:
  - RFID Entry Reader (Reader 2):
    * SS: Pin 8
    * RST: Pin 7
  - RFID Exit Reader (Reader 1):
    * SS: Pin 10
    * RST: Pin 9
  - Traffic Signal LEDs (using digital pin numbers):
    * Signal 0: RED=D2, GREEN=D3, YELLOW=D17 (A3 on Uno)
    * Signal 1: RED=D4, GREEN=D5, YELLOW=D15 (A1 on Uno)
    * Signal 2: RED=D6, GREEN=D16 (A2 on Uno), YELLOW=D14 (A0 on Uno)
    * Signal 3: RED=D18 (A4 on Uno), GREEN=D19 (A5 on Uno), no yellow
  - UART from ESP32:
    * RX: Connect to ESP32 TX2 (GPIO 17)
    * TX: Connect to ESP32 RX2 (GPIO 16)

  Commands received from ESP32 via UART:
  - "AMBULANCE_APPROACH,<signal_id>" - Ambulance approaching, preempt specified signal
  - "AMBULANCE_OUT_OF_RANGE" - Ambulance out of range, restore normal traffic
*/

#include <SPI.h>
#include <MFRC522.h>

// =====================================================
// RFID CONFIGURATION
// =====================================================

// Physical Reader 2 = ENTRY
#define ENTRY_SS_PIN 8
#define ENTRY_RST_PIN 7

// Physical Reader 1 = EXIT
#define EXIT_SS_PIN 10
#define EXIT_RST_PIN 9

MFRC522 entryRFID(ENTRY_SS_PIN, ENTRY_RST_PIN);
MFRC522 exitRFID(EXIT_SS_PIN, EXIT_RST_PIN);


// =====================================================
// TRAFFIC SIGNAL LED CONFIGURATION
// =====================================================

// Signal 0
// RED   = D2
// GREEN = D3
// YELLOW = D14 (A0 on Uno)

// Signal 1
// RED   = D4
// GREEN = D5
// YELLOW = D15 (A1 on Uno)

// Signal 2
// RED   = D6
// GREEN = D16 (A2 on Uno)
// YELLOW = D17 (A3 on Uno)

// Signal 3
// RED   = D18 (A4 on Uno)
// GREEN = D19 (A5 on Uno)
// (No yellow for signal 3)

// Using digital pin numbers for compatibility across Arduino boards
// For Arduino Uno/Nano: A0=14, A1=15, A2=16, A3=17, A4=18, A5=19
// For Arduino Mega: A0=54, A1=55, A2=56, A3=57, A4=58, A5=59

int redLED[4] = {
  2, 4, 6, 18   // Signal 0,1,2,3 RED pins
};

int greenLED[4] = {
  3, 5, 16, 19  // Signal 0,1,2,3 GREEN pins
};

int yellowLED[4] = {
  17, 15, 14, -1  // Signal 0,1,2,3 YELLOW pins (-1 = no yellow for signal 3)
};


// =====================================================
// AUTHORIZED AMBULANCE RFID TAG
// =====================================================

String allowedUIDs[] = {
  "04C0F0F2021390"
};

const int totalTags = 1;


// =====================================================
// TRAFFIC VARIABLES
// =====================================================

int currentSignal = 0;

// Green time for each signal
unsigned long cycleTime = 5000;

unsigned long lastSwitch = 0;


// =====================================================
// AMBULANCE VARIABLES
// =====================================================

bool ambulanceActive = false;
bool uartTriggered = false; // True if ambulance mode triggered by ESP32 UART

// Signal that will become GREEN when ambulance arrives
int entrySignalID = 0;


// =====================================================
// UART COMMAND PROCESSING
// =====================================================

String uartBuffer = "";
unsigned long lastUartActivity = 0;

void processUartCommand(String command) {
  command.trim();
  
  if (command.length() == 0) return;
  
  Serial.print("Received UART command: ");
  Serial.println(command);
  
  if (command.startsWith("AMBULANCE_APPROACH,")) {
    int signalId = command.substring(19).toInt();
    
    if (signalId >= 0 && signalId < 4) {
      Serial.println("================================");
      Serial.println("AMBULANCE APPROACHING FROM ESP32");
      Serial.print("Preempting Signal ");
      Serial.println(signalId);
      Serial.println("================================");
      
      ambulanceActive = true;
      uartTriggered = true;
      entrySignalID = signalId;
      currentSignal = signalId;
      setSignal(currentSignal);
      
      Serial.print("Signal ");
      Serial.print(currentSignal);
      Serial.println(" WILL STAY GREEN");
    }
  }
  else if (command == "AMBULANCE_OUT_OF_RANGE") {
    if (ambulanceActive && uartTriggered) {
      Serial.println("================================");
      Serial.println("AMBULANCE OUT OF RANGE (ESP32)");
      Serial.println("RESTORING NORMAL TRAFFIC");
      Serial.println("================================");
      
      ambulanceActive = false;
      uartTriggered = false;
      lastSwitch = millis();
      setSignal(currentSignal);
    }
  }
  else if (command == "AMBULANCE_EXIT") {
    // This command can be sent manually or via ESP32 if needed
    if (ambulanceActive) {
      Serial.println("================================");
      Serial.println("AMBULANCE EXIT DETECTED");
      Serial.println("RESTORING NORMAL TRAFFIC");
      Serial.println("================================");
      
      ambulanceActive = false;
      uartTriggered = false;
      lastSwitch = millis();
      setSignal(currentSignal);
    }
  }
}

void readUartCommands() {
  while (Serial.available() > 0) {
    char c = Serial.read();
    
    if (c == '\n' || c == '\r') {
      if (uartBuffer.length() > 0) {
        processUartCommand(uartBuffer);
        uartBuffer = "";
      }
    } else {
      uartBuffer += c;
      lastUartActivity = millis();
    }
  }
  
  // Clear buffer if no activity for 100ms (incomplete command)
  if (uartBuffer.length() > 0 && millis() - lastUartActivity > 100) {
    uartBuffer = "";
  }
}


// =====================================================
// SETUP
// =====================================================

void setup() {

  Serial.begin(9600);

  Serial.println();
  Serial.println("================================");
  Serial.println("TRAFFIC + AMBULANCE SYSTEM");
  Serial.println("With ESP32 UART Integration");
  Serial.println("================================");


  // ---------------------------------------------------
  // Start SPI
  // ---------------------------------------------------

  SPI.begin();


  // ---------------------------------------------------
  // RFID SS pins
  // ---------------------------------------------------

  pinMode(ENTRY_SS_PIN, OUTPUT);
  pinMode(EXIT_SS_PIN, OUTPUT);

  // Deselect both RFID readers
  digitalWrite(ENTRY_SS_PIN, HIGH);
  digitalWrite(EXIT_SS_PIN, HIGH);


  // ---------------------------------------------------
  // Initialize ENTRY RFID
  // Physical Reader 2
  // ---------------------------------------------------

  Serial.println("Initializing ENTRY RFID...");

  entryRFID.PCD_Init();

  delay(100);


  // ---------------------------------------------------
  // Initialize EXIT RFID
  // Physical Reader 1
  // ---------------------------------------------------

  Serial.println("Initializing EXIT RFID...");

  exitRFID.PCD_Init();

  delay(100);


  // ---------------------------------------------------
  // Traffic LED pins
  // ---------------------------------------------------

  for (int i = 0; i < 4; i++) {

    pinMode(redLED[i], OUTPUT);
    pinMode(greenLED[i], OUTPUT);

    digitalWrite(redLED[i], LOW);
    digitalWrite(greenLED[i], LOW);
  }


  // ---------------------------------------------------
  // Yellow LED pins
  // ---------------------------------------------------

  for (int i = 0; i < 3; i++) {

    pinMode(yellowLED[i], OUTPUT);

    digitalWrite(yellowLED[i], LOW);
  }


  // ---------------------------------------------------
  // Start traffic
  // ---------------------------------------------------

  currentSignal = 0;

  setSignal(currentSignal);

  lastSwitch = millis();


  Serial.println();
  Serial.println("SYSTEM READY");
  Serial.println("ENTRY = RFID Reader 2");
  Serial.println("EXIT  = RFID Reader 1");
  Serial.println("UART = Connected to ESP32");
  Serial.println("================================");
}


// =====================================================
// MAIN LOOP
// =====================================================

void loop() {

  // ===================================================
  // READ UART COMMANDS FROM ESP32
  // ===================================================
  
  readUartCommands();


  // ===================================================
  // AMBULANCE MODE
  // ===================================================

  if (ambulanceActive) {

    // During ambulance mode:
    // Check EXIT RFID regardless of trigger source
    if (checkRFID(exitRFID, EXIT_SS_PIN)) {

      Serial.println();
      Serial.println("********************************");
      Serial.println("AMBULANCE EXIT DETECTED (RFID)");
      Serial.println("NORMAL TRAFFIC RESUMING");
      Serial.println("********************************");

      // Disable ambulance mode
      ambulanceActive = false;
      uartTriggered = false;

      // Restart normal cycle timer
      lastSwitch = millis();

      // Continue from current signal
      setSignal(currentSignal);
    }

    return;
  }


  // ===================================================
  // NORMAL MODE
  // ===================================================

  // Check ENTRY RFID (legacy manual trigger)
  // Physical Reader 2
  if (checkRFID(entryRFID, ENTRY_SS_PIN)) {

    Serial.println();
    Serial.println("********************************");
    Serial.println("AMBULANCE DETECTED AT ENTRY (RFID)");
    Serial.println("STOPPING NORMAL TRAFFIC");
    Serial.println("********************************");


    // Enable ambulance mode
    ambulanceActive = true;
    uartTriggered = false; // RFID triggered, not UART


    // Set ambulance signal
    currentSignal = entrySignalID;

    setSignal(currentSignal);


    Serial.print("Signal ");
    Serial.print(currentSignal);
    Serial.println(" WILL STAY GREEN");


    return;
  }


  // ===================================================
  // NORMAL TRAFFIC CYCLE
  // ===================================================

  if (millis() - lastSwitch >= cycleTime) {

    changeToNextSignal();

    lastSwitch = millis();
  }
}


// =====================================================
// CHANGE TO NEXT SIGNAL
// =====================================================

void changeToNextSignal() {

  int previousSignal = currentSignal;


  // ---------------------------------------------------
  // Yellow before changing
  // ---------------------------------------------------

  if (previousSignal < 4 && yellowLED[previousSignal] != -1) {

    // Turn GREEN OFF
    digitalWrite(greenLED[previousSignal], LOW);

    // Turn YELLOW ON
    digitalWrite(yellowLED[previousSignal], HIGH);

    Serial.print("Signal ");
    Serial.print(previousSignal);
    Serial.println(" -> YELLOW");


    delay(1000);


    // Turn YELLOW OFF
    digitalWrite(yellowLED[previousSignal], LOW);
  }


  // ---------------------------------------------------
  // Next signal
  // ---------------------------------------------------

  currentSignal++;

  if (currentSignal >= 4) {
    currentSignal = 0;
  }


  // Set next signal GREEN
  setSignal(currentSignal);
}


// =====================================================
// SET SIGNAL
// =====================================================

void setSignal(int signal) {

  Serial.print("Setting Signal ");
  Serial.print(signal);
  Serial.println(" GREEN");


  for (int i = 0; i < 4; i++) {

    if (i == signal) {

      // Selected signal
      digitalWrite(redLED[i], LOW);
      digitalWrite(greenLED[i], HIGH);

    } else {

      // All other signals RED
      digitalWrite(greenLED[i], LOW);
      digitalWrite(redLED[i], HIGH);
    }
  }
}


// =====================================================
// RFID CHECK FUNCTION
// =====================================================

bool checkRFID(MFRC522 &rfid, int ssPin) {

  bool success = false;


  // ---------------------------------------------------
  // Make sure BOTH readers are deselected
  // ---------------------------------------------------

  digitalWrite(ENTRY_SS_PIN, HIGH);
  digitalWrite(EXIT_SS_PIN, HIGH);


  // ---------------------------------------------------
  // Select requested RFID reader
  // ---------------------------------------------------

  digitalWrite(ssPin, LOW);


  // ---------------------------------------------------
  // Check for card
  // ---------------------------------------------------

  if (rfid.PICC_IsNewCardPresent() &&
      rfid.PICC_ReadCardSerial()) {


    // -------------------------------------------------
    // Build UID string
    // -------------------------------------------------

    String scannedUID = "";


    for (byte i = 0; i < rfid.uid.size; i++) {

      if (rfid.uid.uidByte[i] < 0x10) {
        scannedUID += "0";
      }

      scannedUID += String(
        rfid.uid.uidByte[i],
        HEX
      );
    }


    scannedUID.toUpperCase();


    // -------------------------------------------------
    // Print scanned UID
    // -------------------------------------------------

    Serial.print("Scanned UID: ");
    Serial.println(scannedUID);


    // -------------------------------------------------
    // Compare UID
    // -------------------------------------------------

    for (int i = 0; i < totalTags; i++) {

      if (scannedUID == allowedUIDs[i]) {

        success = true;

        break;
      }
    }


    // -------------------------------------------------
    // Authorized
    // -------------------------------------------------

    if (success) {

      Serial.println("AUTHORIZED RFID");
    }

    // -------------------------------------------------
    // Unauthorized
    // -------------------------------------------------

    else {

      Serial.println("UNAUTHORIZED RFID");
    }


    // -------------------------------------------------
    // Stop card communication
    // -------------------------------------------------

    rfid.PICC_HaltA();
    rfid.PCD_StopCrypto1();
  }


  // ---------------------------------------------------
  // Deselect RFID reader
  // ---------------------------------------------------

  digitalWrite(ssPin, HIGH);


  return success;
}
