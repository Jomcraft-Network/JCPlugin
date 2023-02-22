package net.jomcraft.jcplugin.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import net.jomcraft.jcplugin.FileUtilNoMC;
import net.jomcraft.jcplugin.JCLogger;
import net.jomcraft.jcplugin.JCPlugin;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.DependsOn;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;

@DependsOn("forge")
@SortingIndex(1001)
public class JCCorePlugin implements IFMLLoadingPlugin {

    @Override
    public String getAccessTransformerClass() {

        try {
            if (FMLLaunchHandler.side().name().equals("CLIENT")) {
                Map<String, String> aa = (Map<String, String>) Launch.blackboard.get("launchArgs");
                String gameDir = aa.get("--gameDir");

                FileUtilNoMC.mcDataDir = new File(gameDir);
                if (!FMLLaunchHandler.isDeobfuscatedEnvironment()) {
                    File mods = new File(FileUtilNoMC.mcDataDir, "mods");

                    for (File mod : mods.listFiles()) {
                        if (mod.getName().toLowerCase().contains("defaultsettings")) {
                            JarFile jar = new JarFile(mod);

                            ZipEntry toml = jar.getEntry("mcmod.info");
                            if (toml != null) {

                                BufferedReader result = new BufferedReader(new InputStreamReader(jar.getInputStream(toml)));

                                String readerLine;
                                while ((readerLine = result.readLine()) != null) {
                                    if (readerLine.contains("\"modid\": \"defaultsettings\"")) {
                                        JCPlugin.checksSuccessful = true;
                                        break;
                                    }
                                }

                                result.close();
                            }

                            jar.close();
                        }
                    }
                } else {
                    JCLogger.log.info("Loading DefaultSettings Core plugin in development environment");
                    JCPlugin.checksSuccessful = true;
                }
                if (JCPlugin.checksSuccessful) {
                    FileUtilNoMC.restoreContentsFirst();
                } else {
                    JCLogger.log.error("Could not find DefaultSettings, not going to do anything! Shutting down...");
                }
            }
        } catch (Exception e) {
            JCLogger.log.error(e);
        }
        return null;
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;

    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

}