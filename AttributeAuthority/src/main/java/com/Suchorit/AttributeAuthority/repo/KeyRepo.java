package com.Suchorit.AttributeAuthority.repo;

import com.Suchorit.AttributeAuthority.model.Key;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyRepo extends JpaRepository<Key,String> {
}
