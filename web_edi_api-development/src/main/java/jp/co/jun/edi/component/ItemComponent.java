package jp.co.jun.edi.component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.ItemChangeStateModel;
import jp.co.jun.edi.constants.SizeConstants;
import jp.co.jun.edi.entity.TCompositionEntity;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.CompositionModel;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.OrderSupplierModel;
import jp.co.jun.edi.model.SkuModel;
import jp.co.jun.edi.repository.extended.ExtendedTCompositionRepository;
import jp.co.jun.edi.repository.extended.ExtendedTItemRepository;
import jp.co.jun.edi.type.ChangeRegistStatusType;
import jp.co.jun.edi.type.ExternalLinkingType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.RegistStatusType;
import jp.co.jun.edi.util.NumberUtils;

/**
 * 品番関連のコンポーネント.
 */
@Component
public class ItemComponent extends GenericComponent {

    @Autowired
    private ExtendedTCompositionRepository extendedTCompositionRepository;

    @Autowired
    private ExtendedTItemRepository extendedTItemRepository;

    // 共通のカラーコード：00
    private static final String COMMON_COLOR_CODE = "00";

    /**
     * 入力された組成(混率)リストの必須チェック.
     * 組成名、またはパーセントにnullが1つでもあればエラー.
     *
     * @param compositions 組成(混率)リスト
     * @return true : 必須エラーあり, false : 必須エラーなし
     */
    public boolean isCompositionsRequiredError(final List<CompositionModel> compositions) {
        return compositions.isEmpty()
                || compositions.stream().anyMatch(composition -> StringUtils.isEmpty(composition.getCompositionName())
                        || composition.getPercent() == null);
    }

    /**
     * 品番情報の変更状態を取得する.
     *
     * @param reqItem リクエストの品番情報
     * @param dbRegisterdItem DBの品番情報
     * @return 品番情報の変更状態
     */
    public ItemChangeStateModel getItemChangeState(final ItemModel reqItem, final ItemModel dbRegisterdItem) {

        // 品番情報の変更状態初期化（初期値：変更なし）
        final ItemChangeStateModel itemChangeState = new ItemChangeStateModel();

        // 品番の変更判定
        itemChangeState.setPartNoChanged(!Objects.equals(reqItem.getPartNo(), dbRegisterdItem.getPartNo()));

        // 原産国の変更判定
        itemChangeState.setCooCodeChanged(!Objects.equals(reqItem.getCooCode(), dbRegisterdItem.getCooCode()));

        // 生産メーカーの変更判定
        itemChangeState.setMdfMakerCodeChanged(isMdfMakerCodeChanged(reqItem, dbRegisterdItem));

        // 登録済みの組成の変更判定
        itemChangeState.setRegistedCompositionChanged(
                isRegistedCompositionChanged(reqItem.getCompositions(), dbRegisterdItem.getCompositions()));

        // 変更があった組成のカラーコードリスト取得
        itemChangeState.setChangedCompositionsColors(
                extarctChangedCompositionColorCodeList(reqItem, dbRegisterdItem.getCompositions(), dbRegisterdItem.getSkus()));

        // SKUの追加があるか
        itemChangeState.setAddSkuColor(isAddSkuColor(reqItem.getSkus(), dbRegisterdItem.getSkus()));

        // 発注書印字対象の組成の変更判定
        itemChangeState.setPrintCompositionChanged(isPrintCompositionChanged(reqItem));

        return itemChangeState;
    }

    /**
     * 生産メーカーの変更状態を取得する.
     * 商品の状態で、生産メーカーが変更された場合を考慮.
     *
     * @param reqItem リクエストの品番情報
     * @param dbRegisterdItem DBの品番情報
     * @return 生産メーカーの変更状態
     */
    private boolean isMdfMakerCodeChanged(final ItemModel reqItem, final ItemModel dbRegisterdItem) {
        if (CollectionUtils.size(reqItem.getOrderSuppliers()) != 1
                || CollectionUtils.size(dbRegisterdItem.getOrderSuppliers()) != 1) {
            // 生産メーカーの件数が1件以外の場合、変更なしとする
            return false;
        }

        return !StringUtils.equals(
                reqItem.getOrderSuppliers().get(0).getSupplierCode(),
                dbRegisterdItem.getOrderSuppliers().get(0).getSupplierCode());
    }

