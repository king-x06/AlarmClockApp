package AlarmClock;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {
 private LocalTime alarmTime;
 private Timer timer = new Timer();
 private String soundFile = "Enter your sound file location here"; // Set the correct path to your .wav file
 private boolean alarmPlaying = false;
 private Thread alarmThread; //

 @Override
 public void start(Stage primaryStage) {
  System.out.println("Application started.");

  Label timeLabel = new Label("Enter Alarm Time (HH:MM:SS):");
  TextField timeInput = new TextField();
  Button setAlarmButton = new Button("Set Alarm");
  Label statusLabel = new Label();

  setAlarmButton.setOnAction(e -> {
   try {
    alarmTime = LocalTime.parse(timeInput.getText());
    System.out.println("Alarm set for: " + alarmTime);
    statusLabel.setText("Alarm set for: " + alarmTime);
    scheduleAlarm();
   } catch (Exception ex) {
    System.out.println("Invalid time format! Use HH:MM:SS");
    statusLabel.setText("Invalid time format! Use HH:MM:SS");
   }
  });

  VBox layout = new VBox(10, timeLabel, timeInput, setAlarmButton, statusLabel);
  layout.setStyle("-fx-padding: 20px; -fx-alignment: center;");

  primaryStage.setScene(new Scene(layout, 300, 200));
  primaryStage.setTitle("JavaFX Alarm Clock");


  primaryStage.setOnCloseRequest(event -> stopAlarmAndExit());

  primaryStage.show();
  System.out.println("Primary stage is shown. Ready to set alarm.");
  System.out.println("Enter the time in proper HH:MM:SS");
 }

 private void scheduleAlarm() {
  System.out.println("Scheduling alarm...");

  timer.cancel();
  timer = new Timer();

  long delay = java.time.Duration.between(LocalTime.now(), alarmTime).toMillis();
  if (delay < 0) {
   delay += 24 * 60 * 60 * 1000;
  }

  System.out.println("Alarm scheduled for: " + alarmTime + " with delay: " + delay + " milliseconds.");

  timer.schedule(new TimerTask() {
   @Override
   public void run() {
    System.out.println("Alarm triggered!");
    playSound();
   }
  }, delay);
 }

 private void playSound() {
  if (alarmPlaying) {
   System.out.println("Alarm sound is already playing.");
   return;
  }

  alarmPlaying = true;
  System.out.println("Alarm sound started playing...");

  alarmThread = new Thread(new Runnable() {
   @Override
   public void run() {
    try {
     File audioFile = new File(soundFile);
     System.out.println("Looking for file: ");
     if (!audioFile.exists()) {
      System.out.println("Sound file not found!");
      return;
     }

     AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
     Clip clip = AudioSystem.getClip();
     clip.open(audioStream);

     //Repeat code
     while (alarmPlaying) {
      clip.setFramePosition(0);  // Reset
      clip.start();
      System.out.println("Playing sound...");
      try {
       Thread.sleep(clip.getMicrosecondLength() / 1000);
      } catch (InterruptedException e) {

       System.out.println("Alarm thread was interrupted. Stopping sound playback.");
       break;
      }
     }

     clip.close();
     System.out.println("Sound playback finished.");
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
     e.printStackTrace();
    }
   }
  });

  alarmThread.start();
 }


 private void stopAlarm() {
  if (alarmPlaying) {
   alarmPlaying = false;  // Stop the alarm sound
   System.out.println("Alarm stopped.");
  } else {
   System.out.println("No alarm is currently playing.");
  }
 }

 private void stopAlarmAndExit() {
  System.out.println("Closing application...");

  stopAlarm();


  if (timer != null) {
   timer.cancel();
   System.out.println("Timer canceled.");
  }


  if (alarmThread != null && alarmThread.isAlive()) {
   alarmThread.interrupt();
   System.out.println("Alarm thread interrupted.");
  }

  // Clean up
  System.out.println("Application resources cleaned up.");

  // Exit
  System.exit(0);
 }

 public static void main(String[] args) {
  System.out.println("Launching the application...");
  launch(args);
 }
}
