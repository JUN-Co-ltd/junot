package jp.co.jun.edi.component;

import java.io.IOException;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.model.SearchCondition;
import lombok.extern.slf4j.Slf4j;

/**
 * 検索条件のコンポーネント.
 */
@Slf4j
@Component
public class SearchConditionComponent {
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * モデルを取得します.
     *
     * @param <T> SearchConditionのインタフェースを持つクラス
     * @param searchCondition {@link SearchCondition} instance
     * @param valueType SearchConditionのインタフェースを持つクラス
     * @return {@link SearchCondition} instance
     */
    public <T extends SearchCondition> T getSearchCondition(final T searchCondition, final Class<T> valueType) {
        if (StringUtils.isNotEmpty(searchCondition.getPageToken())) {
            try {
                final T localSearchCondition = objectMapper.readValue(
                        Base64.getDecoder().decode(searchCondition.getPageToken()),
                        valueType);

                log.info(localSearchCondition.toString());

                return localSearchCondition;
            } catch (IOException e) {
                log.warn("IOException.", e);
            }
        }

        return searchCondition;
    }

    /**
     * NextPageTokenを取得します.
     *
     * @param <T> SearchConditionのインタフェースを持つクラス
     * @param searchCondition {@link SearchCondition} instance
     * @param nextPage 次のページ
     * @return NextPageToken
     */
    public <T extends SearchCondition> String getNextPageToken(
            final T searchCondition,
            final boolean nextPage) {
        String nextPageToken = "";

        if (!nextPage) {
            return nextPageToken;
        }

        searchCondition.setPageToken(null);
        searchCondition.setPage(searchCondition.getPage() + 1);

        try {
            nextPageToken = Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(searchCondition));
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException.", e);
        }

        return nextPageToken;
    }
}
