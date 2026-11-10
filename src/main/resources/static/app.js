/* ===== helpers ===== */
const $ = id => document.getElementById(id);
const api = async (url, opts) => {
  const r = await fetch(url, opts); let d = null; try { d = await r.json(); } catch (e) {}
  if (!r.ok) { const e = new Error((d && d.message) || 'Request failed'); e.errors = d && d.errors; throw e; }
  return d;
};
const send = (m, b) => ({ method: m, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(b) });
const esc = s => String(s ?? '').replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
const iso = d => d.toLocaleDateString('en-CA');
const nice = s => new Date(s + 'T00:00:00').toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });
const short = s => new Date(s + 'T00:00:00').toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });
const ageOf = dob => { const b = new Date(dob), n = new Date(); let a = n.getFullYear() - b.getFullYear(); if (n < new Date(n.getFullYear(), b.getMonth(), b.getDate())) a--; return a; };
const COLORS = ['#0f8b8d', '#ff7a59', '#6b5bd2', '#d99a00', '#2f80ed', '#d6457a', '#2e9e6b'];
const av = (name, big) => { let h = 0; for (const c of name) h = (h * 31 + c.charCodeAt(0)) % 997;
  const ini = name.replace(/^Dr\.?\s*/i, '').split(/\s+/).map(w => w[0]).slice(0, 2).join('').toUpperCase();
  return `<div class="av" style="background:${COLORS[h % COLORS.length]}${big ? ';width:52px;height:52px;font-size:18px' : ''}">${ini}</div>`; };
function toast(m, err) { const t = $('toast'); t.textContent = m; t.className = 'show' + (err ? ' error' : ''); clearTimeout(toast.t); toast.t = setTimeout(() => t.className = '', 3400); }
const setErr = (f, m) => { const e = $('e_' + f); if (e) e.textContent = m || ''; const i = $(f); if (i) i.classList.toggle('bad', !!m); };
const SLOT_LABEL = {};

/* ===== drawer ===== */
function openDrawer(title, html) { $('dTitle').textContent = title; $('dBody').innerHTML = html; $('drawer').classList.add('on'); $('overlay').classList.add('on'); }
function closeDrawer() { $('drawer').classList.remove('on'); $('overlay').classList.remove('on'); }
$('dClose').onclick = closeDrawer; $('overlay').onclick = closeDrawer;

/* ===== date strip + slot grid component ===== */
function Picker(el, p, getCoach, onChange) {
  const s = { date: null, slot: null };
  const days = Array.from({ length: 8 }, (_, i) => { const d = new Date(); d.setDate(d.getDate() + i); return d; });
  el.innerHTML = `<div class="dates">${days.map(d => `<button type="button" class="dpill" data-d="${iso(d)}"><small>${d.toLocaleDateString('en-IN', { weekday: 'short' })}</small><b>${d.getDate()}</b><small>${d.toLocaleDateString('en-IN', { month: 'short' })}</small></button>`).join('')}</div>
    <div class="slots" id="${p}slots"><div class="empty" style="grid-column:1/-1">Pick a date to see available times</div></div>
    <div class="legend"><span><i style="background:#fff;border:1.5px solid #cfe0dd"></i>Available</span><span><i style="background:#f3d9d9"></i>Booked</span><span><i style="background:#eef2f1"></i>Passed</span></div>`;
  async function load() {
    const coach = getCoach(); const box = $(p + 'slots');
    if (!coach || !s.date) { box.innerHTML = '<div class="empty" style="grid-column:1/-1">' + (coach ? 'Pick a date to see available times' : 'Choose a coach first') + '</div>'; return; }
    try {
      const l = await api(`/api/coaches/${coach}/slots?date=${s.date}`);
      box.innerHTML = l.map(x => `<button type="button" class="slot ${x.state} ${s.slot === x.slot ? 'sel' : ''}" data-s="${x.slot}">${x.label}</button>`).join('');
      l.forEach(x => SLOT_LABEL[x.slot] = x.label);
      box.querySelectorAll('.slot').forEach(b => b.onclick = () => { s.slot = b.dataset.s; load(); onChange(s); });
    } catch (e) { box.innerHTML = `<div class="empty err" style="grid-column:1/-1">${esc(e.message)}</div>`; }
  }
  el.querySelectorAll('.dpill').forEach(b => b.onclick = () => {
    s.date = b.dataset.d; s.slot = null; el.querySelectorAll('.dpill').forEach(x => x.classList.toggle('sel', x === b)); load(); onChange(s);
  });
  return { state: s, refresh: () => { s.slot = null; load(); onChange(s); } };
}

