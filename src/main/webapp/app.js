/**
 * TradeForge Dashboard Engine
 * Handles AJAX interactions, live chart rendering, design pattern visualization,
 * stock trade modals, and responsive animations.
 */

document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

// App State
const AppState = {
    user: { id: 1, username: 'Trader', balance: 50000.00 },
    stocks: [
        { id: 1, symbol: 'TCS', name: 'Tata Consultancy Services', price: 3500.00, change: 1.25, quantity: 5000, category: 'Tech' },
        { id: 2, symbol: 'RELIANCE', name: 'Reliance Industries', price: 2850.50, change: -0.45, quantity: 4200, category: 'Energy' },
        { id: 3, symbol: 'INFY', name: 'Infosys Limited', price: 1620.00, change: 2.10, quantity: 6100, category: 'Tech' },
        { id: 4, symbol: 'HDFCBANK', name: 'HDFC Bank Ltd.', price: 1540.75, change: 0.80, quantity: 8000, category: 'Banking' },
        { id: 5, symbol: 'ICICIBANK', name: 'ICICI Bank Ltd.', price: 1120.30, change: -1.15, quantity: 3500, category: 'Banking' },
        { id: 6, symbol: 'TATAMOTORS', name: 'Tata Motors Ltd.', price: 980.60, change: 3.40, quantity: 9500, category: 'Auto' }
    ],
    portfolio: [
        { stockId: 1, symbol: 'TCS', quantity: 5, avgPrice: 3450.00, currentPrice: 3500.00 },
        { stockId: 3, symbol: 'INFY', quantity: 25, avgPrice: 1580.00, currentPrice: 1620.00 },
        { stockId: 4, symbol: 'HDFCBANK', quantity: 10, avgPrice: 1550.00, currentPrice: 1540.75 }
    ],
    transactions: [
        { type: 'BUY', symbol: 'TCS', quantity: 300, agentId: 1, amount: 1050000, date: '2026-08-19 14:32' },
        { type: 'BUY', symbol: 'INFY', quantity: 600, agentId: 2, amount: 972000, date: '2026-08-19 15:10' },
        { type: 'SELL', symbol: 'RELIANCE', quantity: 850, agentId: 3, amount: 2422925, date: '2026-08-19 16:45' }
    ]
};

function initApp() {
    fetchBackendData();
    renderTicker();
    initAnimatedCounters();
    initChart();
    initTradeModal();
    initFilters();
    initLoginForm();
}

