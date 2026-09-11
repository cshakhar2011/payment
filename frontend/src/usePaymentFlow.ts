import { useState } from "react";
import { api, type Order } from "./api";
import {
    openRazorpayPayment,
    type RazorpayPaymentResponse,
} from "./paymentService";

export function usePaymentFlow(options: {
    orders: Order[];
    setView: (view: "orders") => void;
    refresh: (orderId: string) => Promise<void>;
    setError: (message: string) => void;
}) {
    const [selectedOrder, setSelectedOrder] = useState("");

    const openPayment = async (orderId: string) => {
        const order = options.orders.find((candidate) => candidate.id === orderId);
        if (!order) {
            options.setError("The selected order is no longer available.");
            return;
        }

        const orderReference = order.orderReference || order.receipt;
        if (!orderReference) {
            options.setError("This order has no local reference for payment confirmation.");
            return;
        }

        try {
            const providerOrderId = order.providerOrderId || order.id;
            const keyId =
                import.meta.env.VITE_RAZORPAY_KEY_ID ||
                (await api.providerConfig()).keyId;
            if (!keyId || !providerOrderId) {
                options.setError("Razorpay Checkout is not configured for this order.");
                return;
            }
            if (!Number.isFinite(order.amount) || order.amount <= 0) {
                options.setError("This Razorpay order has a zero amount and cannot be paid.");
                return;
            }

            setSelectedOrder(order.id);
            options.setView("orders");
            await openRazorpayPayment({
                key: keyId,
                amount: Math.round(order.amount),
                currency: order.currency,
                description: `Payment for ${orderReference}`,
                orderId: providerOrderId,
                onSuccess: async (response: RazorpayPaymentResponse) => {
                    try {
                        await api.makePayment(orderReference, {
                            razorpayOrderId: response.razorpay_order_id,
                            razorpayPaymentId: response.razorpay_payment_id,
                            razorpaySignature: response.razorpay_signature,
                        });
                        await options.refresh(order.id);
                    } catch (error) {
                        options.setError(
                            error instanceof Error
                                ? error.message
                                : "Unable to confirm payment",
                        );
                    }
                },
                onDismiss: () =>
                    options.setError("Payment checkout was closed before completion."),
                onFailure: (message) => options.setError(message),
            });
        } catch (error) {
            options.setError(
                error instanceof Error ? error.message : "Unable to start payment",
            );
        }
    };

    return { selectedOrder, setSelectedOrder, openPayment };
}