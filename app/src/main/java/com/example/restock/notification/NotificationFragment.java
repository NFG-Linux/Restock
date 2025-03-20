package com.example.restock.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restock.R;

import java.util.ArrayList;
import java.util.List;

// NotificationFragment.java
// Fragment responsible for displaying notifications
public class NotificationFragment extends Fragment {

    ImageView backButton;
    RecyclerView notificationRecyclerView;
    NotificationAdapter notificationAdapter;
    List<NotificationItem> notificationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);

        // Back button
        backButton = rootView.findViewById(R.id.back_button);
        backButton.setOnClickListener(view -> Navigation.findNavController(view).navigateUp());

        // RecyclerView
        notificationRecyclerView = rootView.findViewById(R.id.notification_recycler_view);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Dummy notifications (replace later with real data)
        notificationList = new ArrayList<>();
        notificationList.add(new NotificationItem("Restock Reminder", "Milk is running low.", "10:30 AM"));
        notificationList.add(new NotificationItem("Item Expired", "Eggs expired yesterday.", "Yesterday"));

        // Set up adapter
        notificationAdapter = new NotificationAdapter(notificationList);
        notificationRecyclerView.setAdapter(notificationAdapter);

        return rootView;
    }
}

// -------- Documentation -------- //
// Fragment responsible for displaying the notifications screen
// Initializes the RecyclerView, sets up the NotificationAdapter
// Navigation back to the previous screen using the back button