/* ===== router ===== */
const VIEWS = {
  dashboard: ['Overview', 'Your wellness programme at a glance', viewDashboard],
  book: ['Book Appointment', 'Pick a member, a coach and a time that works', viewBook],
  appointments: ['Appointments', 'Review, reschedule, complete or cancel sessions', viewAppointments],
  coaches: ['Coaches', 'Wellness experts available for sessions', viewCoaches],
  members: ['Members', 'Registered users of the programme', viewMembers]
};
function route() {
  const v = (location.hash || '#dashboard').slice(1); const cfg = VIEWS[v] || VIEWS.dashboard; const key = VIEWS[v] ? v : 'dashboard';
  document.querySelectorAll('#nav a').forEach(a => a.classList.toggle('on', a.dataset.v === key));
  $('pageTitle').textContent = cfg[0]; $('pageSub').textContent = cfg[1]; closeDrawer(); cfg[2]();
}
window.addEventListener('hashchange', route);
$('todayChip').textContent = new Date().toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long' });

/* ===== DASHBOARD ===== */
async function viewDashboard() {
  $('view').innerHTML = '<div class="empty">Loading...</div>';
  try {
    const [s, all] = await Promise.all([api('/api/stats'), api('/api/appointments?status=BOOKED')]);
    const today = iso(new Date()); const todays = all.filter(a => a.appointmentDate === today).sort((a, b) => a.slot.localeCompare(b.slot));
    const upcoming = all.filter(a => a.appointmentDate > today).sort((a, b) => (a.appointmentDate + a.slot).localeCompare(b.appointmentDate + b.slot)).slice(0, 6);
    const max = Math.max(1, ...s.workload.map(w => w.appointments));
    const row = a => `<div class="line" style="cursor:pointer" onclick="openAppt('${a.code}')">${av(a.user.name)}<div class="grow"><b>${esc(a.user.name)}</b><small>with ${esc(a.coach.name)} &middot; ${esc(a.reason || 'General session')}</small></div><div class="time">${a.appointmentDate === today ? '' : short(a.appointmentDate) + '<br>'}${a.slot.replace('S', '') + ':00'}</div></div>`;
    $('view').innerHTML = `<div class="tiles">
      <div class="tile t1"><b>${s.today}</b><span>Sessions today</span></div><div class="tile t2"><b>${s.booked}</b><span>Upcoming booked</span></div>
      <div class="tile t3"><b>${s.completed}</b><span>Completed</span></div><div class="tile t4"><b>${s.cancelled}</b><span>Cancelled</span></div>
      <div class="tile t5"><b>${s.coaches} / ${s.users}</b><span>Coaches / Members</span></div></div>
      <div class="cols"><div>
        <div class="panel"><h2>Today's schedule</h2><p class="hint">Booked sessions happening today</p>${todays.map(row).join('') || '<div class="empty">No sessions today.</div>'}</div>
        <div class="panel"><h2>Coming up</h2><p class="hint">Next booked sessions</p>${upcoming.map(row).join('') || '<div class="empty">Nothing scheduled yet.</div>'}</div></div>
        <div><div class="panel"><h2>Coach workload</h2><p class="hint">Active appointments per coach</p>
          ${s.workload.map(w => `<div class="bar2"><span>${esc(w.coach)}</span><div><i style="width:${w.appointments / max * 100}%"></i></div><b>${w.appointments}</b></div>`).join('') || '<div class="empty">No data yet.</div>'}</div>
        <div class="panel" style="background:linear-gradient(135deg,#0f8b8d,#35c4b0);color:#fff;border:0"><h2>Need a session?</h2><p class="hint" style="color:#d5f6f1">Book a coach in under a minute.</p><a href="#book" class="btn coral" style="text-decoration:none;display:inline-block">Book appointment</a></div></div></div>`;
  } catch (e) { $('view').innerHTML = `<div class="panel err">${esc(e.message)}</div>`; }
}

