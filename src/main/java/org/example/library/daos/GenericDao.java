package org.example.library.daos;

import java.util.List;
import java.util.Optional;

public interface GenericDao<T, ID> {
    T save(T entity);
    T update(T entity);
    void delete(T entity);
    void deleteById(Class<T> entityClass, ID id);
    Optional<T> findById(Class<T> entityClass, ID id);
    List<T> findAll(Class<T> entityClass);
}






