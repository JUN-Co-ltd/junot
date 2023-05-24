package jp.co.jun.edi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 */
public abstract class GenericService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * @return {@link Logger} instance
     */
    protected Logger getLog() {
        return log;
    }

    /**
     * @param value ログに出力するパラメーター/レスポンス
     * @return パラメーター/レスポンスのJSON文字列
     */
    protected String toJsonString(final Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException occurred.", e);

            return null;
        }
    }

}
