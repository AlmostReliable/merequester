package com.almostreliable.merequester.requester;

import net.minecraft.network.chat.Component;

public interface RequestHost {

    void saveChanges();

    void requestChanged(int index);

    boolean isClientSide();

    Requests getRequests();

    Component getTerminalName();
}
