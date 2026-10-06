package com.example.endwork.state;

import com.example.endwork.Context;

public class Normal implements IState {
    @Override
    public void doAction(Context context) {
        context.setState(this);
    }

    @Override
    public String toString() {
        return "00";
    }
}
