package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.FukukitaruItemComponent;
import jp.co.jun.edi.component.ItemArticleNumberValidateComponent;
import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.JunpcCodmstMaruiItemComponent;
import jp.co.jun.edi.component.JunpcCodmstOriginCountryComponent;
import jp.co.jun.edi.component.JunpcHinmstComponent;
import jp.co.jun.edi.component.LoginUserComponent;
import jp.co.jun.edi.component.MJanNumberComponent;
import jp.co.jun.edi.component.MisleadingRepresentationComponent;
import jp.co.jun.edi.component.OrderComponent;
import jp.co.jun.edi.component.mail.PartRegistSendMailComponent;
import jp.co.jun.edi.component.model.ItemChangeStateModel;
import jp.co.jun.edi.constants.SizeConstants;
import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.entity.TCompositionEntity;
import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.entity.TFileInfoEntity;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.TMisleadingRepresentationEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.entity.TOrderSupplierEntity;
import jp.co.jun.edi.entity.TSkuEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderSupplierEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.ItemFileInfoModel;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.MisleadingRepresentationFileModel;
import jp.co.jun.edi.model.OrderSupplierModel;
import jp.co.jun.edi.model.SkuModel;
import jp.co.jun.edi.model.mail.PartRegistSendModel;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.repository.TCompositionRepository;
import jp.co.jun.edi.repository.TDeliveryRepository;
import jp.co.jun.edi.repository.TFileInfoRepository;
import jp.co.jun.edi.repository.TFileRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TMisleadingRepresentationFileRepository;
import jp.co.jun.edi.repository.TMisleadingRepresentationRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSupplierRepository;
import jp.co.jun.edi.repository.TSkuRepository;
import jp.co.jun.edi.repository.extended.ExtendedTItemRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderSupplierRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;
import jp.co.jun.edi.type.ApprovalType;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.ChangeRegistStatusType;
import jp.co.jun.edi.type.FileInfoMode;
import jp.co.jun.edi.type.ItemValidationType;
import jp.co.jun.edi.type.LinkingStatusType;
import jp.co.jun.edi.type.MCodmstTblIdType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.OrderCategoryType;
import jp.co.jun.edi.type.QualityApprovalType;
import jp.co.jun.edi.type.RegistStatusType;

/**
 * 品番・SKU・組成更新処理.
 */
