package com.payment.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.stereotype.Component;

@Component
public class RazorpayClientProvider {

    private final PaymentProviderProperties properties;

    public RazorpayClientProvider(PaymentProviderProperties properties) {
        this.properties = properties;
    }

    public RazorpayClient getClient() throws RazorpayException {
        if (properties.keyId() == null || properties.keyId().isBlank()
                || properties.keySecret() == null || properties.keySecret().isBlank()) {
            throw new IllegalStateException("Razorpay credentials are not configured");
        }
        return new RazorpayClient(properties.keyId(), properties.keySecret());
    }
}
