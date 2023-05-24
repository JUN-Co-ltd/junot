package jp.co.jun.edi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.TFOrderEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.FukukitaruOrderModel;
import jp.co.jun.edi.repository.MFDestinationRepository;
import jp.co.jun.edi.repository.TFOrderRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.FukukitaruMasterConfirmStatusType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.OccupationType;

/**
 * フクキタル発注情報を確定するサービス.
 */
@Service
public class FukukitaruOrderApproveService
        extends GenericUpdateService<UpdateServiceParameter<FukukitaruOrderModel>, UpdateServiceResponse<FukukitaruOrderModel>> {
    @Autowired
    private TFOrderRepository tfOrderRepository;

    @Autowired
    private TOrderRepository tOrderRepository;

    @Autowired
    private TItemRepository tItemRepository;

    @Autowired
    private MFDestinationRepository mFDestinationRepository;

    @Override
    protected UpdateServiceResponse<FukukitaruOrderModel> execute(final UpdateServiceParameter<FukukitaruOrderModel> serviceParameter) {
        final FukukitaruOrderModel fukukitaruOrderModel = serviceParameter.getItem();
        final CustomLoginUser user = serviceParameter.getLoginUser();

        // DBから最新のフクキタル発注情報を取得する。削除されている場合は、例外をスローする
        final TFOrderEntity currentTFOrderEntity = tfOrderRepository.findByIdDeletedAtIsNull(fukukitaruOrderModel.getId())
                .orElseThrow(() -> new BusinessException(ResultMessages.warning().add(ResultMessage.fromCode(MessageCodeType.CODE_FO_001))));

        // バリデーションチェック
        checkValidate(currentTFOrderEntity, fukukitaruOrderModel, user);

        // フクキタル発注情報を承認
        approveFukukitaruOrder(currentTFOrderEntity, user);

        return UpdateServiceResponse.<FukukitaruOrderModel>builder().item(fukukitaruOrderModel).build();
    }

    /**
     * バリデーションチェック.
     * @param currentTFOrderEntity DBに登録されているフクキタル発注情報
     * @param fukukitaruOrderModel 画面から送信されてきたフクキタル発注情報
     * @param user ログインユーザ情報
     */
    private void checkValidate(final TFOrderEntity currentTFOrderEntity, final FukukitaruOrderModel fukukitaruOrderModel,
            final CustomLoginUser user) {
        // 品番情報が削除されている場合は承認不可
        if (!tItemRepository.findByIdAndDeletedAtIsNull(fukukitaruOrderModel.getPartNoId()).isPresent()) {
            throw new BusinessException(ResultMessages.warning().add(ResultMessage.fromCode(MessageCodeType.CODE_FO_001)));
        }

        // 発注情報が削除されている場合は承認不可
        if (!tOrderRepository.findByOrderId(fukukitaruOrderModel.getOrderId()).isPresent()) {
            throw new BusinessException(ResultMessages.warning().add(ResultMessage.fromCode(MessageCodeType.CODE_FO_001)));
        }

        // 承認権限がない(製造担当でない)場合は承認不可
        if (OccupationType.PRODUCTION != user.getOccupationType()) {
            throw new BusinessException(ResultMessages.warning().add(ResultMessage.fromCode(MessageCodeType.CODE_FO_006)));
        }

        // 請求先がJUNでない(承認不要)の場合は承認不可
        final BooleanType isApprovalRequired =
                mFDestinationRepository.findIsApprovalRequiredByBillingCompanyId(currentTFOrderEntity.getBillingCompanyId());
        if (BooleanType.TRUE != isApprovalRequired) {
            throw new BusinessException(ResultMessages.warning().add(ResultMessage.fromCode(MessageCodeType.CODE_FO_004)));
        }

        // 承認済み(確定ステータスが2(未承認)以外)の場合は承認不可
        if (FukukitaruMasterConfirmStatusType.ORDER_UNAPPROVED != currentTFOrderEntity.getConfirmStatus()) {
            throw new BusinessException(ResultMessages.warning().add(ResultMessage.fromCode(MessageCodeType.CODE_FO_005)));
        }
    }

    /**
     * フクキタル発注情報を更新する.
     * @param currentTFOrderEntity DBに登録されているフクキタル発注情報
     * @param user ログインユーザ情報
     */
    private void approveFukukitaruOrder(final TFOrderEntity currentTFOrderEntity, final CustomLoginUser user) {
        // 確定ステータスに0(未確定)をセット
        currentTFOrderEntity.setConfirmStatus(FukukitaruMasterConfirmStatusType.ORDER_NOT_CONFIRMED);
        currentTFOrderEntity.setUpdatedUserId(user.getUserId());
        tfOrderRepository.save(currentTFOrderEntity);
    }
}
