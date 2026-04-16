package com.example.core.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("STAFF")
@NoArgsConstructor
public class StaffPlayer extends User implements Player {

    @Override public boolean validateEmail() { return false; }
    @Override public void acceptInvitation(Long teamId) {}
    @Override public void rejectInvitation(Long teamId) {}
    @Override public void setAvailable(boolean available) { this.available = available; }
    @Override public String getUserType() { return "STAFF"; }
    @Override public String getProfilePhoto() { return profilePhoto; }

    public Object getProfile() { return null; }
}
