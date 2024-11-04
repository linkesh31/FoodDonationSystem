package student.inti.fooddonationsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TakeAwayFoodActivity extends AppCompatActivity {

    private ListView foodListView;
    private Button backButton;
    private ApiService apiService;
    private int userId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_away_food);

        // Get user ID and username passed from HomeActivity
        userId = getIntent().getIntExtra("user_id", -1);
        username = getIntent().getStringExtra("username");

        foodListView = findViewById(R.id.foodListView);
        backButton = findViewById(R.id.backBtn);

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2/fooddonation/") // Your local server URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Load available food items
        loadAvailableFood();

        // Item click listener
        foodListView.setOnItemClickListener((parent, view, position, id) -> {
            FoodItem selectedFoodItem = (FoodItem) parent.getItemAtPosition(position);

            // Open FoodDetailActivity and pass the selected food details
            Intent intent = new Intent(TakeAwayFoodActivity.this, FoodDetailActivity.class);
            intent.putExtra("food_id", selectedFoodItem.getId());
            intent.putExtra("food_name", selectedFoodItem.getFoodName());
            intent.putExtra("expiry_date", selectedFoodItem.getExpiryDate());
            intent.putExtra("quantity", selectedFoodItem.getQuantity());
            intent.putExtra("category", selectedFoodItem.getCategory());
            intent.putExtra("note", selectedFoodItem.getNote());
            intent.putExtra("location", selectedFoodItem.getLocation());
            intent.putExtra("user_id", userId);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Back button functionality
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(TakeAwayFoodActivity.this, HomeActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        });
    }

    // Function to load available food items
    private void loadAvailableFood() {
        Call<List<FoodItem>> call = apiService.getAvailableFood();
        call.enqueue(new Callback<List<FoodItem>>() {
            @Override
            public void onResponse(Call<List<FoodItem>> call, Response<List<FoodItem>> response) {
                if (response.isSuccessful()) {
                    List<FoodItem> foodList = response.body();
                    Log.d("TakeAwayFoodActivity", "Response: " + foodList); // Log the response
                    if (foodList != null && !foodList.isEmpty()) {
                        FoodAdapter adapter = new FoodAdapter(TakeAwayFoodActivity.this, foodList);
                        foodListView.setAdapter(adapter);
                    } else {
                        Toast.makeText(TakeAwayFoodActivity.this, "No available food items", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TakeAwayFoodActivity.this, "Failed to load available food", Toast.LENGTH_SHORT).show();
                    Log.e("TakeAwayFoodActivity", "Error Response: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<FoodItem>> call, Throwable t) {
                Toast.makeText(TakeAwayFoodActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("TakeAwayFoodActivity", "API call failed: " + t.getMessage());
            }
        });
    }
}
