package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.FukukitaruLinkingCreateCsvFileComponent;
import jp.co.jun.edi.component.FukukitaruLinkingMailCsvFileComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.model.FukukitaruOrderInfoModel;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.TSkuEntity;
import jp.co.jun.edi.entity.extended.ExtendedTCompositionLinkingEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFAttentionAppendicesTermEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFItemLinkingEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFOrderLinkingEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFOrderSkuEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFWashAppendicesTermEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFWashPatternEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.exception.ScheduleException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.CompositionModel;
import jp.co.jun.edi.repository.MUserRepository;
import jp.co.jun.edi.repository.TFOrderRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TSkuRepository;
import jp.co.jun.edi.repository.extended.ExtendedTCompositionLinkingRepository;
import jp.co.jun.edi.repository.extended.ExtendedTFAttentionAppendicesTermRepository;
import jp.co.jun.edi.repository.extended.ExtendedTFItemLinkingRepository;
import jp.co.jun.edi.repository.extended.ExtendedTFOrderLinkingRepository;
import jp.co.jun.edi.repository.extended.ExtendedTFOrderSkuRepository;
import jp.co.jun.edi.repository.extended.ExtendedTFWashAppendicesTermRepository;
import jp.co.jun.edi.repository.extended.ExtendedTFWashPatternRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderLinkingRepository;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.FukukitaruMasterLinkingStatusType;
import jp.co.jun.edi.type.FukukitaruMasterMaterialType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.QualityApprovalType;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * フクキタル連携発注情報を取得するコンポーネント.
 */
@Component
@Slf4j
public class MaterialOrderLinkingComponent {
    /** 共通のカラーコード. */
    private static final String COMMON_COLOR_CODE = "00";

    /** その他の組成コード. */
    private static final String OTHER_COMPOSITION_CODE = "ZZ";

    @Autowired
    private MUserRepository mUserRepository;

    @Autowired
    private ExtendedTFOrderLinkingRepository extendedTFOrderRepository;

    @Autowired
    private ExtendedTFItemLinkingRepository extendedTFItemLinkingRepository;

    @Autowired
    private TSkuRepository tSkuRepository;

    @Autowired
    private ExtendedTCompositionLinkingRepository extendedTCompositionLinkingRepository;

    @Autowired
    private TItemRepository tItemRepository;

    @Autowired
    private ExtendedTOrderLinkingRepository extendedTOrderLinkingRepository;

    @Autowired
    private TFOrderRepository tFOrderRepository;

    @Autowired
    private ExtendedTFWashPatternRepository tFWashPatternRepository;

    @Autowired
    private ExtendedTFAttentionAppendicesTermRepository tFAttentionAppendicesTermRepository;

    @Autowired
    private ExtendedTFWashAppendicesTermRepository tFWashAppendicesTermRepository;

    @Autowired
    private ExtendedTFOrderSkuRepository extendedTFOrderSkuRepository;

    @Autowired
    private PropertyComponent propertyComponent;
    @Autowired
    private FukukitaruLinkingCreateCsvFileComponent linkingCreateCsvFileComponent;

    @Autowired
    private FukukitaruLinkingMailCsvFileComponent linkingMailCsvFileComponent;

