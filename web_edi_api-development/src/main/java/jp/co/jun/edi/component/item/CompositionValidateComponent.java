package jp.co.jun.edi.component.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.GenericComponent;
import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.model.MItemModel;
import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.entity.MItemPartsEntity;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.model.CompositionModel;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.SkuModel;
import jp.co.jun.edi.type.ItemValidationType;
import jp.co.jun.edi.type.MCodmstCompositionsRequiredType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.RegistStatusType;

/**
 * 混率(パーツ、組成、パーセント)関連のコンポーネント.
 */
@Component
public class CompositionValidateComponent extends GenericComponent {

    /** 共通のカラーコード. */
    private static final String COMMON_COLOR_CODE = "00";

    /** 組成のリソース名. */
    private static final String RESOURCE_COMPOSITION = "composition";

    /** パーセント合計(100). */
    private static final int PERCENT_SUM = 100;

    /** パーセント合計(0). */
    private static final int PERCENT_SUM_ZERO = 0;

    @Autowired
    private ItemComponent itemComponent;

    /**
     * 組成の検証（パーツ＝部位）.
     *
     * <pre>
     * ＜フクキタルとの連携について＞
     * - パーツが「その他」の場合、パーツは空で連携する。
     * - 混率が「0」の場合、混率は空で連携する。
     * - パーツが全て空または「その他」かつ、素材が全て「その他」の場合、パーツ、素材、混率はすべて空で連携する。
     * - パーツのみの入力も許可する。
     * </pre>
     *
     * <pre>
     * ＜検証内容＞
     * - 一括登録の場合、組成に紐づくカラーコードがSKUに存在しなければ、エラー。
     * - カラー別の組成の検証。
     *   {@link CompositionValidateComponent#validateCompositionsByColor(List, String, List, MItemModel, MCodmstCompositionsRequiredType, boolean)}
     * - 品番登録 または 登録モードが品番かつ、素材が必須の場合、共通の組成が存在しないとエラー。
     * </pre>
     *
     * @param masterData 品番に関連するマスタデータ
     * @param item 品番情報
     * @param registStatus 登録ステータス
     * @param isBulkRegist 一括登録か
     * @return エラーメッセージリスト
     */
    public List<ResultMessage> validateCompositions(final MItemModel masterData, final ItemModel item,
            final RegistStatusType registStatus, final boolean isBulkRegist) {
        // 戻り値：組成エラーメッセージのリスト
        final List<ResultMessage> errorMessages = new ArrayList<>();

        // アイテムコードに紐づくコードマスタのITEM7から組成の必須有無を取得
        final MCodmstCompositionsRequiredType compositionsRequiredType = MCodmstCompositionsRequiredType.convertToType(masterData.getItem().getItem7());

        // SKUのリストから、カラーコードのセットに変換する
        final Set<String> colorCodeSet = toColorCodeSet(item.getSkus());

        // 組成をカラー別にグルーピング
        final Map<String, List<CompositionModel>> compositionByColorMap = item.getCompositions().stream().collect(
                Collectors.groupingBy(CompositionModel::getColorCode));

        for (final Entry<String, List<CompositionModel>> compositionByColor : compositionByColorMap.entrySet()) {
            if (isBulkRegist && !colorCodeSet.contains(compositionByColor.getKey())) {
                // 一括登録の場合、SKUにカラーが存在しなければ、エラー
                errorMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_IC_01, compositionByColor.getKey()).resource(RESOURCE_COMPOSITION));

                // 次のカラーへ
                continue;
            }

