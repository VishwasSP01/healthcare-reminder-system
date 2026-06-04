package com.tekravio.healthcare.diet.dto;

public record DietComplianceResponse(long totalMeals, long eatenMeals, double compliancePercentage) {
}

