import React, { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  Activity,
  Ambulance,
  Bell,
  CheckCircle2,
  Clock,
  MapPin,
  RadioTower,
  RefreshCcw,
  ShieldAlert,
  Siren,
  Smartphone,
  TrafficCone,
  Wifi,
  WifiOff,
} from "lucide-react";
import {
  seedHospitals,
  subscribeToDashboardData,
  writeAmbulanceStatus,
  writeJunction,
  writeJunctionEvent,
  writeLoRaTelemetry,
  writeTrip,
} from "./integrations/firebaseClient";
import { subscribeToMqtt } from "./integrations/mqttClient";
import AmbulanceMap from "./components/AmbulanceMap";
import "./styles.css";

const now = () => new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit", second: "2-digit" });
const formatTime = (timestamp) =>
  new Date(timestamp || Date.now()).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit", second: "2-digit" });

const initialJunctions = [
  {
    id: "JNC001",
    name: "Main Road Junction",
    lane: "Northbound",
    status: "normal",
    activeTag: null,
    preemptionMode: "none",
    distanceMeters: null,
    rssi: null,
    dwellTime: null,
    lastEvent: "Normal cycle running",
    online: true,
  },
  {
    id: "JNC002",
    name: "Hospital Cross",
    lane: "Eastbound",
    status: "normal",
    activeTag: null,
    preemptionMode: "none",
    distanceMeters: null,
    rssi: null,
    dwellTime: null,
    lastEvent: "Normal cycle running",
    online: true,
  },
  {
    id: "JNC003",
    name: "Emergency Gate",
    lane: "Southbound",
    status: "normal",
    activeTag: null,
    preemptionMode: "none",
    distanceMeters: null,
    rssi: null,
    dwellTime: null,
    lastEvent: "Normal cycle running",
    online: false,
  },
];

const hospitals = [
  { id: "HOSP001", name: "City Care Hospital", distance: "2.4 km", beds: 8, eta: "6 min" },
  { id: "HOSP002", name: "Metro Emergency Center", distance: "3.1 km", beds: 3, eta: "9 min" },
  { id: "HOSP003", name: "St. Mark Trauma Unit", distance: "4.6 km", beds: 11, eta: "12 min" },
];

function eventText(type, junction, tag = "RFID_TAG_001") {
  const action = {
    gps_preempt_started: "LoRa GPS approach confirmed priority preemption",
    rssi_preempt_started: "RSSI fallback triggered conservative preemption",
    rfid_clearance: "Stop-line RC522 confirmed ambulance clearance",
    timeout: "RFID clearance timeout, normal signal restored",
    timeout_restore: "RFID clearance timeout, normal signal restored",
    manual: "Manual override toggled by control room",
    reset: "Signal restored to normal cycle",
    manual_reset: "Signal restored to normal cycle",
    emergency: "Driver activated emergency trip",
  }[type];

  return `${action} at ${junction.name} (${tag})`;
}

function signalStateToStatus(signalState) {
  if (signalState === "priority_active") return "priority";
  if (signalState === "timeout_restore") return "timeout";
  return "normal";
}

function signalStateToEvent(signalState, dwellTime) {
  if (signalState === "priority_active") return "Priority active for ambulance lane";
  if (signalState === "timeout_restore") return "Safety timeout restored signal";
  if (dwellTime) return `Restored after ${dwellTime} dwell time`;
  return "Normal cycle running";
}

function firebaseEventMessage(event) {
  const name = event.junctionName || event.junctionId || "junction";
  const tag = event.rfidTagId || "RFID_TAG_001";
  const messageByType = {
    gps_preempt_started: `LoRa GPS approach started preemption at ${name}`,
    rssi_preempt_started: `RSSI fallback started preemption at ${name}`,
    rfid_clearance: `Stop-line RC522 confirmed clearance at ${name} (${tag})`,
    timeout_restore: `RFID clearance timeout, normal signal restored at ${name} (${tag})`,
    manual_reset: `Signal restored to normal cycle at ${name} (${tag})`,
  };

  return messageByType[event.eventType] || `${event.eventType || "Event"} at ${name}`;
}

