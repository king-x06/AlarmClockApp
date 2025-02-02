module com.example.alarmclock {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop; // for sound functionality

    exports AlarmClock;  // Export your package
    opens AlarmClock to javafx.graphics;  // Allow JavaFX to access this package via reflection
}