/* ===== BOOK ===== */
async function viewBook() {
  const st = { user: '', coach: '', date: '', slot: '' };
  try {
    const [users, coaches] = await Promise.all([api('/api/users'), api('/api/coaches')]);
    $('view').innerHTML = `<div class="book"><div>
      <div class="panel"><h2>1 &middot; Who is the session for?</h2><p class="hint">Select a registered member</p>
        <div class="row2"><label>Member<select id="bUser"><option value="">Select member</option>${users.map(u => `<option value="${u.code}">${esc(u.name)} (${u.code})</option>`).join('')}</select></label>
        <div style="display:flex;align-items:flex-end"><button class="btn line2" id="newMember">+ Register new member</button></div></div><span class="err" id="e_bUser"></span></div>
      <div class="panel"><h2>2 &middot; Choose your coach</h2><p class="hint">Tap a coach card</p>
        <div class="coaches">${coaches.map(c => `<button class="coach" data-c="${c.code}">${av(c.name, true)}<b>${esc(c.name)}</b><small>${c.code}</small><br><span class="chip">${esc(c.specialization)}</span></button>`).join('')}</div></div>
      <div class="panel"><h2>3 &middot; Pick a date &amp; time</h2><p class="hint">Appointments open for the next 7 days. Each session is 1 hour.</p><div id="pk"></div></div>
      <div class="panel"><h2>4 &middot; What would you like to discuss?</h2><p class="hint">Optional</p><textarea id="bReason" rows="3" maxlength="300" placeholder="e.g. Diet plan for weight loss"></textarea></div></div>
      <aside class="panel sumbox" id="sum"></aside></div>`;
    const picker = Picker($('pk'), 'b', () => st.coach, s => { st.date = s.date; st.slot = s.slot; paint(); });
    $('bUser').onchange = e => { st.user = e.target.value; paint(); };
    document.querySelectorAll('.coach').forEach(b => b.onclick = () => { st.coach = b.dataset.c; document.querySelectorAll('.coach').forEach(x => x.classList.toggle('sel', x === b)); picker.refresh(); paint(); });
    $('newMember').onclick = () => memberForm(null, () => viewBook());
    $('bReason').oninput = paint;
    function paint() {
      const u = users.find(x => x.code === st.user), c = coaches.find(x => x.code === st.coach), ok = u && c && st.date && st.slot;
      $('sum').innerHTML = `<h2>Appointment summary</h2><p class="hint">Check everything before confirming</p>
        <div class="sumrow"><span>Member</span><b>${u ? esc(u.name) : '-'}</b></div><div class="sumrow"><span>Coach</span><b>${c ? esc(c.name) : '-'}</b></div>
        <div class="sumrow"><span>Speciality</span><b>${c ? esc(c.specialization) : '-'}</b></div><div class="sumrow"><span>Date</span><b>${st.date ? nice(st.date) : '-'}</b></div>
        <div class="sumrow"><span>Time</span><b>${st.slot ? (SLOT_LABEL[st.slot] || st.slot) : '-'}</b></div>
        <div class="sumrow"><span>Reason</span><b>${esc($('bReason').value.trim()) || '-'}</b></div>
        <div class="err" id="e_sum" style="margin:10px 0"></div><button class="btn coral" style="width:100%;margin-top:6px" id="confirm" ${ok ? '' : 'disabled'}>Confirm appointment</button>`;
      if (ok) $('confirm').onclick = async () => {
        try {
          const a = await api('/api/appointments', send('POST', { userCode: st.user, coachCode: st.coach, appointmentDate: st.date, slot: st.slot, reason: $('bReason').value.trim() }));
          $('sum').innerHTML = `<div style="text-align:center"><div class="av" style="width:64px;height:64px;font-size:30px;background:var(--ok);margin:0 auto 12px">&#10003;</div><h2>Appointment booked!</h2>
            <p class="hint">Your reference</p><div style="font-size:30px;font-weight:800;letter-spacing:2px;color:var(--teal-d)">${a.code}</div>
            <p>${esc(a.user.name)} with ${esc(a.coach.name)}<br><b>${nice(a.appointmentDate)}</b><br>${a.slot ? SLOT_LABEL[a.slot] || a.slot : ''}</p>
            <button class="btn" onclick="viewBook()">Book another</button> <a class="btn line2" style="text-decoration:none;display:inline-block" href="#appointments">View all</a></div>`;
          toast('Appointment ' + a.code + ' booked');
        } catch (e) { $('e_sum').textContent = e.message; picker.refresh(); }
      };
    }
    paint();
  } catch (e) { $('view').innerHTML = `<div class="panel err">${esc(e.message)}</div>`; }
}
window.viewBook = viewBook;

