package com.example.passwordapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.awt.Desktop;
import java.net.URI;

public class App extends Application {

    private TextField passwordField;
    private Label statusLabel;
    private ProgressBar progressBar;
    private Button lockButton, unlockButton, selectFileButton, openFileButton;
    private File selectedFile;
    private boolean passwordVisible = false;  // Track password visibility
    private boolean isFileLocked = false;  // Flag to track if the file is locked

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("File Lock and Unlock");

        // Layout and scene setup
        BorderPane root = new BorderPane();
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20;");
        HBox hbox = new HBox(10);

        // File selector button
        selectFileButton = new Button("Select File");
        selectFileButton.setOnAction(this::selectFile);

        // Open file button (to prompt for password to open a locked file)
        openFileButton = new Button("Open File");
        openFileButton.setOnAction(this::openFile);

        // Password field (initially a PasswordField to hide text)
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");

        // Eye icon button to toggle password visibility
        Label togglePasswordVisibilityButton = new Label("👁️");
        togglePasswordVisibilityButton.setStyle("-fx-font-size: 18px; -fx-cursor: hand;");
        togglePasswordVisibilityButton.setOnMouseClicked(this::togglePasswordVisibility);

        // Lock button
        lockButton = new Button("Lock File");
        lockButton.setOnAction(this::lockFile);

        // Unlock button
        unlockButton = new Button("Unlock File");
        unlockButton.setOnAction(this::unlockFile);

        // Status label
        statusLabel = new Label("Choose a file and enter password.");

        // Progress bar
        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);

        // Adding components to the layout
        vbox.getChildren().addAll(selectFileButton, passwordField, togglePasswordVisibilityButton, lockButton, unlockButton, openFileButton, progressBar, statusLabel);
        root.setCenter(vbox);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void selectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            statusLabel.setText("Selected: " + selectedFile.getName());
            isFileLocked = selectedFile.getName().endsWith(".locked"); // Check if the file is locked
        } else {
            statusLabel.setText("No file selected.");
        }
    }

    private void lockFile(ActionEvent event) {
        if (selectedFile == null || passwordField.getText().isEmpty()) {
            statusLabel.setText("Please select a file and enter a password.");
            return;
        }

        try {
            String password = passwordField.getText();
            FileLockingService fileLockingService = new FileLockingService();
            progressBar.setVisible(true);

            // Locking the file in a background thread
            new Thread(() -> {
                try {
                    fileLockingService.lockFile(selectedFile, password);
                    // UI update must be done on the JavaFX Application thread
                    Platform.runLater(() -> updateStatus("File locked successfully!", 1));
                } catch (Exception e) {
                    // Handle the error on the JavaFX Application thread
                    Platform.runLater(() -> updateStatus("Error: " + e.getMessage(), 0));
                }
            }).start();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void unlockFile(ActionEvent event) {
        if (selectedFile == null || passwordField.getText().isEmpty()) {
            statusLabel.setText("Please select a file and enter a password.");
            return;
        }

        try {
            String password = passwordField.getText();
            FileLockingService fileLockingService = new FileLockingService();
            progressBar.setVisible(true);

            // Unlocking the file in a background thread
            new Thread(() -> {
                try {
                    fileLockingService.unlockFile(selectedFile, password);
                    // UI update must be done on the JavaFX Application thread
                    Platform.runLater(() -> updateStatus("File unlocked successfully!", 1));
                } catch (Exception e) {
                    // Handle the error on the JavaFX Application thread
                    Platform.runLater(() -> updateStatus("Error: " + e.getMessage(), 0));
                }
            }).start();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void openFile(ActionEvent event) {
        if (selectedFile == null) {
            statusLabel.setText("Please select a file.");
            return;
        }

        // Check if the file is locked (has .locked extension)
        if (selectedFile.getName().endsWith(".locked")) {
            // Ask for password to unlock the file
            TextInputDialog passwordDialog = new TextInputDialog();
            passwordDialog.setTitle("Unlock File");
            passwordDialog.setHeaderText("Enter the password to unlock this file:");
            passwordDialog.setContentText("Password:");

            passwordDialog.showAndWait().ifPresent(password -> {
                try {
                    // Try unlocking the file with the provided password
                    FileLockingService fileLockingService = new FileLockingService();
                    fileLockingService.unlockFile(selectedFile, password);
                    updateStatus("File unlocked successfully!", 1);

                    // After unlocking, open the file in the default system application
                    Desktop.getDesktop().open(selectedFile);

                } catch (Exception e) {
                    updateStatus("Error: Incorrect password or file corruption", 0);
                }
            });
        } else {
            // If the file is not locked, just open it
            try {
                Desktop.getDesktop().open(selectedFile);
            } catch (IOException e) {
                updateStatus("Error: Unable to open file", 0);
            }
        }
    }

    private void togglePasswordVisibility(MouseEvent event) {
        if (passwordVisible) {
            // Switch to PasswordField to hide password
            PasswordField passwordFieldNew = new PasswordField();
            passwordFieldNew.setText(passwordField.getText());
            passwordField = passwordFieldNew;
            ((Label) event.getSource()).setText("👁️");  // Update eye symbol
        } else {
            // Switch to TextField to show password
            TextField passwordFieldNew = new TextField();
            passwordFieldNew.setText(passwordField.getText());
            passwordField = passwordFieldNew;
            ((Label) event.getSource()).setText("👁️");  // Update eye symbol
        }
        passwordVisible = !passwordVisible;
    }

    private void updateStatus(String message, double progress) {
        progressBar.setProgress(progress);
        statusLabel.setText(message);
    }
}