            // カラー別の組成の検証
            validateCompositionsByColor(errorMessages, compositionByColor.getKey(), compositionByColor.getValue(), masterData, compositionsRequiredType,
                    isBulkRegist);
        }

        if (RegistStatusType.PART == registStatus || ItemValidationType.PART == item.getValidationType()) {
            // 品番登録 または 登録モードが品番の場合
            if (MCodmstCompositionsRequiredType.BOTH_REQUIRED == compositionsRequiredType
                    || MCodmstCompositionsRequiredType.REQUIRED == compositionsRequiredType) {
                // 素材が必須の場合
                if (!compositionByColorMap.containsKey(COMMON_COLOR_CODE)) {
                    // 共通が未入力(コードも％も両方入力が1件もない)の場合エラー（共通しか発注生産に連携しないため、共通がない場合エラーとする）
                    errorMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_008).field("commonComposition").resource(RESOURCE_COMPOSITION));
                }
            }
        }

        return errorMessages;
    }

    /**
     * SKUのリストから、カラーコードのセットに変換する.
     *
     * @param skus SKUのリスト
     * @return カラーコードのセット
     */
    private Set<String> toColorCodeSet(final List<SkuModel> skus) {
        final Set<String> colorSet = new HashSet<>(
                1 + CollectionUtils.size(skus));

        if (CollectionUtils.isNotEmpty(skus)) {
            // SKUが入力されている場合、共通(00)のカラーとSKUに入力されたカラーを追加
            colorSet.add(COMMON_COLOR_CODE);
            skus.forEach(v -> colorSet.add(v.getColorCode()));
        }

        return colorSet;
    }

    /**
     * カラー別の組成の検証.
     *
     * <pre>
     * ＜検証内容＞
     * - パーツ存在チェック（アイテム別パーツマスタに存在すること、パーツ名称のみ入力されていないこと）
     * - 素材存在チェック（コードマスタ（TBLID="13"（組成））に存在すること、素材名称のみ入力されていないこと）
     * - 素材、混率必須チェック（素材と混率にどちらか入力がある場合、素材と混率は両方入力されていること）
     * - 先頭パーツ必須チェック（先頭のパーツが空かつ、2件目以降にパーツが入力されている（「その他」も含む）場合、エラー。）
     *   例1）
     *     パーツ 素材         混率
     *            ポリエステル 85  ← エラー（パーツを入力する場合は、先頭から入力すること）
     *     表地   綿           15
     * - その他パーツチェック(「その他」が指定されている場合、「その他」以外のパーツが入力されている場合、エラー)
     *   例1）
     *     パーツ 素材         混率
     *     その他 ポリエステル 85
     *     表地   綿           15  ← エラー（「その他」は空で連携されるため、2件目以降のパーツは空もしくは「その他」であること）
     *   例2）
     *     パーツ 素材         混率
     *     表地   ポリエステル 85
     *     その他 綿           15  ← エラー（「その他」は空で連携されるため、2件目以降のパーツは空もしくは「その他」以外のパーツであること）
     * - 素材と混率の検証
     *   {@link CompositionValidateComponent#validateCompositionAndPercent(List, String, List, MCodmstCompositionsRequiredType)}
     * </pre>
     *
     * @param errorMessages エラーメッセージのリスト
     * @param colorCode 色コード
     * @param compositions 色コード別の組成のリスト
     * @param masterData 品番に関連するマスタデータ
     * @param compositionsRequiredType 混率必須区分
     * @param isBulkRegist 一括登録か
     */
    private void validateCompositionsByColor(
            final List<ResultMessage> errorMessages,
            final String colorCode,
            final List<CompositionModel> compositions,
            final MItemModel masterData,
            final MCodmstCompositionsRequiredType compositionsRequiredType,
            final boolean isBulkRegist) {
        if (CollectionUtils.isEmpty(compositions)) {
            // 組成の入力がない場合、以降チェックしない
            return;
        }

        // 組成整形（部位補完）用のリスト（ディープコピー用）
        final List<CompositionModel> copyCompositions = new ArrayList<>(compositions.size());

        // 「その他」のパーツの入力があるか
        boolean hasOtherParts = false;
        // 「その他」を除くパーツの入力があるか
        boolean hasParts = false;

        for (final CompositionModel composition : compositions) {
            // パーツ存在チェック
            if (StringUtils.isNotEmpty(composition.getPartsCode())) {
                // パーツが入力されている場合
                final MItemPartsEntity mItemPartsEntity = masterData.getItemPartMap().get(composition.getPartsCode());

                if (mItemPartsEntity == null) {
                    // パーツがマスタに存在しない場合、エラーにする
                    errorMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("partsCode")
                            // 一括登録の場合、パーツ名称の入力がないため、エラーメッセージにはpartsCodeを設定する
                            // 一括登録以外の場合、エラーメッセージにはpartsNameを設定する
                            .value(toErrorMessageValue(isBulkRegist, composition.getPartsCode(), composition.getPartsName()))
                            .resource(RESOURCE_COMPOSITION));

                    // 以降チェックしない
                    return;
                } else if (mItemPartsEntity.getOtherPartsFlg().convertToValue()) {
                    // 「その他フラグ」がtrue:パーツその他(1)の場合、「その他」のパーツの入力ありとする
                    hasOtherParts = true;
                } else {
                    // 「その他フラグ」がfalseの場合、「その他」を除くパーツの入力ありとする
                    hasParts = true;
                }
            } else if (isPartsCodeEmptyAndPartsNameNotEmpty(composition)) {
                // パーツが空で、パーツ名称に入力がある場合、エラーにする
                errorMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("partsCode")
                        // 一括登録の場合、パーツ名称の入力がないため、エラーメッセージにはpartsCodeを設定する
                        // 一括登録以外の場合、エラーメッセージにはpartsNameを設定する
                        .value(toErrorMessageValue(isBulkRegist, composition.getPartsCode(), composition.getPartsName()))
                        .resource(RESOURCE_COMPOSITION));

                // 以降チェックしない
                return;
            }

            // 素材存在チェック
            if (notExistsComposition(composition, masterData.getCompositionMap())) {
                // 素材がマスタに存在しない場合、エラーにする
                errorMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("compositionCode")
                        // 一括登録の場合、組成名称の入力がないため、エラーメッセージにはcompositionCodeを設定する
                        // 一括登録以外の場合、エラーメッセージにはcompositionNameを設定する
                        .value(toErrorMessageValue(isBulkRegist, composition.getCompositionCode(), composition.getCompositionName()))
                        .resource(RESOURCE_COMPOSITION));

                // 以降チェックしない
                return;
            }

            // 素材、混率必須チェック
            if (isCompositionXorPercentEmpty(composition)) {
                // 素材と混率にどちらか入力がある場合、素材と混率は両方入力されていないとエラーにする
                errorMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_IC_02, colorCode).resource(RESOURCE_COMPOSITION));

                // 以降チェックしない
                return;
            }

            if (isCompositionNotEmpty(composition)) {
                // 組成の入力がある場合、ディープコピーする
                copyCompositions.add(copyCompositionModel(composition));
            }
        }

        // 先頭パーツ必須チェック
        if (StringUtils.isEmpty(compositions.get(0).getPartsCode()) && (hasOtherParts || hasParts)) {
            // 先頭のパーツが空かつ、2件目以降にパーツが入力されている（「その他」も含む）場合、エラー
            errorMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_IC_03, colorCode).resource(RESOURCE_COMPOSITION));

            // 以降チェックしない
            return;
        }

        // その他パーツチェック
        if (hasOtherParts && hasParts) {
            // 「その他」が指定されている場合、「その他」以外のパーツが入力されている場合、エラー
            errorMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_IC_04, colorCode).resource(RESOURCE_COMPOSITION));

            // 以降チェックしない
            return;
        }

        // 素材と混率のバリデーション
        validateCompositionAndPercent(errorMessages, colorCode, copyCompositions, compositionsRequiredType);
    }

    /**
     * 一括登録の場合に、エラーメッセージに設定する値を返却する.
     *
     * @param isBulkRegist 一括登録か
     * @param bulkRegistValue 一括登録の場合に、エラーメッセージに設定する値
     * @param otherValue 一括登録以外の場合に、エラーメッセージに設定する値
     * @return value or bulkRegistValue
     */
    private String toErrorMessageValue(final boolean isBulkRegist, final String bulkRegistValue, final String otherValue) {
        if (isBulkRegist) {
            return bulkRegistValue;
        }

        return otherValue;
    }

    /**
     * 素材/混率入力チェック.
     *
     * <pre>
     * - 素材と混率にどちらか入力がある場合、素材と混率は両方入力されていないとエラー.
     * </pre>
     *
     * @param composition 組成
     * @return true : エラーあり, false : エラーなし
     */
    private boolean isCompositionXorPercentEmpty(final CompositionModel composition) {
        return (StringUtils.isNotEmpty(composition.getCompositionCode()) && composition.getPercent() == null)
                || (StringUtils.isEmpty(composition.getCompositionCode()) && composition.getPercent() != null);
    }

    /**
     * パーツ/素材/混率入力判定.
     *
     * <pre>
     * - パーツ/素材/混率のいずれかの入力がある場合、入力ありとする.
     * </pre>
     *
     * @param composition 組成
     * @return true : 入力あり, false : 入力なし
     */
    private boolean isCompositionNotEmpty(final CompositionModel composition) {
        return StringUtils.isNotEmpty(composition.getPartsCode())
                || StringUtils.isNotEmpty(composition.getCompositionCode())
                || composition.getPercent() != null;
    }

    /**
     * 素材存在チェック.
     *
     * <pre>
     * - 素材コードが空の場合、素材名称に入力がある場合、エラー（画面上で存在しない素材名称が入力されたケース）。
     * - 素材コードがマスタに存在しない場合、エラー。
     *   ただし、IDが設定されている場合、登録後にマスタが削除されたケースのため素材の存在チェックは行わない。
     * </pre>
     *
     * @param composition 組成
     * @param compositionMap 組成マスタのマップ
     * @return true : エラーあり, false : エラーなし
     */
    private boolean notExistsComposition(final CompositionModel composition, final Map<String, MCodmstEntity> compositionMap) {
        if (StringUtils.isEmpty(composition.getCompositionCode())) {
            // 素材コードが空の場合、素材名称に入力がある場合、エラー
            return StringUtils.isNotEmpty(composition.getCompositionName());
        } else {
            if (composition.getId() != null) {
                // IDが設定されている場合、登録後にマスタが削除されたケースのため組成の存在チェックは行わない
                return false;
            }

            // マスタに存在しない場合、エラー
            return !compositionMap.containsKey(composition.getCompositionCode());
        }
    }

    /**
     * パーツ/パーツ名称入力チェック.
     *
     * <pre>
     * - パーツが空で、パーツ名称に入力がある場合、エラー（画面上で存在しないパーツ名称が入力されたケース）.
     * </pre>
     *
     * @param composition 組成
     * @return true : エラーあり, false : エラーなし
     */
    private boolean isPartsCodeEmptyAndPartsNameNotEmpty(final CompositionModel composition) {
        return StringUtils.isEmpty(composition.getPartsCode())
                && StringUtils.isNotEmpty(composition.getPartsName());
    }

    /**
     * コピーしたオブジェクトを返却する.
     *
     * @param composition コピー元
     * @return {@link CompositionModel} コピー後
     */
    private CompositionModel copyCompositionModel(final CompositionModel composition) {
        final CompositionModel copyComposition = new CompositionModel();
        BeanUtils.copyProperties(composition, copyComposition);

        return copyComposition;
    }

    /**
     * 素材と混率の検証.
     *
     * <pre>
     * ＜検証内容＞
     * - パーツ別混率降順チェック（パーツ内の混率が大きい順であること。パーツの入力がない場合は、上のパーツを引き継ぐ）
     * - パーツ別混率合計チェック（パーツ内の混率の合計が100の倍数であること。パーツの入力がない場合は、上のパーツを引き継ぐ）
     * - 素材、混率必須チェック
     * </pre>
     *
     * @param errorMessages エラーメッセージのリスト
     * @param colorCode 色コード
     * @param compositions 色コード別の組成のリスト（ディープコピーしたリストであること）
     * @param compositionsRequiredType 混率必須区分
     */
    private void validateCompositionAndPercent(
            final List<ResultMessage> errorMessages,
            final String colorCode,
            final List<CompositionModel> compositions,
            final MCodmstCompositionsRequiredType compositionsRequiredType) {
        if (CollectionUtils.isEmpty(compositions)) {
            // 組成の入力がない場合、以降チェックしない
            return;
        }

        // パーセントの合計
        int percentSum = 0;

        // 組成整形（部位補完）し、パーツごとに繰り返しチェックする
        for (final List<CompositionModel> formattedCompositions : itemComponent.formatCompositionsToMap(compositions).values()) {
            // 混率NULLを除去した組成のリストを取得
            final List<CompositionModel> percentNotNullCompositions = filterPercentNotNullCompositions(formattedCompositions);

            if (CollectionUtils.isEmpty(percentNotNullCompositions)) {
                // リストが空の場合、次のパーツへ
                continue;
            }

            // パーツ別混率降順チェック
            if (isPercentNotDesc(percentNotNullCompositions)) {
                errorMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_IC_05, colorCode).resource(RESOURCE_COMPOSITION));

                // 以降チェックしない
                return;
            }

            // 部位別の混率を合計する
            final int percentPartsSum = totalPercent(percentNotNullCompositions);

            // パーツ別混率合計チェック
            if (isPercentSumNot100Multiple(percentPartsSum)) {
                errorMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_IC_06, colorCode).resource(RESOURCE_COMPOSITION));

                // 以降チェックしない
                return;
            }

            // 混率を加算する
            percentSum += percentPartsSum;
        }

        // 素材、混率必須チェック
        if (MCodmstCompositionsRequiredType.BOTH_REQUIRED == compositionsRequiredType) {
            if (isCompositionEmpty(compositions) || percentSum == PERCENT_SUM_ZERO) {
                // 素材、混率の入力がない場合、エラー
                errorMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_IC_07, colorCode).resource(RESOURCE_COMPOSITION));

                // 以降チェックしない
                return;
            }
        } else if (MCodmstCompositionsRequiredType.REQUIRED == compositionsRequiredType) {
            if (isCompositionEmpty(compositions)) {
                // 素材の入力がない場合、エラー
                errorMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_IC_08, colorCode).resource(RESOURCE_COMPOSITION));

                // 以降チェックしない
                return;
            }
        }
    }

    /**
     * 混率NULLを除去した組成のリストを取得.
     *
     * @param compositions パーツでグルーピングされた組成
     * @return 率NULLを除去した組成のリスト
     */
    private List<CompositionModel> filterPercentNotNullCompositions(final List<CompositionModel> compositions) {
        return compositions.stream()
                // 混率nullは除去
                .filter(v -> v.getPercent() != null)
                .collect(Collectors.toList());
    }

    /**
     * パーツ別混率降順チェック.
     *
     * @param compositions パーツでグルーピングされた組成（混率NULL以外）
     * @return true : エラーあり, false : エラーなし
     */
    private boolean isPercentNotDesc(final List<CompositionModel> compositions) {
        // 先頭の混率を取得
        int prePercent = compositions.get(0).getPercent().intValue();

        for (final CompositionModel composition : compositions) {
            int percent = composition.getPercent().intValue();

            if (prePercent < percent) {
                // ひとつ前の混率 ＜ 現在の混率の場合、エラーとする
                return true;
            }

            prePercent = percent;
        }

        return false;
    }

    /**
     * 混率を合計する.
     *
     * @param compositions パーツでグルーピングされた組成（混率NULL以外）
     * @return 混率を合計
     */
    private int totalPercent(final List<CompositionModel> compositions) {
        return compositions.stream()
                .mapToInt(composition -> composition.getPercent().intValue())
                // 合計
                .sum();
    }

    /**
     * パーツごとのパーセント合計チェック(混率0不可).
     *
     * <pre>
     * - パーツごとのパーセント合計が100の倍数でない場合、エラー。
     * </pre>
     *
     * @param percentSum 混率の合計
     * @return true : エラーあり, false : エラーなし
     */
    private boolean isPercentSumNot100Multiple(final int percentSum) {
        return (percentSum % PERCENT_SUM) != 0;
    }

    /**
     * 素材入力チェック.
     *
     * <pre>
     * - 素材の入力がない場合、エラー。
     * </pre>
     *
     * @param compositions 組成のリスト
     * @return true : エラーあり, false : エラーなし
     */
    private boolean isCompositionEmpty(final List<CompositionModel> compositions) {
        return !compositions.stream().anyMatch(v -> StringUtils.isNotEmpty(v.getCompositionCode()));
    }
}
