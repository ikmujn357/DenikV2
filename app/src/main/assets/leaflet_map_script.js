// Leaflet map initialization
const map = L.map('map').setView([49.8175, 15.473], 6); // Set initial view to Czech Republic
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {}).addTo(map);

// Marker for the current location
let marker;

// Function to add marker at given coordinates
function addMarker(latitude, longitude) {
    // Create a marker and add it to the map
    marker = L.marker([latitude, longitude]).addTo(map);

    // Call the method in Kotlin to update coordinates
    AndroidBridge.updateCoordinates(latitude, longitude);
}

// Function to remove existing marker
function removeMarker() {
    if (marker) {
        map.removeLayer(marker);
    }
}

// Function to update map to current location
function updateMapToCurrentLocation(latitude, longitude) {
    // Update the map view to the current location
    map.setView([latitude, longitude], 13);

    // Remove existing marker and add a new one at the current location
    removeMarker();
    addMarker(latitude, longitude);
}

// Function to perform search based on input text
function performSearch(locationQuery) {
    // AJAX request for geocoding location
    $.ajax({
        url: 'https://nominatim.openstreetmap.org/search',
        method: 'GET',
        data: { q: locationQuery, format: 'json', limit: 1 },
        success: function (response) {
            if (response.length > 0) {
                // Get the first result
                const result = response[0];
                const latitude = parseFloat(result.lat);
                const longitude = parseFloat(result.lon);

                // Update map to the searched location
                updateMapToCurrentLocation(latitude, longitude);

                // Call method in Kotlin to update coordinates
                Android.updateCoordinates(latitude, longitude);
            } else {
                // Alert if location not found
                alert("Location not found");
            }
        },
        error: function (xhr) {
            // Show error if data loading fails
            console.error('Error:', xhr.status);
            alert("Error loading location data");
        }
    });
}

// Add event listener for Enter key press in search input
document.getElementById('searchInput').addEventListener('keyup', function (event) {
    if (event.key === 'Enter') {
        const locationQuery = event.target.value.trim();
        if (locationQuery !== "") {
            performSearch(locationQuery);
        } else {
            alert("Please enter a location");
        }
    }
});

// Add event listener for map click event
map.on('click', function(event) {
    const clickedLatLng = event.latlng;
    removeMarker();
    addMarker(clickedLatLng.lat, clickedLatLng.lng);
});

// Wait for the DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function () {
    // Add event listener for Get Location button click
    document.getElementById('getLocationButton').addEventListener('click', function () {
        // Call the function to retrieve the location when the button is clicked
        getLocation();
    });
});

// Function to retrieve the location
function getLocation() {
    // Call the function defined in the Android JavaScript interface to request location updates
    LocationRequestInterface.requestLocationUpdates();
}

function updateCoordinates(latitude, longitude) {
    // Update the coordinates in the Android code
    if (typeof AndroidBridge !== 'undefined') {
        AndroidBridge.updateCoordinates(latitude, longitude);
    }

    // Update the coordinates in the text field or do whatever is needed
    document.getElementById('coordinates').value = latitude + ', ' + longitude;
}
