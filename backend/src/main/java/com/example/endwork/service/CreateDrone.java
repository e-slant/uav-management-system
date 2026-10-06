package com.example.endwork.service;

import com.example.endwork.Page;
import com.example.endwork.struct.DataType;
import com.example.endwork.struct.Position;
import com.example.endwork.struct.TempData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CreateDrone {
    final InitService initService;

    public CreateDrone(InitService initService) {
        this.initService = initService;
    }

    //success
    @RequestMapping("/createDrone")
    public String createDrone(@RequestBody Map<String,String> map){
        if(map.isEmpty()){
            return "error";
        }else {
            double lon=Double.parseDouble(map.get("lon"));
            double lat=Double.parseDouble(map.get("lat"));
            String number=map.get("number");
            double speed=Double.parseDouble(map.get("speed"));
            if(Page.AllPage.isTcp){
                initService.tcpService.start();
                DataType dt=new DataType(new Position(lon,lat));
                dt.setSpeed(speed);
                dt.setNumber(number);
                initService.tcpClient.addDrone(dt);
                initService.tcpClient.start();
            }else {
                initService.udpService.start();
                DataType dt=new DataType(new Position(lon,lat));
                dt.setSpeed(speed);
                dt.setNumber(number);
                initService.droneGroup.addDrone(dt);
                initService.droneGroup.start();
            }
            return "success";
        }
    }
}
