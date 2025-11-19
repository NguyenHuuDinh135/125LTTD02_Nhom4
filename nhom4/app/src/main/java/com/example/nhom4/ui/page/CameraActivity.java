package com.example.nhom4.ui.page;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.nhom4.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
public class CameraActivity extends AppCompatActivity{
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private CameraSelector cameraSelector;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private boolean isFlashOn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragmentcamera);

        previewView = findViewById(R.id.previewView);
        ImageButton btnTakePhoto = findViewById(R.id.btnTakePhoto);
        ImageButton btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
        ImageButton btnFlash = findViewById(R.id.btnFlash);

        // Kiểm tra permission camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1001);
        }

        // Nút chụp ảnh
        btnTakePhoto.setOnClickListener(v -> takePhoto());

        // Nút chuyển camera
        btnSwitchCamera.setOnClickListener(v -> switchCamera());

        // Nút flash
        btnFlash.setOnClickListener(v -> toggleFlash(btnFlash));
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(getExternalFilesDir(null),
                "IMG_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(CameraActivity.this,
                                "Đã lưu ảnh: " + photoFile.getAbsolutePath(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(CameraActivity.this, "Lỗi chụp ảnh", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void switchCamera() {
        if (cameraSelector == null) return;

        cameraSelector = cameraSelector.equals(CameraSelector.DEFAULT_BACK_CAMERA)
                ? CameraSelector.DEFAULT_FRONT_CAMERA
                : CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.unbindAll();

            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            // Tắt flash khi đổi camera
            isFlashOn = false;
            ImageButton btnFlash = findViewById(R.id.btnFlash);
            btnFlash.setImageResource(R.drawable.ic_flash_off);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleFlash(ImageButton btnFlash) {
        if (camera == null) return;

        if (!isFlashOn) {
            camera.getCameraControl().enableTorch(true);
            btnFlash.setImageResource(R.drawable.ic_flash_on);
            isFlashOn = true;
        } else {
            camera.getCameraControl().enableTorch(false);
            btnFlash.setImageResource(R.drawable.ic_flash_off);
            isFlashOn = false;
        }
    }
}
