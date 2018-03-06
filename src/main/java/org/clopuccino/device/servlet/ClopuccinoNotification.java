package org.clopuccino.device.servlet;

import java.sql.SQLException;

/**
 * <code>ClopuccinoNotification</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public interface ClopuccinoNotification {

    void send() throws SQLException;

}
