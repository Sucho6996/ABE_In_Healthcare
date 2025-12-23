package com.Suchorit.Hospital.service;



import com.Suchorit.Hospital.model.Hospitals;
import com.Suchorit.Hospital.model.UserPrinciple;
import com.Suchorit.Hospital.repo.HospitalRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    HospitalRepo repo;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       Hospitals user =repo.findById(username).get();
        if (user==null){
            System.out.println("404 not found");
            throw new UsernameNotFoundException("Username 404");
        }
        return new UserPrinciple(user);
    }
}