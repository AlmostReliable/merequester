package com.almostreliable.merequester.client.widgets;

import appeng.client.gui.MathExpressionParser;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ConfirmableTextField;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.core.localization.GuiText;
import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.mixin.accessors.EditBoxMixin;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Consumer;

/**
 * yoinked from {@link NumberEntryWidget}
 */
public class NumberField extends ConfirmableTextField {

    private static final int WIDTH = 52;
    private static final int HEIGHT = 12;

    private static final int TEXT_COLOR = 0xFF_FFFF;
    private static final int ERROR_COLOR = 0xFF_0000;

    private static final NumberEntryType TYPE = NumberEntryType.UNITLESS;
    private static final int MIN_VALUE = 0;

    private final String name;
    private final DecimalFormat decimalFormat;

    NumberField(int x, int y, String name, ScreenStyle style, Consumer<Long> onConfirm) {
        super(style, Minecraft.getInstance().font, x, y, WIDTH, HEIGHT);
        this.name = name;

        decimalFormat = new DecimalFormat("#.######", new DecimalFormatSymbols());
        decimalFormat.setParseBigDecimal(true);
        decimalFormat.setNegativePrefix("-");

        setBordered(false);
        setVisible(true);
        setMaxLength(7);
        setLongValue(0);
        setResponder(text -> validate());
        setOnConfirm(() -> {
            if (getLongValue().isPresent()) {
                onConfirm.accept(getLongValue().getAsLong());
                setFocus(false);
            }
        });
        validate();
    }

    private void validate() {
        List<Component> validationErrors = new ArrayList<>();
        List<Component> infoMessages = new ArrayList<>();

        var possibleValue = getValueInternal();
        if (possibleValue.isPresent()) {
            if (possibleValue.get().scale() > 0) {
                validationErrors.add(Utils.translate("tooltip", "whole_number"));
            } else {
                var value = convertToExternalValue(possibleValue.get());
                if (value < MIN_VALUE) {
                    var formatted = decimalFormat.format(convertToInternalValue(MIN_VALUE));
                    validationErrors.add(GuiText.NumberLessThanMinValue.text(formatted));
                } else if (!isNumber()) {
                    infoMessages.add(Component.literal("= " + decimalFormat.format(possibleValue.get())));
                }
            }
        } else {
            validationErrors.add(GuiText.InvalidNumber.text());
        }

        boolean valid = validationErrors.isEmpty();
        var tooltip = valid ? infoMessages : validationErrors;
        setTextColor(valid ? TEXT_COLOR : ERROR_COLOR);
        setTooltipMessage(tooltip);
    }

    private long convertToExternalValue(BigDecimal internalValue) {
        var multiplicand = BigDecimal.valueOf(TYPE.amountPerUnit());
        var value = internalValue.multiply(multiplicand, MathContext.DECIMAL128);
        value = value.setScale(0, RoundingMode.UP);
        return value.longValue();
    }

    private BigDecimal convertToInternalValue(long externalValue) {
        var divisor = BigDecimal.valueOf(TYPE.amountPerUnit());
        return BigDecimal.valueOf(externalValue).divide(divisor, MathContext.DECIMAL128);
    }

    OptionalLong getLongValue() {
        var internalValue = getValueInternal();
        if (internalValue.isEmpty()) {
            return OptionalLong.empty();
        }

        var externalValue = convertToExternalValue(internalValue.get());
        if (externalValue < MIN_VALUE) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(externalValue);
    }

    void setLongValue(long value) {
        var internalValue = convertToInternalValue(Math.max(value, MIN_VALUE));
        setValue(decimalFormat.format(internalValue));
        moveCursorToEnd();
        validate();
    }

    private boolean isNumber() {
        var position = new ParsePosition(0);
        var textValue = getValue().trim();
        decimalFormat.parse(textValue, position);
        return position.getErrorIndex() == -1 && position.getIndex() == textValue.length();
    }

    private Optional<BigDecimal> getValueInternal() {
        return MathExpressionParser.parse(getValue(), decimalFormat);
    }

    @Override
    public void setTooltipMessage(List<Component> tooltipMessage) {
        tooltipMessage.add(0, Utils.translate("tooltip", name));
        super.setTooltipMessage(tooltipMessage);
        if (!isFocused() || (tooltipMessage.size() > 1 && !tooltipMessage.get(1).getString().startsWith("="))) return;
        tooltipMessage.add(Component.literal("» ").withStyle(ChatFormatting.AQUA)
            .append(Utils.translate(
                "tooltip",
                "enter_to_submit",
                InputConstants.getKey("key.keyboard.enter").getDisplayName()
            ).withStyle(ChatFormatting.GRAY)));
    }

    @Override
    public void setFocus(boolean isFocused) {
        if (isFocused && !Utils.cast(this, EditBoxMixin.class).merequester$isEditable()) {
            return;
        }
        super.setFocus(isFocused);
    }
}
