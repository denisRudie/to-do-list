package ru.job4j.todolist.servlet;

import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.job4j.todolist.model.Task;
import ru.job4j.todolist.persistence.ToDoStore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ToDoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (PrintWriter out = resp.getWriter()) {
            String action = req.getParameter("action");

            switch (action) {
                case "new" -> {
                    resp.setCharacterEncoding("UTF-8");
                    Collection<Task> tasks =
                            ToDoStore.instOf()
                                    .getNewTasks().stream()
                                    .sorted(Comparator.comparing(Task::getId))
                                    .collect(Collectors.toList());
                    String jsonOut = new Gson().toJson(tasks);
                    out.write(jsonOut);
                    out.flush();
                }
                case "all" -> {
                    resp.setCharacterEncoding("UTF-8");
                    Collection<Task> tasks =
                            ToDoStore.instOf()
                                    .getAllTasks().stream()
                                    .sorted(Comparator.comparing(Task::getId))
                                    .collect(Collectors.toList());
                    String jsonOut = new Gson().toJson(tasks);
                    out.write(jsonOut);
                    out.flush();
                }
            }
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (BufferedReader read = req.getReader();
             PrintWriter out = resp.getWriter()) {
            StringBuilder fullLine = new StringBuilder();
            String oneLine;
            while ((oneLine = read.readLine()) != null) {
                fullLine.append(oneLine);
            }
            JSONObject json = (JSONObject) new JSONParser().parse(fullLine.toString());

            String action = (String) json.get("action");
            String stringId = (String) json.get("id");
            String desc = (String) json.get("desc");

            switch (action) {
                case "add" -> {
                    resp.setCharacterEncoding("UTF-8");
                    Task task = new Task();
                    task.setDescription(desc);
                    task.setCreated(new Timestamp(System.currentTimeMillis()));
                    int newTaskId = ToDoStore.instOf().addTask(task);
                    task.setId(newTaskId);
                    String jsonOut = new Gson().toJson(task);
                    out.write(jsonOut);
                    out.flush();
                }
                case "delete" -> ToDoStore.instOf().deleteTask(Integer.parseInt(stringId));
                case "update" -> ToDoStore.instOf().updateTaskStatus(Integer.parseInt(stringId));
            }
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
