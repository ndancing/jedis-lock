package com.github.example.spring.jedis.lock;

import java.io.IOException;

import com.github.example.spring.jedis.lock.exception.JedisLockException;

final class JedisLockScript implements Script {

	private String script;
	private String sha;

	JedisLockScript(String script, String sha) {
		this.script = script;
		this.sha = sha;
	}

	@Override
	public String script() {
		return this.script;
	}

	@Override
	public String sha() {
		return this.sha;
	}

	static String getScriptFromFilePath(String scriptFilePath) {
		try {
			return ScriptFileLoader.load(scriptFilePath);
		} catch (IOException e) {
			throw new JedisLockException("Error when loading script", e);
		}
	}
}
