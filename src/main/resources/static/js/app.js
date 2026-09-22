/**
 * LeaveFlow Enterprise — Application Controller v4.0
 * Handles: REST API, navigation, leave lifecycle, charts, calendar, notifications.
 * Zero browser alert() or confirm() popups.
 */

'use strict';

const API = '/api';

/* ══════════════════════════════════════════════════
   STATE
══════════════════════════════════════════════════ */
let employees         = [];
let leaves            = [];
let notifications     = [];
let currentFilter     = 'ALL';
let currentEmployeeId = null;
let currentCalDate    = new Date();
let pendingDeleteId   = null;
let healthPollInterval = null;

/* ══════════════════════════════════════════════════
   DOM REFS — Shell
══════════════════════════════════════════════════ */
const appSidebar        = document.getElementById('app-sidebar');
const sidebarOverlay    = document.getElementById('sidebar-overlay');
const collapseBtn       = document.getElementById('sidebar-collapse-btn');
const mobileNavBtn      = document.getElementById('mobile-nav-btn');
const globalSearch      = document.getElementById('global-search');
const profileBtn        = document.getElementById('profile-btn');
const profileDropdown   = document.getElementById('profile-dropdown');
const bellBtn           = document.getElementById('btn-bell');
const notifDropdown     = document.getElementById('notif-dropdown');
const notifBadge        = document.getElementById('notif-badge');
const notifList         = document.getElementById('notif-list');
const notifStream       = document.getElementById('notif-stream');
const markAllReadBtn    = document.getElementById('btn-mark-all-read');
const notifMarkAllBtn   = document.getElementById('btn-notif-mark-all');
const pageTitle         = document.getElementById('page-title');
const pageSubtitle      = document.getElementById('page-subtitle');
const tabPendingBadge   = document.getElementById('tab-pending-badge');
const themeToggleBtn    = document.getElementById('btn-theme-toggle');
const iconSun           = document.getElementById('icon-sun');
const iconMoon          = document.getElementById('icon-moon');
const themeLabel        = document.getElementById('theme-label');
const resetDemoBtn      = document.getElementById('btn-reset-demo');
const toastStack        = document.getElementById('toast-stack');

/* ══════════════════════════════════════════════════
   DOM REFS — Stats
══════════════════════════════════════════════════ */
const statEmp       = document.getElementById('count-employees');
const statTotal     = document.getElementById('count-total-leaves');
const statPending   = document.getElementById('count-pending-leaves');
const statApproved  = document.getElementById('count-approved-leaves');
const statRejected  = document.getElementById('count-rejected-leaves');

/* ══════════════════════════════════════════════════
   DOM REFS — Apply Leave Form
══════════════════════════════════════════════════ */
const empSelect       = document.getElementById('leave-emp-select');
const balanceSection  = document.getElementById('balance-section');
const balCasual       = document.getElementById('bal-casual');
const balSick         = document.getElementById('bal-sick');
const balAnnual       = document.getElementById('bal-annual');
const progCasual      = document.getElementById('prog-casual');
const progSick        = document.getElementById('prog-sick');
const progAnnual      = document.getElementById('prog-annual');
const footCasual      = document.getElementById('foot-casual');
const footSick        = document.getElementById('foot-sick');
const footAnnual      = document.getElementById('foot-annual');
const chipCasual      = document.getElementById('chip-casual');
const chipSick        = document.getElementById('chip-sick');
const chipAnnual      = document.getElementById('chip-annual');
const startInput      = document.getElementById('leave-start');
const endInput        = document.getElementById('leave-end');
const durationBanner  = document.getElementById('duration-preview');
const reasonInput     = document.getElementById('leave-reason');
const reasonCounter   = document.getElementById('reason-char-count');
const historyTitle    = document.getElementById('history-title');
const historySub      = document.getElementById('history-subtitle');
const historyList     = document.getElementById('history-list');

/* ══════════════════════════════════════════════════
   DOM REFS — Calendar
══════════════════════════════════════════════════ */
const calMonthTitle = document.getElementById('cal-month-title');
const calGrid       = document.getElementById('cal-grid');
const calPrev       = document.getElementById('cal-prev');
const calNext       = document.getElementById('cal-next');
const calToday      = document.getElementById('cal-today');

/* ══════════════════════════════════════════════════
   DOM REFS — Health Modal
══════════════════════════════════════════════════ */
const healthModal    = document.getElementById('modal-health');
const healthDot      = document.getElementById('health-modal-dot');
const healthStatus   = document.getElementById('health-modal-status');
const healthDb       = document.getElementById('health-db');
const healthPing     = document.getElementById('health-ping');
const healthProfile  = document.getElementById('health-profile');
const healthUptime   = document.getElementById('health-uptime');
const healthMemText  = document.getElementById('health-mem-text');
const healthMemBar   = document.getElementById('health-mem-bar');
const statusDot      = document.getElementById('status-dot');
const statusText     = document.getElementById('status-text');
const dbBadge        = document.getElementById('database-badge-text');

/* ══════════════════════════════════════════════════
   INIT
══════════════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  initShell();
  setupEventListeners();
  setupDateDefaults();
  initApp();
});

async function initApp() {
  await checkHealth();
  healthPollInterval = setInterval(checkHealth, 20000);
  await Promise.all([loadEmployees(), loadLeaves(), loadNotifications(), loadAnalytics()]);
  renderCalendar();
}

/* ══════════════════════════════════════════════════
   SHELL — Sidebar, Mobile Drawer, Dropdowns
══════════════════════════════════════════════════ */
function initShell() {
  // Sidebar collapse state
  const collapsed = localStorage.getItem('lf_sidebar_collapsed') === 'true';
  if (collapsed) appSidebar.classList.add('collapsed');

  collapseBtn?.addEventListener('click', () => {
    appSidebar.classList.toggle('collapsed');
    localStorage.setItem('lf_sidebar_collapsed', appSidebar.classList.contains('collapsed'));
    lucide.createIcons();
  });

  // Mobile drawer
  mobileNavBtn?.addEventListener('click', () => openMobileDrawer());
  sidebarOverlay?.addEventListener('click', () => closeMobileDrawer());

  // Profile dropdown
  profileBtn?.addEventListener('click', (e) => {
    e.stopPropagation();
    closeAllDropdowns('profile-dropdown');
    profileDropdown?.classList.toggle('open');
    profileBtn.setAttribute('aria-expanded', profileDropdown?.classList.contains('open'));
  });

  // Notifications dropdown
  bellBtn?.addEventListener('click', (e) => {
    e.stopPropagation();
    closeAllDropdowns('notif-dropdown');
    notifDropdown?.classList.toggle('open');
    bellBtn.setAttribute('aria-expanded', notifDropdown?.classList.contains('open'));
  });

  // Close dropdowns on outside click
  document.addEventListener('click', (e) => {
    if (!profileDropdown?.contains(e.target) && !profileBtn?.contains(e.target)) {
      profileDropdown?.classList.remove('open');
    }
    if (!notifDropdown?.contains(e.target) && !bellBtn?.contains(e.target)) {
      notifDropdown?.classList.remove('open');
    }
  });

  // Global search
  globalSearch?.addEventListener('input', (e) => {
    const q = e.target.value.toLowerCase().trim();
    const mySearch = document.getElementById('myleaves-search');
    const empSearch = document.getElementById('emp-search');
    const approvalSearch = document.getElementById('approvals-search');
    if (mySearch) { mySearch.value = q; renderMyLeavesTable(); }
    if (empSearch) { empSearch.value = q; filterEmployees(); }
    if (approvalSearch) { approvalSearch.value = q; renderApprovalsTable(); }
  });

  // Header quick actions
  document.getElementById('btn-quick-apply')?.addEventListener('click', () => switchTab('employee-tab'));
  document.getElementById('btn-hero-apply')?.addEventListener('click', () => switchTab('employee-tab'));
  document.getElementById('btn-goto-approvals')?.addEventListener('click', () => switchTab('admin-tab'));

  // Export CSV
  const csvHandler = () => {
    window.location.href = `${API}/leaves/export`;
    showToast('CSV export downloading…', 'info');
  };
  document.getElementById('btn-export-csv')?.addEventListener('click', csvHandler);
  document.getElementById('btn-export-csv-reports')?.addEventListener('click', csvHandler);
  document.getElementById('btn-export-csv-2')?.addEventListener('click', csvHandler);

  // Health indicator
  document.getElementById('health-indicator-btn')?.addEventListener('click', () => {
    checkHealth();
    openModal('modal-health');
  });

  // Re-probe health
  document.getElementById('btn-probe-health')?.addEventListener('click', async () => {
    showToast('Probing server…', 'info');
    await checkHealth();
    showToast('Health data refreshed', 'success');
  });
}

function openMobileDrawer() {
  appSidebar.classList.add('mobile-open');
  sidebarOverlay.classList.add('active');
  mobileNavBtn?.setAttribute('aria-expanded', 'true');
  document.body.style.overflow = 'hidden';
}

function closeMobileDrawer() {
  appSidebar.classList.remove('mobile-open');
  sidebarOverlay.classList.remove('active');
  mobileNavBtn?.setAttribute('aria-expanded', 'false');
  document.body.style.overflow = '';
}

