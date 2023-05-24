package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.MakerReturnComponent;
import jp.co.jun.edi.entity.TMakerReturnEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.MakerReturnModel;
import jp.co.jun.edi.model.MakerReturnProductCompositeModel;
import jp.co.jun.edi.repository.TMakerReturnRepository;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * メーカー返品更新処理.
 */
@Service
public class MakerReturnUpdateService extends GenericUpdateService<UpdateServiceParameter<MakerReturnModel>, UpdateServiceResponse<MakerReturnModel>> {

    @Autowired
    private TMakerReturnRepository makerReturnRepository;

    @Autowired
    private MakerReturnComponent makerReturnComponent;

    @Override
    protected UpdateServiceResponse<MakerReturnModel> execute(final UpdateServiceParameter<MakerReturnModel> serviceParameter) {
        final MakerReturnModel makerReturnRequestValue = serviceParameter.getItem();

        final String voucherNumber = makerReturnRequestValue.getVoucherNumber();
        final List<TMakerReturnEntity> dbMakerReturns = makerReturnRepository
                .findByVoucherNumberAndOrderId(voucherNumber, makerReturnRequestValue.getMakerReturnProducts().get(0).getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // バリデーションチェック
        final ResultMessages rsltMsg = checkValidate(makerReturnRequestValue, dbMakerReturns);
        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }

        final List<BigInteger> updateTargetIds = makerReturnRequestValue.getMakerReturnProducts().stream()
                .map(MakerReturnProductCompositeModel::getId)
                .collect(Collectors.toList());

        final List<TMakerReturnEntity> entities = toEntitiesForUpdate(makerReturnRequestValue, dbMakerReturns, updateTargetIds);
        // メーカー返品情報の更新
        makerReturnRepository.saveAll(entities);

        deleteNotUpdatedRecords(dbMakerReturns, updateTargetIds, serviceParameter.getLoginUser().getUserId());

        // レスポンス作成
        final MakerReturnModel res = makerReturnComponent.toResponseModel(entities);

        return UpdateServiceResponse.<MakerReturnModel>builder().item(res).build();
    }

    /**
     * メーカー返品更新バリデーションチェックを行う.
     * @param makerReturnRequestValue メーカー返品リクエスト値
     * @param dbMakerReturns DB登録済みのメーカー返品情報リスト
     * @return ResultMessages
     */
    private ResultMessages checkValidate(final MakerReturnModel makerReturnRequestValue, final List<TMakerReturnEntity> dbMakerReturns) {
        final ResultMessages rsltMsg = ResultMessages.warning();
        makerReturnComponent.checkValidateAtUpsert(makerReturnRequestValue, rsltMsg);
        makerReturnComponent.checkValidateAtUpdate(dbMakerReturns, rsltMsg);

        return rsltMsg;
    }

    /**
     * @param makerReturnRequestValue メーカー返品リクエスト値
     * @param dbMakerReturns DB登録済みのメーカー返品情報リスト
     * @param updateTargetIds 更新対象IDリスト
     * @return 更新用Entityリスト
     */
    private List<TMakerReturnEntity> toEntitiesForUpdate(
            final MakerReturnModel makerReturnRequestValue,
            final List<TMakerReturnEntity> dbMakerReturns,
            final List<BigInteger> updateTargetIds) {

        return dbMakerReturns.stream()
                .filter(db -> updateTargetIds.contains(db.getId()))
                .map(db -> generateRequestToDbRecord(makerReturnRequestValue, db))
                .collect(Collectors.toList());
    }

    /**
     * @param makerReturnRequestValue メーカー返品リクエスト値
     * @param dbMakerReturn DB登録済みのメーカー返品情報
     * @return リクエスト値を設定したTMakerReturnEntity
     */
    private TMakerReturnEntity generateRequestToDbRecord(
            final MakerReturnModel makerReturnRequestValue,
            final TMakerReturnEntity dbMakerReturn) {
        dbMakerReturn.setReturnAt(makerReturnRequestValue.getReturnAt());
        dbMakerReturn.setMemo(makerReturnRequestValue.getMemo());
        dbMakerReturn.setMdfStaffCode(makerReturnRequestValue.getMdfStaffCode());

        makerReturnRequestValue.getMakerReturnProducts().stream()
        .filter(reqP -> reqP.getId().equals(dbMakerReturn.getId()))
        .findFirst()
        .ifPresent(reqP -> dbMakerReturn.setReturnLot(reqP.getReturnLot()));

        return dbMakerReturn;
    }

    /**
     * リクエストの伝票番号に紐づくDBのレコード中からリクエストになかった(更新しなかった)レコードを削除する.
     * @param dbMakerReturns リクエストの伝票番号に紐づくDBのメーカー返品情報レコードリスト
     * @param updateTargetIds 更新対象IDリスト
     * @param userId ユーザーID
     */
    private void deleteNotUpdatedRecords(
            final List<TMakerReturnEntity> dbMakerReturns,
            final List<BigInteger> updateTargetIds,
            final BigInteger userId) {
        if (dbMakerReturns.size() == updateTargetIds.size()) {
            return;
        }

        final List<BigInteger> deleteIds = dbMakerReturns.stream()
                .map(TMakerReturnEntity::getId)
                .filter(dbId -> !updateTargetIds.contains(dbId))
                .collect(Collectors.toList());

        makerReturnRepository.updateDeletedAtByIds(deleteIds, userId);
    }
}
