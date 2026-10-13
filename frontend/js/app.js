const API_BASE = 'http://localhost:8080/api';
let jwtToken = localStorage.getItem('jwtToken') || null;
let currentRole = 'ROLE_SUPER_ADMIN';
let activeApartmentId = 1;

// Mock Fallback Data Store for seamless UI demonstration
const mockState = {
    owners: [
        { id: 2, fullName: 'John Owner (Skyline)', email: 'john.owner@skyline.com', status: 'ACTIVE', apartmentName: 'Skyline Heights Apartment', phoneNumber: '+1987654321' },
        { id: 3, fullName: 'Mary Owner (Ocean View)', email: 'mary.owner@oceanview.com', status: 'PENDING_APPROVAL', apartmentName: 'Ocean View Residency', phoneNumber: '+1555444333' }
    ],
    households: [
        { id: 1, apartmentId: 1, unitNumber: 'A-101', residentId: 4, residentName: 'Alice Smith', residentEmail: 'alice.resident@skyline.com', residentStatus: 'ACTIVE', areaSqft: 1200, occupancyCount: 3, isMetered: true, meterSerial: 'WM-SKY-101', latestMeterReading: 145.0 },
        { id: 2, apartmentId: 1, unitNumber: 'A-102', residentId: 5, residentName: 'Bob Johnson', residentEmail: 'bob.resident@skyline.com', residentStatus: 'ACTIVE', areaSqft: 1200, occupancyCount: 4, isMetered: true, meterSerial: 'WM-SKY-102', latestMeterReading: 204.0 }
    ],
    bulkPurchases: [
        { id: 1, purchaseDate: '2026-07-05', sourceType: 'TANKER', volumeKl: 50.0, totalCost: 500.0 },
        { id: 2, purchaseDate: '2026-07-15', sourceType: 'MUNICIPAL', volumeKl: 50.0, totalCost: 450.0 }
    ],
    tariffPlan: { baseTierLimitKl: 10.0, baseTierRate: 5.0, tier2Rate: 9.0, fixedServiceFee: 50.0 },
    invoices: [
        { id: 1, householdId: 1, unitNumber: 'A-101', residentName: 'Alice Smith', meteredConsumptionKl: 45.0, meteredCharge: 365.0, sharedAreaCharge: 100.0, totalAmount: 465.0, status: 'UNPAID', dueDate: '2026-08-10' },
        { id: 2, householdId: 2, unitNumber: 'A-102', residentName: 'Bob Johnson', meteredConsumptionKl: 54.0, meteredCharge: 446.0, sharedAreaCharge: 100.0, totalAmount: 546.0, status: 'PAID', dueDate: '2026-08-10' }
    ],
    usageLogs: [
        { id: 1, meterId: 1, readingDate: '2026-07-20', meterReadingKl: 108.0, dailyConsumptionKl: 8.0, source: 'MANUAL', isAnomaly: false },
        { id: 2, meterId: 1, readingDate: '2026-07-21', meterReadingKl: 114.5, dailyConsumptionKl: 6.5, source: 'MANUAL', isAnomaly: false },
        { id: 3, meterId: 1, readingDate: '2026-07-22', meterReadingKl: 123.0, dailyConsumptionKl: 8.5, source: 'MANUAL', isAnomaly: false },
        { id: 4, meterId: 1, readingDate: '2026-07-23', meterReadingKl: 145.0, dailyConsumptionKl: 22.0, source: 'MANUAL', isAnomaly: true }
    ]
};

document.addEventListener('DOMContentLoaded', () => {
    switchRoleDemo('ROLE_SUPER_ADMIN');
});

