package com.example.endwork.basecommand;

import java.sql.SQLException;

public interface ICommand {
    void execute() throws SQLException;
    void undo()throws SQLException;
}
