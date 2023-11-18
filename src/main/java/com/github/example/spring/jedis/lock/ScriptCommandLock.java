package com.github.example.spring.jedis.lock;

public abstract class ScriptCommandLock<T extends ScriptCommand> implements Lock {

	protected final T scriptCommand;

	protected ScriptCommandLock(T scriptCommand) {
		this.scriptCommand = scriptCommand;
	}

	protected abstract Long executeScriptCommand(Script script, int keyCount, String... params);
}
