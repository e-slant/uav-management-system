package com.example.endwork.service;

import com.example.endwork.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ServerEdit {
    final
    InitService initService;

    public ServerEdit(InitService initService) {
        this.initService = initService;
    }

    @RequestMapping("saveConfig")
    public void saveConfig(@RequestBody Map<String,String> map){
        Page.AllPage.host=map.get("host");
        Page.AllPage.port=map.get("port");
        Page.AllPage.database=map.get("database");
        Page.AllPage.tableName[0]=map.get("table1");
        Page.AllPage.tableName[1]=map.get("table2");
        Page.AllPage.userName=map.get("username");
        Page.AllPage.password=map.get("password");
        System.out.println(Page.AllPage.port);
        initService.doInit();
    }
}
