package net.jomcraft.jcplugin.mixin;

import cpw.mods.modlauncher.Environment;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import net.jomcraft.jcplugin.FileUtilNoMC;
import net.jomcraft.jcplugin.JCLogger;
import net.jomcraft.jcplugin.JCPlugin;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class JCMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        try {
            if (getSideName().equals(Constants.SIDE_CLIENT)) {
                String launchTarget = Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.LAUNCHTARGET.get()).get();

                final Path location = Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.GAMEDIR.get()).get();
                FileUtilNoMC.mcDataDir = location.toFile();

                if (!launchTarget.contains("dev")) {

                    File mods = new File(FileUtilNoMC.mcDataDir, "mods");

                    for (File mod : mods.listFiles()) {
                        if (mod.getName().toLowerCase().contains("defaultsettings")) {
                            JarFile jar = new JarFile(mod);

                            ZipEntry toml = jar.getEntry("META-INF/mods.toml");
                            if (toml != null) {

                                BufferedReader result = new BufferedReader(new InputStreamReader(jar.getInputStream(toml)));

                                String readerLine;
                                while ((readerLine = result.readLine()) != null) {
                                    if (readerLine.contains("modId=\"defaultsettings\"")) {
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
    }

    // adapted from org.spongepowered.asm.launch.platform.MixinPlatformAgentMinecraftForge, part of Mixin
    public String getSideName() {
        Environment environment = Launcher.INSTANCE.environment();
        final String launchTarget = environment.getProperty(IEnvironment.Keys.LAUNCHTARGET.get()).orElse("missing").toLowerCase(Locale.ROOT);
        if (launchTarget.contains("server")) {
            return Constants.SIDE_SERVER;
        }
        if (launchTarget.contains("client")) {
            return Constants.SIDE_CLIENT;
        }
        Optional<ILaunchHandlerService> launchHandler = environment.findLaunchHandler(launchTarget);
        if (launchHandler.isPresent()) {
            ILaunchHandlerService service = launchHandler.get();
            try {
                Method mdGetDist = service.getClass().getDeclaredMethod("getDist");
                String strDist = mdGetDist.invoke(service).toString().toLowerCase(Locale.ROOT);
                if (strDist.contains("server")) {
                    return Constants.SIDE_SERVER;
                }
                if (strDist.contains("client")) {
                    return Constants.SIDE_CLIENT;
                }
            } catch (Exception ex) {
                JCLogger.log.error(ex);
                return null;
            }
        }
        return null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}