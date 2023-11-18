if (redis.call('DEL', KEYS[1]) == 1) then
    redis.call('PUBLISH', KEYS[1], 1);
    return 1;
end ;
return 0;