    /**
     * 資材発注連携の実行.
     * @param userId 更新用管理ユーザID
     * @param linkingOrderInfoEntity 資材発注情報
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void execute(final BigInteger userId, final ExtendedTFOrderLinkingEntity linkingOrderInfoEntity) {
        final BigInteger fOrderId = linkingOrderInfoEntity.getId();

        try {
            // フクキタル発注モデル情報を取得
            final FukukitaruOrderInfoModel model = getFukukitaruOrderInfo(userId, linkingOrderInfoEntity);

            // CSVファイル生成
            final List<File> attachementFile = linkingCreateCsvFileComponent.createCsvFile(userId, model);

            // メール送信
            linkingMailCsvFileComponent.mailCsvFile(linkingOrderInfoEntity, attachementFile);

            // 発注ファイルと発注一時ディレクトリを削除
            linkingCreateCsvFileComponent.deleteFilesAndDirectory(linkingOrderInfoEntity.getOrderCode(), attachementFile);

            // ステータスを「連携済（2）」に更新
            updateLinkingStatusOrderSendAt(FukukitaruMasterLinkingStatusType.LINK_COMPLETE, fOrderId, userId);

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            // ステータスを「連携対象外エラー(9)」に更新
            updateLinkingStatus(FukukitaruMasterLinkingStatusType.NON_TARGET, fOrderId, userId);
        }
    }

    /**
     * フクキタル発注情報モデルを取得する.
     *
     * @param userId
     *            ユーザID
     * @param linkingOrderInfoEntity
     *            フクキタル発注情報
     * @return フクキタル発注情報モデル
     * @throws Exception
     *             例外
     */
    private FukukitaruOrderInfoModel getFukukitaruOrderInfo(final BigInteger userId, final ExtendedTFOrderLinkingEntity linkingOrderInfoEntity)
            throws Exception {
        final BigInteger fOrderId = linkingOrderInfoEntity.getId();

        final FukukitaruOrderInfoModel model = new FukukitaruOrderInfoModel();
        // フクキタル発注情報
        model.setLinkingOrderInfoEntity(linkingOrderInfoEntity);

        // フクキタル発注SKU情報の色、サイズ取得
        model.setListExtendedTFOrderSkuEntity(getTFOrderSkuList(fOrderId));

        // フクキタル品番情報取得
        model.setExtendedTFItemLinkingEntity(
                getItemInfo(linkingOrderInfoEntity.getFItemId(), linkingOrderInfoEntity.getOrderId()));

        // フクキタル洗濯マーク情報取得
        model.setListTFWashPatternEntity(getTFWashPatternEntity(linkingOrderInfoEntity.getFItemId()));

        // フクキタル洗濯ネーム付記用語情報取得
        model.setListTFWashAppendicesTermEntity(getTFWashAppendicesTermEntity(linkingOrderInfoEntity.getFItemId()));

        // フクキタルアテンションタグ付記用語情報取得
        model.setListTFAttentionAppendicesTermEntity(getTFAttentionAppendicesTermEntity(linkingOrderInfoEntity.getFItemId()));

        // 品番SKU情報の色、サイズのパターン情報取得
        model.setListTSkuEntity(getTSkuEntity(linkingOrderInfoEntity.getPartNoId()));

        // 品番SKU情報の色のパターン情報取得
        final List<String> colorCodeList = toColorCodeList(model.getListTSkuEntity());

        // 組成情報取得
        model.setListExtendedTCompositionEntity(getExtendedTCompositionEntity(linkingOrderInfoEntity.getPartNoId(), colorCodeList));

        return model;
    }

