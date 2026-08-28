import React, { useEffect, useState } from "react";
import { MapContainer, TileLayer, Marker, Popup, Polyline } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";

// Fix for default marker icon in React Leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png",
  iconUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png",
  shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
});

const ambulanceIcon = new L.Icon({
  iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png",
  shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

const junctionIcon = new L.Icon({
  iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png",
  shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

const hospitalIcon = new L.Icon({
  iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png",
  shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

export default function AmbulanceMap({ ambulance, junctions, hospitals, selectedHospital }) {
  const [mapCenter, setMapCenter] = useState([12.9716, 77.5946]); // Default to JNC001 location
  const [routePath, setRoutePath] = useState([]);

  useEffect(() => {
    if (ambulance?.lastLocation?.lat && ambulance?.lastLocation?.lng) {
      setMapCenter([ambulance.lastLocation.lat, ambulance.lastLocation.lng]);
    }
  }, [ambulance]);

  useEffect(() => {
    if (ambulance?.lastLocation && selectedHospital) {
      // Create a simple route path from ambulance to hospital
      const hospitalLocation = hospitals.find(h => h.id === selectedHospital.id);
      if (hospitalLocation) {
        setRoutePath([
          [ambulance.lastLocation.lat, ambulance.lastLocation.lng],
          [12.9716, 77.5946], // JNC001
          [hospitalLocation.lat || 12.9701, hospitalLocation.lng || 77.6001]
        ]);
      }
    }
  }, [ambulance, selectedHospital, hospitals]);

  return (
    <div className="map-container">
      <MapContainer
        center={mapCenter}
        zoom={14}
        style={{ height: "400px", width: "100%", borderRadius: "8px" }}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />

        {/* Ambulance Marker */}
        {ambulance?.lastLocation?.lat && ambulance?.lastLocation?.lng && (
          <Marker
            position={[ambulance.lastLocation.lat, ambulance.lastLocation.lng]}
            icon={ambulanceIcon}
          >
            <Popup>
              <div>
                <strong>Ambulance {ambulance.ambulanceId}</strong><br />
                Status: {ambulance.status}<br />
                Severity: {ambulance.severity}<br />
                Speed: {ambulance.lastLoRaTelemetry?.speedKmph || 0} km/h
              </div>
            </Popup>
          </Marker>
        )}

        {/* Junction Markers */}
        {junctions.map(junction => (
          <Marker
            key={junction.id}
            position={[junction.location?.lat || 12.9716, junction.location?.lng || 77.5946]}
            icon={junctionIcon}
          >
            <Popup>
              <div>
                <strong>{junction.name}</strong><br />
                Status: {junction.signalState}<br />
                Lane: {junction.activeLane || "N/A"}<br />
                Preemption: {junction.preemptionMode}
              </div>
            </Popup>
          </Marker>
        ))}

        {/* Hospital Markers */}
        {hospitals.map(hospital => (
          <Marker
            key={hospital.id}
            position={[hospital.lat || 12.9701, hospital.lng || 77.6001]}
            icon={hospitalIcon}
          >
            <Popup>
              <div>
                <strong>{hospital.name}</strong><br />
                Beds: {hospital.bedsAvailable}<br />
                Phone: {hospital.phone}
              </div>
            </Popup>
          </Marker>
        ))}

        {/* Route Path */}
        {routePath.length > 0 && (
          <Polyline
            positions={routePath}
            color="red"
            weight={4}
            opacity={0.7}
            dashArray="10, 10"
          />
        )}
      </MapContainer>
    </div>
  );
}