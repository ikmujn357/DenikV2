const AndroidBridge = {
    // Method to update coordinates in Kotlin
    updateCoordinates: function (latitude, longitude) {
        // Call the method in Kotlin
        Android.updateCoordinates(latitude, longitude);
    },

    // Location request interface
    LocationRequestInterface: {
        // Metoda pro žádost o aktualizace polohy
        requestLocationUpdates: function () {
            // Volání metody v Kotlin
            Android.requestLocationUpdates();
        }
    }
};