function closeAllDropdowns(except) {
  if (except !== 'profile-dropdown') profileDropdown?.classList.remove('open');
  if (except !== 'notif-dropdown')   notifDropdown?.classList.remove('open');
}

/* ══════════════════════════════════════════════════
   TAB NAVIGATION
══════════════════════════════════════════════════ */
const PAGE_TITLES = {
  'dashboard-tab':      ['Overview Dashboard',    'Enterprise Leave Management System'],
  'employee-tab':       ['Apply For Leave',        'Submit application & preview live balances'],
  'my-leaves-tab':      ['My Leaves',              'Search, filter, and inspect leave history'],
  'admin-tab':          ['Leave Approvals',        'Review and manage employee applications'],
  'directory-tab':      ['Employee Directory',     'Profiles, departments, and quota allocations'],
  'calendar-tab':       ['Team Schedule',          'Out-of-office visibility & coverage map'],
  'analytics-tab':      ['Analytics & Insights',  'Company-wide leave metrics and trends'],
  'notifications-tab':  ['Notification Center',   'Audit stream & email dispatch log'],
  'reports-tab':        ['Reports & Export',       'Download HR CSV audit spreadsheets'],
  'settings-tab':       ['System Settings',        'Configure preferences and profile options'],
  'architecture-tab':   ['Architecture & APIs',    'Spring Boot 3 layered architecture reference'],
};

function switchTab(tabId) {
  // Update nav buttons
  document.querySelectorAll('.nav-btn').forEach(btn => {
    const isTarget = btn.getAttribute('data-tab') === tabId;
    btn.classList.toggle('active', isTarget);
    btn.setAttribute('aria-selected', isTarget);
  });

  // Show/hide panes
  document.querySelectorAll('.tab-pane').forEach(p => {
    p.classList.remove('active');
    p.setAttribute('aria-hidden', 'true');
  });
  const pane = document.getElementById(tabId);
  if (pane) {
    pane.classList.add('active');
    pane.removeAttribute('aria-hidden');
  }

  // Update header breadcrumb
  if (PAGE_TITLES[tabId]) {
    if (pageTitle)    pageTitle.textContent    = PAGE_TITLES[tabId][0];
    if (pageSubtitle) pageSubtitle.textContent = PAGE_TITLES[tabId][1];
  }

  // Trigger side-effects
  if (tabId === 'calendar-tab') renderCalendar();
  if (tabId === 'analytics-tab') loadAnalytics();
  if (tabId === 'my-leaves-tab') renderMyLeavesTable();
  if (tabId === 'employee-tab' && currentEmployeeId) loadEmployeeHistory(currentEmployeeId);

  // Close mobile drawer after navigation
  closeMobileDrawer();
  lucide.createIcons();
}

// Wire up all nav buttons
document.querySelectorAll('.nav-btn[data-tab]').forEach(btn => {
  btn.addEventListener('click', () => switchTab(btn.getAttribute('data-tab')));
});

/* ══════════════════════════════════════════════════
   THEME
══════════════════════════════════════════════════ */
function initTheme() {
  const saved = localStorage.getItem('lf_theme') || 'dark';
  applyTheme(saved);
  themeToggleBtn?.addEventListener('click', () => {
    const current = document.documentElement.getAttribute('data-theme') || 'dark';
    applyTheme(current === 'dark' ? 'light' : 'dark');
    profileDropdown?.classList.remove('open');
  });
}

function applyTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  localStorage.setItem('lf_theme', theme);
  const isDark = theme === 'dark';
  if (iconSun)  iconSun.style.display  = isDark ? 'none' : 'inline-block';
  if (iconMoon) iconMoon.style.display = isDark ? 'inline-block' : 'none';
  if (themeLabel) themeLabel.textContent = isDark ? 'Switch to Light' : 'Switch to Dark';
  lucide.createIcons();
}

/* ══════════════════════════════════════════════════
   MODAL HELPERS
══════════════════════════════════════════════════ */
function openModal(id) {
  const m = document.getElementById(id);
  if (m) { m.classList.add('open'); document.body.style.overflow = 'hidden'; }
}

function closeModal(id) {
  const m = document.getElementById(id);
  if (m) {
    m.classList.remove('open');
    // Only restore scroll if no other modals open
    if (!document.querySelector('.modal-backdrop.open')) {
      document.body.style.overflow = '';
    }
  }
}

// Global Escape key
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    document.querySelectorAll('.modal-backdrop.open').forEach(m => closeModal(m.id));
    closeAllDropdowns('__none__');
    closeMobileDrawer();
  }
});

// Close on backdrop click
document.querySelectorAll('.modal-backdrop').forEach(m => {
  m.addEventListener('click', (e) => {
    if (e.target === m) closeModal(m.id);
  });
});

/* ══════════════════════════════════════════════════
   TOAST NOTIFICATIONS
══════════════════════════════════════════════════ */
function showToast(message, type = 'info', duration = 4000) {
  if (!toastStack) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.setAttribute('role', 'alert');
  toast.setAttribute('aria-live', 'polite');

  const icons = { success: 'check-circle-2', error: 'alert-triangle', info: 'info', warning: 'alert-circle' };
  const iconColor = { success: 'var(--accent-green)', error: 'var(--accent-rose)', info: 'var(--accent-blue)', warning: 'var(--accent-amber)' };

  toast.innerHTML = `
    <i data-lucide="${icons[type] || 'info'}" style="width:17px;height:17px;color:${iconColor[type] || 'var(--accent-blue)'};flex-shrink:0;"></i>
    <span>${escapeHtml(message)}</span>
  `;

  toastStack.appendChild(toast);
  lucide.createIcons();

  // Auto-dismiss
  const fadeOut = () => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(50px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 320);
  };

  setTimeout(fadeOut, duration);

  // Click to dismiss early
  toast.addEventListener('click', fadeOut);
}

/* ══════════════════════════════════════════════════
   SERVER HEALTH
══════════════════════════════════════════════════ */
async function checkHealth() {
  const t0 = performance.now();
  try {
    const res  = await fetch(`${API}/health?t=${Date.now()}`);
    const ms   = Math.round(performance.now() - t0);
    const json = await res.json();

    if (json.success && json.data) {
      const d = json.data;
      if (statusDot)  { statusDot.className = 'status-dot'; statusDot.setAttribute('aria-label', 'Server online'); }
      if (statusText) statusText.textContent = `Online · ${ms}ms`;
      if (dbBadge)    dbBadge.textContent    = `${d.database || 'H2'} DB`;

      if (healthDot)    { healthDot.className = 'status-dot'; }
      if (healthStatus) healthStatus.textContent = 'Server Status: UP (Operational)';
      if (healthDb)     healthDb.textContent     = `${d.database || 'H2'} (${d.databaseStatus || 'Connected'})`;
      if (healthPing)   healthPing.textContent   = `${ms} ms`;
      if (healthProfile) healthProfile.textContent = d.activeProfile || 'dev';

      const sec = d.uptimeSeconds || 0;
      const h   = Math.floor(sec / 3600);
      const m   = Math.floor((sec % 3600) / 60);
      const s   = sec % 60;
      if (healthUptime) healthUptime.textContent = `${h}h ${m}m ${s}s`;

      if (d.memory) {
        const used  = d.memory.usedMb  || 0;
        const total = d.memory.totalMb || 1;
        const max   = d.memory.maxMb   || total;
        if (healthMemText) healthMemText.textContent = `${used} MB / ${total} MB (Max: ${max} MB)`;
        if (healthMemBar)  healthMemBar.style.width  = `${Math.min(100, Math.round((used / total) * 100))}%`;
      }

      // Update settings db field
      const settingsDb = document.getElementById('settings-db');
      if (settingsDb && d.database) settingsDb.value = `${d.database} (${d.activeProfile || 'dev'})`;
    } else {
      setHealthOffline();
    }
  } catch {
    setHealthOffline();
  }
}

function setHealthOffline() {
  if (statusDot)  { statusDot.className = 'status-dot offline'; statusDot.setAttribute('aria-label', 'Server offline'); }
  if (statusText) statusText.textContent = 'Offline';
  if (healthDot)    healthDot.className  = 'status-dot offline';
  if (healthStatus) healthStatus.textContent = 'Server Status: DOWN';
}

/* ══════════════════════════════════════════════════
   DATE DEFAULTS
══════════════════════════════════════════════════ */
function setupDateDefaults() {
  if (!startInput || !endInput) return;
  const today     = new Date();
  const tomorrow  = new Date(today); tomorrow.setDate(today.getDate() + 1);
  const dayAfter  = new Date(today); dayAfter.setDate(today.getDate() + 3);
  const fmt = d => d.toISOString().split('T')[0];
  startInput.min = fmt(today);
  endInput.min   = fmt(today);
  startInput.value = fmt(tomorrow);
  endInput.value   = fmt(dayAfter);
  calcDuration();
}

