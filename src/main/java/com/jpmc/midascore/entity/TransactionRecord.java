package com.jpmc.midascore.entity;

import jakarta.persistence.*;

@Entity
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private float amount;

    private float incentive;   // 🔥 NEW FIELD

    @ManyToOne
    private UserRecord sender;

    @ManyToOne
    private UserRecord recipient;

    protected TransactionRecord() {}

    // 🔥 UPDATED CONSTRUCTOR
    public TransactionRecord(float amount, float incentive, UserRecord sender, UserRecord recipient) {
        this.amount = amount;
        this.incentive = incentive;
        this.sender = sender;
        this.recipient = recipient;
    }

    // (Optional but good practice — not required for test)
    public float getAmount() {
        return amount;
    }

    public float getIncentive() {
        return incentive;
    }

    public UserRecord getSender() {
        return sender;
    }

    public UserRecord getRecipient() {
        return recipient;
    }
}