package com.example.core.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id")
    private Long teamId;

    private String status;

    @Column(name = "upload_date")
    private LocalDate uploadDate;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "receipt_url")
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