/* ══════════════════════════════════════════════════
   DURATION CALCULATOR & QUOTA SIMULATOR
══════════════════════════════════════════════════ */
function calcDuration() {
  if (!startInput || !endInput || !durationBanner) return;

  const startStr = startInput.value;
  const endStr   = endInput.value;

  // Reset border
  durationBanner.className = 'alert-banner';

  if (!startStr || !endStr) {
    durationBanner.innerHTML = `<i data-lucide="info"></i><span>Select start and end dates to calculate duration</span>`;
    lucide.createIcons();
    return;
  }

  const start = new Date(startStr);
  const end   = new Date(endStr);

  if (start > end) {
    durationBanner.className = 'alert-banner error';
    durationBanner.innerHTML = `<i data-lucide="alert-triangle"></i><span>End date must be after start date</span>`;
    lucide.createIcons();
    return;
  }

  const days        = Math.floor((end - start) / 86400000) + 1;
  const leaveType   = document.querySelector('input[name="leaveType"]:checked')?.value || 'CASUAL';
  const emp         = employees.find(e => e.id == currentEmployeeId);

  let balanceMax = { CASUAL: 12, SICK: 10, ANNUAL: 15, EMERGENCY: 99 };
  let available  = balanceMax[leaveType];
  if (emp) {
    if (leaveType === 'CASUAL')   available = emp.casualLeaveBalance  ?? 12;
    if (leaveType === 'SICK')     available = emp.sickLeaveBalance    ?? 10;
    if (leaveType === 'ANNUAL')   available = emp.annualLeaveBalance  ?? 15;
    if (leaveType === 'EMERGENCY') available = 999;
  }

  const exceeded = leaveType !== 'EMERGENCY' && days > available;

  if (exceeded) {
    durationBanner.className = 'alert-banner warning';
    durationBanner.innerHTML = `<i data-lucide="alert-triangle"></i><span>Requested <strong>${days} day(s)</strong> exceeds your ${leaveType} balance (${available} days available)</span>`;
  } else {
    durationBanner.className = 'alert-banner info';
    durationBanner.innerHTML = `<i data-lucide="calendar"></i><span>Duration: <strong>${days} day(s)</strong> of ${leaveType} leave</span>`;
  }

  updateQuotaSim(days, leaveType);
  lucide.createIcons();
}

function updateQuotaSim(days, type) {
  if (!currentEmployeeId) return;
  const emp = employees.find(e => e.id == currentEmployeeId);
  if (!emp) return;

  const cas = emp.casualLeaveBalance  ?? 12;
  const sik = emp.sickLeaveBalance    ?? 10;
  const ann = emp.annualLeaveBalance  ?? 15;

  const upChip = (chip, prog, foot, bal, max, isActive, reqDays) => {
    if (isActive) {
      const rem = Math.max(0, bal - reqDays);
      if (prog) prog.style.width = `${Math.min(100, Math.round((rem / max) * 100))}%`;
      if (foot) foot.innerHTML   = `After request: <strong>${rem}</strong> days left`;
      chip?.classList.add('active');
    } else {
      if (prog) prog.style.width = `${Math.min(100, Math.round((bal / max) * 100))}%`;
      if (foot) foot.textContent = `${bal} days remaining`;
      chip?.classList.remove('active');
    }
  };

  upChip(chipCasual, progCasual, footCasual, cas, 12, type === 'CASUAL',   days);
  upChip(chipSick,   progSick,   footSick,   sik, 10, type === 'SICK',     days);
  upChip(chipAnnual, progAnnual, footAnnual, ann, 15, type === 'ANNUAL',   days);
}

/* ══════════════════════════════════════════════════
   EVENT LISTENERS
══════════════════════════════════════════════════ */
function setupEventListeners() {
  // Tab navigation already wired above

  // Theme
  resetDemoBtn?.addEventListener('click', handleResetDemo);

  // Leave form listeners
  startInput?.addEventListener('change', calcDuration);
  endInput?.addEventListener('change',   calcDuration);
  document.querySelectorAll('input[name="leaveType"]').forEach(r => r.addEventListener('change', calcDuration));

  reasonInput?.addEventListener('input', () => {
    if (reasonCounter) reasonCounter.textContent = reasonInput.value.length;
  });

  empSelect?.addEventListener('change', (e) => {
    currentEmployeeId = e.target.value || null;
    if (currentEmployeeId) {
      const emp = employees.find(e => e.id == currentEmployeeId);
      if (emp) {
        if (balanceSection)  balanceSection.style.display = '';
        if (balCasual)  balCasual.textContent  = emp.casualLeaveBalance  ?? 12;
        if (balSick)    balSick.textContent    = emp.sickLeaveBalance    ?? 10;
        if (balAnnual)  balAnnual.textContent  = emp.annualLeaveBalance  ?? 15;
        calcDuration();
      }
      loadEmployeeHistory(currentEmployeeId);
    } else {
      if (balanceSection) balanceSection.style.display = 'none';
      resetHistoryView();
    }
  });

  // Leave apply form submit
  document.getElementById('leave-apply-form')?.addEventListener('submit', promptApplyConfirm);
  document.getElementById('btn-confirm-submit')?.addEventListener('click', handleLeaveSubmit);

  // Filter tabs for Approvals
  document.querySelectorAll('.filter-tab').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.filter-tab').forEach(b => {
        b.classList.remove('active');
        b.setAttribute('aria-selected', 'false');
      });
      btn.classList.add('active');
      btn.setAttribute('aria-selected', 'true');
      currentFilter = btn.getAttribute('data-status') || 'ALL';
      renderApprovalsTable();
    });
  });

  // Approvals search
  document.getElementById('approvals-search')?.addEventListener('input', renderApprovalsTable);

  // My Leaves filters
  ['myleaves-search', 'myleaves-status', 'myleaves-type', 'myleaves-sort'].forEach(id => {
    document.getElementById(id)?.addEventListener('input', renderMyLeavesTable);
    document.getElementById(id)?.addEventListener('change', renderMyLeavesTable);
  });

  // Refresh buttons
  document.getElementById('btn-refresh-leaves')?.addEventListener('click', () => {
    loadLeaves();
    showToast('Refreshing records…', 'info');
  });
  document.getElementById('btn-refresh-analytics')?.addEventListener('click', () => {
    loadAnalytics();
    showToast('Analytics refreshed', 'success');
  });

  // Notifications
  markAllReadBtn?.addEventListener('click', handleMarkAllRead);
  notifMarkAllBtn?.addEventListener('click', handleMarkAllRead);

  // Employee directory
  document.getElementById('emp-search')?.addEventListener('input', filterEmployees);
  document.getElementById('emp-dept-filter')?.addEventListener('change', filterEmployees);
  document.getElementById('btn-add-employee')?.addEventListener('click', openEmployeeModal);

  // Employee modal form
  document.getElementById('emp-form')?.addEventListener('submit', handleSaveEmployee);

  // Delete confirm
  document.getElementById('btn-confirm-delete')?.addEventListener('click', handleDeleteEmployee);

  // Remarks form
  document.getElementById('remarks-form')?.addEventListener('submit', handleDecision);
  document.getElementById('remarks-text')?.addEventListener('input', (e) => {
    const el = document.getElementById('remarks-char');
    if (el) el.textContent = e.target.value.length;
  });

  // Calendar nav
  calPrev?.addEventListener('click', () => { currentCalDate.setMonth(currentCalDate.getMonth() - 1); renderCalendar(); });
  calNext?.addEventListener('click', () => { currentCalDate.setMonth(currentCalDate.getMonth() + 1); renderCalendar(); });
  calToday?.addEventListener('click', () => { currentCalDate = new Date(); renderCalendar(); });
}

/* ══════════════════════════════════════════════════
   NOTIFICATIONS
══════════════════════════════════════════════════ */
async function handleMarkAllRead() {
  try {
    await fetch(`${API}/notifications/mark-all-read`, { method: 'POST' });
    await loadNotifications();
    showToast('All notifications marked as read', 'success');
  } catch {
    showToast('Failed to update notifications', 'error');
  }
}

/* ══════════════════════════════════════════════════
   API LOADERS
══════════════════════════════════════════════════ */
async function loadEmployees() {
  try {
    const res  = await fetch(`${API}/employees`);
    const json = await res.json();
    if (json.success) {
      employees = json.data || [];
      populateEmpSelect();
      renderEmployeeGrid(employees);
      populateDeptFilter();
      updateStats();
    }
  } catch (e) {
    console.error('[LeaveFlow] Failed to load employees', e);
    showToast('Could not load employees', 'error');
  }
}

async function loadLeaves() {
  try {
    const res  = await fetch(`${API}/leaves`);
    const json = await res.json();
    if (json.success) {
      leaves = json.data || [];
      renderApprovalsTable();
      renderMyLeavesTable();
      renderOverviewPending();
      renderOverviewUpcoming();
      updateStats();
      renderCalendar();
      renderCharts();
    }
  } catch (e) {
    console.error('[LeaveFlow] Failed to load leaves', e);
    showToast('Could not load leave records', 'error');
  }
}

async function loadNotifications() {
  try {
    const res  = await fetch(`${API}/notifications`);
    const json = await res.json();
    if (json.success) {
      notifications = json.data || [];
      renderNotifBell();
      renderNotifDropdown();
      renderNotifStream();
    }
  } catch (e) {
    console.error('[LeaveFlow] Failed to load notifications', e);
  }
}

