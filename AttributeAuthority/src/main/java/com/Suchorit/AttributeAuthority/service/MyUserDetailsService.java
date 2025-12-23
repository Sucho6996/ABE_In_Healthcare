package com.Suchorit.AttributeAuthority.service;



import com.Suchorit.AttributeAuthority.model.UserData;
import com.Suchorit.AttributeAuthority.model.UserPrinciple;
import com.Suchorit.AttributeAuthority.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    UserRepo repo;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserData user =repo.findByadharNo(username);
        if (user==null){
            System.out.println("404 not found");
            throw new UsernameNotFoundException("Username 404");
        }
        return new UserPrinciple(user);
    }
}