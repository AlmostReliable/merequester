package com.almostreliable.merequester.requester.abstraction;

import com.almostreliable.merequester.requester.Requests;
import net.minecraft.network.chat.Component;

public interface RequestHost {

    void saveChanges();

    void requestChanged(int index);

    boolean isClientSide();

    Requests getRequests();

    Component getTerminalName();
}
