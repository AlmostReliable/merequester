package com.almostreliable.merequester.requester.abstraction;

import com.almostreliable.merequester.requester.RequestManager;

import net.minecraft.network.chat.Component;

public interface RequestHost {

    void requestChanged(int index);

    void requestKeyChanged(int index);

    RequestManager getRequestManager();

    Component getTerminalName();
}
