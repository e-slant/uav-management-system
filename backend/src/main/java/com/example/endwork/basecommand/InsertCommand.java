package com.example.endwork.basecommand;


import com.example.endwork.struct.IEntityStruct;

import java.sql.SQLException;

public class InsertCommand implements ICommand {
    private EntityMemento entityMemento;
    private final IEntityStruct struct;
    private final DataBase dataBase;
    private boolean isUndo = false;
    public InsertCommand(IEntityStruct struct, DataBase dataBase) {
        this.struct = struct;
        this.dataBase = dataBase;
    }
    @Override
    public void execute() throws SQLException {
        isUndo = false;
        int id=dataBase.getId(struct.getTableName())+1;
        entityMemento=new EntityMemento(id,struct);
        dataBase.insertData(id,struct);
    }
    @Override
    public void undo() throws SQLException{
        isUndo = true;
        if(entityMemento!=null&&entityMemento.getId()>=0){
            dataBase.deleteData(entityMemento.getId(),entityMemento.getStruct().getTableName());
        }
    }

    @Override
    public String toString() {
        if(isUndo){
            return "撤销了插入到表"+entityMemento.getStruct().getTableName()+"，id为"+entityMemento.getId()+"的数据";
        }else {
            return "插入数据到表"+entityMemento.getStruct().getTableName()+"，id为"+entityMemento.getId();
        }
    }
}
