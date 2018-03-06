package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>Purchase</code> represents purchase information.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Purchase {

    @JsonProperty("purchase-id")
    private String purchaseId;

    @JsonProperty("product-id")
    private String productId;

    @JsonProperty("user-id")
    private String userId;

    @JsonProperty("quantity")
    private Long quantity;

    @JsonProperty("vendor-transaction-id")
    private String vendorTransactionId;

    @JsonProperty("vendor-user-id")
    private String vendorUserId;

    @JsonProperty("purchase-timestamp")
    private Long purchaseTimestamp;


    public Purchase() {
    }

    public Purchase(String purchaseId, String productId, String userId, Long quantity, String vendorTransactionId, String vendorUserId, Long purchaseTimestamp) {
        this.purchaseId = purchaseId;
        this.productId = productId;
        this.userId = userId;
        this.quantity = quantity;
        this.vendorTransactionId = vendorTransactionId;
        this.vendorUserId = vendorUserId;
        this.purchaseTimestamp = purchaseTimestamp;
    }

    public String getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getVendorTransactionId() {
        return vendorTransactionId;
    }

    public void setVendorTransactionId(String vendorTransactionId) {
        this.vendorTransactionId = vendorTransactionId;
    }

    public String getVendorUserId() {
        return vendorUserId;
    }

    public void setVendorUserId(String vendorUserId) {
        this.vendorUserId = vendorUserId;
    }

    public Long getPurchaseTimestamp() {
        return purchaseTimestamp;
    }

    public void setPurchaseTimestamp(Long purchaseTimestamp) {
        this.purchaseTimestamp = purchaseTimestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Purchase{");
        sb.append("purchaseId='").append(purchaseId).append('\'');
        sb.append(", productId='").append(productId).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", quantity=").append(quantity);
        sb.append(", vendorTransactionId='").append(vendorTransactionId).append('\'');
        sb.append(", vendorUserId='").append(vendorUserId).append('\'');
        sb.append(", purchaseTimestamp=").append(purchaseTimestamp);
        sb.append('}');
        return sb.toString();
    }
}
