package student.inti.fooddonationsystem;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryAdapter extends ArrayAdapter<HistoryItem> {

    private ApiService apiService;
    private String historyType; // Added to track donation or takeaway
    private List<HistoryItem> historyItems; // To manage the list locally
    private Context context;

    public HistoryAdapter(Context context, List<HistoryItem> historyItems, String historyType) {
        super(context, 0, historyItems);
        this.context = context;
        this.historyItems = historyItems; // Manage list locally
        this.historyType = historyType;
        apiService = ApiClient.getClient().create(ApiService.class); // Initialize ApiService
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HistoryItem historyItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.history_item, parent, false);
        }

        TextView foodNameTextView = convertView.findViewById(R.id.foodNameTextView);
        TextView statusTextView = convertView.findViewById(R.id.statusTextView);
        TextView timestampTextView = convertView.findViewById(R.id.timestampTextView);
        TextView quantityTextView = convertView.findViewById(R.id.quantityTextView);
        TextView categoryTextView = convertView.findViewById(R.id.categoryTextView);
        TextView noteTextView = convertView.findViewById(R.id.noteTextView);
        TextView locationTextView = convertView.findViewById(R.id.locationTextView);
        Button editButton = convertView.findViewById(R.id.editButton);
        Button deleteButton = convertView.findViewById(R.id.deleteButton);

        // Set the values from historyItem
        foodNameTextView.setText("Food Name: " + historyItem.getFood_name());
        statusTextView.setText("Status: " + historyItem.getStatus());
        timestampTextView.setText("Timestamp: " + historyItem.getTimestamp());
        quantityTextView.setText("Quantity: " + historyItem.getQuantity());
        categoryTextView.setText("Category: " + historyItem.getCategory());
        noteTextView.setText("Note: " + historyItem.getNotes());
        locationTextView.setText("Location: " + historyItem.getLocation());

        // Enable edit and delete only for pending items
        if ("pending".equals(historyItem.getStatus())) {
            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);

            // Set click listener for edit
            editButton.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), FoodHistoryDetailActivity.class);
                intent.putExtra("food_id", historyItem.getFood_id());
                intent.putExtra("user_id", historyItem.getUser_id());
                intent.putExtra("food_name", historyItem.getFood_name());
                intent.putExtra("donation_status", historyItem.getStatus());
                intent.putExtra("location", historyItem.getLocation());
                intent.putExtra("quantity", String.valueOf(historyItem.getQuantity()));
                intent.putExtra("category", historyItem.getCategory());
                intent.putExtra("note", historyItem.getNotes());
                getContext().startActivity(intent);
            });

            // Set click listener for delete
            deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Food")
                        .setMessage("Are you sure you want to delete this food item?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            deleteFoodItem(historyItem.getFood_id(), historyItem.getUser_id(), position);
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            });

        } else {
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        }

        return convertView;
    }

    // Method to delete food item
    private void deleteFoodItem(int foodId, int userId, int position) {
        // Create Retrofit call to delete food from both tables
        Call<ResponseData> call = apiService.deleteFood(foodId);

        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    // Notify user and remove the item from the list
                    Toast.makeText(getContext(), "Food item deleted successfully", Toast.LENGTH_SHORT).show();
                    historyItems.remove(position); // Remove the item locally
                    notifyDataSetChanged(); // Refresh the adapter

                    // Check if the list is now empty and notify HistoryActivity
                    if (historyItems.isEmpty()) {
                        ((HistoryActivity) context).showNoHistoryMessage(); // Call method to show no history
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to delete food item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
