package student.inti.fooddonationsystem;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText email, newPassword, confirmPassword;
    private Button resetPasswordBtn, backBtn; // Declare the Back button
    private CheckBox showPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        email = findViewById(R.id.email);
        newPassword = findViewById(R.id.newPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        resetPasswordBtn = findViewById(R.id.resetPasswordBtn);
        showPassword = findViewById(R.id.showPassword);
        backBtn = findViewById(R.id.backBtn); // Initialize the Back button

        // Toggle Show/Hide Password
        showPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                newPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                confirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                newPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                confirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        // Reset Password Button click listener
        resetPasswordBtn.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            String newPasswordText = newPassword.getText().toString().trim();
            String confirmPasswordText = confirmPassword.getText().toString().trim();

            // Email format validation
            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                Toast.makeText(ForgotPasswordActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate fields
            if (emailText.isEmpty() || newPasswordText.isEmpty() || confirmPasswordText.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPasswordText.equals(confirmPasswordText)) {
                Toast.makeText(ForgotPasswordActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Initialize Retrofit for API call
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.2.2/fooddonation/") // Update with your server URL
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);

            // Make API call to check if the email exists and reset the password
            Call<ResponseData> call = apiService.resetPassword(emailText, newPasswordText);
            call.enqueue(new Callback<ResponseData>() {
                @Override
                public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                    if (response.isSuccessful()) {
                        ResponseData responseData = response.body();
                        if (responseData != null && responseData.getStatus().equals("success")) {
                            Toast.makeText(ForgotPasswordActivity.this, "Password reset successful!", Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity after successful reset
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, responseData != null ? responseData.getMessage() : "Reset failed!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseData> call, Throwable t) {
                    Toast.makeText(ForgotPasswordActivity.this, "Reset failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Back Button click listener to navigate to MainActivity
        backBtn.setOnClickListener(v -> {
            finish(); // Close the current activity to go back to MainActivity
        });
    }
}
