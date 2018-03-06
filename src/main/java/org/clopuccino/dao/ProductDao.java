package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.clopuccino.Utility;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.Product;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <code>ProductDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ProductDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ProductDao.class.getSimpleName());


    public ProductDao() {
        super();
    }

    public ProductDao(DatabaseAccess dbAccess) {
        super(dbAccess);
    }

    public boolean isProductTableExists() {
        boolean exists = true;

        Connection conn = null;
        ResultSet rs = null;

        try {
            conn = dbAccess.getConnection();

            DatabaseMetaData dbMetaData = conn.getMetaData();

            /* check if table product exists */
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_PRODUCT, new String[]{"TABLE"});

            exists = rs.next();
        } catch (Exception e) {
            exists = false;

            LOGGER.error(String.format("Error on checking if table '%s' exists.\nerror message:\n%s", DatabaseConstants.TABLE_NAME_PRODUCT, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(rs, null, null, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return exists;
    }

    public boolean createTableIfNotExists() {
        boolean success = true;

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dbAccess.getConnection();

            DatabaseMetaData dbMetaData = conn.getMetaData();

            try {
                /* check if table product exists */
                rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_PRODUCT, new String[]{"TABLE"});

                if (!rs.next()) {
                    statement = conn.createStatement();

                    statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_PRODUCT);
                    statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_PRODUCT_VENDOR);
                }
            } catch (Exception e) {
                success = false;

                LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_PRODUCT, e.getMessage()), e);
            }

            /* check if table product_detail exists */
            try {
                rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_PRODUCT_DETAIL, new String[]{"TABLE"});

                if (!rs.next()) {
                    statement = conn.createStatement();

                    statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_PRODUCT_DETAIL);
                    statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_PRODUCT_DETAIL_PRODUCT_ID);
                    statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_PRODUCT_DETAIL_PRODUCT_LOCALE);
                }
            } catch (Exception e) {
                success = false;

                LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_PRODUCT_DETAIL, e.getMessage()), e);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on building connection or accessing database.\nerror message:\n%s", e.getMessage()), e);
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

    /**
     * Deletes all existsing countries and create default ones.
     *
     * @return true if all created successfully; otherwise false.
     */
    public boolean createDefaultProductsIfNotExists() {
        boolean success = true;

        Connection conn = null;
        Statement statement = null;
        PreparedStatement pStatement = null;

        BufferedReader reader = null;

        try {
            ClassLoader classLoader = getClass().getClassLoader();

            InputStream productInputStream = classLoader.getResourceAsStream("/" + DatabaseConstants.FILE_NAME_DEFAULT_PRODUCTS);

            if (productInputStream == null) {
                productInputStream = classLoader.getResourceAsStream(DatabaseConstants.FILE_NAME_DEFAULT_PRODUCTS);
            }

            if (productInputStream == null) {
                throw new FileNotFoundException("Product file not found: " + DatabaseConstants.FILE_NAME_DEFAULT_PRODUCTS);
            }

//            File productFile = new File(System.getProperty("user.dir"), DatabaseConstants.FILE_NAME_DEFAULT_PRODUCTS);
//
//            if (!productFile.exists()) {
//                throw new FileNotFoundException("Product file not found: " + productFile.getAbsolutePath());
//            }

            conn = dbAccess.getConnection();

            statement = conn.createStatement();

            /* create or update products and product-details */
            reader = new BufferedReader(new InputStreamReader(productInputStream, "UTF-8"));

//            reader = new BufferedReader(new InputStreamReader(new FileInputStream(productFile), "UTF-8"));

            /* product or product-detail */
            final String section_product = "product";
            final String section_product_detail = "product-detail";
            final int token_count_for_section_product = 6;
            final int token_count_for_section_product_detail = 7;

            String currentSection = "";

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.trim().length() > 0 && !line.trim().startsWith(DatabaseConstants.DEFAULT_PLAIN_TEXT_FILE_COMMENT_CHARACTER)) {
                    if (line.trim().startsWith(section_product + ":")) {
                        /* start product */

                        currentSection = section_product;

                        continue;
                    } else if ((line.trim().startsWith(section_product_detail + ":"))) {
                        /* start product-detail */

                        currentSection = section_product_detail;

                        continue;
                    } else if (currentSection.equals(section_product)) {
                        /* create or update product */

                        StringTokenizer tokenizer = new StringTokenizer(line, DatabaseConstants.FILE_DEFAULT_PRODUCTS_DELIMITERS);

                        if (tokenizer.countTokens() != token_count_for_section_product) {
                            LOGGER.error("Error format of default products file.\nSection: " + section_product + "\nLine: '" + line + "'");

                            continue;
                        }

                        /*
                         * Given the format of each line:
                         * COLUMN_NAME_PRODUCT_ID
                         * COLUMN_NAME_VENDOR
                         * COLUMN_NAME_VENDOR_PRODUCT_TYPE
                         * COLUMN_NAME_TRANSFER_BYTES
                         * COLUMN_NAME_DISPLAYED_TRANSFER_CAPACITY
                         * COLUMN_NAME_PRODUCT_IS_UNLIMITED
                         */
                        String productId = tokenizer.nextToken().trim();

                        if (!existingProductById(productId)) {
                            /* create */

                            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_PRODUCT);

                            pStatement.setString(1, productId);

                            for (int parameterIndex = 2; tokenizer.hasMoreTokens(); parameterIndex++) {
                                String token = tokenizer.nextToken().trim();

                                if (parameterIndex == 4) {
                                    /* COLUMN_NAME_TRANSFER_BYTES */
                                    pStatement.setLong(parameterIndex, Long.valueOf(token));
                                } else if (parameterIndex == 6) {
                                    /* COLUMN_NAME_PRODUCT_IS_UNLIMITED */
                                    if (token.equalsIgnoreCase("TRUE")) {
                                        pStatement.setBoolean(parameterIndex, true);
                                    } else {
                                        pStatement.setBoolean(parameterIndex, false);
                                    }
                                } else {
                                    /* COLUMN_NAME_VENDOR,
                                     * COLUMN_NAME_VENDOR_PRODUCT_TYPE,
                                     * COLUMN_NAME_DISPLAYED_TRANSFER_CAPACITY
                                     */
                                    pStatement.setString(parameterIndex, token);
                                }
                            }

                            if (pStatement.executeUpdate() > 0) {
                                LOGGER.debug("Product: " + productId + " created successfully.");
                            } else {
                                LOGGER.warn("Failure to create product: " + productId);
                            }

                            pStatement.close();
                        } else {
                            /* update */

                            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_PRODUCT);

                            pStatement.setString(6, productId);

                            for (int parameterIndex = 1; tokenizer.hasMoreTokens(); parameterIndex++) {
                                String token = tokenizer.nextToken().trim();

                                if (parameterIndex == 3) {
                                    /* COLUMN_NAME_TRANSFER_BYTES */
                                    pStatement.setLong(parameterIndex, Long.valueOf(token));
                                } else if (parameterIndex == 5) {
                                    /* COLUMN_NAME_PRODUCT_IS_UNLIMITED */
                                    if (token.equalsIgnoreCase("TRUE")) {
                                        pStatement.setBoolean(parameterIndex, true);
                                    } else {
                                        pStatement.setBoolean(parameterIndex, false);
                                    }
                                } else {
                                    /* COLUMN_NAME_VENDOR
                                     * COLUMN_NAME_VENDOR_PRODUCT_TYPE
                                     * COLUMN_NAME_DISPLAYED_TRANSFER_CAPACITY
                                     */
                                    pStatement.setString(parameterIndex, token);
                                }
                            }

                            if (pStatement.executeUpdate() > 0) {
                                LOGGER.debug("Product: " + productId + " updated successfully.");
                            } else {
                                LOGGER.warn("No change to the content of product: " + productId);
                            }

                            pStatement.close();
                        }
                    } else if (currentSection.equals(section_product_detail)) {
                        /* create or update product-detail */

                        StringTokenizer tokenizer = new StringTokenizer(line, DatabaseConstants.FILE_DEFAULT_PRODUCTS_DELIMITERS);

                        if (tokenizer.countTokens() != token_count_for_section_product_detail) {
                            LOGGER.error("Error format of default products file.\nSection: " + section_product_detail + "\nLine: '" + line + "'");

                            continue;
                        }

                        /*
                         * Given the format of each line:
                         * COLUMN_NAME_PRODUCT_DETAIL_ID
                         * COLUMN_NAME_PRODUCT_ID
                         * COLUMN_NAME_PRODUCT_LOCALE
                         * COLUMN_NAME_PRODUCT_NAME
                         * COLUMN_NAME_PRODUCT_PRICE
                         * COLUMN_NAME_PRODUCT_DISPLAYED_PRICE
                         * COLUMN_NAME_PRODUCT_DESCRIPTION
                         */
                        String productDetailId = tokenizer.nextToken().trim();

                        if (!existingProductDetailById(productDetailId)) {
                            /* create */

                            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_PRODUCT_DETAIL);

                            pStatement.setString(1, productDetailId);

                            for (int parameterIndex = 2; tokenizer.hasMoreTokens(); parameterIndex++) {
                                String token = tokenizer.nextToken().trim();

                                /* COLUMN_NAME_PRODUCT_ID
                                 * COLUMN_NAME_PRODUCT_LOCALE
                                 * COLUMN_NAME_PRODUCT_NAME
                                 * COLUMN_NAME_PRODUCT_PRICE
                                 * COLUMN_NAME_PRODUCT_DISPLAYED_PRICE
                                 * COLUMN_NAME_PRODUCT_DESCRIPTION
                                 */

                                if (parameterIndex == 5) {
                                    pStatement.setBigDecimal(parameterIndex, new BigDecimal(token));
                                } else {
                                    pStatement.setString(parameterIndex, token);
                                }
                            }

                            if (pStatement.executeUpdate() > 0) {
                                LOGGER.debug("Product detail: " + productDetailId + " created successfully.");
                            } else {
                                LOGGER.warn("Failure to create product detail: " + productDetailId);
                            }

                            pStatement.close();
                        } else {
                            /* update */

                            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_PRODUCT_DETAIL);

                            pStatement.setString(7, productDetailId);

                            for (int parameterIndex = 1; tokenizer.hasMoreTokens(); parameterIndex++) {
                                String token = tokenizer.nextToken().trim();

                                /* COLUMN_NAME_PRODUCT_ID
                                 * COLUMN_NAME_PRODUCT_LOCALE
                                 * COLUMN_NAME_PRODUCT_NAME
                                 * COLUMN_NAME_PRODUCT_PRICE
                                 * COLUMN_NAME_PRODUCT_DISPLAYED_PRICE
                                 * COLUMN_NAME_PRODUCT_DESCRIPTION
                                 */

                                if (parameterIndex == 4) {
                                    pStatement.setBigDecimal(parameterIndex, new BigDecimal(token));
                                } else {
                                    pStatement.setString(parameterIndex, token);
                                }
                            }

                            if (pStatement.executeUpdate() > 0) {
                                LOGGER.debug("Product detail: " + productDetailId + " updated successfully.");
                            } else {
                                LOGGER.warn("No change to the content of product detail: " + productDetailId);
                            }

                            pStatement.close();
                        }
                    } else {
                        LOGGER.error("Error format of default products file.\nLine: '" + line + "'");
                    }
                }
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating or updating product default data.\nerror message:\n%s", e.getMessage()), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    /* ignored */
                }
            }

            if (dbAccess != null) {
                try {
                    dbAccess.close(null, statement, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return success;
    }

    public boolean existingProductById(String productId) {
        boolean exists = false;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_PRODUCT_BY_ID);

            pStatement.setString(1, productId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                exists = true;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding if product exists for id '%s'\nerror message:\n%s", productId, e.getMessage()), e);
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

    public boolean existingProductDetailById(String productDetailId) {
        boolean exists = false;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_PRODUCT_DETAIL_BY_ID);

            pStatement.setString(1, productDetailId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                exists = true;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding if product detail exists for id '%s'\nerror message:\n%s", productDetailId, e.getMessage()), e);
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

    public Product findProductById(String productId) {
        Product record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_PRODUCT_BY_ID);

            pStatement.setString(1, productId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = new Product();

                record.setProductId(resultSet.getString(DatabaseConstants.COLUMN_NAME_PRODUCT_ID));
                record.setProductVendor(resultSet.getString(DatabaseConstants.COLUMN_NAME_VENDOR));
                record.setVendorProductType(resultSet.getString(DatabaseConstants.COLUMN_NAME_VENDOR_PRODUCT_TYPE));
                record.setTransferBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_TRANSFER_BYTES));
                record.setDisplayedTransferCapacity(resultSet.getString(DatabaseConstants.COLUMN_NAME_DISPLAYED_TRANSFER_CAPACITY));
                record.setUnlimited(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_PRODUCT_IS_UNLIMITED));
            }
        } catch (Exception e) {
            record = null;

            LOGGER.error(String.format("Error on finding product for id '%s'\nerror message:\n%s", productId, e.getMessage()), e);
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

    public List<Product> findProductssByVendor(String vendor, String locale) {
        List<Product> products = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_PRODUCTS_BY_VENDOR);

            pStatement.setString(1, vendor);
            pStatement.setString(2, locale);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                Product record = new Product();

                record.setProductId(resultSet.getString(DatabaseConstants.COLUMN_NAME_PRODUCT_ID));
                record.setProductVendor(resultSet.getString(DatabaseConstants.COLUMN_NAME_VENDOR));
                record.setVendorProductType(resultSet.getString(DatabaseConstants.COLUMN_NAME_VENDOR_PRODUCT_TYPE));
                record.setTransferBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_TRANSFER_BYTES));
                record.setDisplayedTransferCapacity(resultSet.getString(DatabaseConstants.COLUMN_NAME_DISPLAYED_TRANSFER_CAPACITY));
                record.setUnlimited(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_PRODUCT_IS_UNLIMITED));
                record.setLocale(resultSet.getString(DatabaseConstants.COLUMN_NAME_PRODUCT_LOCALE));
                record.setProductName(resultSet.getString(DatabaseConstants.COLUMN_NAME_PRODUCT_NAME));
                record.setProductPrice(resultSet.getBigDecimal(DatabaseConstants.COLUMN_NAME_PRODUCT_PRICE));
                record.setProductDisplayedPrice(resultSet.getString(DatabaseConstants.COLUMN_NAME_PRODUCT_DISPLAYED_PRICE));
                record.setProductDescription(resultSet.getString(DatabaseConstants.COLUMN_NAME_PRODUCT_DESCRIPTION));

                products.add(record);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding product for vendor '%s', locale '%s'\nerror message:\n%s", vendor, locale, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return products;
    }

    public Product createProduct(Product product) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        boolean success = true;

        String productId = product.getProductId();
        String productVendor = product.getProductVendor();
        String venderProductType = product.getVendorProductType();
        Long transferBytes = product.getTransferBytes();
        String displayedTransferCapacity = product.getDisplayedTransferCapacity();
        Boolean unlimited = product.getUnlimited();
        String productLocale = product.getLocale();
        String productName = product.getProductName();
        BigDecimal productPrice = product.getProductPrice();
        String productDisplayedPrice = product.getProductDisplayedPrice();
        String productDescription = product.getProductDescription();

        try {
            conn = dbAccess.getConnection();

            /* create product master if not exists */
            if (!existingProductById(productId)) {
                conn.setAutoCommit(false);

                pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_PRODUCT);
                pStatement.setString(1, productId);
                pStatement.setString(2, productVendor);
                pStatement.setString(3, venderProductType);
                pStatement.setLong(4, transferBytes);
                pStatement.setString(5, displayedTransferCapacity);
                pStatement.setBoolean(6, unlimited);

                pStatement.executeUpdate();
            }

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_PRODUCT_DETAIL);
            pStatement.setString(1, Utility.generateProductDetailIdFrom(productId, productLocale));
            pStatement.setString(2, productId);
            pStatement.setString(3, productLocale);
            pStatement.setString(4, productName);
            pStatement.setBigDecimal(5, productPrice);
            pStatement.setString(6, productDisplayedPrice);
            pStatement.setString(7, productDescription);

            pStatement.executeUpdate();

            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        } catch (Exception e) {
            success = false;

            try {
                if (conn != null && !conn.getAutoCommit()) {
                    conn.rollback();
                }
            } catch (Exception e1) {
                /* ignored */
            }

            LOGGER.error(String.format("Error on create product\n'%s'\nerror message:\n%s", product.toString(), e.getMessage()), e);
        } finally {
            /* Closing connection means recycle, not deleted,
             * so we have to prevent the connection to be used again as a non-auto-commited one
             */
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
            return findProductById(productId);
        } else {
            return null;
        }
    }

