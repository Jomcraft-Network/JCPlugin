package net.jomcraft.jcplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;

public class JCPlugin implements GraphicsBootstrapper {

	public static boolean checksSuccessful = false;

	@Override
	public String name() {
		return "jcplugin";
	}

    @Override
    public void bootstrap(String[] arguments) {
		try {
			if (FMLLoader.getCurrent().getDist().isClient()) {

				final Path location = FMLLoader.getCurrent().getGameDir();
				FileUtilNoMC.mcDataDir = location.toFile();

				if (FMLLoader.getCurrent().isProduction()) {

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
}