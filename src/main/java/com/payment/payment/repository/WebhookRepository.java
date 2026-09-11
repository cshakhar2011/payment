package com.payment.payment.repository;

import com.payment.payment.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookRepository extends JpaRepository<Webhook, Long> {
    Optional<Webhook> findByProviderWebhookId(String providerWebhookId);
}