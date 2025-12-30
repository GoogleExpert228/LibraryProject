package org.example.library.configs;

import org.example.library.entities.Admin;
import org.example.library.entities.Book;
import org.example.library.entities.Borrow;
import org.example.library.entities.FormRequest;
import org.example.library.entities.Notification;
import org.example.library.entities.Operator;
import org.example.library.entities.Reader;
import org.example.library.entities.User;
import org.example.library.entities.UserRating;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {

    private static final SessionFactory sessionFactory;

    static {
        try {
            Configuration configuration = new Configuration();

            configuration.configure("hibernate.cfg.xml");
            /*
             * Relying solely on hibernate.cfg.xml mappings proved brittle once we switched
             * the application to the Java module path – the abstract User base class was no
             * longer discovered and Hibernate refused to treat it as an entity. Registering
             * all annotated classes programmatically guarantees they end up in the
             * Metamodel regardless of how the configuration file is loaded.
             */
            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(Admin.class);
            configuration.addAnnotatedClass(Operator.class);
            configuration.addAnnotatedClass(Reader.class);
            configuration.addAnnotatedClass(Book.class);
            configuration.addAnnotatedClass(Borrow.class);
            configuration.addAnnotatedClass(FormRequest.class);
            configuration.addAnnotatedClass(Notification.class);
            configuration.addAnnotatedClass(UserRating.class);
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            sessionFactory = configuration.buildSessionFactory(serviceRegistry);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("Hibernate initialization error: " + e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

}
