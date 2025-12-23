package com.Suchorit.Admin.service;


import com.Suchorit.Admin.model.AdminBody;
import com.Suchorit.Admin.model.UserPrinciple;
import com.Suchorit.Admin.repo.AdminRepo;
import com.Suchorit.Admin.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    AdminRepo repo;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminBody user =repo.findByadharNo(username);
        if (user==null){
            System.out.println("404 not found");
            throw new UsernameNotFoundException("Username 404");
        }
        return new UserPrinciple(user);
    }
}