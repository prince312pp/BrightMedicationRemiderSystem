package com.example.servlet;

import com.example.model.MedicineReminder;
import com.example.util.MedicineApiUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class AddReminderServlet extends HttpServlet {
    private SessionFactory sessionFactory;

    @Override
    public void init() throws ServletException {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String query = req.getParameter("q");
        List<String> suggestions = null;
        if (query != null && !query.isEmpty()) {
            suggestions = MedicineApiUtil.searchMedicineNames(query);
        }
        req.setAttribute("suggestions", suggestions);
        req.getRequestDispatcher("/jsp/addReminder.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String medicineName = req.getParameter("medicineName");
        String dosage = req.getParameter("dosage");
        String reminderTime = req.getParameter("reminderTime");
        String notes = req.getParameter("notes");
        String frequency = req.getParameter("frequency");
        String[] days = req.getParameterValues("daysOfWeek");
        String daysOfWeek = null;
        if (days != null && days.length > 0) {
            daysOfWeek = String.join(",", days);
        }
        MedicineReminder reminder = new MedicineReminder();
        reminder.setMedicineName(medicineName);
        reminder.setDosage(dosage);
        LocalDateTime time = LocalDateTime.parse(reminderTime);
        reminder.setReminderTime(time);
        reminder.setNextReminderTime(time);
        reminder.setNotes(notes);
        if (frequency != null && !frequency.isEmpty()) reminder.setFrequency(frequency);
        if (daysOfWeek != null) reminder.setDaysOfWeek(daysOfWeek);
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(reminder);
        session.getTransaction().commit();
        session.close();
        resp.sendRedirect("view-reminders");
    }

    @Override
    public void destroy() {
        if (sessionFactory != null) sessionFactory.close();
    }
}
