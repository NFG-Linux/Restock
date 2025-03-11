package com.example.restock;

// BarcodeScannerFragment.java
// import android.graphics.drawable.Drawable;
import android.widget.EditText;
// import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
// import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.text.InputType;

import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
// import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.core.content.ContextCompat;
// import androidx.lifecycle.LifecycleOwner;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.DocumentReference;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import com.google.firebase.firestore.FirebaseFirestore;
// import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;

import java.util.List;
// import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class BarcodeScannerFragment extends Fragment {

    private static final String TAG = "BarcodeScannerFragment";

    private ProcessCameraProvider cameraProvider;
    private PreviewView previewView;
    private View barcodeOverlay;
    private BarcodeScanner barcodeScanner;
    private ExecutorService cameraExecutor;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private TextView barcodeResultTextView;

    private BarcodeAnalyzer barcodeAnalyzerInstance; // pause scanner

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        // back button - angie
        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp(); // Go back to the previous screen
        });

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
                imageAnalyzer.setAnalyzer(cameraExecutor, barcodeAnalyzerInstance = new BarcodeAnalyzer(barcodes -> { // Store instance
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (!barcodes.isEmpty()) {
                                Barcode barcode = barcodes.get(0);
                                String rawValue = barcode.getRawValue();
                                Log.d(TAG, "Barcode detected: " + rawValue);
                                barcodeResultTextView.setText("Scanning...");
                                checkBarcodeInDatabase(rawValue, 0);
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

        private boolean isScanningEnabled = true; // pause scanner

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
            Log.d(TAG, "analyze: isScanningEnabled = " + isScanningEnabled);
            if (!isScanningEnabled) {
                imageProxy.close();
                return;
            }

            Image mediaImage = Objects.requireNonNull(imageProxy.getImage());
            InputImage inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            barcodeScanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        if (listener != null && !barcodes.isEmpty()) {
                            listener.onBarcodesDetected(barcodes);
                            Log.d(TAG, "analyze: barcode detected, disabling scanning");
                            isScanningEnabled = false;
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BarcodeAnalyzer", "Barcode detection failed", e);
                        Log.d(TAG, "BarcodeAnalyzer: Barcode processing failed");
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        }

        public void enableScanning() {
            isScanningEnabled = true;
        }

    }

    private void checkBarcodeInDatabase(String barcode, Integer qty) {
        Log.d(TAG, "Checking imported_barcodes for barcode: " + barcode);
        db.collection("imported_barcodes").document(barcode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (documentSnapshot.exists()) {
                                String productName = documentSnapshot.getString("product_name");
                                String brand = documentSnapshot.getString("brand");
                                String category = documentSnapshot.getString("category");
                                String ingredients = documentSnapshot.getString("ingredients_text");

                                Log.d(TAG, "Imported Barcode Details:");
                                Log.d(TAG, "Product Name: " + productName);
                                Log.d(TAG, "Brand: " + brand);
                                Log.d(TAG, "Category: " + category);
                                Log.d(TAG, "Ingredients: " + ingredients);

                                Log.d(TAG, "Found the item in imported_barcodes");

                                showItemDetailsDialog(productName, brand, category, ingredients);
                                setOverlaySuccess();
                                addItemToPantry(barcode, productName, qty);
                            } else {
                                Log.d(TAG, "Didn't find in imported_barcodes, searching in user_created_barcodes");
                                checkUserCreatedBarcodes(barcode, qty);
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
                            Log.d(TAG, "Database check failed, resetting scanner (imported_barcodes)");
                            resetScanner();
                        });
                    }
                });
    }

    private void checkUserCreatedBarcodes(String barcode, Integer qty) {
        db.collection("user_created_barcodes").document(barcode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (documentSnapshot.exists()) {
                                String productName = documentSnapshot.getString("product_name");
                                String brand = documentSnapshot.getString("brand");
                                String category = documentSnapshot.getString("category");
                                String ingredients = documentSnapshot.getString("ingredients_text");

                                Log.d(TAG, "User Created Barcode Details:");
                                Log.d(TAG, "Product Name: " + productName);
                                Log.d(TAG, "Brand: " + brand);
                                Log.d(TAG, "Category: " + category);
                                Log.d(TAG, "Ingredients: " + ingredients);

                                showItemDetailsDialog(productName, brand, category, ingredients);
                                setOverlaySuccess();
                                addItemToPantry(barcode, productName, qty);
                            } else {
                                promptUserToAddBarcode(barcode);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            barcodeResultTextView.setText("");
                            Log.e(TAG, "Error checking user_created_barcodes", e);
                            setOverlayFailure();
                            Log.d(TAG, "Database check failed, resetting scanner (user_created_barcodes)");
                            resetScanner();
                        });
                    }
                });
    }

    private void promptUserToAddBarcode(String barcode) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setTitle("Barcode Not Found")
                        .setMessage("Would you like to add it?")
                        .setPositiveButton("Yes", (dialog, which) -> showAddBarcodeDialog(barcode))
                        .setNegativeButton("No", (dialog, which) -> {
                            setOverlayFailure();
                            Log.d(TAG, "showAddBarcodeDialog no button pressed");
                        })
                        .setOnDismissListener(dialogInterface -> resetScanner());
                builder.show();
            });
        }
    }

    private void showAddBarcodeDialog(String barcode) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Item Details");


        final EditText productNameInput = new EditText(getContext());
        productNameInput.setHint("Product Name");
        final EditText brandInput = new EditText(getContext());
        brandInput.setHint("Brand");
        final EditText categoryInput = new EditText(getContext());
        categoryInput.setHint("Category");
        final EditText ingredientsInput = new EditText(getContext());
        ingredientsInput.setHint("Ingredients");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(productNameInput);
        layout.addView(brandInput);
        layout.addView(categoryInput);
        layout.addView(ingredientsInput);
        builder.setView(layout);


        builder.setPositiveButton("Add", (dialog, which) -> {
            String productName = productNameInput.getText().toString();
            String brand = brandInput.getText().toString();
            String category = categoryInput.getText().toString();
            String ingredients = ingredientsInput.getText().toString();
            addItemToUserDatabase(barcode, productName, brand, category, ingredients);
            resetScanner();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            setOverlayFailure();
            resetScanner();
            Log.d(TAG, "showAddBarcodeDialog cancel button pressed");
        });

        builder.show();
    }

    private void showItemDetailsDialog(String productName, String brand, String category, String ingredients) {
        new AlertDialog.Builder(getContext())
                .setTitle("Item Details")
                .setMessage("Product: " + productName + "\nBrand: " + brand + "\nCategory: " + category + "\nIngredients: " + ingredients)
                .setPositiveButton("OK", (dialog, which) -> resetScanner())
                .show();
    }

    private void addItemToUserDatabase(String barcode, String productName, String brand, String category, String ingredients) {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String userEmail = auth.getCurrentUser().getEmail();
        String namePull = userEmail.split("@")[0];
        String pantryDocId = namePull + "-" + barcode;

        db.collection("user_created_barcodes").document(barcode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        db.collection("pantry_items").document(pantryDocId).get()
                                .addOnSuccessListener(pantryDoc -> {
                                    Long existingQty = pantryDoc.getLong("quantity");
                                    if (existingQty == null) existingQty = 0L;
                                    showUpdateQuantityDialog(barcode, productName, pantryDocId, existingQty);
                                })
                                        .addOnFailureListener(e -> Log.e(TAG, "Couldn't get item quantity", e));
                    } else {
                        Map<String, Object> barcodeData = new HashMap<>();
                        barcodeData.put("code", barcode);
                        barcodeData.put("product_name", productName);
                        barcodeData.put("brand", brand);
                        barcodeData.put("category", category);
                        barcodeData.put("ingredients_text", ingredients);
                        barcodeData.put("added_by", Objects.requireNonNull(auth.getCurrentUser()).getEmail());
                        barcodeData.put("timestamp", FieldValue.serverTimestamp());

                        db.collection("user_created_barcodes").document(barcode)
                                .set(barcodeData)
                                .addOnSuccessListener(aVoid -> {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            barcodeResultTextView.setText("Item Added!");
                                            Log.d(TAG, "Item added successfully!");
                                            setOverlaySuccess();
                                            showQuantityDialog(pantryDocId, productName);
                                            showItemDetailsDialog(productName, brand, category, ingredients);
                                        });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            barcodeResultTextView.setText("");
                                            Log.e(TAG, "Error adding item", e);
                                            setOverlayFailure();
                                            resetScanner();
                                        });
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for existing barcode", e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            barcodeResultTextView.setText("");
                            setOverlayFailure();
                            resetScanner();
                        });
                    }
                });
    }

    private void showQuantityDialog(String pantryDocId, String productName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter Quantity");
        builder.setMessage("How many of this item should we add to your pantry?");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String inputText = input.getText().toString();
            if (!inputText.isEmpty()) {
                try {
                    int quantity = Integer.parseInt(inputText);

                    DocumentReference pantryRef = db.collection("pantry_items").document(pantryDocId);

                    Map<String, Object> pantryItem = new HashMap<>();
                    pantryItem.put("code", pantryDocId.split("-")[1]); // Extract barcode
                    pantryItem.put("product_name", productName);
                    pantryItem.put("quantity", quantity);
                    pantryItem.put("user_id", auth.getCurrentUser().getUid());
                    pantryItem.put("email", auth.getCurrentUser().getEmail());
                    pantryItem.put("timestamp", FieldValue.serverTimestamp());

                    pantryRef.set(pantryItem)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Item added to pantry: " + productName))
                            .addOnFailureListener(e -> Log.e(TAG, "Error adding to pantry", e));

                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid quantity entered", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> resetScanner());
        builder.show();
    }

    private void showAlreadyExistsDialog(String pantryDocId, String productName) {
        db.collection("pantry_items").document(pantryDocId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        AtomicReference<Long> existingQty = new AtomicReference<>(documentSnapshot.getLong("quantity"));
                        if (existingQty.get() == null) existingQty.set(0L);

                        getActivity().runOnUiThread(() -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Item Already in Pantry");
                            builder.setMessage("This item is already in your pantry. Current quantity: " + String.valueOf(existingQty.get()) +
                                    "\nHow many more would you like to add?");

                            final EditText input = new EditText(getContext());
                            input.setInputType(InputType.TYPE_CLASS_NUMBER);
                            builder.setView(input);

                            builder.setPositiveButton("Update", (dialog, which) -> {
                                String inputText = input.getText().toString();
                                if (!inputText.isEmpty()) {
                                    try {
                                        int additionalQty = Integer.parseInt(inputText);
                                        updatePantryQuantity(pantryDocId, existingQty.get(), additionalQty);
                                    } catch (NumberFormatException e) {
                                        Toast.makeText(getContext(), "Invalid quantity entered", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            builder.setNegativeButton("Cancel", (dialog, which) -> resetScanner());
                            builder.show();
                        });
                    }
                });
    }

    private void addItemToPantry(String barcode, String productName, Integer qty) {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String userEmail = auth.getCurrentUser().getEmail();

        String namePull = userEmail.split("@")[0];

        String pantryDocId = namePull + "-" + barcode;
        DocumentReference pantryRef = db.collection("pantry_items").document(pantryDocId);

        pantryRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long existingQty = documentSnapshot.getLong("quantity");
                if (existingQty == null) existingQty = 0L;

                Log.d(TAG, "Item in pantry already, asking user to update quantity");
                showAlreadyExistsDialog(pantryDocId, productName);
            } else {
                Log.d(TAG, "New item; asking user for initial quantity");
                showQuantityDialog(pantryDocId, productName);
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error checking pantry_items collection", e));
    }

    private void showUpdateQuantityDialog(String barcode, String productName, String pantryDocId, Long existingQty) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Update Item Quantity");
        builder.setMessage("This item was found in your pantry. \nCurrent quantity: " + existingQty + "\nHow many would you like to add?");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String inputText = input.getText().toString();
            if (!inputText.isEmpty()) {
                try {
                    int additionalQty = Integer.parseInt(inputText);
                    updatePantryQuantity(pantryDocId, existingQty, additionalQty);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid quantity entered", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> resetScanner());
        builder.show();
    }

    private void updatePantryQuantity(String pantryDocId, Long existingQty, int additionalQty) {
        DocumentReference pantryRef = db.collection("pantry_items").document(pantryDocId);

        Long newQty = existingQty + additionalQty;

        pantryRef.update("quantity", newQty)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Updated item quantity to: " + newQty))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating pantry quantity", e));
    }

    private void resetScanner() {
        if (barcodeAnalyzerInstance != null) {
            Log.d(TAG, "resetScanner: Enabling scanning");
            barcodeAnalyzerInstance.enableScanning();
        } else {
            Log.d(TAG, "resetScanner: barcodeAnalyzerInstance is null");
        }
        Log.d(TAG, "resetScanner() called");
        resetOverlay();
    }

    private void setOverlaySuccess() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Log.d(TAG, "setOverlaySuccess() called");
                barcodeOverlay.setSelected(true);
                barcodeOverlay.setActivated(false);
            });
        }
    }

    private void setOverlayFailure() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Log.d(TAG, "setOverlayFailure() called");
                resetOverlay();
                barcodeOverlay.setSelected(false);
                barcodeOverlay.setActivated(true);
            });
        }
    }

    private void resetOverlay(){
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Log.d(TAG, "resetOverlay() called");
                barcodeOverlay.setSelected(false);
                barcodeOverlay.setActivated(false);
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