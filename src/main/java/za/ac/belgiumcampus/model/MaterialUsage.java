package za.ac.belgiumcampus.model;

public class MaterialUsage {

    private final int materialId;
    private final String materialName;
    private final int totalIssued;

    public MaterialUsage(int materialId, String materialName, int totalIssued) {
        this.materialId = materialId;
        this.materialName = materialName;
        this.totalIssued = totalIssued;
    }

    public int getMaterialId() {
        return materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public int getTotalIssued() {
        return totalIssued;
    }
}
