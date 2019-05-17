/*
Copyright (c) 2012 Marco Amadei.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package net.ucanaccess.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class ExecuteUpdate extends AbstractExecute {
    public ExecuteUpdate(UcanaccessPreparedStatement statement) {
        super(statement);
    }

    ExecuteUpdate(UcanaccessStatement statement) {
        super(statement);
    }

    public ExecuteUpdate(UcanaccessStatement statement, String sql) {
        super(statement, sql);
    }

    public ExecuteUpdate(UcanaccessStatement statement, String sql, int autoGeneratedKeys) {
        super(statement, sql, autoGeneratedKeys);
    }

    public ExecuteUpdate(UcanaccessStatement statement, String sql, int[] indexes) {
        super(statement, sql, indexes);
    }

    public ExecuteUpdate(UcanaccessStatement statement, String sql, String[] columnNames) {
        super(statement, sql, columnNames);
    }

    public int execute() throws SQLException {
        return (Integer) executeBase();
    }

    public int[] executeBatch() throws SQLException {
        this.commandType = CommandType.BATCH;
        return (int[]) executeBase();
    }

    @Override
    public Object executeWrapped() throws SQLException {
        Statement w = super.getWrappedStatement();
        switch (commandType) {
        case BATCH:
            return w.executeBatch();
        case PREPARED_STATEMENT:
            return ((PreparedStatement) w).executeUpdate();
        case NO_ARGUMENTS:
            return w.executeUpdate(sql);
        case WITH_COLUMNS_NAME:
            return w.executeUpdate(sql, columnNames);
        case WITH_AUTO_GENERATED_KEYS:
            return w.executeUpdate(sql, autoGeneratedKeys);
        case WITH_INDEXES:
            return w.executeUpdate(sql, indexes);
        default:
            return 0;
        }
    }
}
