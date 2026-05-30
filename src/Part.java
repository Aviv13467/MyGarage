import java.io.Serializable;

public class Part implements Serializable {

    private final String sku;
    private final String partName;
    private final double price;
    private final String modelName;
    private int quantity = 1;

    public Part(String sku, String partName, double price, String modelName) {
        this.sku = sku;
        this.partName = partName;
        this.price = price;
        this.modelName = modelName;
    }
    public Part(String sku, String partName, double price, String modelName, int quantity) {
        this.sku = sku;
        this.partName = partName;
        this.price = price;
        this.modelName = modelName;
        this.quantity = quantity;
    }

    public String getSku() { return sku; }
    public String getPartName() { return partName; }
    public double getPrice() { return price; }
    public String getModelName() { return modelName; }
    public int getQuantity() { return quantity; }

public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return partName + " (SKU: " + sku + ")";
    }
}