    /**
     * ユーザIDを取得する.
     *
     * @return ユーザID
     */
    public BigInteger getAdminUserId() {
        final String accountName = propertyComponent.getCommonProperty().getAdminUserAccountName();
        final String company = propertyComponent.getCommonProperty().getAdminUserCompany();
        return mUserRepository.findByAccountNameAndCompanyIgnoreSystemManaged(accountName, company)
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002))).getId();
    }

    /**
     * 連携対象となる資材発注情報を取得し、資材発注.連携ステータスを「連携中(1)」に更新する.
     *
     * <pre>
     *  連携対象の条件
     *   ・資材発注.確定ステータス が「確定(1)」、かつ、資材発注.責任発注が「TRUE(1)」
     *   または、
     *   ・資材発注.確定ステータス が「確定(1)」、かつ、資材発注.責任発注が「FALSE(0)」の場合、
     *    品番.優良誤認承認区分（組成）、（国）、（有害物質）がすべて、「非（0）」または「承認済（9）」
     * </pre>
     *
     * @param userId
     *            ユーザID
     * @return 確定済、未連携のフクキタル発注情報のリスト
     */
    @Transactional
    public List<ExtendedTFOrderLinkingEntity> getOrderInfo(final BigInteger userId) {
        final List<ExtendedTFOrderLinkingEntity> listLinkingOrderInfo = extendedTFOrderRepository.find(PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .filter(entity -> {
                    if (entity.getIsResponsibleOrder() == BooleanType.TRUE) {
                        // 責任発注の場合、連携対象とする
                        return true;
                    }
                    // 責任発注ではない場合、品番.優良誤認承認区分（組成）（国）（有害物質）がすべて、「非（0）」または「承認済（9）」の場合、連携対象とする
                    if ((entity.getQualityCompositionStatus() == QualityApprovalType.NON_TARGET
                            || entity.getQualityCompositionStatus() == QualityApprovalType.ACCEPT)
                            && (entity.getQualityCooStatus() == QualityApprovalType.NON_TARGET || entity.getQualityCooStatus() == QualityApprovalType.ACCEPT)
                            && (entity.getQualityHarmfulStatus() == QualityApprovalType.NON_TARGET
                                    || entity.getQualityHarmfulStatus() == QualityApprovalType.ACCEPT)) {
                        return true;
                    }
                    return false;
                }).collect(Collectors.toList());

        // 連携対象の資材発注.連携ステータスを「連携中(1)」に更新
        if (!listLinkingOrderInfo.isEmpty()) {
            final List<BigInteger> ids = listLinkingOrderInfo.stream().map(entity -> entity.getId()).collect(Collectors.toList());
            tFOrderRepository.updateLinkingStatus(FukukitaruMasterLinkingStatusType.LINKING, ids, userId);
        }

        return listLinkingOrderInfo;
    }

    /**
     * フクキタル発注情報を取得する.
     *
     * @param fOrderId
     *            フクキタル発注ID
     * @return フクキタル発注情報
     */
    private List<ExtendedTFOrderSkuEntity> getTFOrderSkuList(final BigInteger fOrderId) {
        final List<ExtendedTFOrderSkuEntity> listExtendedTFOrderSkuEntity = new ArrayList<ExtendedTFOrderSkuEntity>();
        // 洗濯ネーム
        listExtendedTFOrderSkuEntity.addAll(extendedTFOrderSkuRepository
                .findByFOrderIdJoinMaterialWashName(fOrderId, FukukitaruMasterMaterialType.WASH_NAME.getValue(), PageRequest.of(0, Integer.MAX_VALUE))
                .getContent());
        // アテンション下札
        listExtendedTFOrderSkuEntity.addAll(extendedTFOrderSkuRepository.findByFOrderIdJoinMaterialAttentionHangTag(fOrderId,
                FukukitaruMasterMaterialType.ATTENTION_HANG_TAG.getValue(), PageRequest.of(0, Integer.MAX_VALUE)).getContent());
        // アテンションネーム
        listExtendedTFOrderSkuEntity.addAll(extendedTFOrderSkuRepository
                .findByFOrderIdJoinMaterialAttentionName(fOrderId, FukukitaruMasterMaterialType.ATTENTION_NAME.getValue(), PageRequest.of(0, Integer.MAX_VALUE))
                .getContent());
        // アテンションタグ
        listExtendedTFOrderSkuEntity.addAll(extendedTFOrderSkuRepository
                .findByFOrderIdJoinMaterialAttentionTag(fOrderId, FukukitaruMasterMaterialType.ATTENTION_TAG.getValue(), PageRequest.of(0, Integer.MAX_VALUE))
                .getContent());
        // 下札
        listExtendedTFOrderSkuEntity.addAll(extendedTFOrderSkuRepository
                .findByFOrderIdJoinMaterialHangTag(fOrderId, FukukitaruMasterMaterialType.HANG_TAG.getValue(), PageRequest.of(0, Integer.MAX_VALUE))
                .getContent());
        // 下札同封副資材
        listExtendedTFOrderSkuEntity.addAll(extendedTFOrderSkuRepository.findByFOrderIdJoinMaterialHangTagAuxiliary(fOrderId,
                FukukitaruMasterMaterialType.HANG_TAG_AUXILIARY_MATERIAL.getValue(), PageRequest.of(0, Integer.MAX_VALUE)).getContent());
        // 洗濯ネーム同封副資材
        listExtendedTFOrderSkuEntity.addAll(extendedTFOrderSkuRepository.findByFOrderIdJoinMaterialWashAuxiliary(fOrderId,
                FukukitaruMasterMaterialType.WASH_AUXILIARY_MATERIAL.getValue(), PageRequest.of(0, Integer.MAX_VALUE)).getContent());
        // NERGY用メリット下札
        listExtendedTFOrderSkuEntity.addAll(extendedTFOrderSkuRepository.findByFOrderIdJoinMaterialHangTagNergyMerit(fOrderId,
                FukukitaruMasterMaterialType.HANG_TAG_NERGY_MERIT.getValue(), PageRequest.of(0, Integer.MAX_VALUE)).getContent());

        return listExtendedTFOrderSkuEntity;
    }

    /**
     * フクキタル品番情報を取得する.
     *
     * @param fItemId
     *            フクキタル品番ID
     * @param orderId
     *            発注ID
     * @return フクキタル品番情報のリスト
     */
    private ExtendedTFItemLinkingEntity getItemInfo(final BigInteger fItemId, final BigInteger orderId) {
        return extendedTFItemLinkingRepository.findByFItemId(fItemId, orderId).orElseThrow(() -> new ScheduleException(
                LogStringUtil.of("getItemInfo").message("t_f_item not found.").value("fItemId", fItemId).value("orderId", orderId).build()));
    }

    /**
     * 品番情報のカラー、サイズの情報を取得する.
     *
     * @param fItemId
     *            フクキタル品番ID
     * @return 品番情報のカラー、サイズの情報のリスト
     */
    private List<TSkuEntity> getTSkuEntity(final BigInteger fItemId) {
        return tSkuRepository.findByPartNoId(fItemId, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("colorCode")))).getContent();
    }

    /**
     * 組成情報を取得する.
     * パーツが空（もしくはその他）で素材がZZ（その他）のみの場合、フクキタルに連携しない。
     * 組成が空の場合、共通の組成を設定する
     *
     * @param fItemId
     *            フクキタル品番ID
     * @param colorCodeList
     *            カラーコードのリスト
     * @return 組成情報のリスト
     */
    private List<ExtendedTCompositionLinkingEntity> getExtendedTCompositionEntity(final BigInteger fItemId, final List<String> colorCodeList) {
        final Map<String, List<ExtendedTCompositionLinkingEntity>> map = extendedTCompositionLinkingRepository
                .findByPartNoId(fItemId, PageRequest.of(0, Integer.MAX_VALUE)).getContent().stream()
                // カラーコードでグルーピング
                .collect(Collectors.groupingBy(ExtendedTCompositionLinkingEntity::getColorCode));

        final List<ExtendedTCompositionLinkingEntity> list = new ArrayList<>();

        // 共通の組成を取得
        final List<ExtendedTCompositionLinkingEntity> commonCompositions = getCommonCompositions(map);

        // 共通を追加
        if (CollectionUtils.isNotEmpty(commonCompositions)) {
            list.addAll(commonCompositions);
        }

        colorCodeList.stream().forEach(colorCode -> {
            final List<ExtendedTCompositionLinkingEntity> compositions = map.get(colorCode);

            if (CollectionUtils.isEmpty(compositions)) {
                // 組成がない場合、共通の組成を設定する
                if (CollectionUtils.isNotEmpty(commonCompositions)) {
                    list.addAll(toCopyCompositions(colorCode, commonCompositions));
                }
            } else if (isLinkingCompositions(compositions)) {
                // 連携対象の組成の場合、追加
                list.addAll(compositions);
            }
        });

        return list;
    }

    /**
     * メール本文に必要な品番、品名情報を取得する.
     *
     * @param partNoId
     *            品番ID
     * @return 品番、品名情報
     */
    public TItemEntity getTItemEntity(final BigInteger partNoId) {
        return tItemRepository.findByIdAndDeletedAtIsNull(partNoId)
                .orElseThrow(() -> new ScheduleException(LogStringUtil.of("getTItemEntity")
                 .message("t_item not found.").value("partNoId", partNoId).build()));
    }

    /**
     * 生産メーカーを取得する.
     *
     * @param orderId
     *            発注ID
     * @return 生産メーカー
     */
    public String getSire(final BigInteger orderId) {
        return extendedTOrderLinkingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ScheduleException(LogStringUtil.of("getSire").message("t_order not found.").value("orderId", orderId).build()))
                .getSire();
    }

    /**
     * フクキタル洗濯パターン情報を取得する.
     *
     * @param fItemId
     *            フクキタル品番ID
     * @return フクキタル洗濯パターン情報リスト
     */
    private List<ExtendedTFWashPatternEntity> getTFWashPatternEntity(final BigInteger fItemId) {
        return tFWashPatternRepository.findByFItemId(fItemId, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("color_code")))).getContent();
    }

    /**
     * アテンション付記用語情報を取得する.
     *
     * @param fItemId
     *            フクキタル品番ID
     * @return フクキタル用アテンションタグ付記用語情報のリスト
     */
    private List<ExtendedTFAttentionAppendicesTermEntity> getTFAttentionAppendicesTermEntity(final BigInteger fItemId) {
        return tFAttentionAppendicesTermRepository.findByFItemId(fItemId, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("color_code")))).getContent();
    }

    /**
     * 洗濯ネーム付記用語情報を取得する.
     *
     * @param fItemId
     *            フクキタル品番ID
     * @return フクキタル用洗濯ネーム付記用語情報のリスト
     */
    private List<ExtendedTFWashAppendicesTermEntity> getTFWashAppendicesTermEntity(final BigInteger fItemId) {
        return tFWashAppendicesTermRepository.findByFItemId(fItemId, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("color_code")))).getContent();
    }


    /**
     * 連携ステータスを更新する.
     *
     * @param type
     *            連携ステータス
     * @param fOrderId
     *            フクキタル発注ID
     * @param updatedUserId
     *            更新ユーザID
     * @return 更新件数
     */
    private FukukitaruMasterLinkingStatusType updateLinkingStatus(final FukukitaruMasterLinkingStatusType type, final BigInteger fOrderId,
            final BigInteger updatedUserId) {
        tFOrderRepository.updateLinkingStatus(type, fOrderId, updatedUserId);
        return type;
    }

    /**
     * 連携ステータスと発注送信日を更新する.
     *
     * @param type
     *            連携ステータス
     * @param fOrderId
     *            フクキタル発注ID
     * @param updatedUserId
     *            更新ユーザID
     * @return 更新件数
     */
    private FukukitaruMasterLinkingStatusType updateLinkingStatusOrderSendAt(final FukukitaruMasterLinkingStatusType type, final BigInteger fOrderId,
            final BigInteger updatedUserId) {
        tFOrderRepository.updateLinkingStatusOrderSendAt(type, fOrderId, updatedUserId);
        return type;
    }

    /**
     * SKUのリストから、カラーコードのリストに変換する.
     *
     * @param skus SKUのリスト
     * @return カラーコードのリスト
     */
    private List<String> toColorCodeList(final List<TSkuEntity> skus) {
        return skus.stream()
                // カラーコードを取得
                .map(sku -> sku.getColorCode())
                // 重複を除外
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 共通の組成を取得する.
     * 連携対象外の組成の場合、空のリストを返却する。
     *
     * @param compositionsMap 組成のマップ
     * @return 共通の組成のリスト
     */
    private List<ExtendedTCompositionLinkingEntity> getCommonCompositions(
            final Map<String, List<ExtendedTCompositionLinkingEntity>> compositionsMap) {
        // 共通の組成を取得
        final List<ExtendedTCompositionLinkingEntity> commonCompositions = compositionsMap.get(COMMON_COLOR_CODE);

        if (CollectionUtils.isNotEmpty(commonCompositions)) {
            if (isLinkingCompositions(commonCompositions)) {
                // 連携対象の組成の場合、返却する
                return commonCompositions;
            }
        }

        return Collections.emptyList();
    }

    /**
     * 連携対象の組成か判定する.
     *
     * <pre>
     * - パーツが空（もしくはその他）で、素材がZZ（その他）のみの場合、連携対象外。空で連携する。
     * </pre>
     *
     * @param compositions 組成のリスト
     * @return true : 連携対象の組成 / false : 連携対象外の組成
     */
    private boolean isLinkingCompositions(
            final List<ExtendedTCompositionLinkingEntity> compositions) {
        return compositions.stream().anyMatch(entity -> !(StringUtils.isEmpty(entity.getParts())
                && StringUtils.equals(entity.getCompositionCode(), OTHER_COMPOSITION_CODE)));
    }

    /**
     * 共通の組成をコピーして、カラーコードを設定した組成のリストを返却する.
     *
     * @param colorCode カラーコード
     * @param compositions コピー元の組成
     * @return {@link CompositionModel} コピー後
     */
    private List<ExtendedTCompositionLinkingEntity> toCopyCompositions(
            final String colorCode,
            final List<ExtendedTCompositionLinkingEntity> compositions) {
        return compositions.stream().map(composition -> {
            final ExtendedTCompositionLinkingEntity copyComposition = new ExtendedTCompositionLinkingEntity();
            BeanUtils.copyProperties(composition, copyComposition);

            // カラーコードを設定
            copyComposition.setColorCode(colorCode);

            return copyComposition;
        }).collect(Collectors.toList());
    }
}
