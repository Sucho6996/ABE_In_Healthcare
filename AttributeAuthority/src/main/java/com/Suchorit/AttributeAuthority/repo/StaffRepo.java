package com.Suchorit.AttributeAuthority.repo;

import com.Suchorit.AttributeAuthority.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

@Repository
public interface StaffRepo extends JpaRepository<Staff,String> {
    void deleteAllByHospitalId(String id);
}
