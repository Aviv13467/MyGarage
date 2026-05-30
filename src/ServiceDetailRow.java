public class ServiceDetailRow {
    private final String partName;
    private final String sku;
    private final int quantity;
    private final double totalCost;

    public ServiceDetailRow(String partName, String sku, int quantity, double totalCost) {
        this.partName = partName;
        this.sku = sku;
        this.quantity = quantity;
        this.totalCost = totalCost;
    }

    public String getPartName() { return partName; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public double getTotalCost() { return totalCost; }
}