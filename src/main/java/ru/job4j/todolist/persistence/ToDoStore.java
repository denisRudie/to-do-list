package ru.job4j.todolist.persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.job4j.todolist.model.Category;
import ru.job4j.todolist.model.Task;
import ru.job4j.todolist.model.User;

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
    public Collection<Task> getAllTasksByUser(User user) {
        return tx(
                session -> session.createQuery(
                        "select distinct t from Task t left join fetch t.categories " +
                                "where t.owner = :user")
                        .setParameter("user", user)
                        .list()
        );
    }

    public void addCat(Category cat) {
        consume(session -> session.save(cat));
    }

    @Override
    public Collection<Task> getNewTasksByUser(User user) {
        return tx(
                session -> session.createQuery(
                        "select distinct t from Task t left join fetch t.categories " +
                                "where t.owner = :user and t.status = false")
                        .setParameter("user", user)
                        .list()
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

    @Override
    public int addUser(User user) {
        return tx(session -> (Integer) session.save(user));
    }

    @Override
    public User getUserById(int id) {
        return tx(session -> session.get(User.class, id));
    }

    @Override
    public User getUserByEmail(String email) {
        return tx(
                session -> (User) session.createQuery("from User where email = :email")
                        .setParameter("email", email)
                        .uniqueResult());
    }

    @Override
    public Category getCatById(int id) {
        return tx(
                session -> session.get(Category.class, id)
        );
    }

    @Override
    public Collection<Category> getAllCategories() {
        return tx(
                session -> session.createQuery("from Category ").list()
        );
    }

    public static void main(String[] args) {
//        for (int i = 0; i <10 ; i++) {
//            Task task1 = new Task();
//            task1.setDescription("task2");
//            task1.setCreated(new Timestamp(System.currentTimeMillis()));
//            ToDoStore.instOf().addTask(task1);
//        }

//        User u = ToDoStore.instOf().getUserById(1);
//        User u = new User();
//        u.setName("123");
//        u.setPassword("1321");
//        ToDoStore.instOf().addUser(u);
//        ToDoStore.instOf().addUser(u);

//        Category cat1 = new Category();
//        Category cat2 = new Category();
//        cat1.setName("cat1");
//        cat2.setName("cat2");
//        ToDoStore.instOf().addCat(cat1);
//        ToDoStore.instOf().addCat(cat2);

//        Task t = new Task();
//        t.setCreated(new Timestamp(System.currentTimeMillis()));
//        t.setDescription("0");
//        t.setOwner(ToDoStore.instOf().getUserById(1));
//        t.addCat(ToDoStore.instOf().getCatById(1));
//        t.addCat(ToDoStore.instOf().getCatById(2));
//        ToDoStore.instOf().addTask(t);

//        ToDoStore.instOf().getAllTasksByUser(
//                ToDoStore.instOf().getUserById(1))
//                .forEach(System.out::println);
    }
}
