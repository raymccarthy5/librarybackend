package com.x00179223.librarybackend.repository;

import com.x00179223.librarybackend.model.Role;
import com.x00179223.librarybackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository <User, Long>{

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    Optional<User> findById(Long id);

}
