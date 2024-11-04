package student.inti.fooddonationsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    Button logoutBtn, donateFoodBtn, takeAwayFoodBtn, tipsBtn, donationHistoryBtn, takeawayHistoryBtn;
    TextView welcomeTextView;
    int userId;
    String username;

    private static final int REQUEST_CODE_HISTORY_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize buttons and TextView
        logoutBtn = findViewById(R.id.logoutBtn);
        donateFoodBtn = findViewById(R.id.donateFoodBtn);
        takeAwayFoodBtn = findViewById(R.id.takeAwayFoodBtn);
        tipsBtn = findViewById(R.id.tipsBtn);
        donationHistoryBtn = findViewById(R.id.donationHistoryBtn);
        takeawayHistoryBtn = findViewById(R.id.takeawayHistoryBtn);
        welcomeTextView = findViewById(R.id.welcomeTextView);

        // Get the user ID and username passed from the previous activity
        userId = getIntent().getIntExtra("user_id", -1);
        username = getIntent().getStringExtra("username");

        // Set the welcome message
        setWelcomeMessage();

        // Logout Button click listener
        logoutBtn.setOnClickListener(v -> {
            // Show confirmation dialog before logging out
            showLogoutConfirmationDialog();
        });

        // Donate Food Button click listener
        donateFoodBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DonateFoodActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Take Away Food Button click listener
        takeAwayFoodBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TakeAwayFoodActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("username", username);  // Pass the username
            startActivity(intent);
        });

        // Donation History Button click listener
        donationHistoryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("username", username);
            intent.putExtra("history_type", "donation");
            startActivityForResult(intent, REQUEST_CODE_HISTORY_ACTIVITY);
        });

        // Takeaway History Button click listener
        takeawayHistoryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("username", username);
            intent.putExtra("history_type", "takeaway");
            startActivityForResult(intent, REQUEST_CODE_HISTORY_ACTIVITY);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_HISTORY_ACTIVITY && resultCode == RESULT_OK && data != null) {
            userId = data.getIntExtra("user_id", -1);
            username = data.getStringExtra("username");
            setWelcomeMessage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get the user ID and username passed from the previous activity
        userId = getIntent().getIntExtra("user_id", -1);
        username = getIntent().getStringExtra("username");

        // Set the welcome message with the username
        setWelcomeMessage();
    }

    private void setWelcomeMessage() {
        if (username != null && !username.isEmpty()) {
            welcomeTextView.setText("Welcome, " + username);  // Display the username
        } else {
            welcomeTextView.setText("Welcome");
        }
    }

    private void showLogoutConfirmationDialog() {
        // Create an AlertDialog for confirmation
        new AlertDialog.Builder(HomeActivity.this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // User confirmed, proceed with logout
                    startActivity(new Intent(HomeActivity.this, MainActivity.class));
                    finish(); // Close the current activity
                })
                .setNegativeButton("No", null) // Dismiss the dialog if "No" is pressed
                .show();
    }
}
