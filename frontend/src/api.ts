const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8092'

export type Order = { id: string; amount: number; currency: string; status: string; providerOrderId?: string; orderReference?: string; receipt?: string }
export type Payment = { id: string; amount: number; currency: string; status: string; method?: string; orderId?: string; email?: string }
export type Refund = { id: string; amount: number; currency: string; status: string; paymentId?: string; speedRequested?: string }
export type Settlement = { id: string; amount: number; status: string; fees: number; tax: number; utr?: string }
export type Downtime = { id: string; method: string; status: string; severity: string; instrument?: Record<string, unknown> }
export type Webhook = { id: string; url: string; active: boolean; events: string[]; alertEmail?: string }
export type CreateOrderRequest = { customerId: string; amount: number; currency: string; description?: string }
type CreatedOrderResponse = Omit<Order, 'id'> & { id: number; providerOrderId?: string }
export type CreatePaymentLinkRequest = { amount: number; currency: string; description: string; customer?: Record<string, unknown>;[key: string]: unknown }
export type CreateQrCodeRequest = { type: string; name: string; usage: string; fixedAmount: boolean; paymentAmount?: number;[key: string]: unknown }
export type CreateRefundRequest = { amount: number; speed?: string; receipt?: string; notes?: Record<string, unknown> }
export type MakePaymentRequest = { razorpayOrderId: string; razorpayPaymentId: string; razorpaySignature: string }
export type VerifyPaymentRequest = { razorpayOrderId: string; razorpayPaymentId: string; razorpaySignature: string }
export type PaymentLink = { shortUrl?: string; short_url?: string; id?: string; status?: string }

async function request<T>(path: string, options?: RequestInit): Promise<T> {
    const headers = new Headers(options?.headers)
    headers.set('Accept', 'application/json')
    if (options?.body) headers.set('Content-Type', 'application/json')
    const response = await fetch(`${API_BASE}${path}`, { ...options, headers })
    if (!response.ok) throw new Error(`${response.status} ${await response.text()}`)
    return response.json() as Promise<T>
}

export const api = {
    createOrder: async (body: CreateOrderRequest) => {
        const order = await request<CreatedOrderResponse>('/orders', { method: 'POST', body: JSON.stringify(body) })
        return { ...order, id: order.providerOrderId || String(order.id), amount: order.amount * 100 }
    },
    orders: () => request<Order[]>('/orders'),
    providerConfig: () => request<{ provider: string; keyId: string }>('/payments/config'),
    payments: (orderId: string) => request<Payment[]>(`/payments/orders/${orderId}/payments`),
    createPaymentLink: (body: CreatePaymentLinkRequest) => request<unknown>('/payments/payment-links', { method: 'POST', body: JSON.stringify(body) }),
    createPaymentLinkForOrder: (orderReference: string) => request<PaymentLink>(`/payments/orders/${encodeURIComponent(orderReference)}/payment-link`, { method: 'POST' }),
    createQrCode: (body: CreateQrCodeRequest) => request<unknown>('/payments/qr-codes', { method: 'POST', body: JSON.stringify(body) }),
    getQrCode: (qrCodeId: string) => request<unknown>(`/payments/qr-codes/${qrCodeId}`),
    getPayment: (paymentId: string) => request<Payment>(`/payments/${paymentId}`),
    capturePayment: (paymentId: string, body: { amount: number; currency: string }) => request<Payment>(`/payments/${paymentId}/capture`, { method: 'POST', body: JSON.stringify(body) }),
    createRefund: (paymentId: string, body: CreateRefundRequest, idempotencyKey: string) => request<Refund>(`/refunds/${paymentId}/refund`, { method: 'POST', headers: { 'X-Refund-Idempotency': idempotencyKey }, body: JSON.stringify(body) }),
    getRefund: (refundId: string) => request<Refund>(`/refunds/${refundId}`),
    makePayment: (orderReference: string, body: MakePaymentRequest) => request<Payment>(`/payments/orders/${encodeURIComponent(orderReference)}/pay`, { method: 'POST', body: JSON.stringify(body) }),
    verifyPayment: (body: VerifyPaymentRequest) => request<Order>('/payments/razorpay/verify', { method: 'POST', body: JSON.stringify(body) }),
    settlements: () => request<{ items: Settlement[] }>('/payments/settlements'),
    downtimes: () => request<{ items: Downtime[] }>('/payments/downtimes'),
    getDowntime: (downtimeId: string) => request<Downtime>(`/payments/downtimes/${downtimeId}`),
    getSettlement: (settlementId: string) => request<Settlement>(`/payments/settlements/${settlementId}`),
    reconciliation: (year: number, month: number, day: number) => request<{ items: unknown[] }>(`/payments/settlements/recon/combined?year=${year}&month=${month}&day=${day}`),
    webhooks: (accountId: string) => request<Webhook[]>(`/webhooks/${accountId}`),
    getWebhook: (accountId: string, webhookId: string) => request<Webhook>(`/webhooks/${accountId}/${webhookId}`),
    updateWebhook: (accountId: string, webhookId: string, body: { url?: string; alertEmail?: string; events: string[] }) => request<Webhook>(`/webhooks/${accountId}/${webhookId}`, { method: 'PATCH', body: JSON.stringify(body) }),
    deleteWebhook: (accountId: string, webhookId: string) => request<Webhook[]>(`/webhooks/${accountId}/${webhookId}`, { method: 'DELETE' }),
}
