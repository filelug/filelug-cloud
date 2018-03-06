package org.clopuccino.domain;

/**
 * <code>HierarchicalModelType</code> describes the type of class {#link HierarchicalModel}.
 *
 * @author masonhsieh
 * @version 1.0
 */
public enum TransferHistoryTimeType {

    TRANSFER_HISTORY_TYPE_LATEST_20(0), // DEFAULT VALUE
    TRANSFER_HISTORY_TYPE_LATEST_WEEK(1),
    TRANSFER_HISTORY_TYPE_LATEST_MONTH(2),
    TRANSFER_HISTORY_TYPE_ALL(3);

    private final int value;

    private TransferHistoryTimeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean isValid(int testValue) {
        return testValue >= 0 && testValue <= 3;
    }

    /**
     * Default value is TRANSFER_HISTORY_TYPE_LATEST_20 if value is invalid.
     */
    public static TransferHistoryTimeType toType(int value) {
        TransferHistoryTimeType type;

        switch (value) {
            case 1:
                type = TRANSFER_HISTORY_TYPE_LATEST_WEEK;
                break;
            case 2:
                type = TRANSFER_HISTORY_TYPE_LATEST_MONTH;
                break;
            case 3:
                type = TRANSFER_HISTORY_TYPE_ALL;
                break;
            default:
                type = TRANSFER_HISTORY_TYPE_LATEST_20;
        }

        return type;
    }
}
