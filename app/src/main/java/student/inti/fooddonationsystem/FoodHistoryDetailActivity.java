package student.inti.fooddonationsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FoodHistoryDetailActivity extends AppCompatActivity {

    private TextView foodNameTextView, statusTextView, categoryTextView;
    private EditText locationEditText, quantityEditText, noteEditText;
    private Button saveButton, deleteButton, backButton;
    private ApiService apiService;
    private int foodId;
    private String donationStatus;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_history_detail);

        // Initialize views
        foodNameTextView = findViewById(R.id.foodNameTextView);
        categoryTextView = findViewById(R.id.categoryTextView);
        statusTextView = findViewById(R.id.statusTextView);
        locationEditText = findViewById(R.id.locationEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        noteEditText = findViewById(R.id.noteEditText);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        backButton = findViewById(R.id.backButton);

        // Get data passed from the history list
        foodId = getIntent().getIntExtra("food_id", -1);
        userId = getIntent().getIntExtra("user_id", -1);
        String foodName = getIntent().getStringExtra("food_name");
        String category = getIntent().getStringExtra("category");
        donationStatus = getIntent().getStringExtra("donation_status");
        String location = getIntent().getStringExtra("location");
        String quantity = getIntent().getStringExtra("quantity");
        String note = getIntent().getStringExtra("note");

        // Set the data in views
        foodNameTextView.setText("Food Name: " + foodName);
        categoryTextView.setText("Category: " + category);
        statusTextView.setText("Donation Status: " + donationStatus);
        locationEditText.setText(location);
        quantityEditText.setText(quantity);
        noteEditText.setText(note);

        // Disable editing if donation is successful
        if ("successful".equals(donationStatus)) {
            saveButton.setEnabled(false);
            deleteButton.setEnabled(false);
            locationEditText.setEnabled(false);
            quantityEditText.setEnabled(false);
            noteEditText.setEnabled(false);
        }

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2/fooddonation/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Save Button functionality (for pending donations)
        saveButton.setOnClickListener(v -> {
            String updatedLocation = locationEditText.getText().toString();
            String updatedQuantity = quantityEditText.getText().toString();
            String updatedNote = noteEditText.getText().toString();

            updateFoodDetails(foodId, updatedLocation, updatedQuantity, updatedNote);
        });

        // Delete Button functionality (for pending donations)
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(FoodHistoryDetailActivity.this)
                    .setTitle("Delete Food")
                    .setMessage("Are you sure you want to delete this food item?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteFoodItem(foodId))
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });

        // Back Button functionality
        backButton.setOnClickListener(v -> navigateBackToHistory());
    }

    private void navigateBackToHistory() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);  // Indicate that changes were made
        finish();  // Close the activity
    }

    // Override onBackPressed to ensure the user is taken back with correct data
    @Override
    public void onBackPressed() {
        navigateBackToHistory();
    }

    // Update the food item details
    private void updateFoodDetails(int foodId, String location, String quantity, String note) {
        Call<ResponseData> call = apiService.updateFood(foodId, location, quantity, note);
        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    Toast.makeText(FoodHistoryDetailActivity.this, "Food details updated successfully", Toast.LENGTH_SHORT).show();
                    navigateBackToHistory();  // Go back to HistoryActivity after updating
                } else {
                    Toast.makeText(FoodHistoryDetailActivity.this, "Failed to update food details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Toast.makeText(FoodHistoryDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Delete the food item
    private void deleteFoodItem(int foodId) {
        Call<ResponseData> call = apiService.deleteFood(foodId);
        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    Toast.makeText(FoodHistoryDetailActivity.this, "Food item deleted successfully", Toast.LENGTH_SHORT).show();
                    navigateBackToHistory();  // Go back to HistoryActivity after deleting
                } else {
                    Toast.makeText(FoodHistoryDetailActivity.this, "Failed to delete food item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Toast.makeText(FoodHistoryDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
