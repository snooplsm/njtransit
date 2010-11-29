package com.gent.mp;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {
	private Connection conn;
	public TransactionManager(Connection conn) {
		this.conn = conn;
	}
	
	public void exec(Transactional t) throws SQLException {
		conn.setAutoCommit(false);
		t.work(conn);
		conn.commit();
	}
}