package com.kopi.belimang.core.repositories;

import com.kopi.belimang.core.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query(nativeQuery = true, value = """
            SELECT *
            FROM users
            WHERE username = :username
            AND is_admin = true 
            """)
    User findAdminByUsername(@Param("username") String username);

    @Query(nativeQuery = true, value = """
            SELECT *
            FROM users
            WHERE username = :username
            AND is_admin = false 
            """)
    User findNonadminByUsername(@Param("username") String username);

    @Query(nativeQuery = true, value = """
            SELECT *
            FROM users
            WHERE email = :email
            AND is_admin = true 
            """)
    User findAdminByEmail(@Param("email") String email);

    @Query(nativeQuery = true, value = """
            SELECT *
            FROM users
            WHERE email = :email
            AND is_admin = false 
            """)
    User findNonadminByEmail(@Param("email") String email);

    @Query(nativeQuery = true, value = """
            SELECT *
            FROM users
            WHERE username = :username 
            """)
    List<User> findByUsername(@Param("username") String username);
}
