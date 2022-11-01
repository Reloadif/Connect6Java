package main.net.packet.commandParameters;

public class ConnectGameParameter extends BaseCommandParameter {
    public String chipColor;
    public int fieldSize;

    public ConnectGameParameter(String chipColor, int fieldSize) {
        this.chipColor = chipColor;
        this.fieldSize = fieldSize;
    }

    @Override
    public String toString() {
        return String.format("chipColor = %s, fieldSize = %d", chipColor, fieldSize);
    }
}
