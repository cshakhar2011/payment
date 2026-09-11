import { useEffect, useMemo, useState } from "react";
import {
  api,
  type Downtime,
  type Order,
  type Payment,
  type Settlement,
  type Webhook,
} from "./api";
import { usePaymentFlow } from "./usePaymentFlow";
import "./App.css";

type View = "overview" | "orders" | "payments" | "risk" | "webhooks";

const nav: { id: View; label: string; icon: string }[] = [
  { id: "overview", label: "Overview", icon: "◈" },
  { id: "orders", label: "Orders", icon: "▤" },
  { id: "payments", label: "Payments", icon: "↗" },
  { id: "risk", label: "Reliability", icon: "⌁" },
  { id: "webhooks", label: "Webhooks", icon: "◎" },
];

function formatAmount(amount = 0, currency = "INR") {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount / 100);
}
function Status({ value }: { value?: string }) {
  return (
    <span className={`status ${value?.toLowerCase()}`}>
      {value || "unknown"}
    </span>
  );
}
function pulseHeights(orders: Order[], payments: Payment[]) {
  const activity = [...orders.slice(0, 12), ...payments.slice(0, 12)].slice(
    0,
    12,
  );
  const values = activity.map((item) => {
    const status = item.status.toLowerCase();
    if (status === "captured" || status === "paid" || status === "authorized")
      return 92;
    if (status === "failed" || status === "refunded") return 38;
    if (status === "payment_pending" || status === "created") return 64;
    return 52;
  });
  return [
    ...values,
    ...Array.from({ length: Math.max(0, 12 - values.length) }, () => 18),
  ];
}

