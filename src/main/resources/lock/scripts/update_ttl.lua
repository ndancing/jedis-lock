if (redis.call('HEXISTS', KEYS[1], ARGV[1]) == 1) then
    redis.call('PEXPIRE', KEYS[1], ARGV[2]);
end ;