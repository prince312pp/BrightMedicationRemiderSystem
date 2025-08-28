<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.model.MedicineReminder" %>
<html>
<head>
    <title>Update Reminder - Bright Medication Reminder System</title>
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/styles.css" />
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
        <h2>Update Reminder</h2>
        <%
            MedicineReminder reminder = (MedicineReminder) request.getAttribute("reminder");
            if (reminder != null) {
                String days = reminder.getDaysOfWeek() == null ? "" : reminder.getDaysOfWeek();
        %>
        <form method="post" action="update-reminder">
            <input type="hidden" name="id" value="<%= reminder.getId() %>" />
            <div class="form-row">
                <label class="label" for="medicineName">Medication Name</label>
                <input class="input" type="text" name="medicineName" value="<%= reminder.getMedicineName() %>" required />
            </div>
            <div class="form-row">
                <label class="label" for="dosage">Dosage</label>
                <input class="input" type="text" name="dosage" value="<%= reminder.getDosage() %>" required />
            </div>
            <div class="form-row">
                <label class="label" for="reminderTime">Reminder Time</label>
                <input class="input" type="datetime-local" name="reminderTime" value="<%= reminder.getReminderTime().toString().substring(0,16) %>" required />
            </div>
            <div class="form-row">
                <label class="label" for="frequency">Frequency</label>
                <select class="select" name="frequency" id="frequency" onchange="toggleDays()">
                    <option value="EVERYDAY" <%= "EVERYDAY".equals(reminder.getFrequency()) ? "selected" : "" %>>Every day</option>
                    <option value="WEEKLY" <%= "WEEKLY".equals(reminder.getFrequency()) ? "selected" : "" %>>Specific days of week</option>
                </select>
            </div>
            <div class="form-row" id="daysRow" style="display:<%= "WEEKLY".equals(reminder.getFrequency()) ? "block" : "none" %>;">
                <label class="label">Days of Week</label>
                <div>
                    <label><input type="checkbox" name="daysOfWeek" value="MON" <%= days.contains("MON")?"checked":"" %> /> Mon</label>
                    <label><input type="checkbox" name="daysOfWeek" value="TUE" <%= days.contains("TUE")?"checked":"" %> /> Tue</label>
                    <label><input type="checkbox" name="daysOfWeek" value="WED" <%= days.contains("WED")?"checked":"" %> /> Wed</label>
                    <label><input type="checkbox" name="daysOfWeek" value="THU" <%= days.contains("THU")?"checked":"" %> /> Thu</label>
                    <label><input type="checkbox" name="daysOfWeek" value="FRI" <%= days.contains("FRI")?"checked":"" %> /> Fri</label>
                    <label><input type="checkbox" name="daysOfWeek" value="SAT" <%= days.contains("SAT")?"checked":"" %> /> Sat</label>
                    <label><input type="checkbox" name="daysOfWeek" value="SUN" <%= days.contains("SUN")?"checked":"" %> /> Sun</label>
                </div>
            </div>
            <div class="form-row">
                <label class="label" for="notes">Notes</label>
                <input class="input" type="text" name="notes" value="<%= reminder.getNotes() %>" />
            </div>
            <div class="form-row">
                <label class="label"><input type="checkbox" name="taken" disabled <%= reminder.isTaken()?"checked":"" %> /> Mark as taken (use actions on list)</label>
            </div>
            <button class="btn btn-primary" type="submit">Update Reminder</button>
        </form>
        <% } else { %>
        <div class="card">Reminder not found.</div>
        <% } %>
    </div>

    <div class="footer">Built with Servlets, Hibernate, MySQL</div>
</div>
<script>
function toggleDays(){
    var sel = document.getElementById('frequency');
    document.getElementById('daysRow').style.display = sel.value === 'WEEKLY' ? 'block' : 'none';
}
</script>
</body>
</html>
