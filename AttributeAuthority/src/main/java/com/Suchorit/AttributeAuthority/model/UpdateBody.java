package com.Suchorit.AttributeAuthority.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ModelAttribute;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateBody {
    private String adharNo;
    private String phNo;
    private String pass;
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

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }


}
