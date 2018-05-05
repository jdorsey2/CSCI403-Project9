package tools;

import data.DatabaseManager;

import java.sql.ResultSet;
import java.util.function.Consumer;

public class QueryRunner extends Thread {
    private Consumer<ResultSet> callback;
    private String query;

    public QueryRunner(String q, Consumer<ResultSet> c) {
        super("Database Thread");
        this.query = q;
        this.callback = c;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        ResultSet results = DatabaseManager.runQuery(query);
        callback.accept(results);
    }
}
