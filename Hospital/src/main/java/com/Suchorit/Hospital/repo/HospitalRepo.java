package com.Suchorit.Hospital.repo;

import com.Suchorit.Hospital.model.Hospitals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalRepo extends JpaRepository<Hospitals,String> {

}
