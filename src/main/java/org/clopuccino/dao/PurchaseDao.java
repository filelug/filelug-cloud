package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.Purchase;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>PurchaseDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class PurchaseDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(PurchaseDao.class.getSimpleName());


    public PurchaseDao() {
        super();
    }

    public PurchaseDao(DatabaseAccess dbAccess) {
        super(dbAccess);
    }


    public boolean createTableIfNotExists() {
        boolean success = true;

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dbAccess.getConnection();

            DatabaseMetaData dbMetaData = conn.getMetaData();

            /* check if table purchase exists */
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_PURCHASE, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_PURCHASE);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_PURCHASE_PRODUCT_ID);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_PURCHASE_AUTH_USER_ID);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_PURCHASE_VENDOR_USER_ID);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_PURCHASE, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(rs, statement, null, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return success;
    }

    public boolean existingPurchaseById(String purchaseId) {
        boolean exists = false;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_PURCHASE_BY_ID);

            pStatement.setString(1, purchaseId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                exists = true;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding if purchase exists for id '%s'\nerror message:\n%s", purchaseId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return exists;
    }

    public Purchase findPurchaseById(String purchaseId) {
        Purchase record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_PURCHASE_BY_ID);

            pStatement.setString(1, purchaseId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = new Purchase();

                record.setPurchaseId(resultSet.getString(DatabaseConstants.COLUMN_NAME_PURCHASE_ID));
                record.setProductId(resultSet.getString(DatabaseConstants.COLUMN_NAME_PURCHASE_ID));
                record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
                record.setQuantity(resultSet.getLong(DatabaseConstants.COLUMN_NAME_PURCHASE_QUANTITY));
                record.setVendorTransactionId(resultSet.getString(DatabaseConstants.COLUMN_NAME_VENDOR_TRANSACTION_ID));
                record.setVendorUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_VENDOR_USER_ID));
                record.setPurchaseTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_PURCHASE_TIMESTAMP));
            }
        } catch (Exception e) {
            record = null;

            LOGGER.error(String.format("Error on finding purchase for id '%s'\nerror message:\n%s", purchaseId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return record;
    }

    /* ordered by purchase timestamp, DESC */
    public List<Purchase> findPurchasessByUserId(String userId) {
        List<Purchase> purchases = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_PURCHASES_BY_USER);

            pStatement.setString(1, userId);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                Purchase record = new Purchase();

                record.setPurchaseId(resultSet.getString(DatabaseConstants.COLUMN_NAME_PURCHASE_ID));
                record.setProductId(resultSet.getString(DatabaseConstants.COLUMN_NAME_PRODUCT_ID));
                record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
                record.setQuantity(resultSet.getLong(DatabaseConstants.COLUMN_NAME_PURCHASE_QUANTITY));
                record.setVendorTransactionId(resultSet.getString(DatabaseConstants.COLUMN_NAME_VENDOR_TRANSACTION_ID));
                record.setVendorUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_VENDOR_USER_ID));
                record.setPurchaseTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_PURCHASE_TIMESTAMP));

                purchases.add(record);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding purchase for user '%s'\nerror message:\n%s", userId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return purchases;
    }

    public Purchase createPurchase(Purchase purchase, boolean increaseTransmissionCapacity) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        boolean success = true;

        String purchaseId = purchase.getPurchaseId();
        String productId = purchase.getProductId();
        String userId = purchase.getUserId();
        Long quantity = purchase.getQuantity();
        String vendorTransactionId = purchase.getVendorTransactionId();
        String vendorUserId = purchase.getVendorUserId();
        Long purchaseTimestamp = purchase.getPurchaseTimestamp();

        try {
            conn = dbAccess.getConnection();

            if (increaseTransmissionCapacity) {
                conn.setAutoCommit(false);
            }

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_PURCHASE);
            pStatement.setString(1, purchaseId);
            pStatement.setString(2, productId);
            pStatement.setString(3, userId);
            pStatement.setLong(4, quantity);
            pStatement.setString(5, vendorTransactionId);
            pStatement.setString(6, vendorUserId);
            pStatement.setLong(7, purchaseTimestamp);

            pStatement.executeUpdate();

            if (increaseTransmissionCapacity) {
                UserDao userDao = new UserDao(dbAccess);

                long currentAvailableTransferBytes = userDao.findAvailableTransferBytesForUser(userId);

                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_AVAILABLE_TRANSFER_BYTES_BY_USER);

                long newAvailableTransferBytes = BigDecimal.valueOf(currentAvailableTransferBytes).add(BigDecimal.valueOf(DatabaseConstants.DEFAULT_PURCHASE_CAPACITY_BYTES_PER_QUANTITY).multiply(BigDecimal.valueOf(quantity))).longValue();

                pStatement.setLong(1, newAvailableTransferBytes);
                pStatement.setString(2, userId);

                int count = pStatement.executeUpdate();

                if (count > 0) {
                    conn.commit();

                    LOGGER.info(String.format("Update available transfer bytes to %s for user: '%s'", String.valueOf(newAvailableTransferBytes), userId));
                }
            }
        } catch (Exception e) {
            success = false;

            if (increaseTransmissionCapacity && conn != null) {
                try {
                    conn.rollback();
                } catch (Exception e1) {
                    /* ignored */
                }
            }

            LOGGER.error(String.format("Error on create purchase\n'%s'\nerror message:\n%s", purchase.toString(), e.getMessage()), e);
        } finally {
            if (conn != null) {
                try {
                    if (!conn.getAutoCommit()) {
                        conn.setAutoCommit(true);
                    }
                } catch (Exception e) {
                    /* ignored */
                }
            }

            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        if (success) {
            return findPurchaseById(purchaseId);
        } else {
            return null;
        }
    }
}
