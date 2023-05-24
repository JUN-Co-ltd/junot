package jp.co.jun.edi.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.FukukitaruItemComponent;
import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.JunpcCodmstOriginCountryComponent;
import jp.co.jun.edi.component.JunpcHinmstComponent;
import jp.co.jun.edi.component.LoginUserComponent;
import jp.co.jun.edi.component.MJanNumberComponent;
import jp.co.jun.edi.component.MisleadingRepresentationComponent;
import jp.co.jun.edi.component.mail.ItemRegistSendMailComponent;
import jp.co.jun.edi.component.model.ItemChangeStateModel;
import jp.co.jun.edi.constants.SizeConstants;
import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.entity.TCompositionEntity;
import jp.co.jun.edi.entity.TExternalSkuEntity;
import jp.co.jun.edi.entity.TFileInfoEntity;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.TOrderSupplierEntity;
import jp.co.jun.edi.entity.TSkuEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderSupplierEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.CompositionModel;
import jp.co.jun.edi.model.ExternalSkuModel;
import jp.co.jun.edi.model.FukukitaruItemModel;
import jp.co.jun.edi.model.ItemFileInfoModel;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.OrderSupplierModel;
import jp.co.jun.edi.model.SkuModel;
import jp.co.jun.edi.model.mail.ItemRegistSendModel;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.repository.TCompositionRepository;
import jp.co.jun.edi.repository.TExternalSkuRepository;
import jp.co.jun.edi.repository.TFileInfoRepository;
import jp.co.jun.edi.repository.TFileRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TOrderSupplierRepository;
import jp.co.jun.edi.repository.TSkuRepository;
import jp.co.jun.edi.repository.extended.ExtendedTItemRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderSupplierRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.CreateServiceResponse;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.ChangeRegistStatusType;
import jp.co.jun.edi.type.ExternalLinkingType;
import jp.co.jun.edi.type.FileInfoMode;
import jp.co.jun.edi.type.MCodmstTblIdType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.OrderCategoryType;
import jp.co.jun.edi.type.PsType;
import jp.co.jun.edi.type.QualityApprovalType;
import jp.co.jun.edi.type.RegistStatusType;
import lombok.extern.slf4j.Slf4j;

/**
 * 品番情報を作成するサービス.
 */
