package com.Suchorit.Doctor.repo;

import com.Suchorit.Doctor.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<Staff,String> {

    Staff findByregNo(String username);
}