    /**
     * 品番情報を取得する.
     *
     * @param id 品番ID
     * @return 品番情報
     */
    public ExtendedTItemEntity getExtendedTItem(final BigInteger id) {
        return extendedTItemRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));
    }

    /**
     * 登録済みの組成(混率)の内容に変更があるかチェック.
     * 登録済みの組成のパーツ、組成名、パーセントいずれかが変更されている場合は変更あり
     * 登録済みの組成に00のものがない かつ 00の組成が追加された場合は変更あり
     *
     * @param compositionModelList 入力された組成(混率)リスト
     * @param dbRegisteredCompositions t_compositionに登録されているデータ
     * @return true : 変更あり, false : 変更なし
     */
    private boolean isRegistedCompositionChanged(final List<CompositionModel> compositionModelList,
            final List<CompositionModel> dbRegisteredCompositions) {
        // (1)色コードで変更ありか判断：

        // DBに登録済みの組成情報の色コードリストを取得
        final Set<String> currentCompositionColorCodeSet = dbRegisteredCompositions.stream()
                .map(currentComposition -> currentComposition.getColorCode())
                .collect(Collectors.toCollection(HashSet::new));

        // 入力された組成(混率)リストより色コードリストを取得
        final Set<String> compositionColorCodeSet = compositionModelList.stream()
                .map(idNotExistComposition -> idNotExistComposition.getColorCode())
                .collect(Collectors.toCollection(HashSet::new));

        if (!compositionColorCodeSet.isEmpty()) {
            // DBに登録されている組成に00のものがない かつ
            // 入力された組成(混率)の色コードリストに00の組成がある場合は、変更ありとしてtrueを返却
            if (currentCompositionColorCodeSet.stream()
                    .noneMatch(colorCode -> Objects.equals(colorCode, COMMON_COLOR_CODE))
                    && compositionColorCodeSet.stream()
                            .anyMatch(colorCode -> Objects.equals(colorCode, COMMON_COLOR_CODE))) {
                return true;
            }
        }

        // (2)組成内容で変更ありか判断：

        // 入力された組成のソート用Comparator作成(nullの値は最後に持っていく)
        final Comparator<CompositionModel> modelComparator = Comparator.comparing(CompositionModel::getColorCode)
                .thenComparing(CompositionModel::getPartsCode, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(CompositionModel::getCompositionCode, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(CompositionModel::getPercent, Comparator.nullsLast(Comparator.naturalOrder()));

        // DBに登録済みの組成情報の色コードを含む入力された組成(混率)リストを取得する(ソートかける)
        final List<CompositionModel> colorRegistedCompositionList = compositionModelList.stream()
                .map(compositionModel -> {
                    // パーツが空白("")の場合は、nullに置き換える
                    if (StringUtils.isEmpty(compositionModel.getPartsCode())) {
                        compositionModel.setPartsCode(null);
                    }
                    return compositionModel;
                })
                .filter(composition -> currentCompositionColorCodeSet.contains(composition.getColorCode()))
                .sorted(modelComparator)
                .collect(Collectors.toList());

        // DBに登録済みの組成(混率)リストとcolorRegistedCompositionListの数が異なる場合は、変更ありとしてtrueを返却
        if (dbRegisteredCompositions.size() != colorRegistedCompositionList.size()) {
            return true;
        }

        // DBに登録済みの組成(混率)リストとcolorRegistedCompositionList両方空の場合は、変更なしとしてfalseを返却
        if (dbRegisteredCompositions.isEmpty() && colorRegistedCompositionList.isEmpty()) {
            return false;
        }

        // DBに登録されている組成情報も入力値と同じ条件でソートする
        final List<CompositionModel> sortedDbRegisteredCompositions = dbRegisteredCompositions.stream()
                .sorted(modelComparator)
                .collect(Collectors.toList());

        // DBに登録済みのソート後組成(混率)リストとcolorRegistedCompositionListの内容を比較
        for (int index = 0; index < sortedDbRegisteredCompositions.size(); index++) {
            // 内容が異なる場合は、変更ありとしてtrueを返却
            if (!compareComposition(colorRegistedCompositionList.get(index), sortedDbRegisteredCompositions.get(index))) {
                return true;
            }
        }

        // 上記以外は変更なしとしてfalseを返却
        return false;
    }

    /**
     * @param reqSkus リクエストのSKUリスト
     * @param dbSkus DBのSKUリスト
     * @return true:SKUのカラーが追加された
     */
    private boolean isAddSkuColor(final List<SkuModel> reqSkus, final List<SkuModel> dbSkus) {

        // リクエストのSKUの中に、t_skuにないもの(今回追加されたもの)が存在するか
        return reqSkus.stream()
                .map(rs -> rs.getColorCode())
                .filter(c -> dbSkus.stream()
                        .noneMatch(s -> s.getColorCode().equals(c)))
                .findFirst()
                .isPresent();
    }

    /**
     * 入力に変更がある組成のカラーコードリストを取得する.
     * ※共通は除く.共通が変更された場合、共通を使っている色コードを変更扱いとする.
     *
     * @param reqItem リクエストパラメータ
     * @param dbRegisteredCompositions t_compositionに登録されているデータ
     * @param dbSkus t_skuに登録されているデータ
     * @return 変更があった組成のカラーコードリスト
     */
    private List<String> extarctChangedCompositionColorCodeList(
            final ItemModel reqItem,
            final List<CompositionModel> dbRegisteredCompositions,
            final List<SkuModel> dbSkus) {

        // 共通が変更の場合、
        // reqSKUにあるがreqCompositionにないものを変更扱いとする

        // カラーコードでグルーピング
        final Map<String, List<CompositionModel>> reqGrpByColorCode = reqItem.getCompositions().stream()
                .collect(Collectors.groupingBy(CompositionModel::getColorCode));
        final Map<String, List<CompositionModel>> dbGrpByColorCode = dbRegisteredCompositions.stream()
                .collect(Collectors.groupingBy(CompositionModel::getColorCode));

        // t_compositionから変更がある組成のカラーコードをリストに追加
        final List<String> changedCompositionColorCodeList = dbGrpByColorCode.keySet().stream()
                .filter(colorCode -> isChangeCompositionColor(reqGrpByColorCode.get(colorCode),
                        dbGrpByColorCode.get(colorCode)))
                .map(colorCode -> colorCode)
                .collect(Collectors.toList());

        // t_compositionが未登録の場合、リクエストにある組成のカラーコードをリストに追加
        // (組成のカラーを今回チェックつけたケース)
        reqGrpByColorCode.keySet().stream()
        .filter(colorCode -> !dbGrpByColorCode.containsKey(colorCode))
        .forEach(c -> changedCompositionColorCodeList.add(c));

        addColorAsChangeAtCommonChange(changedCompositionColorCodeList, reqItem.getCompositions(), reqItem.getSkus());

        // 共通を変更カラーコードから除外
        changedCompositionColorCodeList.remove(COMMON_COLOR_CODE);
        return changedCompositionColorCodeList;
    }

    /**
     * 共通が変更の場合、リクエストのskuにあり、リクエストの組成にないカラーコードを
     * 変更があった組成のカラーコードリストに追加する.
     * →組成にないカラー＝共通の組成を使っているカラー
     * (t_misleading_representationの承認済の取消対象となるカラーコード)
     *
     * @param changedCompositionColorCodeList 変更があった組成のカラーコードリスト
     * @param reqCompositions 組成リストリクエストパラメータ
     * @param reqSkus SKUリストリクエストパラメータ
     */
    private void addColorAsChangeAtCommonChange(final List<String> changedCompositionColorCodeList,
            final List<CompositionModel> reqCompositions, final List<SkuModel> reqSkus) {
        if (!changedCompositionColorCodeList.contains(COMMON_COLOR_CODE)) {
            return; // 共通に変更なし
        }

        final List<String> compositionColorCodeList = reqCompositions.stream().map(comp -> comp.getColorCode())
                .collect(Collectors.toList());
        reqSkus.stream()
                .map(sku -> sku.getColorCode())
                .filter(skuColorCode -> !compositionColorCodeList.contains(skuColorCode))
                .forEach(changeColorCode -> changedCompositionColorCodeList.add(changeColorCode));
    }

    /**
     * 登録済の組成が変更されたカラーであるか判定する.
     *
     * @param reqCompositions リクエストパラメータ(同一のカラーコードのみ)
     * @param dbCompositions t_compositionに登録されているデータ(同一のカラーコードのみ)
     * @return trure:変更あり
     */
    private boolean isChangeCompositionColor(final List<CompositionModel> reqCompositions,
            final List<CompositionModel> dbCompositions) {
        // 組成の件数比較。不一致の場合変更あり(件数見ないと同じ組成が増えたときに変更判定できない)
        if (isNotCompositionSizeEquals(reqCompositions, dbCompositions)) {
            return true;
        }

        // 件数が同じ場合は全て精査
        // DB登録済の組成がリクエストパラメータの組成の中に一致するものがなければ変更あり
        return dbCompositions.stream()
                .anyMatch(dbComp -> isNotSameCompositionContains(reqCompositions, dbComp));
    }

    /**
     * 組成の件数が等しくないか判定する.
     *
     * @param reqCompositions リクエストパラメータ
     * @param dbCompositions t_compositionに登録されているデータ
     * @return true;等しくない
     */
    private boolean isNotCompositionSizeEquals(final List<CompositionModel> reqCompositions,
            final List<CompositionModel> dbCompositions) {
        return CollectionUtils.isEmpty(reqCompositions) || dbCompositions.size() != reqCompositions.size();
    }

    /**
     * リクエスパラメータに完全一致する組成がないか判定する.
     * 完全一致：パーツコード、組成コード、パーセント全てが完全一致.
     *
     * @param reqCompositions リクエスパラメータの組成リスト
     * @param dbComposition 処理中の組成
     * @return true:リクエスパラメータに完全一致する組成がない
     */
    private boolean isNotSameCompositionContains(final List<CompositionModel> reqCompositions,
            final CompositionModel dbComposition) {
        return reqCompositions.stream().noneMatch(reqComp -> compareComposition(reqComp, dbComposition));
    }

    /**
     * 発注書に印字する組成（素材、混率）の変更有無を判定する.
     * <p>
     * TODO JUNoTで発注書のPDFを作成する場合は、全ての色の組成（部位、素材、混率）を印字するため、本判定条件も変更が必要となる。
     * </p>
     * <p>
     * 以下の条件に該当する場合は、変更ありとする。
     * </p>
     *
     * <pre>
     * - カラーコードが最小値の色の組成（素材、混率）が追加、変更、削除された。（発注生産に連携している色の組成が変更された）
     * </pre>
     *
     * @param itemModel 更新対象の品番情報
     * @return 判定結果
     *
     *         <pre>
     *  - true : 変更あり
     *  - false : 変更なし
     *         </pre>
     */
    private boolean isPrintCompositionChanged(final ItemModel itemModel) {
        // 入力された組成(混率)リスト
        final List<CompositionModel> compositionModelList = itemModel.getCompositions();

        // 品番IDを基に最新の組成情報をDBから取得
        final List<CompositionModel> currentCompositionModelList = extendedTCompositionRepository
                .findByPartNoId(itemModel.getId(),
                        PageRequest.of(0, Integer.MAX_VALUE,
                                Sort.by(Order.asc("color_code"), Order.asc("serial_number"))))
                .stream()
                .map(tComposition -> {
                    final CompositionModel compositionModel = new CompositionModel();
                    // 組成情報のコピー
                    BeanUtils.copyProperties(tComposition, compositionModel);
                    return compositionModel;
                }).collect(Collectors.toList());

        // 入力された組成(混率)リストの一番小さい色コード
        final String smallestColorCode = createSmallestColorCode(compositionModelList);

        // DBから取得した最新の組成(混率)リストの一番小さい色コード
        final String currentSmallestColorCode = createSmallestColorCode(currentCompositionModelList);

        // 入力とDBの一番小さい色コードが異なる場合は、変更ありとしてtrueを返却：
        if (!Objects.equals(smallestColorCode, currentSmallestColorCode)) {
            return true;
        }

        // 入力とDBの一番小さい色コードが同じ場合：
        // ※以下組成(混率)リストは全て色コードが一番小さいもの

        // 入力された組成(混率)リストを取得
        final List<CompositionModel> smallestColorCodeCompositionList = createSmallestColorCodeCompositionList(
                compositionModelList, smallestColorCode);

        // DBから取得した最新の組成(混率)リストを取得
        final List<CompositionModel> currentSmallestColorCodeCompositionList = createSmallestColorCodeCompositionList(
                currentCompositionModelList, currentSmallestColorCode);

        // 印刷対象の組成（素材、混率）を比較
        final int size1 = smallestColorCodeCompositionList.size();
        final int size2 = currentSmallestColorCodeCompositionList.size();

        for (int index = 0; index < getMaxSize(size1, size2); index++) {
            if (!comparePrintComposition(
                    getComposition(smallestColorCodeCompositionList, size1, index),
                    getComposition(currentSmallestColorCodeCompositionList, size2, index))) {
                return true;
            }
        }

        return false;
    }

    /**
     * 一番小さい色コードを作成.
     * 取得できない場合はnullを返却
     *
     * @param compositionModelList 組成(混率)リスト
     * @return 色コード
     */
    private String createSmallestColorCode(final List<CompositionModel> compositionModelList) {
        return compositionModelList.stream().map(compositionModel -> compositionModel.getColorCode())
                .sorted().findFirst().orElse(null);
    }

    /**
     * 色コードが一番小さい組成(混率)リストを作成.
     *
     * @param compositionModelList 組成(混率)リスト
     * @param smallestColorCode 一番小さい色コード
     * @return 色コードが一番小さい組成(混率)リスト
     */
    private List<CompositionModel> createSmallestColorCodeCompositionList(
            final List<CompositionModel> compositionModelList, final String smallestColorCode) {
        return compositionModelList.stream()
                .filter(smallestColorCodeComposition -> Objects.equals(smallestColorCodeComposition.getColorCode(),
                        smallestColorCode))
                .collect(Collectors.toList());
    }

    /**
     * 組成(混率)の内容の比較をする.
     *
     * @param composition 入力された組成(混率)
     * @param currentComposition DBに登録されている組成(混率)
     * @return 内容が同じ：true、内容が異なる：false
     */
    private boolean compareComposition(final CompositionModel composition, final CompositionModel currentComposition) {

        // Nullと空文字は同一とみなす
        return StringUtils.equals(StringUtils.defaultString(composition.getColorCode()), StringUtils.defaultString(currentComposition.getColorCode()))
                && StringUtils.equals(StringUtils.defaultString(composition.getPartsCode()), StringUtils.defaultString(currentComposition.getPartsCode()))
                && StringUtils.equals(StringUtils.defaultString(composition.getCompositionCode()),
                                       StringUtils.defaultString(currentComposition.getCompositionCode()))
                && StringUtils.equals(jp.co.jun.edi.util.StringUtils.defaultString(composition.getPercent()),
                                      jp.co.jun.edi.util.StringUtils.defaultString(currentComposition.getPercent()));

    }

    /**
     * 入力された組成リストのサイズと、DBに登録された組成リストのサイズを比較し、大きい方の値を取得する.
     *
     * @param size1 入力された組成リストのサイズ
     * @param size2 DBに登録された組成リストのサイズ
     * @return サイズ
     */
    private int getMaxSize(final int size1, final int size2) {
        if (size1 > size2) {
            return size1;
        }

        return size2;
    }

    /**
     * 組成を取得する.
     * <p>
     * インデックス >= サイズの場合は、組成はnullを返却する。
     * </p>
     *
     * @param compositions 組成のリスト
     * @param size 組成のリストのサイズ
     * @param index 組成のリストのインデックス
     * @return 組成
     */
    private CompositionModel getComposition(final List<CompositionModel> compositions, final int size,
            final int index) {
        if (index >= size) {
            return null;
        }

        return compositions.get(index);
    }

    /**
     * 印刷対象の組成（素材、混率）を比較する.
     * <p>
     * TODO JUNoTで発注書のPDFを作成する場合は、全ての色の組成（部位、素材、混率）を印字するため、本判定条件も変更が必要となる。
     * </p>
     *
     * @param composition1 入力された組成
     * @param composition2 DBに登録されている組成
     * @return 判定結果
     *
     *         <pre>
     *  - true : 一致
     *  - false : 不一致
     *         </pre>
     */
    private boolean comparePrintComposition(final CompositionModel composition1, final CompositionModel composition2) {
        if (Objects.isNull(composition1) && Objects.isNull(composition2)) {
            // 両方nullの場合は、一致
            return true;
        } else if (Objects.isNull(composition1) || Objects.isNull(composition2)) {
            // 片方がnullの場合は、不一致
            return false;
        }

        return Objects.equals(composition1.getCompositionCode(), composition2.getCompositionCode())
                && Objects.equals(composition1.getPercent(), composition2.getPercent());
    }

    /**
     * @param orderSuppliers 仕入先メーカーリスト
     * @return true : 登録可能件数超え
     */
    public boolean isSuppiperLengthOver(final List<OrderSupplierModel> orderSuppliers) {
        return SizeConstants.SUPPRIER_REGIST < orderSuppliers.size();
    }

    /**
     * 優良誤認承認テーブルをupsertするか判定.
     * 品番として登録、または品番更新する場合にupsertする.
     *
     * @param item リクエストパラメータ
     * @return true:upsertする
     */
    public boolean isUpsertIntoMisleadingRepresentation(final ItemModel item) {
        return isRegistPartNo(item.getChangeRegistStatusType())
                || item.getRegistStatus() == RegistStatusType.PART.getValue();
    }

    /**
     * 品番として登録するかどうかのチェック.
     *
     * @param changeRegistStatusType 登録ステータス変更区分
     * @return ture 品番として登録する false:品番として登録する以外
     */
    public boolean isRegistPartNo(final Integer changeRegistStatusType) {

        if (null != changeRegistStatusType
                && ChangeRegistStatusType.PART.getValue() == changeRegistStatusType) {
            return true;
        }

        return false;
    }

    /**
     * 読み取り専用の品番の場合、更新不可エラーとする.
     *
     * @param externalLinkingType DBのExternalLinkingType
     */
    public void validateReadOnly(final String externalLinkingType) {
        // readOnlyの場合、エラー
        if (isReadOnly(externalLinkingType)) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_012));
        }
    }

    /**
     * 外部連携区分が「1:JUNoT登録」以外の場合、読み取り専用の品番とする.
     *
     * @param externalLinkingType DBのExternalLinkingType
     * @return true: 読み取り専用 / false: 更新可能
     */
    public boolean isReadOnly(final String externalLinkingType) {
        return !ExternalLinkingType.JUNOT.getValue().equals(externalLinkingType);
    }

    /**
     * 組成(混率)の整形.
     * ※戻り値はMap型
     *
     * ・省略しているパーツ(部位)の補完
     *
     * @param compositions 組成(混率)リスト
     * @return 整形したMap
     */
    public Map<String, List<CompositionModel>> formatCompositionsToMap(final List<CompositionModel> compositions) {
        // 組成・パーセントがあるがパーツがない場合、省略しているパーツ(部位)を補完
        // ※accumlatorで元のパーツIDが書き換えられないよう、初期パーツIDを別インスタンスで保持
        final CompositionModel initialComposition = new CompositionModel();
        initialComposition.setPartsCode(StringUtils.defaultString(compositions.get(0).getPartsCode()));

        compositions.stream().reduce(initialComposition, (accum, composition) -> {
            // 直接記入も考慮し、Codeだけでなく入力内容の空白もチェックする(IDに空白をセット)
            if (StringUtils.isEmpty(composition.getPartsCode())) {
                // パーツがない場合、上位のパーツIDをセット
                composition.setPartsCode(accum.getPartsCode());
            } else {
                // パーツIDがある場合はaccumのパーツIDを書き換える
                accum.setPartsCode(composition.getPartsCode());
            }
            return accum;
        });

        // 同パーツでグルーピング
        final Map<String, List<CompositionModel>> groupByPartsCodeMap = compositions.stream()
                .collect(Collectors.groupingBy(
                        CompositionModel::getPartsCode,
                        // 入力されたパーツ順
                        LinkedHashMap::new,
                        Collectors.toList()));

        return groupByPartsCodeMap;
    }

    /**
     * カラー別の組成(混率)のマップに変換.
     * ※戻り値はMap型
     *
     * @param compositions 組成(混率)リスト
     * @return 整形したMap
     */
    public Map<String, List<CompositionModel>> formatCompositionByColorMap(
            final List<CompositionModel> compositions) {
        return compositions.stream().collect(
                Collectors.groupingBy(
                        CompositionModel::getColorCode,
                        // 入力されたカラー順
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    /**
     * 組成(混率)の変換.
     * ※戻り値はList型
     *
     * <pre>
     * ・品番ID、品番の設定
     * ・パーツ/組成コード/率が未入力の場合は除外する
     * </pre>
     *
     * @param compositions 組成(混率)リスト
     * @param itemId 品番ID
     * @param partNo 品番
     * @return 整形したList
     */
    public List<TCompositionEntity> compositionsModelToEntity(
            final List<CompositionModel> compositions,
            final BigInteger itemId,
            final String partNo) {
        if (CollectionUtils.isEmpty(compositions)) {
            // 混率(組成)が空の場合は空のリストを返す
            return Collections.emptyList();
        }

        final List<TCompositionEntity> entities = new ArrayList<>(compositions.size());

        // カラー別にグルーピング
        for (final List<CompositionModel> compositionByColorList : formatCompositionByColorMap(compositions).values()) {
            // カラー別に連番を初期化
            int serialNumber = 1;

            for (final CompositionModel composition : compositionByColorList) {
                // パーツ/組成コード/率が未入力の場合は登録しない
                if (StringUtils.isEmpty(composition.getPartsCode())
                        && StringUtils.isEmpty(composition.getCompositionCode())
                        && composition.getPercent() == null) {
                    continue;
                }

                final TCompositionEntity entity = new TCompositionEntity();

                // データをコピー
                BeanUtils.copyProperties(composition, entity);

                // 品番IDをセット
                entity.setPartNoId(itemId);
                // 品番をセット
                entity.setPartNo((partNo));
                // 連番をセット
                entity.setSerialNumber(serialNumber);

                entities.add(entity);

                serialNumber++;
            }
        }

        return entities;
    }

    /**
     * 対象項目を右トリムして、トリム後の値が空文字の場合は、nullを返却する.
     *
     * <pre>
     * - 組成のパーツ、パーツ名称、組成、組成名称
     * </pre>
     *
     * @param item 品番情報
     */
    public void rtrimToNull(
            final ItemModel item) {
        if (CollectionUtils.isNotEmpty(item.getCompositions())) {
            item.getCompositions().forEach(model -> {
                model.setPartsCode(jp.co.jun.edi.util.StringUtils.rtrimToNull(model.getPartsCode()));
                model.setPartsName(jp.co.jun.edi.util.StringUtils.rtrimToNull(model.getPartsName()));
                model.setCompositionCode(jp.co.jun.edi.util.StringUtils.rtrimToNull(model.getCompositionCode()));
                model.setCompositionName(jp.co.jun.edi.util.StringUtils.rtrimToNull(model.getCompositionName()));
            });
        }
    }


    /**
     * 品番の各単価の合計を返却する.
     * <pre>
     *  - 合算対象
     *     生地原価
     *　   加工賃
     *　   附属品
     *　   その他原価
     * </pre>
     * @param entity 品番情報
     * @return 単価合計
     *
     */
    public BigDecimal sumCosts(final TItemEntity entity) {

        if (Objects.isNull(entity)) {
            return BigDecimal.ZERO;
        }

        final BigDecimal matlCost = NumberUtils.defaultInt(entity.getMatlCost());
        final BigDecimal processingCost = NumberUtils.defaultInt(entity.getProcessingCost());
        final BigDecimal accessoriesCost = NumberUtils.defaultInt(entity.getAccessoriesCost());
        final BigDecimal otherCost = NumberUtils.defaultInt(entity.getOtherCost());

        return matlCost.add(processingCost).add(accessoriesCost).add(otherCost);

    }
}


