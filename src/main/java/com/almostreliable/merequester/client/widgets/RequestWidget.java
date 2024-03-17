package com.almostreliable.merequester.client.widgets;

import appeng.client.gui.style.ScreenStyle;
import com.almostreliable.merequester.client.abstraction.RequestDisplay;
import com.almostreliable.merequester.client.abstraction.RequesterReference;
import com.almostreliable.merequester.network.RequestUpdatePacket;
import com.almostreliable.merequester.requester.Requests.Request;
import net.minecraft.client.gui.components.AbstractWidget;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.almostreliable.merequester.Utils.f;

@OnlyIn(Dist.CLIENT)
public class RequestWidget {

    private final RequestDisplay host;
    private final int index;
    private final int x;
    private final int y;
    private final ScreenStyle style;
    private final Map<String, AbstractWidget> subWidgets;

    private StateBox stateBox;
    private NumberField amountField;
    private NumberField batchField;
    private SubmitButton submitButton;
    private StatusDisplay statusDisplay;

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
        stateBox = new StateBox(x, y, style, () -> stateBoxChanged(host.getTargetRequest(index)));
        host.addSubWidget(f("request_state_{}", index), stateBox, subWidgets);

        amountField = new NumberField(x + 38, y, "amount", style, amount -> amountFieldSubmitted(host.getTargetRequest(index), amount));
        host.addSubWidget(f("request_amount_{}", index), amountField, subWidgets);

        batchField = new NumberField(x + 92, y, "batch", style, amount -> batchFieldSubmitted(host.getTargetRequest(index), amount));
        host.addSubWidget(f("request_batch_{}", index), batchField, subWidgets);

        submitButton = new SubmitButton(x + 146, y, style, () -> submitButtonClicked(host.getTargetRequest(index)));
        host.addSubWidget(f("request_submit_{}", index), submitButton, subWidgets);

        statusDisplay = new StatusDisplay(x + 39, y + 15, () -> isInactive(host.getTargetRequest(index)));
        host.addSubWidget(f("request_status_{}", index), statusDisplay, subWidgets);
    }

    public void hide() {
        subWidgets.values().forEach(w -> w.visible = false);
    }

    public void applyRequest(Request request) {
        subWidgets.values().forEach(w -> w.visible = true);
        stateBox.setSelected(request.getState());
        var status = request.getClientStatus();
        statusDisplay.setStatus(status);
        amountField.adjustToType(request.getKey());
        batchField.adjustToType(request.getKey());
        if (status.locksRequest()) {
            amountField.setEditable(false);
            batchField.setEditable(false);
        } else {
            amountField.setEditable(true);
            batchField.setEditable(true);
        }
        if (amountField.isFocused() || batchField.isFocused() || submitButton.isFocused()) return;
        amountField.setLongValue(request.getAmount());
        batchField.setLongValue(request.getBatch());
    }

    private void stateBoxChanged(@Nullable Request request) {
        if (request == null) return;
        var newState = stateBox.isSelected();
        request.updateState(newState); // prevent jittery animation before server information is received
        var requesterId = ((RequesterReference) request.getRequesterReference()).getRequesterId();
        PacketDistributor.SERVER.noArg().send(new RequestUpdatePacket(requesterId, request.getIndex(), newState));
    }

    private void amountFieldSubmitted(@Nullable Request request, long amount) {
        if (request == null) return;
        var oldValue = request.getAmount();
        request.updateAmount(amount);
        if (oldValue == request.getAmount()) {
            amountField.setLongValue(oldValue);
        } else {
            submitButtonClicked(request);
        }
    }

    private void batchFieldSubmitted(@Nullable Request request, long batch) {
        if (request == null) return;
        var oldValue = request.getBatch();
        request.updateBatch(batch);
        if (oldValue == request.getBatch()) {
            batchField.setLongValue(oldValue);
        } else {
            submitButtonClicked(request);
        }
    }

    private void submitButtonClicked(@Nullable Request request) {
        if (request == null) return;
        long amount = amountField.getLongValue().orElse(0);
        long batch = batchField.getLongValue().orElse(1);
        var requesterId = ((RequesterReference) request.getRequesterReference()).getRequesterId();
        PacketDistributor.SERVER.noArg().send(new RequestUpdatePacket(requesterId, request.getIndex(), amount, batch));
    }

    private boolean isInactive(@Nullable Request request) {
        return request == null || !request.isRequesting() || request.getAmount() == 0;
    }
}
