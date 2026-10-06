package com.example.endwork.service;

import com.example.endwork.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ClearDrone {
    final
    InitService initService;

    public ClearDrone(InitService initService) {
        this.initService = initService;
    }
    @RequestMapping("clearDrone")
    public void clearDrone(@RequestBody Map<String,String> result){
        if(result!=null){
            if(Page.AllPage.isTcp){
                initService.tcpClient.removeDrone(result.get("number"));
                initService.tcpClient.stop();
                initService.tcpService.stop();
            }else {

                initService.droneGroup.removeDrone(result.get("number"));

            }
        }
    }
}
