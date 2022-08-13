package net.jomcraft.jcplugin;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

	public static final Logger log = LogManager.getLogger("JCPlugin Launcher");

	@Override
	public String name() {
		return "jcplugin";
	}

	@Override
	public void initialize(IEnvironment environment) {
		try {
			if (getSideName().equals(Constants.SIDE_CLIENT)) {
				final Path location = environment.getProperty(IEnvironment.Keys.GAMEDIR.get()).get();
				FileUtilNoMC.mcDataDir = location.toFile();
				FileUtilNoMC.restoreContentsFirst();
			}
		} catch (Exception e) {
			log.error(e);
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
				log.error(ex);
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