/* ===== APPOINTMENTS ===== */
async function viewAppointments() {
  const f = { status: '', coach: '', q: '' }; let list = [];
  try {
    const coaches = await api('/api/coaches');
    $('view').innerHTML = `<div class="panel"><div class="filters"><div class="seg" id="seg">${['', 'BOOKED', 'COMPLETED', 'CANCELLED'].map(s => `<button data-s="${s}" class="${s === '' ? 'on' : ''}">${s || 'All'}</button>`).join('')}</div>
      <select id="fCoach"><option value="">All coaches</option>${coaches.map(c => `<option value="${c.code}">${esc(c.name)}</option>`).join('')}</select>
      <input id="fQ" placeholder="Search code or member"></div>
      <div class="tablewrap"><table><thead><tr><th>Code</th><th>Member</th><th>Coach</th><th>Date</th><th>Time</th><th>Status</th><th>Resched.</th></tr></thead><tbody id="tb"></tbody></table></div></div>`;
    async function load() { list = await api('/api/appointments' + (f.status ? '?status=' + f.status : '')); draw(); }
    function draw() {
      const q = f.q.toLowerCase();
      const r = list.filter(a => (!f.coach || a.coach.code === f.coach) && (!q || a.code.toLowerCase().includes(q) || a.user.name.toLowerCase().includes(q)));
      $('tb').innerHTML = r.map(a => `<tr onclick="openAppt('${a.code}')"><td><b>${a.code}</b></td><td>${esc(a.user.name)}</td><td>${esc(a.coach.name)}</td><td>${short(a.appointmentDate)}</td><td>${SLOT_LABEL[a.slot] || a.slot.replace('S', '') + ':00'}</td>
        <td><span class="badge s-${a.status}">${a.status}</span></td><td>${a.rescheduleCount}</td></tr>`).join('') || '<tr><td colspan="7" class="empty">No appointments found</td></tr>';
    }
    $('seg').querySelectorAll('button').forEach(b => b.onclick = () => { f.status = b.dataset.s; $('seg').querySelectorAll('button').forEach(x => x.classList.toggle('on', x === b)); load(); });
    $('fCoach').onchange = e => { f.coach = e.target.value; draw(); }; $('fQ').oninput = e => { f.q = e.target.value; draw(); };
    window.reloadAppts = load; await load();
  } catch (e) { $('view').innerHTML = `<div class="panel err">${esc(e.message)}</div>`; }
}

