package ru.job4j.todolist.persistence;

import ru.job4j.todolist.model.Task;

import java.util.Collection;

public interface Store {

    Collection<Task> getAllTasks();
    Collection<Task> getNewTasks();
    Task getTaskById(int id);
    int addTask(Task task);
    void updateTaskStatus(int id);
    void deleteTask(int id);
}
