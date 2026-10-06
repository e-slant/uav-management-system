package com.example.endwork.basecommand;

import com.example.endwork.struct.IEntityStruct;
import org.yaml.snakeyaml.util.Tuple;

import java.sql.SQLException;

public class DeleteCommand implements ICommand{
    private final DataBase db;
    private EntityMemento entityMemento;
    private final String tableName;
    int id;
    private boolean isUndo;
    public DeleteCommand( DataBase db,int id,String tableName) {
        this.id = id;
        this.db=db;
        this.tableName = tableName;
    }

    @Override
    public void execute() throws SQLException {
        isUndo=false;
        Tuple<Integer,IEntityStruct> item=db.selectItem(id,tableName);
        entityMemento=new EntityMemento(item._1(),item._2());
        db.deleteData(id,tableName);
    }

    @Override
    public void undo() throws SQLException {
        isUndo=true;
        if(entityMemento.getStruct()!=null){
            db.insertData(entityMemento.getId(),entityMemento.getStruct());
        }
    }

    @Override
    public String toString() {
        if(isUndo){
            return "还原了表"+entityMemento.getStruct().getTableName()+"id为"+entityMemento.getId()+"的数据";
        }else {
            return "删除了表"+entityMemento.getStruct().getTableName()+"id为"+entityMemento.getId()+"的数据";
        }
    }
}
