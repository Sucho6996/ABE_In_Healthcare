package com.Suchorit.UserService.repo;


import com.Suchorit.UserService.model.PatientDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepo extends JpaRepository<PatientDetails,String> {
    List<PatientDetails> findAllByadharNo(String adharNo);

    List<PatientDetails> findAllByhosId(String hosId);

    List<PatientDetails> findAllByAllowedRoleAndAllowedSpecialization(String allowedRole, String allowedSpecialization);
}
