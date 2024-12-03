package com.example.spendwise;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.spendwise.R;
import com.example.spendwise.BudgetModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BudgetFragment extends Fragment {

    private EditText etBudgetAmount;
    private TextView tvStartDate, tvEndDate, tvProgress, tvDailyAllowance;
    private ProgressBar progressBar;
    private Button btnPickStartDate, btnPickEndDate, btnSaveBudget;

    private Calendar startDateCalendar, endDateCalendar;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private double budgetAmount = 0.0;
    private double remainingAmount = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        // Initialize Firebase and UI elements
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Budgets").child(mAuth.getUid());

        etBudgetAmount = view.findViewById(R.id.etBudgetAmount);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        tvProgress = view.findViewById(R.id.tvProgress);
        tvDailyAllowance = view.findViewById(R.id.tvDailyAllowance);
        progressBar = view.findViewById(R.id.progressBar);
        btnPickStartDate = view.findViewById(R.id.btnPickStartDate);
        btnPickEndDate = view.findViewById(R.id.btnPickEndDate);
        btnSaveBudget = view.findViewById(R.id.btnSaveBudget);

        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();

        // Set date pickers
        btnPickStartDate.setOnClickListener(v -> pickDate(startDateCalendar, tvStartDate));
        btnPickEndDate.setOnClickListener(v -> pickDate(endDateCalendar, tvEndDate));

        // Save or Edit Budget
        btnSaveBudget.setOnClickListener(v -> saveOrEditBudget());

        // Load existing budget data
        loadBudgetFromFirebase();

        return view;
    }

    private void pickDate(Calendar calendar, TextView textView) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    textView.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void saveOrEditBudget() {
        String budgetText = etBudgetAmount.getText().toString();
        if (budgetText.isEmpty()) {
            etBudgetAmount.setError("Budget amount is required");
            etBudgetAmount.requestFocus();
            return;
        }

        budgetAmount = Double.parseDouble(budgetText);

        long startDateMillis = startDateCalendar.getTimeInMillis();
        long endDateMillis = endDateCalendar.getTimeInMillis();

        if (startDateMillis >= endDateMillis) {
            Toast.makeText(requireContext(), "End date must be after the start date", Toast.LENGTH_SHORT).show();
            return;
        }

        remainingAmount = budgetAmount;

        // Save to Firebase
        saveBudgetToFirebase(budgetAmount, startDateMillis, endDateMillis, remainingAmount);

        // Update progress and daily allowance
        updateProgress();
    }

    private void saveBudgetToFirebase(double budgetAmount, long startDate, long endDate, double remainingAmount) {
        BudgetModel budget = new BudgetModel(budgetAmount, startDate, endDate, remainingAmount);

        databaseReference.setValue(budget)
                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Budget saved successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save budget: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadBudgetFromFirebase() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    BudgetModel budget = snapshot.getValue(BudgetModel.class);
                    if (budget != null) {
                        budgetAmount = budget.getBudgetAmount();
                        remainingAmount = budget.getRemainingAmount();
                        startDateCalendar.setTimeInMillis(budget.getStartDate());
                        endDateCalendar.setTimeInMillis(budget.getEndDate());

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                        tvStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
                        tvEndDate.setText(dateFormat.format(endDateCalendar.getTime()));

                        etBudgetAmount.setText(String.valueOf(budgetAmount));

                        // Auto-renew budget if it has expired
                        autoRenewBudget();

                        // Update progress and daily allowance
                        updateProgress();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load budget: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void autoRenewBudget() {
        if (System.currentTimeMillis() > endDateCalendar.getTimeInMillis()) {
            long duration = endDateCalendar.getTimeInMillis() - startDateCalendar.getTimeInMillis();
            startDateCalendar.setTimeInMillis(endDateCalendar.getTimeInMillis());
            endDateCalendar.setTimeInMillis(startDateCalendar.getTimeInMillis() + duration);

            // Reset the remaining amount and save the updated budget
            remainingAmount = budgetAmount;
            saveBudgetToFirebase(budgetAmount, startDateCalendar.getTimeInMillis(), endDateCalendar.getTimeInMillis(), remainingAmount);
        }
    }

    private void updateProgress() {
        int remainingDays = (int) ((endDateCalendar.getTimeInMillis() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24));
        double dailyAllowance = remainingAmount / remainingDays;

        tvProgress.setText(String.format("Remaining: $%.2f (%.0f%%)", remainingAmount, (remainingAmount / budgetAmount) * 100));
        progressBar.setProgress((int) ((remainingAmount / budgetAmount) * 100));
        tvDailyAllowance.setText(String.format("Daily Allowance: $%.2f", dailyAllowance));
    }
}
