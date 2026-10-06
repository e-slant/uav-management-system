package com.example.endwork.service;

import com.example.endwork.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ServicePage {

    final
    InitService initService;

    public ServicePage(InitService initService) {
        this.initService = initService;
    }

    @RequestMapping("/ServicePage")
    public void page(@RequestBody Map<String,String> map){
        initService.doInit();
        Page.AllPage.database=map.get("database");
        Page.AllPage.userName=map.get("userName");
        Page.AllPage.password=map.get("password");
        Page.AllPage.tableName=map.get("tableName").split(",");
        Page.AllPage.host=map.get("host");
        Page.AllPage.port=map.get("port");

    }
}
