package com.example.endwork.basecommand;


import com.example.endwork.struct.IEntityStruct;

public class EntityMemento {
    private IEntityStruct struct;

    private int id;
    public EntityMemento(IEntityStruct struct) {
        this.struct = struct==null?null:struct.renew();
    }
    public EntityMemento(int id) {
        this.id = id;
    }
    public EntityMemento( int id,IEntityStruct struct) {
        this.struct = struct==null?null:struct.renew();
        this.id = id;
    }
    public IEntityStruct getStruct() {
        return struct;
    }
    public int getId() {
        return id;
    }
}
