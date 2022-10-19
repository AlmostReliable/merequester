package com.almostreliable.merequester.wireless.ae2wtlib;

import appeng.init.client.InitScreens;
import de.mari_023.ae2wtlib.IWTLibAddonEntrypoint;

public class WTLibClient implements IWTLibAddonEntrypoint {
    @Override
    public void onWTLibInitialized() {
        InitScreens.register(WRMenu.TYPE, WRScreen::new, "/screens/ae2wtlib_wrt.json");
    }
}
