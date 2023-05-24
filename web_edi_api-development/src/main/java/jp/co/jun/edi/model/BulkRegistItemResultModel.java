package jp.co.jun.edi.model;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.util.xlsx.bulkRegistItem.BulkRegistItem;
import jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet.ItemSheet;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 品番・商品一括登録の結果用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class BulkRegistItemResultModel extends GenericModel {
    private static final long serialVersionUID = 1L;

    /** ブランドコード. */
    private String brandCode;

    /** 取込商品・品番数. */
    private int itemCount = 0;

    /** エラー商品・品番数. */
    private int errorItemCount = 0;

    /** 取込SKU数. */
    private int skuCount = 0;

    /** エラーSKU数. */
    private int errorSkuCount = 0;

    /**
     * ブランドコードでマージして、ブランドコードの昇順でソート.
     *
     * @param rows マージ前のリスト
     * @return マージ後のリスト
     */
    public static List<BulkRegistItemResultModel> mergeAndSort(
            final List<BulkRegistItemResultModel> rows) {
        return rows.stream().collect(Collectors.groupingBy(BulkRegistItemResultModel::getBrandCode)).values().stream().map(mapper -> {
            final BulkRegistItemResultModel result = new BulkRegistItemResultModel();

            result.setBrandCode(mapper.get(0).getBrandCode());

            mapper.forEach(v -> {
                result.setItemCount(result.getItemCount() + v.getItemCount());
                result.setErrorItemCount(result.getErrorItemCount() + v.getErrorItemCount());
                result.setSkuCount(result.getSkuCount() + v.getSkuCount());
                result.setErrorSkuCount(result.getErrorSkuCount() + v.getErrorSkuCount());
            });

            return result;
        }).sorted(Comparator.comparing(BulkRegistItemResultModel::getBrandCode)).collect(Collectors.toList());
    }

    /**
     * エラーの {@link BulkRegistItemResultModel} に変換する.
     *
     * @param row {@link BulkRegistItem} instance.
     * @return {@link BulkRegistItemResultModel} instance.
     */
    public static BulkRegistItemResultModel toErrorResult(
            final BulkRegistItem row) {
        final BulkRegistItemResultModel result = new BulkRegistItemResultModel();
        result.setBrandCode(ItemSheet.getBrandCode(row.getPartNo()));
        result.setErrorItemCount(1);
        result.setErrorSkuCount(CollectionUtils.size(row.getSkuJans()) + CollectionUtils.size(row.getSkuUpcs()));

        return result;
    }

    /**
     * エラーの {@link BulkRegistItemResultModel} に変換する.
     *
     * @param item {@link ItemModel} instance.
     * @return {@link BulkRegistItemResultModel} instance.
     */
    public static BulkRegistItemResultModel toErrorResult(
            final ItemModel item) {
        final BulkRegistItemResultModel result = new BulkRegistItemResultModel();
        result.setBrandCode(item.getBrandCode());
        result.setErrorItemCount(1);
        result.setErrorSkuCount(CollectionUtils.size(item.getSkus()));

        return result;
    }

    /**
     * 正常の {@link BulkRegistItemResultModel} に変換する.
     *
     * @param item {@link ItemModel} instance.
     * @return {@link BulkRegistItemResultModel} instance.
     */
    public static BulkRegistItemResultModel toNormalResult(
            final ItemModel item) {
        final BulkRegistItemResultModel result = new BulkRegistItemResultModel();
        result.setBrandCode(item.getBrandCode());
        result.setItemCount(1);
        result.setSkuCount(CollectionUtils.size(item.getSkus()));

        return result;
    }
}
