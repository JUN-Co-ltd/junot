package jp.co.jun.edi.util.xlsx.bulkRegistItem;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.type.JanType;
import jp.co.jun.edi.type.RegistStatusType;
import jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet.CompositionSheet;
import jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet.ItemSheet;
import jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet.SkuJanSheet;
import jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet.SkuUpcSheet;
import lombok.Builder;
import lombok.Data;

/**
 * 品番・商品一括登録用のデータ.
 */
@Data
@Builder
public class BulkRegistItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_CODE_PREFIX = "{jp.co.jun.edi.util.xlsx.bulkRegistItem.BulkRegistItem";

    /** SKUのMax. */
    private static final int SKU_MAX = 50;

    /** 組成のMax. */
    private static final int COMPOSITION_MAX = SKU_MAX * 10;

    /** 品番. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".partNo.NotBlank}", groups = { Default.class })
    private final String partNo;

    /** 品番・商品のリスト. */
    @NotNull(message = MESSAGE_CODE_PREFIX + ".items.NotNull}", groups = { Default.class })
    @Size(message = MESSAGE_CODE_PREFIX + ".items.Size}", min = 1, max = 1, groups = { Default.class })
    @Valid
    private final List<ItemSheet> items;

    /** SKU(JAN)のリスト. */
    @NotNull(message = MESSAGE_CODE_PREFIX + ".skuJans.NotNull}", groups = { Default.class })
    @Size(message = MESSAGE_CODE_PREFIX + ".skuJans.Size}", max = SKU_MAX, groups = { Default.class })
    @Valid
    private final List<SkuJanSheet> skuJans;

    /** SKU(UPC)のリスト. */
    @NotNull(message = MESSAGE_CODE_PREFIX + ".skuUpcs.NotNull}", groups = { Default.class })
    @Size(message = MESSAGE_CODE_PREFIX + ".skuUpcs.Size}", max = SKU_MAX, groups = { Default.class })
    @Valid
    private final List<SkuUpcSheet> skuUpcs;

    /** 組成のリスト. */
    @NotNull(message = MESSAGE_CODE_PREFIX + ".compositions.NotNull}", groups = { Default.class })
    @Size(message = MESSAGE_CODE_PREFIX + ".compositions.Size}", max = COMPOSITION_MAX, groups = { Default.class })
    @Valid
    private final List<CompositionSheet> compositions;

    /**
     * {@link ItemModel} に変換する.
     *
     * @param row {@link BulkRegistItem} instance.
     * @param registStatus {@link RegistStatusType}
     * @return {@link ItemModel} instance.
     */
    public static ItemModel toItem(
            final BulkRegistItem row,
            final RegistStatusType registStatus) {
        final ItemModel item = ItemSheet.toItem(row.getItems().get(0), registStatus);

        if (CollectionUtils.isNotEmpty(row.getSkuJans())) {
            if (row.getSkuJans().stream().anyMatch(sku -> StringUtils.isNotEmpty(sku.getJanCode()))) {
                // 1件でも他社JANが指定されている場合、他社JANを設定
                item.setJanType(JanType.OTHER_JAN);
            } else {
                item.setJanType(JanType.IN_HOUSE_JAN);
            }

            item.setSkus(row.getSkuJans().stream().map(SkuJanSheet::toSku).collect(Collectors.toList()));
        } else if (CollectionUtils.isNotEmpty(row.getSkuUpcs())) {
            item.setJanType(JanType.OTHER_UPC);
            item.setSkus(row.getSkuUpcs().stream().map(SkuUpcSheet::toSku).collect(Collectors.toList()));
        } else {
            item.setJanType(JanType.IN_HOUSE_JAN);
            item.setSkus(Collections.emptyList());
        }

        if (CollectionUtils.isNotEmpty(row.getCompositions())) {
            item.setCompositions(row.getCompositions().stream().map(CompositionSheet::toComposition).collect(Collectors.toList()));
        } else {
            item.setCompositions(Collections.emptyList());
        }

        return item;
    }

    /**
     * 品番の昇順でソート.
     *
     * @param rows 行リスト
     */
    public static void sort(
            final List<BulkRegistItem> rows) {
        rows.sort(Comparator.comparing(BulkRegistItem::getPartNo));
    }
}
