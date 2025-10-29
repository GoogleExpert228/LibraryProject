package org.example.library.configs;

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

            configuration.setProperty("hibernate.connection.url", System.getenv("DB_URL"));
            configuration.setProperty("hibernate.connection.username", System.getenv("DB_USER"));
            configuration.setProperty("hibernate.connection.password", System.getenv("DB_PASS"));

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
