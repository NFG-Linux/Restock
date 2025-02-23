package com.example.restock;

import static android.content.Context.CAMERA_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BarcodeScannerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BarcodeScannerFragment extends Fragment {


    private static final String TAG = "BarcodeScannerFragment";


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private CameraDevice cameraDevice;

    private CameraCaptureSession captureSession;
    private TextureView textureView;
    private View barcodeOverlay;
    private BarcodeScanner barcodeScanner;

    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            openCamera();  // handles camera texture
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                // handles texture size changing
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;   //handles texture destruction
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                // handles texture updates
        }
    };

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // Permission granted
                    openCamera();
                } else {
                    // Permission denied
                    Toast.makeText(getContext(), "Camera permission is required to use the barcode scanner", Toast.LENGTH_SHORT).show();
                }
            }
    );

    public BarcodeScannerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BarcodeScannerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BarcodeScannerFragment newInstance(String param1, String param2) {
        BarcodeScannerFragment fragment = new BarcodeScannerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barcode_scanner, container, false);
        textureView = view.findViewById(R.id.texture_view);
        barcodeOverlay = view.findViewById(R.id.barcode_overlay);
        textureView.setSurfaceTextureListener(textureListener);
        Context context = getContext();
        if (context != null) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
            } else {
                openCamera();
            }
        }
        return view;
    }

    @SuppressLint("MissingPermission")
    private void openCamera(){
        if (getContext() == null || getActivity() == null) {
            Log.e(TAG, "Context or Activity is null");
            return;
        }

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
            return;
        }
        CameraManager manager = (CameraManager) getActivity().getSystemService(CAMERA_SERVICE);

        try {
            String cameraId = manager.getCameraIdList()[0];
            manager.openCamera(cameraId, stateCallback, null);
        } catch (Exception e){
            Log.e(TAG, "Error opening camera", e);
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;   // starts new camera session
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
            Log.e(TAG, "Camera error: " + error);
        }
    };
    @SuppressWarnings("resource")
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (texture == null) {
                Log.e(TAG, "SurfaceTexture is null");
                return;
            }
            texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
            Surface surface = new Surface(texture);

            ImageReader imageReader = ImageReader.newInstance(textureView.getWidth(), textureView.getHeight(), ImageFormat.YUV_420_888, 2);
            imageReader.setOnImageAvailableListener(imageAvailableListener, null);

            CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            captureRequestBuilder.addTarget(imageReader.getSurface());

            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) {
                        return;
                    }
                    captureSession = session;
                    try {
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        captureSession.setRepeatingRequest(captureRequestBuilder.build(), captureCallback, null);
                    } catch (CameraAccessException e) {
                        Log.e(TAG, "Error starting camera preview", e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(getContext(), "Failed to start camera preview", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error creating camera preview session", e);
        }
    }


    private final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }
    };

    private final ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            try {
                image = reader.acquireLatestImage();
                if (image != null) {
                    InputImage inputImage = InputImage.fromMediaImage(image, 0);

                    Rect overlayBounds = new Rect();
                    barcodeOverlay.getHitRect(overlayBounds);

                    processImage(inputImage, overlayBounds);
                    image.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing image", e);
                if (image != null) {
                    image.close();
                }
            }
        }
    };

    private void processImage(InputImage inputImage, Rect overlayBounds) {
        barcodeScanner.process(inputImage)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        Rect barcodeBoundingBox = barcode.getBoundingBox();
                        if (barcodeBoundingBox != null && overlayBounds.contains(barcodeBoundingBox)) {
                            Log.d(TAG, "Barcode detected: " + barcode.getRawValue());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Barcode detection failed", e));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (barcodeScanner != null){
            barcodeScanner.close();
        }
    }


}

