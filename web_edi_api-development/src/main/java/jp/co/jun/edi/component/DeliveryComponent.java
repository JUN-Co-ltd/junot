package jp.co.jun.edi.component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.mail.DeliveryRequestRegistSendMailComponent;
import jp.co.jun.edi.component.model.SpecialtyQubeDeleteResponseXmlModel;
import jp.co.jun.edi.component.model.SpecialtyQubeRequestXmlModel;
import jp.co.jun.edi.entity.MJunmstEntity;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.entity.TDeliveryFileInfoEntity;
import jp.co.jun.edi.entity.TDeliverySkuEntity;
import jp.co.jun.edi.entity.TDeliveryStoreEntity;
import jp.co.jun.edi.entity.TDeliveryStoreSkuEntity;
import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.entity.constants.DefaultValueConstants;
import jp.co.jun.edi.entity.extended.ExtendedSendMailDeliveryAtEntity;
import jp.co.jun.edi.entity.extended.ExtendedTDeliveryEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliveryDetailModel;
import jp.co.jun.edi.model.DeliveryFileInfoModel;
import jp.co.jun.edi.model.DeliveryModel;
import jp.co.jun.edi.model.DeliverySearchConditionModel;
import jp.co.jun.edi.model.DeliverySkuModel;
import jp.co.jun.edi.model.DeliveryStoreModel;
import jp.co.jun.edi.model.DeliveryStoreSkuModel;
import jp.co.jun.edi.model.DeliveryVoucherFileInfoModel;
import jp.co.jun.edi.model.PurchaseModel;
import jp.co.jun.edi.model.ThresholdModel;
import jp.co.jun.edi.model.mail.DeliveryRequestRegistSendModel;
import jp.co.jun.edi.repository.DeliveryLotRepository;
import jp.co.jun.edi.repository.MJunmstRepository;
import jp.co.jun.edi.repository.TDeliveryDetailRepository;
import jp.co.jun.edi.repository.TDeliveryFileInfoRepository;
import jp.co.jun.edi.repository.TDeliveryRepository;
import jp.co.jun.edi.repository.TDeliverySkuRepository;
import jp.co.jun.edi.repository.TDeliveryStoreRepository;
import jp.co.jun.edi.repository.TDeliveryStoreSkuRepository;
import jp.co.jun.edi.repository.TDeliveryVoucherFileInfoRepository;
import jp.co.jun.edi.repository.extended.ExtendedSendMailDeliveryAtRepository;
import jp.co.jun.edi.repository.extended.ExtendedTDeliveryRepository;
import jp.co.jun.edi.repository.extended.ExtendedTSkuRepository;
import jp.co.jun.edi.repository.specification.TDeliverySpecification;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.type.AllocationType;
import jp.co.jun.edi.type.ApprovalType;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.CarryType;
import jp.co.jun.edi.type.CompleteType;
import jp.co.jun.edi.type.DivisionCodeType;
import jp.co.jun.edi.type.ExpenseItemType;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.LinkingStatusType;
import jp.co.jun.edi.type.MNumberColumnNameType;
import jp.co.jun.edi.type.MNumberTableNameType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.SaleType;
import jp.co.jun.edi.type.SendCodeType;
import jp.co.jun.edi.type.SpecialtyQubeDeleteStatusType;
import lombok.extern.slf4j.Slf4j;

/**
 * 納品依頼関連のコンポーネント.
 */
@Component
@Slf4j
public class DeliveryComponent extends GenericComponent {
    @Autowired
    private TDeliveryFileInfoRepository tDeliveryFileInfoRepository;

    @Autowired
    private TDeliveryVoucherFileInfoRepository deliveryVoucherFileInfoRepository;

    @Autowired
    private TDeliveryRepository deliveryRepository;

    @Autowired
    private TDeliverySkuRepository deliverySkuRepository;

    @Autowired
    private TDeliveryStoreRepository deliveryStoreRepository;

    @Autowired
    private TDeliveryStoreSkuRepository deliveryStoreSkuRepository;

    @Autowired
    private DeliveryLotRepository deliveryLotRepository;

    @Autowired
    private TDeliveryDetailRepository deliveryDetailRepository;

    @Autowired
    private ExtendedTDeliveryRepository extendedTDeliveryRepository;

    @Autowired
    private ExtendedSendMailDeliveryAtRepository extendedSendMailDeliveryAtRepository;

    @Autowired
    private ExtendedTSkuRepository extendedTSkuRepository;

    @Autowired
    private LoginUserComponent loginUserComponent;

    @Autowired
    private MNumberComponent numberComponent;

    @Autowired
    private OrderComponent orderComponent;

    @Autowired
    private DeliveryComponent deliveryComponent;

    @Autowired
    private DeliveryRequestRegistSendMailComponent deliveryRequestRegistSendMailComponent;

    @Autowired
    private ThresholdComponent thresholdComponent;

    @Autowired
    private TDeliverySpecification deliverySpec;

    @Autowired
    private SpecialtyQubeComponent sqComponent;

    @Autowired
    private MJunmstRepository mJunmstRepository;

    // PRD_0010 add SIT start
    @Autowired
    private ShipmentComponent shipmentComponent;
    // PRD_0010 add SIT end

    // PRD_0131 #10039 add JFE start
    @Autowired
    private MCodmstComponent mCodmstComponent;
    // PRD_0131 #10039 add JFE end

    /**
     * 納品IDをキーに納品情報を検索する.
     * @param deliveryId 納品ID
     * @return 納品情報
     */
    public DeliveryModel findDeliveryById(final BigInteger deliveryId) {
        // 納品情報を取得し、データが存在しない場合は例外を投げる
        final ExtendedTDeliveryEntity exDeliveryEntity = extendedTDeliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        final DeliveryModel deliveryModel = new DeliveryModel();
        BeanUtils.copyProperties(exDeliveryEntity, deliveryModel);

        // 納品依頼IDが紐づく納品伝票ファイル情報を取得する
        generateDeliveryVoucherFileInfos(deliveryModel, exDeliveryEntity);

        // 納品詳細を取得する
        generateDeliveryDetails(deliveryModel, exDeliveryEntity.getId());

        return deliveryModel;
    }

    /**
     * 納品IDをキーに納品伝票ファイル情報リストを取得して設定する.
     * ※納品依頼未承認の場合はファイル取得しない
     *
     * @param deliveryModel 納品依頼モデル
     * @param exDeliveryEntity 拡張納品依頼情報
     */
    private void generateDeliveryVoucherFileInfos(final DeliveryModel deliveryModel, final ExtendedTDeliveryEntity exDeliveryEntity) {
        if (!isDeliveryApproved(exDeliveryEntity.getDeliveryApproveStatus())) {
            // 納品依頼未承認の場合は納品伝票ファイルが作成されないため、何もしない
            return;
        }

        deliveryModel.setDeliveryVoucherFileInfos(
                deliveryVoucherFileInfoRepository.findByDeliveryIdAndExistsTfile(exDeliveryEntity.getId())
                .stream()
                .map(tDeliveryVoucherFileInfo -> {
                    final DeliveryVoucherFileInfoModel deliveryVoucherFileInfoModel = new DeliveryVoucherFileInfoModel();

                    // 納品伝票ファイル情報のコピー
                    BeanUtils.copyProperties(tDeliveryVoucherFileInfo, deliveryVoucherFileInfoModel);

                    return deliveryVoucherFileInfoModel;
                }).collect(Collectors.toList()));
    }