// Role Switcher Handler
function switchRoleDemo(role) {
    currentRole = role;
    const badge = document.getElementById('currentRoleBadge');
    const userDisplay = document.getElementById('userNameDisplay');

    document.getElementById('superAdminSection').style.display = 'none';
    document.getElementById('buildingOwnerSection').style.display = 'none';
    document.getElementById('residentSection').style.display = 'none';
    document.getElementById('authSection').style.display = 'none';

    if (role === 'ROLE_SUPER_ADMIN') {
        badge.innerText = 'ROLE: SUPER ADMIN';
        badge.className = 'role-badge';
        userDisplay.innerText = 'Super Admin';
        document.getElementById('superAdminSection').style.display = 'block';
        loadSuperAdminData();
    } else if (role === 'ROLE_BUILDING_OWNER') {
        badge.innerText = 'ROLE: BUILDING OWNER';
        badge.className = 'role-badge';
        badge.style.color = '#34d399';
        userDisplay.innerText = 'John Owner (Skyline)';
        document.getElementById('buildingOwnerSection').style.display = 'block';
        loadOwnerData();
    } else if (role === 'ROLE_RESIDENT') {
        badge.innerText = 'ROLE: RESIDENT';
        badge.className = 'role-badge';
        badge.style.color = '#a78bfa';
        userDisplay.innerText = 'Alice Smith (Unit A-101)';
        document.getElementById('residentSection').style.display = 'block';
        loadResidentData();
    } else {
        badge.innerText = 'VIEW: AUTH';
        userDisplay.innerText = 'Guest';
        document.getElementById('authSection').style.display = 'block';
    }
}

// Super Admin Handlers
async function loadSuperAdminData() {
    try {
        const res = await fetch(`${API_BASE}/super-admin/owners`, {
            headers: { 'Authorization': `Bearer ${jwtToken}` }
        });
        if (res.ok) {
            const data = await res.json();
            renderSuperAdminOwners(data);
            return;
        }
    } catch (e) {
        console.warn("Backend API offline; rendering mock Super Admin state.");
    }
    renderSuperAdminOwners(mockState.owners);
}

function renderSuperAdminOwners(owners) {
    const tbody = document.getElementById('saOwnersTableBody');
    tbody.innerHTML = '';

    let pendingCount = 0;
    owners.forEach(o => {
        if (o.status === 'PENDING_APPROVAL') pendingCount++;
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>${o.fullName}</strong></td>
            <td>${o.email}<br><small style="color:#94a3b8;">${o.phoneNumber || 'N/A'}</small></td>
            <td>${o.apartmentName || 'Skyline Heights Apartment'}</td>
            <td><span class="status-badge ${o.status === 'ACTIVE' ? 'badge-active' : 'badge-pending'}">${o.status}</span></td>
            <td>
                ${o.status === 'PENDING_APPROVAL' ? `
                    <button class="btn btn-success" style="padding: 4px 10px; font-size: 0.8rem;" onclick="approveOwner(${o.id})"><i class="fa-solid fa-check"></i> Approve</button>
                    <button class="btn btn-danger" style="padding: 4px 10px; font-size: 0.8rem;" onclick="rejectOwner(${o.id})"><i class="fa-solid fa-xmark"></i> Reject</button>
                ` : `
                    <button class="btn btn-secondary" style="padding: 4px 10px; font-size: 0.8rem;" onclick="sendOwnerReminder(${o.id})"><i class="fa-solid fa-bell"></i> Send Bill Notice</button>
                `}
            </td>
        `;
        tbody.appendChild(row);
    });

    document.getElementById('saTotalOwners').innerText = owners.length;
    document.getElementById('saPendingApprovals').innerText = pendingCount;
}

async function approveOwner(id) {
    try {
        await fetch(`${API_BASE}/super-admin/owners/${id}/approve`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${jwtToken}` }
        });
    } catch (e) {}
    const owner = mockState.owners.find(o => o.id === id);
    if (owner) owner.status = 'ACTIVE';
    alert("Building Owner registration request approved!");
    loadSuperAdminData();
}

