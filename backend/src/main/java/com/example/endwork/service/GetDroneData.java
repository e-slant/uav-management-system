package com.example.endwork.service;

import com.example.endwork.struct.DataType;
import com.example.endwork.struct.TempData;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class GetDroneData {
    private TempData tempData;
    public GetDroneData(TempData tempData){
        this.tempData=tempData;
    }
    @RequestMapping("/droneData")
    public List< DataType> droneData(){
        return new ArrayList<>(tempData.getDtAll().values());
    }
}