    /**
     * 納品IDをキーに納品明細情報を取得して設定する.
     * @param deliveryModel 納品依頼モデル
     * @param deliveryId 納品ID
     */
    private void generateDeliveryDetails(final DeliveryModel deliveryModel, final BigInteger deliveryId) {
        deliveryModel.setDeliveryDetails(deliveryDetailRepository.findByDeliveryId(
                deliveryId,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("divisionCode")))).stream()
                .map(tDeliveryDetail -> {
                    final DeliveryDetailModel deliveryDetailModel = new DeliveryDetailModel();

                    // 納品詳細情報のコピー
                    BeanUtils.copyProperties(tDeliveryDetail, deliveryDetailModel);
                    deliveryDetailModel.setAllocationCode(tDeliveryDetail.getLogisticsCode().substring(0, 1));

                    final BigInteger deliveryDetailId = tDeliveryDetail.getId();

                    // 納品SKU情報を取得する
                    generateDeliverySkus(deliveryDetailModel, deliveryDetailId);

                    // 納品得意先情報を取得する
                    generateDeliveryStores(deliveryDetailModel, deliveryDetailId);

                    return deliveryDetailModel;
                }).collect(Collectors.toList()));
    }

    /**
     * 納品明細IDをキーに納品SKU情報を取得して設定する.
     * @param deliveryDetailModel 納品明細モデル
     * @param deliveryDetailId 納品明細ID
     */
    private void generateDeliverySkus(final DeliveryDetailModel deliveryDetailModel, final BigInteger deliveryDetailId) {
        deliveryDetailModel.setDeliverySkus(deliverySkuRepository.findByDeliveryDetailId(
                deliveryDetailId,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("colorCode")))).stream()
                .map(tDeliverySku -> {
                    final DeliverySkuModel deliverySkuModel = new DeliverySkuModel();

                    // 納品SKU情報のコピー
                    BeanUtils.copyProperties(tDeliverySku, deliverySkuModel);

                    return deliverySkuModel;
                }).collect(Collectors.toList()));
    }

    /**
     * 納品明細IDをキーに納品得意先情報を取得して設定する.
     * @param deliveryDetailModel 納品明細モデル
     * @param deliveryDetailId 納品明細ID
     */
    private void generateDeliveryStores(final DeliveryDetailModel deliveryDetailModel, final BigInteger deliveryDetailId) {
        deliveryDetailModel.setDeliveryStores(deliveryStoreRepository.findByDeliveryDetailId(
                deliveryDetailId,
                PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .map(tDeliveryStore -> {
                    final DeliveryStoreModel deliveryStoreModel = new DeliveryStoreModel();

                    // 納品得意先情報のコピー
                    BeanUtils.copyProperties(tDeliveryStore, deliveryStoreModel);

                    // 納品得意先SKU情報を取得する
                    generateDeliveryStoreSkus(deliveryStoreModel, deliveryStoreModel.getId());

                    return deliveryStoreModel;
                }).collect(Collectors.toList()));
    }

    /**
     * 納品得意先IDをキーに納品得意先SKU情報を取得して設定する.
     * @param deliveryStoreModel 納品得意先モデル
     * @param deliveryStoreId 納品得意先ID
     */
    private void generateDeliveryStoreSkus(final DeliveryStoreModel deliveryStoreModel, final BigInteger deliveryStoreId) {
        deliveryStoreModel.setDeliveryStoreSkus(deliveryStoreSkuRepository.findByDeliveryStoreId(
                deliveryStoreId,
                PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .map(tDeliveryStoreSku -> {
                    final DeliveryStoreSkuModel deliveryStoreSkuModel = new DeliveryStoreSkuModel();

                    // 納品得意先情報のコピー
                    BeanUtils.copyProperties(tDeliveryStoreSku, deliveryStoreSkuModel);
                    return deliveryStoreSkuModel;
                }).collect(Collectors.toList()));
    }

    /**
     * 検索条件に一致した納品依頼情報リストを取得する.
     * @param searchCondition 検索条件
     * @param offset offset
     * @param limit limit
     * @return 納品依頼情報リスト
     */
    public List<DeliveryModel> findDeliveriesBySpec(final DeliverySearchConditionModel searchCondition, final Integer offset, final Integer limit) {
        final List<DeliveryModel> deliveryModelList = new ArrayList<>();

        // 指定した発注IDに紐づく納品依頼ファイル情報を取得
        final List<TDeliveryFileInfoEntity> deliveryFileInfoList = findDeliveryFileInfoListByOrderId(searchCondition.getOrderId());

        // TDeliverySpecificationを利用して動的に条件文を生成し、データ取得する。
        for (final TDeliveryEntity deliveryEntity : deliveryRepository.findAll(Specification
                .where(deliverySpec.notDeleteContains())
                .and(deliverySpec.orderIdContains(searchCondition.getOrderId()))
                .and(deliverySpec.orderNumberContains(searchCondition.getOrderNumber()))
                .and(deliverySpec.deliveryIdContains(searchCondition.getDeliveryId()))
                .and(deliverySpec.partNoContains(searchCondition.getPartNo())),
                PageRequest.of(offset, limit, Sort.by(Order.asc("id"))))) {

            final DeliveryModel deliveryModel = new DeliveryModel();
            BeanUtils.copyProperties(deliveryEntity, deliveryModel);

            // 納品明細情報を取得する
            deliveryModel.setDeliveryDetails(deliveryDetailRepository.findByDeliveryId(
                    deliveryEntity.getId(),
                    PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("divisionCode")))).stream()
                    .map(tDeliveryDetail -> {
                        final DeliveryDetailModel deliveryDetail = new DeliveryDetailModel();

                        // 納品詳細情報のコピー
                        BeanUtils.copyProperties(tDeliveryDetail, deliveryDetail);

                        // 納品依頼回数が紐づく納品依頼ファイル情報を取得
                        deliveryDetail.setDeliveryFileInfo(
                                deliveryComponent.findFileInfoByDeliveryCount(
                                        deliveryFileInfoList, tDeliveryDetail.getDeliveryCount()));

                        // 納品SKU情報を取得する
                        deliveryDetail.setDeliverySkus(deliverySkuRepository.findByDeliveryDetailId(
                                tDeliveryDetail.getId(),
                                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("colorCode")))).stream()
                                .map(tDeliverySku -> {
                                    final DeliverySkuModel deliverySku = new DeliverySkuModel();

                                    // 納品SKU情報のコピー
                                    BeanUtils.copyProperties(tDeliverySku, deliverySku);

                                    return deliverySku;
                                }).collect(Collectors.toList()));

                        return deliveryDetail;
                    }).collect(Collectors.toList()));

            // レスポンスに返却する
            deliveryModelList.add(deliveryModel);
        }

        if (searchCondition.isIdSortDesc()) {
            Collections.sort(deliveryModelList, Comparator.comparing(DeliveryModel::getId).reversed());
        }
        return deliveryModelList;
    }

    /**
     * 納品依頼承認済チェック.
     *
     * 承認ステータスが
     * 承認済(1)の場合、納品依頼承認済
     *
     * @param deliveryApproveStatus 納品依頼承認ステータス
     * @return true : 承認済, false : 未承認
     */
    // 本来はApprovalType型を引数にすべきですが既に多くの箇所でString前提で作られているのでこのままにしてください
    public boolean isDeliveryApproved(final String deliveryApproveStatus) {
        return ApprovalType.APPROVAL.getValue().equals(deliveryApproveStatus);
    }

    /**
     * 共通のバリデーションチェックを行う.
     * @param deliveryModel 納品情報(入力値)
     * @param extendedTItemEntity DBから取得した品番情報
     * @param extendedTOrderEntity DBから取得した発注情報
     * @return ResultMessages
     */
    public ResultMessages checkCommonValidate(
            final DeliveryModel deliveryModel,
            final ExtendedTItemEntity extendedTItemEntity,
            final ExtendedTOrderEntity extendedTOrderEntity) {
        final ResultMessages rsltMsg = ResultMessages.warning();

        final BigDecimal prodUnitPrice = deliveryModel.getNonConformingProductUnitPrice();

        // B級品区分チェック時のB級品単価存在チェック
        if (deliveryModel.isNonConformingProductType() && prodUnitPrice == null) {
            // 区分チェック時に単価が未入力の場合、エラーにする
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_D_006));
        }

        // B級品単価正数チェック
        if (prodUnitPrice != null && prodUnitPrice.compareTo(new BigDecimal(0)) != 1) {
            // B級品単価が0以下の場合、エラーにする
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_D_007));
        }

        // 発注数量の閾値を超えた場合、エラー
        if (isThresholdRateOver(deliveryModel, extendedTItemEntity, extendedTOrderEntity)) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_D_023));
        }

        // 数量必須入力チェック
        if (!isExistDeliveryDetails(deliveryModel.getDeliveryDetails())) {
            rsltMsg.add(MessageCodeType.CODE_D_011);
        }

