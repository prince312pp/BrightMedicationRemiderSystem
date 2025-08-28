package com.example.servlet;

import com.example.model.MedicineReminder;
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

public class UpdateReminderServlet extends HttpServlet {
    private SessionFactory sessionFactory;

    @Override
    public void init() throws ServletException {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr != null) {
            Long id = Long.parseLong(idStr);
            Session session = sessionFactory.openSession();
            MedicineReminder reminder = session.get(MedicineReminder.class, id);
            session.close();
            req.setAttribute("reminder", reminder);
            req.getRequestDispatcher("/jsp/updateReminder.jsp").forward(req, resp);
        } else {
            resp.sendRedirect("view-reminders");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr != null) {
            Long id = Long.parseLong(idStr);
            String medicineName = req.getParameter("medicineName");
            String dosage = req.getParameter("dosage");
            String reminderTime = req.getParameter("reminderTime");
            String notes = req.getParameter("notes");
            String frequency = req.getParameter("frequency");
            String[] days = req.getParameterValues("daysOfWeek");
            String daysOfWeek = (days != null && days.length > 0) ? String.join(",", days) : null;
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            MedicineReminder reminder = session.get(MedicineReminder.class, id);
            if (reminder != null) {
                reminder.setMedicineName(medicineName);
                reminder.setDosage(dosage);
                LocalDateTime time = LocalDateTime.parse(reminderTime);
                reminder.setReminderTime(time);
                reminder.setNextReminderTime(time);
                reminder.setNotes(notes);
                if (frequency != null && !frequency.isEmpty()) reminder.setFrequency(frequency);
                reminder.setDaysOfWeek(daysOfWeek);
            }
            session.getTransaction().commit();
            session.close();
        }
        resp.sendRedirect("view-reminders");
    }

    @Override
    public void destroy() {
        if (sessionFactory != null) sessionFactory.close();
    }
}
