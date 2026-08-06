const API_BASE = "http://localhost:8080";
const CURRENCY = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});
const CURRENCY_FORMATTER_CACHE = new Map();
const LOCAL_SCHEDULED_DRAFTS_KEY = "pps_scheduled_drafts";

let paymentStatusChart;
let paymentTypeChart;

function formatAmount(value, currencyCode = "USD") {
  const amount = Number(value || 0);
  const normalizedCode = String(currencyCode || "USD").trim().toUpperCase();

  if (!/^[A-Z]{3}$/.test(normalizedCode)) {
    return CURRENCY.format(amount);
  }

  if (!CURRENCY_FORMATTER_CACHE.has(normalizedCode)) {
    try {
      CURRENCY_FORMATTER_CACHE.set(
        normalizedCode,
        new Intl.NumberFormat("en-US", {
          style: "currency",
          currency: normalizedCode,
        })
      );
    } catch {
      CURRENCY_FORMATTER_CACHE.set(normalizedCode, CURRENCY);
    }
  }

  return CURRENCY_FORMATTER_CACHE.get(normalizedCode).format(amount);
}

function formatDate(value) {
  if (!value) {
    return "-";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "-";
  }

  return date.toLocaleString([], {
    year: "numeric",
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function derivePaymentStatus(rawStatus, scheduledAt) {
  const normalized = String(rawStatus || "pending").toLowerCase();
  const protectedStatuses = ["failed", "refunded", "cancelled"];

  if (protectedStatuses.includes(normalized)) {
    return normalized;
  }

  const scheduleDate = scheduledAt ? new Date(scheduledAt) : null;
  const hasFutureSchedule = scheduleDate && !Number.isNaN(scheduleDate.getTime()) && scheduleDate.getTime() > Date.now();

  if (hasFutureSchedule) {
    return "scheduled";
  }

  if (normalized === "success" || normalized === "done" || normalized === "processed") {
    return "completed";
  }

  return normalized;
}

function toDateKey(value) {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

function statusPill(status) {
  const lower = String(status || "pending").toLowerCase();
  const styleByStatus = {
    completed: "success",
    pending: "warning",
    failed: "danger",
    refunded: "info",
    scheduled: "info",
    cancelled: "danger",
  };
  const css = styleByStatus[lower] || "warning";
  return `<span class="pill ${css}">${lower}</span>`;
}

function setAlert(container, message, type = "success") {
  if (!container) {
    return;
  }
  container.innerHTML = `
    <div class="alert alert-${type} alert-dismissible fade show" role="alert">
      ${message}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  `;
}

function toBackendPaymentType(label) {
  const value = String(label || "")
    .trim()
    .toUpperCase()
    .replace(/\s+/g, "_");

  const map = {
    INDIVIDUAL_PAYMENT: "INDIVIDUAL_PAYMENT",
    ENTITY_PAYMENT: "ENTITY_PAYMENT",
    INTERNATIONAL_PAYMENT: "INTERNATIONAL_PAYMENT",
  };

  return map[value] || value;
}

function generatePaymentReference() {
  const now = new Date();
  const datePart = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, "0")}${String(now.getDate()).padStart(2, "0")}`;
  const randomPart = Math.floor(1000 + Math.random() * 9000);
  return `PAY-${datePart}-${randomPart}`;
}

function buildPaymentPayload(form, includeSchedule = false) {
  const paymentTypeDisplay = form.paymentType.value;
  const paymentType = toBackendPaymentType(paymentTypeDisplay);
  const recipientName = form.recipientName.value.trim();
  const account = form.account.value.trim();
  const amount = Number(form.amount.value);
  const currency = String(form.currency.value || "").trim().toUpperCase();
  const paymentReference = form.paymentReference?.value?.trim() || generatePaymentReference();

  const payload = {
    // Primary keys
    paymentReference,
    paymentType,
    recipientName,
    recipientAccount: account,
    account,
    amount,
    currency,

    // Compatibility aliases for common backend DTO names
    reference: paymentReference,
    type: paymentType,
    recipient: recipientName,
    accountNumber: account,
  };

  if (includeSchedule && form.scheduleAt?.value) {
    const iso = new Date(form.scheduleAt.value).toISOString();
    payload.scheduledAt = iso;
    payload.scheduledDateTime = iso;
  }

  return payload;
}

function getLocalScheduledDrafts() {
  try {
    const raw = localStorage.getItem(LOCAL_SCHEDULED_DRAFTS_KEY);
    if (!raw) {
      return [];
    }

    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function saveLocalScheduledDrafts(drafts) {
  localStorage.setItem(LOCAL_SCHEDULED_DRAFTS_KEY, JSON.stringify(drafts.slice(0, 100)));
}

function saveLocalScheduledDraft(payload, result = {}) {
  const paymentReference = String(result?.reference || result?.paymentReference || payload.paymentReference || "");
  const paymentId = String(result?.id || result?.paymentId || paymentReference || "");
  const scheduledAt = payload.scheduledAt || payload.scheduledDateTime || null;

  if (!paymentReference && !paymentId) {
    return;
  }

  const draft = {
    paymentId: paymentId || paymentReference,
    paymentReference: paymentReference || paymentId,
    paymentType: String(payload.paymentType || "-"),
    paymentTypeKey: normalizePaymentTypeKey(payload.paymentType),
    amount: Number(payload.amount || 0),
    currency: String(payload.currency || "-"),
    rawStatus: "scheduled",
    status: "scheduled",
    date: new Date().toISOString(),
    scheduledAt,
    recipientName: String(payload.recipientName || "Unknown"),
    reason: "-",
  };

  const drafts = getLocalScheduledDrafts().filter((item) => {
    return item.paymentReference !== draft.paymentReference && item.paymentId !== draft.paymentId;
  });

  drafts.unshift(draft);
  saveLocalScheduledDrafts(drafts);
}

function mergeLocalScheduledDrafts(payments) {
  const merged = [...payments];
  const drafts = getLocalScheduledDrafts();

  drafts.forEach((draft) => {
    const index = merged.findIndex((payment) => {
      return payment.paymentReference === draft.paymentReference || payment.paymentId === draft.paymentId;
    });

    if (index >= 0) {
      merged[index] = {
        ...merged[index],
        ...draft,
        paymentId: merged[index].paymentId || draft.paymentId,
        paymentReference: merged[index].paymentReference || draft.paymentReference,
        scheduledAt: draft.scheduledAt || merged[index].scheduledAt,
        rawStatus: "scheduled",
        status: "scheduled",
      };
      return;
    }

    merged.unshift(draft);
  });

  return merged;
}

function getNumberFromKeys(source, keys, fallback = 0) {
  for (const key of keys) {
    const value = source?.[key];
    if (typeof value === "number" && Number.isFinite(value)) {
      return value;
    }
    if (typeof value === "string" && value.trim() !== "" && Number.isFinite(Number(value))) {
      return Number(value);
    }
  }
  return fallback;
}

function normalizePaymentTypeKey(paymentType) {
  const normalized = String(paymentType || "")
    .trim()
    .toLowerCase()
    .replace(/[_-]+/g, " ")
    .replace(/\s+/g, " ");

  if (normalized.includes("individual")) {
    return "individual payment";
  }
  if (normalized.includes("entity")) {
    return "entity payment";
  }
  if (normalized.includes("international")) {
    return "international payment";
  }

  return normalized;
}

function normalizePayment(item) {
  const paymentId = item?.paymentId ?? item?.id ?? item?.paymentReference ?? item?.reference ?? "-";
  const paymentReference = item?.paymentReference ?? item?.reference ?? paymentId;
  const paymentType = item?.paymentType ?? item?.type ?? "-";
  const amount = getNumberFromKeys(item, ["amount", "paymentAmount", "totalAmount"], 0);
  const currency = item?.currency ?? item?.currencyCode ?? "-";
  const rawStatus = String(item?.status || "pending").toLowerCase();
  const serverScheduledAt =
    item?.scheduledAt ??
    item?.scheduledTime ??
    item?.scheduleDateTime ??
    item?.scheduledDateTime ??
    item?.scheduledDate ??
    null;
  const scheduledAt = serverScheduledAt;
  const status = derivePaymentStatus(item?.status, scheduledAt);
  const date = item?.date ?? item?.createdAt ?? item?.updatedAt ?? item?.timestamp ?? null;

  return {
    paymentId: String(paymentId),
    paymentReference: String(paymentReference),
    paymentType: String(paymentType),
    paymentTypeKey: normalizePaymentTypeKey(paymentType),
    amount,
    currency: String(currency),
    rawStatus,
    status,
    date,
    scheduledAt,
    recipientName: String(item?.recipientName ?? item?.payerName ?? "Unknown"),
    reason: String(item?.reason ?? "-"),
  };
}

async function apiRequest(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      Accept: "application/json",
      ...(options.headers || {}),
    },
    ...options,
  });

  const contentType = response.headers.get("content-type") || "";
  const isJson = contentType.includes("application/json");
  const body = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    const validationMessage =
      body?.message ||
      body?.error ||
      body?.details ||
      body?.errors?.[0]?.defaultMessage ||
      body?.errors?.[0]?.message;

    const message = isJson ? validationMessage || JSON.stringify(body) : body || `Request failed with status ${response.status}`;
    const error = new Error(message);
    error.status = response.status;
    error.path = path;
    throw error;
  }

  return body;
}

function isMethodNotAllowed(error) {
  return Number(error?.status) === 405 || String(error?.message || "").toLowerCase().includes("method not allowed");
}

async function schedulePaymentWithFallback(payload) {
  const attempts = [
    { path: "/api/payments/schedule", method: "POST" },
    { path: "/api/payments", method: "POST" },
    { path: "/api/payments/schedule", method: "PUT" },
  ];

  let lastError = null;

  for (const attempt of attempts) {
    try {
      return await apiRequest(attempt.path, {
        method: attempt.method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
    } catch (error) {
      lastError = error;
      if (!isMethodNotAllowed(error)) {
        throw error;
      }
    }
  }

  throw lastError || new Error("Unable to schedule payment.");
}

async function fetchPaymentHistory() {
  const payload = await apiRequest("/api/payments/history", { method: "GET" });
  const list = Array.isArray(payload) ? payload : payload?.data ?? payload?.items ?? [];
  const payments = Array.isArray(list) ? list.map(normalizePayment) : [];
  return mergeLocalScheduledDrafts(payments);
}

async function fetchDashboardStatistics() {
  const payload = await apiRequest("/api/dashboard/statistics", { method: "GET" });
  const data = payload?.data ?? payload ?? {};

  return {
    totalPayments: getNumberFromKeys(data, ["totalPayments", "total", "totalCount"]),
    completedPayments: getNumberFromKeys(data, ["completedPayments", "completed", "completedCount"]),
    failedPayments: getNumberFromKeys(data, ["failedPayments", "failed", "failedCount"]),
    refundedPayments: getNumberFromKeys(data, ["refundedPayments", "refunded", "refundedCount"]),
    scheduledPayments: getNumberFromKeys(data, ["scheduledPayments", "scheduled", "scheduledCount"]),
    totalAmount: getNumberFromKeys(data, ["totalAmount", "amountTotal", "paymentAmountTotal"]),
    individualPayment: getNumberFromKeys(data, ["individualPayment", "individualPayments", "individual_payment", "individual", "individualCount"]),
    entityPayment: getNumberFromKeys(data, ["entityPayment", "entityPayments", "entity_payment", "entity", "entityCount"]),
    internationalPayment: getNumberFromKeys(data, ["internationalPayment", "internationalPayments", "international_payment", "international", "internationalCount"]),
  };
}

function renderPaymentStatusChart(data) {
  const canvas = document.getElementById("paymentStatusChart");
  if (!canvas || typeof Chart === "undefined") {
    return;
  }

  if (paymentStatusChart) {
    paymentStatusChart.destroy();
  }

  paymentStatusChart = new Chart(canvas, {
    type: "pie",
    data: {
      labels: ["Completed", "Failed", "Refunded", "Scheduled"],
      datasets: [
        {
          data: [data.completedPayments, data.failedPayments, data.refundedPayments, data.scheduledPayments],
          backgroundColor: ["#16a34a", "#dc2626", "#0ea5a0", "#f59e0b"],
          borderColor: "#ffffff",
          borderWidth: 2,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: "bottom" },
      },
    },
  });
}

function renderPaymentTypeChart(data) {
  const canvas = document.getElementById("paymentTypeChart");
  if (!canvas || typeof Chart === "undefined") {
    return;
  }

  if (paymentTypeChart) {
    paymentTypeChart.destroy();
  }

  paymentTypeChart = new Chart(canvas, {
    type: "bar",
    data: {
      labels: ["Individual Payment", "Entity Payment", "International Payment"],
      datasets: [
        {
          label: "Payments",
          data: [data.individualPayment, data.entityPayment, data.internationalPayment],
          backgroundColor: ["#0f766e", "#0ea5a0", "#f59e0b"],
          borderRadius: 12,
          borderSkipped: false,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: {
        y: {
          beginAtZero: true,
          ticks: { precision: 0 },
        },
      },
    },
  });
}

function renderDashboardRecentRows(payments) {
  const rows = document.getElementById("dashboardRecentRows");
  if (!rows) {
    return;
  }

  if (payments.length === 0) {
    rows.innerHTML = `<tr><td colspan="6" class="empty-state">No transactions available.</td></tr>`;
    return;
  }

  rows.innerHTML = payments
    .slice(0, 6)
    .map(
      (payment) => `
      <tr>
        <td>${payment.paymentId}</td>
        <td>${payment.paymentType}</td>
        <td>${formatAmount(payment.amount, payment.currency)}</td>
        <td>${payment.currency}</td>
        <td>${statusPill(payment.status)}</td>
        <td>${formatDate(payment.date)}</td>
      </tr>
    `
    )
    .join("");
}

function setDashboardCards(summary) {
  document.getElementById("kpi-total-payments").textContent = String(summary.totalPayments);
  document.getElementById("kpi-total-amount").textContent = formatAmount(summary.totalAmount);
  document.getElementById("kpi-completed-payments").textContent = String(summary.completedPayments);
  document.getElementById("kpi-failed-payments").textContent = String(summary.failedPayments);
  document.getElementById("kpi-refunded-payments").textContent = String(summary.refundedPayments);
  document.getElementById("kpi-scheduled-payments").textContent = String(summary.scheduledPayments);
}

async function mountDashboard() {
  const alertBox = document.getElementById("dashboardAlert");
  setAlert(alertBox, "Loading dashboard data...", "info");

  try {
    const [dashboardStats, history] = await Promise.all([
      fetchDashboardStatistics().catch(() => null),
      fetchPaymentHistory(),
    ]);

    const derived = {
      totalPayments: history.length,
      totalAmount: history.reduce((sum, p) => sum + Number(p.amount || 0), 0),
      completedPayments: history.filter((p) => p.status === "completed").length,
      failedPayments: history.filter((p) => p.status === "failed").length,
      refundedPayments: history.filter((p) => p.status === "refunded").length,
      scheduledPayments: history.filter((p) => p.status === "scheduled").length,
      individualPayment: history.filter((p) => p.paymentTypeKey === "individual payment").length,
      entityPayment: history.filter((p) => p.paymentTypeKey === "entity payment").length,
      internationalPayment: history.filter((p) => p.paymentTypeKey === "international payment").length,
    };

    const summary = {
      totalPayments: dashboardStats?.totalPayments > 0 ? dashboardStats.totalPayments : derived.totalPayments,
      totalAmount: dashboardStats?.totalAmount > 0 ? dashboardStats.totalAmount : derived.totalAmount,
      completedPayments: dashboardStats?.completedPayments > 0 ? dashboardStats.completedPayments : derived.completedPayments,
      failedPayments: dashboardStats?.failedPayments > 0 ? dashboardStats.failedPayments : derived.failedPayments,
      refundedPayments: dashboardStats?.refundedPayments > 0 ? dashboardStats.refundedPayments : derived.refundedPayments,
      scheduledPayments: dashboardStats?.scheduledPayments > 0 ? dashboardStats.scheduledPayments : derived.scheduledPayments,
      individualPayment: dashboardStats?.individualPayment > 0 ? dashboardStats.individualPayment : derived.individualPayment,
      entityPayment: dashboardStats?.entityPayment > 0 ? dashboardStats.entityPayment : derived.entityPayment,
      internationalPayment: dashboardStats?.internationalPayment > 0 ? dashboardStats.internationalPayment : derived.internationalPayment,
    };

    setDashboardCards(summary);
    renderPaymentStatusChart(summary);
    renderPaymentTypeChart(summary);

    const recent = [...history].sort((a, b) => new Date(b.date || 0) - new Date(a.date || 0));
    renderDashboardRecentRows(recent);
    setAlert(alertBox, "Dashboard loaded successfully.");
  } catch (error) {
    setDashboardCards({
      totalPayments: 0,
      totalAmount: 0,
      completedPayments: 0,
      failedPayments: 0,
      refundedPayments: 0,
      scheduledPayments: 0,
    });
    renderPaymentStatusChart({ completedPayments: 0, failedPayments: 0, refundedPayments: 0, scheduledPayments: 0 });
    renderPaymentTypeChart({ individualPayment: 0, entityPayment: 0, internationalPayment: 0 });
    renderDashboardRecentRows([]);
    setAlert(alertBox, `Unable to load dashboard data. ${error?.message || "Please verify backend connectivity."}`, "danger");
  }
}

function mountCreatePayment() {
  const form = document.getElementById("paymentForm");
  const alertBox = document.getElementById("paymentAlert");
  const scheduleCheckbox = document.getElementById("schedulePayment");
  const scheduleGroup = document.getElementById("scheduleAtGroup");
  const scheduleInput = document.getElementById("scheduleAt");
  const submitButton = form.querySelector("button[type='submit']");

  function toggleScheduleFields() {
    const enabled = scheduleCheckbox.checked;
    scheduleGroup.classList.toggle("d-none", !enabled);
    scheduleInput.required = enabled;
    if (!enabled) {
      scheduleInput.value = "";
      scheduleInput.setCustomValidity("");
    }
  }

  scheduleCheckbox.addEventListener("change", toggleScheduleFields);
  scheduleInput.min = new Date(Date.now() + 60 * 1000).toISOString().slice(0, 16);
  toggleScheduleFields();

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (scheduleCheckbox.checked && !scheduleInput.value) {
      scheduleInput.setCustomValidity("Schedule date and time is required.");
    } else {
      scheduleInput.setCustomValidity("");
    }

    if (!form.checkValidity()) {
      form.classList.add("was-validated");
      return;
    }

    const payload = buildPaymentPayload(form, scheduleCheckbox.checked);

    try {
      submitButton.disabled = true;
      submitButton.textContent = scheduleCheckbox.checked ? "Scheduling..." : "Submitting...";

      const result = scheduleCheckbox.checked
        ? await schedulePaymentWithFallback(payload)
        : await apiRequest("/api/payments", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
          });

      form.reset();
      form.classList.remove("was-validated");
      toggleScheduleFields();
      const reference = result?.id || result?.paymentId || result?.reference;
      if (scheduleCheckbox.checked) {
        saveLocalScheduledDraft(payload, result);
      }
      setAlert(alertBox, `${scheduleCheckbox.checked ? "Payment scheduled" : "Payment submitted"} successfully.${reference ? ` Reference: ${reference}` : ""}`);
    } catch (error) {
      setAlert(alertBox, `Unable to submit payment. ${error?.message || "Please verify the backend is running."}`, "danger");
    } finally {
      submitButton.disabled = false;
      submitButton.textContent = "Submit";
    }
  });
}

function mountHistory() {
  const alertBox = document.getElementById("historyAlert");
  const typeFilter = document.getElementById("historyType");
  const statusFilter = document.getElementById("historyStatus");
  const currencyFilter = document.getElementById("historyCurrency");
  const dateFilter = document.getElementById("historyDate");
  const sortFilter = document.getElementById("historySort");
  const searchInput = document.getElementById("historySearch");
  const exportButton = document.getElementById("historyExportCsv");
  const prevButton = document.getElementById("historyPrev");
  const nextButton = document.getElementById("historyNext");
  const pageInfo = document.getElementById("historyPageInfo");
  const rows = document.getElementById("historyRows");

  const pageSize = 10;
  let currentPage = 1;
  let allPayments = [];
  let filteredPayments = [];

  function populateCurrencyOptions(payments) {
    const existing = new Set(["all"]);
    const options = ["<option value=\"all\" selected>all</option>"];
    payments.forEach((payment) => {
      const code = payment.currency;
      if (code && !existing.has(code)) {
        existing.add(code);
        options.push(`<option value="${code}">${code}</option>`);
      }
    });
    currencyFilter.innerHTML = options.join("");
  }

  function escapeCsvValue(value) {
    const text = String(value ?? "");
    return `"${text.replace(/"/g, '""')}"`;
  }

  function updatePagination(totalItems) {
    const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));
    currentPage = Math.max(1, Math.min(currentPage, totalPages));
    pageInfo.textContent = `Page ${currentPage} of ${totalPages}`;
    prevButton.disabled = currentPage <= 1;
    nextButton.disabled = currentPage >= totalPages;
  }

  function getCurrentPageItems(items) {
    const start = (currentPage - 1) * pageSize;
    return items.slice(start, start + pageSize);
  }

  function exportFilteredToCsv() {
    if (filteredPayments.length === 0) {
      setAlert(alertBox, "There is no filtered data to export.", "warning");
      return;
    }

    const header = ["Payment ID", "Type", "Amount", "Currency", "Status", "Date"];
    const lines = [header.map(escapeCsvValue).join(",")];

    filteredPayments.forEach((payment) => {
      lines.push([
        payment.paymentId,
        payment.paymentType,
        payment.amount,
        payment.currency,
        payment.status,
        formatDate(payment.date),
      ].map(escapeCsvValue).join(","));
    });

    const blob = new Blob([lines.join("\n")], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `payment-history-${new Date().toISOString().slice(0, 10)}.csv`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
  }

  function render() {
    const selectedType = typeFilter.value.toLowerCase();
    const selectedStatus = statusFilter.value.toLowerCase();
    const selectedCurrency = currencyFilter.value;
    const selectedDate = dateFilter.value;
    const selectedSort = sortFilter.value;
    const term = searchInput.value.trim().toLowerCase();

    filteredPayments = allPayments.filter((payment) => {
      const matchesType = selectedType === "all" || payment.paymentTypeKey === selectedType;
      const matchesStatus = selectedStatus === "all" || payment.status === selectedStatus;
      const matchesCurrency = selectedCurrency === "all" || payment.currency === selectedCurrency;
      const matchesDate = !selectedDate || toDateKey(payment.date) === selectedDate;
      const matchesReference = payment.paymentId.toLowerCase().includes(term);

      return matchesType && matchesStatus && matchesCurrency && matchesDate && matchesReference;
    });

    if (selectedSort === "newest") {
      filteredPayments.sort((a, b) => new Date(b.date || 0) - new Date(a.date || 0));
    }
    if (selectedSort === "oldest") {
      filteredPayments.sort((a, b) => new Date(a.date || 0) - new Date(b.date || 0));
    }
    if (selectedSort === "amount") {
      filteredPayments.sort((a, b) => Number(b.amount || 0) - Number(a.amount || 0));
    }

    updatePagination(filteredPayments.length);
    const pageItems = getCurrentPageItems(filteredPayments);

    if (filteredPayments.length === 0) {
      rows.innerHTML = `<tr><td colspan="7" class="empty-state">No transactions match your filters.</td></tr>`;
      return;
    }

    rows.innerHTML = pageItems
      .map((payment) => `
        <tr>
          <td>${payment.paymentId}</td>
          <td>${payment.paymentType}</td>
          <td>${formatAmount(payment.amount, payment.currency)}</td>
          <td>${payment.currency}</td>
          <td>${statusPill(payment.status)}</td>
          <td>${formatDate(payment.date)}</td>
          <td>
            <a class="btn btn-sm btn-outline-secondary" href="refund.html?paymentId=${encodeURIComponent(payment.paymentId)}">Refund</a>
          </td>
        </tr>
      `)
      .join("");
  }

  async function loadAndRender() {
    setAlert(alertBox, "Loading payment history...", "info");
    try {
      allPayments = await fetchPaymentHistory();
      populateCurrencyOptions(allPayments);
      currentPage = 1;
      render();
      setAlert(alertBox, `Loaded ${allPayments.length} payment record(s).`);
    } catch (error) {
      rows.innerHTML = `<tr><td colspan="7" class="empty-state">Unable to load payment history.</td></tr>`;
      setAlert(alertBox, `Unable to load payment history from /api/payments/history. ${error?.message || "Please verify backend connectivity."}`, "danger");
    }
  }

  function onFilterChange() {
    currentPage = 1;
    render();
  }

  typeFilter.addEventListener("change", onFilterChange);
  statusFilter.addEventListener("change", onFilterChange);
  currencyFilter.addEventListener("change", onFilterChange);
  dateFilter.addEventListener("change", onFilterChange);
  sortFilter.addEventListener("change", onFilterChange);
  searchInput.addEventListener("input", onFilterChange);

  prevButton.addEventListener("click", () => {
    currentPage -= 1;
    render();
  });
  nextButton.addEventListener("click", () => {
    currentPage += 1;
    render();
  });
  exportButton.addEventListener("click", exportFilteredToCsv);

  loadAndRender();
}

