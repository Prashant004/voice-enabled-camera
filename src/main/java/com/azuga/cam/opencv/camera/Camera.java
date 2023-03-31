package com.azuga.cam.opencv.camera;

import com.azuga.cam.constants.AzugaConstants;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Camera extends JFrame {

    // camera screen
    private final JLabel cameraScreen;
    private final JButton btnCapture;
    private VideoCapture capture;
    private Mat image;
    private boolean clicked = false;


    public Camera() {
        // design UI
        setLayout(null);
        cameraScreen = new JLabel();
        cameraScreen.setBounds(0, 0, 640, 480);
        add(cameraScreen);

        btnCapture = new JButton("capture");
        btnCapture.setBounds(300, 480, 80, 40);
        add(btnCapture);

        btnCapture.addActionListener((ActionEvent e) -> {
            clicked = true;
        });

        // stop application when window closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                capture.release();
                image.release();
                System.exit(0);
            }
        });

        setSize(new Dimension(640, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) throws IOException {

        // Start speech recognition
        // Set up the configuration
        Configuration configuration = new Configuration();
//        configuration.setAcousticModelPath("/Users/azuga/Documents/POC/boot-cam/cam/src/main/resources/en-us");
//        configuration.setDictionaryPath("/Users/azuga/Documents/POC/boot-cam/cam/src/main/resources/speech.dict");
//        configuration.setLanguageModelPath("/Users/azuga/Documents/POC/boot-cam/cam/src/main/resources/en-us.lm.bin");
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("/Users/azuga/Documents/tutorials/voice-enabled-camera/src/main/resources/5023.dic");
        configuration.setLanguageModelPath("/Users/azuga/Documents/tutorials/voice-enabled-camera/src/main/resources/5023.lm");

        // Create the live recognizer
        LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);

        // Start recognition
        recognizer.startRecognition(true);

        // Loop until the user says "stop"
        System.out.println("Start speaking. Say 'stop' to exit.");


        while (true) {
            // Get the next command
            System.out.println("Listening...");
            String speech = recognizer.getResult().getHypothesis();
            System.out.println("You said: " + speech);

            switch (speech) {
                case "START CAMERA STREAMING":
                    StartCameraStreaming();
                    break;
                case "OPEN AZUGA FLEET PORTAL":
                    Runtime.getRuntime().exec(new String[]{AzugaConstants.terminal, AzugaConstants.option,
                            AzugaConstants.browser, AzugaConstants.livemap});
                    break;
                case "OPEN TRIPS REPORT":
                    Runtime.getRuntime().exec(new String[]{AzugaConstants.terminal, AzugaConstants.option,
                            AzugaConstants.browser, AzugaConstants.trips});
                    break;
                case "OPEN ALERTS REPORT":
                    Runtime.getRuntime().exec(new String[]{AzugaConstants.terminal, AzugaConstants.option,
                            AzugaConstants.browser, AzugaConstants.alerts});
                    break;
                case "OPEN BREADCRUMB REPORT":
                    Runtime.getRuntime().exec(new String[]{AzugaConstants.terminal, AzugaConstants.option,
                            AzugaConstants.browser, AzugaConstants.breadcrumb});
                    break;

            }
            // Exit the loop if the user says "stop"
            if (speech.equalsIgnoreCase("stop")) {
                break;
            }
        }
        // Stop recognition
        recognizer.stopRecognition();
    }

    private static void StartCameraStreaming() {
        // put this in a method
        OpenCV.loadLocally();
        System.out.println("Load successful");
        EventQueue.invokeLater(() -> {
            Camera camera = new Camera();

            //start new thread in camera
            new Thread(() -> {
                camera.startCamera();
            }).start();
        });
    }

    // create camera
    public void startCamera() {
//        capture = new VideoCapture("rtsp://192.168.1.94:8080/h264_ulaw.sdp");
        capture = new VideoCapture(0);
        image = new Mat();
        byte[] imageData;
        ImageIcon icon;
        while (true) {
            // read image to matrix
            capture.read(image);
//            capture.open("http://192.168.1.94:6677/index");
            // convert matrix to byte
            final MatOfByte buf = new MatOfByte();
            Imgcodecs.imencode(".jpg", image, buf);
            imageData = buf.toArray();

            // add to JLabel
            icon = new ImageIcon(imageData);
            cameraScreen.setIcon(icon);

            //capture and save to file
            if (clicked) {
                //prompt for image name
                String name = JOptionPane.showInputDialog("Enter image name");
                if (name == null) {
                    name = new SimpleDateFormat("dd-mm-yyyy-hh-mm-ss").format(new Date());
                }
                //write to file
                Imgcodecs.imwrite("images/" + name + ".jpg", image);
                clicked = false;
            }
        }
    }
}
