package net.jomcraft.jcplugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import org.apache.logging.log4j.Level;

public class PrivateJSON {

	public String information = "This file is part of the internal structure of the DefaultSettings mod. Do NOT edit it! If you are a modpack creator, also do not include it in your pack export! This will break the mod!";
	public static transient final long serialVersionUID = 498123L;
	public HashMap<String, String> currentHash = new HashMap<String, String>();
	public String targetProfile = "!NEW!";
	public String currentProfile = "!NEW!";
	public String privateIdentifier = null;
	public boolean firstBootUp = true;
	public boolean disableCreatorCheck = false;

	public void save() {
		try (FileWriter writer = new FileWriter(new File(FileUtilNoMC.mcDataDir, "ds_private_storage.json"))) {
			FileUtilNoMC.gson.toJson(this, writer);
		} catch (IOException e) {
			JCLogger.log.log(Level.ERROR, "Exception at processing startup: ", e);
		}
	}
}