package com.example.restock;

// ProfileFragment.java

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private ImageView backButton;
    private CircleImageView profileImageView;
    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private MaterialButton saveButton;
    private TextView uploadText;

    private Uri imageUri;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private FirebaseUser user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            imageUri = data.getData();
                            profileImageView.setImageURI(imageUri);
                            uploadImageToFirebase();
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        backButton = rootView.findViewById(R.id.back_button);
        profileImageView = rootView.findViewById(R.id.profile_image);
        nameEditText = rootView.findViewById(R.id.name_edit_text);
        emailEditText = rootView.findViewById(R.id.email_edit_text);
        saveButton = rootView.findViewById(R.id.save_button);
        uploadText = rootView.findViewById(R.id.upload_text);

        backButton.setOnClickListener(view -> Navigation.findNavController(view).navigateUp());

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            emailEditText.setText(user.getEmail());
            loadUserData();
        }


        profileImageView.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        saveButton.setOnClickListener(view -> saveUserData());

        return rootView;
    }


    private void loadUserData() {
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String imageUrl = documentSnapshot.getString("profileImageUrl");

                if (name != null) {
                    nameEditText.setText(name);
                }

                if (imageUrl != null) {
                    Glide.with(this).load(imageUrl).into(profileImageView);
                }
            }
        });
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_images/" + user.getUid() + ".jpg");
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> saveImageUrlToDatabase(uri.toString()));
            }).addOnFailureListener(e -> Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void saveImageUrlToDatabase(String imageUrl) {
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).update("profileImageUrl", imageUrl);
    }

    private void saveUserData() {
        String name = Objects.requireNonNull(nameEditText.getText()).toString().trim();
        if (!name.isEmpty()) {
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).update("name", name);
        }
    }
}
