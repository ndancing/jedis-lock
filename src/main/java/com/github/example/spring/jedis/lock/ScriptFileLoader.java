package com.github.example.spring.jedis.lock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

final class ScriptFileLoader {

	private ScriptFileLoader() {}

	private static final Map<String, String> SCRIPT_FILE_CONTENTS = new ConcurrentHashMap<>(4);

	static String load(String scriptFilePath) throws IOException {
		String scriptContent = SCRIPT_FILE_CONTENTS.get(scriptFilePath);
		if (Objects.nonNull(scriptContent)) {
			return scriptContent;
		}
		synchronized (ScriptFileLoader.class) {
			scriptContent = SCRIPT_FILE_CONTENTS.get(scriptFilePath);
			if (Objects.nonNull(scriptContent)) {
				return scriptContent;
			}
			scriptContent = readFile(scriptFilePath);
			SCRIPT_FILE_CONTENTS.put(scriptFilePath, scriptContent);
		}
		return scriptContent;
	}

	private static String readFile(String scriptFilePath) throws IOException {
		final StringBuilder sb = new StringBuilder();
		InputStream stream = ScriptFileLoader.class.getClassLoader().getResourceAsStream(scriptFilePath);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))){
			String str;
			while ((str = br.readLine()) != null) {
				sb.append(str).append(System.lineSeparator());
			}
		}
		return sb.toString();
	}
}
