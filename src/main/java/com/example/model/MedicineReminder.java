package com.example.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicine_reminders")
public class MedicineReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String medicineName;

    @Column(nullable = false)
    private String dosage;

    @Column(nullable = false)
    private LocalDateTime reminderTime;

    private String notes;

    @Column(nullable = false)
    private String frequency = "EVERYDAY"; // EVERYDAY or WEEKLY

    private String daysOfWeek; // e.g., MON,TUE,FRI when WEEKLY

    @Column(nullable = false)
    private boolean taken = false;

    private LocalDateTime nextReminderTime;
    
    private LocalDateTime lastTakenTime; // Track when medication was last marked as taken

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public LocalDateTime getReminderTime() { return reminderTime; }
    public void setReminderTime(LocalDateTime reminderTime) { this.reminderTime = reminderTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public boolean isTaken() { return taken; }
    public void setTaken(boolean taken) { this.taken = taken; }

    public LocalDateTime getNextReminderTime() { return nextReminderTime; }
    public void setNextReminderTime(LocalDateTime nextReminderTime) { this.nextReminderTime = nextReminderTime; }
    
    public LocalDateTime getLastTakenTime() { return lastTakenTime; }
    public void setLastTakenTime(LocalDateTime lastTakenTime) { this.lastTakenTime = lastTakenTime; }
}
