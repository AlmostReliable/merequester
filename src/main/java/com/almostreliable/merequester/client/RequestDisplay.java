package com.almostreliable.merequester.client;

import com.almostreliable.merequester.requester.Requests.Request;
import net.minecraft.client.gui.components.AbstractWidget;

import javax.annotation.Nullable;
import java.util.Map;

public interface RequestDisplay {
    void addSubWidget(String id, AbstractWidget widget, Map<String, AbstractWidget> subWidgets);

    @Nullable
    Request getTargetRequest(int listIndex);
}
