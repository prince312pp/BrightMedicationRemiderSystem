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
import java.util.List;

public class ViewRemindersServlet extends HttpServlet {
    private SessionFactory sessionFactory;

    @Override
    public void init() throws ServletException {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Session session = sessionFactory.openSession();
        List<MedicineReminder> reminders = session.createQuery("from MedicineReminder", MedicineReminder.class).list();
        session.close();
        req.setAttribute("reminders", reminders);
        req.getRequestDispatcher("/jsp/viewReminders.jsp").forward(req, resp);
    }

    @Override
    public void destroy() {
        if (sessionFactory != null) sessionFactory.close();
    }
}
