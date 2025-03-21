package com.transactions.users.repos;

import com.transactions.users.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<Users, Long> {

    Users findByMobileNumberOrEmail(String mobileNumber, String email);
}
