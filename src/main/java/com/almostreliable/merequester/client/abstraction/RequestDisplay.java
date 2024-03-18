package com.almostreliable.merequester.client.abstraction;

import com.almostreliable.merequester.client.RequestSlot;
import com.almostreliable.merequester.requester.Requests.Request;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface RequestDisplay {

    void addSubWidget(String id, AbstractWidget widget, Map<String, AbstractWidget> subWidgets);

    @Nullable
    Request getTargetRequest(int listIndex);

    @Nullable
    List<Component> getEmptyingTooltip(RequestSlot slot, ItemStack carried);
}
