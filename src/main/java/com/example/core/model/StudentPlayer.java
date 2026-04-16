package com.example.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("STUDENT")
@NoArgsConstructor
@Getter
@Setter
public class StudentPlayer extends User implements Player {

    @Column(name = "semester")
    private Integer semester;

    @Override public boolean validateEmail() { return false; }
    @Override public void acceptInvitation(Long teamId) {}
    @Override public void rejectInvitation(Long teamId) {}
    @Override public void setAvailable(boolean available) { this.available = available; }
    @Override public String getUserType() { return "STUDENT"; }
    @Override public String getProfilePhoto() { return profilePhoto; }

    public Object getProfile() { return null; }
}
