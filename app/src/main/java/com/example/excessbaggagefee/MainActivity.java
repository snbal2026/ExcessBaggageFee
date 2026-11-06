package com.example.excessbaggagefee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Tag used to find our messages in Logcat
    private static final String TAG = "BaggageFee";

    // Rules from the assignment
    private static final double ECONOMY_LIMIT = 20.0;   // kg
    private static final double BUSINESS_LIMIT = 40.0;  // kg
    private static final double SERVICE_TAX = 0.08;     // 8%

    // Views
    private TextInputLayout layoutWeight, layoutRate;
    private TextInputEditText etWeight, etRate;
    private RadioButton rbBusiness;
    private Button btnCalculate, btnReset;
    private View cardResult;
    private TextView tvExcessWeight, tvCharge, tvTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2. Connect the Java variables to the layout (using the ids)
        layoutWeight   = findViewById(R.id.layoutWeight);
        layoutRate     = findViewById(R.id.layoutRate);
        etWeight       = findViewById(R.id.etWeight);
        etRate         = findViewById(R.id.etRate);
        rbBusiness     = findViewById(R.id.rbBusiness);
        btnCalculate   = findViewById(R.id.btnCalculate);
        btnReset       = findViewById(R.id.btnReset);
        cardResult     = findViewById(R.id.cardResult);
        tvExcessWeight = findViewById(R.id.tvExcessWeight);
        tvCharge       = findViewById(R.id.tvCharge);
        tvTotal        = findViewById(R.id.tvTotal);

        // 3. What happens when buttons are clicked
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateFee();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetResultText();
            }
        });
    }

    // ---------- CALCULATION ----------
    private void calculateFee() {
        // Clear old error messages
        layoutWeight.setError(null);
        layoutRate.setError(null);

        String weightText = etWeight.getText().toString().trim();
        String rateText = etRate.getText().toString().trim();

        boolean valid = true;
        double weight = 0;
        double rate = 0;

        // Check the weight
        if (weightText.isEmpty()) {
            layoutWeight.setError("Please enter the baggage weight");
            valid = false;
        } else {
            try {
                weight = Double.parseDouble(weightText);
                if (weight <= 0) {
                    layoutWeight.setError("Weight must be more than 0 kg");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid weight: " + weightText, e);
                layoutWeight.setError("Please enter a valid number");
                valid = false;
            }
        }

        // Check the rate
        if (rateText.isEmpty()) {
            layoutRate.setError("Please enter the rate per kg");
            valid = false;
        } else {
            try {
                rate = Double.parseDouble(rateText);
                if (rate <= 0) {
                    layoutRate.setError("Rate must be more than RM 0");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid rate: " + rateText, e);
                layoutRate.setError("Please enter a valid number");
                valid = false;
            }
        }

        // Stop here if something is wrong
        if (!valid) {
            Toast.makeText(this, "Please fix the errors above", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ticket class: business = 40 kg, otherwise economy = 20 kg
        double limit = rbBusiness.isChecked() ? BUSINESS_LIMIT : ECONOMY_LIMIT;

        // Excess weight (never negative)
        double excessWeight = Math.max(0, weight - limit);

        // Excess charge = excess weight x rate
        double excessCharge = excessWeight * rate;

        // Total = charge + (8% x charge)
        double totalPayable = excessCharge + (SERVICE_TAX * excessCharge);

        Log.d(TAG, "weight=" + weight + " limit=" + limit + " rate=" + rate
                + " excess=" + excessWeight + " charge=" + excessCharge
                + " total=" + totalPayable);

        // Show the results
        tvExcessWeight.setText(String.format(Locale.US,
                "Excess weight: %.2f kg", excessWeight));
        tvCharge.setText(String.format(Locale.US,
                "Excess charge: RM %.2f", excessCharge));
        tvTotal.setText(String.format(Locale.US,
                "Total payable (incl. 8%% tax): RM %.2f", totalPayable));

        cardResult.setVisibility(View.VISIBLE);

        if (excessWeight == 0) {
            Toast.makeText(this, "Your baggage is within the free allowance!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- RESET ----------
    private void resetResultText() {
        tvExcessWeight.setText("Excess weight: 0.00 kg");
        tvCharge.setText("Excess charge: RM 0.00");
        tvTotal.setText("Total payable (incl. 8% tax): RM 0.00");
    }

    // ---------- MENU ----------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            shareApp();
            return true;
        } else if (id == R.id.action_about) {
            // Open the About screen
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ---------- SHARE ----------
    private void shareApp() {
        String message = "Check out the Baggage Fee Calculator app: "
                + getString(R.string.share_url);

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Baggage Fee Calculator");
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(sendIntent, "Share app via"));
    }
}