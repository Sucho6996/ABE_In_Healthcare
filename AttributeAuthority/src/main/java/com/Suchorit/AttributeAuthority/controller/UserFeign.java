package com.Suchorit.AttributeAuthority.controller;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("UserService")
public interface UserFeign {
    @PostMapping("user/getPubKey")
    public ResponseEntity<Map<String,String>> getPubKey(@RequestParam("adharNo") String adharNo);
}
