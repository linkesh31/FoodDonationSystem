package student.inti.fooddonationsystem;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DonateFoodActivity extends AppCompatActivity {

    private EditText foodName, expiryDate, quantity, note, location;
    private Spinner categorySpinner;
    private Button addFoodBtn, backBtn;

    private int userId;  // Logged-in user ID
    private String username;  // Logged-in username

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate_food);

        // Get the user ID and username passed from HomeActivity
        userId = getIntent().getIntExtra("user_id", -1);
        username = getIntent().getStringExtra("username");

        // Initialize UI components
        foodName = findViewById(R.id.foodName);
        expiryDate = findViewById(R.id.expiryDate);
        quantity = findViewById(R.id.quantity);
        note = findViewById(R.id.note);
        location = findViewById(R.id.location);
        categorySpinner = findViewById(R.id.categorySpinner);
        addFoodBtn = findViewById(R.id.addFoodBtn);
        backBtn = findViewById(R.id.backBtn);

        // Populate the spinner with categories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Set up the date picker for expiry date
        expiryDate.setOnClickListener(v -> showDatePicker());

        // Handle Add Food button click with validation
        addFoodBtn.setOnClickListener(v -> {
            String food = foodName.getText().toString();
            String expiry = expiryDate.getText().toString();
            String qty = quantity.getText().toString();
            String cat = categorySpinner.getSelectedItem().toString();
            String foodNote = note.getText().toString(); // Optional field
            String loc = location.getText().toString();

            // Validate input fields before proceeding
            if (validateInputs(food, expiry, qty, cat, loc)) {
                // Log all values to check correctness before API call
                Log.d("DonateFoodActivity", "Food Name: " + food);
                Log.d("DonateFoodActivity", "Expiry Date: " + expiry);
                Log.d("DonateFoodActivity", "Quantity: " + qty);
                Log.d("DonateFoodActivity", "Category: " + cat);
                Log.d("DonateFoodActivity", "Location: " + loc);
                Log.d("DonateFoodActivity", "User ID: " + userId);

                // Make API call to add food, passing the userId
                addFood(food, expiry, qty, cat, foodNote, loc, userId);
            }
        });

        // Handle Back button click
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DonateFoodActivity.this, HomeActivity.class);
            intent.putExtra("user_id", userId);  // Pass the user ID back to HomeActivity
            intent.putExtra("username", username);  // Pass the username back to HomeActivity
            startActivity(intent);
            finish();
        });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(DonateFoodActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    expiryDate.setText(formattedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    // Function to validate inputs
    private boolean validateInputs(String food, String expiry, String quantity, String category, String location) {
        if (food.isEmpty()) {
            foodName.setError("Food name is required");
            return false;
        }
        if (expiry.isEmpty()) {
            expiryDate.setError("Expiry date is required");
            return false;
        }
        if (quantity.isEmpty()) {
            this.quantity.setError("Quantity is required");
            return false;
        }
        if (category.isEmpty()) {
            Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (location.isEmpty()) {
            this.location.setError("Location is required");
            return false;
        }
        return true; // All validations passed
    }

    private void addFood(String foodName, String expiryDate, String quantity, String category, String note, String location, int userId) {
        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2/fooddonation/") // Ensure this matches your server URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<ResponseData> call = apiService.addFood(foodName, expiryDate, quantity, category, note, location, userId);

        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DonateFoodActivity.this, "Food added successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DonateFoodActivity.this, HomeActivity.class);
                    intent.putExtra("user_id", userId); // Pass the user ID back to HomeActivity
                    intent.putExtra("username", username);  // Pass the username back to HomeActivity
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorResponse = response.errorBody().string();
                            Log.e("DonateFoodActivity", "Error Response: " + errorResponse);
                        }
                    } catch (Exception e) {
                        Log.e("DonateFoodActivity", "Exception reading error response: " + e.getMessage());
                    }
                    Toast.makeText(DonateFoodActivity.this, "Failed to add food. Check logs for details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Toast.makeText(DonateFoodActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("DonateFoodActivity", "API call failed: " + t.getMessage());
            }
        });
    }
}
