package ru.job4j.todolist.persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.job4j.todolist.model.Task;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

public class ToDoStore implements Store, AutoCloseable {
    private final static ToDoStore INST = new ToDoStore();
    private final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
            .configure().build();
    private final SessionFactory sf = new MetadataSources(registry)
            .buildMetadata().buildSessionFactory();

    private ToDoStore() {
    }

    public static ToDoStore instOf() {
        return INST;
    }

    @Override
    public void close() {
        StandardServiceRegistryBuilder.destroy(registry);
    }

    @Override
    public Collection<Task> getAllTasks() {
        List<Task> tasks;
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            tasks = session.createQuery("from Task ").list();
            session.getTransaction().commit();
        }
        return tasks;
    }

    @Override
    public Collection<Task> getNewTasks() {
        List<Task> tasks;
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            tasks = session.createQuery("from Task where status = false ").list();
            session.getTransaction().commit();
        }
        return tasks;
    }

    @Override
    public Task getTaskById(int id) {
        Task rsl;
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            rsl = session.get(Task.class, id);
            session.getTransaction().commit();
        }
        return rsl;
    }

    @Override
    public int addTask(Task task) {
        int rsl;
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            rsl = (Integer) session.save(task);
            session.getTransaction().commit();
        }
        return rsl;
    }

    @Override
    public void updateTaskStatus(int id) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            Task existing = session.get(Task.class, id);
            existing.setStatus(true);
            session.update(existing);
            session.getTransaction().commit();
        }
    }

    @Override
    public void deleteTask(int id) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            Task deleting = new Task();
            deleting.setId(id);
            session.delete(deleting);
            session.getTransaction().commit();
        }
    }

    public static void main(String[] args) {
//        for (int i = 0; i <10 ; i++) {
//            Task task1 = new Task();
//            task1.setDescription("task2");
//            task1.setCreated(new Timestamp(System.currentTimeMillis()));
//            ToDoStore.instOf().addTask(task1);
//        }
        System.out.println(ToDoStore.instOf().getNewTasks());
//        ToDoStore.instOf().getAllTasks().forEach(System.out::println);
//        ToDoStore.instOf().updateTaskStatus(1);
    }
}
