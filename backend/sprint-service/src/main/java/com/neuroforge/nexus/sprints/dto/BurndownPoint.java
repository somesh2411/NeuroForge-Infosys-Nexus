package com.neuroforge.nexus.sprints.dto;

public record BurndownPoint(
    String date,
    double idealRemaining,
    double actualRemaining
) {}
