package com.Suchorit.Admin.repo;

import com.Suchorit.Admin.model.AdminBody;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepo extends JpaRepository<AdminBody,String> {

    AdminBody findByadharNo(String adharNo);
}
