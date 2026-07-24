package za.ac.belgiumcampus.model;

public class DashboardSummary {

    private final int totalMaterials;
    private final int lowStockItems;
    private final int totalCleaners;
    private final int recentIssuances;

    public DashboardSummary(int totalMaterials, int lowStockItems, int totalCleaners, int recentIssuances) {
        this.totalMaterials = totalMaterials;
        this.lowStockItems = lowStockItems;
        this.totalCleaners = totalCleaners;
        this.recentIssuances = recentIssuances;
    }

    public int getTotalMaterials() {
        return totalMaterials;
    }

    public int getLowStockItems() {
        return lowStockItems;
    }

    public int getTotalCleaners() {
        return totalCleaners;
    }

    public int getRecentIssuances() {
        return recentIssuances;
    }
}