@Service
@Slf4j
public class ItemCreateService
        extends GenericCreateService<CreateServiceParameter<ItemModel>, CreateServiceResponse<ItemModel>> {
    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Autowired
    private TItemRepository itemRepository;

    @Autowired
    private ExtendedTItemRepository exTItemRepository;

    @Autowired
    private TOrderSupplierRepository orderSupplierRepository;

    @Autowired
    private TCompositionRepository compositionRepository;

    @Autowired
    private TSkuRepository skuRepository;

    @Autowired
    private TExternalSkuRepository externalSkuRepository;

    @Autowired
    private TFileRepository fileRepository;

    @Autowired
    private TFileInfoRepository fileInfoRepository;

    @Autowired
    private LoginUserComponent loginUserComponent;

    @Autowired
    private JunpcHinmstComponent junpcHinmstComponent;

    @Autowired
    private JunpcCodmstOriginCountryComponent junpcCodmstOriginCountryComponent;

    @Autowired
    private ItemRegistSendMailComponent itemRegistSendMailComponent;

    @Autowired
    private MisleadingRepresentationComponent misleadingRepresentationComponent;

    @Autowired
    private FukukitaruItemComponent fukukitaruComponent;

    @Autowired
    private MJanNumberComponent mJanNumberComponent;

    @Autowired
    private ItemComponent itemComponent;

    @Autowired
    private ExtendedTOrderSupplierRepository extendedTOrderSupplierRepository;

    // PRD_0142 #10423 JFE add start
    /** TAGDAT作成フラグ（未作成）. */
	private static final String NOT_CREATED = "0";
	// PRD_0142 #10423 JFE add end

    @Override
    protected CreateServiceResponse<ItemModel> execute(final CreateServiceParameter<ItemModel> serviceParameter) {
        final ItemModel item = serviceParameter.getItem();

        // 外部連携区分に 1:JUNoT登録 を設定
        item.setExternalLinkingType(ExternalLinkingType.JUNOT.getValue());

        // 登録ステータス取得
        final RegistStatusType registStatus = RegistStatusType.convertToType(item.getRegistStatus());

        // 登録ステータス変更区分取得
        final ChangeRegistStatusType changeRegistStatusType = ChangeRegistStatusType.convertToType(item.getChangeRegistStatusType());

        log.info("create:" + item.getPartNo());
        // バリデーションチェック
        ResultMessages rsltMsg = checkValidate(item);
        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }

        // 部門取得のためアイテムを取得する
        List<MCodmstEntity> mCodmstEntityList = mCodmstRepository.findByTblidAndCode1AndCode2OrderById(
                MCodmstTblIdType.ITEM.getValue(), item.getBrandCode(), item.getItemCode(), PageRequest.of(0, 1)).getContent();
        MCodmstEntity itemCodmst = new MCodmstEntity();
        if (mCodmstEntityList.size() > 0) {
            itemCodmst = mCodmstEntityList.get(0);
        }

        // ブランドソートのためブランドを取得する
        mCodmstEntityList = mCodmstRepository.findByTblidAndCode1OrderById(
                MCodmstTblIdType.BRAND.getValue(), item.getBrandCode(), PageRequest.of(0, 1)).getContent();
        MCodmstEntity blandCodmst = new MCodmstEntity();
        if (mCodmstEntityList.size() > 0) {
            blandCodmst = mCodmstEntityList.get(0);
        }

        // 丸井デプトブランドを取得
        mCodmstEntityList = mCodmstRepository.findByTblidAndCode1OrderById(
                MCodmstTblIdType.MARUI_DEPT.getValue(), item.getBrandCode(), PageRequest.of(0, 1)).getContent();
        MCodmstEntity maruiDeptCodmst = new MCodmstEntity();
        if (mCodmstEntityList.size() > 0) {
            maruiDeptCodmst = mCodmstEntityList.get(0);
        }

        // 品番情報をEntityにコピー
        TItemEntity tItemEntity = new TItemEntity();
        BeanUtils.copyProperties(item, tItemEntity);
        tItemEntity.setDeptCode(itemCodmst.getItem2()); // 部門をセット
        tItemEntity.setBrandSortCode(blandCodmst.getItem3()); // ブランドソートをセット
        tItemEntity.setMaruiDeptBrand(maruiDeptCodmst.getItem1()); // 丸井デプトブランド
        tItemEntity.setPsType(PsType.PROPER);   // PS区分：P プロパー固定
        tItemEntity.setSaleRetailPrice(BigDecimal.ZERO); // セール区分 0固定
        tItemEntity.setJunpcTanto(loginUserComponent.getAccountNameWithAffiliation(serviceParameter.getLoginUser())); // 社内の場合の連携入力者をセット
        // PRD_0142 #10423 JFE add start
        // TAGDAT作成フラグに未作成を設定
        tItemEntity.setTagdatCreatedFlg(NOT_CREATED);
        // PRD_0142 #10423 JFE add end

        // 優良誤認(組成、国、有害物質)と優良誤認区分をセット
        setQualityStatusAndMisleadingRepresentation(item, tItemEntity);

        // 品番情報の登録
        itemRepository.save(tItemEntity);

        // 優良誤認承認情報のupsert(品番一括登録時のみ)
        if (itemComponent.isUpsertIntoMisleadingRepresentation(item)) {
            item.setId(tItemEntity.getId());
            misleadingRepresentationComponent.upsertMisleadingRepresentation(item, new ItemChangeStateModel(), serviceParameter.getLoginUser());
        }

        final BigInteger itemId = tItemEntity.getId();
        final String partNo = tItemEntity.getPartNo();

        registProductOrderSupplier(tItemEntity, item.getOrderSuppliers(), serviceParameter.getLoginUser());    // 発注先メーカー情報の登録

        registSku(item.getSkus(), itemId, partNo, item, registStatus, changeRegistStatusType); // SKUの登録

        registCompositions(item.getCompositions(), itemId, partNo); // 組成の登録

        registFileInfo(item.getItemFileInfos(), itemId, serviceParameter.getLoginUser().getUserId()); // ファイル情報の登録

        registFKItem(item.getFkItem(), itemId); // フクキタル品番情報の登録

        item.setId(tItemEntity.getId());

        sendMail(item.getId(), serviceParameter.getLoginUser().getAccountName()); // メール送信

        return CreateServiceResponse.<ItemModel>builder().item(item).build();
    }

    /**
     * バリデーションチェックを行う.
     * @param item 品番情報
     * @return ResultMessages
     */
    private ResultMessages checkValidate(final ItemModel item) {
        ResultMessages rsltMsg = ResultMessages.warning();

        if (item.getPartNo() != null) {
            // JUNoT内の品番の重複を確認
            if (itemRepository.existsByPartNo(item.getPartNo())) {
                // 対象レコードが存在する場合、エラーにする
                rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_I_01));
            }

            // 発注生産側の品番重複チェック
            if (junpcHinmstComponent.existsByPartNoAndYear(item.getPartNo(), item.getYear())) {
                // 対象レコードが存在する場合、エラーにする
                rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_I_02));
            }
        }

        // 原産国コードの入力がある場合、存在チェック
        String cooCode = item.getCooCode();
        if (StringUtils.isNotEmpty(cooCode) && !junpcCodmstOriginCountryComponent.isExitsCooCode(cooCode)) {
            // 原産国コードがマスタに存在しない場合、エラーにする
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_I_08));
        }

        // 仕入先メーカー数チェック
        if (itemComponent.isSuppiperLengthOver(item.getOrderSuppliers())) {
            // 登録可能最大件数を超える場合、エラーにする
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_I_31, SizeConstants.SUPPRIER_REGIST));
        }

        return rsltMsg;
    }

    /**
     * 優良誤認(組成、国、有害物質)と優良誤認区分をセットする.
     * @param itemModel 品番情報Model
     * @param tItemEntity 優良誤認をセットする品番情報Entity
     */
    private void setQualityStatusAndMisleadingRepresentation(final ItemModel itemModel, final TItemEntity tItemEntity) {
        // 優良誤認(組成、国、有害物質)の初期値をセット(全て非対象(0))
        tItemEntity.setQualityCompositionStatus(QualityApprovalType.NON_TARGET.getValue());
        tItemEntity.setQualityCooStatus(QualityApprovalType.NON_TARGET.getValue());
        tItemEntity.setQualityHarmfulStatus(QualityApprovalType.NON_TARGET.getValue());

        // 優良誤認対象であれば優良誤認(組成)を対象にセット
        if (misleadingRepresentationComponent.isQualityCompositionTarget(itemModel)) {
            tItemEntity.setQualityCompositionStatus(QualityApprovalType.TARGET.getValue());
        }
        // 優良誤認対象であれば優良誤認(国)を対象にセット
        if (misleadingRepresentationComponent.isQualityCooTarget(itemModel.getCooCode())) {
            tItemEntity.setQualityCooStatus(QualityApprovalType.TARGET.getValue());
        }
        // 優良誤認対象であれば優良誤認(有害物質)を対象にセット
        if (misleadingRepresentationComponent.isQualityHarmfulTarget(itemModel.getOrderSuppliers().get(0).getSupplierCode())) {
            tItemEntity.setQualityHarmfulStatus(QualityApprovalType.TARGET.getValue());
        }

        // 優良誤認区分をセット
        tItemEntity.setMisleadingRepresentation(misleadingRepresentationComponent.isMisleadingRepresentationTarget(tItemEntity));
    }

    /**
     * 生産メーカー情報を登録.
     * @param itemEntity DB登録済の品番情報
     * @param orderSupplierModelList    画面から入力されたメーカー情報のリスト
     * @param loginUser ログインユーザ情報
     */
    private void registProductOrderSupplier(final TItemEntity itemEntity,
                                             final List<OrderSupplierModel> orderSupplierModelList,
                                             final CustomLoginUser loginUser) {

        final List<TOrderSupplierEntity> list = orderSupplierModelList.stream().map(model -> {

            final TOrderSupplierEntity orderSupplierEntity = new TOrderSupplierEntity();

            orderSupplierEntity.setPartNoId(itemEntity.getId());                             // 品番ID
            orderSupplierEntity.setSupplierCode(model.getSupplierCode());                    // メーカーコード
            orderSupplierEntity.setOrderCategoryType(OrderCategoryType.PRODUCT);             // 発注分類区分：製品で固定
            orderSupplierEntity.setSupplierFactoryCode(model.getSupplierFactoryCode());      // 工場コード
            orderSupplierEntity.setConsignmentFactory(model.getConsignmentFactory());        // 委託先工場名
            orderSupplierEntity.setSupplierStaffId(loginUserComponent.getUserIdWithoutAffiliation(loginUser)); // 社外の場合の生産メーカー担当をセット

            return orderSupplierEntity;

        })
        .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(list)) {
            orderSupplierRepository.saveAll(list);

            // 登録した発注先メーカー情報で一番小さいIDを取得する。
             BigInteger orderSupplierId =  list.stream().map(entity -> entity.getId())
                                                 .min(Comparator.naturalOrder()).get();

            // 発注先メーカーID(最新製品)を更新する。
            itemEntity.setCurrentProductOrderSupplierId(orderSupplierId);
            itemRepository.save(itemEntity);
        }
    }

    /**
     * SKUの登録を行う.
     * @param skuList SKUリスト
     * @param itemId 品番ID
     * @param partNo 品番
     * @param item 品番情報
     * @param registStatus 登録ステータス
     * @param changeRegistStatusType 登録ステータス変更区分
     */
    private void registSku(final List<SkuModel> skuList, final BigInteger itemId, final String partNo, final ItemModel item,
            final RegistStatusType registStatus,
            final ChangeRegistStatusType changeRegistStatusType) {
        if (Objects.nonNull(skuList)) {
            // 代表JAN区分
            BooleanType representationJanFlg = BooleanType.TRUE;

            for (final SkuModel skuModel : skuList) {
                final TSkuEntity skuEntity = new TSkuEntity();
                BeanUtils.copyProperties(skuModel, skuEntity); // データをコピー
                skuEntity.setPartNoId(itemId); // 品番情報のIDをセット
                skuEntity.setPartNo(partNo); // 品番をセット

                // 代表JAN区分を設定
                skuEntity.setRepresentationJanFlg(representationJanFlg);
                // 先頭1件のみ代表JAN区分を設定
                representationJanFlg = BooleanType.FALSE;

                if (mJanNumberComponent.isInHouseJanNumbering(item.getJanType(), registStatus, changeRegistStatusType, skuModel.getJanCode())) {
                    // 自社JANの採番対象の場合、採番したJANを設定
                    skuEntity.setJanCode(mJanNumberComponent.createJan(item.getBrandCode()));
                } else {
                    // 採番対象以外の場合、前ゼロ付与する。
                    skuEntity.setJanCode(mJanNumberComponent.zeroPaddingArticleNumber(item.getJanType(), skuModel.getJanCode()));
                }

                skuRepository.save(skuEntity);

                // 外部SKU登録
                registExternalSku(skuModel.getExternalSku(), itemId, skuEntity.getId());
            }
        }
    }

    /**
     * 外部SKUの登録を行う.
     * @param externalSkuModel 外部SKU
     * @param partNoId 品番ID
     * @param skuId SKUID
     */
    private void registExternalSku(final ExternalSkuModel externalSkuModel, final BigInteger partNoId, final BigInteger skuId) {
        if (Objects.nonNull(externalSkuModel)) {
            final TExternalSkuEntity externalSkuEntity = new TExternalSkuEntity();
            BeanUtils.copyProperties(externalSkuModel, externalSkuEntity);
            externalSkuEntity.setPartNoId(partNoId); // 品番情報のIDをセット
            externalSkuEntity.setSkuId(skuId); // SKU情報のIDをセット
            externalSkuRepository.save(externalSkuEntity);
        }
    }

    /**
     * 組成の登録を行う.
     * @param compositionList 組成リスト
     * @param itemId 品番ID
     * @param partNo 品番
     */
    private void registCompositions(final List<CompositionModel> compositionList, final BigInteger itemId, final String partNo) {
        if (Objects.nonNull(compositionList)) {
            // 組成の整形
            for (final TCompositionEntity entity : itemComponent.compositionsModelToEntity(
                    compositionList,
                    itemId,
                    partNo)) {
                compositionRepository.save(entity);
            }
        }
    }

    /**
     * ファイル情報の登録を行う.
     * @param itemFileInfoList 品番ファイル情報リスト
     * @param itemId 品番ID
     * @param userId ユーザーID
     */
    private void registFileInfo(final List<ItemFileInfoModel> itemFileInfoList, final BigInteger itemId, final BigInteger userId) {
        if (Objects.nonNull(itemFileInfoList)) {
            final Date deleteAt = new Date(); // 削除日付

            for (final ItemFileInfoModel tFileInfoModel : itemFileInfoList) {
                final TFileInfoEntity tFileInfoEntity = new TFileInfoEntity();
                BeanUtils.copyProperties(tFileInfoModel, tFileInfoEntity); // データをコピー

                tFileInfoEntity.setPartNoId(itemId); // 品番IDをセット

                if (FileInfoMode.INSERT == tFileInfoModel.getMode()) {
                    fileInfoRepository.save(tFileInfoEntity);
                } else if (FileInfoMode.DELETED == tFileInfoModel.getMode()) {
                    // 削除
                    // データ本体の削除日を登録
                    fileRepository.updateDeleteAtById(tFileInfoModel.getFileNoId(), userId);

                    // 該当のファイルIDのレコードも削除する
                    tFileInfoEntity.setDeletedAt(deleteAt);
                    fileInfoRepository.save(tFileInfoEntity);
                }
            }
        }
    }

    /**
     * メール送信を行う.
     * @param itemId 品番ID
     * @param accountName アカウント名
     */
    private void sendMail(final BigInteger itemId, final String accountName) {
        // メール用モデル(itemRegistSendModel)にデータつめる
        final ItemRegistSendModel itemRegistSendModel = new ItemRegistSendModel();
        final ExtendedTItemEntity extendedTItemEntity = exTItemRepository.findById(itemId).orElse(new ExtendedTItemEntity());

        // メーカー情報取得
        final ExtendedTOrderSupplierEntity exTOrderSupplierEntity =
                extendedTOrderSupplierRepository.findById(extendedTItemEntity.getCurrentProductOrderSupplierId()).orElse(new ExtendedTOrderSupplierEntity());

        BeanUtils.copyProperties(extendedTItemEntity, itemRegistSendModel);

        // メーカーコード
        itemRegistSendModel.setMdfMakerCode(exTOrderSupplierEntity.getSupplierCode());
        // メーカー名
        itemRegistSendModel.setMdfMakerName(exTOrderSupplierEntity.getSupplierName());
        // メーカー担当者
        itemRegistSendModel.setMdfMakerStaffId(exTOrderSupplierEntity.getSupplierStaffId());

        // メール送信
        itemRegistSendMailComponent.sendMail(itemRegistSendModel, accountName);
    }

    /**
     * フクキタル品番情報の登録を行う.
     * @param fkItemModel フクキタル品番情報モデル
     * @param itemId 品番ID
     */
    private void registFKItem(final FukukitaruItemModel fkItemModel, final BigInteger itemId) {
        fkItemModel.setPartNoId(itemId);
        fukukitaruComponent.save(fkItemModel);
    }
}