function mountSchedule() {
  const form = document.getElementById("scheduleForm");
  const alertBox = document.getElementById("scheduleAlert");
  const listAlert = document.getElementById("scheduleListAlert");
  const rows = document.getElementById("scheduledRows");
  const refreshButton = document.getElementById("scheduleRefresh");

  if (!form) {
    return;
  }

  const submitButton = form.querySelector("button[type='submit']");
  const scheduledAtInput = document.getElementById("scheduledAt");
  scheduledAtInput.min = new Date(Date.now() + 60 * 1000).toISOString().slice(0, 16);

  function isScheduledHistoryEntry(payment) {
    if (payment.status === "scheduled") {
      return true;
    }

    return ["created", "validated"].includes(payment.rawStatus) && Boolean(payment.scheduledAt);
  }

  async function loadScheduledPayments() {
    setAlert(listAlert, "Loading upcoming payments...", "info");

    try {
      const history = await fetchPaymentHistory();
      const upcoming = history.filter(isScheduledHistoryEntry);

      if (upcoming.length === 0) {
        rows.innerHTML = `<tr><td colspan="4" class="empty-state">No upcoming scheduled payments.</td></tr>`;
      } else {
        rows.innerHTML = upcoming
          .map((payment) => `
            <tr>
              <td>${payment.paymentId}</td>
              <td>${formatDate(payment.date)}</td>
              <td>${formatDate(payment.scheduledAt)}</td>
              <td>${statusPill(payment.status)}</td>
            </tr>
          `)
          .join("");
      }

      setAlert(listAlert, `Loaded ${upcoming.length} upcoming payment(s).`);
    } catch (error) {
      rows.innerHTML = `<tr><td colspan="4" class="empty-state">Unable to load scheduled payments.</td></tr>`;
      setAlert(listAlert, `Unable to load scheduled payments. ${error?.message || "Please verify backend connectivity."}`, "danger");
    }
  }

  refreshButton.addEventListener("click", loadScheduledPayments);

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (!form.checkValidity()) {
      form.classList.add("was-validated");
      return;
    }

    const payload = {
      ...buildPaymentPayload(form, false),
      scheduledAt: new Date(form.scheduledAt.value).toISOString(),
      scheduledDateTime: new Date(form.scheduledAt.value).toISOString(),
      note: form.note.value.trim(),
    };

    try {
      submitButton.disabled = true;
      submitButton.textContent = "Scheduling...";

      const result = await schedulePaymentWithFallback(payload);

      form.reset();
      form.classList.remove("was-validated");
      saveLocalScheduledDraft(payload, result);
      setAlert(alertBox, `Payment scheduled successfully.${result?.id ? ` Reference: ${result.id}` : ""}`);
      await loadScheduledPayments();
    } catch (error) {
      setAlert(alertBox, `Unable to schedule payment. ${error?.message || "Please try again."}`, "danger");
    } finally {
      submitButton.disabled = false;
      submitButton.textContent = "Schedule Payment";
    }
  });

  loadScheduledPayments();
}