window.openAppt = async code => {
  try {
    const a = await api('/api/appointments/' + code); const slotL = SLOT_LABEL[a.slot] || a.slot.replace('S', '') + ':00';
    const act = a.status === 'BOOKED' ? `<div class="dfoot" id="acts"><button class="btn line2 sm" id="aRe">Reschedule</button><button class="btn sm" id="aDone">Mark completed</button><button class="btn danger sm" id="aCancel">Cancel</button></div><div id="aPanel"></div>` : '';
    openDrawer('Appointment ' + a.code, `<div style="display:flex;gap:14px;align-items:center">${av(a.user.name, true)}<div><b style="font-size:18px">${esc(a.user.name)}</b><br><span class="badge s-${a.status}">${a.status}</span></div></div>
      <div class="kv"><div><span>Coach</span><b>${esc(a.coach.name)}</b></div><div><span>Speciality</span><b>${esc(a.coach.specialization)}</b></div>
      <div><span>Date</span><b>${nice(a.appointmentDate)}</b></div><div><span>Time</span><b>${slotL}</b></div>
      <div><span>Member contact</span><b>${a.user.mobile}</b></div><div><span>Rescheduled</span><b>${a.rescheduleCount} time(s)</b></div></div>
      <div class="kv" style="grid-template-columns:1fr"><div><span>Reason</span><b>${esc(a.reason || '-')}</b></div>${a.coachNotes ? `<div><span>Coach notes</span><b>${esc(a.coachNotes)}</b></div>` : ''}</div>${act}`);
    if (a.status !== 'BOOKED') return;
    $('aRe').onclick = () => {
      $('aPanel').innerHTML = `<h3 style="margin:18px 0 6px">Choose a new date &amp; time</h3><div id="rpk"></div><div class="err" id="e_re" style="margin:8px 0"></div><button class="btn coral" id="reGo" disabled>Confirm new time</button>`;
      const pk = Picker($('rpk'), 'r', () => a.coach.code, s => { $('reGo').disabled = !(s.date && s.slot); });
      $('reGo').onclick = async () => { try { await api(`/api/appointments/${code}/reschedule`, send('PUT', { appointmentDate: pk.state.date, slot: pk.state.slot })); toast('Rescheduled'); closeDrawer(); refreshView(); }
        catch (e) { $('e_re').textContent = e.message; } };
    };
    $('aDone').onclick = () => {
      $('aPanel').innerHTML = `<h3 style="margin:18px 0 6px">Coach notes</h3><textarea id="notes" rows="4" maxlength="600" placeholder="Summary of the session (optional)"></textarea><div class="err" id="e_dn" style="margin:8px 0"></div><button class="btn" id="doneGo">Save &amp; complete</button>`;
      $('doneGo').onclick = async () => { try { await api(`/api/appointments/${code}/complete`, send('PUT', { notes: $('notes').value.trim() })); toast('Marked as completed'); closeDrawer(); refreshView(); } catch (e) { $('e_dn').textContent = e.message; } };
    };
    $('aCancel').onclick = async () => { if (!confirm('Cancel appointment ' + code + '?')) return; try { await api(`/api/appointments/${code}/cancel`, { method: 'PUT' }); toast('Appointment cancelled'); closeDrawer(); refreshView(); } catch (e) { toast(e.message, true); } };
  } catch (e) { toast(e.message, true); }
};
const refreshView = () => { (location.hash || '#dashboard') === '#appointments' ? window.reloadAppts && window.reloadAppts() : route(); };

