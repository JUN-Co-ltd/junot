package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.FukukitaruItemComponent;
import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.LoginUserComponent;
import jp.co.jun.edi.component.MJanNumberComponent;
import jp.co.jun.edi.component.OrderComponent;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.CompositionModel;
import jp.co.jun.edi.model.FileModel;
import jp.co.jun.edi.model.FukukitaruItemModel;
import jp.co.jun.edi.model.ItemFileInfoModel;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.MisleadingRepresentationFileModel;
import jp.co.jun.edi.model.OrderModel;
import jp.co.jun.edi.model.OrderSupplierModel;
import jp.co.jun.edi.model.SkuModel;
import jp.co.jun.edi.repository.TDeliveryRepository;
import jp.co.jun.edi.repository.TFileInfoSelectRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.extended.ExtendedTCompositionRepository;
import jp.co.jun.edi.repository.extended.ExtendedTItemRepository;
import jp.co.jun.edi.repository.extended.ExtendedTMisleadingRepresentationFileRepository;
import jp.co.jun.edi.repository.extended.ExtendedTMisleadingRepresentationRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderSupplierRepository;
import jp.co.jun.edi.repository.extended.ExtendedTSkuRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.type.ApprovalType;
import jp.co.jun.edi.type.FileInfoCategory;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.MisleadingRepresentationType;
import jp.co.jun.edi.type.OrderCategoryType;
import jp.co.jun.edi.type.QualityApprovalType;

/**
 * 品番情報を取得するサービス.
 */
@Service
public class ItemGetService extends GenericGetService<GetServiceParameter<BigInteger>, GetServiceResponse<ItemModel>> {
    @Autowired
    private ExtendedTItemRepository extendedTItemRepository;

    @Autowired
    private ExtendedTCompositionRepository extendedTCompositionRepository;

    @Autowired
    private ExtendedTSkuRepository extendedTSkuRepository;

    @Autowired
    private TFileInfoSelectRepository tFileInfoSelectRepository;

    @Autowired
    private ExtendedTMisleadingRepresentationFileRepository extendedTMisleadingRepresentationFileRepository;

    @Autowired
    private FukukitaruItemComponent fukukitaruItemComponent;

    @Autowired
    private ExtendedTOrderSupplierRepository extendedTOrderSupplierRepository;

    @Autowired
    private MJanNumberComponent mJanNumberComponent;

    @Autowired
    private ItemComponent itemComponent;

    @Autowired
    private TOrderRepository tOrderRepository;

    @Autowired
    private OrderComponent orderComponent;

    @Autowired
    private TDeliveryRepository tDeliveryRepository;

    @Autowired
    private ExtendedTMisleadingRepresentationRepository exMisleadingRepresentationRepository;

    @Autowired
    private LoginUserComponent loginUserComponent;

    @Override
    protected GetServiceResponse<ItemModel> execute(final GetServiceParameter<BigInteger> serviceParameter) {
        // 品番情報を取得し、データが存在しない場合は例外を投げる
        final ExtendedTItemEntity entity = extendedTItemRepository.findById(serviceParameter.getId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        final ItemModel item = new ItemModel();

        // 品番情報のコピー
        BeanUtils.copyProperties(entity, item);

        // 発注先メーカー情報を取得する。
        item.setOrderSuppliers(
                extendedTOrderSupplierRepository.findByPartNoId(item.getId(), OrderCategoryType.PRODUCT.getValue(),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).stream()
                .filter(o -> loginUserComponent.isSupplierAuthority(serviceParameter.getLoginUser(), o.getSupplierCode()))
                .map(tOrderSupplier -> {
                    final OrderSupplierModel orderSupplier = new OrderSupplierModel();

                    // 発注先メーカー情報をコピー
                    BeanUtils.copyProperties(tOrderSupplier, orderSupplier);

                    return orderSupplier;
                }).collect(Collectors.toList()));

        // 発注先メーカー(製品)の情報を設定する。
        setProductOrderSupplier(item, serviceParameter.getLoginUser());

        // SKU情報を取得する
        item.setSkus(extendedTSkuRepository.findByPartNoId(entity.getId(),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("color_code")))).stream()
                .map(tSku -> {
                    final SkuModel sku = new SkuModel();

                    // SKU情報のコピー
                    BeanUtils.copyProperties(tSku, sku);

                    // 先頭ゼロ除去
                    sku.setJanCode(mJanNumberComponent.zeroSuppressArticleNumber(item.getJanType(), tSku.getJanCode()));

                    return sku;
                }).collect(Collectors.toList()));

