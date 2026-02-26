package com.back.global.redis

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RedisExampleCustomRepository : CrudRepository<RedisCustomEntity, Int>