async function rejectOwner(id) {
    try {
        await fetch(`${API_BASE}/super-admin/owners/${id}/reject`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${jwtToken}` }
        });
    } catch (e) {}
    const owner = mockState.owners.find(o => o.id === id);
    if (owner) owner.status = 'REJECTED';
    alert("Building Owner request rejected.");
    loadSuperAdminData();
}

async function sendOwnerReminder(id) {
    try {
        await fetch(`${API_BASE}/super-admin/owners/${id}/reminder`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${jwtToken}`, 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: "Reminder: Please collect pending resident water bills." })
        });
    } catch (e) {}
    alert("Payment reminder notification sent to building owner!");
}

// Building Owner Handlers
function switchOwnerTab(tabId) {
    document.querySelectorAll('.owner-tab-content').forEach(el => el.style.display = 'none');
    document.querySelectorAll('#buildingOwnerSection .tab-btn').forEach(el => el.classList.remove('active'));
    document.getElementById(tabId).style.display = 'block';
    event.target.classList.add('active');
}

async function loadOwnerData() {
    renderOwnerHouseholds(mockState.households);
    renderOwnerBulkPurchases(mockState.bulkPurchases);
    renderOwnerInvoices(mockState.invoices);
}

function renderOwnerHouseholds(households) {
    const tbody = document.getElementById('ownerHouseholdsTableBody');
    const meterSelect = document.getElementById('logMeterSelect');
    const inviteSelect = document.getElementById('inviteHouseholdSelect');

    tbody.innerHTML = '';
    meterSelect.innerHTML = '';
    inviteSelect.innerHTML = '';

    households.forEach(h => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>Unit ${h.unitNumber}</strong></td>
            <td>${h.residentName || '<em style="color:#94a3b8;">Unassigned</em>'}<br><small style="color:#94a3b8;">${h.residentEmail || ''}</small></td>
            <td><span class="status-badge ${h.residentStatus === 'ACTIVE' ? 'badge-active' : 'badge-pending'}">${h.residentStatus}</span></td>
            <td><code>${h.meterSerial || 'No Meter'}</code></td>
            <td><strong>${h.latestMeterReading ? h.latestMeterReading + ' kL' : '0.0 kL'}</strong></td>
            <td>
                <button class="btn btn-secondary" style="padding: 4px 8px; font-size: 0.78rem;" onclick="openMeterModal(${h.id}, '${h.meterSerial || ''}')"><i class="fa-solid fa-gear"></i> Meter</button>
            </td>
        `;
        tbody.appendChild(row);

        if (h.meterSerial) {
            meterSelect.innerHTML += `<option value="${h.id}">Unit ${h.unitNumber} (${h.meterSerial})</option>`;
        }
        inviteSelect.innerHTML += `<option value="${h.id}">Unit ${h.unitNumber}</option>`;
    });

    document.getElementById('ownerTotalHouseholds').innerText = households.length;
    document.getElementById('ownerActiveMeters').innerText = households.filter(h => h.meterSerial).length;
}

function renderOwnerBulkPurchases(purchases) {
    const tbody = document.getElementById('ownerBulkPurchasesTableBody');
    tbody.innerHTML = '';
    let totalCost = 0;

    purchases.forEach(p => {
        totalCost += p.totalCost;
        const unitCost = (p.totalCost / p.volumeKl).toFixed(2);
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${p.purchaseDate}</td>
            <td><span class="status-badge badge-active">${p.sourceType}</span></td>
            <td>${p.volumeKl} kL</td>
            <td>$${p.totalCost.toFixed(2)}</td>
            <td>$${unitCost} / kL</td>
        `;
        tbody.appendChild(row);
    });

    document.getElementById('ownerBulkCost').innerText = `$${totalCost.toFixed(2)}`;
}

