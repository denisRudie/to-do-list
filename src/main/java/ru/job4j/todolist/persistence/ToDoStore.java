package ru.job4j.todolist.persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.job4j.todolist.model.Task;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

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

    private <T> T tx(final Function<Session, T> command) {
        T rsl;
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            rsl = command.apply(session);
            session.getTransaction().commit();
        }
        return rsl;
    }

    private void consume(final Consumer<Session> command) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            command.accept(session);
            session.getTransaction().commit();
        }
    }

    @Override
    public Collection<Task> getAllTasks() {
        return tx(
                session -> session.createQuery("from Task ").list()
        );
    }

    @Override
    public Collection<Task> getNewTasks() {
        return tx(
                session -> session.createQuery("from Task where status = false ").list()
        );
    }

    @Override
    public Task getTaskById(int id) {
        return tx(
                session -> session.get(Task.class, id)
        );
    }

    @Override
    public int addTask(Task task) {
        return tx(
                session -> (Integer) session.save(task)
        );
    }

    @Override
    public void updateTaskStatus(int id) {
        consume(session -> {
            Task existing = session.get(Task.class, id);
            existing.setStatus(true);
            session.update(existing);
        });
    }

    @Override
    public void deleteTask(int id) {
        consume(session -> {
            Task deleting = new Task();
            deleting.setId(id);
            session.delete(deleting);
        });
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
