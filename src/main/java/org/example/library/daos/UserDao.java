package org.example.library.daos;

import org.example.library.entities.User;
import org.example.library.enums.Role;
import org.example.library.enums.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserDao extends GenericDao<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);
    List<User> findByStatus(UserStatus status);
}






