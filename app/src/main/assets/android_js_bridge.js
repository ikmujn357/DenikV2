const AndroidBridge = {
    // Method to update coordinates in Kotlin
    updateCoordinates: function (latitude, longitude) {
        // Call the method in Kotlin
        Android.updateCoordinates(latitude, longitude);
    }
};