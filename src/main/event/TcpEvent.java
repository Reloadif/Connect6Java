package main.event;

import main.net.packet.CommonPacket;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class TcpEvent {
    private final Set<Consumer<CommonPacket>> listeners = new HashSet<>();

    public void addListener(Consumer<CommonPacket> listener) {
        listeners.add(listener);
    }

    public void invoke(CommonPacket packet) {
        listeners.forEach(x -> x.accept(packet));
    }
}