async function loadAnalytics() {
  try {
    const res  = await fetch(`${API}/leaves/analytics`);
    const json = await res.json();
    if (json.success && json.data) {
      const d = json.data;
      setText('analytics-rate',     `${d.approvalRate || 0}%`);
      setText('analytics-approved', d.approvedRequests || 0);
      setText('analytics-rejected', d.rejectedRequests || 0);
      setText('analytics-pending',  d.pendingRequests  || 0);
    }
  } catch (e) {
    console.error('[LeaveFlow] Failed to load analytics', e);
  }
}

/* ══════════════════════════════════════════════════
   STATS
══════════════════════════════════════════════════ */
function updateStats() {
  const pending  = leaves.filter(l => l.status === 'PENDING').length;
  const approved = leaves.filter(l => l.status === 'APPROVED').length;
  const rejected = leaves.filter(l => l.status === 'REJECTED').length;

  setText('count-employees',     employees.length);
  setText('count-total-leaves',  leaves.length);
  setText('count-pending-leaves', pending);
  setText('count-approved-leaves', approved);
  setText('count-rejected-leaves', rejected);

  if (tabPendingBadge) {
    tabPendingBadge.textContent = pending;
    tabPendingBadge.style.display = pending > 0 ? 'inline-flex' : 'none';
  }

  setText('filter-all-count',      leaves.length);
  setText('filter-pending-count',  pending);
  setText('filter-approved-count', approved);
  setText('filter-rejected-count', rejected);
}

/* ══════════════════════════════════════════════════
   DASHBOARD OVERVIEW
══════════════════════════════════════════════════ */
function renderOverviewPending() {
  const tbody = document.getElementById('overview-pending-tbody');
  if (!tbody) return;

  const pending = leaves.filter(l => l.status === 'PENDING');
  if (!pending.length) {
    tbody.innerHTML = `<tr class="table-empty-row"><td colspan="4" style="padding:24px;text-align:center;color:var(--text-muted);font-size:0.84rem;">✅ No pending reviews — all caught up!</td></tr>`;
    return;
  }

  tbody.innerHTML = pending.slice(0, 6).map(l => `
    <tr>
      <td><strong style="color:var(--text-primary);">${escapeHtml(l.employeeName)}</strong></td>
      <td><span class="badge badge-${(l.leaveType||'casual').toLowerCase()}">${l.leaveType||'CASUAL'}</span></td>
      <td class="col-number"><strong>${l.durationDays||1}d</strong></td>
      <td><span class="badge badge-pending">Pending</span></td>
    </tr>
  `).join('');
}

function renderOverviewUpcoming() {
  const container = document.getElementById('overview-upcoming-list');
  if (!container) return;

  const approved = leaves.filter(l => l.status === 'APPROVED');
  if (!approved.length) {
    container.innerHTML = `<div class="empty-state" style="padding:20px 0;"><i data-lucide="calendar"></i><p>No upcoming approved leaves.</p></div>`;
    lucide.createIcons();
    return;
  }

  container.innerHTML = approved.slice(0, 5).map(l => `
    <div style="display:flex;align-items:center;justify-content:space-between;padding:10px 14px;background:var(--bg-app);border-radius:var(--radius-md);border:1px solid var(--border-subtle);">
      <div>
        <strong style="font-size:0.88rem;color:var(--text-primary);">${escapeHtml(l.employeeName)}</strong>
        <div style="font-size:0.74rem;color:var(--text-muted);margin-top:2px;font-family:var(--font-mono);">${l.startDate} → ${l.endDate}</div>
      </div>
      <span class="badge badge-${(l.leaveType||'casual').toLowerCase()}">${l.leaveType||'CASUAL'}</span>
    </div>
  `).join('');
  lucide.createIcons();
}

/* ══════════════════════════════════════════════════
   MY LEAVES TABLE
══════════════════════════════════════════════════ */
function renderMyLeavesTable() {
  const tbody   = document.getElementById('myleaves-tbody');
  if (!tbody) return;

  const search  = (document.getElementById('myleaves-search')?.value  || '').toLowerCase().trim();
  const status  = document.getElementById('myleaves-status')?.value   || 'ALL';
  const type    = document.getElementById('myleaves-type')?.value     || 'ALL';
  const sort    = document.getElementById('myleaves-sort')?.value     || 'NEWEST';

  let filtered = [...leaves];
  if (status !== 'ALL') filtered = filtered.filter(l => l.status    === status);
  if (type   !== 'ALL') filtered = filtered.filter(l => l.leaveType === type);
  if (search)           filtered = filtered.filter(l =>
    (l.reason        || '').toLowerCase().includes(search) ||
    (l.employeeName  || '').toLowerCase().includes(search) ||
    (l.leaveType     || '').toLowerCase().includes(search)
  );

  if (sort === 'OLDEST') filtered.sort((a,b) => a.id - b.id);
  else if (sort === 'DAYS') filtered.sort((a,b) => (b.durationDays||0) - (a.durationDays||0));
  else filtered.sort((a,b) => b.id - a.id);

  if (!filtered.length) {
    tbody.innerHTML = `<tr class="table-empty-row"><td colspan="10"><div class="empty-state" style="padding:32px 0;"><i data-lucide="calendar-x"></i><h4>No Results</h4><p>No leave requests match your filters.</p></div></td></tr>`;
    lucide.createIcons();
    return;
  }

  tbody.innerHTML = filtered.map(l => `
    <tr>
      <td class="col-id">#${l.id}</td>
      <td><strong style="color:var(--text-primary);">${escapeHtml(l.employeeName)}</strong></td>
      <td><span class="badge badge-${(l.leaveType||'casual').toLowerCase()}">${l.leaveType}</span></td>
      <td style="font-family:var(--font-mono);font-size:0.82rem;">${l.startDate}</td>
      <td style="font-family:var(--font-mono);font-size:0.82rem;">${l.endDate}</td>
      <td class="col-number"><strong>${l.durationDays||1}</strong></td>
      <td><span title="${escapeHtml(l.reason)}">${escapeHtml(truncate(l.reason, 30))}</span></td>
      <td>${statusBadge(l.status)}</td>
      <td>${l.managerRemarks ? `<span title="${escapeHtml(l.managerRemarks)}" style="color:var(--accent-amber);font-size:0.8rem;">${escapeHtml(truncate(l.managerRemarks, 25))}</span>` : '<span style="color:var(--text-muted);">—</span>'}</td>
      <td class="col-actions"><button class="btn btn-outline btn-sm" onclick="openLeaveDetails(${l.id})">Details</button></td>
    </tr>
  `).join('');
  lucide.createIcons();
}

/* ══════════════════════════════════════════════════
   APPROVALS TABLE
══════════════════════════════════════════════════ */
function renderApprovalsTable() {
  const tbody  = document.getElementById('approvals-tbody');
  if (!tbody) return;

  const q = (document.getElementById('approvals-search')?.value || '').toLowerCase().trim();
  let filtered = [...leaves];

  if (currentFilter !== 'ALL') filtered = filtered.filter(l => l.status === currentFilter);
  if (q) filtered = filtered.filter(l =>
    (l.employeeName       || '').toLowerCase().includes(q) ||
    (l.employeeDepartment || '').toLowerCase().includes(q) ||
    (l.reason             || '').toLowerCase().includes(q) ||
    (l.leaveType          || '').toLowerCase().includes(q)
  );

  if (!filtered.length) {
    tbody.innerHTML = `<tr class="table-empty-row"><td colspan="11"><div class="empty-state" style="padding:32px 0;"><i data-lucide="calendar-x"></i><h4>No Requests Found</h4><p>Try adjusting your search or filter.</p></div></td></tr>`;
    lucide.createIcons();
    return;
  }

  tbody.innerHTML = filtered.map(l => {
    const isPending = l.status === 'PENDING';
    const applied   = l.appliedDate ? new Date(l.appliedDate).toLocaleDateString('en-IN') : 'N/A';
    const actions   = isPending
      ? `<div class="action-group">
           <button class="btn-approve" onclick="openDecisionModal(${l.id},'APPROVED')" title="Approve leave request">Approve</button>
           <button class="btn-reject"  onclick="openDecisionModal(${l.id},'REJECTED')" title="Reject leave request">Reject</button>
         </div>`
      : `<span style="font-size:0.76rem;color:var(--text-muted);font-weight:600;">Resolved</span>`;

    return `
      <tr>
        <td class="col-id">#${l.id}</td>
        <td>
          <div style="font-weight:700;color:var(--text-primary);">${escapeHtml(l.employeeName||'—')}</div>
          <div style="font-size:0.72rem;color:var(--text-muted);">${escapeHtml(l.employeeEmail||'')}</div>
        </td>
        <td style="color:var(--text-secondary);">${escapeHtml(l.employeeDepartment||'General')}</td>
        <td><span class="badge badge-${(l.leaveType||'casual').toLowerCase()}">${l.leaveType||'CASUAL'}</span></td>
        <td style="font-family:var(--font-mono);font-size:0.8rem;">${l.startDate} → ${l.endDate}</td>
        <td class="col-number"><strong>${l.durationDays||1}</strong></td>
        <td><span title="${escapeHtml(l.reason)}">${escapeHtml(truncate(l.reason, 28))}</span></td>
        <td>${l.managerRemarks ? `<span title="${escapeHtml(l.managerRemarks)}" style="color:var(--accent-amber);font-size:0.79rem;">${escapeHtml(truncate(l.managerRemarks, 22))}</span>` : '<span style="color:var(--text-muted);">—</span>'}</td>
        <td style="font-size:0.8rem;color:var(--text-muted);">${applied}</td>
        <td>${statusBadge(l.status)}</td>
        <td class="col-actions">${actions}</td>
      </tr>
    `;
  }).join('');
  lucide.createIcons();
}