function App() {
  const [view, setView] = useState<View>("overview");
  const [orders, setOrders] = useState<Order[]>([]);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [settlements, setSettlements] = useState<Settlement[]>([]);
  const [downtimes, setDowntimes] = useState<Downtime[]>([]);
  const [webhooks, setWebhooks] = useState<Webhook[]>([]);
  const [accountId, setAccountId] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [lastSynced, setLastSynced] = useState<Date | null>(null);

  const load = async (orderId = "") => {
    setLoading(true);
    setError("");
    try {
      const results = await Promise.allSettled([
        api.orders(),
        api.settlements(),
        api.downtimes(),
        orderId ? api.payments(orderId) : Promise.resolve(null),
        accountId ? api.webhooks(accountId) : Promise.resolve(null),
      ]);
      const [
        ordersResult,
        settlementsResult,
        downtimesResult,
        paymentsResult,
        webhooksResult,
      ] = results;
      const failures = results.filter(
        (result): result is PromiseRejectedResult =>
          result.status === "rejected",
      );
      if (ordersResult.status === "fulfilled") setOrders(ordersResult.value);
      if (settlementsResult.status === "fulfilled")
        setSettlements(settlementsResult.value.items);
      if (downtimesResult.status === "fulfilled")
        setDowntimes(downtimesResult.value.items);
      if (paymentsResult.status === "fulfilled" && paymentsResult.value)
        setPayments(paymentsResult.value);
      if (webhooksResult.status === "fulfilled" && webhooksResult.value)
        setWebhooks(webhooksResult.value);
      if (failures.length)
        setError(
          failures
            .map((failure) =>
              failure.reason instanceof Error
                ? failure.reason.message
                : "A provider request failed",
            )
            .join(" | "),
        );
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Unable to reach the payment service",
      );
    } finally {
      setLoading(false);
      setLastSynced(new Date());
    }
  };
  useEffect(() => {
    void load();
  }, []);

  const { selectedOrder, openPayment } = usePaymentFlow({
    orders,
    setView: (nextView) => setView(nextView),
    refresh: load,
    setError,
  });

  const captured = payments.filter((payment) => payment.status === "captured");
  const totalVolume = captured.reduce(
    (sum, payment) => sum + payment.amount,
    0,
  );
  const health =
    downtimes.length === 0
      ? "All systems operational"
      : `${downtimes.length} provider alerts`;
  const recentOrders = useMemo(() => orders.slice(0, 6), [orders]);

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">P</span>
          <span>payframe</span>
        </div>
        <div className="workspace-label">OPERATIONS CONSOLE</div>
        <nav>
          {nav.map((item) => (
            <button
              key={item.id}
              className={view === item.id ? "nav-item active" : "nav-item"}
              onClick={() => setView(item.id)}
            >
              <span>{item.icon}</span>
              {item.label}
            </button>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="health-dot" />
          <div>
            <strong>Provider connected</strong>
            <small>Razorpay gateway</small>
          </div>
        </div>
      </aside>
      <main className="main">
        <header className="topbar">
          <div>
            <span className="eyebrow">
              PAYMENT OPERATIONS / {view.toUpperCase()}
            </span>
            <h1>{nav.find((item) => item.id === view)?.label}</h1>
          </div>
          <div className="top-actions">
            <span className="environment">
              <i /> LIVE MODE
            </span>
            <span className="sync-status">
              {lastSynced
                ? `Synced ${lastSynced.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}`
                : "Awaiting sync"}
            </span>
            <button
              className="refresh"
              onClick={() => void load()}
              disabled={loading}
              aria-label="Refresh data"
            >
              ↻ <span>{loading ? "Syncing" : "Refresh"}</span>
            </button>
            <div className="avatar">OP</div>
          </div>
        </header>
        {error && (
          <div className="error-banner">
            <strong>Connection issue</strong>
            <span>{error}</span>
            <button onClick={() => setError("")}>×</button>
          </div>
        )}
        {view === "overview" && (
          <Overview
            orders={recentOrders}
            payments={payments}
            settlements={settlements}
            health={health}
            totalVolume={totalVolume}
            onSelect={openPayment}
          />
        )}
        {view === "orders" && (
          <Orders
            orders={orders}
            selectedOrder={selectedOrder}
            onSelect={openPayment}
            payments={payments}
            onCreate={(order) => setOrders((current) => [order, ...current])}
          />
        )}
        {view === "payments" && <Payments payments={payments} />}
        {view === "risk" && (
          <Reliability downtimes={downtimes} settlements={settlements} />
        )}
        {view === "webhooks" && (
          <Webhooks
            accountId={accountId}
            setAccountId={setAccountId}
            webhooks={webhooks}
            onLoad={() =>
              void api
                .webhooks(accountId)
                .then(setWebhooks)
                .catch((err) => setError(err.message))
            }
          />
        )}
      </main>
    </div>
  );
}

function Overview({
  orders,
  payments,
  settlements,
  health,
  totalVolume,
  onSelect,
}: {
  orders: Order[];
  payments: Payment[];
  settlements: Settlement[];
  health: string;
  totalVolume: number;
  onSelect: (id: string) => void;
}) {
  const successfulTransactions = payments.filter((payment) =>
    ["captured", "authorized", "paid"].includes(payment.status.toLowerCase()),
  ).length;
  const failedTransactions = payments.filter((payment) =>
    ["failed", "refunded"].includes(payment.status.toLowerCase()),
  ).length;
  return (
    <>
      <section className="hero-strip">
        <div>
          <span className="eyebrow warm">TODAY, AUG 27 2026</span>
          <h2>Good morning, operator.</h2>
          <p>
            A clear view of your money movement, provider health, and recent
            activity.
          </p>
        </div>
        <div className="hero-orbit">
          <span>₹</span>
          <b>99.98%</b>
          <small>success rate</small>
        </div>
      </section>
      <section className="metric-grid">
        <Metric
          label="Payment volume"
          value={formatAmount(totalVolume)}
          detail={`${payments.length} loaded payments`}
          tone="blue"
        />
        <Metric
          label="Orders today"
          value={String(orders.length)}
          detail="Across all channels"
          tone="mint"
        />
        <Metric
          label="Settlements"
          value={formatAmount(
            settlements.reduce((sum, item) => sum + item.amount, 0),
          )}
          detail={`${settlements.length} recent batches`}
          tone="gold"
        />
        <Metric
          label="Gateway health"
          value="Healthy"
          detail={health}
          tone="coral"
        />
      </section>
      <div className="content-grid">
        <section className="panel activity">
          <PanelHeading title="Recent orders" action="View all" />
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Order</th>
                  <th>Reference</th>
                  <th>Amount</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {orders.length ? (
                  orders.map((order) => (
                    <tr key={order.id} onClick={() => onSelect(order.id)}>
                      <td className="mono">{order.id}</td>
                      <td>{order.orderReference || "—"}</td>
                      <td>{formatAmount(order.amount, order.currency)}</td>
                      <td>
                        <Status value={order.status} />
                      </td>
                    </tr>
                  ))
                ) : (
                  <Empty label="No orders loaded yet" />
                )}
              </tbody>
            </table>
          </div>
        </section>
        <section className="panel pulse">
          <PanelHeading title="System pulse" action="Live activity" />
          <div className="pulse-chart">
            <div className="bars">
              {pulseHeights(orders, payments).map((height, index) => (
                <i key={index} style={{ height: `${height}%` }} />
              ))}
            </div>
            <div className="chart-labels">
              <span>Latest</span>
              <span>-6 events</span>
              <span>-3 events</span>
              <span>Recent</span>
              <span>Now</span>
            </div>
          </div>
          <div className="pulse-stat">
            <span>
              <i className="dot green" /> Successful transactions
            </span>
            <strong>{successfulTransactions}</strong>
          </div>
          <div className="pulse-stat">
            <span>
              <i className="dot orange" /> Failed transactions
            </span>
            <strong>{failedTransactions}</strong>
          </div>
        </section>
      </div>
    </>
  );
}
function Metric({
  label,
  value,
  detail,
  tone,
}: {
  label: string;
  value: string;
  detail: string;
  tone: string;
}) {
  return (
    <div className={`metric ${tone}`}>
      <div className="metric-icon">
        {tone === "blue"
          ? "↗"
          : tone === "mint"
            ? "▤"
            : tone === "gold"
              ? "◆"
              : "✓"}
      </div>
      <span>{label}</span>
      <strong>{value}</strong>
      <small>{detail}</small>
    </div>
  );
}
function PanelHeading({ title, action }: { title: string; action: string }) {
  return (
    <div className="panel-heading">
      <h3>{title}</h3>
      <button>
        {action} <span>→</span>
      </button>
    </div>
  );
}
function Empty({ label }: { label: string }) {
  return (
    <tr>
      <td colSpan={4} className="empty">
        {label}
      </td>
    </tr>
  );
}
function Orders({
  orders,
  selectedOrder,
  onSelect,
  payments,
  onCreate,
}: {
  orders: Order[];
  selectedOrder: string;
  onSelect: (id: string) => void;
  payments: Payment[];
  onCreate: (order: Order) => void;
}) {
  const [showCreate, setShowCreate] = useState(false);
  const [customerId, setCustomerId] = useState("");
  const [amount, setAmount] = useState("100");
  const [description, setDescription] = useState("");
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState("");

  const createOrder = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setCreating(true);
    setCreateError("");
    try {
      const order = await api.createOrder({
        customerId,
        amount: Number(amount),
        currency: "INR",
        description: description || undefined,
      });
      onCreate(order);
      setShowCreate(false);
      setCustomerId("");
      setAmount("100");
      setDescription("");
    } catch (error) {
      setCreateError(
        error instanceof Error ? error.message : "Unable to create order",
      );
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="page-stack">
      <div className="section-intro">
        <div>
          <span className="eyebrow">RAZORPAY ORDERS</span>
          <h2>Order ledger</h2>
          <p>Inspect provider order state and linked payment attempts.</p>
        </div>
        <button
          className="primary"
          onClick={() => setShowCreate((current) => !current)}
        >
          ＋ Create order
        </button>
      </div>
      {showCreate && (
        <form className="panel webhook-tool" onSubmit={createOrder}>
          <label>
            Customer ID
            <input
              required
              value={customerId}
              onChange={(event) => setCustomerId(event.target.value)}
              placeholder="UUID"
            />
          </label>
          <label>
            Amount (INR)
            <input
              required
              min="1"
              step="0.01"
              type="number"
              value={amount}
              onChange={(event) => setAmount(event.target.value)}
            />
          </label>
          <label>
            Description
            <input
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              placeholder="Order description"
            />
          </label>
          <button className="primary" disabled={creating}>
            {creating ? "Creating..." : "Submit order"}
          </button>
          {createError && <span className="error-banner">{createError}</span>}
        </form>
      )}
      <section className="panel">
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Provider ID</th>
                <th>Reference</th>
                <th>Amount</th>
                <th>Currency</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {orders.length ? (
                orders.map((order) => (
                  <tr
                    className={selectedOrder === order.id ? "selected" : ""}
                    key={order.id}
                    onClick={() => onSelect(order.id)}
                  >
                    <td className="mono">{order.id}</td>
                    <td>{order.orderReference || order.receipt || "—"}</td>
                    <td>{formatAmount(order.amount, order.currency)}</td>
                    <td>{order.currency}</td>
                    <td>
                      <Status value={order.status} />
                    </td>
                  </tr>
                ))
              ) : (
                <Empty label="No orders found" />
              )}
            </tbody>
          </table>
        </div>
      </section>
      {selectedOrder && (
        <section className="panel detail-panel">
          <PanelHeading
            title={`Payments for ${selectedOrder}`}
            action={`${payments.length} attempts`}
          />
          <Payments payments={payments} compact />
        </section>
      )}
    </div>
  );
}
function Payments({
  payments,
  compact = false,
}: {
  payments: Payment[];
  compact?: boolean;
}) {
  return (
    <div className={compact ? "payments-list compact" : "page-stack"}>
      {!compact && (
        <div className="section-intro">
          <div>
            <span className="eyebrow">TRANSACTION STREAM</span>
            <h2>Payments</h2>
            <p>Every authorization, capture, and failure in one place.</p>
          </div>
          <button className="secondary">Export CSV ↓</button>
        </div>
      )}
      <section className="panel">
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Payment ID</th>
                <th>Method</th>
                <th>Amount</th>
                <th>Customer</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {payments.length ? (
                payments.map((payment) => (
                  <tr key={payment.id}>
                    <td className="mono">{payment.id}</td>
                    <td>{payment.method || "—"}</td>
                    <td>{formatAmount(payment.amount, payment.currency)}</td>
                    <td>{payment.email || "—"}</td>
                    <td>
                      <Status value={payment.status} />
                    </td>
                  </tr>
                ))
              ) : (
                <Empty label="Select an order to load payments" />
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
function Reliability({
  downtimes,
  settlements,
}: {
  downtimes: Downtime[];
  settlements: Settlement[];
}) {
  return (
    <div className="page-stack">
      <div className="section-intro">
        <div>
          <span className="eyebrow">PROVIDER SIGNALS</span>
          <h2>Reliability</h2>
          <p>Watch payment availability and settlement movement.</p>
        </div>
      </div>
      <div className="content-grid">
        <section className="panel">
          <PanelHeading
            title="Payment downtimes"
            action={`${downtimes.length} active`}
          />
          {downtimes.length ? (
            downtimes.map((item) => (
              <div className="signal" key={item.id}>
                <span className="signal-icon">!</span>
                <div>
                  <strong>
                    {item.method} · {item.severity}
                  </strong>
                  <small>
                    {Object.entries(item.instrument || {})
                      .map(([key, value]) => `${key}: ${value}`)
                      .join(" · ") || "Provider-wide signal"}
                  </small>
                </div>
                <Status value={item.status} />
              </div>
            ))
          ) : (
            <div className="empty">No active payment downtimes</div>
          )}
        </section>
        <section className="panel">
          <PanelHeading title="Settlement batches" action="View report" />
          {settlements.length ? (
            settlements.map((item) => (
              <div className="settlement" key={item.id}>
                <div>
                  <strong className="mono">{item.id}</strong>
                  <small>{item.utr || "UTR pending"}</small>
                </div>
                <span>{formatAmount(item.amount)}</span>
                <Status value={item.status} />
              </div>
            ))
          ) : (
            <div className="empty">No settlements loaded</div>
          )}
        </section>
      </div>
    </div>
  );
}
function Webhooks({
  accountId,
  setAccountId,
  webhooks,
  onLoad,
}: {
  accountId: string;
  setAccountId: (value: string) => void;
  webhooks: Webhook[];
  onLoad: () => void;
}) {
  return (
    <div className="page-stack">
      <div className="section-intro">
        <div>
          <span className="eyebrow">EVENT DELIVERY</span>
          <h2>Webhooks</h2>
          <p>Monitor the event destinations connected to your account.</p>
        </div>
      </div>
      <section className="panel webhook-tool">
        <label>
          Razorpay account ID
          <input
            value={accountId}
            onChange={(event) => setAccountId(event.target.value)}
            placeholder="acc_..."
          />
        </label>
        <button className="primary" onClick={onLoad} disabled={!accountId}>
          Load webhooks
        </button>
      </section>
      <section className="panel">
        {webhooks.length ? (
          webhooks.map((hook) => (
            <div className="webhook-row" key={hook.id}>
              <div className="webhook-icon">◎</div>
              <div>
                <strong>{hook.url}</strong>
                <small>
                  {hook.id} · {hook.alertEmail || "No alert email"}
                </small>
              </div>
              <div className="event-tags">
                {hook.events.slice(0, 3).map((event) => (
                  <span key={event}>{event}</span>
                ))}
                {hook.events.length > 3 && (
                  <span>+{hook.events.length - 3}</span>
                )}
              </div>
              <Status value={hook.active ? "active" : "disabled"} />
            </div>
          ))
        ) : (
          <div className="empty">Enter an account ID to load webhooks</div>
        )}
      </section>
    </div>
  );
}

export default App;
