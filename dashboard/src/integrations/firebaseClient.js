import { initializeApp } from "firebase/app";
import { getDatabase, onValue, push, ref, set, update } from "firebase/database";

export const firebaseConfig = {
  apiKey: "AIzaSyCqg4gsohXZZB3wBEeAKR1wND-vYTg9H70",
  authDomain: "smart-ambulance-36f9d.firebaseapp.com",
  databaseURL: "https://smart-ambulance-36f9d-default-rtdb.firebaseio.com",
  projectId: "smart-ambulance-36f9d",
  storageBucket: "smart-ambulance-36f9d.firebasestorage.app",
  messagingSenderId: "735414353984",
  appId: "1:735414353984:web:0401c5a04025560e4e9fa5",
};

export const firebasePaths = {
  ambulances: "ambulances",
  emergencyTrips: "emergencyTrips",
  junctions: "junctions",
  junctionEvents: "junctionEvents",
  loraTelemetry: "loraTelemetry",
  hospitals: "hospitals",
};

export const firebaseApp = initializeApp(firebaseConfig);
export const database = getDatabase(firebaseApp);

export function subscribeToDashboardData(onData) {
  return onValue(ref(database), (snapshot) => {
    onData(snapshot.val() || {});
  });
}

export function writeAmbulanceStatus(ambulanceId, data) {
  return update(ref(database, `${firebasePaths.ambulances}/${ambulanceId}`), data);
}

export function writeTrip(tripId, data) {
  return update(ref(database, `${firebasePaths.emergencyTrips}/${tripId}`), data);
}

export function writeJunction(junctionId, data) {
  return update(ref(database, `${firebasePaths.junctions}/${junctionId}`), data);
}

export function writeJunctionEvent(event) {
  return push(ref(database, firebasePaths.junctionEvents), event);
}

export function writeLoRaTelemetry(junctionId, ambulanceId, data) {
  return update(ref(database, `${firebasePaths.loraTelemetry}/${junctionId}/${ambulanceId}`), data);
}

export function seedHospitals(hospitals) {
  const hospitalMap = Object.fromEntries(hospitals.map((hospital) => [hospital.id, hospital]));
  return set(ref(database, firebasePaths.hospitals), hospitalMap);
}
