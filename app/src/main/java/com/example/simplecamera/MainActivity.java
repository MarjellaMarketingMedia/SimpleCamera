package com.example.simplecamera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

public class MainActivity extends AppCompatActivity {

    private PreviewView previewView;
    private boolean isUsingBackCamera = true; // Flag to track the current camera
    private View change_orientation; // Declare the button here
    private View flash_enabler;
    private View shoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.viewFinder);
        change_orientation = findViewById(R.id.change_orientation); // Initialize the button here
        flash_enabler = findViewById(R.id.flash_enabler);
        shoot = findViewById(R.id.shoot);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1001);
        } else {
            startCamera();
        }

        // Set up a listener for the change_orientation button
        change_orientation.setOnClickListener(v -> toggleCamera());

        flash_enabler.setOnClickListener(v -> enableFlash());

        shoot.setOnClickListener(v -> capturePicture());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = isUsingBackCamera ?
                        CameraSelector.DEFAULT_BACK_CAMERA : CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void toggleCamera() {
        isUsingBackCamera = !isUsingBackCamera; // Toggle the flag

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = isUsingBackCamera ?
                        CameraSelector.DEFAULT_BACK_CAMERA : CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void enableFlash() {
        try {
            CameraSelector cameraSelector = isUsingBackCamera ?
                    CameraSelector.DEFAULT_BACK_CAMERA : CameraSelector.DEFAULT_FRONT_CAMERA;

            // Check if the camera has a flash unit
            if (cameraSelector != null) {
                Camera camera = ProcessCameraProvider.getInstance(this).get().bindToLifecycle(this, cameraSelector);
                if (camera.getCameraInfo().hasFlashUnit()) {
                    // Toggle the torch (flash)
                    boolean isTorchOn = camera.getCameraInfo().getTorchState().getValue() == TorchState.ON;
                    camera.getCameraControl().enableTorch(!isTorchOn);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void capturePicture() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Create an image capture use case
                ImageCapture imageCapture = new ImageCapture.Builder().build();

                // Select the default back camera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Unbind all use cases before binding the capture use case
                cameraProvider.unbindAll();

                // Bind the capture use case to the lifecycle
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture);

                // Capture the image
                imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        // Image captured successfully, you can process it here if needed
                        // Closing the image is not necessary here as it's automatically closed
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        // Image capture failed, handle the error here
                        exception.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

}
