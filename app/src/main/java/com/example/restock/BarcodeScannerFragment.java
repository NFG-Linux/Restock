package com.example.restock;

import android.graphics.drawable.Drawable;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

// ListenableFuture import
import com.google.common.util.concurrent.ListenableFuture;

// Barcode scanner & Firestore
import androidx.appcompat.app.AlertDialog;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;

import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BarcodeScannerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BarcodeScannerFragment extends Fragment {


    private static final String TAG = "BarcodeScannerFragment";


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ProcessCameraProvider cameraProvider;
    private PreviewView previewView;
    private View barcodeOverlay;
    private BarcodeScanner barcodeScanner;
    private ExecutorService cameraExecutor;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private TextView barcodeResultTextView;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
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
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barcode_scanner, container, false);
        previewView = view.findViewById(R.id.preview_view);
        barcodeOverlay = view.findViewById(R.id.barcode_overlay);
        barcodeResultTextView = view.findViewById(R.id.barcode_result);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraExecutor = Executors.newSingleThreadExecutor();
        barcodeScanner = BarcodeScanning.getClient(
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build()
        );
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        Context context = getContext();
        if (context != null) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
            } else {
                startCamera();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalyzer.setAnalyzer(cameraExecutor, new BarcodeAnalyzer(barcodes -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (!barcodes.isEmpty()) {
                                Barcode barcode = barcodes.get(0);
                                String rawValue = barcode.getRawValue();
                                Log.d(TAG, "Barcode detected: " + rawValue);
                                barcodeResultTextView.setText("Scanning...");
                                checkBarcodeInDatabase(rawValue);
                            } else {
                                barcodeResultTextView.setText("");
                            }
                        });
                    }
                }, barcodeScanner));

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer);

            } catch (Exception exc) {
                Log.e(TAG, "Error starting camera", exc);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private static class BarcodeAnalyzer implements ImageAnalysis.Analyzer {
        private final BarcodeScanner barcodeScanner;

        private final Listener listener;

        BarcodeAnalyzer(Listener listener, BarcodeScanner barcodeScanner) {
            this.listener = listener;
            this.barcodeScanner = barcodeScanner;
        }

        interface Listener {
            void onBarcodesDetected(List<Barcode> barcodes);
        }

        @OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            Image mediaImage = Objects.requireNonNull(imageProxy.getImage());
            InputImage inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            barcodeScanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        if (listener != null) {
                            listener.onBarcodesDetected(barcodes);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("BarcodeAnalyzer", "Barcode detection failed", e))
                    .addOnCompleteListener(task -> imageProxy.close());
        }

    }

    private void checkBarcodeInDatabase(String barcode) {
        db.collection("imported_barcodes").document(barcode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (documentSnapshot.exists()) {
                                barcodeResultTextView.setText("Product found: " + documentSnapshot.getString("product_name"));
                                Toast.makeText(getContext(), "Product found: " + documentSnapshot.getString("product_name"), Toast.LENGTH_SHORT).show();
                                setOverlaySuccess();
                            } else {
                                checkUserCreatedBarcodes(barcode);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            barcodeResultTextView.setText("");
                            Log.e(TAG, "Error checking imported_barcodes", e);
                            setOverlayFailure();
                        });
                    }
                });
    }

    private void checkUserCreatedBarcodes(String barcode) {
        db.collection("user_created_barcodes").document(barcode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (documentSnapshot.exists()) {
                                barcodeResultTextView.setText("Product found in user database!");
                                Toast.makeText(getContext(), "Product found in user database!", Toast.LENGTH_SHORT).show();
                                setOverlaySuccess();
                            } else {
                                promptUserToAddBarcode(barcode);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if(getActivity() != null){
                        getActivity().runOnUiThread(() -> {
                            barcodeResultTextView.setText("");
                            Log.e(TAG, "Error checking user_created_barcodes", e);
                            setOverlayFailure();
                        });
                    }
                });
    }

    private void promptUserToAddBarcode(String barcode) {
        if(getActivity() != null){
            getActivity().runOnUiThread(() -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Barcode Not Found")
                        .setMessage("Would you like to add it?")
                        .setPositiveButton("Yes", (dialog, which) -> addBarcodeToUserDatabase(barcode))
                        .setNegativeButton("No", null)
                        .show();
                setOverlayFailure();
            });
        }

    }

    private void addBarcodeToUserDatabase(String barcode) {
        Map<String, Object> barcodeData = new HashMap<>();
        barcodeData.put("code", barcode);
        barcodeData.put("product_name", "");
        barcodeData.put("brand", "");
        barcodeData.put("category", "");
        barcodeData.put("ingredients_text", "");
        barcodeData.put("added_by", Objects.requireNonNull(auth.getCurrentUser()).getEmail());
        barcodeData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("user_created_barcodes").document(barcode)
                .set(barcodeData)
                .addOnSuccessListener(aVoid -> {
                    if(getActivity() != null){
                        getActivity().runOnUiThread(() -> {
                            barcodeResultTextView.setText("Barcode Added!");
                            Log.d(TAG, "Barcode added successfully!");
                            setOverlaySuccess();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if(getActivity() != null){
                        getActivity().runOnUiThread(() -> {
                            barcodeResultTextView.setText("");
                            Log.e(TAG, "Error adding barcode", e);
                            setOverlayFailure();
                        });
                    }
                });
    }

    private void setOverlaySuccess(){
        if(getActivity() != null){
            getActivity().runOnUiThread(() ->{
                barcodeOverlay.setBackgroundResource(R.drawable.barcode_outline_success);
            });
        }
    }

    private void setOverlayFailure(){
        if(getActivity() != null){
            getActivity().runOnUiThread(() ->{
                barcodeOverlay.setBackgroundResource(R.drawable.barcode_outline_failure);
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }


}