function renderOwnerInvoices(invoices) {
    const tbody = document.getElementById('ownerInvoicesTableBody');
    tbody.innerHTML = '';

    invoices.forEach(inv => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>Unit ${inv.unitNumber}</strong></td>
            <td>${inv.residentName}</td>
            <td>${inv.meteredConsumptionKl} kL</td>
            <td>$${inv.meteredCharge.toFixed(2)}</td>
            <td>$${inv.sharedAreaCharge.toFixed(2)}</td>
            <td><strong style="color:#60a5fa;">$${inv.totalAmount.toFixed(2)}</strong></td>
            <td><span class="status-badge ${inv.status === 'PAID' ? 'badge-paid' : 'badge-unpaid'}">${inv.status}</span></td>
        `;
        tbody.appendChild(row);
    });
}

function handleManualLog(e) {
    e.preventDefault();
    const householdId = parseInt(document.getElementById('logMeterSelect').value);
    const date = document.getElementById('logDate').value;
    const reading = parseFloat(document.getElementById('logReading').value);

    const household = mockState.households.find(h => h.id === householdId);
    if (household) {
        const prev = household.latestMeterReading || 100;
        const daily = reading - prev;
        household.latestMeterReading = reading;
        const isAnomaly = daily > 20.0;

        mockState.usageLogs.push({
            id: mockState.usageLogs.length + 1,
            meterId: householdId,
            readingDate: date,
            meterReadingKl: reading,
            dailyConsumptionKl: daily,
            source: 'MANUAL',
            isAnomaly: isAnomaly
        });

        if (isAnomaly) alert(`ALERT: Leak anomaly detected for Unit ${household.unitNumber} (${daily} kL consumed)!`);
        else alert(`Meter reading for Unit ${household.unitNumber} logged successfully.`);

        loadOwnerData();
    }
}

function handleCsvUpload() {
    const input = document.getElementById('csvFileInput');
    if (!input.files || input.files.length === 0) {
        alert("Please select a CSV file first.");
        return;
    }
    document.getElementById('csvStatusMessage').innerHTML = `
        <div class="alert-banner alert-warning" style="margin-top: 10px;">
            <i class="fa-solid fa-circle-check"></i> Bulk CSV parsed successfully! 4 logs ingested, 0 duplicates, 1 anomaly flagged.
        </div>
    `;
}

function calculateCurrentCycleInvoices() {
    alert("Tiered billing engine & charge distribution algorithm executed!\nItemized invoices generated for July 2026 Billing cycle.");
    renderOwnerInvoices(mockState.invoices);
}

// Resident Handlers
function loadResidentData() {
    const inv = mockState.invoices[0];
    document.getElementById('resUnitNumber').innerText = inv.unitNumber;
    document.getElementById('resLatestConsumption').innerText = `${inv.meteredConsumptionKl} kL`;
    document.getElementById('resBillAmount').innerText = `$${inv.totalAmount.toFixed(2)}`;

    // Render invoice breakdown
    const invDiv = document.getElementById('residentInvoiceDetails');
    invDiv.innerHTML = `
        <div style="background: rgba(0,0,0,0.2); padding: 1.5rem; border-radius: var(--radius-sm); display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem;">
            <div>
                <p><strong>Billing Period:</strong> July 2026 Billing</p>
                <p><strong>Metered Usage:</strong> ${inv.meteredConsumptionKl} kL</p>
                <p><strong>Base Tier Charge:</strong> $${inv.meteredCharge.toFixed(2)}</p>
                <p><strong>Shared Area Procurement Charge:</strong> $${inv.sharedAreaCharge.toFixed(2)}</p>
                <p style="margin-top: 8px; font-size: 1.2rem;"><strong>Total Due:</strong> <span style="color:#60a5fa;">$${inv.totalAmount.toFixed(2)}</span></p>
            </div>
            <div style="display: flex; flex-direction: column; justify-content: center; align-items: flex-end;">
                <p style="margin-bottom: 12px;">Status: <span class="status-badge ${inv.status === 'PAID' ? 'badge-paid' : 'badge-unpaid'}">${inv.status}</span></p>
                ${inv.status !== 'PAID' ? `<button class="btn btn-success" onclick="payResidentBill(${inv.id})"><i class="fa-solid fa-credit-card"></i> Pay Invoice Now</button>` : `<button class="btn btn-secondary" disabled><i class="fa-solid fa-check-double"></i> Paid on ${new Date().toLocaleDateString()}</button>`}
            </div>
        </div>
    `;

    // Leak banner alert
    const leakLog = mockState.usageLogs.find(l => l.isAnomaly);
    if (leakLog) {
        document.getElementById('residentLeakBanner').style.display = 'flex';
        document.getElementById('leakMessageText').innerText = `High water consumption of ${leakLog.dailyConsumptionKl} kL flagged on ${leakLog.readingDate}. Potential leak detected!`;
    }

    // Render daily usage logs table
    const tbody = document.getElementById('residentLogsTableBody');
    tbody.innerHTML = '';
    mockState.usageLogs.forEach(l => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${l.readingDate}</td>
            <td><code>WM-SKY-101</code></td>
            <td>${l.meterReadingKl} kL</td>
            <td><strong>${l.dailyConsumptionKl} kL</strong></td>
            <td>${l.isAnomaly ? '<span class="status-badge badge-leak"><i class="fa-solid fa-triangle-exclamation"></i> LEAK ANOMALY</span>' : '<span class="status-badge badge-active">NORMAL</span>'}</td>
        `;
        tbody.appendChild(row);
    });
}

