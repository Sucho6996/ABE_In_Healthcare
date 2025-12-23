package com.Suchorit.Doctor.repo;


import com.Suchorit.Doctor.model.Key;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyRepo extends JpaRepository<Key,String> {

}