//        // PRD_0131 #10039 add JFE start
//        // 物流コードチェック（店舗配分時）
//        final List<DeliveryDetailModel> deliveryDetails = deliveryModel.getDeliveryDetails();
//        if(deliveryModel.getFromStoreScreenFlg() == BooleanType.TRUE) {
//        	for (DeliveryDetailModel Detail : deliveryDetails) {
//        		String divisionCode =  Detail.getDivisionCode();//課コード
//        		String brandCode =   deliveryModel.getPartNo().substring(0, 2);//ブランドコード
//        		String codmstLogisticsCode = mCodmstComponent.getLogisticsCode(divisionCode,brandCode);//コードマスタ上の物流コード
//                //テーブルID25、ブランドコード、課コード、メンテフラグ('',1,2)、で物流コード(item3)を取る。
//        		if(!(Detail.getLogisticsCode().equals( codmstLogisticsCode))) {
//        			//取得出来なければリクエスト側と比較をする。一致しない場合はエラー。
//        			rsltMsg.add(MessageCodeType.CODE_D_031);
//        			log.info(LogStringUtil.of("checkCommonValidate")
//        	                .message("登録する物流コードがコードマスタの物流コードと一致しません。.")
//        	                .value("entryLogisticsCode", Detail.getLogisticsCode())
//        	                .value("RegistedLogisticsCode", codmstLogisticsCode)
//        	                .build());
//        			break;
//        		}
//        	}
//     	}
//		// PRD_0131 #10039 add JFE end
        return rsltMsg;
    }

    /**
     * 納品情報リストを検索する.
     * @param deliverySearchConditionModel 納品情報検索モデル
     * @return 納品情報リスト
     */
    public List<DeliveryModel> findDeliveries(final DeliverySearchConditionModel deliverySearchConditionModel) {
        final List<DeliveryModel> deliveryModelList = new ArrayList<>();

        // 検索用Limit、Offset
        final Integer limit = 100;
        final Integer offset = 0;

        final BigInteger orderId = deliverySearchConditionModel.getOrderId();

        // 指定した発注IDに紐づく納品依頼ファイル情報を取得
        final List<TDeliveryFileInfoEntity> deliveryFileInfoList = findDeliveryFileInfoListByOrderId(orderId);

        // TDeliverySpecificationを利用して動的に条件文を生成し、データ取得する。
        for (final TDeliveryEntity deliveryEntity : deliveryRepository.findAll(Specification
                .where(deliverySpec.notDeleteContains())
                .and(deliverySpec.orderIdContains(orderId))
                .and(deliverySpec.orderNumberContains(deliverySearchConditionModel.getOrderNumber()))
                .and(deliverySpec.deliveryIdContains(deliverySearchConditionModel.getDeliveryId()))
                .and(deliverySpec.partNoContains(deliverySearchConditionModel.getPartNo())),
                PageRequest.of(offset, limit, Sort.by(Order.asc("id"))))) {

            final DeliveryModel deliveryModel = new DeliveryModel();
            BeanUtils.copyProperties(deliveryEntity, deliveryModel);

            // 納品明細情報を取得する
            deliveryModel.setDeliveryDetails(deliveryDetailRepository.findByDeliveryId(
                    deliveryEntity.getId(),
                    PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("divisionCode")))).stream()
                    .map(tDeliveryDetail -> {
                        final DeliveryDetailModel deliveryDetail = new DeliveryDetailModel();

                        // 納品詳細情報のコピー
                        BeanUtils.copyProperties(tDeliveryDetail, deliveryDetail);

                        // 納品依頼回数が紐づく納品依頼ファイル情報を取得
                        deliveryDetail.setDeliveryFileInfo(findFileInfoByDeliveryCount(
                                        deliveryFileInfoList, tDeliveryDetail.getDeliveryCount()));

                        // 納品SKU情報を取得する
                        deliveryDetail.setDeliverySkus(deliverySkuRepository.findByDeliveryDetailId(
                                tDeliveryDetail.getId(),
                                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("colorCode")))).stream()
                                .map(tDeliverySku -> {
                                    final DeliverySkuModel deliverySku = new DeliverySkuModel();

                                    // 納品SKU情報のコピー
                                    BeanUtils.copyProperties(tDeliverySku, deliverySku);

                                    return deliverySku;
                                }).collect(Collectors.toList()));

                        return deliveryDetail;
                    }).collect(Collectors.toList()));

            // レスポンスに返却する
            deliveryModelList.add(deliveryModel);
        }

        if (deliverySearchConditionModel.isIdSortDesc()) {
            Collections.sort(deliveryModelList, Comparator.comparing(DeliveryModel::getId).reversed());
        }

        return deliveryModelList;
    }

    /**
     * 閾値超過チェック.
     * @param deliveryData 納品情報(入力値)
     * @param extendedTItemEntity DBから取得した品番情報
     * @param extendedTOrderEntity DBから取得した発注情報
     * @return true:納品数量が閾値比率超過
     */
    private boolean isThresholdRateOver(
            final DeliveryModel deliveryData,
            final ExtendedTItemEntity extendedTItemEntity,
            final ExtendedTOrderEntity extendedTOrderEntity) {
        // 閾値取得
        final List<ThresholdModel> thresholds = thresholdComponent.listThreshold(
                extendedTItemEntity.getBrandCode(),
                extendedTItemEntity.getItemCode());

        // 発注数
        final BigDecimal quantity = extendedTOrderEntity.getQuantity();

        // 過去納品数量合計取得
        final int historyLot = deliveryLotRepository.sumDeliveryLotByOrderId(deliveryData.getOrderId(), deliveryData.getId());

        // 今回の数量入力合計
        final int inputSum = extractInputLotSum(deliveryData);

        // 返品数合計
        final int totalReturnLot = orderComponent.getTOrderSkus(deliveryData.getOrderId()).stream()
                .mapToInt(sku -> sku.getReturnLot())
                .sum();

        return new BigDecimal(historyLot + inputSum - totalReturnLot)
                .compareTo((quantity.multiply(thresholds.get(0).getThreshold())).add(quantity)) == 1;
    }

    /**
     * 今回の数量入力合計.
     * @param deliveryData リクエストパラメータ
     * @return 数量入力合計
     */
    private int extractInputLotSum(final DeliveryModel deliveryData) {
        // 店舗配分の場合は店舗
        if (BooleanType.TRUE == deliveryData.getFromStoreScreenFlg()) {
            return deliveryData.getDeliveryDetails().stream()
                    .flatMap(dd -> dd.getDeliveryStores().stream())
                    .flatMap(ds -> ds.getDeliveryStoreSkus().stream())
                    .mapToInt(ds -> ds.getDeliveryLot())
                    .sum();
        }

        // 課別配分の場合は課別
        return deliveryData.getDeliveryDetails().stream()
                .flatMap(dd -> dd.getDeliverySkus().stream())
                .mapToInt(ds -> ds.getDeliveryLot())
                .sum();
    }

    /**
     * 指定した発注IDに紐づく納品依頼ファイル情報を取得する.
     * ただしfile_no_idがt_fileのidと紐付かないレコードは取得しない.
     * 各回数毎に納品依頼回数の最新の納品依頼ファイル情報を取得するため.
     * 回数の昇順、IDの降順で取得する.
     * @param orderId 発注ID
     * @return List<TDeliveryFileInfoEntity>
     */
    public List<TDeliveryFileInfoEntity> findDeliveryFileInfoListByOrderId(final BigInteger orderId) {
         return tDeliveryFileInfoRepository.findByOrderIdAndExistsTfile(
                 orderId, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("deliveryCount"), Order.desc("id"))));
    }

    /**
     * 納品依頼回数が紐づく納品依頼ファイル情報を取得する.
     * @param deliveryFileInfoList 納品依頼ファイル情報リスト
     * @param deliveryCount 納品依頼回数
     * @return DeliveryFileInfoModel 紐付かない場合はnull
     */
    public DeliveryFileInfoModel findFileInfoByDeliveryCount(final List<TDeliveryFileInfoEntity> deliveryFileInfoList, final int deliveryCount) {
        // 納品依頼回数が合致した納品依頼ファイル情報を取得
        Optional<TDeliveryFileInfoEntity> optional = deliveryFileInfoList.stream()
                .filter(deliveryFileInfoEntity -> deliveryFileInfoEntity.getDeliveryCount() == deliveryCount).findFirst();
        if (!optional.isPresent()) {
            return null;    // 紐付かない場合はnull
        }

        // 納品依頼ファイル情報のコピー
        final DeliveryFileInfoModel deliveryFileInfoModel = new DeliveryFileInfoModel();
        BeanUtils.copyProperties(optional.get(), deliveryFileInfoModel);
        return deliveryFileInfoModel;
    }

    /**
     * 登録・更新用にソートする.
     * 納品明細リストを
     * 1.縫製検品・本社撮影を先頭、その他は場所コード昇順、
     * 2.キャリー区分昇順.
     * @param deliveryModel DeliveryModel
     * @return ソート済のList<DeliveryDetailModel>
     */
    public List<DeliveryDetailModel> sortDeliveryDetailForUpsert(final DeliveryModel deliveryModel) {
        final List<DeliveryDetailModel> deliveryDetails = deliveryModel.getDeliveryDetails();

        final List<DeliveryDetailModel> sortedDeliveryDetails = new ArrayList<>();
        final List<DeliveryDetailModel> sewingList = deliveryDetails.stream()
                .filter(dd ->  Objects.equals(dd.getDivisionCode(), DivisionCodeType.SEWING.getValue())).collect(Collectors.toList());
        Collections.sort(sewingList, Comparator.comparing(DeliveryDetailModel::getCarryType));
        sortedDeliveryDetails.addAll(sewingList);

        final List<DeliveryDetailModel> photoList = deliveryDetails.stream()
                .filter(dd ->  Objects.equals(dd.getDivisionCode(), DivisionCodeType.PHOTO.getValue())).collect(Collectors.toList());
        Collections.sort(photoList, Comparator.comparing(DeliveryDetailModel::getCarryType));
        sortedDeliveryDetails.addAll(photoList);

        final List<DeliveryDetailModel> productList = deliveryDetails.stream()
                .filter(dd -> !Objects.equals(dd.getDivisionCode(), DivisionCodeType.SEWING.getValue())
                           && !Objects.equals(dd.getDivisionCode(), DivisionCodeType.PHOTO.getValue())).collect(Collectors.toList());
        Collections.sort(productList, Comparator.comparing(DeliveryDetailModel::getAllocationCode).thenComparing(DeliveryDetailModel::getCarryType));
        sortedDeliveryDetails.addAll(productList);

        return sortedDeliveryDetails;
    }

    /**
     * 納品依頼回数をカウントするかの判断.
     * 縫製検品、本社撮影、または直前ループのレコードと場所コードが不一致の場合、
     * 納品依頼回数をカウントするとしてtrueを返却.
     * 縫製検品、本社撮影、または直前ループのレコードと場所コードが一致の場合、
     * キャリー区分が不一致の場合、納品依頼回数をカウントするとしてtrueを返却.
     *
     * @param previousAllocationCode 前回ループで処理した納品明細情報の場所コード
     * @param previousCarryType 前回ループで処理したキャリー区分
     * @param deliveryDetailModel 入力された納品依頼明細
     * @return true：納品依頼回数をカウントする、false：納品依頼回数カウントしない
     */
    public boolean isCountDelivery(final String previousAllocationCode, final CarryType previousCarryType, final DeliveryDetailModel deliveryDetailModel) {
        final String divisionCode = deliveryDetailModel.getDivisionCode();
        final boolean notMatch = Objects.equals(divisionCode, DivisionCodeType.SEWING.getValue())
                || Objects.equals(divisionCode, DivisionCodeType.PHOTO.getValue())
                || !Objects.equals(previousAllocationCode, deliveryDetailModel.getAllocationCode());
        if (notMatch) {
            return true;
        } else {
            return !previousCarryType.equals(deliveryDetailModel.getCarryType());
        }
    }

    /**
     * 納品明細リストの入力ありなしチェック.
     *
     * 以下場合は納品明細入力なしと判断し、falseを返却
     * ・納品依頼明細が入力されてない
     * ・全ての納品依頼SKUが入力されてない
     * ・納品数量合計が0
     *
     * @param deliveryDetails 画面で入力された納品明細情報リスト
     * @return true：納品明細が入力されている,
     *          false：納品明細が入力されていない
     */
    public boolean isExistDeliveryDetails(final List<DeliveryDetailModel> deliveryDetails) {
        if (Objects.isNull(deliveryDetails) || deliveryDetails.isEmpty()
                || !isDeliverySkusEntry(deliveryDetails)
                || !isSumDeliveryLotLargeThanZero(deliveryDetails)) {
            return false;
        }
        return true;
    }

    /**
     * 納品依頼SKUが入力されているかチェックする.
     *
     * @param deliveryDetails 画面で入力された納品明細情報リスト
     *
     * @return true：納品依頼SKUが入力されている,
     *          false：納品依頼SKUが入力されていない
     */
    private boolean isDeliverySkusEntry(final List<DeliveryDetailModel> deliveryDetails) {
        return deliveryDetails.stream()
                .anyMatch(deliveryDetailModel -> !deliveryDetailModel.getDeliverySkus().isEmpty());
    }

    /**
     * 入力された納品依頼SKUの 納品依頼数合計 > 0 かチェックする.
     *
     * @param deliveryDetails 画面で入力された納品明細情報リスト
     *
     * @return true：納品依頼数合計 > 0,
     *          false：納品依頼数合計 <= 0
     */
    private boolean isSumDeliveryLotLargeThanZero(final List<DeliveryDetailModel> deliveryDetails) {
        // 納品依頼数合計
        int sumDeliveryLot = 0;

        for (DeliveryDetailModel deliveryDetailModel : deliveryDetails) {
            sumDeliveryLot = sumDeliveryLot + deliveryDetailModel.getDeliverySkus().stream()
            .mapToInt(deliverySkuModel -> deliverySkuModel.getDeliveryLot())
            .sum();
        }

        return sumDeliveryLot > 0;
    }

    /**
     * 品番情報と発注情報の値を設定.
     *
     * @param tDelivery 納品依頼情報
     * @param tItem 品番情報
     * @param tOrder 発注情報
     */
    public void setValueForExtendedTItemAndExtendedTOrder(final TDeliveryEntity tDelivery,
            final ExtendedTItemEntity tItem, final ExtendedTOrderEntity tOrder) {
        // 品番IDを設定
        tDelivery.setPartNoId(tItem.getId());
        // 品番を設定
        tDelivery.setPartNo(tItem.getPartNo());
        // 発注IDを設定
        tDelivery.setOrderId(tOrder.getId());
        // 発注Noを設定
        tDelivery.setOrderNumber(tOrder.getOrderNumber());
    }

    /**
     * INSERT用の納品依頼情報の値再セット.
     *
     * @param extendedTItemEntity DB最新の品番情報
     * @param extendedTOrderEntity DB最新の発注情報
     * @param deliveryModel 画面から入力された納品依頼情報
     *
     * @return 画面の情報とAPI側で算出する情報をセットした納品依頼情報Entity
     */
    public TDeliveryEntity setValueForDeliveryInsert(final ExtendedTItemEntity extendedTItemEntity, final ExtendedTOrderEntity extendedTOrderEntity,
            final DeliveryModel deliveryModel) {
        // 登録用の納品依頼情報Entity
        final TDeliveryEntity deliveryEntity = new TDeliveryEntity();

        /** 画面側で設定される項目 */
        // 配分率区分
        deliveryEntity.setDistributionRatioType(deliveryModel.getDistributionRatioType());
        // メモ
        deliveryEntity.setMemo(deliveryModel.getMemo());
        // B級品区分
        deliveryEntity.setNonConformingProductType(deliveryModel.isNonConformingProductType());
        // B級品単価
        deliveryEntity.setNonConformingProductUnitPrice(deliveryModel.getNonConformingProductUnitPrice());
        // 最終納品ステータス
        deliveryEntity.setLastDeliveryStatus(deliveryModel.getLastDeliveryStatus());

        /** APIで設定する項目 */
        // (最新の)品番ID、品番、発注ID、発注No
        setValueForExtendedTItemAndExtendedTOrder(deliveryEntity, extendedTItemEntity, extendedTOrderEntity);
        // 納品依頼回数
        deliveryEntity.setDeliveryCount(DefaultValueConstants.DEFAULT_INT_ZERO);
        // 承認ステータス
        deliveryEntity.setDeliveryApproveStatus(ApprovalType.UNAPPROVED.getValue());

        return deliveryEntity;
    }

    /**
     * 納期変更理由IDと詳細の値を設定.
     *
     * 納期 > 発注時納期(発注の製品修正納期)の場合
     * 画面から入力された納期変更理由IDと詳細をセット
     * それ以外はNULLをセット
     *
     * @param registDeliveryEntity 値を設定する納品依頼情報
     * @param extendedTOrderEntity DB最新の発注情報
     * @param deliveryModel 画面から入力された納品依頼情報
     * @param deliveryDetailModel 画面から入力された納品明細情報
     */
    public void generateDeliveryDateChangeReasonIdAndDetail(final TDeliveryEntity registDeliveryEntity, final ExtendedTOrderEntity extendedTOrderEntity,
            final DeliveryModel deliveryModel, final DeliveryDetailModel deliveryDetailModel) {
        // 納期 > 発注時納期の場合、納期変更理由IDと詳細の値をセット
        if (deliveryDetailModel.getCorrectionAt().after(extendedTOrderEntity.getProductCorrectionDeliveryAt())) {
            registDeliveryEntity.setDeliveryDateChangeReasonId(deliveryModel.getDeliveryDateChangeReasonId());
            registDeliveryEntity.setDeliveryDateChangeReasonDetail(deliveryModel.getDeliveryDateChangeReasonDetail());
        } else {
            // 上記以外はNULLをセット
            registDeliveryEntity.setDeliveryDateChangeReasonId(null);
            registDeliveryEntity.setDeliveryDateChangeReasonDetail(null);
        }
    }

    /**
     * INSERT用の納品明細情報を作成.
     *
     * @param loginUser ログインユーザ情報
     * @param deliveryDetailModel 画面から入力された納品依頼情報
     * @param deliveryId 納品依頼ID
     * @param isFromStoreScreen 得意先配分画面からフラグ
     * @return 画面の情報とAPI側で算出する情報をセットした納品依頼明細Entity
     */
    // PRD_0044 mod SIT start
    //public TDeliveryDetailEntity generateDeliveryDetailForInsert(final CustomLoginUser loginUser, final DeliveryDetailModel deliveryDetailModel,
    //        final BigInteger deliveryId, final boolean isFromStoreScreen) {
    public TDeliveryDetailEntity generateDeliveryDetailForInsert(final CustomLoginUser loginUser, final DeliveryDetailModel deliveryDetailModel,
            final BigInteger deliveryId, final boolean isFromStoreScreen, final boolean isStoreScreenSaveCorrect) {
    // PRD_0044 mod SIT end
        // INSERT用の納品依頼明細情報Entity
        final TDeliveryDetailEntity deliveryDetailEntity = new TDeliveryDetailEntity();

        /** 画面側で設定される項目 */
        // 納品依頼日
        deliveryDetailEntity.setDeliveryRequestAt(deliveryDetailModel.getDeliveryRequestAt());
        // 課コード
        deliveryDetailEntity.setDivisionCode(deliveryDetailModel.getDivisionCode());
        // 物流コード
        deliveryDetailEntity.setLogisticsCode(deliveryDetailModel.getLogisticsCode());
        // 修正納期
        deliveryDetailEntity.setCorrectionAt(deliveryDetailModel.getCorrectionAt());
        // 配分率ID
        deliveryDetailEntity.setDistributionRatioId(deliveryDetailModel.getDistributionRatioId());
        // ファックス送信フラグ
        deliveryDetailEntity.setFaxSend(deliveryDetailModel.getFaxSend());
        // キャリー区分
        deliveryDetailEntity.setCarryType(deliveryDetailModel.getCarryType());
        // 店舗配分画面からの登録・更新(一時保存以外)の場合は店舗別登録済フラグにtrueをセット
        // PRD_0044 mod SIT start
        //if (isFromStoreScreen) {
        if (isFromStoreScreen && !isStoreScreenSaveCorrect) {
        // PRD_0044 mod SIT end
            deliveryDetailEntity.setStoreRegisteredFlg(BooleanType.TRUE);
        }

        /** APIで計算する項目 */
        // 納品ID
        deliveryDetailEntity.setDeliveryId(deliveryId);
        // 納品No
        deliveryDetailEntity.setDeliveryNumber(DefaultValueConstants.DEFAULT_DELIVERY_NUMBER);
        // 納品依頼回数
        deliveryDetailEntity.setDeliveryCount(DefaultValueConstants.DEFAULT_INT_ZERO);
        // 配分区分
        deliveryDetailEntity.setAllocationType(AllocationType.NORMAL);
        // セール対象品区分
        deliveryDetailEntity.setSaleType(SaleType.NORMAL);
        // 納品依頼No
        deliveryDetailEntity.setDeliveryRequestNumber(DefaultValueConstants.DEFAULT_DELIVERY_NUMBER);
        // 納期(修正納期をセット)
        deliveryDetailEntity.setDeliveryAt(deliveryDetailModel.getCorrectionAt());
        // 配分完納フラグ
        deliveryDetailEntity.setAllocationCompletePaymentFlg(BooleanType.FALSE);
        // 配分確定フラグ
        deliveryDetailEntity.setAllocationConfirmFlg(BooleanType.FALSE);
        // 入荷フラグ
        deliveryDetailEntity.setArrivalFlg(BooleanType.FALSE);
        // ピッキングフラグ
        deliveryDetailEntity.setPickingFlg(BooleanType.FALSE);
        // 配分完了フラグ
        deliveryDetailEntity.setAllocationCompleteFlg(BooleanType.FALSE);
        // 出荷指示済フラグ
        deliveryDetailEntity.setShippingInstructionsFlg(BooleanType.FALSE);
        // 出荷停止区分
        deliveryDetailEntity.setShippingStoped(BooleanType.FALSE);
        // 納品依頼書発行フラグ
        deliveryDetailEntity.setDeliverySheetOut(BooleanType.FALSE);
        // 連携入力者
        deliveryDetailEntity.setJunpcTanto(loginUserComponent.getAccountNameWithAffiliation(loginUser));
        // 連携ステータス
        deliveryDetailEntity.setLinkingStatus(LinkingStatusType.TARGET);

        return deliveryDetailEntity;
    }

    /**
     * INSERT用の納品SKU情報を作成する.
     *
     * @param registeredDeliveryDetailEntity リクエストパラメータの納品IDに紐づく納品明細情報
     * @param deliverySkuModel 画面から入力された納品SKU情報
     * @return deliverySkuEntity 画面の情報とAPI側で算出する情報をセットした納品依頼SKUEntity
     */
    public TDeliverySkuEntity generateDeliverySkuForInsert(final TDeliveryDetailEntity registeredDeliveryDetailEntity,
            final DeliverySkuModel deliverySkuModel) {
        // INSERT用の納品依頼SKU情報Entity
        final TDeliverySkuEntity deliverySkuEntity = new TDeliverySkuEntity();

        /* 画面側で設定される項目 */
        // 課コード
        deliverySkuEntity.setDivisionCode(deliverySkuModel.getDivisionCode());
        // サイズ
        deliverySkuEntity.setSize(deliverySkuModel.getSize());
        // 色
        deliverySkuEntity.setColorCode(deliverySkuModel.getColorCode());
        // 納品数量
        deliverySkuEntity.setDeliveryLot(deliverySkuModel.getDeliveryLot());

        /* APIで計算する項目 */
        // 納品明細ID
        deliverySkuEntity.setDeliveryDetailId(registeredDeliveryDetailEntity.getId());
        // 納品依頼No
        deliverySkuEntity.setDeliveryRequestNumber(registeredDeliveryDetailEntity.getDeliveryRequestNumber());
        // 入荷数量
        deliverySkuEntity.setArrivalLot(DefaultValueConstants.DEFAULT_INT_ZERO);

        return deliverySkuEntity;
    }

    /**
     * 納品依頼及びその子孫のテーブルの登録(INSERT)処理.
     *
     * @param registeredDbTItemEntity 現時点でDBに登録されている品番情報
     * @param registeredDbTOrderEntity 現時点でDBに登録されている発注情報
     * @param loginUser ログインユーザ情報
     * @param requestDeliveryModel リクエストパラメータの納品依頼情報
     * @param resultDeliveries 今回登録した納品依頼のリスト
     */
    public void insertIntoDeliveryAndChildRelationship(final ExtendedTItemEntity registeredDbTItemEntity,
            final ExtendedTOrderEntity registeredDbTOrderEntity, final CustomLoginUser loginUser,
            final DeliveryModel requestDeliveryModel, final List<TDeliveryEntity> resultDeliveries) {

        // 納品得意先の入力がない場合、設定する.
        final List<DeliveryDetailModel> deliveryDetailModel = requestDeliveryModel.getDeliveryDetails();
        final List<DeliveryStoreModel> deliveryStores = deliveryDetailModel.get(0).getDeliveryStores();
        if (Objects.isNull(deliveryStores) || deliveryStores.size() == 0) {
            this.generateDefaultStoreRecord(registeredDbTItemEntity, deliveryDetailModel);
        }

        final List<DeliveryDetailModel> deliveryDetails = sortDeliveryDetailForUpsert(requestDeliveryModel);

        String previousAllocationCode = ""; // 前回ループで処理した納品明細情報の場所コード
        CarryType previousCarryType = null; // 前回ループで処理した納品明細情報のキャリー区分
        TDeliveryEntity registDeliveryEntity = new TDeliveryEntity();   // 登録する納品依頼

        for (final DeliveryDetailModel deliveryDetail : deliveryDetails) {
            // 縫製検品、本社撮影、または直前ループのレコードと場所コードが不一致の場合、納品依頼を登録する
            // 一致の場合はキャリー区分が不一致であれば、納品依頼を登録する
            if (isCountDelivery(previousAllocationCode, previousCarryType, deliveryDetail)) {
                // 納品依頼情報の値をセットしDBに登録
                registDeliveryEntity = setValueForDeliveryInsert(registeredDbTItemEntity, registeredDbTOrderEntity, requestDeliveryModel);
                // 納期変更理由IDと詳細のセット
                generateDeliveryDateChangeReasonIdAndDetail(registDeliveryEntity, registeredDbTOrderEntity, requestDeliveryModel, deliveryDetail);
                deliveryRepository.save(registDeliveryEntity);

                // 登録した納品依頼をregistedDeliveryListに追加
                resultDeliveries.add(registDeliveryEntity);

                // ループで使用する場所コードをセット(製品のみ)
                final String divisionCode = deliveryDetail.getDivisionCode();
                if (!Objects.equals(divisionCode, DivisionCodeType.SEWING.getValue())
                        && !Objects.equals(divisionCode, DivisionCodeType.PHOTO.getValue())) {
                    previousAllocationCode = deliveryDetail.getAllocationCode();
                }
                previousCarryType = deliveryDetail.getCarryType();
            }

            // 納品依頼明細の値をセットしDBに登録
            final boolean isFromStoreScreen = BooleanType.TRUE.equals(requestDeliveryModel.getFromStoreScreenFlg());
            // PRD_0044 mod SIT start
            //final TDeliveryDetailEntity registDeliveryDetailEntity = generateDeliveryDetailForInsert(loginUser,
            //        deliveryDetail, registDeliveryEntity.getId(), isFromStoreScreen);
            final boolean isStoreScreenSaveCorrect = BooleanType.TRUE.equals(requestDeliveryModel.getStoreScreenSaveCorrectFlg());
            final TDeliveryDetailEntity registDeliveryDetailEntity = generateDeliveryDetailForInsert(loginUser,
                    deliveryDetail, registDeliveryEntity.getId(), isFromStoreScreen, isStoreScreenSaveCorrect);
            // PRD_0044 mod SIT end
            deliveryDetailRepository.save(registDeliveryDetailEntity);

            // 納品SKUの登録
            insertDeliverySku(deliveryDetail.getDeliverySkus(), registDeliveryDetailEntity);

            // 納品得意先の登録
            insertDeliveryStore(deliveryDetail.getDeliveryStores(), registDeliveryDetailEntity);
        }
    }

    /**
     * デフォルトの納品得意先情報を作成する.
     * @param registeredDbTItemEntity 品番情報
     * @param deliveryDetails 画面で入力した納品明細情報リスト
     */
    private void generateDefaultStoreRecord(final ExtendedTItemEntity registeredDbTItemEntity, final List<DeliveryDetailModel> deliveryDetails) {

        final List<String> divisionCodeList = deliveryDetails.stream()
                .map(deliveryDetailModel -> deliveryDetailModel.getDivisionCode())
                .collect(Collectors.toList());

        // 課ごとに配分順が一番少ない店舗コードを1件ずつ取得
        final List<MJunmstEntity> junmstList = mJunmstRepository
                .findMinHjunByBrandAndDivisionCodeListGroupByHka(registeredDbTItemEntity.getBrandCode(), divisionCodeList);

        // 課ごとに得意先設定
        deliveryDetails.stream().forEach(deliveryDetailModel -> setDeliveryStores(junmstList, deliveryDetailModel));
    }

    /**
     * 納品明細モデルにデフォルトの得意先情報を設定する.
     * @param junmstList 配分順マスタリスト
     * @param deliveryDetailModel 納品明細モデル
     */
    private void setDeliveryStores(final List<MJunmstEntity> junmstList, final DeliveryDetailModel deliveryDetailModel) {
        final String divisionCode = deliveryDetailModel.getDivisionCode();
        final MJunmstEntity matchJunmst = junmstList.stream()
                .filter(junmst -> divisionCode.equals(junmst.getHka()))
                .findFirst().get();

        final List<DeliveryStoreModel> deliveryStores = new ArrayList<>(1);
        final DeliveryStoreModel deliveryStore = new DeliveryStoreModel();

        // 店舗コード
        deliveryStore.setStoreCode(matchJunmst .getShpcd());
        // 配分順
        deliveryStore.setDistributionSort(matchJunmst .getHjun());
        // 配分区分
        deliveryStore.setAllocationType(AllocationType.NORMAL);

        // 得意先SKU
        final List<DeliveryStoreSkuModel> deliveryStoreSkus = deliveryDetailModel
                .getDeliverySkus()
                .stream()
                .map(this::generateDeliveryStoreSku)
                .collect(Collectors.toList());
        deliveryStore.setDeliveryStoreSkus(deliveryStoreSkus);

        deliveryStores.add(deliveryStore);
        deliveryDetailModel.setDeliveryStores(deliveryStores);
    }

    /**
     * @param deliverySku DeliverySkuModel
     * @return デフォルト納品得意先SKU情報
     */
    private DeliveryStoreSkuModel generateDeliveryStoreSku(final DeliverySkuModel deliverySku) {
        final DeliveryStoreSkuModel deliveryStoreSku = new DeliveryStoreSkuModel();
        deliveryStoreSku.setSize(deliverySku.getSize());
        deliveryStoreSku.setColorCode(deliverySku.getColorCode());
        deliveryStoreSku.setDeliveryLot(deliverySku.getDeliveryLot());
        return deliveryStoreSku;
    }

    /**
     * 納品得意先の登録.
     * @param requestDeliveryStores 画面から入力された納品得意先情報リスト
     * @param registDeliveryDetailEntity 登録した納品明細のリスト
     */
    private void insertDeliveryStore(final List<DeliveryStoreModel> requestDeliveryStores, final TDeliveryDetailEntity registDeliveryDetailEntity) {
        for (DeliveryStoreModel deliveryStoreModel: requestDeliveryStores) {
            // 納品得意先の値をセット
            final TDeliveryStoreEntity registDeliveryStore = generateDeliveryStoreForInsert(registDeliveryDetailEntity, deliveryStoreModel);
            deliveryStoreRepository.save(registDeliveryStore);

            // 納品得意先SKUの登録
            insertDeliveryStoreSku(deliveryStoreModel.getDeliveryStoreSkus(), registDeliveryStore);
        }
    }

    /**
     * 納品得意先SKUの登録.
     * @param deliveryStoreSkus 画面から入力された納品得意先SKU情報リスト
     * @param registDeliveryStore 登録した納品得意先のリスト
     */
    private void insertDeliveryStoreSku(final List<DeliveryStoreSkuModel> deliveryStoreSkus,
            final TDeliveryStoreEntity registDeliveryStore) {
        // 納品得意先SKUの値をセット
        final List<TDeliveryStoreSkuEntity> registDeliveryStoreSkus = deliveryStoreSkus.stream()
                .map(deliveryStoreSkuModel -> generateDeliveryStoreSkuForInsert(registDeliveryStore, deliveryStoreSkuModel))
                .collect(Collectors.toList());

        deliveryStoreSkuRepository.saveAll(registDeliveryStoreSkus);
    }

    /**
     * INSERT用の納品得意先情報を作成する.
     *
     * @param registDeliveryDetailEntity INSERT用の納品依頼明細情報
     * @param deliveryStoreModel 画面から入力された納品得意先情報
     * @return deliverySkuEntity 画面の情報とAPI側で算出する情報をセットした納品得意先Entity
     */
    private TDeliveryStoreEntity generateDeliveryStoreForInsert(final TDeliveryDetailEntity registDeliveryDetailEntity,
            final DeliveryStoreModel deliveryStoreModel) {
        // INSERT用の納品得意先情報Entity
        final TDeliveryStoreEntity deliveryStoreEntity = new TDeliveryStoreEntity();

        /* 画面側で設定される項目 */
        // 店舗コード
        final String storeCode = deliveryStoreModel.getStoreCode();
        deliveryStoreEntity.setStoreCode(storeCode);
        // 店舗別配分率ID
        deliveryStoreEntity.setStoreDistributionRatioId(deliveryStoreModel.getStoreDistributionRatioId());
        // 店舗別配分率区分
        deliveryStoreEntity.setStoreDistributionRatioType(deliveryStoreModel.getStoreDistributionRatioType());
        // 店舗別配分率
        deliveryStoreEntity.setStoreDistributionRatio(deliveryStoreModel.getStoreDistributionRatio());
        // 配分順
        deliveryStoreEntity.setDistributionSort(deliveryStoreModel.getDistributionSort());
        // 配分区分
        deliveryStoreEntity.setAllocationType(deliveryStoreModel.getAllocationType());

        /* APIで計算する項目 */
        // 納品明細ID
        deliveryStoreEntity.setDeliveryDetailId(registDeliveryDetailEntity.getId());

        return deliveryStoreEntity;
    }

    /**
     * INSERT用の納品得意先SKU情報を作成する.
     *
     * @param registDeliveryStore INSERT用の納品依頼明細情報
     * @param deliveryStoreSkuModel 画面から入力された納品得意先情報
     * @return 画面の情報とAPI側で算出する情報をセットした納品得意先SKUEntity
     */
    private TDeliveryStoreSkuEntity generateDeliveryStoreSkuForInsert(final TDeliveryStoreEntity registDeliveryStore,
            final DeliveryStoreSkuModel deliveryStoreSkuModel) {
        // INSERT用の納品得意先SKU情報Entity
        final TDeliveryStoreSkuEntity deliveryStoreSkuEntity = new TDeliveryStoreSkuEntity();

        /* 画面側で設定される項目 */
        // サイズ
        deliveryStoreSkuEntity.setSize(deliveryStoreSkuModel.getSize());
        // 色
        deliveryStoreSkuEntity.setColorCode(deliveryStoreSkuModel.getColorCode());
        // 納品数量
        deliveryStoreSkuEntity.setDeliveryLot(deliveryStoreSkuModel.getDeliveryLot());

        /* APIで計算する項目 */
        // 納品得意先ID
        deliveryStoreSkuEntity.setDeliveryStoreId(registDeliveryStore.getId());
        // 入荷数量
        deliveryStoreSkuEntity.setArrivalLot(DefaultValueConstants.DEFAULT_INT_ZERO);

        return deliveryStoreSkuEntity;
    }

    /**
     * 登録した納品依頼から一番小さい納品依頼IDを取得.
     * nullの場合はシステムエラー
     *
     * @param registedDeliveries 登録した納品依頼のリスト
     * @return 登録した一番小さい納品依頼ID
     */
    public BigInteger getMinimumRegistedDeliveryId(final List<TDeliveryEntity> registedDeliveries) {
        final BigInteger minimumRegistedDeliveryId = registedDeliveries.stream()
                .map(registedDelivery -> registedDelivery.getId()).sorted().findFirst().orElse(null);
        if (Objects.isNull(minimumRegistedDeliveryId)) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.SYSTEM_ERROR));
        }

        return minimumRegistedDeliveryId;
    }

    /**
     * DeliveryCreateServiceとDeliveryUpdateServiceで納品SKU情報を登録(INSERT)する処理.
     * ・納品SKUの値をセットし登録
     *
     * @param deliverySkus 画面から入力された納品依頼SKU情報リスト
     * @param deliveryDetailEntity 登録済みの納品依頼明細情報
     */
    private void insertDeliverySku(final List<DeliverySkuModel> deliverySkus, final TDeliveryDetailEntity deliveryDetailEntity) {
        // 納品依頼SKUの値をセット
        final List<TDeliverySkuEntity> registDeliverySkus = deliverySkus.stream()
                .map(deliverySkuModel -> generateDeliverySkuForInsert(deliveryDetailEntity, deliverySkuModel))
                .collect(Collectors.toList());

        deliverySkuRepository.saveAll(registDeliverySkus);
    }

    /**
     * DeliveryCreateServiceとDeliveryUpdateServiceで登録した納品依頼のメールを送信する.
     *
     * @param extendedTItemEntity DB最新の品番情報
     * @param extendedTOrderEntity DB最新の発注情報
     * @param loginUser ログインユーザ情報
     * @param registedDeliveries 登録した納品依頼リスト
     */
    public void sendDeliveryRequestRegistMails(final ExtendedTItemEntity extendedTItemEntity, final ExtendedTOrderEntity extendedTOrderEntity,
            final CustomLoginUser loginUser, final List<TDeliveryEntity> registedDeliveries) {
        if (registedDeliveries.isEmpty()) {
            return;
        }

        // メール送信用データ作成
        final List<DeliveryRequestRegistSendModel> deliveryRequestRegistSendModels = new ArrayList<>();

        registedDeliveries.stream().forEach(registedDelivery -> {
            final DeliveryRequestRegistSendModel sendModel = generateSendMailData(extendedTItemEntity, extendedTOrderEntity, registedDelivery);
            deliveryRequestRegistSendModels.add(sendModel);
        });

        // メールを送信する(登録した納品依頼の数だけ送る)
        deliveryRequestRegistSendModels.stream().forEach(sendModel -> {
            deliveryRequestRegistSendMailComponent.sendMail(sendModel, loginUser.getAccountName());
        });
    }

    /**
     * DeliveryCreateServiceとDeliveryUpdateServiceで登録した納品依頼の
     * メール送信用データ作成.
     *
     * @param extendedTItemEntity DB最新の品番情報
     * @param extendedTOrderEntity DB最新の発注情報
     * @param tDeliveryEntity TDeliveryEntity
     * @return deliveryRequestRegistModel メール送信用データ
     */
    private DeliveryRequestRegistSendModel generateSendMailData(final ExtendedTItemEntity extendedTItemEntity, final ExtendedTOrderEntity extendedTOrderEntity,
            final TDeliveryEntity tDeliveryEntity) {
        final DeliveryRequestRegistSendModel deliveryRequestRegistModel = new DeliveryRequestRegistSendModel();

        // 品番情報をセット
        BeanUtils.copyProperties(extendedTItemEntity, deliveryRequestRegistModel);

        // 発注情報をセット
        deliveryRequestRegistModel.setMdfMakerCode(extendedTOrderEntity.getMdfMakerCode());
        deliveryRequestRegistModel.setMdfMakerName(extendedTOrderEntity.getMdfMakerName());
        deliveryRequestRegistModel.setMdfStaffCode(extendedTOrderEntity.getMdfStaffCode());
        deliveryRequestRegistModel.setOrderNumber(extendedTOrderEntity.getOrderNumber());
        deliveryRequestRegistModel.setQuantity(extendedTOrderEntity.getQuantity());
        deliveryRequestRegistModel.setProductDeliveryAt(extendedTOrderEntity.getProductCorrectionDeliveryAt()); // 製品修正納期を使用

        // 納期3種を取得
        final BigInteger deliveryId = tDeliveryEntity.getId();
        final ExtendedSendMailDeliveryAtEntity sendDeliveryAtEntity = extendedSendMailDeliveryAtRepository
                .findDeliveryAt(deliveryId,
                        DivisionCodeType.PHOTO.getValue(), DivisionCodeType.SEWING.getValue())
                .orElse(null);
        deliveryRequestRegistModel.setPhotoDeliveryAt(sendDeliveryAtEntity.getPhotoDeliveryAt());
        deliveryRequestRegistModel.setSewingDeliveryAt(sendDeliveryAtEntity.getSewingDeliveryAt());
        deliveryRequestRegistModel.setDeliveryAt(sendDeliveryAtEntity.getDeliveryAt());

        // 納品数量を取得
        final BigDecimal allDeliveredLot = extendedTSkuRepository.cntAllDeliveredLot(deliveryId);
        deliveryRequestRegistModel.setAllDeliveredLot(allDeliveredLot);

        return deliveryRequestRegistModel;
    }

    /**
     * 納品依頼Noを採番する.
     * @return 納品依頼No採番値
     */
    public BigInteger numberingDeliveryRequestNumber() {
        final BigInteger deliveryRequestNumber = numberComponent.createNumber(
                MNumberTableNameType.T_DELIVERY_DETAIL,
                MNumberColumnNameType.DELIVERY_REQUEST_NUMBER);
        // 納品依頼Noの採番値がNULLの場合、エラー返却
        if (Objects.isNull(deliveryRequestNumber)) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_D_003));
        }
        return deliveryRequestNumber;
    }

    /**
     * 納品依頼明細情報リストを取得する.
     *
     * @param deliveryId 納品依頼ID
     * @return DBの最新の納品依頼明細リスト
     */
    public List<TDeliveryDetailEntity> getTDeliveryDetailList(final BigInteger deliveryId) {
        final List<TDeliveryDetailEntity> deliveryDetals =
                deliveryDetailRepository.findByDeliveryId(deliveryId, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        if (deliveryDetals.isEmpty()) {
            throw new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002));
        }
        return deliveryDetals;
    }

    /**
     * 訂正・削除可否チェック.
     *
     * @param deliveryEntity 納品情報.
     */
    public void checkChangeDelivery(final TDeliveryEntity deliveryEntity) {
        final BigInteger orderId = deliveryEntity.getOrderId();
        final ExtendedTOrderEntity extendedTOrderEntity = orderComponent.getExtendedTOrder(orderId);

        // 発注情報の全済区分が済の場合、訂正不可
        if (CompleteType.COMPLETE == extendedTOrderEntity.getAllCompletionType()) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_D_019));
        }

        // 発注情報の費目が生地発注または附属発注の場合、訂正不可
        final ExpenseItemType expenseItem = extendedTOrderEntity.getExpenseItem();
        if (ExpenseItemType.MALT_ORDER == expenseItem || ExpenseItemType.ATTACHMENT_ORDER == expenseItem) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_D_020));
        }

        // 納品明細情報の送信区分が送信済が1件以上ある場合訂正不可
        final List<TDeliveryDetailEntity> deliveryDetails = deliveryComponent.getTDeliveryDetailList(deliveryEntity.getId());
        if (deliveryDetails.stream().anyMatch(deliveryDetail ->
                            SendCodeType.SEND == SendCodeType.findByValue(deliveryDetail.getSendCode()).orElse(null))) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_D_021));
        }
    }

    /**
     * 削除時承認済の場合のバリデーション.
     *
     * @param serviceParameter リクエストパラメータ
     * @param tDeliveryEntity 納品情報
     */
    public void judgeDeleteValidateAtApproval(final DeleteServiceParameter<BigInteger> serviceParameter,
            final TDeliveryEntity tDeliveryEntity) {
        // メーカー権限の場合、業務エラー
        if (!serviceParameter.getLoginUser().isAffiliation()) {
            log.warn("without Affiliation, cannot delete in approval.");
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_D_005));
        }

        // 変更可否チェック.不可の場合業務エラー
        deliveryComponent.checkChangeDelivery(tDeliveryEntity);

        // SQAPIを呼び、削除OKか確認。NG・エラーだったら業務エラー
        final SpecialtyQubeRequestXmlModel reqParamModel = new SpecialtyQubeRequestXmlModel();
        reqParamModel.setOrderNum(tDeliveryEntity.getOrderNumber());
        reqParamModel.setOrderCount(String.format("%02d", tDeliveryEntity.getDeliveryCount()));
        final SpecialtyQubeDeleteResponseXmlModel sqDeleteResult = sqComponent.executeDelete(reqParamModel);

        if (Objects.isNull(sqDeleteResult) || sqDeleteResult.getStatus() == SpecialtyQubeDeleteStatusType.OTHER_ERROR) { // その他エラー
            log.error("SQ error. reslut:", sqDeleteResult);
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_D_013));
        } else if (sqDeleteResult.getStatus() == SpecialtyQubeDeleteStatusType.NO_DATA) {   // データ無し(SQ未連携)
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_D_022));
        } else if (sqDeleteResult.getStatus() == SpecialtyQubeDeleteStatusType.DELETE_NG) { // 仮仕入が確定
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_D_014));
        }
    }

    /**
     * 仕入済バリデーション.
     * @param registeredDeliveryDetails DB登録済納品明細リスト
     * @param purcahses DB登録済仕入情報リスト
     * @param rsltMsg エラーメッセージ
     */
    public void checkArrived(final List<TDeliveryDetailEntity> registeredDeliveryDetails,
                              final List<TPurchaseEntity> purcahses,
                              final ResultMessages rsltMsg) {

        // DB登録済の仕入情報が1つでもLG送信済の場合はエラー
        if (!CollectionUtils.isEmpty(purcahses)) {
            if (purcahses.stream().anyMatch(p -> LgSendType.INSTRUCTION == p.getLgSendType())) {
                rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_D_026));
            }
        }

        // 仕入情報が無くても、入荷済の場合もエラー
        if (registeredDeliveryDetails.stream().anyMatch(dd -> BooleanType.TRUE == dd.getArrivalFlg())) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_D_026));
        }
    }

    /**
     * 配分出荷指示済バリデーション.
     * @param registeredDeliveryDetails DB登録済納品明細リスト
     * @param rsltMsg エラーメッセージ
     */
    public void checkShippingInstructed(final List<TDeliveryDetailEntity> registeredDeliveryDetails, final ResultMessages rsltMsg) {
        if (registeredDeliveryDetails.stream().anyMatch(dd -> BooleanType.TRUE == dd.getShippingInstructionsFlg())) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_D_027));
        }
    }

    /**
     * 課別画面からの更新の場合、得意先・得意先SKUを削除して再設定する.
     * @param requestDeliveryModel リクエストの納品情報
     * @param loginUserId ユーザーID
     * @param registeredDbTItemEntity DB登録済の品番情報
     */
    public void prepareUpdateFromDivisionScreen(
            final DeliveryModel requestDeliveryModel,
            final BigInteger loginUserId,
            final ExtendedTItemEntity registeredDbTItemEntity) {
        if (BooleanType.TRUE != requestDeliveryModel.getFromStoreScreenFlg()) {
            final BigInteger deliveryId = requestDeliveryModel.getId();
            deliveryStoreRepository.updateDeletedAtByDeliveryId(deliveryId, loginUserId);
            deliveryStoreSkuRepository.updateDeletedAtByDeliveryId(deliveryId, loginUserId);
            generateDefaultStoreRecord(registeredDbTItemEntity, requestDeliveryModel.getDeliveryDetails());
        }
    }

    /**
     * 店舗配分訂正かつ未入荷時の課別納品数量との不一致チェック.
     * @param requestDeliveryModel リクエストパラメータの納品依頼情報
     * @param rsltMsg ResultMessages
     */
    public void checkNotMatchToDivisionLot(
            final DeliveryModel requestDeliveryModel,
            final ResultMessages rsltMsg) {
        final DeliveryModel dbDelivery = deliveryComponent.findDeliveryById(requestDeliveryModel.getId());
        final List<DeliveryDetailModel> reqDetails = requestDeliveryModel.getDeliveryDetails();

        if (dbDelivery.getDeliveryDetails().stream().anyMatch(dd -> isNotMatch(reqDetails, dd))) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_D_028));
        }
    }

    /**
     * @param reqDetails リクエストの納品明細リスト
     * @param dbDetail DB登録済の納品明細情報
     * @return true:合計数量不一致
     */
    private boolean isNotMatch(
            final List<DeliveryDetailModel> reqDetails,
            final DeliveryDetailModel dbDetail) {
        final Map<String, Integer> divGrp = sumDeliveryLotGroupBySku(dbDetail);
        final Optional<DeliveryDetailModel> reqDdOpt = extractDeliveryDetailByDivisionCode(reqDetails, dbDetail.getDivisionCode());
        if (!reqDdOpt.isPresent()) {
            return true;
        }
        final Map<String, Integer> storeGrp = sumStoreDeliveryLotGroupBySku(reqDdOpt.get());

        return isNotMatchBySku(divGrp, storeGrp);
    }

    /**
     * @param divGrp DB登録済の課別納品数量
     * @param storeGrp リクエストの得意先納品数量
     * @return true:課・SKUごとの合計数量不一致
     */
    private boolean isNotMatchBySku(
            final Map<String, Integer> divGrp,
            final Map<String, Integer> storeGrp) {
        for (final Entry<String, Integer> entry : divGrp.entrySet()) {
            if (!Objects.equals(storeGrp.get(entry.getKey()), entry.getValue())) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param deliveryDetails 納品明細リスト
     * @param divisionCode 課コード
     * @return 指定した課コードに該当する納品明細情報
     */
    private Optional<DeliveryDetailModel> extractDeliveryDetailByDivisionCode(
            final List<DeliveryDetailModel> deliveryDetails,
            final String divisionCode) {
        return deliveryDetails.stream()
                .filter(dd -> dd.getDivisionCode().equals(divisionCode))
                .findFirst();
    }

    /**
     * @param dd 納品明細情報
     * @return SKUごとグループした納品数量合計
     */
    private Map<String, Integer> sumDeliveryLotGroupBySku(final DeliveryDetailModel dd) {
        return dd.getDeliverySkus().stream()
                .collect(Collectors.groupingBy(ds -> generateSkuKey(ds.getColorCode(), ds.getSize()),
                        Collectors.summingInt(DeliverySkuModel::getDeliveryLot)));
    }

    /**
     * @param dd 納品明細情報
     * @return SKUごとグループした得意先の納品数量合計
     */
    private Map<String, Integer> sumStoreDeliveryLotGroupBySku(final DeliveryDetailModel dd) {
        return dd.getDeliveryStores().stream()
                .flatMap(ds -> ds.getDeliveryStoreSkus().stream())
                .collect(Collectors.groupingBy(ds -> generateSkuKey(ds.getColorCode(), ds.getSize()),
                        Collectors.summingInt(DeliveryStoreSkuModel::getDeliveryLot)));
    }

    /**
     * @param colorCode カラーコード
     * @param size サイズ
     * @return カラーコードとサイズを「_」で連結した文字列
     */
    public String generateSkuKey(final String colorCode, final String size) {
        final StringBuffer sb = new StringBuffer();
        sb.append(colorCode).append("_").append(size);
        return sb.toString();
    }


    /**
     * @param deliveryId 納品ID
     * @return true:ゼロ確
     */
    public boolean isZeroFix(final BigInteger deliveryId) {
        final DeliveryModel dbDeliveryModel = deliveryComponent.findDeliveryById(deliveryId);
        return dbDeliveryModel.getDeliveryDetails()
                .stream()
                .filter(dd -> BooleanType.TRUE == dd.getArrivalFlg())
                .anyMatch(dd -> 0 == dd.getDeliverySkus().stream().mapToInt(DeliverySkuModel::getArrivalLot).sum());
    }


    /**
     * 仕入情報の入荷場所で納品明細の入荷場所を更新.
     *
     * @param purchaseModel 画面入力されたpurchaseModel
     * @param loginIserId ログインユーザID
     */
    public void updateDeliveryDetailArrivalPlace(final PurchaseModel purchaseModel, final BigInteger loginIserId) {

        final List<TDeliveryDetailEntity> deliveryDetailEntityList =
                                               deliveryDetailRepository.findByDeliveryId(purchaseModel.getDeliveryId(),
                                                                                         PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        deliveryDetailEntityList.stream().forEach(dd -> {
            // PRD_0010 mod SIT start
            //dd.setArrivalPlace(StringUtils.left(purchaseModel.getArrivalPlace(), 1));
            dd.setArrivalPlace(StringUtils.left(
                    shipmentComponent.extraxtOldLogisticsCode(purchaseModel.getArrivalShop()), 1));
            // PRD_0010 mod SIT end
            dd.setUpdatedUserId(loginIserId);
            deliveryDetailRepository.save(dd);
        });

    }

}
