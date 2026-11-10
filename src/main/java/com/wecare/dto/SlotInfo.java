package com.wecare.dto;

/** state: AVAILABLE, TAKEN or PAST */
public record SlotInfo(String slot, String label, String state) {}
