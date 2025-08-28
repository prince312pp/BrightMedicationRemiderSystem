package com.example.servlet;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DeleteReminderServlet extends HttpServlet {
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
            session.beginTransaction();
            Object reminder = session.get("com.example.model.MedicineReminder", id);
            if (reminder != null) {
                session.delete(reminder);
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
