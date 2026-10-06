package com.example.endwork.basecommand;

import com.example.endwork.struct.IEntityStruct;
import org.yaml.snakeyaml.util.Tuple;

import java.sql.SQLException;

public class UpdateCommand implements ICommand{
    private final DataBase db;
    private EntityMemento entityMemento;
    private final IEntityStruct struct;
    boolean isUndo;

    public UpdateCommand( IEntityStruct struct,DataBase db) {
        this.struct = struct;
        this.db=db;
    }

    @Override
    public void execute() throws SQLException {
        isUndo=false;
        Tuple<Integer,IEntityStruct> item=db.selectItem(struct.getId(),struct.getTableName());
        entityMemento=new EntityMemento(item._1(),item._2());
        db.updateData(struct.getId(),struct);
    }

    @Override
    public void undo() throws SQLException {
        isUndo=true;
        if(entityMemento.getStruct()!=null){
            db.updateData(entityMemento.getId(),entityMemento.getStruct());
        }
    }

    @Override
    public String toString() {
        if(isUndo){
            return "还原了表"+entityMemento.getStruct().getTableName()+"的id为"+entityMemento.getId()+"的行";
        }else {
            return "更改了表"+entityMemento.getStruct().getTableName()+"的id为"+entityMemento.getId()+"的行";
        }
    }
}