@Service
public class ItemUpdateService
extends GenericUpdateService<UpdateServiceParameter<ItemModel>, UpdateServiceResponse<ItemModel>> {

    @Autowired
    private TItemRepository itemRepository;

    @Autowired
    private ExtendedTItemRepository exTItemRepository;

    @Autowired
    private TCompositionRepository compositionRepository;

    @Autowired
    private TSkuRepository skuRepository;

    @Autowired
    private TFileRepository fileRepository;

    @Autowired
    private TFileInfoRepository fileInfoRepository;

    @Autowired
    private TMisleadingRepresentationRepository misleadingRepresentationRepository;

    @Autowired
    private TMisleadingRepresentationFileRepository misleadingRepresentationFileRepository;

    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Autowired
    private TOrderRepository orderRepository;

    @Autowired
    private TDeliveryRepository deliveryRepository;

    @Autowired
    private LoginUserComponent loginUserComponent;

    @Autowired
    private JunpcHinmstComponent junpcHinmstComponent;

    @Autowired
    private JunpcCodmstOriginCountryComponent junpcCodmstOriginCountryComponent;

    @Autowired
    private JunpcCodmstMaruiItemComponent junpcCodmstMaruiItemComponent;

    @Autowired
    private PartRegistSendMailComponent partRegistSendMailComponent;

    @Autowired
    private ItemComponent itemComponent;

    @Autowired
    private OrderComponent orderComponent;

    @Autowired
    private MisleadingRepresentationComponent misleadingRepresentationComponent;

    @Autowired
    private FukukitaruItemComponent fukukitaruItemComponent;

    @Autowired
    private TOrderSupplierRepository orderSupplierRepository;

    @Autowired
    private ItemArticleNumberValidateComponent itemArticleNumberValidateComponent;

    @Autowired
    private MJanNumberComponent mJanNumberComponent;

    @Autowired
    private ExtendedTOrderSupplierRepository extendedTOrderSupplierRepository;

    /** 丸井品番デフォルト値. */
    private static final String DEFAULT_MARUI_GARMENT_NO = "000000";

    /** ゾーンコードデフォルト値. */
    private static final String DEFAULT_ZONE_CODE = "00";

	// PRD_0142 #10423 JFE add start
    /** TAGDAT作成フラグ（未作成）. */
	private static final String NOT_CREATED = "0";
	// PRD_0142 #10423 JFE add end

    @Override
    protected UpdateServiceResponse<ItemModel> execute(final UpdateServiceParameter<ItemModel> serviceParameter) {

        final ItemModel reqItem = serviceParameter.getItem();
        final CustomLoginUser loginUser = serviceParameter.getLoginUser();
        final BigInteger partNoId = reqItem.getId();

        // 登録ステータス変更区分取得
        final ChangeRegistStatusType changeRegistStatusType = ChangeRegistStatusType.convertToType(reqItem.getChangeRegistStatusType());

        // 品番情報を取得する
        final Optional<TItemEntity> dbItemOptimal = itemRepository.findById(partNoId);

        // 発注情報リストを取得する
        final List<TOrderEntity> dbOrders = orderRepository.findByPartNoId(partNoId);

        // 削除・存在チェック
        existenceCheck(dbItemOptimal);

        // 更新前DBの品番情報
        final TItemEntity dbItemEntity = dbItemOptimal.get();

        // 更新前の品番情報をモデルで取得する(SKU、Composition込み)
        final ItemModel dbRegisteredItem = serviceParameter.getPreItem();

        // 品番情報の変更状態取得
        final ItemChangeStateModel itemChangeState = itemComponent.getItemChangeState(reqItem, dbRegisteredItem);

        // 更新可否チェック
        checkCanUpdated(reqItem, dbItemEntity, dbOrders, itemChangeState);

        // 更新データ作成
        final TItemEntity itemEntityForUpdate = generateSaveData(reqItem, dbItemEntity, itemChangeState, loginUser);

        // 更新
        update(itemEntityForUpdate, reqItem, dbOrders, itemChangeState, changeRegistStatusType, loginUser);

        // 商品を品番として登録する時のみメール送信
        if (ChangeRegistStatusType.PART == changeRegistStatusType) {
            sendMail(partNoId, loginUser.getAccountName());
        }

        return UpdateServiceResponse.<ItemModel>builder().item(reqItem).build();
    }

    /**
     * @param reqItem リクエストパラメータ
     * @param dbItemEntity DBの品番情報
     * @param itemChangeState 変更状態
     * @param loginUser ユーザー情報
     * @return 更新用品番Entity
     */
    private TItemEntity generateSaveData(
            final ItemModel reqItem,
            final TItemEntity dbItemEntity,
            final ItemChangeStateModel itemChangeState,
            final CustomLoginUser loginUser) {

        final TItemEntity itemEntityForUpdate = new TItemEntity();
        BeanUtils.copyProperties(reqItem, itemEntityForUpdate);

        // 発注先メーカーID(最新生地)はDBの値を引き継ぐ
        itemEntityForUpdate.setCurrentMatlOrderSupplierId(dbItemEntity.getCurrentMatlOrderSupplierId());

        // 発注先メーカーID(最新製品)はDBの値を引き継ぐ
        itemEntityForUpdate.setCurrentProductOrderSupplierId(dbItemEntity.getCurrentProductOrderSupplierId());

        // PS区分は元の値を引き継ぐ
        itemEntityForUpdate.setPsType(dbItemEntity.getPsType());

        // セール上代は元の値を引き継ぐ
        itemEntityForUpdate.setSaleRetailPrice(dbItemEntity.getSaleRetailPrice());

        // 優良誤認(組成)の設定
        final int qualityCompositionData = generateQualityCompositionData(reqItem, dbItemEntity, itemChangeState);
        itemEntityForUpdate.setQualityCompositionStatus(qualityCompositionData);

        // 優良誤認(国)の設定
        final int qualityCooData = generateQualityCooData(reqItem, dbItemEntity, itemChangeState);
        itemEntityForUpdate.setQualityCooStatus(qualityCooData);

        // 優良誤認(有害物質)の設定
        itemEntityForUpdate.setQualityHarmfulStatus(generateQualityHarmfulStatus(reqItem, dbItemEntity, itemChangeState));

        // 優良誤認区分の設定
        final boolean isTarget = misleadingRepresentationComponent.isMisleadingRepresentationTarget(itemEntityForUpdate);
        itemEntityForUpdate.setMisleadingRepresentation(isTarget);

        // 部門の設定
        setDept(itemEntityForUpdate, reqItem);

        // ブランドソートの設定
        setBrandSort(itemEntityForUpdate, reqItem);

        // 丸井デプトブランドの設定
        setMaruiDeptBrand(itemEntityForUpdate, reqItem);

        if (itemEntityForUpdate.getMaruiGarmentNo() == null) {
            // 丸井品番のデフォルト値を設定する
            itemEntityForUpdate.setMaruiGarmentNo(DEFAULT_MARUI_GARMENT_NO);
        }

        if (itemEntityForUpdate.getZoneCode() == null) {
            // ゾーンコードのデフォルト値を設定する
            itemEntityForUpdate.setZoneCode(DEFAULT_ZONE_CODE);
        }

        // 社内の場合の連携入力者をセット
        itemEntityForUpdate.setJunpcTanto(loginUserComponent.getAccountNameWithAffiliation(loginUser));

        // 作成日、作成ユーザIDをクリア
        itemEntityForUpdate.setCreatedAt(null);
        itemEntityForUpdate.setCreatedUserId(null);

        // 連携ステータスに0を設定
        itemEntityForUpdate.setLinkingStatus(LinkingStatusType.TARGET);

        // 生産メーカー担当を設定
        itemEntityForUpdate.setCurrentProductOrderSupplierId(dbItemEntity.getCurrentProductOrderSupplierId());

        // PRD_0142 #10423 JFE add start
        // TAGDAT作成フラグに未作成を設定
        itemEntityForUpdate.setTagdatCreatedFlg(NOT_CREATED);
        // PRD_0142 #10423 JFE add end

        return itemEntityForUpdate;
    }

    /**
     * @param reqItem リクエストパラメータ
     * @param dbItemEntity DBの品番情報
     * @param itemChangeState 変更状態
     * @return 優良誤認(組成)に設定する値
     */
    private int generateQualityCompositionData(
            final ItemModel reqItem,
            final TItemEntity dbItemEntity,
            final ItemChangeStateModel itemChangeState) {

        if (!itemChangeState.isAddSkuColor()
                && itemChangeState.getChangedCompositionsColors().size() == 0) {
            // SKU追加なし、かつ組成変更なしの場合は、DB登録値を返す
            return dbItemEntity.getQualityCompositionStatus();
        }

        // 優良誤認非対象(組成の入力なし)であれば「非」を返す
        if (!misleadingRepresentationComponent.isQualityCompositionTarget(reqItem)) {
            return QualityApprovalType.NON_TARGET.getValue();
        }

        // 優良誤認情報を取得
        final Page<TMisleadingRepresentationEntity> mrPage = misleadingRepresentationRepository.findByPartNoId(
                reqItem.getId(), PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id"))));

        // 承認済みが1つもなければ「対象」
        if (isNoApproval(mrPage, itemChangeState.getChangedCompositionsColors())) {
            return QualityApprovalType.TARGET.getValue();
        }

        // 承認済みが1つもであれば「一部」
        return QualityApprovalType.PART.getValue();
    }

    /**
     * @param mrPage 優良誤認情報
     * @param changedCompositionsColors 変更された組成があるカラーコードリスト
     * @return true:承認済みなし(or 今回の変更で全てなくなる)
     */
    private boolean isNoApproval(
            final Page<TMisleadingRepresentationEntity> mrPage,
            final List<String> changedCompositionsColors) {

        return mrPage.getTotalPages() == 0
                || mrPage.getContent().stream().allMatch(m -> nonApproveComposition(m, changedCompositionsColors));
    }

    /**
     * @param mr 優良誤認情報
     * @param changeCompositionsColors 変更された組成があるカラーコードリスト
     * @return true:未承認または承認取り消し
     */
    private boolean nonApproveComposition(
            final TMisleadingRepresentationEntity mr,
            final List<String> changeCompositionsColors) {
        return mr.getApprovalAt() == null || changeCompositionsColors.contains(mr.getColorCode());
    }

    /**
     * @param reqItem リクエストパラメータ
     * @param dbItemEntity DBの品番情報
     * @param itemChangeState 変更状態
     * @return 優良誤認(国)に設定する値
     */
    private int generateQualityCooData(
            final ItemModel reqItem,
            final TItemEntity dbItemEntity,
            final ItemChangeStateModel itemChangeState) {

        // 原産国に変更があるかをチェック
        if (!itemChangeState.isCooCodeChanged()) {
            // 変更なしの場合は、DB登録値
            return dbItemEntity.getQualityCooStatus();
        }

        // 変更後の原産国が優良誤認対象であれば「対象」を返す
        if (misleadingRepresentationComponent.isQualityCooTarget(reqItem.getCooCode())) {
            return QualityApprovalType.TARGET.getValue();
        }

        // 「非」
        return QualityApprovalType.NON_TARGET.getValue();
    }

    /**
     * 優良誤認(有害物質)の設定.
     * 商品の状態で、生産メーカーが変更された場合を考慮.
     *
     * @param reqItem リクエストパラメータ
     * @param dbItemEntity DBの品番情報
     * @param itemChangeState 変更状態
     * @return 優良誤認(有害物質)に設定する値
     */
    private int generateQualityHarmfulStatus(
            final ItemModel reqItem,
            final TItemEntity dbItemEntity,
            final ItemChangeStateModel itemChangeState) {
        // 生産メーカーに変更があるかをチェック
        if (!itemChangeState.isMdfMakerCodeChanged()) {
            // 変更なしの場合は、DB登録値
            return dbItemEntity.getQualityHarmfulStatus();
        }

        // 変更後の生産メーカーが優良誤認対象であれば「対象」を返す
        // 商品の状態では、発注先メーカー情報は1件しか存在しないため、先頭の1件を取得
        return misleadingRepresentationComponent.decideQualityHarmfulStatus(reqItem.getOrderSuppliers().get(0).getSupplierCode()).getValue();
    }

    /**
     * 部門の設定.
     * @param itemEntityForUpdate 更新用品番Entity
     * @param reqItem リクエストパラメータ
     */
    private void setDept(final TItemEntity itemEntityForUpdate, final ItemModel reqItem) {
        if (itemEntityForUpdate.getDeptCode() != null) {
            return;
        }

        // 部門取得のためアイテムを取得する
        final List<MCodmstEntity> mCodmstEntityList = mCodmstRepository.findByTblidAndCode1AndCode2OrderById(
                MCodmstTblIdType.ITEM.getValue(), reqItem.getBrandCode(), reqItem.getItemCode(),
                PageRequest.of(0, 1)).getContent();

        if (mCodmstEntityList.size() > 0) {
            // 部門をセット
            itemEntityForUpdate.setDeptCode(mCodmstEntityList.get(0).getItem2());
        }
    }

    /**
     * ブランドソートの設定.
     * @param itemEntityForUpdate 更新用品番Entity
     * @param reqItem リクエストパラメータ
     */
    private void setBrandSort(final TItemEntity itemEntityForUpdate, final ItemModel reqItem) {
        if (itemEntityForUpdate.getBrandSortCode() != null) {
            return;
        }

        // ブランドソート取得のためブランドを取得する
        final List<MCodmstEntity> mCodmstEntityList = mCodmstRepository.findByTblidAndCode1OrderById(
                MCodmstTblIdType.BRAND.getValue(), reqItem.getBrandCode(),
                PageRequest.of(0, 1)).getContent();

        if (mCodmstEntityList.size() > 0) {
            // ブランドソートをセット
            itemEntityForUpdate.setBrandSortCode(mCodmstEntityList.get(0).getItem3());
        }
    }

    /**
     * 丸井デプトブランドの設定.
     * @param itemEntityForUpdate 更新用品番Entity
     * @param reqItem リクエストパラメータ
     */
    private void setMaruiDeptBrand(final TItemEntity itemEntityForUpdate, final ItemModel reqItem) {
        if (itemEntityForUpdate.getMaruiDeptBrand() != null) {
            return;
        }

        // 丸井デプトブランドを取得
        final List<MCodmstEntity> mCodmstEntityList = mCodmstRepository.findByTblidAndCode1OrderById(
                MCodmstTblIdType.MARUI_DEPT.getValue(), reqItem.getBrandCode(),
                PageRequest.of(0, 1)).getContent();

        if (mCodmstEntityList.size() > 0) {
            // 丸井デプトブランド
            itemEntityForUpdate.setMaruiDeptBrand(mCodmstEntityList.get(0).getItem1());
        }
    }

    /**
     * 更新処理.
     * @param itemEntityForUpdate 更新用品番Entity
     * @param reqItem リクエストパラメータ
     * @param dbOrders DBの発注情報リスト
     * @param itemChangeState 品番情報の変更状態
     * @param changeRegistStatusType 登録ステータス変更区分
     * @param loginUser ユーザー情報
     */
    private void update(
            final TItemEntity itemEntityForUpdate,
            final ItemModel reqItem,
            final List<TOrderEntity> dbOrders,
            final ItemChangeStateModel itemChangeState,
            final ChangeRegistStatusType changeRegistStatusType,
            final CustomLoginUser loginUser) {

        // 登録ステータス取得
        final RegistStatusType registStatus = RegistStatusType.convertToType(reqItem.getRegistStatus());

        // 品番更新
        itemRepository.save(itemEntityForUpdate);

        // 発注先メーカー情報更新
        updateProductOrderSupplier(reqItem, loginUser);

        // SKU情報の更新
        updateSku(reqItem, registStatus, changeRegistStatusType);

        // 組成情報の更新
        updateComposition(reqItem);

        // 品番ファイル情報の更新
        updateItemFileInfo(reqItem, loginUser);

        // 優良誤認承認情報のupsert
        if (itemComponent.isUpsertIntoMisleadingRepresentation(reqItem)) {
            misleadingRepresentationComponent.upsertMisleadingRepresentation(reqItem, itemChangeState, loginUser);
        }

        // 優良誤認検査ファイル情報の更新
        updateMisleadingRepresentationFileInfo(reqItem.getMisleadingRepresentationFiles(), loginUser);

        // 品番IDに紐づく発注情報を更新する
        orderComponent.updateOrderByItemChange(reqItem, dbOrders, itemChangeState);

        // フクキタル品番情報を更新する
        fukukitaruItemComponent.update(reqItem.getFkItem());
    }

    /**
     * 生産メーカー情報を登録.
     *
     * @param reqItem リクエストパラメータ
     * @param loginUser ログインユーザ情報
     */
    private void updateProductOrderSupplier(final ItemModel reqItem, final CustomLoginUser loginUser) {
        final List<TOrderSupplierEntity> list = reqItem
                .getOrderSuppliers()
                .stream()
                .map(m -> toOrderSupplierEntity(m, reqItem.getId(), loginUser))
                .collect(Collectors.toList());
        orderSupplierRepository.saveAll(list);
    }

    /**
     * @param model OrderSupplierModel
     * @param itemId 品番ID
     * @param loginUser ログインユーザー
     * @return 登録用TOrderSupplierEntity
     */
    private TOrderSupplierEntity toOrderSupplierEntity(
            final OrderSupplierModel model,
            final BigInteger itemId,
            final CustomLoginUser loginUser) {
        final TOrderSupplierEntity entity = new TOrderSupplierEntity();

        // ID
        entity.setId(model.getId());

        // 品番ID
        entity.setPartNoId(itemId);

        // メーカーコード
        entity.setSupplierCode(model.getSupplierCode());

        // 発注分類区分：製品で固定
        entity.setOrderCategoryType(OrderCategoryType.PRODUCT);

        // 工場コード
        entity.setSupplierFactoryCode(model.getSupplierFactoryCode());

        // 委託先工場名
        entity.setConsignmentFactory(model.getConsignmentFactory());

        // 生産メーカー担当は更新しない

        return entity;
    }

    /**
     * SKU情報の更新を行う.
     *
     * @param item ItemModel
     * @param registStatus 登録ステータス
     * @param changeRegistStatusType 登録ステータス変更区分
     */
    private void updateSku(final ItemModel item,
            final RegistStatusType registStatus,
            final ChangeRegistStatusType changeRegistStatusType) {

        final Date deletedAt = new Date();

        final List<TSkuEntity> skuEntityList = new ArrayList<TSkuEntity>();

        final List<TSkuEntity> currentSkuEntityList = skuRepository.findByPartNoId(item.getId());

        // Comparator作成(ID/カラーコード/サイズでソート)
        final Comparator<SkuModel> comparator = Comparator
                .comparing(SkuModel::getId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SkuModel::getColorCode).thenComparing(SkuModel::getSize);

        // ModelをEntityに変換
        item.getSkus().stream().sorted(comparator).forEach(tSkuModel -> {
            final TSkuEntity currentSkuEntity = currentSkuEntityList.stream().filter(entity -> {

                if (Objects.isNull(tSkuModel.getId())) {
                    return false;
                }

                return entity.getId().compareTo(tSkuModel.getId()) == 0;
            })
                    .findFirst()
                    .orElse(new TSkuEntity());
            skuEntityList.add(setSkuData(item, tSkuModel, currentSkuEntity, registStatus, changeRegistStatusType));
        });

        // 品番として登録する際に、先頭のSKUに代表JANフラグを立てる
        if (itemComponent.isRegistPartNo(item.getChangeRegistStatusType())) {
            skuEntityList.stream().findFirst().get().setRepresentationJanFlg(BooleanType.TRUE);
        }

        // SKUを保存
        final List<TSkuEntity> resultSkuEntityList = skuRepository.saveAll(skuEntityList);

        // idを抽出
        final List<BigInteger> skuIdList = resultSkuEntityList.stream().map(TSkuEntity::getId)
                .collect(Collectors.toList());

        // 0件の場合は、存在しないID(0)を設定する
        if (skuIdList.size() == 0) {
            skuIdList.add(BigInteger.ZERO);
        }

        // リストに格納したID以外のレコードに削除日を設定する。
        List<TSkuEntity> deleteEntityList = skuRepository.findByPartNoIdAndIds(
                item.getId(), skuIdList,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).getContent();
        for (TSkuEntity deleteEntity : deleteEntityList) {
            deleteEntity.setDeletedAt(deletedAt);
            skuRepository.save(deleteEntity);
        }
    }

    /**
     * 更新用のSKUデータを作成する.
     *
     * @param itemModel 品番情報
     * @param tSkuModel SKU情報
     * @param skuEntity DBに登録されているSKU情報
     * @param registStatus 登録ステータス
     * @param changeRegistStatusType 登録ステータス変更区分
     * @return 更新用データをセットしたTSkuEntity
     */
    private TSkuEntity setSkuData(final ItemModel itemModel, final SkuModel tSkuModel, final TSkuEntity skuEntity,
            final RegistStatusType registStatus,
            final ChangeRegistStatusType changeRegistStatusType) {

        if (Objects.nonNull(skuEntity.getId())) {
            // IDがセットされている場合はJAN/UPC値の書き換えを行う。
            skuEntity.setJanCode(tSkuModel.getJanCode());
            // 色コードの書き換えを行う(色コードが変更されたことを考慮し、色コード再セット)。
            skuEntity.setColorCode(tSkuModel.getColorCode());
            // 品番の書き換えを行う(品種が変更されたことを考慮し、品番再セット)。
            skuEntity.setPartNo(itemModel.getPartNo());
        } else {

            // 新規の場合はデータをコピー
            BeanUtils.copyProperties(tSkuModel, skuEntity);

            // 品番情報のIDをコピー
            skuEntity.setPartNoId((itemModel.getId()));
            // 品番情報の品番IDをコピー
            skuEntity.setPartNo((itemModel.getPartNo()));
        }

        if (mJanNumberComponent.isInHouseJanNumbering(itemModel.getJanType(), registStatus, changeRegistStatusType, skuEntity.getJanCode())) {
            // 自社JANの採番対象の場合、採番したJANを設定
            skuEntity.setJanCode(mJanNumberComponent.createJan(itemModel.getBrandCode()));
        } else {
            // 採番対象以外の場合、前ゼロ付与する。
            skuEntity.setJanCode(mJanNumberComponent.zeroPaddingArticleNumber(itemModel.getJanType(), skuEntity.getJanCode()));
        }

        // 代表JAN区分が1以外の時は0をセット
        if (skuEntity.getRepresentationJanFlg() != BooleanType.TRUE) {
            skuEntity.setRepresentationJanFlg(BooleanType.FALSE);
        }

        return skuEntity;

    }

    /**
     * 組成情報の更新を行う.
     *
     * @param item ItemModel
     */
    private void updateComposition(final ItemModel item) {
        final List<BigInteger> compositionIdList = new ArrayList<>();
        final Date deletedAt = new Date();

        // 組成の整形
        for (final TCompositionEntity entity : itemComponent.compositionsModelToEntity(
                item.getCompositions(),
                item.getId(),
                item.getPartNo())) {
            compositionRepository.save(entity);

            // 更新・登録したIDはリストに格納
            compositionIdList.add(entity.getId());
        }

        // 0件の場合は、存在しないID(0)を設定する
        if (compositionIdList.size() == 0) {
            compositionIdList.add(BigInteger.ZERO);
        }

        // リストに格納したID以外のレコードに削除日を設定する。
        List<TCompositionEntity> deleteEntityList = compositionRepository.findByPartNoIdAndIds(
                item.getId(), compositionIdList,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).getContent();
        for (TCompositionEntity deleteEntity : deleteEntityList) {
            deleteEntity.setDeletedAt(deletedAt);
            compositionRepository.save(deleteEntity);
        }
    }

    /**
     * 品番ファイル情報の更新を行う.
     *
     * @param item ItemModel
     * @param loginUser CustomLoginUser
     */
    private void updateItemFileInfo(final ItemModel item, final CustomLoginUser loginUser) {

        // 削除日付
        final Date deleteAt = new Date();

        TFileInfoEntity tFileInfoEntity;
        for (ItemFileInfoModel tFileInfoModel : item.getItemFileInfos()) {
            tFileInfoEntity = new TFileInfoEntity();
            // データをコピー
            BeanUtils.copyProperties(tFileInfoModel, tFileInfoEntity);
            // 品番IDをセット
            tFileInfoEntity.setPartNoId(item.getId());

            if (FileInfoMode.INSERT == tFileInfoModel.getMode()) {

                fileInfoRepository.save(tFileInfoEntity);

            } else if (FileInfoMode.DELETED == tFileInfoModel.getMode()) {
                // 削除。データ本体の削除日を登録
                // ファイル本体の方をsaveでupdateするためには実態まで取得する
                // 必要があり、通信負荷となるためファイル本体の削除のみ独自updateで対応
                fileRepository.updateDeleteAtById(tFileInfoModel.getFileNoId(), loginUser.getUserId());

                // ファイル情報にIDが入っていたらそのレコードも削除日を登録
                if (tFileInfoModel.getFileNoId() != null) {
                    tFileInfoEntity.setDeletedAt(deleteAt);
                    fileInfoRepository.save(tFileInfoEntity);
                }
            }
        }
    }

    /**
     * 優良誤認検査ファイル情報の更新を行う.
     * 優良誤認検査結果ファイルが削除モードであれば論理削除で更新する.
     * 削除するファイルIDが優良誤認検査結果ファイルテーブルから全て削除されれば
     * ファイル情報テーブルからも論理削除で更新する.
     *
     * @param misleadingRepresentationFileList
     *            List<MisleadingRepresentationFileModel>
     * @param loginUser CustomLoginUser
     */
    private void updateMisleadingRepresentationFileInfo(
            final List<MisleadingRepresentationFileModel> misleadingRepresentationFileList,
            final CustomLoginUser loginUser) {
        for (MisleadingRepresentationFileModel tMisleadingRepresentationModel : misleadingRepresentationFileList) {
            if (FileInfoMode.DELETED == tMisleadingRepresentationModel.getMode()) {
                BigInteger loginUserId = loginUser.getUserId();
                BigInteger fileNoId = tMisleadingRepresentationModel.getFile().getId();
                BigInteger partNoId = tMisleadingRepresentationModel.getPartNoId();

                // ファイルIDと品番IDをキーに優良誤認検査結果ファイルテーブル削除
                misleadingRepresentationFileRepository.updateDeleteAtByFileNoIdAndPartNoId(fileNoId, partNoId,
                        loginUserId);

                // ファイルIDをキーに優良誤認検査結果ファイル件数取得し、0件であればIDをキーにファイル情報テーブル削除
                if (misleadingRepresentationFileRepository.cntByFileNoId(fileNoId) == 0) {
                    fileRepository.updateDeleteAtById(fileNoId, loginUserId);
                }
            }
        }
    }

    /**
     * UPDATEが可能かチェックを行う.
     *
     * @param item 更新対象の品番情報
     * @param itemEntity DBの品番情報
     * @param dbOrders DBの発注情報リスト
     * @param itemChangeState 品番情報の変更状態
     */
    private void checkCanUpdated(final ItemModel item, final TItemEntity itemEntity,
            final List<TOrderEntity> dbOrders,
            final ItemChangeStateModel itemChangeState) {

        // 外部連携区分:JUNoT登録以外の場合更新不可
        itemComponent.validateReadOnly(itemEntity.getExternalLinkingType());

        // 品番項目変更可否チェック
        checkCanChangeItem(item, itemEntity, dbOrders, itemChangeState);

        // 品番重複チェック
        partNumberDuplicationCheck(item, itemEntity);

        // 登録ステータスチェック
        regsistStatusCheck(item, itemEntity);

        final ItemValidationType validationType = item.getValidationType();
        final String cooCode = item.getCooCode();
        if (StringUtils.isNotEmpty(cooCode)) {
            // 原産国コードが入力されている場合、原産国コード存在チェック
            checkCooCodeExists(cooCode);
        } else if (validationType == ItemValidationType.PART) {
            // 入力されていない場合、品番バリデーションであればエラー
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_I_12));
        }

        // 品番バリデーション時に丸井品番チェック
        if (validationType == ItemValidationType.PART) {
            maruiGarmentNoCheck(item, itemEntity);
        }

        // 仕入先メーカー数チェック
        if (itemComponent.isSuppiperLengthOver(item.getOrderSuppliers())) {
            // 登録可能最大件数を超える場合、エラーにする
            throw new BusinessException(ResultMessages.warning().add(
                    ResultMessage.fromCode(MessageCodeType.CODE_I_31, SizeConstants.SUPPRIER_REGIST)));
        }

        // JAN/UPCチェック
        final List<ResultMessage> errorResultMessageList = itemArticleNumberValidateComponent.validateArticleNumber(item);

        if (CollectionUtils.isNotEmpty(errorResultMessageList)) {
            throw new BusinessException(ResultMessages.warning().addAll(errorResultMessageList));
        }
    }

    /**
     * 品番項目の変更が可能かチェックを行う.
     *
     * @param itemModel 更新対象の品番情報
     * @param itemEntity 最新のDBの品番情報
     * @param currentOrderList 最新のDBの発注情報リスト
     * @param itemChangeState 品番情報の変更状態
     */
    private void checkCanChangeItem(final ItemModel itemModel, final TItemEntity itemEntity,
            final List<TOrderEntity> currentOrderList,
            final ItemChangeStateModel itemChangeState) {
        // 発注情報リストがない場合は処理しない
        if (currentOrderList.isEmpty()) {
            return;
        }

        // 初回納品依頼承認済 または 全ての発注が完納
        // DBから最新の承認済の納品依頼情報リストを取得する
        List<TDeliveryEntity> approvedDeliveryList = deliveryRepository
                .findMatchApproveStatusDeliverysByPartNoId(itemModel.getId(), ApprovalType.APPROVAL.getValue());

        // 登録済みの組成が変更された場合
        if (itemChangeState.isRegistedCompositionChanged()) {
            // 初回納品依頼承認済または全ての発注が完納の場合は登録済みの組成変更不可となるため、エラーにする
            if (orderComponent.isCompleteOrderList(currentOrderList) || !approvedDeliveryList.isEmpty()) {
                throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_I_14));
            }
        }
    }

    /**
     * 品番重複チェック.
     *
     * @param item 重複チェックを行う品番情報
     * @param itemEntity DBの品番情報
     */
    private void partNumberDuplicationCheck(final ItemModel item, final TItemEntity itemEntity) {
        if (item.getPartNo() != null) {
            if (StringUtils.equals(item.getPartNo(), itemEntity.getPartNo())) {
                // 品番が変更されていない場合、処理を抜ける
                return;
            }

            // JUNoT内の品番の重複を確認
            if (itemRepository.existsByPartNo(item.getPartNo())) {
                // 対象レコードが存在する場合、エラーにする
                throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_I_01));
            }
            // 発注生産側の品番重複チェック
            if (item.getChangeRegistStatusType() != null) {
                if (ChangeRegistStatusType.PART.getValue() == item.getChangeRegistStatusType()
                        && junpcHinmstComponent.existsByPartNoAndYear(item.getPartNo(), item.getYear())) {
                    // 対象レコードが存在する場合、エラーにする
                    throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_I_02));
                }
            }

        }
    }

    /**
     * 登録ステータスチェック.
     *
     * @param item 更新データ
     * @param nowItemEntity 更新先のデータ
     */
    private void regsistStatusCheck(final ItemModel item, final TItemEntity nowItemEntity) {

        // 登録ステータス変更区分がnullの時に品番に対して商品の状態で更新を行おうとするとエラーにする。
        if (item.getChangeRegistStatusType() == null) {
            if (item.getRegistStatus() != nowItemEntity.getRegistStatus()
                    && item.getRegistStatus() == RegistStatusType.ITEM.getValue()) {

                // 更新データが商品で、更新対象データ品番の場合、エラーにする
                throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_I_04));
            }
        } else {
            // 登録ステータス変更区分：1(品番)の時に既に品番になっている時にエラー
            if (item.getChangeRegistStatusType() == ChangeRegistStatusType.PART.getValue()
                    && nowItemEntity.getRegistStatus() == RegistStatusType.PART.getValue()) {
                throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_I_05));
            }
        }

    }

    /**
     * 丸井品番のチェック.
     * ・丸井品番の存在チェック
     * ・丸井品番の必須チェック
     * ・丸井品番の更新可否チェック
     *
     * @param itemModel 更新対象の品番情報
     * @param currentItemEntity DBの最新の品番情報
     */
    private void maruiGarmentNoCheck(final ItemModel itemModel, final TItemEntity currentItemEntity) {
        // 丸井品番の存在チェック
        if (StringUtils.isNotEmpty(itemModel.getMaruiGarmentNo())
                && !DEFAULT_MARUI_GARMENT_NO.equals(itemModel.getMaruiGarmentNo())) {
            checkMaruiGarmentNoExists(itemModel);
        }

        // 丸井品番の必須チェック
        // 丸井品番リストがあるのに丸井品番がない場合、業務エラー
        if (junpcCodmstMaruiItemComponent.isExitsMaruiItemList(itemModel.getBrandCode())
                && (StringUtils.isEmpty(itemModel.getMaruiGarmentNo())
                        || DEFAULT_MARUI_GARMENT_NO.equals(itemModel.getMaruiGarmentNo()))) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_I_15));
        }

        // 丸井品番の更新可否チェック
        // 丸井品番に変更があるかをチェック
        if (!Objects.equals(itemModel.getMaruiGarmentNo(), currentItemEntity.getMaruiGarmentNo())) {
            // 初回納品依頼承認済の場合は丸井品番変更不可となるため、納品依頼承認ステータスのチェックをする
            checkIsDeliveryApproved(itemModel);
        }
    }

    /**
     * 削除・存在チェック.
     *
     * @param itemOptimal 取得したデータ
     */
    private void existenceCheck(final Optional<TItemEntity> itemOptimal) {
        if (!itemOptimal.isPresent() || itemOptimal.get().getDeletedAt() != null) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_002));
        }
    }

    /**
     * 原産国コードの存在チェック.
     *
     * @param cooCode 原産国コード
     */
    private void checkCooCodeExists(final String cooCode) {
        if (!junpcCodmstOriginCountryComponent.isExitsCooCode(cooCode)) {
            // 原産国コードがマスタに存在しない場合、エラーにする
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_I_08));
        }
    }

    /**
     * 丸井品番の存在チェック.
     *
     * @param itemModel 更新対象の品番情報
     */
    private void checkMaruiGarmentNoExists(final ItemModel itemModel) {
        if (!junpcCodmstMaruiItemComponent.isExitsMaruiGarmentNo(itemModel.getBrandCode(),
                itemModel.getMaruiGarmentNo())) {
            // 丸井品番がマスタに存在しない場合、エラーにする
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_I_16));
        }
    }

    /**
     * 納品依頼承認済チェック.
     *
     * @param itemModel 更新対象の品番情報
     */
    private void checkIsDeliveryApproved(final ItemModel itemModel) {
        // DBから最新の承認済の納品依頼情報リストを取得する
        List<TDeliveryEntity> approvedDeliveryList = deliveryRepository
                .findMatchApproveStatusDeliverysByPartNoId(itemModel.getId(), ApprovalType.APPROVAL.getValue());

        // 承認済みの納品依頼情報がある場合、エラーにする
        if (!approvedDeliveryList.isEmpty()) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_I_14));
        }
    }

    /**
     * メール送信を行う.
     * @param itemId 品番ID
     * @param accountName アカウント名
     */
    private void sendMail(final BigInteger itemId, final String accountName) {

        // メール用モデル(PartRegistSendModel)にデータつめる
        final PartRegistSendModel partRegistSendModel = new PartRegistSendModel();
        final ExtendedTItemEntity extendedTItemEntity = exTItemRepository.findById(itemId).orElse(new ExtendedTItemEntity());
        // メーカー情報取得
        final ExtendedTOrderSupplierEntity exTOrderSupplierEntity =
                extendedTOrderSupplierRepository.findById(extendedTItemEntity.getCurrentProductOrderSupplierId()).orElse(new ExtendedTOrderSupplierEntity());

        BeanUtils.copyProperties(extendedTItemEntity, partRegistSendModel);

        // メーカーコード
        partRegistSendModel.setMdfMakerCode(exTOrderSupplierEntity.getSupplierCode());
        // メーカー名
        partRegistSendModel.setMdfMakerName(exTOrderSupplierEntity.getSupplierName());
        // メーカー担当者
        partRegistSendModel.setMdfMakerStaffId(exTOrderSupplierEntity.getSupplierStaffId());

        partRegistSendMailComponent.sendMail(partRegistSendModel, accountName);
    }
}
