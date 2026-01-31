package org.example.library.services;

import org.example.library.contracts.LibraryService;

import java.util.HashMap;
import java.util.Map;

public class ServiceFactory {
    private static Map<Class<? extends LibraryService>, LibraryService> services = new HashMap<>();

    public static <T extends LibraryService> T service(Class<T> clazz) {
        //if already in the map just return it
        if(services.containsKey(clazz)) {
            return (T) services.get(clazz);
        }

        //if not then add it to the map and only then return it
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            services.put(clazz, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Could not create service: " + clazz, e);
        }
    }
}
