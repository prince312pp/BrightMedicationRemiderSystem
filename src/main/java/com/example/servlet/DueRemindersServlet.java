package com.example.servlet;

import com.example.model.MedicineReminder;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

public class DueRemindersServlet extends HttpServlet {
    private SessionFactory sessionFactory;

    @Override
    public void init() throws ServletException {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDateTime now = LocalDateTime.now();
        Session session = sessionFactory.openSession();
        // Fetch reminders due now or earlier
        List<MedicineReminder> due = session.createQuery(
                "from MedicineReminder r where (r.nextReminderTime is not null and r.nextReminderTime <= :now) or (r.nextReminderTime is null and r.reminderTime <= :now)",
                MedicineReminder.class)
            .setParameter("now", now)
            .list();
        session.close();

        JSONArray arr = new JSONArray();
        for (MedicineReminder r : due) {
            JSONObject o = new JSONObject();
            o.put("id", r.getId());
            o.put("name", r.getMedicineName());
            o.put("dosage", r.getDosage());
            o.put("time", r.getNextReminderTime() != null ? r.getNextReminderTime().toString() : r.getReminderTime().toString());
            arr.put(o);
        }
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.write(arr.toString());
    }

    @Override
    public void destroy() {
        if (sessionFactory != null) sessionFactory.close();
    }
}