/* ===== generic person form ===== */
function fieldHTML(n, label, type, val, extra = '') { return `<label>${label}<input id="${n}" type="${type}" value="${esc(val ?? '')}" ${extra}><span class="err" id="e_${n}"></span></label>`; }
function genderHTML(v) { return `<label>Gender<select id="gender">${['', 'Male', 'Female', 'Other'].map(g => `<option value="${g}" ${g === v ? 'selected' : ''}>${g || 'Select'}</option>`).join('')}</select><span class="err" id="e_gender"></span></label>`; }
const get = id => $(id).value.trim();
function checkCommon(min, max, who) {
  let ok = true; const bad = (f, m) => { setErr(f, m); ok = false; };
  ['name', 'dateOfBirth', 'gender', 'mobile'].forEach(f => setErr(f, ''));
  if (get('name').length < 3) bad('name', 'Name must be at least 3 characters');
  if (!get('dateOfBirth')) bad('dateOfBirth', 'Date of birth is required'); else { const a = ageOf(get('dateOfBirth')); if (a < min || a > max) bad('dateOfBirth', `${who} age must be between ${min} and ${max} (entered ${a})`); }
  if (!get('gender')) bad('gender', 'Select gender');
  if (!/^[6-9]\d{9}$/.test(get('mobile'))) bad('mobile', 'Mobile must be 10 digits starting with 6-9');
  return { ok, bad };
}
function serverErr(e) { if (e.errors) Object.entries(e.errors).forEach(([f, m]) => setErr(f, m)); else toast(e.message, true); }

/* ===== COACHES ===== */
async function viewCoaches() {
  try {
    const list = await api('/api/coaches');
    $('view').innerHTML = `<div style="display:flex;justify-content:flex-end;margin-bottom:14px"><button class="btn" id="addC">+ Add coach</button></div>
      <div class="coaches">${list.map(c => `<div class="panel" style="margin:0">${av(c.name, true)}<h2 style="margin-top:12px">${esc(c.name)}</h2><small style="color:var(--muted)">${c.code} &middot; ${c.gender} &middot; ${ageOf(c.dateOfBirth)} yrs</small><br>
        <span class="chip">${esc(c.specialization)}</span><p style="margin:10px 0;font-size:13.5px">&#9742; ${c.mobile}</p>
        <div style="display:flex;gap:8px"><button class="btn line2 sm" onclick="editCoach('${c.code}')">Edit</button><button class="btn danger sm" onclick="delCoach('${c.code}')">Delete</button></div></div>`).join('')}</div>`;
    $('addC').onclick = () => coachForm(null);
    window.__coaches = list;
  } catch (e) { $('view').innerHTML = `<div class="panel err">${esc(e.message)}</div>`; }
}
window.editCoach = code => coachForm(window.__coaches.find(c => c.code === code));
window.delCoach = async code => { if (!confirm('Delete coach ' + code + '?')) return; try { await api('/api/coaches/' + code, { method: 'DELETE' }); toast('Coach deleted'); viewCoaches(); } catch (e) { toast(e.message, true); } };
function coachForm(c) {
  openDrawer(c ? 'Edit coach ' + c.code : 'Add coach', `<div class="form">${fieldHTML('name', 'Full name', 'text', c?.name)}${fieldHTML('dateOfBirth', 'Date of birth', 'date', c?.dateOfBirth, `max="${iso(new Date())}"`)}
    <div class="row2">${genderHTML(c?.gender)}${fieldHTML('mobile', 'Mobile', 'text', c?.mobile, 'maxlength="10"')}</div>${fieldHTML('specialization', 'Specialization', 'text', c?.specialization)}
    <div class="dfoot"><button class="btn line2" onclick="closeDrawer()">Cancel</button><button class="btn" id="saveP">${c ? 'Save changes' : 'Add coach'}</button></div></div>`);
  $('saveP').onclick = async () => {
    const { ok, bad } = checkCommon(22, 70, 'Coach'); setErr('specialization', ''); if (!get('specialization')) bad('specialization', 'Specialization is required'); if (!ok) return;
    const body = { name: get('name'), dateOfBirth: get('dateOfBirth'), gender: get('gender'), mobile: get('mobile'), specialization: get('specialization') };
    try { c ? await api('/api/coaches/' + c.code, send('PUT', body)) : await api('/api/coaches', send('POST', body)); toast(c ? 'Coach updated' : 'Coach added'); closeDrawer(); viewCoaches(); } catch (e) { serverErr(e); }
  };
}
window.closeDrawer = closeDrawer;

