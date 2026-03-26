package com.example.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private Long id;
    private Long teamId;
    private String status;
    private LocalDate uploadDate;
    private LocalDate reviewDate;
    private String approvedBy;
    private String receiptUrl;
    private String comments;

    public void approve(String organizer) {
        this.status = "Aprobado";
        this.approvedBy = organizer;
        this.reviewDate = LocalDate.now();
    }

    public void reject(String comments) {
        this.status = "Rechazado";
        this.comments = comments;
        this.reviewDate = LocalDate.now();
    }

    public void sendToReview() {
        this.status = "En revisión";
    }

    public boolean isApproved() {
        return "Aprobado".equals(this.status);
    }
}