package main.net.packet.commandParameters;

import java.io.Serializable;

public abstract class BaseCommandParameter implements Serializable {
    @Override
    public abstract String toString();
}
