<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.model.MedicineReminder" %>
<html>
<head>
    <title>Bright Medication Reminder System</title>
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/styles.css" />
    <style>
        .toast-container { position: fixed; right: 16px; bottom: 16px; display: flex; flex-direction: column; gap: 10px; z-index: 2000; }
        .toast { min-width: 260px; background: rgba(56,189,248,0.15); border:1px solid rgba(56,189,248,0.35); color:#e0f2fe; padding:12px 14px; border-radius:10px; box-shadow:0 10px 24px rgba(0,0,0,0.3); display: flex; align-items: start; gap: 10px; }
        .toast-title { font-weight: 600; margin-bottom: 4px; }
        .toast-body { font-size: 14px; color: #cdeafe; }
        .toast-close { margin-left: auto; background: transparent; border: none; color: #e0f2fe; font-size: 18px; line-height: 1; cursor: pointer; }
        .bar { display:flex; gap:10px; align-items:center; margin: 12px 0; }
        .pill { padding:6px 10px; border:1px solid rgba(255,255,255,0.15); border-radius:999px; background: rgba(255,255,255,0.06); color:#e5e7eb; cursor:pointer; }
        .pill.active { background: rgba(34,197,94,0.25); border-color: rgba(34,197,94,0.5); }
        .hint { color:#94a3b8; font-size: 13px; }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <div class="brand">Bright Medication Reminder System</div>
        <div class="nav">
            <a href="view-reminders">View Reminders</a>
            <a href="add-reminder">Add Reminder</a>
        </div>
    </div>

    <div class="card">
        <h2>Your Medication Reminders</h2>
        <div class="bar">
            <button id="enableNotifications" class="pill">Enable Browser Notifications</button>
            <button id="enableSound" class="pill">Enable Sound Alarm</button>
            <span class="hint">Keep this tab open to receive alerts.</span>
        </div>
        <div class="table-wrapper">
            <table class="table">
                <tr>
                    <th>Medication</th>
                    <th>Dosage</th>
                    <th>Reminder Time</th>
                    <th>Next</th>
                    <th>Freq</th>
                    <th>Notes</th>
                    <th>Actions</th>
                </tr>
                <%
                    List<MedicineReminder> reminders = (List<MedicineReminder>) request.getAttribute("reminders");
                    if (reminders != null) {
                        for (MedicineReminder r : reminders) {
                %>
                <tr>
                    <td><%= r.getMedicineName() %></td>
                    <td><%= r.getDosage() %></td>
                    <td><%= r.getReminderTime() %></td>
                    <td><%= r.getNextReminderTime() %></td>
                    <td><%= r.getFrequency() %><% if (r.getDaysOfWeek()!=null) { %> (<%= r.getDaysOfWeek() %>)<% } %></td>
                    <td><%= r.getNotes() %></td>
                    <td>
                        <%
                            // Check if medication is in cooldown period
                            boolean inCooldown = false;
                            String cooldownMessage = "";
                            if (r.getLastTakenTime() != null) {
                                java.time.LocalDateTime cooldownEnd = r.getLastTakenTime().plusHours(23);
                                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                                if (now.isBefore(cooldownEnd)) {
                                    inCooldown = true;
                                    java.time.Duration remaining = java.time.Duration.between(now, cooldownEnd);
                                    long hours = remaining.toHours();
                                    long minutes = remaining.toMinutes() % 60;
                                    cooldownMessage = String.format("Cooldown: %dh %dm remaining", hours, minutes);
                                }
                            }
                        %>
                        <form method="post" action="mark-taken" style="display:inline;">
                            <input type="hidden" name="id" value="<%= r.getId() %>"/>
                            <button class="btn" type="submit" <%= inCooldown ? "disabled" : "" %> 
                                    title="<%= inCooldown ? cooldownMessage : "Mark medication as taken" %>">
                                <%= inCooldown ? "In Cooldown" : "Mark Taken" %>
                            </button>
                        </form>
                        <% if (inCooldown) { %>
                            <div class="cooldown-info" style="font-size: 12px; color: #94a3b8; margin-top: 4px;">
                                <%= cooldownMessage %>
                            </div>
                        <% } %>
                        <a class="btn" href="update-reminder?id=<%= r.getId() %>">Update</a>
                        <a class="btn btn-danger" href="delete-reminder?id=<%= r.getId() %>" onclick="return confirm('Delete this reminder?');">Delete</a>
                    </td>
                </tr>
                <%      }
                    }
                %>
            </table>
        </div>
    </div>

    <div class="footer">Built with Servlets, Hibernate, MySQL</div>
</div>
<div id="toastContainer" class="toast-container"></div>
<script>
// Helpers
function setLS(k,v){ try{ localStorage.setItem(k, JSON.stringify(v)); }catch(e){} }
function getLS(k,def){ try{ const v=localStorage.getItem(k); return v==null?def:JSON.parse(v);}catch(e){return def;} }

// Browser Notifications toggle
const notifBtn = document.getElementById('enableNotifications');
let notificationsEnabled = getLS('notifEnabled', false);

function updateNotifBtn(){
  if (notificationsEnabled) { notifBtn.classList.add('active'); notifBtn.textContent='Disable Browser Notifications'; }
  else { notifBtn.classList.remove('active'); notifBtn.textContent='Enable Browser Notifications'; }
}

function showHint(msg){
  const el = document.createElement('div');
  el.className = 'toast';
  el.textContent = msg;
  const close = document.createElement('button'); close.className = 'toast-close'; close.innerHTML='&times;'; close.onclick=()=>el.remove();
  el.appendChild(close);
  document.getElementById('toastContainer').appendChild(el);
}

if ('Notification' in window) {
  notifBtn.onclick = () => {
    if (!notificationsEnabled) {
      if (Notification.permission !== 'granted') {
        Notification.requestPermission().then((p)=>{
          if (p === 'granted') { notificationsEnabled = true; setLS('notifEnabled', true); updateNotifBtn(); showHint('Browser notifications enabled.'); }
          else if (p === 'denied') { showHint('Notifications blocked. Allow them in your browser site settings.'); }
          else { showHint('Notification permission dismissed.'); }
        }).catch(()=> showHint('Unable to request notification permission.'));
      } else {
        notificationsEnabled = true; setLS('notifEnabled', true); updateNotifBtn(); showHint('Browser notifications enabled.');
      }
    } else {
      notificationsEnabled = false; setLS('notifEnabled', false); updateNotifBtn(); showHint('Browser notifications disabled.');
    }
  };
  if (Notification.permission !== 'granted') {
    if (notificationsEnabled) notificationsEnabled = false;
  }
  updateNotifBtn();
} else {
  notifBtn.disabled = true; notifBtn.title = 'Notifications not supported';
}

// Sound Alarm via Web Audio toggle
let soundEnabled = getLS('soundEnabled', false);
let audioCtx = null;
let playingNodes = [];
const soundBtn = document.getElementById('enableSound');

function updateSoundBtn(){
  if (soundEnabled) { soundBtn.classList.add('active'); soundBtn.textContent='Disable Sound Alarm'; }
  else { soundBtn.classList.remove('active'); soundBtn.textContent='Enable Sound Alarm'; }
}

soundBtn.onclick = () => {
  if (!soundEnabled) {
    try {
      if (!audioCtx) audioCtx = new (window.AudioContext || window.webkitAudioContext)();
      if (audioCtx.state === 'suspended') { audioCtx.resume(); }
      soundEnabled = true; setLS('soundEnabled', true); updateSoundBtn();
      showHint('Sound alarm enabled.');
    } catch(e) { showHint('Unable to enable sound.'); }
  } else {
    soundEnabled = false; setLS('soundEnabled', false); updateSoundBtn();
    try { playingNodes.forEach(n=>{try{n.stop();}catch(_){}}); playingNodes = []; } catch(_){ }
    showHint('Sound alarm disabled.');
  }
};

function playAlarm(seconds=5){
  if (!soundEnabled) return;
  if (!audioCtx) {
    try { audioCtx = new (window.AudioContext || window.webkitAudioContext)(); } catch(e) { return; }
  }
  const now = audioCtx.currentTime;
  const osc = audioCtx.createOscillator();
  const gain = audioCtx.createGain();
  osc.type = 'sine';
  osc.frequency.setValueAtTime(880, now);
  gain.gain.setValueAtTime(0.0001, now);
  for (let i=0;i<seconds;i++){
    const t = now + i*0.5;
    gain.gain.setValueAtTime(0.0001, t);
    gain.gain.linearRampToValueAtTime(0.2, t+0.05);
    gain.gain.linearRampToValueAtTime(0.0001, t+0.25);
  }
  osc.connect(gain).connect(audioCtx.destination);
  osc.start(now);
  osc.stop(now + seconds*0.5 + 0.3);
  playingNodes.push(osc);
  osc.onended = () => { playingNodes = playingNodes.filter(n=>n!==osc); };
}

// Toasts
const toastContainer = document.getElementById('toastContainer');
let lastShownIds = new Set();

function showToast(title, body){
  const el = document.createElement('div');
  el.className = 'toast';
  const content = document.createElement('div');
  const h = document.createElement('div'); h.className = 'toast-title'; h.textContent = title;
  const p = document.createElement('div'); p.className = 'toast-body'; p.textContent = body || '';
  const close = document.createElement('button'); close.className = 'toast-close'; close.innerHTML = '&times;';
  close.onclick = () => { toastContainer.removeChild(el); };
  content.appendChild(h); content.appendChild(p);
  el.appendChild(content); el.appendChild(close);
  toastContainer.appendChild(el);
}

function notifyUser(title, body){
  if (notificationsEnabled && 'Notification' in window && Notification.permission === 'granted') {
    try { new Notification(title, { body }); } catch(e) { showToast(title, body); }
  } else {
    showToast(title, body);
  }
}

function pollDue(){
  fetch('due-reminders')
    .then(r=> r.ok ? r.json() : [])
    .then(list => {
      (list || []).forEach(item => {
        const id = item.id;
        if (!lastShownIds.has(id)) {
          lastShownIds.add(id);
          const title = 'Time to take ' + item.name;
          const body = 'Dosage: ' + (item.dosage || '');
          notifyUser(title, body);
          playAlarm(6);
        }
      });
      setTimeout(()=> { lastShownIds.clear(); }, 5*60*1000);
    })
    .catch(()=>{});
}

updateSoundBtn();
updateNotifBtn();
setInterval(pollDue, 30000);
pollDue();
</script>
</body>
</html>
