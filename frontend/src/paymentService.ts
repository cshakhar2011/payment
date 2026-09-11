export type RazorpayPaymentResponse = {
    razorpay_order_id: string;
    razorpay_payment_id: string;
    razorpay_signature: string;
};

type RazorpayFailureResponse = {
    error?: { description?: string };
};

type RazorpayCheckout = {
    open: () => void;
    on: (
        event: "payment.failed",
        handler: (response: RazorpayFailureResponse) => void,
    ) => void;
};

type RazorpayConstructor = new (
    options: Record<string, unknown>,
) => RazorpayCheckout;

declare global {
    interface Window {
        Razorpay?: RazorpayConstructor;
    }
}

function loadCheckoutScript() {
    return new Promise<void>((resolve, reject) => {
        const existingScript = document.querySelector(
            'script[src="https://checkout.razorpay.com/v1/checkout.js"]',
        );
        if (existingScript) {
            resolve();
            return;
        }

        const script = document.createElement("script");
        script.src = "https://checkout.razorpay.com/v1/checkout.js";
        script.onload = () => resolve();
        script.onerror = () =>
            reject(new Error("Unable to load Razorpay Checkout"));
        document.body.appendChild(script);
    });
}

export async function openRazorpayPayment(options: {
    key: string;
    amount: number;
    currency: string;
    orderId: string;
    description: string;
    onSuccess: (response: RazorpayPaymentResponse) => void | Promise<void>;
    onDismiss: () => void;
    onFailure: (message: string) => void;
}) {
    if (!window.Razorpay) await loadCheckoutScript();
    if (!window.Razorpay) throw new Error("Razorpay Checkout is unavailable");

    const checkout = new window.Razorpay({
        key: options.key,
        amount: options.amount,
        currency: options.currency,
        name: "Payframe",
        description: options.description,
        order_id: options.orderId,
        handler: options.onSuccess,
        modal: { ondismiss: options.onDismiss },
    });

    checkout.on("payment.failed", (response) => {
        options.onFailure(
            response.error?.description || "Razorpay could not complete the payment.",
        );
    });
    checkout.open();
}