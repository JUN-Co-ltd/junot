package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DistributionShipmentComponent;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliveryStoreSkuEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DistributionShipmentConfirmListModel;
import jp.co.jun.edi.model.DistributionShipmentConfirmModel;
import jp.co.jun.edi.repository.TDeliveryDetailRepository;
import jp.co.jun.edi.repository.extended.ExtendedDeliveryStoreConfirmRepository;
import jp.co.jun.edi.service.parameter.ApprovalServiceParameter;
import jp.co.jun.edi.service.response.ApprovalServiceResponse;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.CarryType;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * (納品出荷指示送信)用Service.
 */
@Service
public class DistributionShipmentConfirmService
extends GenericUpdateService<ApprovalServiceParameter<DistributionShipmentConfirmListModel>, ApprovalServiceResponse> {

    @Autowired
    private MessageSource messageSource;
    @Autowired
    private TDeliveryDetailRepository deliveryDetailRepository;
    @Autowired
    private ExtendedDeliveryStoreConfirmRepository extendedDeliveryStoreConfirmRepository;
    @Autowired
    private DistributionShipmentComponent distributionShipmentComponent;

    @Override
    protected ApprovalServiceResponse execute(final ApprovalServiceParameter<DistributionShipmentConfirmListModel> serviceParameter) {

        final DistributionShipmentConfirmListModel distributionShipmentConfirmListModel
        = serviceParameter.getItem();
        final List<DistributionShipmentConfirmModel> reqDistributionShipments
        = distributionShipmentConfirmListModel.getDistributionShipmentConfirms();

        // 各リクエストパラメータのリスト(重複除去)
        final List<BigInteger> deliverydetailIds =
                distributionShipmentConfirmListModel.getDistributionShipmentConfirms().stream()
                .map(DistributionShipmentConfirmModel::getId).distinct().collect(Collectors.toList());

        // リクエストのキーと合致するレコード抽出
        final List<TDeliveryDetailEntity> dbDeliveryDetails = deliveryDetailRepository.findByIds(deliverydetailIds).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // バリデーションチェック
        final ResultMessages rsltMsg = checkValidate(reqDistributionShipments, dbDeliveryDetails);
        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }

        // 倉庫連携ファイル情報登録
        final TWmsLinkingFileEntity insertWmsLinkingFile = distributionShipmentComponent
                .insertWmsLinkingFile(BusinessType.DISTRIBUTION_SHIPMENT_INSTRUCTION);

        // 更新
        confirm(dbDeliveryDetails, insertWmsLinkingFile, serviceParameter.getLoginUser().getUserId());

        return ApprovalServiceResponse.builder().build();
    }

    /**
     * バリデーションチェックを行う.
     * @param requestList リクエストデータリスト
     * @param dbDeliveryDetails DB登録済の納品明細情報リスト
     * @return ResultMessages
     */
    private ResultMessages checkValidate(
            final List<DistributionShipmentConfirmModel> requestList,
            final List<TDeliveryDetailEntity> dbDeliveryDetails
            ) {

        final ResultMessages rsltMsg = ResultMessages.warning();

        // [納品明細が存在しない]DBから取得できなければエラー
        if (dbDeliveryDetails.isEmpty()) {
            return ResultMessages.warning().add(MessageCodeType.CODE_002);
        }

        // 納品情報のバリデーション
        requestList.stream().forEach(r -> addIfDeliveryDetailError(r, dbDeliveryDetails, rsltMsg));

        return rsltMsg;
    }

    /**
     * エラーがあればエラー情報を追加する.
     * @param req リクエストパラメータ
     * @param dbDeliveriyDetails DBの納品明細情報リスト
     * @param rsltMsg エラーメッセージ
     */
    private void addIfDeliveryDetailError(
            final DistributionShipmentConfirmModel req,
            final List<TDeliveryDetailEntity> dbDeliveriyDetails,
            final ResultMessages rsltMsg) {

        final Optional<TDeliveryDetailEntity> opt
        = dbDeliveriyDetails.stream().filter(d -> d.getId().equals(req.getId())).findFirst();

        // 存在しない場合エラー
        if (!opt.isPresent()) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_DI_001,
                    getMessage("code.400_DI_01", opt.get().getDeliveryNumber(), opt.get().getDeliveryCount())));
            return;
        }

        // 店舗配分されていない
        if (opt.get().getStoreRegisteredFlg().equals(BooleanType.FALSE)) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_DI_001,
                    getMessage("code.400_DI_01", opt.get().getDeliveryNumber(), opt.get().getDeliveryCount())));
            return;
        }

        // 仕入れ確定されていないd
        if (opt.get().getStoreRegisteredFlg().equals(BooleanType.FALSE)) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_DI_002,
                    getMessage("code.400_DI_02", opt.get().getDeliveryNumber(), opt.get().getDeliveryCount())));
            return;
        }

        // 出荷指示済み
        if (opt.get().getShippingInstructionsFlg().equals(BooleanType.TRUE)) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_DI_003,
                    getMessage("code.400_DI_03", opt.get().getDeliveryNumber(), opt.get().getDeliveryCount())));
            return;
        }

        // 直送
        if (opt.get().getCarryType().equals(CarryType.DIRECT)) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_DI_004,
                    getMessage("code.400_DI_04", opt.get().getDeliveryNumber(), opt.get().getDeliveryCount())));
            return;
        }
    }

    /**
     * メッセージ取得.
     * @param code コード
     * @param args 引数
     * @return メッセージ
     */
    private String getMessage(final String code, final Object... args) {
        return messageSource.getMessage(code, args, Locale.JAPANESE);
    }

    /**
     * 納品明細・納品得意先情報更新.
     * LG送信指示済に更新.
     * @param deliveryDetails 更新用の納品明細情報Entityリスト(リクエストのキーで抽出したDBの仕入情報リスト)
     * @param insertWmsLinkingFile 登録済の倉庫連携ファイル情報
     * @param userId ユーザID
     */
    private void confirm(
            final List<TDeliveryDetailEntity> deliveryDetails, final TWmsLinkingFileEntity insertWmsLinkingFile, final BigInteger userId) {
        distributionShipmentComponent.sortForUpdate(deliveryDetails);

        final List<TDeliveryStoreSkuEntity> sendEntities = new ArrayList<TDeliveryStoreSkuEntity>();
        distributionShipmentComponent.prepareSaveData(deliveryDetails, sendEntities, insertWmsLinkingFile, userId);

        deliveryDetailRepository.saveAll(deliveryDetails);
        extendedDeliveryStoreConfirmRepository.saveAll(sendEntities);
    }
}
