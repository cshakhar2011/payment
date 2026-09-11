package com.payment.payment.repository;

import com.payment.payment.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByProviderRefundId(String providerRefundId);
}