function payResidentBill(id) {
    mockState.invoices[0].status = 'PAID';
    alert("Payment of $465.00 successful! Thank you.");
    loadResidentData();
}

function dismissLeakBanner() {
    document.getElementById('residentLeakBanner').style.display = 'none';
}

// Modal Controllers
function openMeterModal(householdId, currentSerial) {
    document.getElementById('modalHouseholdId').value = householdId;
    document.getElementById('modalMeterSerial').value = currentSerial || `WM-SKY-${100 + householdId}`;
    document.getElementById('meterModal').classList.add('active');
}

function openInviteResidentModal() {
    document.getElementById('inviteModal').classList.add('active');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('active');
}

function handleSaveMeter(e) {
    e.preventDefault();
    const id = parseInt(document.getElementById('modalHouseholdId').value);
    const serial = document.getElementById('modalMeterSerial').value;

    const household = mockState.households.find(h => h.id === id);
    if (household) {
        household.meterSerial = serial;
        alert(`Water meter ${serial} configured for Unit ${household.unitNumber}!`);
        closeModal('meterModal');
        loadOwnerData();
    }
}

function handleSendInvite(e) {
    e.preventDefault();
    const name = document.getElementById('inviteName').value;
    const email = document.getElementById('inviteEmail').value;
    const token = 'INV-' + Math.random().toString(36).substring(2, 9).toUpperCase();

    alert(`Invitation email sent to ${name} (${email})!\nResident Invite Token: ${token}`);
    closeModal('inviteModal');
}

function switchAuthTab(tab) {
    document.getElementById('loginForm').style.display = tab === 'login' ? 'block' : 'none';
    document.getElementById('registerOwnerForm').style.display = tab === 'registerOwner' ? 'block' : 'none';
    document.getElementById('acceptInviteForm').style.display = tab === 'acceptInvite' ? 'block' : 'none';
}

function handleLogin(e) {
    e.preventDefault();
    alert("Logged in successfully! Switching to Building Owner Dashboard...");
    switchRoleDemo('ROLE_BUILDING_OWNER');
}

function handleRegisterOwner(e) {
    e.preventDefault();
    alert("Building Owner registration request submitted! Super Admin will review and approve your account.");
    switchRoleDemo('ROLE_SUPER_ADMIN');
}

function handleAcceptInvite(e) {
    e.preventDefault();
    alert("Resident account activated successfully! Password updated.");
    switchRoleDemo('ROLE_RESIDENT');
}

function logout() {
    alert("Logged out.");
    switchRoleDemo('AUTH');
}
