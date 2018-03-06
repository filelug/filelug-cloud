package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * <code>Product</code> represents product information base on the locale.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Product {

    @JsonProperty("product-id")
    private String productId;

    @JsonProperty("product-vendor")
    private String productVendor;

    @JsonProperty("vendor-product-type")
    private String vendorProductType;

    @JsonProperty("transfer-bytes")
    private Long transferBytes;

    @JsonProperty("displayed-transfer-capacity")
    private String displayedTransferCapacity;

    @JsonProperty("unlimited")
    private Boolean unlimited;

    @JsonProperty("locale")
    private String locale;

    @JsonProperty("product-name")
    private String productName;

    @JsonProperty("product-price")
    private BigDecimal productPrice;

    @JsonProperty("product-displayed-price")
    private String productDisplayedPrice;

    @JsonProperty("product-description")
    private String productDescription;


    public Product() {
    }

    public Product(String productId, String productVendor, String vendorProductType, Long transferBytes, String displayedTransferCapacity, Boolean unlimited, String locale, String productName, BigDecimal productPrice, String productDisplayedPrice, String productDescription) {
        this.productId = productId;
        this.productVendor = productVendor;
        this.vendorProductType = vendorProductType;
        this.transferBytes = transferBytes;
        this.displayedTransferCapacity = displayedTransferCapacity;
        this.unlimited = unlimited;
        this.locale = locale;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productDisplayedPrice = productDisplayedPrice;
        this.productDescription = productDescription;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductVendor() {
        return productVendor;
    }

    public void setProductVendor(String productVendor) {
        this.productVendor = productVendor;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getVendorProductType() {
        return vendorProductType;
    }

    public void setVendorProductType(String vendorProductType) {
        this.vendorProductType = vendorProductType;
    }

    public Long getTransferBytes() {
        return transferBytes;
    }

    public void setTransferBytes(Long transferBytes) {
        this.transferBytes = transferBytes;
    }

    public String getDisplayedTransferCapacity() {
        return displayedTransferCapacity;
    }

    public void setDisplayedTransferCapacity(String displayedTransferCapacity) {
        this.displayedTransferCapacity = displayedTransferCapacity;
    }

    public Boolean getUnlimited() {
        return unlimited;
    }

    public void setUnlimited(Boolean unlimited) {
        this.unlimited = unlimited;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductDisplayedPrice() {
        return productDisplayedPrice;
    }

    public void setProductDisplayedPrice(String productDisplayedPrice) {
        this.productDisplayedPrice = productDisplayedPrice;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Product{");
        sb.append("productId='").append(productId).append('\'');
        sb.append(", productVendor='").append(productVendor).append('\'');
        sb.append(", vendorProductType='").append(vendorProductType).append('\'');
        sb.append(", transferBytes=").append(transferBytes);
        sb.append(", displayedTransferCapacity='").append(displayedTransferCapacity).append('\'');
        sb.append(", unlimited=").append(unlimited);
        sb.append(", locale='").append(locale).append('\'');
        sb.append(", productName='").append(productName).append('\'');
        sb.append(", productPrice=").append(productPrice);
        sb.append(", productDisplayedPrice=").append(productDisplayedPrice);
        sb.append(", productDescription='").append(productDescription).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
