<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Add Medicine Reminder</title>
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/styles.css" />
</head>
<body>
<div class="container">
    <div class="header">
        <div class="brand">Medicine Reminder</div>
        <div class="nav">
            <a href="view-reminders">View Reminders</a>
            <a href="add-reminder">Add Reminder</a>
        </div>
    </div>

    <div class="card">
        <h2>Add Medicine Reminder</h2>
        <form method="post" action="add-reminder" autocomplete="off">
            <div class="form-row autocomplete">
                <label class="label" for="medicineName">Medicine Name</label>
                <input class="input" type="text" name="medicineName" id="medicineName" required oninput="handleInput()" />
                <div id="ac-list" class="autocomplete-list" style="display:none;"></div>
            </div>
            <div class="form-row">
                <label class="label" for="dosage">Dosage</label>
                <input class="input" type="text" name="dosage" id="dosage" required />
            </div>
            <div class="form-row">
                <label class="label" for="reminderTime">Reminder Time</label>
                <input class="input" type="datetime-local" name="reminderTime" id="reminderTime" required />
            </div>
            <div class="form-row">
                <label class="label" for="frequency">Frequency</label>
                <select class="select" name="frequency" id="frequency" onchange="toggleDays()">
                    <option value="EVERYDAY" selected>Every day</option>
                    <option value="WEEKLY">Specific days of week</option>
                </select>
            </div>
            <div class="form-row" id="daysRow" style="display:none;">
                <label class="label">Days of Week</label>
                <div>
                    <label><input type="checkbox" name="daysOfWeek" value="MON" /> Mon</label>
                    <label><input type="checkbox" name="daysOfWeek" value="TUE" /> Tue</label>
                    <label><input type="checkbox" name="daysOfWeek" value="WED" /> Wed</label>
                    <label><input type="checkbox" name="daysOfWeek" value="THU" /> Thu</label>
                    <label><input type="checkbox" name="daysOfWeek" value="FRI" /> Fri</label>
                    <label><input type="checkbox" name="daysOfWeek" value="SAT" /> Sat</label>
                    <label><input type="checkbox" name="daysOfWeek" value="SUN" /> Sun</label>
                </div>
            </div>
            <div class="form-row">
                <label class="label" for="notes">Notes</label>
                <input class="input" type="text" name="notes" id="notes" />
            </div>
            <button class="btn btn-primary" type="submit">Add Reminder</button>
        </form>
    </div>

    <div class="footer">Built with Servlets, Hibernate, MySQL</div>
</div>
<script>
const cache = new Map();
let debounceTimer = null;
let inflight = null;
const MIN_LEN = 2;
const listEl = document.getElementById('ac-list');
const inputEl = document.getElementById('medicineName');

function toggleDays(){
    var sel = document.getElementById('frequency');
    document.getElementById('daysRow').style.display = sel.value === 'WEEKLY' ? 'block' : 'none';
}

function hideList(){ listEl.style.display='none'; listEl.innerHTML=''; }
function showLoading(){ listEl.style.display='block'; listEl.innerHTML=''; const m=document.createElement('div'); m.className='autocomplete-muted'; m.textContent='Searching...'; listEl.appendChild(m); }
function render(items){
    listEl.innerHTML='';
    if (!items || !items.length) { hideList(); return; }
    items.forEach(function(n){
        const div = document.createElement('div');
        div.className = 'autocomplete-item';
        div.setAttribute('data-v', n);
        div.textContent = n;
        listEl.appendChild(div);
    });
    listEl.style.display='block';
}

listEl.addEventListener('click', function(e){
    const item = e.target.closest('.autocomplete-item');
    if (!item) return;
    inputEl.value = item.getAttribute('data-v');
    hideList();
});

function handleInput(){
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(fetchSuggestions, 200);
}

function fetchSuggestions(){
    const q = inputEl.value.trim();
    if (q.length < MIN_LEN) { hideList(); return; }
    if (cache.has(q)) { render(cache.get(q)); return; }
    if (inflight) inflight.abort();
    inflight = new AbortController();
    showLoading();
    fetch('suggest-medicines?q=' + encodeURIComponent(q), { signal: inflight.signal })
        .then(function(r){ return r.ok ? r.json() : []; })
        .then(function(arr){ cache.set(q, arr); render(arr); })
        .catch(function(){ hideList(); });
}

window.addEventListener('click', function(e){
    if (!e.target.closest('.autocomplete')) hideList();
});
</script>
</body>
</html>
