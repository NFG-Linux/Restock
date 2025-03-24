package com.example.restock;

// MainActivity.java
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
// import android.widget.ImageView;
//import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
// import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.restock.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Set up navigation controller
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // Listen for navigation changes to hide/show header & footer
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean hideNavigation = (destination.getId() == R.id.FirstFragment ||
                    destination.getId() == R.id.LoginFragment ||
                    destination.getId() == R.id.RegisterFragment ||
                    destination.getId() == R.id.profileFragment ||
                    destination.getId() == R.id.notificationFragment)||
                    destination.getId() == R.id.BarcodeScannerFragment;

            findViewById(R.id.header_fragment).setVisibility(hideNavigation ? View.GONE : View.VISIBLE);
            findViewById(R.id.footer_fragment).setVisibility(hideNavigation ? View.GONE : View.VISIBLE);
        });

        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}