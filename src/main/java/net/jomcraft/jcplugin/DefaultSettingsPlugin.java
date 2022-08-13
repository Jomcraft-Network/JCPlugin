package net.jomcraft.jcplugin;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.Level;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class DefaultSettingsPlugin {

	public static void setup(String side) {
		try {
			restoreContents();
		} catch (NullPointerException | NoSuchAlgorithmException | IOException e) {
			JCPlugin.log.log(Level.ERROR, "Exception at processing startup: ", e);
		}
	}
	
	public static final File mcDataDir = new File(".");
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static MainJSON mainJson;
	public static PrivateJSON privateJson;
	public volatile static Thread registryChecker;
	public volatile static boolean options_exists = false;
	public volatile static boolean keys_exists = false;
	public static ArrayList<String> deleted = new ArrayList<String>();
	public volatile static boolean servers_exists = false;
	public static String activeProfile = "Default";
	public static boolean otherCreator = false;
	public static boolean firstBootUp;
	public static final FileFilter fileFilterModular = new FileFilter() {

		@Override
		public boolean accept(File file) {
			if (!file.getName().equals("defaultsettings") && !file.getName().equals("defaultsettings.json") && !file.getName().equals("sharedConfigs") && !file.getName().equals("ignore.json") && !file.getName().equals("ds_dont_export.json") && !file.getName().equals("keys.txt") && !file.getName().equals("options.txt") && !file.getName().equals("optionsof.txt") && !file.getName().equals("optionsshaders.txt") && !file.getName().equals("options.justenoughkeys.txt") && !file.getName().equals("options.amecsapi.txt") && !file.getName().equals("servers.dat"))
				return true;

			return false;
		}
	};

	public static final FileFilter fileFilterAnti = new FileFilter() {

		@Override
		public boolean accept(File file) {
			if (!file.getName().equals("defaultsettings") && !file.getName().equals("defaultsettings.json") && !file.getName().equals("sharedConfigs") && !file.getName().equals("ignore.json") && !file.getName().equals("ds_dont_export.json") && !file.getName().equals("keys.txt") && !file.getName().equals("options.txt") && !file.getName().equals("optionsof.txt") && !file.getName().equals("optionsshaders.txt") && !file.getName().equals("options.justenoughkeys.txt") && !file.getName().equals("options.amecsapi.txt") && !file.getName().equals("servers.dat"))
				return true;

			return false;
		}
	};

	public static final FileFilter fileFilter = new FileFilter() {

		@Override
		public boolean accept(File file) {

			if (!file.getName().equals("defaultsettings") && !file.getName().equals("defaultsettings.json") && !file.getName().equals("ds_dont_export.json") && !file.getName().equals("keys.txt") && !file.getName().equals("options.txt") && !file.getName().equals("optionsof.txt") && !file.getName().equals("optionsshaders.txt") && !file.getName().equals("options.justenoughkeys.txt") && !file.getName().equals("options.amecsapi.txt") && !file.getName().equals("servers.dat") && !new File(getMainFolder(), "sharedConfigs/" + file.getName()).exists())
				return true;

			return false;
		}
	};

	public static File getMainFolder() {
		final File storeFolder = new File(mcDataDir, "config/defaultsettings");
		storeFolder.mkdir();
		return storeFolder;
	}

	@SuppressWarnings("unused")
	public static void switchState(Byte state, String query) {

		FileFilter ff = null;
		if (!query.isEmpty()) {
			ff = new FileFilter() {

				@Override
				public boolean accept(File file) {

					if (!file.getName().equals("defaultsettings") && !file.getName().equals("defaultsettings.json") && !file.getName().equals("ds_dont_export.json") && !file.getName().equals("keys.txt") && !file.getName().equals("options.txt") && !file.getName().equals("optionsof.txt") && !file.getName().equals("optionsshaders.txt") && !file.getName().equals("servers.dat") && !new File(getMainFolder(), "sharedConfigs/" + file.getName()).exists() && file.getName().toLowerCase().startsWith(query.toLowerCase()))
						return true;

					return false;
				}
			};
		} else {
			ff = DefaultSettingsPlugin.fileFilter;
		}

		if (state == 1 || state == 2) {

		} else if (state == 0) {

		}

		mainJson.save();
	}

	public static void setActive(String name, boolean active) {
		mainJson.save();
	}

	public static void switchActive(String name) {
		mainJson.save();
	}

	public static void initialSetupJSON() throws UnknownHostException, SocketException, NoSuchAlgorithmException {

		getPrivateJSON();

		final String version = getMainJSON().getVersion();

		if (!JCPlugin.VERSION.equals(version))
			mainJson.setVersion(JCPlugin.VERSION).setPrevVersion(version);

		if (!privateJson.privateIdentifier.equals(mainJson.generatedBy) && !mainJson.generatedBy.equals("<default>")) {
			otherCreator = true;
		}

		mainJson.save();
	}

	public static PrivateJSON getPrivateJSON() {

		if (privateJson != null)
			return privateJson;
		final File privateFile = new File(mcDataDir, "ds_private_storage.json");
		if (privateFile.exists()) {
			try (Reader reader = new FileReader(privateFile)) {
				privateJson = gson.fromJson(reader, PrivateJSON.class);

				if (privateJson.privateIdentifier == null || privateJson.privateIdentifier.isEmpty())
					privateJson.privateIdentifier = UUID.randomUUID().toString();

				privateJson.save();

			} catch (Exception e) {
				JCPlugin.log.log(Level.ERROR, "Exception at processing startup: ", e);
			}

		} else {

			privateJson = new PrivateJSON();
			privateJson.privateIdentifier = UUID.randomUUID().toString();
			privateJson.save();
		}
		return privateJson;
	}

	public static MainJSON getMainJSON() {

		if (mainJson != null)
			return mainJson;

		File mainFile = new File(mcDataDir, "config/defaultsettings.json");

		if (mainFile.exists()) {
			try (Reader reader = new FileReader(mainFile)) {
				mainJson = gson.fromJson(reader, MainJSON.class);

			} catch (Exception e) {
				JCPlugin.log.log(Level.ERROR, "Exception at processing configs: ", e);
				if (e instanceof JsonSyntaxException) {
					mainFile.renameTo(new File(mcDataDir, "config/defaultsettings_malformed.json"));
					getMainJSON();
				}

			}

		} else {
			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

			mainJson = new MainJSON().setVersion("3.0.2").setCreated(formatter.format(date));

			// File fileDir = new File(mcDataDir, "config");
			// for (File file : fileDir.listFiles(fileFilter))
			// mainJson.activeConfigs.add(file.getName());

			mainJson.save();
		}
		return mainJson;
	}

	public static void setPopup(boolean active) {
		mainJson.save();
	}

	public static void restoreContents() throws NullPointerException, IOException, NoSuchAlgorithmException {

		new File(mcDataDir, "config").mkdir();
		
		initialSetupJSON();

		initialToDefaultProfile();

		String firstFolder = "<ERROR>";

		for (File file : getMainFolder().listFiles()) {
			if (file.isDirectory() && !file.getName().equals("sharedConfigs")) {
				firstFolder = file.getName();
				break;
			}
		}

		if (!new File(getMainFolder(), mainJson.mainProfile).exists())
			mainJson.mainProfile = firstFolder;

		if (privateJson.targetProfile.equals("!NEW!"))
			privateJson.targetProfile = mainJson.mainProfile;

		if (privateJson.currentProfile.equals("!NEW!"))
			privateJson.currentProfile = mainJson.mainProfile;

		if (!new File(getMainFolder(), privateJson.targetProfile).exists())
			privateJson.targetProfile = firstFolder;

		if (!new File(getMainFolder(), privateJson.currentProfile).exists())
			privateJson.currentProfile = firstFolder;

		privateJson.save();

		mainJson.save();

		boolean switchProf = switchProfile();

		activeProfile = privateJson.currentProfile;

		final File options = new File(mcDataDir, "options.txt");
		firstBootUp = !options.exists();
		if (firstBootUp) {
			restoreConfigs();
		} else if (switchProf) {
			restoreConfigs();
			mainJson.save();
		} else {
			copyAndHashPrivate(true, true);
			mainJson.save();
		}

	//	if (!options.exists())
	//		options.createNewFile();

	}

	private static boolean switchProfile() throws IOException {
		if (mainJson.generatedBy.equals("<default>"))
			mainJson.generatedBy = privateJson.privateIdentifier;
		if (!privateJson.currentProfile.equals(privateJson.targetProfile)) {

			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");

			String profileName = formatter.format(date);

			File fileDir = new File(getMainFolder(), profileName);
			fileDir.mkdir();

			activeProfile = profileName;

			moveAllConfigs();
			checkMD5(true, false, null); // TODO: This second "false" is a place holder

			String[] extensions = new String[] { "zip" };
			List<Path> oldestFiles = Collections.emptyList();

			Collection<File> files = FileUtils.listFiles(getMainFolder(), extensions, false);

			if (files.size() >= 10) {

				final List<Path> list2 = files.stream().map(File::toPath).collect(Collectors.toList());
				Comparator<? super Path> lastModifiedComparator = (p1, p2) -> Long.compare(p1.toFile().lastModified(), p2.toFile().lastModified());
				try (Stream<Path> paths = list2.stream()) {
					oldestFiles = paths.filter(Files::isRegularFile).sorted(lastModifiedComparator).limit(files.size() - 8).collect(Collectors.toList());
					oldestFiles.stream().forEach(t -> {
						try {
							Files.delete(t);
						} catch (IOException e) {
							JCPlugin.log.log(Level.ERROR, "Exception while processing profiles: ", e);
						}
					});
				}

			}

			Path pf = new File(getMainFolder(), profileName + ".zip").toPath();
			Files.createFile(pf);
			try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(pf))) {
				Path pt = Paths.get(fileDir.getPath());
				Files.walk(pt).filter(path -> !Files.isDirectory(path)).forEach(path -> {
					ZipEntry zipEntry = new ZipEntry(pt.relativize(path).toString());
					try {
						zos.putNextEntry(zipEntry);
						Files.copy(path, zos);
						zos.closeEntry();
					} catch (IOException e) {
						JCPlugin.log.log(Level.ERROR, "Exception while processing profiles: ", e);
					}
				});
			}

			try {
				deleted.add(fileDir.getName());

				FileUtils.deleteDirectory(fileDir);

			} catch (IOException e) {
				Thread thread = new Thread("File deletion thread") {
					public void run() {
						try {
							Thread.sleep(10000);
							FileUtils.deleteDirectory(fileDir);
						} catch (InterruptedException | IOException e) {

						}

					}
				};
				thread.start();
				try {
					FileUtils.forceDeleteOnExit(fileDir);
				} catch (IOException e1) {

				}

			}

			activeProfile = privateJson.targetProfile;
			privateJson.currentProfile = activeProfile;
			privateJson.save();

			return true;

		}
		return false;

	}

	private static void initialToDefaultProfile() {
		if (getMainJSON().mainProfile.equals("!NEW!")) {

			new File(getMainFolder(), "Default").mkdir();

			FileFilter ffm = new FileFilter() {

				@Override
				public boolean accept(File file) {
					if (!file.getName().equals("Default"))
						return true;

					return false;
				}
			};

			try {
				FileUtils.copyDirectory(getMainFolder(), new File(getMainFolder(), "Default"), ffm);
			} catch (IOException e) {
				JCPlugin.log.log(Level.ERROR, "Couldn't move config files: ", e);
			}

			for (File f : getMainFolder().listFiles(ffm)) {
				try {
					if (f.isDirectory())
						FileUtils.deleteDirectory(f);
					else
						Files.delete(f.toPath());
				} catch (IOException e) {
					JCPlugin.log.log(Level.ERROR, "Couldn't move config files: ", e);
				}
			}

			privateJson.targetProfile = "Default";
			privateJson.save();
			getMainJSON().mainProfile = "Default";
			mainJson.save();

		}
		String firstFolder = "<ERROR>";
		for (File file : getMainFolder().listFiles()) {
			if (file.isDirectory() && !file.getName().equals("sharedConfigs")) {
				firstFolder = file.getName();
				break;
			}
		}

		if (firstFolder.equals("<ERROR>")) {
			new File(getMainFolder(), "Default").mkdir();

			privateJson.targetProfile = "Default";
			privateJson.save();
			getMainJSON().mainProfile = "Default";
			mainJson.save();
		}
	}

	public static void copyAndHashPrivate(boolean options, boolean configs) throws NullPointerException, IOException {
		ArrayList<String> toRemove = new ArrayList<String>();

		if (options) {

		}

		if (configs) {

			File filec = new File(mcDataDir, "config");
			Collection<File> config = FileUtils.listFilesAndDirs(new File(getMainFolder(), activeProfile), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File configFile : config) {
				if (!configFile.isDirectory() && !configFile.getName().contains("defaultsettings") && !configFile.getName().equals("ignore.json") && !optUse.contains(configFile.getName())) {
					String relativePath = configFile.getPath().substring((mcDataDir.getPath().length()));
					String pathString = activeProfile + "/" + relativePath.split("defaultsettings")[1].substring(1).split(activeProfile)[1].substring(1);

					if (!privateJson.currentHash.containsKey(pathString) || (mainJson.hashes.containsKey(pathString) && !privateJson.currentHash.get(pathString).equals(mainJson.hashes.get(pathString)))) {

						if (mainJson.hashes.containsKey(pathString)) {
							FileUtils.copyFile(configFile, new File(filec, relativePath.split("defaultsettings")[1].substring(1).split(activeProfile)[1].substring(1)));
							privateJson.currentHash.put(pathString, mainJson.hashes.get(pathString));
						}

					}

				}
			}
		}

		privateJson.save();

		if (toRemove.size() > 0) {
			mainJson.save();
		}

	}

	public static boolean optionsFilesExist() {
		final File optionsFile = new File(getMainFolder(), activeProfile + "/options.txt");
		final File optionsofFile = new File(getMainFolder(), activeProfile + "/optionsof.txt");
		final File optionsShadersFile = new File(getMainFolder(), activeProfile + "/optionsshaders.txt");
		final File optionsJEKFile = new File(getMainFolder(), activeProfile + "/options.justenoughkeys.txt");
		final File optionsAmecsFile = new File(getMainFolder(), activeProfile + "/options.amecsapi.txt");
		return optionsFile.exists() || optionsofFile.exists() || optionsShadersFile.exists() || optionsJEKFile.exists() || optionsAmecsFile.exists();
	}

	public static void restoreConfigs() throws IOException {
		try {
			FileUtils.copyDirectory(new File(getMainFolder(), activeProfile), new File(mcDataDir, "config"), fileFilterModular);
		} catch (IOException e) {
			throw e;
		}

		FileUtils.listFilesAndDirs(new File(getMainFolder(), activeProfile), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).stream().filter(file -> !file.isDirectory()).forEach(file -> {
			try {
				privateJson.currentHash.put(activeProfile + "/" + file.getName(), fileToHash(new FileInputStream(file)));
			} catch (IOException e) {
				JCPlugin.log.log(Level.ERROR, "Process the files: ", e);
			}
		});

		mainJson.save();
		privateJson.save();
	}

	public static void moveAllConfigs() throws IOException {
		try {

			FileUtils.copyDirectory(new File(getMainFolder(), activeProfile), new File(mcDataDir, "config"), fileFilterAnti);

		} catch (IOException e) {
			throw e;
		}

		mainJson.save();
	}

	public static String getUUID(String uuid) throws NoSuchAlgorithmException {
		return stringToHash(uuid);
	}

	public static String stringToHash(String string) throws NoSuchAlgorithmException {
		return DigestUtils.md5Hex(string).toUpperCase();
	}

	public static String byteToHash(byte[] array) throws NoSuchAlgorithmException {
		return Hashing.sha256().hashBytes(array).toString();
	}

	public static String fileToHash(InputStream is) throws IOException {
		return DigestUtils.md5Hex(is).toUpperCase();
	}

	public static boolean checkForConfigFiles() {
		try {

			Collection<File> config = FileUtils.listFilesAndDirs(new File(getMainFolder(), activeProfile), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File configFile : config) {
				if (!(configFile.getName().equals(activeProfile) || configFile.getName().contains("defaultsettings") || optUse.contains(configFile.getName())))
					return false;
			}

		} catch (Exception e) {
			JCPlugin.log.log(Level.ERROR, "Error while saving configs: ", e);
		}

		return true;
	}

	public static boolean checkChangedConfig() {
		boolean ret = false;
		try {

			Collection<File> config = FileUtils.listFilesAndDirs(new File(getMainFolder(), activeProfile), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File configFile : config) {
				if (!configFile.isDirectory() && !configFile.getName().equals("ignore.json") && !configFile.getName().contains("defaultsettings") && !optUse.contains(configFile.getName())) {
					String relativePath = configFile.getPath().substring((mcDataDir.getPath().length()));
					String pathString = activeProfile + "/" + relativePath.split("defaultsettings")[1].substring(1).split(activeProfile)[1].substring(1);

					if (!mainJson.hashes.containsKey(pathString))
						continue;

					String hashC = fileToHash(new FileInputStream(configFile));

					String writtenHashS = mainJson.hashes.get(pathString);

					if (!hashC.equals(writtenHashS)) {
						ret = true;
					}
				}
			}

		} catch (Exception e) {
			JCPlugin.log.log(Level.ERROR, "Error while saving configs: ", e);
		}

		return ret;
	}

	public static boolean checkChanged() {
		boolean ret = false;
		try {

		} catch (Exception e) {
			JCPlugin.log.log(Level.ERROR, "Error while saving configs: ", e);
		}

		return ret;
	}

	public static ArrayList<String> listConfigFiles() throws FileNotFoundException, IOException {
		ArrayList<String> files = new ArrayList<String>();
		for (File configFile : new File(getMainFolder(), activeProfile).listFiles()) {
			if (!configFile.getName().equals("ignore.json") && !configFile.getName().contains("defaultsettings")) {
				String relativePath = configFile.getPath().substring((mcDataDir.getPath().length()));
				String pathString = relativePath.split("defaultsettings")[1].substring(1).split(activeProfile)[1].substring(1);
				files.add(pathString);
			}
		}
		return files;
	}
	
	public static final ArrayList<String> optUse = new ArrayList<String>() {
		private static final long serialVersionUID = -6765486158086901202L;
		{
			add("options.txt");
			add("servers.dat");
			add("optionsof.txt");
			add("optionsshaders.txt");
			add("options.justenoughkeys.txt");
			add("options.amecsapi.txt");
			add("keys.txt");
		}
	};

	public static void checkMD5(boolean updateExisting, boolean configs, String file) throws FileNotFoundException, IOException {
		Collection<File> config = null;
		File dir = new File(getMainFolder(), activeProfile);
		if (file == null) {
			config = FileUtils.listFilesAndDirs(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		} else {
			config = FileUtils.listFilesAndDirs(new File(dir, file), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		}

		for (File configFile : config) {
			if (!configFile.isDirectory() && !configFile.getName().equals("ignore.json") && !configFile.getName().contains("defaultsettings")) {
				if (optUse.contains(configFile.getName()) && configs)
					continue;
				String relativePath = configFile.getPath().substring((mcDataDir.getPath().length()));
				String pathString = activeProfile + "/" + relativePath.split("defaultsettings")[1].substring(1).split(activeProfile)[1].substring(1);
				if (!updateExisting && mainJson.hashes.containsKey(pathString)) {

				} else {
					mainJson.hashes.put(pathString, fileToHash(new FileInputStream(configFile)));
				}
			}
		}

		mainJson.save();

	}
	
}