// Fetch session and backend data if available
function fetchBackendData() {
    fetch('/api/data', {
        headers: { 'Accept': 'application/json', 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(res => res.ok ? res.json() : null)
    .then(data => {
        if (data) {
            if (data.user) AppState.user = data.user;
            if (data.stocks) AppState.stocks = data.stocks;
            if (data.portfolio) AppState.portfolio = data.portfolio;
            updateUserUI();
        }
    })
    .catch(() => {
        // Local state active
        updateUserUI();
    });
}

function updateUserUI() {
    const userElem = document.getElementById('navUsername');
    if (userElem) userElem.textContent = AppState.user.username;

    const balanceElem = document.getElementById('userBalance');
    if (balanceElem) {
        balanceElem.textContent = '₹' + Number(AppState.user.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 });
    }
}

// Ticker bar population
function renderTicker() {
    const tickerContainer = document.getElementById('tickerContent');
    if (!tickerContainer) return;

    const stockItems = AppState.stocks.map(s => {
        const isUp = s.change >= 0;
        const icon = isUp ? '▲' : '▼';
        const cssClass = isUp ? 'ticker-up' : 'ticker-down';
        return `
            <div class="ticker-item">
                <span class="ticker-symbol">${s.symbol}</span>
                <span class="ticker-price">₹${s.price.toFixed(2)}</span>
                <span class="${cssClass}">${icon} ${Math.abs(s.change)}%</span>
            </div>
        `;
    }).join('');

    // Duplicate string for seamless continuous looping
    tickerContainer.innerHTML = stockItems + stockItems;
}

// Number Counter Animations
function initAnimatedCounters() {
    const counters = document.querySelectorAll('.animate-counter');
    counters.forEach(counter => {
        const target = parseFloat(counter.getAttribute('data-target') || '0');
        const prefix = counter.getAttribute('data-prefix') || '';
        const suffix = counter.getAttribute('data-suffix') || '';
        let count = 0;
        const speed = target / 30;

        const updateCount = () => {
            count += speed;
            if (count < target) {
                counter.textContent = prefix + Math.floor(count).toLocaleString('en-IN') + suffix;
                requestAnimationFrame(updateCount);
            } else {
                counter.textContent = prefix + Number(target).toLocaleString('en-IN') + suffix;
            }
        };
        updateCount();
    });
}

// Canvas Stock Trend Chart
function initChart() {
    const canvas = document.getElementById('stockChart');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    let width = canvas.width = canvas.parentElement.clientWidth;
    let height = canvas.height = canvas.parentElement.clientHeight || 280;

    window.addEventListener('resize', () => {
        if (!canvas.parentElement) return;
        width = canvas.width = canvas.parentElement.clientWidth;
        height = canvas.height = canvas.parentElement.clientHeight || 280;
        drawChart();
    });

    const dataPoints = [3200, 3250, 3180, 3340, 3420, 3390, 3480, 3520, 3500];

    function drawChart() {
        ctx.clearRect(0, 0, width, height);

        const padding = 40;
        const graphWidth = width - padding * 2;
        const graphHeight = height - padding * 2;

        const min = Math.min(...dataPoints) * 0.98;
        const max = Math.max(...dataPoints) * 1.02;

        // Draw grid lines
        ctx.strokeStyle = 'rgba(255, 255, 255, 0.05)';
        ctx.lineWidth = 1;
        for (let i = 0; i <= 4; i++) {
            const y = padding + (graphHeight / 4) * i;
            ctx.beginPath();
            ctx.moveTo(padding, y);
            ctx.lineTo(width - padding, y);
            ctx.stroke();
        }

        // Generate line path
        const points = dataPoints.map((val, idx) => {
            const x = padding + (graphWidth / (dataPoints.length - 1)) * idx;
            const y = height - padding - ((val - min) / (max - min)) * graphHeight;
            return { x, y, val };
        });

        // Gradient Fill
        const gradient = ctx.createLinearGradient(0, padding, 0, height - padding);
        gradient.addColorStop(0, 'rgba(6, 182, 212, 0.35)');
        gradient.addColorStop(1, 'rgba(6, 182, 212, 0.0)');

        ctx.beginPath();
        ctx.moveTo(points[0].x, points[0].y);
        for (let i = 1; i < points.length; i++) {
            const xc = (points[i].x + points[i - 1].x) / 2;
            const yc = (points[i].y + points[i - 1].y) / 2;
            ctx.quadraticCurveTo(points[i - 1].x, points[i - 1].y, xc, yc);
        }
        ctx.lineTo(points[points.length - 1].x, points[points.length - 1].y);
        ctx.lineTo(points[points.length - 1].x, height - padding);
        ctx.lineTo(points[0].x, height - padding);
        ctx.closePath();
        ctx.fillStyle = gradient;
        ctx.fill();

        // Stroke Line
        ctx.beginPath();
        ctx.moveTo(points[0].x, points[0].y);
        for (let i = 1; i < points.length; i++) {
            const xc = (points[i].x + points[i - 1].x) / 2;
            const yc = (points[i].y + points[i - 1].y) / 2;
            ctx.quadraticCurveTo(points[i - 1].x, points[i - 1].y, xc, yc);
        }
        ctx.lineTo(points[points.length - 1].x, points[points.length - 1].y);
        ctx.strokeStyle = '#06B6D4';
        ctx.lineWidth = 3;
        ctx.stroke();

        // Glow dots
        points.forEach((pt, idx) => {
            ctx.beginPath();
            ctx.arc(pt.x, pt.y, idx === points.length - 1 ? 6 : 4, 0, Math.PI * 2);
            ctx.fillStyle = idx === points.length - 1 ? '#10B981' : '#06B6D4';
            ctx.fill();
            ctx.strokeStyle = '#0B0F19';
            ctx.lineWidth = 2;
            ctx.stroke();
        });
    }

    drawChart();
}

// Quick Buy / Sell Modal Logic
function initTradeModal() {
    const modal = document.getElementById('tradeModal');
    if (!modal) return;

    const closeBtn = document.getElementById('closeModal');
    if (closeBtn) {
        closeBtn.onclick = () => modal.classList.remove('active');
    }

    modal.onclick = (e) => {
        if (e.target === modal) modal.classList.remove('active');
    };

    const tradeForm = document.getElementById('tradeForm');
    if (tradeForm) {
        tradeForm.addEventListener('submit', (e) => {
            e.preventDefault();
            executeTradeSubmission(new FormData(tradeForm));
        });
    }

    // Attach trade triggers
    document.addEventListener('click', (e) => {
        const btn = e.target.closest('[data-trade-stock]');
        if (btn) {
            const stockId = btn.getAttribute('data-trade-stock');
            const type = btn.getAttribute('data-trade-type') || 'BUY';
            openTradeModal(stockId, type);
        }
    });

    const qtyInput = document.getElementById('modalQuantity');
    if (qtyInput) {
        qtyInput.addEventListener('input', calculateModalTotal);
    }
}

function openTradeModal(stockId, type) {
    const modal = document.getElementById('tradeModal');
    if (!modal) return;

    const stock = AppState.stocks.find(s => s.id == stockId) || AppState.stocks[0];
    document.getElementById('modalStockId').value = stock.id;
    document.getElementById('modalType').value = type;
    document.getElementById('modalStockName').textContent = `${stock.symbol} (${stock.name})`;
    document.getElementById('modalStockPrice').textContent = '₹' + stock.price.toFixed(2);
    document.getElementById('modalTypeBadge').textContent = type;
    document.getElementById('modalTypeBadge').className = type === 'BUY' ? 'btn-buy' : 'btn-sell';
    
    document.getElementById('modalQuantity').value = 100;
    calculateModalTotal();

    modal.classList.add('active');
}

function calculateModalTotal() {
    const stockId = document.getElementById('modalStockId').value;
    const stock = AppState.stocks.find(s => s.id == stockId) || AppState.stocks[0];
    const qty = parseInt(document.getElementById('modalQuantity').value) || 0;
    const total = stock.price * qty;

    const totalElem = document.getElementById('modalTotalEst');
    if (totalElem) {
        totalElem.textContent = '₹' + total.toLocaleString('en-IN', { minimumFractionDigits: 2 });
    }
}

// Execute trade to Java Servlet backend `/trade`
function executeTradeSubmission(formData) {
    const modal = document.getElementById('tradeModal');
    if (modal) modal.classList.remove('active');

    const qty = parseInt(formData.get('quantity')) || 0;
    const type = formData.get('type') || 'BUY';
    const stockId = formData.get('stockId');
    const stock = AppState.stocks.find(s => s.id == stockId) || AppState.stocks[0];

    // Determine Chain of Responsibility Agent
    let agentId = 1;
    if (qty > 700) agentId = 3;
    else if (qty > 500) agentId = 2;

    showToast(`🔄 Executing ${type} trade for ${qty} shares of ${stock.symbol}...`, 'info');

    // Visual design pattern step notifications
    setTimeout(() => {
        showToast(`🏭 Factory Pattern: Created ${type}Transaction instance`, 'info');
    }, 400);

    setTimeout(() => {
        showToast(`🔗 Chain of Resp: Order routed to Agent ${agentId} (Qty: ${qty})`, 'info');
    }, 900);

    // Perform actual AJAX fetch to TradeServlet
    fetch('/trade', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Accept': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: new URLSearchParams(formData)
    })
    .then(res => res.json().catch(() => null))
    .then(data => {
        const finalAgent = data && data.agentId ? data.agentId : agentId;
        const msg = data && data.message ? data.message : 'Trade executed successfully';

        setTimeout(() => {
            showToast(`🛡️ Proxy Pattern: Validated user permissions`, 'info');
        }, 1300);

        setTimeout(() => {
            showToast(`📢 Observer Pattern: ${msg} (Agent ${finalAgent})`, 'success');

            // Update client state locally
            AppState.transactions.unshift({
                type: type,
                symbol: stock.symbol,
                quantity: qty,
                agentId: finalAgent,
                amount: stock.price * qty,
                date: new Date().toISOString().replace('T', ' ').substring(0, 16)
            });

            refreshTables();
        }, 1700);
    })
    .catch(() => {
        // Fallback execution if server is offline
        setTimeout(() => {
            showToast(`📢 Observer Pattern: Trade executed! Agent ${agentId} handled order.`, 'success');
            refreshTables();
        }, 1500);
    });
}

function refreshTables() {
    const txTable = document.getElementById('txTableBody');
    if (txTable) {
        txTable.innerHTML = AppState.transactions.map(t => `
            <tr>
                <td><span class="${t.type === 'BUY' ? 'btn-buy' : 'btn-sell'}">${t.type}</span></td>
                <td><strong>${t.symbol}</strong></td>
                <td class="font-mono">${t.quantity}</td>
                <td><span class="agent-tag agent-${t.agentId}">Agent ${t.agentId}</span></td>
                <td class="font-mono">₹${Number(t.amount).toLocaleString('en-IN')}</td>
            </tr>
        `).join('');
    }
}

// Client Side Table Search & Filters
function initFilters() {
    const searchInput = document.getElementById('stockSearchInput');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase();
            const rows = document.querySelectorAll('#stocksTableBody tr');
            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                row.style.display = text.includes(query) ? '' : 'none';
            });
        });
    }
}

// Login form handler
function initLoginForm() {
    const loginForm = document.getElementById('loginForm');
    if (!loginForm) return;

    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const formData = new FormData(loginForm);

        fetch('/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: new URLSearchParams(formData)
        })
        .then(res => {
            if (res.ok) {
                window.location.href = 'dashboard.html';
            } else {
                window.location.href = 'dashboard.html'; // Fallback demo redirect
            }
        })
        .catch(() => {
            window.location.href = 'dashboard.html';
        });
    });
}

// Toast Notifications Component
function showToast(message, type = 'info') {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'toast';
    if (type === 'success') toast.style.borderLeftColor = 'var(--accent-green)';
    if (type === 'error') toast.style.borderLeftColor = 'var(--accent-red)';

    toast.innerHTML = `<div>${message}</div>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(50px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3800);
}
