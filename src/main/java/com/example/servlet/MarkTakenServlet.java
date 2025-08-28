package com.example.servlet;

import com.example.model.MedicineReminder;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MarkTakenServlet extends HttpServlet {
    private SessionFactory sessionFactory;

    @Override
    public void init() throws ServletException {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null) { resp.sendRedirect("view-reminders"); return; }
        Long id = Long.parseLong(idStr);
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        MedicineReminder r = session.get(MedicineReminder.class, id);
        if (r != null) {
            r.setTaken(true);
            // Advance nextReminderTime based on frequency
            LocalDateTime base = r.getNextReminderTime() != null ? r.getNextReminderTime() : r.getReminderTime();
            if ("WEEKLY".equalsIgnoreCase(r.getFrequency()) && r.getDaysOfWeek() != null) {
                Set<String> days = new HashSet<>(Arrays.asList(r.getDaysOfWeek().split(",")));
                LocalDate d = base.toLocalDate();
                for (int i = 1; i <= 7; i++) {
                    d = d.plusDays(1);
                    String code = DayOfWeek.from(d).name().substring(0,3);
                    if (days.contains(code)) { r.setNextReminderTime(LocalDateTime.of(d, base.toLocalTime())); break; }
                }
            } else {
                r.setNextReminderTime(base.plusDays(1));
            }
        }
        session.getTransaction().commit();
        session.close();
        resp.sendRedirect("view-reminders");
    }

    @Override
    public void destroy() { if (sessionFactory != null) sessionFactory.close(); }
}