function firebaseEventType(eventType) {
  if (eventType === "timeout_restore") return "timeout";
  if (eventType === "manual_reset") return "reset";
  if (eventType === "gps_preempt_started" || eventType === "rssi_preempt_started") return "entry";
  if (eventType === "rfid_clearance") return "exit";
  return eventType || "system";
}

function eventKey(event) {
  return [event.junctionId || event.junction, event.eventType || event.message, event.timestamp || event.time].join(":");
}

function mergeEvents(...lists) {
  const seen = new Set();
  return lists
    .flat()
    .filter((event) => {
      const key = eventKey(event);
      if (seen.has(key)) return false;
      seen.add(key);
      return true;
    })
    .sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0))
    .slice(0, 10);
}

function applySignalToJunction(junction, payload) {
  if (!payload) return junction;
  const signalState = payload.signalState || (payload.preemptionEligible ? "priority_active" : "normal");
  return {
    ...junction,
    activeTag: payload.activeAmbulanceId ? "RFID_TAG_001" : null,
    preemptionMode: payload.preemptionMode || junction.preemptionMode,
    distanceMeters: payload.distanceMeters ?? junction.distanceMeters,
    rssi: payload.rssi ?? junction.rssi,
    lastEvent: signalStateToEvent(signalState, payload.lastDwellTime || junction.dwellTime),
    online: true,
    status: signalStateToStatus(signalState),
  };
}

