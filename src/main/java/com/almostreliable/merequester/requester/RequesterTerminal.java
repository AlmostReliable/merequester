package com.almostreliable.merequester.requester;

import appeng.api.parts.IPartItem;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.parts.reporting.AbstractDisplayPart;
import appeng.parts.reporting.PatternAccessTerminalPart;

/**
 * logic taken from {@link PatternAccessTerminalPart}
 */
public class RequesterTerminal extends AbstractDisplayPart implements IConfigurableObject {

    public RequesterTerminal(IPartItem<?> partItem) {
        super(partItem, true);
    }

    @Override
    public IConfigManager getConfigManager() {
        return null;
    }
}
