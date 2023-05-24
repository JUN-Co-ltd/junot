package jp.co.jun.edi.model;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * 品番・商品一括登録用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class BulkRegistItemModel extends GenericModel {
    private static final long serialVersionUID = 1L;

    /** 結果リスト. */
    private List<BulkRegistItemResultModel> results;

    /** エラーリスト. */
    private List<String> errors;

    /** 品番リスト(画面に返却しない). */
    @JsonIgnore
    private List<ItemModel> items;

    @Override
    public Object getLogObject() {
        if (log.isDebugEnabled()) {
            return toDebugLogObject(this);
        }

        return toLogObject(this);
    }

    /**
     * ログ出力用オブジェクトに変換.
     *
     * @param model {@link BulkRegistItemModel} instance
     * @return ログ出力用オブジェクト
     */
    public static LinkedHashMap<String, Object> toDebugLogObject(final BulkRegistItemModel model) {
        return new LinkedHashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("results", model.getResults());
                put("errors", model.getErrors());
                put("items", model.getItems());
            }
        };
    }

    /**
     * ログ出力用オブジェクトに変換.
     *
     * @param model {@link BulkRegistItemModel} instance
     * @return ログ出力用オブジェクト
     */
    private static LinkedHashMap<String, Object> toLogObject(final BulkRegistItemModel model) {
        return new LinkedHashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("resultCount", CollectionUtils.size(model.getResults()));
                put("errorCount", CollectionUtils.size(model.getErrors()));
                put("itemCount", CollectionUtils.size(model.getItems()));
            }
        };
    }
}
