package com.Suchorit.Admin.repo;


import com.Suchorit.Admin.model.AdminBody;
import com.Suchorit.Admin.model.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<UserData,String> {

    UserData findByadharNo(String adharNo);
}
