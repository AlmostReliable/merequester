package com.almostreliable.merequester.requester;

import appeng.block.AEBaseEntityBlock;
import net.minecraft.world.level.material.Material;

public class RequesterBlock extends AEBaseEntityBlock<RequesterBlockEntity> {
    public RequesterBlock() {
        super(defaultProps(Material.METAL));
    }
}
