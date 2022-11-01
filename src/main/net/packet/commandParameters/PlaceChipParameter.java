package main.net.packet.commandParameters;

public class PlaceChipParameter extends BaseCommandParameter {
    public String chipColor;
    public int column;
    public int row;

    public PlaceChipParameter(String chipColor, int column, int row) {
        this.chipColor = chipColor;
        this.column = column;
        this.row = row;
    }

    @Override
    public String toString() {
        return String.format("chipColor = %s, column = %d, row = %d", chipColor, column, row);
    }
}
