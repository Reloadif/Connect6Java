package main.net.packet.commandParameters;

public class FinishGameParameter extends BaseCommandParameter{
    public String winnerChipColor;

    public FinishGameParameter(String winnerChipColor) {
        this.winnerChipColor = winnerChipColor;
    }

    @Override
    public String toString() {
        return winnerChipColor;
    }
}