        // 組成情報を取得する
        item.setCompositions(extendedTCompositionRepository.findByPartNoId(entity.getId(),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("color_code"), Order.asc("serial_number")))).stream()
                .map(tComposition -> {
                    final CompositionModel composition = new CompositionModel();

                    // 組成情報のコピー
                    BeanUtils.copyProperties(tComposition, composition);

                    return composition;
                }).collect(Collectors.toList()));

        // 品番ファイル情報を取得
        item.setItemFileInfos(tFileInfoSelectRepository.findByPartNoId(entity.getId(),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).stream()
                .map(tFileInfoSelect -> {
                    final ItemFileInfoModel fileInfo = new ItemFileInfoModel();

                    // 品番ファイル情報のコピー
                    BeanUtils.copyProperties(tFileInfoSelect, fileInfo);
                    // enumはコピーできないので個別対応
                    fileInfo.setFileCategory(FileInfoCategory.findByValue(tFileInfoSelect.getFileCategory()).get());

                    return fileInfo;
                }).collect(Collectors.toList()));

        // 優良誤認検査ファイル情報を取得
        item.setMisleadingRepresentationFiles(extendedTMisleadingRepresentationFileRepository.findByPartNoId(entity.getId(),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).stream()
                .map(tMisleadingRepresentationFile -> {
                    final MisleadingRepresentationFileModel misleadingRepresentationFile = new MisleadingRepresentationFileModel();
                    final FileModel file = new FileModel();

                    // 優良誤認検査ファイル情報のコピー
                    BeanUtils.copyProperties(tMisleadingRepresentationFile, misleadingRepresentationFile);
                    BeanUtils.copyProperties(tMisleadingRepresentationFile, file);

                    // ファイル情報のidはfile_no_idをセット
                    file.setId(tMisleadingRepresentationFile.getFileNoId());
                    misleadingRepresentationFile.setFile(file);

                    return misleadingRepresentationFile;
                }).collect(Collectors.toList()));

        // フクキタル品番情報を取得する
        item.setFkItem(getTFItem(entity.getId()));

        // 品番IDに紐づく、発注情報を取得する
        final List<TOrderEntity> tOrders = tOrderRepository.findByPartNoId(entity.getId());

        // 発注情報のリストを設定する
        setOrders(item, serviceParameter.getLoginUser(), tOrders);

        // 品番情報の更新可否を判断するための情報を設定する
        setUpdateAvailability(item, tOrders);

        return GetServiceResponse.<ItemModel>builder().item(item).build();
    }

    /**
     * フクキタル品番情報を取得する.
     * @param partNoId 品番ID
     * @return FukukitaruItemModel
     */
    private FukukitaruItemModel getTFItem(final BigInteger partNoId) {
        // フクキタル品番情報を取得し、データが存在しない場合は例外をスローする
        return fukukitaruItemComponent.generatedFukukitaruItemModelSearchPartNoId(partNoId)
                .orElse(new FukukitaruItemModel());
    }

    /**
     * 発注先メーカー(製品)の情報を設定する.
     * メーカー権限で対象のレコードが存在しない場合や、JUN権限で発注先メーカーID(最新製品)に紐づくレコードが存在しない場合はエラーとする
     *
     * @param itemModel 品番情報Model
     * @param user ログインユーザ権限
     */
    private void setProductOrderSupplier(final ItemModel itemModel, final CustomLoginUser user) {
        final OrderSupplierModel orderSupplier = itemModel.getOrderSuppliers()
                .stream()
                .filter(supplier -> {
                    if (user.isAffiliation()) {
                        // JUN権限の場合
                        return Objects.equals(supplier.getId(), itemModel.getCurrentProductOrderSupplierId());
                    } else {
                        // メーカー権限の場合
                        return StringUtils.equals(user.getCompany(), supplier.getSupplierCode());
                    }
                })
                .findFirst().orElse(new OrderSupplierModel());

        // メーカー権限チェック
        loginUserComponent.validateSupplierAuthority(user, orderSupplier.getSupplierCode());

        itemModel.setMdfMakerCode(orderSupplier.getSupplierCode());
        itemModel.setMdfMakerName(orderSupplier.getSupplierName());
        itemModel.setMdfMakerFactoryCode(orderSupplier.getSupplierFactoryCode());
        itemModel.setMdfMakerFactoryName(orderSupplier.getSupplierFactoryName());
        itemModel.setConsignmentFactory(orderSupplier.getConsignmentFactory());
    }

    /**
     * 発注情報のリストを設定する.
     *
     * @param itemModel {@link ItemModel} instance
     * @param user ログインユーザ権限
     * @param tOrders 発注情報テーブルのリスト
     */
    private void setOrders(final ItemModel itemModel, final CustomLoginUser user, final List<TOrderEntity> tOrders) {
        // 「発注一覧」を設定する.
        itemModel.setOrders(tOrders.stream()
                .filter(o -> loginUserComponent.isSupplierAuthority(user, o.getMdfMakerCode()))
                .map(o -> toOrderModel(o))
                .collect(Collectors.toList()));
    }

    /**
     * 発注情報のEntityをModelに変換する.
     *
     * @param tOrder 発注情報Entity
     * @return 発注情報Model
     */
    private OrderModel toOrderModel(final TOrderEntity tOrder) {
        final OrderModel orderModel = new OrderModel();

        // 「ID」を設定する
        orderModel.setId(tOrder.getId());
        // 「発注No」を設定する
        orderModel.setOrderNumber(tOrder.getOrderNumber());
        // 「製品発注日」を設定する
        orderModel.setProductOrderAt(tOrder.getProductOrderAt());
        // 「発注承認ステータス」を設定する
        orderModel.setOrderApproveStatus(tOrder.getOrderApproveStatus());

        return orderModel;
    }

    /**
     * 品番情報の更新可否を判断するための情報を設定する.
     *
     * @param itemModel {@link ItemModel} instance
     * @param tOrders 発注情報Entityのリスト
     */
    private void setUpdateAvailability(final ItemModel itemModel, final List<TOrderEntity> tOrders) {
        // 「読み取り専用」を設定する
        itemModel.setReadOnly(itemComponent.isReadOnly(itemModel.getExternalLinkingType()));

        // 「受注・発注登録済み」に発注の登録状態を設定する
        itemModel.setRegisteredOrder(CollectionUtils.isNotEmpty(tOrders));

        // 「発注承認済み」に発注の承認状態を設定する
        itemModel.setApprovedOrder(orderComponent.isApprovedOrderList(tOrders));

        // 「全ての発注が完納」に全ての発注の完納状態を設定する
        itemModel.setCompletedAllOrder(orderComponent.isCompleteOrderList(tOrders));

        // 「納品依頼承認済み」に納品依頼の承認状態を設定する
        itemModel.setApprovedDelivery(CollectionUtils.isNotEmpty(tDeliveryRepository
                .findMatchApproveStatusDeliverysByPartNoId(itemModel.getId(), ApprovalType.APPROVAL.getValue())));

        // 「優良誤認承認済み」に優良誤認の承認状態を設定する
        itemModel.setApprovedMisleadingRepresentation(isApprovedMisleadingRepresentation(itemModel));

        // 「優良誤認（組成）承認済みのカラーのリスト」に優良誤認（組成）承認済みのカラーコードのリストを設定する
        itemModel.setApprovedColors(getApprovedColors(itemModel.getId()));
    }

    /**
     * 優良誤認の承認状態を取得する.
     * 組成、国、有害物質のいずれかが「承認」「一部（承認済み）」の場合、承認済みとする。
     *
     * @param itemModel {@link ItemModel} instance
     * @return 優良誤認の承認状態
     */
    private boolean isApprovedMisleadingRepresentation(final ItemModel itemModel) {
        return (isApprovedMisleadingRepresentation(QualityApprovalType.convertToType(itemModel.getQualityCompositionStatus()))
                || isApprovedMisleadingRepresentation(QualityApprovalType.convertToType(itemModel.getQualityCooStatus()))
                || isApprovedMisleadingRepresentation(QualityApprovalType.convertToType(itemModel.getQualityHarmfulStatus())));
    }

    /**
     * 優良誤認の承認状態を取得する.
     * 「承認」「一部（承認済み）」の場合、承認済みとする。
     *
     * @param qualityApproval {@link QualityApprovalType} instance
     * @return 優良誤認の承認状態
     */
    private boolean isApprovedMisleadingRepresentation(final QualityApprovalType qualityApproval) {
        return (QualityApprovalType.PART == qualityApproval
                || QualityApprovalType.ACCEPT == qualityApproval);
    }

    /**
     * 優良誤認（組成）承認済みのカラーコードのリストを取得する.
     *
     * @param partNoId 品番ID
     * @return 優良誤認（組成）承認済みのカラーコードのリスト
     */
    private List<String> getApprovedColors(final BigInteger partNoId) {
        return exMisleadingRepresentationRepository.findByPartNoId(partNoId, PageRequest.of(0, Integer.MAX_VALUE)).stream()
                // 組成かつ、承認日が入っているレコードのみ抽出
                .filter(entity -> ((MisleadingRepresentationType.COMPOSITION == entity.getMisleadingRepresentationType())
                        && Objects.nonNull(entity.getApprovalAt())))
                .map(entity -> entity.getColorCode())
                .collect(Collectors.toList());
    }
}
