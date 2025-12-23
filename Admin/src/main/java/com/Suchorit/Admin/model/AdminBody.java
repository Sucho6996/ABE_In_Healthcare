package com.Suchorit.Admin.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminBody {
    @Id
    private String adharNo;
    private String phNo;
    private String password;

    public String getAdharNo() {
        return adharNo;
    }

    public void setAdharNo(String adharNo) {
        this.adharNo = adharNo;
    }

    public String getPhNo() {
        return phNo;
    }

    public void setPhNo(String phNo) {
        this.phNo = phNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
