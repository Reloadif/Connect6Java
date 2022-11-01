package main.net.packet;

import main.net.packet.commandParameters.BaseCommandParameter;

import java.io.Serializable;

public class CommonPacket implements Serializable {
    public HeaderType header;
    public CommandType command;
    public BaseCommandParameter commandParameter;

    public CommonPacket() {
        this.header = HeaderType.NONE;
        this.command = CommandType.NONE;
        this.commandParameter = null;
    }

    public CommonPacket(HeaderType header, CommandType command, BaseCommandParameter commandParameter) {
        this.header = header;
        this.command = command;
        this.commandParameter = commandParameter;
    }

    public boolean isValidPacket() {
        return this.header != HeaderType.NONE && this.command != CommandType.NONE && this.commandParameter != null;
    }
    public boolean isServerPacket() {
        return this.header == HeaderType.TO_SERVER;
    }
    public boolean isClientPacket() {
        return this.header == HeaderType.TO_CLIENT;
    }
}
