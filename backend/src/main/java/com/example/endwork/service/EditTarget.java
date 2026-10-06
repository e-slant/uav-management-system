package com.example.endwork.service;

import com.example.endwork.Page;
import com.example.endwork.struct.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class EditTarget {
    final
    InitService initService;

    public EditTarget(InitService initService) {
        this.initService = initService;
    }
    @RequestMapping("/target")
    public void editTarget(@RequestBody Map<String,String> map){
        String number=map.get("number");
        double x=Double.parseDouble(map.get("x"));
        double y=Double.parseDouble(map.get("y"));
        if(Page.AllPage.isTcp){
            initService.tcpService.send(new Position(x,y));
        }else {
            initService.udpService.send(number,new Position(x,y));
        }
    }
}