/* ══════════════════════════════════════════════════
   LEAVE DETAILS MODAL
══════════════════════════════════════════════════ */
function openLeaveDetails(id) {
  const l = leaves.find(l => l.id === id);
  if (!l) return;

  const stepper = document.getElementById('details-stepper');
  const content = document.getElementById('details-content');

  const isApproved = l.status === 'APPROVED';
  const isRejected = l.status === 'REJECTED';

  if (stepper) {
    stepper.innerHTML = `
      <div class="stepper-step done">
        <div class="step-node"><i data-lucide="send" style="width:12px;height:12px;"></i></div>
        <span class="step-label">Submitted</span>
      </div>
      <div class="stepper-line done"></div>
      <div class="stepper-step done">
        <div class="step-node"><i data-lucide="eye" style="width:12px;height:12px;"></i></div>
        <span class="step-label">Under Review</span>
      </div>
      <div class="stepper-line ${isApproved || isRejected ? 'done' : ''}"></div>
      <div class="stepper-step ${isApproved ? 'approved' : isRejected ? 'rejected' : ''}">
        <div class="step-node">${isApproved ? '<i data-lucide="check" style="width:12px;height:12px;"></i>' : isRejected ? '<i data-lucide="x" style="width:12px;height:12px;"></i>' : '3'}</div>
        <span class="step-label">${isApproved ? 'Approved' : isRejected ? 'Rejected' : 'Pending'}</span>
      </div>
    `;
  }

  if (content) {
    content.innerHTML = `
      <div class="sub-panel">
        <div style="font-weight:800;font-size:1rem;color:var(--text-primary);">${escapeHtml(l.employeeName)}</div>
        <div style="font-size:0.78rem;color:var(--text-muted);margin-top:3px;">${escapeHtml(l.employeeEmail)} &bull; ${escapeHtml(l.employeeDepartment)}</div>
      </div>
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;font-size:0.86rem;">
        <div><span style="color:var(--text-muted);font-weight:600;">Leave Type</span><div style="font-weight:700;margin-top:2px;">${l.leaveType||'CASUAL'}</div></div>
        <div><span style="color:var(--text-muted);font-weight:600;">Duration</span><div style="font-weight:700;margin-top:2px;">${l.durationDays||1} day(s)</div></div>
        <div><span style="color:var(--text-muted);font-weight:600;">Start Date</span><div style="font-weight:700;font-family:var(--font-mono);font-size:0.84rem;margin-top:2px;">${l.startDate}</div></div>
        <div><span style="color:var(--text-muted);font-weight:600;">End Date</span><div style="font-weight:700;font-family:var(--font-mono);font-size:0.84rem;margin-top:2px;">${l.endDate}</div></div>
      </div>
      <div style="padding:12px 14px;background:var(--bg-input);border-left:3px solid var(--accent-indigo);border-radius:var(--radius-md);">
        <span style="font-size:0.72rem;text-transform:uppercase;font-weight:700;color:var(--text-muted);">Reason</span>
        <div style="margin-top:4px;font-size:0.88rem;color:var(--text-primary);">${escapeHtml(l.reason)}</div>
      </div>
      ${l.managerRemarks ? `
        <div style="padding:12px 14px;background:var(--accent-amber-subtle);border-left:3px solid var(--accent-amber);border-radius:var(--radius-md);">
          <span style="font-size:0.72rem;text-transform:uppercase;font-weight:700;color:var(--accent-amber);">Manager Note</span>
          <div style="margin-top:4px;font-size:0.88rem;color:var(--text-primary);">${escapeHtml(l.managerRemarks)}</div>
        </div>
      ` : ''}
    `;
  }

  openModal('modal-leave-details');
  lucide.createIcons();
}

/* ══════════════════════════════════════════════════
   APPLY LEAVE — Confirmation & Submit
══════════════════════════════════════════════════ */
function promptApplyConfirm(e) {
  e.preventDefault();
  const empId = empSelect?.value;
  if (!empId) { showToast('Please select an employee profile', 'error'); return; }

  const emp      = employees.find(e => e.id == empId);
  const leaveTp  = document.querySelector('input[name="leaveType"]:checked')?.value || 'CASUAL';
  const startStr = startInput?.value;
  const endStr   = endInput?.value;
  const reason   = reasonInput?.value.trim();

  if (!reason) { showToast('Please enter a reason for leave', 'error'); reasonInput?.focus(); return; }
  if (!startStr || !endStr) { showToast('Please select valid dates', 'error'); return; }

  const start = new Date(startStr);
  const end   = new Date(endStr);
  if (start > end) { showToast('End date must be after start date', 'error'); return; }

  const days = Math.floor((end - start) / 86400000) + 1;

  const body = document.getElementById('apply-confirm-body');
  if (body) {
    body.innerHTML = `
      <div class="sub-panel" style="display:flex;flex-direction:column;gap:6px;">
        <div style="display:grid;grid-template-columns:120px 1fr;gap:8px;font-size:0.88rem;">
          <span style="color:var(--text-muted);font-weight:600;">Employee</span>
          <span style="color:var(--text-primary);font-weight:700;">${escapeHtml(emp?.name||'Selected Employee')} <span style="color:var(--text-muted);font-weight:500;">(${escapeHtml(emp?.department||'')})</span></span>
          <span style="color:var(--text-muted);font-weight:600;">Leave Type</span>
          <span>${leaveTp}</span>
          <span style="color:var(--text-muted);font-weight:600;">Period</span>
          <span style="font-family:var(--font-mono);font-size:0.84rem;">${startStr} → ${endStr} (<strong>${days} day(s)</strong>)</span>
          <span style="color:var(--text-muted);font-weight:600;">Reason</span>
          <span style="font-style:italic;">"${escapeHtml(truncate(reason, 80))}"</span>
        </div>
      </div>
      <div class="alert-banner info" style="margin-top:4px;">
        <i data-lucide="info"></i>
        <span>Submitting will deduct leave from the employee's balance and send an email notification.</span>
      </div>
    `;
  }

  openModal('modal-apply-confirm');
  lucide.createIcons();
}

async function handleLeaveSubmit() {
  closeModal('modal-apply-confirm');

  const submitBtn = document.getElementById('btn-submit-leave');
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.innerHTML = `<div class="spinner sm"></div><span>Submitting…</span>`;
  }

  const payload = {
    employeeId: parseInt(empSelect?.value),
    leaveType:  document.querySelector('input[name="leaveType"]:checked')?.value || 'CASUAL',
    startDate:  startInput?.value,
    endDate:    endInput?.value,
    reason:     reasonInput?.value.trim(),
  };

  try {
    const res  = await fetch(`${API}/leaves`, {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify(payload),
    });
    const json = await res.json();

    if (json.success) {
      triggerConfetti();
      showToast('Leave request submitted successfully!', 'success');
      if (reasonInput)   reasonInput.value = '';
      if (reasonCounter) reasonCounter.textContent = '0';
      await Promise.all([loadLeaves(), loadEmployees(), loadNotifications()]);
      switchTab('admin-tab');
    } else {
      showToast(json.message || 'Failed to submit leave request', 'error');
    }
  } catch {
    showToast('Server error — could not submit request', 'error');
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = `<i data-lucide="send"></i><span>Submit Leave Request</span>`;
      lucide.createIcons();
    }
  }
}

/* ══════════════════════════════════════════════════
   DECISIONS — Approve / Reject
══════════════════════════════════════════════════ */
function openDecisionModal(leaveId, targetStatus) {
  const l = leaves.find(l => l.id === leaveId);
  if (!l) return;

  document.getElementById('remarks-leave-id').value     = leaveId;
  document.getElementById('remarks-target-status').value = targetStatus;
  document.getElementById('remarks-text').value          = '';
  document.getElementById('remarks-char').textContent    = '0';

  const isApprove = targetStatus === 'APPROVED';
  const modalTitle = document.getElementById('modal-remarks-title');
  if (modalTitle) modalTitle.textContent = isApprove ? `Approve Request #${leaveId}` : `Reject Request #${leaveId}`;

  const btnLabel = document.getElementById('remarks-btn-label');
  if (btnLabel) btnLabel.textContent = isApprove ? 'Confirm Approval' : 'Confirm Rejection';

  const confirmBtn = document.getElementById('btn-confirm-decision');
  if (confirmBtn) {
    confirmBtn.className = `btn ${isApprove ? 'btn-success' : 'btn-danger'}`;
    confirmBtn.querySelector('i')?.setAttribute('data-lucide', isApprove ? 'check-circle-2' : 'x-circle');
  }

  const preview = document.getElementById('remarks-preview');
  if (preview) {
    preview.innerHTML = `
      <div style="font-weight:800;color:var(--text-primary);margin-bottom:4px;">${escapeHtml(l.employeeName)} <span style="color:var(--text-muted);font-weight:500;">(${escapeHtml(l.employeeDepartment||'')})</span></div>
      <div style="color:var(--text-secondary);font-size:0.82rem;">${l.leaveType||'CASUAL'} Leave &bull; ${l.startDate} → ${l.endDate} (${l.durationDays||1} days)</div>
      <div style="margin-top:6px;font-style:italic;color:var(--text-muted);font-size:0.82rem;">"${escapeHtml(l.reason)}"</div>
    `;
  }

  openModal('modal-remarks');
  lucide.createIcons();
}

