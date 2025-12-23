package com.Suchorit.AttributeAuthority.repo;


import com.Suchorit.AttributeAuthority.model.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<UserData,String> {

    UserData findByadharNo(String adharNo);
}
