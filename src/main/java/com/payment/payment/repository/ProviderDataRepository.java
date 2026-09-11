package com.payment.payment.repository;

import com.payment.payment.entity.ProviderData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderDataRepository extends JpaRepository<ProviderData, Long> {
    Optional<ProviderData> findByResourceTypeAndProviderId(String resourceType, String providerId);
}