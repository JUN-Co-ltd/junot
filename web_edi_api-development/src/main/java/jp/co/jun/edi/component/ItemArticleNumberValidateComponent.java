package jp.co.jun.edi.component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.SkuModel;
import jp.co.jun.edi.type.JanType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.RegistStatusType;

/**
 * 品番検証用のコンポーネント.
 */
@Component
public class ItemArticleNumberValidateComponent extends GenericComponent {
    /** SKUのリソース名. */
    private static final String RESOURCE_SKU = "sku";

    @Autowired
    private MJanNumberComponent mJanNumberComponent;

    /**
     * JANコードバリデーションチェック.
     *
     * @param itemModel SKUの値がセットされている品番情報
     * @return エラーチェック内容のList
     */
    public List<ResultMessage> validateArticleNumber(final ItemModel itemModel) {
        final RegistStatusType registStatus = RegistStatusType.convertToType(itemModel.getRegistStatus());

        final List<ResultMessage> resultMessages = new ArrayList<>();

        switch (itemModel.getJanType()) {

        case OTHER_JAN: // 他社JAN
            validateOtherJan(itemModel.getSkus(), registStatus, resultMessages);
            break;
        case OTHER_UPC: // 他社UPC
            validateUpc(itemModel.getSkus(), registStatus, resultMessages);
            break;
        default:
            break;
        }

        return resultMessages;
    }

    /**
     * SKUのリストから空文字以外のJAN/UPCを抽出する.
     *
     * @param skuList SKUのリスト
     * @return JAN/UPCを抽出したリスト SKUリストが空の場合は空の配列を戻す.
     */
    private List<String> extractJanList(final List<SkuModel> skuList) {
        return skuList.stream()
                .filter(sku -> StringUtils.isNotEmpty(sku.getJanCode()))
                .map(sku -> sku.getJanCode())
                .collect(Collectors.toList());
    }

    /**
     * JANコードのバリデーションチェック.
     *
     * @param skuList SKUのリスト
     * @param registStatus 登録ステータス
     * @param resultMessages ResultMessageのリスト。エラーがある場合、リストに追加される。
     */
    private void validateOtherJan(final List<SkuModel> skuList, final RegistStatusType registStatus, final List<ResultMessage> resultMessages) {
        // SKUのリストから空文字以外のJAN/UPCを抽出
        final List<String> articleNumberList = extractJanList(skuList);

        if ((RegistStatusType.PART == registStatus) && (skuList.size() != articleNumberList.size())) {
            // 品番登録の場合、1件でも空のJANコードがある場合、必須エラー
            resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_I_22).resource(RESOURCE_SKU));
        }

        // JANの設定があるもののみチェック
        skuList.stream()
                .filter(skuModel -> StringUtils.isNotEmpty(skuModel.getJanCode()))
                .forEach(skuModel -> {
                    // 社内JAN枠範囲内エラーチェック
                    if (mJanNumberComponent.isInternalRange(skuModel.getJanCode())) {
                        resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_I_18, skuModel.getJanCode()).resource(RESOURCE_SKU));
                    }
                    // 登録済チェック
                    if (mJanNumberComponent.isRegistered(skuModel.getId(),
                            // 先頭ゼロ埋め
                            mJanNumberComponent.zeroPaddingArticleNumber(JanType.OTHER_JAN, skuModel.getJanCode()))) {
                        resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_I_19, skuModel.getJanCode()).resource(RESOURCE_SKU));
                    }
                    // 重複エラーチェック
                    if (mJanNumberComponent.isDuplicate(skuModel.getJanCode(), articleNumberList)) {
                        resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_I_20, skuModel.getJanCode()).resource(RESOURCE_SKU));
                    }
                    // チェックデジットチェック
                    if (!mJanNumberComponent.isCorrectCheckDigit(skuModel.getJanCode())) {
                        resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_I_21, skuModel.getJanCode()).resource(RESOURCE_SKU));
                    }
                });
    }

    /**
     * UPCコードのバリデーションチェック.
     *
     * @param skuList SKUのリスト
     * @param registStatus 登録ステータス
     * @param resultMessages ResultMessageのリスト。エラーがある場合、リストに追加される。
     */
    private void validateUpc(final List<SkuModel> skuList, final RegistStatusType registStatus, final List<ResultMessage> resultMessages) {
        // SKUのリストから空文字以外のJAN/UPCを抽出
        final List<String> articleNumberList = extractJanList(skuList);

        if ((RegistStatusType.PART == registStatus) && (skuList.size() != articleNumberList.size())) {
            // 品番登録の場合、1件でも空のJANコードがある場合、必須エラー
            resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_I_22).resource(RESOURCE_SKU));
        }

        // JANの設定があるもののみチェック
        skuList.stream()
                .filter(skuModel -> StringUtils.isNotEmpty(skuModel.getJanCode()))
                .forEach(skuModel -> {
                    // 登録済チェック
                    if (mJanNumberComponent.isRegistered(skuModel.getId(),
                            // 先頭ゼロ埋め
                            mJanNumberComponent.zeroPaddingArticleNumber(JanType.OTHER_UPC, skuModel.getJanCode()))) {
                        resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_I_19, skuModel.getJanCode()).resource(RESOURCE_SKU));
                    }
                    // 重複エラーチェック
                    if (mJanNumberComponent.isDuplicate(skuModel.getJanCode(), articleNumberList)) {
                        resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_I_20, skuModel.getJanCode()).resource(RESOURCE_SKU));
                    }
                    // チェックデジットチェック
                    if (!mJanNumberComponent.isCorrectCheckDigit(skuModel.getJanCode())) {
                        resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_I_21, skuModel.getJanCode()).resource(RESOURCE_SKU));
                    }
                });
    }
}
