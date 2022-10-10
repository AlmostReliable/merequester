package com.almostreliable.merequester.client.widgets;

import appeng.client.gui.style.ScreenStyle;
import com.almostreliable.merequester.client.RequestDisplay;
import com.almostreliable.merequester.client.RequesterReference;
import com.almostreliable.merequester.network.PacketHandler;
import com.almostreliable.merequester.network.RequestUpdatePacket;
import com.almostreliable.merequester.requester.Requests.Request;
import net.minecraft.client.gui.components.AbstractWidget;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.almostreliable.merequester.Utils.f;

public class RequestWidget {

    private final RequestDisplay host;
    private final int index;
    private final int x;
    private final int y;
    private final ScreenStyle style;
    private final Map<String, AbstractWidget> subWidgets;

    @Nullable private StateBox stateBox;
    @Nullable private SubmitButton submitButton;

    public RequestWidget(RequestDisplay host, int index, int x, int y, ScreenStyle style) {
        this.host = host;
        this.index = index;
        this.x = x;
        this.y = y;
        this.style = style;
        this.subWidgets = new HashMap<>();
    }

    /**
     * Has to be called from a {@link RequestDisplay} implementation before
     * the super call to the init method.
     * <p>
     * This removes all sub-widgets from the widget container so the super init
     * doesn't throw an error because the widgets are styleless.
     *
     * @param widgetContainer the widget container
     */
    public void preInit(Map<String, AbstractWidget> widgetContainer) {
        subWidgets.forEach(widgetContainer::remove);
    }

    /**
     * Has to be called from a {@link RequestDisplay} implementation after
     * the super call to the init method.
     * <p>
     * This adds all sub-widgets to the widget container manually so no
     * style is required. This is necessary because the widgets are styleless.
     */
    public void postInit() {
        stateBox = new StateBox(x, y, style);
        stateBox.setChangeListener(() -> stateBoxChanged(host.getTargetRequest(index)));
        host.addSubWidget(f("request_state_{}", index), stateBox, subWidgets);

        submitButton = new SubmitButton(x + 146, y, style);
        submitButton.setChangeListener(() -> submitButtonClicked(host.getTargetRequest(index)));
        host.addSubWidget(f("request_submit_{}", index), submitButton, subWidgets);
    }

    public void hide() {
        subWidgets.values().forEach(w -> w.visible = false);
    }

    public void applyRequest(Request request) {
        subWidgets.values().forEach(w -> w.visible = true);
        if (stateBox != null) {
            stateBox.setSelected(request.getState());
        }
    }

    private void stateBoxChanged(@Nullable Request request) {
        if (request == null || stateBox == null) return;
        var newState = stateBox.isSelected();
        request.updateState(newState); // prevent jittery animation before server information is received
        var requesterId = ((RequesterReference) request.getRequesterReference()).getRequesterId();
        PacketHandler.CHANNEL.sendToServer(new RequestUpdatePacket(requesterId, request.getSlot(), newState));
    }

    private void submitButtonClicked(@Nullable Request request) {
        if (request == null || submitButton == null) return;
        // TODO: pull information from textbox widgets when they are implemented
        var requesterId = ((RequesterReference) request.getRequesterReference()).getRequesterId();
        PacketHandler.CHANNEL.sendToServer(new RequestUpdatePacket(requesterId, request.getSlot(), request.getCount(), request.getBatch()));
    }
}
