package za.ac.belgiumcampus.model;

import java.time.LocalDateTime;

public class StockIssuance {

    private int issuanceId;
    private int materialId;
    private String materialName;
    private int cleanerId;
    private String cleanerName;
    private Integer issuedBy;
    private String issuedByName;
    private int quantityIssued;
    private LocalDateTime issuanceDate;
    private String notes;

    public StockIssuance() {
    }

    public StockIssuance(int issuanceId, int materialId, String materialName, int cleanerId, String cleanerName,
                          Integer issuedBy, String issuedByName, int quantityIssued,
                          LocalDateTime issuanceDate, String notes) {
        this.issuanceId = issuanceId;
        this.materialId = materialId;
        this.materialName = materialName;
        this.cleanerId = cleanerId;
        this.cleanerName = cleanerName;
        this.issuedBy = issuedBy;
        this.issuedByName = issuedByName;
        this.quantityIssued = quantityIssued;
        this.issuanceDate = issuanceDate;
        this.notes = notes;
    }

    public int getIssuanceId() {
        return issuanceId;
    }

    public void setIssuanceId(int issuanceId) {
        this.issuanceId = issuanceId;
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

    public int getCleanerId() {
        return cleanerId;
    }

    public void setCleanerId(int cleanerId) {
        this.cleanerId = cleanerId;
    }

    public String getCleanerName() {
        return cleanerName;
    }

    public void setCleanerName(String cleanerName) {
        this.cleanerName = cleanerName;
    }

    public Integer getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(Integer issuedBy) {
        this.issuedBy = issuedBy;
    }

    public String getIssuedByName() {
        return issuedByName;
    }

    public void setIssuedByName(String issuedByName) {
        this.issuedByName = issuedByName;
    }

    public int getQuantityIssued() {
        return quantityIssued;
    }

    public void setQuantityIssued(int quantityIssued) {
        this.quantityIssued = quantityIssued;
    }

    public LocalDateTime getIssuanceDate() {
        return issuanceDate;
    }

    public void setIssuanceDate(LocalDateTime issuanceDate) {
        this.issuanceDate = issuanceDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return quantityIssued + " x " + materialName + " -> " + cleanerName;
    }
}
