package com.example.spendwise;

public class BudgetModel {
    private double budgetAmount;  // Total budget amount in dollars
    private long startDate;       // Start date of the budget (Unix timestamp in milliseconds)
    private long endDate;         // End date of the budget (Unix timestamp in milliseconds)
    private double remainingAmount;  // Remaining budget amount in dollars

    public BudgetModel() {
    }

    public BudgetModel(double budgetAmount, long startDate, long endDate, double remainingAmount) {
        this.budgetAmount = budgetAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.remainingAmount = remainingAmount;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
}
