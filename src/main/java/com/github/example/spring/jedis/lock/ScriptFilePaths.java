package com.github.example.spring.jedis.lock;

final class ScriptFilePaths {

	private ScriptFilePaths() {
	}

	static final String LOCK_SCRIPT = "lock/scripts/lock.lua";
	static final String UNLOCK_SCRIPT = "lock/scripts/unlock.lua";
	static final String FORCE_UNLOCK_SCRIPT = "lock/scripts/force_unlock.lua";
	static final String UPDATE_TTL_SCRIPT = "lock/scripts/update_ttl.lua";

}
