package jp.co.jun.edi.util;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * ログ出力文字列生成用ユーティリティ.
 */
@Slf4j
public final class LogStringUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     */
    private LogStringUtil() {
    }

    /**
     * ログ文字列構築用のビルダーを生成する.
     *
     * @param name ログ名称
     * @return {@link LogStringBuilder}
     */
    public static LogStringBuilder of(final String name) {
        return new LogStringBuilder(name);
    }

    /**
     * Mapビルダーを生成する.
     *
     * @return {@link MapBuilder}
     */
    public static MapBuilder ofMap() {
        return new MapBuilder();
    }

    /**
     * ログ文字列構築用のビルダー.
     */
    public static class LogStringBuilder {
        private final Map<String, Map<String, Object>> baseMap;
        private final Map<String, Object> valueMap;

        /**
         * @param name ログ名称
         */
        public LogStringBuilder(final String name) {
            baseMap = new LinkedHashMap<>(1);
            valueMap = new LinkedHashMap<>();
            baseMap.put(name, this.valueMap);
        }

        /**
         * ログメッセージを設定する.
         *
         * @param message ログメッセージ
         * @return {@link LogStringBuilder}
         */
        public LogStringBuilder message(final String message) {
            this.valueMap.put("message", message);
            return this;
        }

        /**
         * 例外を設定する.
         *
         * @param exception 例外
         * @return {@link LogStringBuilder}
         */
        public LogStringBuilder exception(final Exception exception) {
            final Map<String, String> map = new LinkedHashMap<>(1);
            map.put("message", exception.getMessage());
            this.valueMap.put(exception.getClass().getName(), map);
            return this;
        }

        /**
         * 値を設定する.
         *
         * @param key キー
         * @param value 値
         * @return {@link LogStringBuilder}
         */
        public LogStringBuilder value(final String key, final Object value) {
            this.valueMap.put(key, value);
            return this;
        }

        /**
         * @return ログ文字列
         */
        public String build() {
            try {
                return OBJECT_MAPPER.writeValueAsString(baseMap);
            } catch (JsonProcessingException e) {
                log.warn("JsonProcessingException", e);
                return "";
            }
        }
    }

    /**
     * Mapビルダー.
     */
    public static class MapBuilder {
        private final Map<String, Object> map;

        /**
         * Mapビルダーを生成.
         */
        public MapBuilder() {
            map = new LinkedHashMap<>();
        }

        /**
         * Mapに値を登録する.
         *
         * @param key キー
         * @param value 値
         * @return {@link MapBuilder}
         */
        public MapBuilder put(final String key, final Object value) {
            map.put(key, value);
            return this;
        }

        /**
         * @return {@link Map}
         */
        public Map<String, Object> toMap() {
            return map;
        }
    }
}
