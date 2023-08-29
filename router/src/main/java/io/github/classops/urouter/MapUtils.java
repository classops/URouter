package io.github.classops.urouter;

import androidx.collection.ArrayMap;
import androidx.core.util.Pair;

import java.util.Map;

/**
 * 类名：MapUtils <br/>
 * 描述：ArrayMap工具类
 * 创建时间：2022/11/19 21:46
 *
 * @author hanter
 * @version 1.0
 */
public class MapUtils {

    public static final class MapBuilder {

        private final Map<String, Integer> map = new ArrayMap<>();

        public MapBuilder add(String name, Integer type) {
            this.map.put(name, type);
            return this;
        }

        public Map<String, Integer> build() {
            return this.map;
        }

    }

    public static MapBuilder newBuilder() {
        return new MapBuilder();
    }

    @SafeVarargs
    public static Map<String, Integer> of(Pair<String, Integer>... params) {
        Map<String, Integer> paramsType = new ArrayMap<>();

        for (Pair<String, Integer> param : params) {
            paramsType.put(param.first, param.second);
        }
        return paramsType;
    }

}
