package com.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private Long id;
    private String status;
    private LocalDate uploadDate;
    private LocalDate reviewDate;
    private String approvedBy;
    private String comments;

    public void approve(String organizer) {}
    public void reject(String comments) {}
    public void sendToReview() {}
    public boolean isApproved() { return false; }
}