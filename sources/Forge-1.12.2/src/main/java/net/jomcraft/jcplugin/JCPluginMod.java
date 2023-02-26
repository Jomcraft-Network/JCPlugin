package net.jomcraft.jcplugin;

import net.minecraftforge.fml.common.Mod;

//@Mod(modid = JCPluginMod.MODID, acceptedMinecraftVersions = "[1.7.10,1.12.2]", name = JCPluginMod.NAME, version = JCPluginMod.VERSION, clientSideOnly = true)
public class JCPluginMod {

    @Mod.Instance
    public static JCPluginMod instance;

    public static final String MODID = "jcplugin";
    public static final String NAME = "JCPlugin";
    public static final String VERSION = JCPluginMod.class.getPackage().getImplementationVersion();

    public JCPluginMod() {
        instance = this;
    }
}
