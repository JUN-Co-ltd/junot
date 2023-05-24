package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.PurchaseComponent;
import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.PurchaseConfirmListModel;
import jp.co.jun.edi.model.PurchaseConfirmModel;
import jp.co.jun.edi.repository.TDeliveryRepository;
import jp.co.jun.edi.repository.TPurchaseRepository;
import jp.co.jun.edi.service.parameter.ApprovalServiceParameter;
import jp.co.jun.edi.service.response.ApprovalServiceResponse;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * LG送信(仕入確定)用Service.
 */
@Service
public class PurchaseConfirmService extends GenericUpdateService<ApprovalServiceParameter<PurchaseConfirmListModel>, ApprovalServiceResponse> {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private TPurchaseRepository purchaseRepository;

    @Autowired
    private TDeliveryRepository deliveryRepository;

    @Autowired
    private PurchaseComponent purchaseComponent;

    @Override
    protected ApprovalServiceResponse execute(final ApprovalServiceParameter<PurchaseConfirmListModel> serviceParameter) {
        final PurchaseConfirmListModel purchaseConfirmListModel = serviceParameter.getItem();

        final List<PurchaseConfirmModel> reqPurchases = purchaseConfirmListModel.getPurchases();

        // 各リクエストパラメータのリスト(重複除去)
        final List<BigInteger> deliveryIds =
                purchaseConfirmListModel.getPurchases().stream().map(PurchaseConfirmModel::getDeliveryId).distinct().collect(Collectors.toList());
        final List<String> divisionCodes =
                purchaseConfirmListModel.getPurchases().stream().map(PurchaseConfirmModel::getDivisionCode).distinct().collect(Collectors.toList());

        // リクエストパラメータに該当する納品情報、仕入情報取得
        final List<TDeliveryEntity> dbDeliveries = deliveryRepository.findByIds(deliveryIds).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));
        final List<TPurchaseEntity> noFilterDbPurchases = purchaseRepository.findByLgKeyList(deliveryIds, divisionCodes).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // リクエストのキーと合致するレコード抽出
        final List<TPurchaseEntity> dbPurchases = noFilterDbPurchases.stream().filter(p -> existsMatchKey(reqPurchases, p)).collect(Collectors.toList());

        // バリデーションチェック
        final ResultMessages rsltMsg = checkValidate(reqPurchases, dbDeliveries, dbPurchases);
        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }

        // 倉庫連携ファイル情報登録
        final TWmsLinkingFileEntity wmsLinkingFileEntity = purchaseComponent.insertWmsLinkingFile(BusinessType.PURCHASE_INSTRUCTION);

        // 更新
        confirm(dbPurchases, wmsLinkingFileEntity, serviceParameter.getLoginUser().getUserId());

        return ApprovalServiceResponse.builder().build();
    }

    /**
     * @param reqPurchases リクエストの仕入リスト
     * @param dbPurchase DBの仕入情報
     * @return リクエストのキーと合致する仕入情報
     */
    private boolean existsMatchKey(final List<PurchaseConfirmModel> reqPurchases, final TPurchaseEntity dbPurchase) {
        return reqPurchases.stream().anyMatch(p -> isMatchLgKey(dbPurchase, p));
    }

    /**
     * @param dbPurchase DBの仕入情報
     * @param req リクエストパラメータ
     * @return true:LG送信のキーが同じ
     */
    private boolean isMatchLgKey(final TPurchaseEntity dbPurchase, final PurchaseConfirmModel req) {
        return dbPurchase.getDeliveryId().equals(req.getDeliveryId())
                && dbPurchase.getDivisionCode().equals(req.getDivisionCode());
    }

    /**
     * バリデーションチェックを行う.
     * @param requestList リクエストデータリスト
     * @param dbDeliveries DB登録済の納品情報リスト
     * @param dbPurchases DB登録済の仕入情報リスト
     * @return ResultMessages
     */
    private ResultMessages checkValidate(
            final List<PurchaseConfirmModel> requestList,
            final List<TDeliveryEntity> dbDeliveries,
            final List<TPurchaseEntity> dbPurchases) {

        final ResultMessages rsltMsg = ResultMessages.warning();

        // DBから取得できなければエラー
        if (dbPurchases.isEmpty()) {
            return ResultMessages.warning().add(MessageCodeType.CODE_002);
        }

        // 納品情報のバリデーション
        requestList.stream().forEach(r -> addIfDeliveryError(r, dbDeliveries, rsltMsg));
        // 仕入情報のバリデーション
        requestList.stream().forEach(r -> addIfPurchaseError(r, dbPurchases, rsltMsg));

        return rsltMsg;
    }

    /**
     * エラーがあればエラー情報を追加する.
     * @param req リクエストパラメータ
     * @param dbDeliveries DBの納品依頼情報リスト
     * @param rsltMsg エラーメッセージ
     */
    private void addIfDeliveryError(
            final PurchaseConfirmModel req,
            final List<TDeliveryEntity> dbDeliveries,
            final ResultMessages rsltMsg) {

        final Optional<TDeliveryEntity> opt = dbDeliveries.stream().filter(d -> d.getId().equals(req.getDeliveryId())).findFirst();

        // 存在しない場合エラー
        if (!opt.isPresent()) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_PC_006, getMessage("code.400_PC_06", req.getDivisionCode(), req.getPurchaseCount())));
            return;
        }

        // ロック中の場合エラー
        if (BooleanType.TRUE == opt.get().getSqLockFlg()) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_PC_005, getMessage("code.400_PC_05", req.getDivisionCode(), req.getPurchaseCount())));
        }
    }

    /**
     * エラーがあればエラー情報を追加する.
     * @param req リクエストパラメータ
     * @param dbPurchases DBの仕入情報リスト
     * @param rsltMsg エラーメッセージ
     */
    private void addIfPurchaseError(
            final PurchaseConfirmModel req,
            final List<TPurchaseEntity> dbPurchases,
            final ResultMessages rsltMsg) {

        final List<TPurchaseEntity> filteredList = filterByLgKey(req, dbPurchases);

        // 存在しない場合エラー
        if (filteredList.isEmpty()) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_PC_006, getMessage("code.400_PC_06", req.getDivisionCode(), req.getPurchaseCount())));
            return;
        }

        // LG送信指示済みの場合エラー
        final boolean exitstLgErr = filteredList.stream().anyMatch(lg -> LgSendType.INSTRUCTION == lg.getLgSendType());
        if (exitstLgErr) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_PC_003, getMessage("code.400_PC_03", req.getDivisionCode(), req.getPurchaseCount())));
        }
    }

    /**
     * @param req リクエストパラメータ
     * @param purchases DBの仕入情報リスト
     * @return LG送信のキーで抽出した仕入情報リスト
     */
    private List<TPurchaseEntity> filterByLgKey(final PurchaseConfirmModel req, final List<TPurchaseEntity> purchases) {
        return purchases.stream().filter(p -> isMatchLgKey(p, req)).collect(Collectors.toList());
    }

    /**
     * @param code コード
     * @param args 引数
     * @return メッセージ
     */
    private String getMessage(final String code, final Object... args) {
        return messageSource.getMessage(code, args, Locale.JAPANESE);
    }

    /**
     * 仕入情報更新.
     * LG送信指示済に更新.
     * @param dbPurchases リクエストのキーで抽出したDBの仕入情報リスト
     * @param wmsLinkingFileEntity 登録済の倉庫連携ファイル情報Entity
     * @param userId ユーザーID
     */
    private void confirm(final List<TPurchaseEntity> dbPurchases,
                          final TWmsLinkingFileEntity wmsLinkingFileEntity,
                          final BigInteger userId) {

        purchaseComponent.sortForUpdate(dbPurchases);

        purchaseComponent.prepareSaveData(dbPurchases, wmsLinkingFileEntity, userId);

        purchaseRepository.saveAll(dbPurchases);

    }
}
