package com.example.servlet;

import com.example.util.MedicineApiUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class MedicineSuggestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String query = req.getParameter("q");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        if (query == null || query.trim().length() < 2) {
            out.write("[]");
            return;
        }
        List<String> suggestions = MedicineApiUtil.searchMedicineNames(query.trim());
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < suggestions.size(); i++) {
            String name = suggestions.get(i).replace("\\", "\\\\").replace("\"", "\\\"");
            sb.append('"').append(name).append('"');
            if (i < suggestions.size() - 1) sb.append(',');
        }
        sb.append(']');
        out.write(sb.toString());
    }
}
