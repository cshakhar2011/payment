package com.payment.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "webhooks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Webhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_webhook_id", nullable = false, unique = true, length = 150)
    private String providerWebhookId;

    @Column(name = "provider_service", length = 150)
    private String service;

    @Column(name = "owner_id", length = 150)
    private String ownerId;

    @Column(name = "owner_type", length = 50)
    private String ownerType;

    @Column(length = 1000)
    private String url;

    @Column(name = "alert_email", length = 255)
    private String alertEmail;

    @Column(name = "secret_exists")
    private Boolean secretExists;

    @Column
    private Boolean active;

    @Lob
    @Column(name = "events_json", columnDefinition = "TEXT")
    private String eventsJson;

    @Column(name = "disabled_at")
    private Long disabledAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void updateTimestamp() {
        updatedAt = OffsetDateTime.now();
    }
}