package com.example.endwork.basecommand;

import com.example.endwork.logs.Log;

import java.util.Stack;

public class CommandInvoker {
    private Stack<ICommand> stack=new Stack<>();
    private final Log log=new Log();
    public void execute(ICommand command){
        stack.push(command);
        try {
            command.execute();
            log.writeLine(command.toString());
        }catch(Exception e){
            System.out.println(e);
        }
    }
    public void undo(){
        if(!stack.isEmpty()){
            try {
                ICommand command =stack.pop();
                command.undo();
                log.writeLine(command.toString());
            }catch(Exception e){
                System.out.println(e);
            }
        }else {
            System.out.println("没有可撤销的命令");
        }

    }
}
