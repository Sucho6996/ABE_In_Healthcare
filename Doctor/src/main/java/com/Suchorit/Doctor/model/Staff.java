package com.Suchorit.Doctor.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Staff{
    @Id
    protected String regNo;
    protected String name;
    protected String designation;
    @Nullable
    protected String specialization;
    private String pass;
    @Nullable
    protected String HospitalId;
    private String lastLoginTime;
    @Nullable
    private String publicKey;

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }


    public String getPass() {
            return pass;
        }

        public String getHospitalId() {
            return HospitalId;
        }

        public void setHospitalId(String hospitalId) {
            HospitalId = hospitalId;
        }

        public String getRegNo() {
            return regNo;
        }

        public void setRegNo(String regNo) {
            this.regNo = regNo;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesignation() {
            return designation;
        }

        public void setDesignation(String designation) {
            this.designation = designation;
        }

        @Nullable
        public String getSpecialization() {
            return specialization;
        }

        public void setSpecialization(@Nullable String specialization) {
            this.specialization = specialization;
        }

        public void setPass(String pass) {
            this.pass = pass;
        }

    @Nullable
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(@Nullable String publicKey) {
        this.publicKey = publicKey;
    }
}
