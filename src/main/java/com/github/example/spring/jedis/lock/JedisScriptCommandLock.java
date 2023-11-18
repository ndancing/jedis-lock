package com.github.example.spring.jedis.lock;

import redis.clients.jedis.exceptions.JedisNoScriptException;

public abstract class JedisScriptCommandLock extends ScriptCommandLock<JedisScriptCommand> {

	protected JedisScriptCommandLock(JedisScriptCommand scriptCommand) {
		super(scriptCommand);
	}

	@Override
	protected Long executeScriptCommand(Script script, int keyCount, String... params) {
		Long result;
		try {
			result = (Long)scriptCommand.evalsha(script.sha(), keyCount, params);
		} catch (JedisNoScriptException e) {
			result = (Long)scriptCommand.eval(script.script(), keyCount, params);
		}
		return result;
	}

	protected abstract void subscribe();

	protected abstract void unsubscribe();

}
