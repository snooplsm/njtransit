package com.gent.mp;

import java.sql.Connection;

public interface Transactional {
	public void work(Connection conn);
}