async function handleDecision(e) {
  e.preventDefault();
  const leaveId = document.getElementById('remarks-leave-id')?.value;
  const status  = document.getElementById('remarks-target-status')?.value;
  const remarks = document.getElementById('remarks-text')?.value.trim() || '';

  try {
    const url = `${API}/leaves/${leaveId}/status?status=${status}&remarks=${encodeURIComponent(remarks)}`;
    const res  = await fetch(url, { method: 'PATCH' });
    const json = await res.json();

    if (json.success) {
      closeModal('modal-remarks');
      if (status === 'APPROVED') triggerConfetti();
      showToast(`Leave #${leaveId} ${status.toLowerCase()} successfully!`, 'success');
      await Promise.all([loadLeaves(), loadEmployees(), loadNotifications(), loadAnalytics()]);
    } else {
      showToast(json.message || 'Failed to update leave status', 'error');
    }
  } catch {
    showToast('Error communicating with server', 'error');
  }
}

/* ══════════════════════════════════════════════════
   EMPLOYEE MANAGEMENT
══════════════════════════════════════════════════ */
function populateEmpSelect() {
  if (!empSelect) return;
  const prev = empSelect.value;
  empSelect.innerHTML = `<option value="">— Select employee profile —</option>` +
    employees.map(e => `<option value="${e.id}">${escapeHtml(e.name)} (${escapeHtml(e.department)})</option>`).join('');
  if (prev) empSelect.value = prev;
}

function populateDeptFilter() {
  const filter = document.getElementById('emp-dept-filter');
  if (!filter) return;
  const depts = [...new Set(employees.map(e => e.department))].filter(Boolean).sort();
  filter.innerHTML = `<option value="ALL">All Departments</option>` +
    depts.map(d => `<option value="${escapeHtml(d)}">${escapeHtml(d)}</option>`).join('');
}

function filterEmployees() {
  const q    = (document.getElementById('emp-search')?.value       || '').toLowerCase().trim();
  const dept = (document.getElementById('emp-dept-filter')?.value  || 'ALL');

  const filtered = employees.filter(e => {
    const matchQ    = !q || e.name.toLowerCase().includes(q) || e.email.toLowerCase().includes(q) || e.department.toLowerCase().includes(q);
    const matchDept = dept === 'ALL' || e.department === dept;
    return matchQ && matchDept;
  });

  renderEmployeeGrid(filtered);
}

function renderEmployeeGrid(list) {
  const grid = document.getElementById('emp-grid');
  if (!grid) return;

  if (!list.length) {
    grid.innerHTML = `
      <div class="empty-state" style="grid-column:1/-1;">
        <i data-lucide="users"></i>
        <h4>No Employees Found</h4>
        <p>Try adjusting the search or add a new employee.</p>
      </div>
    `;
    lucide.createIcons();
    return;
  }

  // Gradient options per employee (cycles through)
  const gradients = [
    'linear-gradient(135deg,#38bdf8,#6366f1)',
    'linear-gradient(135deg,#a855f7,#6366f1)',
    'linear-gradient(135deg,#10b981,#38bdf8)',
    'linear-gradient(135deg,#f59e0b,#f43f5e)',
    'linear-gradient(135deg,#6366f1,#a855f7)',
  ];

  grid.innerHTML = list.map((emp, i) => {
    const initials = emp.name.split(' ').map(n => n[0]).join('').substring(0,2).toUpperCase();
    const grad     = gradients[i % gradients.length];
    return `
      <div class="emp-card">
        <div class="emp-card-top">
          <div class="emp-avatar" style="background:${grad};" aria-label="${escapeHtml(emp.name)} avatar">${initials}</div>
          <div class="emp-info">
            <div class="emp-name">${escapeHtml(emp.name)}</div>
            <div class="emp-dept">${escapeHtml(emp.department)} &bull; ${escapeHtml(emp.designation||'Staff')}</div>
          </div>
        </div>
        <div class="emp-email" title="${escapeHtml(emp.email)}">${escapeHtml(emp.email)}</div>
        <div class="emp-quota-grid">
          <div class="quota-item"><span class="quota-label">Casual</span><span class="quota-value" style="color:var(--accent-blue);">${emp.casualLeaveBalance??12}</span></div>
          <div class="quota-item"><span class="quota-label">Sick</span><span class="quota-value" style="color:var(--accent-rose);">${emp.sickLeaveBalance??10}</span></div>
          <div class="quota-item"><span class="quota-label">Annual</span><span class="quota-value" style="color:var(--accent-green);">${emp.annualLeaveBalance??15}</span></div>
        </div>
        <div class="emp-card-footer">
          <button class="btn btn-outline btn-sm" onclick="selectEmpForLeave(${emp.id})" aria-label="Apply leave for ${escapeHtml(emp.name)}">
            <i data-lucide="plane-takeoff"></i> Apply Leave
          </button>
          <button class="btn btn-ghost btn-sm" style="color:var(--accent-rose);" onclick="promptDeleteEmployee(${emp.id},'${escapeHtml(emp.name)}')" aria-label="Delete ${escapeHtml(emp.name)}">
            <i data-lucide="trash-2"></i>
          </button>
        </div>
      </div>
    `;
  }).join('');

  lucide.createIcons();
}

function selectEmpForLeave(empId) {
  switchTab('employee-tab');
  if (empSelect) {
    empSelect.value = empId;
    empSelect.dispatchEvent(new Event('change'));
  }
}

function openEmployeeModal() {
  const form = document.getElementById('emp-form');
  if (form) form.reset();
  const idEl = document.getElementById('emp-id');
  if (idEl) idEl.value = '';
  const title = document.getElementById('modal-emp-title');
  if (title) title.textContent = 'Add New Employee';
  const label = document.getElementById('emp-save-label');
  if (label) label.textContent = 'Save Employee';
  openModal('modal-employee-form');
}

async function handleSaveEmployee(e) {
  e.preventDefault();

  const payload = {
    name:        document.getElementById('emp-name')?.value.trim(),
    email:       document.getElementById('emp-email')?.value.trim(),
    department:  document.getElementById('emp-dept')?.value.trim(),
    designation: document.getElementById('emp-desig')?.value.trim(),
  };

  if (!payload.name || !payload.email || !payload.department || !payload.designation) {
    showToast('Please fill all required fields', 'error');
    return;
  }

  const saveBtn = document.getElementById('btn-save-emp');
  if (saveBtn) { saveBtn.disabled = true; saveBtn.innerHTML = `<div class="spinner sm"></div><span>Saving…</span>`; }

  try {
    const res  = await fetch(`${API}/employees`, {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify(payload),
    });
    const json = await res.json();

    if (json.success) {
      closeModal('modal-employee-form');
      showToast('Employee profile created!', 'success');
      await loadEmployees();
    } else {
      showToast(json.message || 'Failed to save employee', 'error');
    }
  } catch {
    showToast('Server error while saving employee', 'error');
  } finally {
    if (saveBtn) {
      saveBtn.disabled = false;
      saveBtn.innerHTML = `<i data-lucide="check"></i><span id="emp-save-label">Save Employee</span>`;
      lucide.createIcons();
    }
  }
}

function promptDeleteEmployee(empId, name) {
  pendingDeleteId = empId;
  const text = document.getElementById('delete-confirm-text');
  if (text) text.innerHTML = `Are you sure you want to permanently delete the profile for <strong>${escapeHtml(name)}</strong>? All associated leave records will also be removed.`;
  openModal('modal-delete-confirm');
}

