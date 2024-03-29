package net.jomcraft.jcplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.spongepowered.asm.util.Constants;
import cpw.mods.modlauncher.Environment;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import joptsimple.OptionSpecBuilder;

public class JCPlugin implements ITransformationService {

	public static boolean checksSuccessful = false;

	@Override
	public String name() {
		return "jcplugin";
	}

	@Override
	public void initialize(IEnvironment environment) {
		try {
			if (getSideName().equals(Constants.SIDE_CLIENT)) {
				String launchTarget = environment.getProperty(IEnvironment.Keys.LAUNCHTARGET.get()).get();

				final Path location = environment.getProperty(IEnvironment.Keys.GAMEDIR.get()).get();
				FileUtilNoMC.mcDataDir = location.toFile();// new File(".");//

				if (!launchTarget.contains("dev")) {

					File mods = new File(FileUtilNoMC.mcDataDir, "mods");

					for (File mod : mods.listFiles()) {
						if (mod.getName().toLowerCase().contains("defaultsettings")) {
							JarFile jar = new JarFile(mod);

							ZipEntry toml = jar.getEntry("META-INF/mods.toml");
							if (toml != null) {

								BufferedReader result = new BufferedReader(new InputStreamReader(jar.getInputStream(toml)));

								boolean containsDefaultSettings = false;
								String readerLine;
								boolean versionsMatch = false;
								while ((readerLine = result.readLine()) != null) {
									if (readerLine.contains("modId=\"defaultsettings\"")) {
										containsDefaultSettings = true;
									} else if (readerLine.contains("version=")) {
										String version = readerLine.split("\"")[1];
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
					checksSuccessful = true;
				}
				if (checksSuccessful) {
					FileUtilNoMC.restoreContentsFirst();
				} else {
					JCLogger.log.error("Could not find DefaultSettings, not going to do anything! Shutting down...");
				}
			}
		} catch (Exception e) {
			JCLogger.log.error(e);
		}
	}

	@Override
	public void beginScanning(IEnvironment environment) {

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
	public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {

	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<ITransformer> transformers() {
		List<ITransformer> list = new ArrayList<>();
		return list;
	}

	@Override
	public void arguments(BiFunction<String, String, OptionSpecBuilder> argumentBuilder) {

	}

	@Override
	public void argumentValues(OptionResult option) {

	}

	@Override
	public Map.Entry<Set<String>, Supplier<Function<String, Optional<URL>>>> additionalClassesLocator() {
		return null;
	}

	@Override
	public Map.Entry<Set<String>, Supplier<Function<String, Optional<URL>>>> additionalResourcesLocator() {
		return null;
	}
}