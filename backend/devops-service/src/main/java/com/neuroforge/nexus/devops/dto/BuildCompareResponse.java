package com.neuroforge.nexus.devops.dto;

public record BuildCompareResponse(
    BuildResponse buildA,
    BuildResponse buildB,
    boolean statusMatch,
    long durationDifferenceMs,
    boolean commitMatch,
    int testCountDifference,
    String deploymentComparisonNote
) {}