async function handleDeleteEmployee() {
  if (!pendingDeleteId) return;
  const id = pendingDeleteId;
  pendingDeleteId = null;
  closeModal('modal-delete-confirm');

  try {
    const res  = await fetch(`${API}/employees/${id}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast('Employee profile deleted', 'success');
      await Promise.all([loadEmployees(), loadLeaves()]);
    } else {
      showToast(json.message || 'Failed to delete employee', 'error');
    }
  } catch {
    showToast('Server error while deleting', 'error');
  }
}

/* ══════════════════════════════════════════════════
   EMPLOYEE LEAVE HISTORY
══════════════════════════════════════════════════ */
async function loadEmployeeHistory(empId) {
  const emp = employees.find(e => e.id == empId);
  if (!emp) return;

  if (historyTitle)  historyTitle.textContent  = `${emp.name}'s Leave History`;
  if (historySub)    historySub.textContent    = `${emp.department} · ${emp.email}`;
  if (historyList)   historyList.innerHTML     = `<div class="empty-state" style="padding:24px;"><div class="spinner"></div><p>Loading history…</p></div>`;

  try {
    const res  = await fetch(`${API}/leaves/employee/${empId}`);
    const json = await res.json();
    if (json.success) renderEmployeeHistory(json.data || []);
  } catch {
    if (historyList) historyList.innerHTML = `<div class="empty-state"><i data-lucide="alert-triangle"></i><p>Failed to load history</p></div>`;
    lucide.createIcons();
  }
}

function renderEmployeeHistory(list) {
  if (!historyList) return;
  if (!list.length) {
    historyList.innerHTML = `<div class="empty-state"><i data-lucide="calendar-off"></i><h4>No Past Requests</h4><p>This employee has no leave records yet.</p></div>`;
    lucide.createIcons();
    return;
  }

  historyList.innerHTML = list.map(item => `
    <div class="timeline-item status-${(item.status||'pending').toLowerCase()}">
      <div class="timeline-item-header">
        <span class="badge badge-${(item.leaveType||'casual').toLowerCase()}">${item.leaveType||'CASUAL'}</span>
        ${statusBadge(item.status)}
      </div>
      <div class="timeline-dates" style="font-family:var(--font-mono);font-size:0.82rem;">${item.startDate} → ${item.endDate} (${item.durationDays||1} days)</div>
      <div class="timeline-reason">${escapeHtml(item.reason)}</div>
      ${item.managerRemarks ? `<div class="timeline-remark">Manager: ${escapeHtml(item.managerRemarks)}</div>` : ''}
    </div>
  `).join('');
  lucide.createIcons();
}

function resetHistoryView() {
  if (historyTitle) historyTitle.textContent = 'Leave History';
  if (historySub)   historySub.textContent   = 'Select an employee to view their past requests.';
  if (historyList)  historyList.innerHTML    = `<div class="empty-state"><i data-lucide="user-check"></i><h4>No Employee Selected</h4><p>Choose an employee profile from the dropdown above.</p></div>`;
  lucide.createIcons();
}

/* ══════════════════════════════════════════════════
   NOTIFICATIONS
══════════════════════════════════════════════════ */
function relativeTime(dateInput) {
  if (!dateInput) return 'Recently';
  const diff = (Date.now() - new Date(dateInput)) / 1000;
  if (diff < 60)    return 'Just now';
  if (diff < 3600)  return `${Math.floor(diff / 60)}m ago`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
  return `${Math.floor(diff / 86400)}d ago`;
}

function renderNotifBell() {
  const unread = notifications.filter(n => !n.read).length;
  if (notifBadge) {
    notifBadge.textContent    = unread;
    notifBadge.style.display  = unread > 0 ? 'flex' : 'none';
  }
}

function renderNotifDropdown() {
  if (!notifList) return;
  if (!notifications.length) {
    notifList.innerHTML = `<div class="empty-state" style="padding:20px;"><i data-lucide="inbox"></i><p>No notifications yet</p></div>`;
    lucide.createIcons();
    return;
  }
  notifList.innerHTML = notifications.slice(0, 8).map(n => `
    <div class="notif-item ${n.read ? '' : 'unread'}" onclick="openEmailModal(${n.id})" role="listitem" tabindex="0"
         onkeydown="if(event.key==='Enter') openEmailModal(${n.id})">
      <div class="notif-item-top">
        <span class="notif-subject">${escapeHtml(n.title)}</span>
        <span class="notif-time">${relativeTime(n.createdAt)}</span>
      </div>
      <div class="notif-body">${escapeHtml(truncate(n.message, 70))}</div>
    </div>
  `).join('');
  lucide.createIcons();
}

function renderNotifStream() {
  if (!notifStream) return;
  if (!notifications.length) {
    notifStream.innerHTML = `<div class="empty-state"><i data-lucide="bell-off"></i><h4>No Notifications</h4><p>Events will appear here after leave actions.</p></div>`;
    lucide.createIcons();
    return;
  }
  notifStream.innerHTML = notifications.map(n => `
    <div class="notif-item ${n.read ? '' : 'unread'}" onclick="openEmailModal(${n.id})" role="listitem" tabindex="0"
         onkeydown="if(event.key==='Enter') openEmailModal(${n.id})"
         style="padding:14px 16px;">
      <div class="notif-item-top">
        <span class="notif-subject" style="font-size:0.88rem;">${escapeHtml(n.title)}</span>
        <span class="notif-time">${relativeTime(n.createdAt)}</span>
      </div>
      <div class="notif-body" style="font-size:0.84rem;margin:4px 0;">${escapeHtml(n.message)}</div>
      <div style="font-size:0.74rem;color:var(--accent-indigo);font-weight:700;">
        <i data-lucide="mail" style="width:12px;height:12px;vertical-align:middle;"></i>
        To: ${escapeHtml(n.recipientName)} &lt;${escapeHtml(n.recipientEmail)}&gt;
      </div>
    </div>
  `).join('');
  lucide.createIcons();
}

function openEmailModal(notifId) {
  const n = notifications.find(n => n.id === notifId);
  if (!n) return;

  const subject = document.getElementById('email-modal-subject');
  const from    = document.getElementById('email-from');
  const to      = document.getElementById('email-to');
  const date    = document.getElementById('email-date');
  const body    = document.getElementById('email-body');

  if (subject) subject.textContent = n.title;
  if (from)    from.textContent    = 'LeaveFlow System <no-reply@leaveflow.org>';
  if (to)      to.textContent      = `${n.recipientName} <${n.recipientEmail}>`;
  if (date)    date.textContent    = n.createdAt ? new Date(n.createdAt).toLocaleString() : new Date().toLocaleString();

  if (body) {
    body.innerHTML = `
      <p style="margin-bottom:10px;">Dear <strong>${escapeHtml(n.recipientName)}</strong>,</p>
      <p style="margin-bottom:16px;">${escapeHtml(n.message)}</p>
      <div style="background:var(--bg-input);border-left:3px solid var(--accent-indigo);padding:10px 12px;border-radius:var(--radius-sm);margin-bottom:14px;">
        <div style="font-size:0.7rem;color:var(--text-muted);text-transform:uppercase;font-weight:700;margin-bottom:3px;">Audit Reference</div>
        <code>EVT-${n.id}-${Date.now().toString(36).toUpperCase()}</code>
      </div>
      <p style="color:var(--text-muted);font-size:0.8rem;">— LeaveFlow Automated System</p>
    `;
  }

  openModal('modal-email');
  lucide.createIcons();

  // Mark as read
  fetch(`${API}/notifications/${notifId}/read`, { method: 'PATCH' })
    .then(() => loadNotifications())
    .catch(() => {});
}

/* ══════════════════════════════════════════════════
   ANALYTICS CHARTS (Native SVG)
══════════════════════════════════════════════════ */
function renderCharts() {
  renderDonutChart();
  renderBarChart();
  renderLineChart();
}

function renderDonutChart() {
  const container = document.getElementById('chart-donut');
  if (!container) return;

  const counts = { CASUAL: 0, SICK: 0, ANNUAL: 0, EMERGENCY: 0 };
  leaves.forEach(l => {
    const t = (l.leaveType || 'CASUAL').toUpperCase();
    if (counts[t] !== undefined) counts[t]++;
  });

  const total  = Object.values(counts).reduce((a, b) => a + b, 0);
  const colors = { CASUAL: '#38bdf8', SICK: '#f43f5e', ANNUAL: '#10b981', EMERGENCY: '#f59e0b' };
  const labels = { CASUAL: 'Casual', SICK: 'Sick', ANNUAL: 'Annual', EMERGENCY: 'Emergency' };

  if (!total) {
    container.innerHTML = `<div class="empty-state" style="padding:24px;"><i data-lucide="pie-chart"></i><p>No data yet</p></div>`;
    lucide.createIcons();
    return;
  }

  let angle = 0;
  let paths = '';

  Object.entries(counts).forEach(([type, count]) => {
    if (!count) return;
    const pct       = count / total;
    const sweep     = pct * 360;
    const x1        = 100 + 75 * Math.cos(((angle - 90) * Math.PI) / 180);
    const y1        = 100 + 75 * Math.sin(((angle - 90) * Math.PI) / 180);
    const x2        = 100 + 75 * Math.cos(((angle + sweep - 90) * Math.PI) / 180);
    const y2        = 100 + 75 * Math.sin(((angle + sweep - 90) * Math.PI) / 180);
    const large     = sweep > 180 ? 1 : 0;

    paths += `
      <path d="M 100 100 L ${x1} ${y1} A 75 75 0 ${large} 1 ${x2} ${y2} Z"
            fill="${colors[type]}" opacity="0.88" style="cursor:pointer;"
            onmouseover="this.style.opacity='1'" onmouseout="this.style.opacity='0.88'">
        <title>${labels[type]}: ${count} (${Math.round(pct*100)}%)</title>
      </path>
    `;
    angle += sweep;
  });

  const legendHtml = Object.entries(counts)
    .filter(([,c]) => c > 0)
    .map(([t, c]) => `<div class="chart-legend-item"><span class="legend-color" style="background:${colors[t]};"></span>${labels[t]} (${c})</div>`)
    .join('');

  container.innerHTML = `
    <svg viewBox="0 0 200 200" width="160" height="160" role="img" aria-label="Leave usage donut chart">
      <title>Leave Usage by Type</title>
      ${paths}
      <circle cx="100" cy="100" r="48" fill="var(--bg-card)"/>
      <text x="100" y="96" text-anchor="middle" fill="var(--text-primary)" font-weight="800" font-size="18" font-family="Plus Jakarta Sans">${total}</text>
      <text x="100" y="112" text-anchor="middle" fill="var(--text-muted)" font-size="10">Leaves</text>
    </svg>
    <div class="chart-legend" style="justify-content:center;">${legendHtml}</div>
  `;
}

function renderBarChart() {
  const container = document.getElementById('chart-bar');
  if (!container) return;

  const deptCounts = {};
  leaves.forEach(l => {
    const d = l.employeeDepartment || 'General';
    deptCounts[d] = (deptCounts[d] || 0) + 1;
  });

  const entries = Object.entries(deptCounts);
  if (!entries.length) {
    container.innerHTML = `<div class="empty-state" style="width:100%;"><i data-lucide="bar-chart-3"></i><p>No data yet</p></div>`;
    lucide.createIcons();
    return;
  }

  const maxVal = Math.max(...Object.values(deptCounts), 1);
  const barColors = ['#6366f1','#38bdf8','#10b981','#f59e0b','#a855f7','#f43f5e'];

  container.innerHTML = entries.map(([dept, count], i) => {
    const h = Math.max(8, Math.round((count / maxVal) * 150));
    return `
      <div class="bar-col" title="${escapeHtml(dept)}: ${count} leave(s)">
        <span class="bar-count">${count}</span>
        <div class="bar-fill" style="height:${h}px;background:${barColors[i % barColors.length]};"></div>
        <span class="bar-label">${escapeHtml(dept)}</span>
      </div>
    `;
  }).join('');
}

function renderLineChart() {
  const container = document.getElementById('chart-line');
  if (!container) return;

  // Build monthly counts from actual leave data
  const monthMap = {};
  leaves.forEach(l => {
    if (!l.startDate) return;
    const d = new Date(l.startDate);
    const key = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}`;
    monthMap[key] = (monthMap[key] || 0) + 1;
  });

  // Last 6 months
  const months = [];
  const now    = new Date();
  for (let i = 5; i >= 0; i--) {
    const d   = new Date(now.getFullYear(), now.getMonth() - i, 1);
    const key = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}`;
    const lbl = d.toLocaleString('default', { month: 'short' });
    months.push({ key, label: lbl, count: monthMap[key] || 0 });
  }

  const maxVal = Math.max(...months.map(m => m.count), 1);
  const W = 220, H = 140, padX = 20, padY = 14;

  const points = months.map((m, i) => {
    const x = padX + i * ((W - padX*2) / (months.length - 1));
    const y = padY + ((maxVal - m.count) / maxVal) * (H - padY*2);
    return { x, y, ...m };
  });

  const polyPts = points.map(p => `${p.x},${p.y}`).join(' ');
  const areaPath = `M ${points[0].x} ${H} ` +
    points.map(p => `L ${p.x} ${p.y}`).join(' ') +
    ` L ${points[points.length-1].x} ${H} Z`;

  container.innerHTML = `
    <svg viewBox="0 0 ${W} ${H}" width="100%" height="${H}" role="img" aria-label="Monthly leave trend chart">
      <title>Monthly Leave Request Trend</title>
      <defs>
        <linearGradient id="lineGrad" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="var(--accent-indigo)" stop-opacity="0.3"/>
          <stop offset="100%" stop-color="var(--accent-indigo)" stop-opacity="0"/>
        </linearGradient>
      </defs>
      <path d="${areaPath}" fill="url(#lineGrad)"/>
      <polyline fill="none" stroke="var(--accent-indigo)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" points="${polyPts}"/>
      ${points.map(p => `
        <circle cx="${p.x}" cy="${p.y}" r="4" fill="var(--accent-blue)" stroke="var(--bg-card)" stroke-width="2">
          <title>${p.label}: ${p.count} request(s)</title>
        </circle>
        <text x="${p.x}" y="${H - 2}" text-anchor="middle" fill="var(--text-muted)" font-size="9">${p.label}</text>
      `).join('')}
    </svg>
  `;
}

/* ══════════════════════════════════════════════════
   TEAM CALENDAR
══════════════════════════════════════════════════ */
function renderCalendar() {
  if (!calGrid || !calMonthTitle) return;

  const year  = currentCalDate.getFullYear();
  const month = currentCalDate.getMonth();
  const MONTHS = ['January','February','March','April','May','June','July','August','September','October','November','December'];

  calMonthTitle.textContent = `${MONTHS[month]} ${year}`;

  const firstDay    = new Date(year, month, 1).getDay();
  const lastDate    = new Date(year, month + 1, 0).getDate();
  const prevLastDt  = new Date(year, month, 0).getDate();
  const today       = new Date();

  let html = '';

  // Previous month filler
  for (let i = firstDay; i > 0; i--) {
    html += `<div class="cal-cell other-month"><span class="cal-day-num">${prevLastDt - i + 1}</span></div>`;
  }

  // Current month days
  for (let day = 1; day <= lastDate; day++) {
    const isToday  = year === today.getFullYear() && month === today.getMonth() && day === today.getDate();
    const dateStr  = `${year}-${String(month+1).padStart(2,'0')}-${String(day).padStart(2,'0')}`;
    const dayLeaves = leaves.filter(l => dateStr >= l.startDate && dateStr <= l.endDate);

    const pills = dayLeaves.slice(0, 3).map(l => {
      const bg    = l.status === 'APPROVED' ? 'var(--accent-green-subtle)' : 'var(--accent-amber-subtle)';
      const color = l.status === 'APPROVED' ? 'var(--accent-green)'        : 'var(--accent-amber)';
      const name  = (l.employeeName || 'User').split(' ')[0];
      return `<div class="cal-pill" style="background:${bg};color:${color};"
                onclick="openLeaveDetails(${l.id})" title="${escapeHtml(l.employeeName)} (${l.leaveType})">${escapeHtml(name)}</div>`;
    }).join('');

    const overflowCount = dayLeaves.length > 3 ? `<div style="font-size:0.62rem;color:var(--text-muted);padding:1px 4px;">+${dayLeaves.length-3} more</div>` : '';

    html += `
      <div class="cal-cell${isToday ? ' today' : ''}">
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <span class="cal-day-num">${day}</span>
          ${isToday ? '<span class="today-tag">Today</span>' : ''}
        </div>
        ${pills}${overflowCount}
      </div>
    `;
  }

  // Next month filler
  const totalCells = firstDay + lastDate;
  const remaining  = (7 - (totalCells % 7)) % 7;
  for (let j = 1; j <= remaining; j++) {
    html += `<div class="cal-cell other-month"><span class="cal-day-num">${j}</span></div>`;
  }

  calGrid.innerHTML = html;
  lucide.createIcons();
}

/* ══════════════════════════════════════════════════
   RESET DEMO
══════════════════════════════════════════════════ */
async function handleResetDemo() {
  const btn = document.getElementById('btn-reset-demo');
  if (btn) { btn.disabled = true; btn.innerHTML = `<div class="spinner sm"></div><span>Resetting…</span>`; }

  try {
    const res  = await fetch(`${API}/health/reset-demo`, { method: 'POST' });
    const json = await res.json();
    if (json.success) {
      triggerConfetti();
      showToast('Demo data reloaded successfully!', 'success');
      await initApp();
    } else {
      showToast('Failed to reset demo data', 'error');
    }
  } catch {
    showToast('Server communication error', 'error');
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = `<i data-lucide="sparkles"></i><span>Reset Demo Data</span>`;
      profileDropdown?.classList.remove('open');
      lucide.createIcons();
    }
  }
}

/* ══════════════════════════════════════════════════
   UTILITY FUNCTIONS
══════════════════════════════════════════════════ */
function statusBadge(status) {
  const map = {
    APPROVED: `<span class="badge badge-approved"><i data-lucide="check" style="width:11px;height:11px;"></i> Approved</span>`,
    REJECTED: `<span class="badge badge-rejected"><i data-lucide="x"     style="width:11px;height:11px;"></i> Rejected</span>`,
    PENDING:  `<span class="badge badge-pending"><i  data-lucide="clock"  style="width:11px;height:11px;"></i> Pending</span>`,
  };
  return map[status] || `<span class="badge badge-pending">Pending</span>`;
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function truncate(str, len) {
  if (!str) return '';
  return str.length > len ? str.substring(0, len) + '…' : str;
}

function setText(id, val) {
  const el = document.getElementById(id);
  if (el) el.textContent = val;
}

function triggerConfetti() {
  if (typeof confetti === 'function') {
    confetti({ particleCount: 90, spread: 75, origin: { y: 0.65 } });
  }
}

// Make openLeaveDetails, openDecisionModal, openEmailModal, switchTab global
window.openLeaveDetails    = openLeaveDetails;
window.openDecisionModal   = openDecisionModal;
window.openEmailModal      = openEmailModal;
window.switchTab           = switchTab;
window.closeModal          = closeModal;
window.promptDeleteEmployee = promptDeleteEmployee;
window.selectEmpForLeave   = selectEmpForLeave;