/* ===== MEMBERS ===== */
async function viewMembers() {
  try {
    const list = await api('/api/users'); window.__users = list;
    $('view').innerHTML = `<div class="panel"><div class="filters"><input id="mq" placeholder="Search name, city or code"><span style="flex:1"></span><button class="btn" id="addM">+ Register member</button></div>
      <div class="tablewrap"><table><thead><tr><th>Member</th><th>Code</th><th>Age</th><th>Mobile</th><th>Email</th><th>City</th></tr></thead><tbody id="mt"></tbody></table></div></div>`;
    const draw = () => { const q = $('mq').value.toLowerCase();
      $('mt').innerHTML = list.filter(u => !q || [u.name, u.city, u.code].some(x => x.toLowerCase().includes(q))).map(u => `<tr onclick="editMember('${u.code}')"><td><div style="display:flex;gap:10px;align-items:center">${av(u.name)}<b>${esc(u.name)}</b></div></td>
        <td>${u.code}</td><td>${ageOf(u.dateOfBirth)}</td><td>${u.mobile}</td><td>${esc(u.email)}</td><td>${esc(u.city)}, ${esc(u.state)}</td></tr>`).join('') || '<tr><td colspan="6" class="empty">No members found</td></tr>'; };
    $('mq').oninput = draw; draw(); $('addM').onclick = () => memberForm(null, viewMembers);
  } catch (e) { $('view').innerHTML = `<div class="panel err">${esc(e.message)}</div>`; }
}
window.editMember = code => memberForm(window.__users.find(u => u.code === code), viewMembers);
function memberForm(u, after) {
  openDrawer(u ? 'Edit member ' + u.code : 'Register member', `<div class="form">${fieldHTML('name', 'Full name', 'text', u?.name)}${fieldHTML('dateOfBirth', 'Date of birth', 'date', u?.dateOfBirth, `max="${iso(new Date())}"`)}
    <div class="row2">${genderHTML(u?.gender)}${fieldHTML('mobile', 'Mobile', 'text', u?.mobile, 'maxlength="10"')}</div>${fieldHTML('email', 'Email', 'email', u?.email)}
    <div class="row2">${fieldHTML('pincode', 'Pincode (6 digits)', 'text', u?.pincode, 'maxlength="6"')}${fieldHTML('city', 'City', 'text', u?.city)}</div>
    <div class="row2">${fieldHTML('state', 'State', 'text', u?.state)}${fieldHTML('country', 'Country', 'text', u?.country || 'India')}</div>
    <div class="dfoot">${u ? '<button class="btn danger" id="delP">Delete</button>' : ''}<button class="btn line2" onclick="closeDrawer()">Cancel</button><button class="btn" id="saveP">${u ? 'Save changes' : 'Register'}</button></div></div>`);
  if (u) $('delP').onclick = async () => { if (!confirm('Delete member ' + u.code + '?')) return; try { await api('/api/users/' + u.code, { method: 'DELETE' }); toast('Member deleted'); closeDrawer(); after(); } catch (e) { toast(e.message, true); } };
  $('saveP').onclick = async () => {
    const { ok, bad } = checkCommon(20, 100, 'User'); ['email', 'pincode', 'city', 'state', 'country'].forEach(f => setErr(f, ''));
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(get('email'))) bad('email', 'Enter a valid email');
    if (!/^\d{6}$/.test(get('pincode'))) bad('pincode', 'Pincode must be exactly 6 digits');
    ['city', 'state', 'country'].forEach(f => { if (!get(f)) bad(f, 'Required'); }); if (!ok) return;
    const body = { name: get('name'), dateOfBirth: get('dateOfBirth'), gender: get('gender'), mobile: get('mobile'), email: get('email'), pincode: get('pincode'), city: get('city'), state: get('state'), country: get('country') };
    try { u ? await api('/api/users/' + u.code, send('PUT', body)) : await api('/api/users', send('POST', body)); toast(u ? 'Member updated' : 'Member registered'); closeDrawer(); after(); } catch (e) { serverErr(e); }
  };
}

api('/api/slots').then(l => l.forEach(s => SLOT_LABEL[s.slot] = s.label)).catch(() => {}).finally(route);
