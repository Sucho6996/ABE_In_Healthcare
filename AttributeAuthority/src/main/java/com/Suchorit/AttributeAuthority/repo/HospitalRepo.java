package com.Suchorit.AttributeAuthority.repo;

import com.Suchorit.AttributeAuthority.model.Hospitals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalRepo extends JpaRepository<Hospitals,String> {
}
