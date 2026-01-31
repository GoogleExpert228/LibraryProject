package org.example.library.daos.impl;

import org.hibernate.SessionFactory;

import java.lang.reflect.Field;

/**
 * Helper class for testing DAO implementations.
 * Provides utilities to create DAO instances and set the sessionFactory field
 * without triggering HibernateUtil static initialization.
 */
public class DaoTestHelper {

    /**
     * Sets the sessionFactory field on a DAO instance using reflection.
     * This method removes the final modifier and sets the field value.
     *
     * @param dao the DAO instance
     * @param sessionFactory the mock SessionFactory to set
     * @throws Exception if reflection fails
     */
    public static void setSessionFactory(Object dao, SessionFactory sessionFactory) throws Exception {
        Field field = GenericHibernateDao.class.getDeclaredField("sessionFactory");
        field.setAccessible(true);
        
        // Remove final modifier (works in Java 8-11, may need different approach in Java 12+)
        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            int modifiers = field.getModifiers();
            modifiersField.setInt(field, modifiers & ~java.lang.reflect.Modifier.FINAL);
        } catch (NoSuchFieldException e) {
            // In Java 12+, modifiers field might not be accessible
            // Try to set the field anyway
        }
        
        field.set(dao, sessionFactory);
    }

    /**
     * Creates a DAO instance without triggering static initialization.
     * Uses Unsafe to allocate the instance without calling the constructor.
     *
     * @param daoClass the DAO class to instantiate
     * @param <T> the DAO type
     * @return a new instance of the DAO
     * @throws Exception if instantiation fails
     */
    @SuppressWarnings("unchecked")
    public static <T> T createInstanceWithoutInitialization(Class<T> daoClass) throws Exception {
        try {
            // Try using Unsafe to allocate instance without calling constructor
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Object unsafe = unsafeField.get(null);
            
            java.lang.reflect.Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
            return (T) allocateInstance.invoke(unsafe, daoClass);
        } catch (Exception e) {
            // Fallback: try normal instantiation
            // This will fail if static initialization fails, but we'll handle it
            try {
                return daoClass.getDeclaredConstructor().newInstance();
            } catch (ExceptionInInitializerError | NoClassDefFoundError initError) {
                throw new RuntimeException(
                    "Failed to create DAO instance. Static initialization failed. " +
                    "Unsafe allocation also failed: " + e.getMessage() + 
                    ". Make sure to add --add-opens java.base/sun.misc=ALL-UNNAMED to JVM arguments.", e);
            }
        }
    }
}

