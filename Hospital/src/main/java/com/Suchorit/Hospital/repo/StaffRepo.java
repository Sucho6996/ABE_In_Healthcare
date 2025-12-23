package com.Suchorit.Hospital.repo;

import com.Suchorit.Hospital.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepo extends JpaRepository<Staff,String> {
    List<Staff> findAllByHospitalId(String hosId);
}