//    public Purchase createPurchase(Purchase purchase) {
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//
//        boolean success = true;
//
//        String purchaseId = purchase.getPurchaseId();
//        String productId = purchase.getProductId();
//        String userId = purchase.getUserId();
//        Long quantity = purchase.getQuantity();
//        String vendorTransactionId = purchase.getVendorTransactionId();
//        String vendorUserId = purchase.getVendorUserId();
//        Long vendorPurchaseTimestamp = purchase.getPurchaseTimestamp();
//
//        try {
//            conn = dbAccess.getConnection();
//
//            /* create product master if not exists */
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_PURCHASE);
//            pStatement.setString(1, purchaseId);
//            pStatement.setString(2, productId);
//            pStatement.setString(3, userId);
//            pStatement.setLong(4, quantity);
//            pStatement.setString(5, vendorTransactionId);
//            pStatement.setString(5, vendorUserId);
//            pStatement.setLong(5, vendorPurchaseTimestamp);
//
//            pStatement.executeUpdate();
//        } catch (Exception e) {
//            success = false;
//
//            LOGGER.error(String.format("Error on create purchase\n'%s'\nerror message:\n%s", purchase.toString(), e.getMessage()), e);
//        } finally {
//            if (dbAccess != null) {
//                try {
//                    dbAccess.close(null, null, pStatement, conn);
//                } catch (Exception e) {
//                    /* ignored */
//                }
//            }
//        }
//
//        if (success) {
//            return findPurchaseById(purchaseId);
//        } else {
//            return null;
//        }
//    }
//
//    public Purchase findPurchaseById(String purchaseId) {
//        Purchase record = null;
//
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//        ResultSet resultSet = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_PURCHASE_BY_ID);
//
//            pStatement.setString(1, purchaseId);
//
//            resultSet = pStatement.executeQuery();
//
//            if (resultSet.next()) {
//                record = new Purchase();
//
//                record.setPurchaseId(resultSet.getString(DatabaseConstants.COLUMN_NAME_PURCHASE_ID));
//                record.setPurchaseVendor(resultSet.getString(DatabaseConstants.COLUMN_NAME_VENDOR));
//                record.setVendorPurchaseType(resultSet.getString(DatabaseConstants.COLUMN_NAME_VENDOR_PURCHASE_TYPE));
//                record.setTransferBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_TRANSFER_BYTES));
//                record.setDisplayedTransferCapacity(resultSet.getString(DatabaseConstants.COLUMN_NAME_DISPLAYED_TRANSFER_CAPACITY));
//                record.setUnlimited(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_PURCHASE_IS_UNLIMITED));
//            }
//        } catch (Exception e) {
//            record = null;
//
//            LOGGER.error(String.format("Error on finding purchase for id '%s'\nerror message:\n%s", purchaseId, e.getMessage()), e);
//        } finally {
//            if (dbAccess != null) {
//                try {
//                    dbAccess.close(resultSet, null, pStatement, conn);
//                } catch (Exception e) {
//                    /* ignored */
//                }
//            }
//        }
//
//        return record;
//    }
}
