package com.github.example.spring.jedis.lock;

public interface ScriptCommand {

	Object eval(String script, int keyCount, String... params);

	String scriptLoad(String script);

	Object evalsha(String sha, int keyCount, String... params);

}
