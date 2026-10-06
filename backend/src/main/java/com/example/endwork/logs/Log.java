package com.example.endwork.logs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Arrays;

public class Log {
    Path path= Paths.get("src/main/java/com/example/endwork/logs/logs.txt");
    public Log(){
        if(!Files.exists(path)){
            try {
                Files.createFile(path);
            } catch (IOException e) {
                System.out.println(e);;
            }
        }
    }
    public void writeLine(String str){
        try {
            String temp= LocalDateTime.now() +str+"\n";
            Files.write(path,temp.getBytes(), StandardOpenOption.APPEND);
        }catch (Exception e){
            System.out.println(e);
        }
    }
    public String readAll(){
        StringBuilder sb = new StringBuilder();
        char[] bt=new char[1024];
        try(InputStreamReader reader=new InputStreamReader(Files.newInputStream(path))) {
            while(reader.read(bt)!=-1){
                sb.append(new String(bt).replaceAll("\0",""));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString().trim();
    }
}
