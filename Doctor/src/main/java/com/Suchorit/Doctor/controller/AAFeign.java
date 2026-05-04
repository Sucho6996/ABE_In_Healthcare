package com.Suchorit.Doctor.controller;

import com.Suchorit.Doctor.model.KeyRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("AttributeAuthority")
public interface AAFeign {
    @PostMapping("/AA/getKey")
    public ResponseEntity<Map<String,String>> retrieve(@RequestParam("role") String role);

    @PostMapping("AA/genStaffAttrKey")
    public ResponseEntity<Map<String, Object>> generateStaffAttributeKeys
            (@RequestBody KeyRequest payload);
}
