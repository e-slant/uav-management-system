package com.example.endwork.Communication;

import com.example.endwork.struct.DataType;

public interface IComm {
    void send(DataType dt);
    DataType receive();
    void close();
}
