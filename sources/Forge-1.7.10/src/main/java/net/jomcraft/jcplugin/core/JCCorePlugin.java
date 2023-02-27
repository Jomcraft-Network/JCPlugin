package net.jomcraft.jcplugin.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import net.jomcraft.jcplugin.ComparableVersion;
import net.jomcraft.jcplugin.FileUtilNoMC;
import net.jomcraft.jcplugin.JCLogger;
import net.jomcraft.jcplugin.JCPlugin;
import net.minecraft.launchwrapper.Launch;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.DependsOn;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;

@DependsOn("forge")
@SortingIndex(1001)
public class JCCorePlugin implements IFMLLoadingPlugin {

    @Override
    public String getAccessTransformerClass() {

        try {
            if (FMLLaunchHandler.side().name().equals("CLIENT")) {
                Map<String, String> launchArgsList = (Map<String, String>) Launch.blackboard.get("launchArgs");
                String gameDir = launchArgsList.get("--gameDir");

                FileUtilNoMC.mcDataDir = new File(gameDir);

                Field f = Class.forName("cpw.mods.fml.relauncher.CoreModManager").getDeclaredField("deobfuscatedEnvironment");
                f.setAccessible(true);
                boolean devEnv = (boolean) f.get(null);

                if (!devEnv) {
                    File mods = new File(FileUtilNoMC.mcDataDir, "mods");

                    for (File mod : mods.listFiles()) {
                        if (mod.getName().toLowerCase().contains("defaultsettings")) {
                            JarFile jar = new JarFile(mod);

                            ZipEntry toml = jar.getEntry("mcmod.info");
                            if (toml != null) {

                                BufferedReader result = new BufferedReader(new InputStreamReader(jar.getInputStream(toml)));
                                boolean containsDefaultSettings = false;
                                String readerLine;
                                boolean versionsMatch = false;
                                while ((readerLine = result.readLine()) != null) {
                                    if (readerLine.contains("\"modid\": \"defaultsettings\"")) {
                                        containsDefaultSettings = true;
                                    } else if (readerLine.contains("\"version\": ")) {
                                        String version = readerLine.split("\"")[3];
                                        versionsMatch = JCLogger.isEqualOrNewer(new ComparableVersion(version));
                                    }
                                }

                                if(containsDefaultSettings && versionsMatch) {
                                    JCPlugin.checksSuccessful = true;
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