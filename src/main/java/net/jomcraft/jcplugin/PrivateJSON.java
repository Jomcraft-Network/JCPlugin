package net.jomcraft.jcplugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.apache.logging.log4j.Level;

public class PrivateJSON {

	public static transient final long serialVersionUID = 498123L;
	public HashMap<String, String> currentHash = new HashMap<String, String>();
	public String targetProfile = "!NEW!";
	public String currentProfile = "!NEW!";
	public String privateIdentifier = null;

	public void save() {
		try (FileWriter writer = new FileWriter(new File(DefaultSettingsPlugin.mcDataDir, "ds_private_storage.json"))) {
			DefaultSettingsPlugin.gson.toJson(this, writer);
		} catch (IOException e) {
			JCPlugin.log.log(Level.ERROR, "Exception at processing startup: ", e);
		}
	}
}