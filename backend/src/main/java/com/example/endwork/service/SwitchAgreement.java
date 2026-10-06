package com.example.endwork.service;

import com.example.endwork.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SwitchAgreement {
    final
    InitService initService;

    public SwitchAgreement(InitService initService) {
        this.initService = initService;
    }

    @RequestMapping("/Agreement")
    public String switchAgreement(@RequestParam("agreement") boolean agreement){
        if(!initService.isinit){
            if(Page.AllPage.isTcp!=agreement){
                initService.doInit();
                Page.switchAgree();
            }
        }else {
            return "isinit";
        }
        return "ok";
    }
}
