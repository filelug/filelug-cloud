package org.clopuccino.dao;

import org.clopuccino.DatabaseUtility;
import org.clopuccino.db.DatabaseAccess;

import java.util.List;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class AbstractDao {

    protected DatabaseAccess dbAccess;

    public AbstractDao() {
        dbAccess = DatabaseUtility.createDatabaseAccess();
    }

    public AbstractDao(DatabaseAccess dbAccess) {
        if (dbAccess == null) {
            this.dbAccess = DatabaseUtility.createDatabaseAccess();
        } else {
            this.dbAccess = dbAccess;
        }
    }

    // Convert to string like "'AAA', 'BBB', 'CCC'"
    // without the double-quotes '"' in the beginning and the end of the string.
    // If the element in stringList is null or empty, skips it.
    public String convertToInClauseStringFrom(List<String> stringList) {
        StringBuilder buffer = new StringBuilder("");

        if (stringList != null && stringList.size() > 0) {
            int size = stringList.size();

            int seq = 0;
            for (String aString : stringList) {
                seq++;

                if (aString != null && aString.trim().length() > 0) {
                    buffer.append("'");
                    buffer.append(aString);
                    buffer.append("'");

                    if (size > seq) {
                        buffer.append(", ");
                    }
                }
            }
        } else {
            buffer.append("''");
        }

        return buffer.toString();
    }

}
