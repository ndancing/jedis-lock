if (redis.call('HEXISTS', KEYS[1], ARGV[1]) == 1) then
    redis.call('HINCRBY', KEYS[1], ARGV[1], -1);
    redis.call('PEXPIRE', KEYS[1], ARGV[2]);
    if (tonumber(redis.call('HGET', KEYS[1], ARGV[1])) < 1) then
        redis.call('DEL', KEYS[1]);
        redis.call('PUBLISH', KEYS[1], 1);
        return 1;
    end ;
    return 2;
end ;
return 0;