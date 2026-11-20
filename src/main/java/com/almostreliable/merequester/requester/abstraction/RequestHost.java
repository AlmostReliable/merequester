package com.almostreliable.merequester.requester.abstraction;

import com.almostreliable.merequester.requester.RequestManager;

import net.minecraft.network.chat.Component;

public interface RequestHost {

    void saveChanges();

    void requestChanged(int index);

    void requestKeyChanged(int index);

    boolean isClientSide();

    RequestManager getRequestManager();

    Component getTerminalName();
}