function mountRefund() {
  const form = document.getElementById("refundForm");
  const paymentSelect = document.getElementById("paymentId");
  const amountInput = document.getElementById("refundAmount");
  const maxHint = document.getElementById("refundMaxHint");
  const rows = document.getElementById("refundRows");
  const alertBox = document.getElementById("refundAlert");
  const fullRadio = document.getElementById("refundFull");

  let paymentsById = new Map();

  function renderRefundHistory(payments) {
    const refunds = payments.filter((payment) => payment.status === "refunded");

    if (refunds.length === 0) {
      rows.innerHTML = `<tr><td colspan="6" class="empty-state">No refunds processed yet.</td></tr>`;
      return;
    }

    rows.innerHTML = refunds
      .map((refund) => `
        <tr>
          <td>REF-${refund.paymentId}</td>
          <td>${refund.paymentId}</td>
          <td>${refund.reason}</td>
          <td>${formatAmount(refund.amount, refund.currency)}</td>
          <td>${statusPill(refund.status)}</td>
          <td>${formatDate(refund.date)}</td>
        </tr>
      `)
      .join("");
  }

  function updateMaxAmount() {
    const selected = paymentSelect.options[paymentSelect.selectedIndex];
    const max = Number(selected?.dataset?.amount || 0);

    amountInput.max = String(max || "");
    maxHint.textContent = max ? `Maximum refundable amount: ${formatAmount(max)}` : "";

    if (fullRadio.checked && max > 0) {
      amountInput.value = String(max);
      amountInput.readOnly = true;
    } else {
      amountInput.readOnly = false;
    }
  }

  function fillPaymentOptions(payments) {
    paymentsById = new Map(payments.map((payment) => [payment.paymentId, payment]));

    if (payments.length === 0) {
      paymentSelect.innerHTML = `<option value="">No eligible payments</option>`;
      paymentSelect.disabled = true;
      amountInput.disabled = true;
      return;
    }

    paymentSelect.disabled = false;
    amountInput.disabled = false;
    paymentSelect.innerHTML = [
      '<option value="" selected disabled>Select payment</option>',
      ...payments.map(
        (payment) =>
          `<option value="${payment.paymentId}" data-amount="${payment.amount}">${payment.paymentId} - ${payment.recipientName} (${formatAmount(payment.amount, payment.currency)})</option>`
      ),
    ].join("");
  }

  async function loadRefundContext(showSuccess = true) {
    try {
      const history = await fetchPaymentHistory();
      const refundable = history.filter((payment) => payment.status === "completed");
      fillPaymentOptions(refundable);
      renderRefundHistory(history);
      updateMaxAmount();

      const queryId = new URLSearchParams(window.location.search).get("paymentId");
      if (queryId && paymentsById.has(queryId)) {
        paymentSelect.value = queryId;
        updateMaxAmount();
      }

      if (showSuccess) {
        setAlert(alertBox, `Loaded ${refundable.length} refundable payment(s).`);
      }
    } catch (error) {
      fillPaymentOptions([]);
      rows.innerHTML = `<tr><td colspan="6" class="empty-state">Unable to load refunds.</td></tr>`;
      setAlert(alertBox, `Unable to load payment data for refunds. ${error?.message || "Please try again."}`, "danger");
    }
  }

  paymentSelect.addEventListener("change", updateMaxAmount);
  document.querySelectorAll("input[name='refundType']").forEach((radio) => {
    radio.addEventListener("change", updateMaxAmount);
  });

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (!form.checkValidity()) {
      form.classList.add("was-validated");
      return;
    }

    const paymentId = paymentSelect.value;
    const selected = paymentSelect.options[paymentSelect.selectedIndex];
    const max = Number(selected?.dataset?.amount || 0);
    const amount = Number(amountInput.value);

    if (amount <= 0 || amount > max) {
      setAlert(alertBox, "Refund amount must be greater than 0 and not exceed the original payment.", "danger");
      return;
    }

    const submitButton = form.querySelector("button[type='submit']");

    try {
      submitButton.disabled = true;
      submitButton.textContent = "Submitting...";

      await apiRequest(`/api/payments/${encodeURIComponent(paymentId)}/refund`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          amount,
          reason: form.reason.value,
          notes: form.notes.value.trim(),
        }),
      });

      form.reset();
      form.classList.remove("was-validated");
      setAlert(alertBox, `Refund for payment ${paymentId} was submitted successfully.`);
      await loadRefundContext(false);
    } catch (error) {
      setAlert(alertBox, `Unable to process refund. ${error?.message || "Please try again."}`, "danger");
    } finally {
      submitButton.disabled = false;
      submitButton.textContent = "Complete Refund";
    }
  });

  loadRefundContext(true);
}

function setActiveNav() {
  const page = document.body.dataset.page;
  if (!page) {
    return;
  }

  document.querySelectorAll("[data-nav]").forEach((link) => {
    link.classList.toggle("active", link.dataset.nav === page);
  });
}

document.addEventListener("DOMContentLoaded", () => {
  setActiveNav();

  const page = document.body.dataset.page;
  if (page === "dashboard") {
    mountDashboard();
  }
  if (page === "create-payment") {
    mountCreatePayment();
  }
  if (page === "history") {
    mountHistory();
  }
  if (page === "schedule") {
    mountSchedule();
  }
  if (page === "refund") {
    mountRefund();
  }
});
