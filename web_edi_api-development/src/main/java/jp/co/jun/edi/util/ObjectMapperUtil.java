package jp.co.jun.edi.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * ObjectMapperユーティリティ.
 * ObjectMapperのインスタンス生成は時間がかかるため、JSON形式の変換は、基本的にはこのユーティリティを利用すること。
 * （read/wirte中にObjectMapperの設定が変更されなければスレッドセーフである。）
 */
@Slf4j
public final class ObjectMapperUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     */
    private ObjectMapperUtil() {
    }

    /**
     * ObjectMapperを返却する.
     *
     * @return ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * JSONオブジェクトからJSON文字列に変換する.
     * 変換に失敗した場は、空の文字列を返却する.
     *
     * @param value JSONオブジェクト
     * @return JSON文字列
     */
    public static String writeValueAsString(final Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn(LogStringUtil.of("writeValueAsString").exception(e).build());
            return "";
        }
    }

    /**
     * JSONオブジェクトからJSONバイト配列に変換する.
     * 変換に失敗した場は、空のバイト配列を返却する.
     *
     * @param value JSONオブジェクト
     * @return JSONバイト配列
     */
    public static byte[] writeValueAsBytes(final Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            log.warn(LogStringUtil.of("writeValueAsBytes").exception(e).build());
            return new byte[0];
        }
    }

    /**
     * JSON文字列からJSONオブジェクトに変換する.
     * 変換に失敗した場は、nullを返却する.
     *
     * @param <T> JSONオブジェクトのクラス
     * @param content JSONオブジェクト
     * @param valueType JSONオブジェクトのクラス
     * @return JSONオブジェクト
     */
    public static <T> T readValue(final String content, final Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(content, valueType);
        } catch (JsonParseException e) {
            log.warn(LogStringUtil.of("readValue").exception(e).build());
        } catch (JsonMappingException e) {
            log.warn(LogStringUtil.of("readValue").exception(e).build());
        } catch (IOException e) {
            log.warn(LogStringUtil.of("readValue").exception(e).build());
        }

        return null;
    }
}