function App() {
  const [emergencyActive, setEmergencyActive] = useState(false);
  const [ambulanceLocation, setAmbulanceLocation] = useState(null);
  const [selectedHospital, setSelectedHospital] = useState(hospitals[0]);
  const [junctions, setJunctions] = useState(initialJunctions);
  const [firebaseOnline, setFirebaseOnline] = useState(false);
  const [mqttStatus, setMqttStatus] = useState("connecting");
  const [liveEvents, setLiveEvents] = useState([]);
  const [events, setEvents] = useState([
    { id: 1, time: now(), type: "system", message: "Dashboard simulator ready", junction: "Control Room" },
  ]);

  useEffect(() => {
    seedHospitals(hospitals).catch((error) => {
      addEvent("timeout", `Firebase seed failed: ${error.message}`, "Firebase");
    });

    const unsubscribe = subscribeToDashboardData((data) => {
      setFirebaseOnline(true);

      const ambulance = data.ambulances?.AMB001;
      if (ambulance) {
        setEmergencyActive(Boolean(ambulance.emergencyActive || ambulance.status === "emergency_active"));
        setAmbulanceLocation(ambulance.lastLocation || null);
      }

      const trip = data.emergencyTrips?.TRIP001;
      if (trip?.destinationHospitalId) {
        const destination = hospitals.find((hospital) => hospital.id === trip.destinationHospitalId);
        if (destination) setSelectedHospital(destination);
      }

      if (data.junctions) {
        setJunctions(
          initialJunctions.map((junction) => {
            const cloudJunction = data.junctions[junction.id];
            if (!cloudJunction) return junction;

            return {
              ...junction,
              activeTag: cloudJunction.activeAmbulanceId ? "RFID_TAG_001" : null,
              preemptionMode: cloudJunction.preemptionMode || "none",
              distanceMeters: cloudJunction.distanceMeters || null,
              rssi: cloudJunction.rssi || null,
              dwellTime: cloudJunction.lastDwellTime || null,
              lastEvent: signalStateToEvent(cloudJunction.signalState, cloudJunction.lastDwellTime),
              online: true,
              status: signalStateToStatus(cloudJunction.signalState),
            };
          }),
        );
      }

      if (data.junctionEvents) {
        const cloudEvents = Object.entries(data.junctionEvents)
          .map(([id, event]) => ({
            id,
            junction: event.junctionName || event.junctionId || "Junction",
            junctionId: event.junctionId,
            eventType: event.eventType,
            message: firebaseEventMessage(event),
            time: formatTime(event.timestamp),
            timestamp: event.timestamp || 0,
            type: firebaseEventType(event.eventType),
          }))
          .sort((a, b) => b.timestamp - a.timestamp)
          .slice(0, 10);

        setEvents(cloudEvents);
      }
    });

    return () => unsubscribe();
  }, []);

  useEffect(() => {
    return subscribeToMqtt({
      onStatus: setMqttStatus,
      onEvent: (data) => {
        const mapped = {
          id: `mqtt-${data.junctionId}-${data.eventType}-${data.timestamp}`,
          junction: data.junctionName || data.junctionId || "Junction",
          junctionId: data.junctionId,
          eventType: data.eventType,
          message: firebaseEventMessage(data),
          time: formatTime(data.timestamp),
          timestamp: data.timestamp || Date.now(),
          type: firebaseEventType(data.eventType),
        };
        setLiveEvents((current) => [mapped, ...current].slice(0, 20));
        if (data.eventType === "gps_preempt_started" || data.eventType === "rssi_preempt_started") {
          setEmergencyActive(true);
        }
      },
      onSignal: (data, topic) => {
        const junctionId = data.junctionId || topic.split("/")[2];
        setJunctions((current) =>
          current.map((junction) => (junction.id === junctionId ? applySignalToJunction(junction, data) : junction)),
        );
        if (data.signalState === "priority_active") setEmergencyActive(true);
      },
      onTelemetry: (data, topic) => {
        const junctionId = data.junctionId || (topic.includes("/junctions/") ? topic.split("/")[2] : "JNC001");
        if (data.lat && data.lng) {
          setAmbulanceLocation({ lat: data.lat, lng: data.lng, source: "LoRa MQTT" });
        }
        setJunctions((current) =>
          current.map((junction) =>
            junction.id === junctionId
              ? {
                  ...junction,
                  distanceMeters: data.distanceMeters ?? junction.distanceMeters,
                  rssi: data.rssi ?? junction.rssi,
                  preemptionMode: data.source || data.preemptionMode || junction.preemptionMode,
                  online: true,
                }
              : junction,
          ),
        );
      },
    });
  }, []);

  const visibleEvents = useMemo(() => mergeEvents(liveEvents, events), [liveEvents, events]);
  const mqttLabel = mqttStatus === "live" ? "Live" : mqttStatus === "connecting" ? "Connecting" : "Offline";

  const activeJunctions = junctions.filter((junction) => junction.status === "priority").length;
  const normalJunctions = junctions.length - activeJunctions;

  const routeProgress = useMemo(() => {
    if (!emergencyActive) return 12;
    return activeJunctions > 0 ? 58 : 35;
  }, [activeJunctions, emergencyActive]);

  function addEvent(type, message, junction = "Control Room") {
    setEvents((current) => [
      { id: Date.now(), time: now(), type, message, junction },
      ...current,
    ].slice(0, 10));
  }

  function persistJunctionEvent(type, junction, extra = {}) {
    return writeJunctionEvent({
      ambulanceId: "AMB001",
      eventType: type,
      junctionId: junction.id,
      junctionName: junction.name,
      lane: junction.lane,
      rfidTagId: extra.rfidTagId || "RFID_TAG_001",
      timestamp: Date.now(),
      ...extra,
    }).catch((error) => {
      addEvent("timeout", `Firebase write failed: ${error.message}`, "Firebase");
    });
  }

  function startEmergency() {
    setEmergencyActive(true);
    writeAmbulanceStatus("AMB001", {
      ambulanceId: "AMB001",
      driverId: "DRV001",
      emergencyActive: true,
      status: "emergency_active",
      destinationHospitalId: selectedHospital.id,
      updatedAt: Date.now(),
    });
    writeTrip("TRIP001", {
      tripId: "TRIP001",
      ambulanceId: "AMB001",
      driverId: "DRV001",
      destinationHospitalId: selectedHospital.id,
      status: "active",
      startedAt: Date.now(),
    });
    addEvent("emergency", "AMB001 emergency trip started from driver app", "AMB001");
  }

  function endEmergency() {
    setEmergencyActive(false);
    setJunctions((current) =>
      current.map((junction) => ({
        ...junction,
        status: "normal",
        activeTag: null,
        preemptionMode: "none",
        distanceMeters: null,
        rssi: null,
        lastEvent: "Normal cycle running",
      })),
    );
    writeAmbulanceStatus("AMB001", {
      emergencyActive: false,
      status: "available",
      updatedAt: Date.now(),
    });
    writeTrip("TRIP001", {
      status: "completed",
      endedAt: Date.now(),
    });
    addEvent("reset", "Emergency trip completed and corridor released", "AMB001");
  }

  function simulateGpsPreempt(id) {
    setEmergencyActive(true);
    const distanceMeters = Math.floor(Math.random() * 180) + 320;
    const rssi = -(Math.floor(Math.random() * 12) + 68);
    setJunctions((current) =>
      current.map((junction) =>
        junction.id === id
          ? {
              ...junction,
              status: "priority",
              activeTag: "RFID_TAG_001",
              preemptionMode: "gps_lora",
              distanceMeters,
              rssi,
              dwellTime: null,
              lastEvent: `GPS-LoRa preempt at ${distanceMeters} m`,
            }
          : junction,
      ),
    );

    const junction = junctions.find((item) => item.id === id);
    writeJunction(id, {
      activeAmbulanceId: "AMB001",
      activeLane: junction.lane,
      preemptionMode: "gps_lora",
      distanceMeters,
      rssi,
      signalState: "priority_active",
      updatedAt: Date.now(),
    });
    writeLoRaTelemetry(id, "AMB001", {
      ambulanceId: "AMB001",
      tripId: "TRIP001",
      lat: 12.9716,
      lng: 77.5946,
      speedKmph: 42,
      headingDeg: 185,
      gpsFix: true,
      rssi,
      distanceMeters,
      bearingToJunctionDeg: 182,
      approaching: true,
      preemptionEligible: true,
      updatedAt: Date.now(),
    });
    persistJunctionEvent("gps_preempt_started", junction, { preemptionMode: "gps_lora", distanceMeters, rssi });
    addEvent("entry", eventText("gps_preempt_started", junction), junction.name);
  }

  function simulateRssiFallback(id) {
    setEmergencyActive(true);
    const rssi = -(Math.floor(Math.random() * 6) + 60);
    setJunctions((current) =>
      current.map((junction) =>
        junction.id === id
          ? {
              ...junction,
              status: "priority",
              activeTag: "RFID_TAG_001",
              preemptionMode: "rssi_fallback",
              distanceMeters: null,
              rssi,
              dwellTime: null,
              lastEvent: `RSSI fallback active at ${rssi} dBm`,
            }
          : junction,
      ),
    );

    const junction = junctions.find((item) => item.id === id);
    writeJunction(id, {
      activeAmbulanceId: "AMB001",
      activeLane: junction.lane,
      preemptionMode: "rssi_fallback",
      rssi,
      signalState: "priority_active",
      updatedAt: Date.now(),
    });
    writeLoRaTelemetry(id, "AMB001", {
      ambulanceId: "AMB001",
      tripId: "TRIP001",
      gpsFix: false,
      rssi,
      approaching: true,
      preemptionEligible: true,
      source: "rssi_fallback",
      updatedAt: Date.now(),
    });
    persistJunctionEvent("rssi_preempt_started", junction, { preemptionMode: "rssi_fallback", rssi });
    addEvent("entry", eventText("rssi_preempt_started", junction), junction.name);
  }

  function simulateRfidClearance(id) {
    const dwellTime = `${Math.floor(Math.random() * 9) + 6}s`;
    setJunctions((current) =>
      current.map((junction) =>
        junction.id === id
          ? {
              ...junction,
              status: "normal",
              activeTag: null,
              preemptionMode: "none",
              distanceMeters: null,
              rssi: null,
              dwellTime,
              lastEvent: `Restored after ${dwellTime} dwell time`,
            }
          : junction,
      ),
    );

    const junction = junctions.find((item) => item.id === id);
    writeJunction(id, {
      activeAmbulanceId: null,
      activeLane: null,
      preemptionMode: "none",
      lastDwellTime: dwellTime,
      signalState: "normal",
      updatedAt: Date.now(),
    });
    persistJunctionEvent("rfid_clearance", junction, { dwellTime, preemptionMode: "rfid_clearance" });
    addEvent("exit", eventText("rfid_clearance", junction), junction.name);
  }

  function simulateTimeout(id) {
    setJunctions((current) =>
      current.map((junction) =>
        junction.id === id
          ? {
              ...junction,
              status: "timeout",
              activeTag: null,
              preemptionMode: "none",
              distanceMeters: null,
              rssi: null,
              dwellTime: "timeout",
              lastEvent: "Safety timeout restored signal",
            }
          : junction,
      ),
    );

    const junction = junctions.find((item) => item.id === id);
    writeJunction(id, {
      activeAmbulanceId: null,
      activeLane: null,
      preemptionMode: "none",
      signalState: "timeout_restore",
      updatedAt: Date.now(),
    });
    persistJunctionEvent("timeout_restore", junction);
    addEvent("timeout", eventText("timeout", junction), junction.name);
  }

  function resetJunction(id) {
    setJunctions((current) =>
      current.map((junction) =>
        junction.id === id
          ? {
              ...junction,
              status: "normal",
              activeTag: null,
              preemptionMode: "none",
              distanceMeters: null,
              rssi: null,
              dwellTime: null,
              lastEvent: "Normal cycle running",
            }
          : junction,
      ),
    );

    const junction = junctions.find((item) => item.id === id);
    writeJunction(id, {
      activeAmbulanceId: null,
      activeLane: null,
      preemptionMode: "none",
      signalState: "normal",
      updatedAt: Date.now(),
    });
    persistJunctionEvent("manual_reset", junction);
    addEvent("reset", eventText("reset", junction), junction.name);
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Traffic Police Control Room</p>
          <h1>Smart Ambulance Priority Dashboard</h1>
        </div>
        <div className={`status-pill ${emergencyActive ? "active" : ""}`}>
          <Siren size={18} />
          {emergencyActive ? "Emergency active" : "Standby"}
        </div>
      </header>

      <section className="overview-grid" aria-label="System overview">
        <Metric icon={Ambulance} label="Ambulance" value="AMB001" detail={emergencyActive ? "Tracking live" : "Ready"} />
        <Metric icon={TrafficCone} label="Priority Signals" value={activeJunctions} detail={`${normalJunctions} normal`} />
        <Metric icon={Clock} label="ETA" value={selectedHospital.eta} detail={selectedHospital.name} />
        <Metric
          icon={MapPin}
          label="GPS"
          value={ambulanceLocation ? `${Number(ambulanceLocation.lat).toFixed(3)}, ${Number(ambulanceLocation.lng).toFixed(3)}` : "Waiting"}
          detail={ambulanceLocation?.source || "Android app"}
        />
        <Metric icon={Activity} label="Firebase" value={firebaseOnline ? "Live" : "Connecting"} detail="Realtime Database" />
        <Metric icon={RadioTower} label="MQTT" value={mqttLabel} detail="HiveMQ junction feed" />
      </section>

      <section className="control-layout">
        <div className="route-panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Live Corridor</p>
              <h2>AMB001 to {selectedHospital.name}</h2>
            </div>
            <MapPin size={24} />
          </div>

          <div className="route-map" aria-label="Live ambulance map">
            <AmbulanceMap
              ambulance={{
                ambulanceId: "AMB001",
                status: emergencyActive ? "emergency_active" : "available",
                severity: "Serious",
                lastLocation: ambulanceLocation,
                lastLoRaTelemetry: junctions.find(j => j.status === "priority")
              }}
              junctions={junctions.map(j => ({
                ...j,
                location: { lat: 12.9716, lng: 77.5946 } // Default location, would come from Firebase
              }))}
              hospitals={hospitals.map(h => ({
                ...h,
                lat: 12.9701 + (h.id === "HOSP001" ? 0 : h.id === "HOSP002" ? 0.01 : -0.01),
                lng: 77.6001 + (h.id === "HOSP001" ? 0 : h.id === "HOSP002" ? 0.01 : -0.01)
              }))}
              selectedHospital={selectedHospital}
            />
          </div>

          <div className="action-row">
            <button className="primary-action" onClick={startEmergency}>
              <Bell size={18} />
              Start emergency
            </button>
            <button className="secondary-action" onClick={endEmergency}>
              <CheckCircle2 size={18} />
              Complete trip
            </button>
          </div>
        </div>

        <aside className="hospital-panel">
          <div className="panel-header compact">
            <h2>Hospitals</h2>
            <ShieldAlert size={22} />
          </div>
          <div className="hospital-list">
            {hospitals.map((hospital) => (
              <button
                key={hospital.id}
                className={`hospital-option ${selectedHospital.id === hospital.id ? "selected" : ""}`}
                onClick={() => setSelectedHospital(hospital)}
              >
                <span>{hospital.name}</span>
                <small>{hospital.distance} · {hospital.eta} · {hospital.beds} beds</small>
              </button>
            ))}
          </div>
        </aside>
      </section>

      <section className="junction-section">
        <div className="section-title">
          <h2>Junction Simulator</h2>
          <p>Live MQTT/Firebase from ESP32, or use these buttons for a tabletop demo.</p>
        </div>
        <div className="junction-grid">
          {junctions.map((junction) => (
            <article className={`junction-card ${junction.status}`} key={junction.id}>
              <div className="junction-head">
                <div>
                  <h3>{junction.name}</h3>
                  <p>{junction.id} · {junction.lane}</p>
                </div>
                {junction.online ? <Wifi size={20} /> : <WifiOff size={20} />}
              </div>

              <div className="signal-stack" aria-label={`${junction.name} signal state`}>
                <span className={junction.status === "normal" ? "red off" : "red"} />
                <span className={junction.status === "timeout" ? "amber" : "amber off"} />
                <span className={junction.status === "priority" ? "green" : "green off"} />
              </div>

              <p className="junction-event">{junction.lastEvent}</p>
              <div className="tag-row">
                <RadioTower size={16} />
                <span>
                  {junction.activeTag
                    ? `${junction.preemptionMode} · ${junction.distanceMeters ? `${junction.distanceMeters} m` : `${junction.rssi} dBm`}`
                    : "No active preemption"}
                </span>
              </div>

              <div className="control-buttons">
                <button onClick={() => simulateGpsPreempt(junction.id)}>
                  <Siren size={16} />
                  GPS
                </button>
                <button onClick={() => simulateRssiFallback(junction.id)}>
                  <RadioTower size={16} />
                  RSSI
                </button>
                <button onClick={() => simulateRfidClearance(junction.id)}>
                  <CheckCircle2 size={16} />
                  RFID
                </button>
                <button onClick={() => simulateTimeout(junction.id)}>
                  <Clock size={16} />
                  Timeout
                </button>
                <button onClick={() => resetJunction(junction.id)}>
                  <RefreshCcw size={16} />
                  Reset
                </button>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="bottom-layout">
        <div className="events-panel">
          <div className="panel-header compact">
            <h2>Event Log</h2>
            <RadioTower size={22} />
          </div>
          <div className="event-list">
            {visibleEvents.map((event) => (
              <div className="event-row" key={event.id}>
                <span className={`event-type ${event.type}`} />
                <div>
                  <strong>{event.message}</strong>
                  <small>{event.time} · {event.junction}</small>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="integration-panel">
          <div className="panel-header compact">
            <h2>Software Readiness</h2>
            <Smartphone size={22} />
          </div>
          <ul>
            <li>Dashboard follows Firebase and live MQTT from the junction ESP32.</li>
            <li>Serial SIM on the junction still works with no LoRa hardware.</li>
            <li>Ambulance ESP32 broadcasts GPS-LoRa JSON; the phone app still owns Firebase GPS.</li>
            <li>Set WIFI_SSID on the junction to push events to this screen automatically.</li>
          </ul>
        </div>
      </section>
    </main>
  );
}

function Metric({ icon: Icon, label, value, detail }) {
  return (
    <article className="metric">
      <Icon size={24} />
      <div>
        <span>{label}</span>
        <strong>{value}</strong>
        <small>{detail}</small>
      </div>
    </article>
  );
}

createRoot(document.getElementById("root")).render(<App />);
