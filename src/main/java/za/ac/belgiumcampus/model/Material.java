package za.ac.belgiumcampus.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain object representing a single cleaning material held in inventory.
 * Mirrors the {@code materials} table (see 004_create_materials.sql).
 *
 * This class is intentionally a plain, encapsulated POJO (private fields,
 * public getters/setters) so it can be reused unchanged by the DAO layer,
 * the Swing table model, and any future reporting code - a small,
 * concrete demonstration of encapsulation.
 *
 * @author user
 */
public class Material {

    private int materialId;
    private String materialName;
    private String description;
    private String unitOfMeasure;
    private int quantity;
    private int reorderLevel;
    private BigDecimal unitPrice;
    private Integer supplierId;     // nullable - FK, ON DELETE SET NULL
    private String supplierName;    // convenience field populated by joined queries only
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Material() {
        // Used when building a new material from form input before it has an id
        this.unitOfMeasure = "unit";
        this.quantity = 0;
        this.reorderLevel = 0;
        this.unitPrice = BigDecimal.ZERO;
        this.active = true;
    }

    public Material(int materialId, String materialName, String description, String unitOfMeasure,
                     int quantity, int reorderLevel, BigDecimal unitPrice, Integer supplierId,
                     String supplierName, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.materialId = materialId;
        this.materialName = materialName;
        this.description = description;
        this.unitOfMeasure = unitOfMeasure;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.unitPrice = unitPrice;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Business rule helper: a material is "low stock" once its quantity has
     * fallen to, or below, its configured reorder level. Used to drive the
     * red-row highlighting in MaterialsFrame and the Dashboard/Reports
     * low-stock counts (see vw_low_stock_materials).
     */
    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return materialName + " (" + quantity + " " + unitOfMeasure + " in stock)";
    }
}
