package com.example.endwork.service;

import com.example.endwork.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoadPage {
    final
    InitService initService;

    public LoadPage(InitService initService) {
        this.initService = initService;
    }
    @RequestMapping("loadPage")
    public Map<String,String> loadPage(){
        Map<String,String> map = new HashMap<>();
        if(!initService.isinit){
            map.put("host", Page.AllPage.host);
            map.put("port", Page.AllPage.port);
            map.put("database", Page.AllPage.database);
            map.put("table1",Page.AllPage.tableName[0]);
            map.put("table2",Page.AllPage.tableName[1]);
            map.put("username",Page.AllPage.userName);
            map.put("password",Page.AllPage.password);
            if(Page.AllPage.isTcp){
                map.put("protocol","TCP");
            }else {
                map.put("protocol","UDP");
            }
            map.put("state","ok");
        }else {
            map.put("state","isinit");
        }
        return map;
    }
}
