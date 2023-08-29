package io.github.classops.urouter.demo.service

import com.google.gson.Gson
import io.github.classops.urouter.annotation.Route
import io.github.classops.urouter.service.SerializationService
import java.lang.reflect.Type

/**
 * GsonService
 *
 * @author wangmingshuo
 * @since 2023/04/21 16:27
 */
@Route(path = "serialization_service")
class GsonService : SerializationService {

    private val gson = Gson()

    override fun <T : Any?> parseObject(json: String?, type: Type): T {
        return gson.fromJson(json, type)
    }

    override fun toJson(obj: Any?): String? {
        return gson.toJson(obj)
    }

}