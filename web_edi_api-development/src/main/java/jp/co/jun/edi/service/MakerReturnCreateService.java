package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.MNumberComponent;
import jp.co.jun.edi.component.MakerReturnComponent;
import jp.co.jun.edi.component.ShipmentComponent;
import jp.co.jun.edi.entity.TMakerReturnEntity;
import jp.co.jun.edi.entity.constants.NumberConstants;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.MakerReturnModel;
import jp.co.jun.edi.model.MakerReturnProductCompositeModel;
import jp.co.jun.edi.repository.TMakerReturnRepository;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.CreateServiceResponse;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.MNumberColumnNameType;
import jp.co.jun.edi.type.MNumberTableNameType;
import jp.co.jun.edi.util.StringUtils;

/**
 * メーカー返品情報を作成するサービス.
 */
@Service
public class MakerReturnCreateService
extends GenericCreateService<CreateServiceParameter<MakerReturnModel>, CreateServiceResponse<MakerReturnModel>> {

    @Autowired
    private TMakerReturnRepository makerReturnRepository;

    @Autowired
    private MakerReturnComponent makerReturnComponent;

    @Autowired
    private MNumberComponent numberComponent;

    @Autowired
    private ShipmentComponent shipmentComponent;

    @Override
    protected CreateServiceResponse<MakerReturnModel> execute(final CreateServiceParameter<MakerReturnModel> serviceParameter) {
        final MakerReturnModel request = serviceParameter.getItem();

        // バリデーションチェック
        final ResultMessages rsltMsg = ResultMessages.warning();
        makerReturnComponent.checkValidateAtUpsert(request, rsltMsg);
        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }

        final List<TMakerReturnEntity> makerReturnEntities = toEntitiesForInsert(request);

        // メーカー返品情報の登録
        makerReturnRepository.saveAll(makerReturnEntities);

        // レスポンス作成
        final MakerReturnModel res = makerReturnComponent.toResponseModel(makerReturnEntities);

        return CreateServiceResponse.<MakerReturnModel>builder().item(res).build();
    }

    /**
     * @param makerReturnRequestValue メーカー返品リクエスト値
     * @return Insert用のentityリスト
     */
    private List<TMakerReturnEntity> toEntitiesForInsert(final MakerReturnModel makerReturnRequestValue) {

        // 発注Id順にソート
        sortForInsert(makerReturnRequestValue.getMakerReturnProducts());

        MakerReturnProductCompositeModel preRecord = null; // 前回ループで処理したメーカー返品情報
        int seq = 1; // キー単位の連番
        BigInteger voucherNo = new BigInteger("1");

        final String logisticsCode = shipmentComponent.extraxtOldLogisticsCode(makerReturnRequestValue.getDistaCode());

        final List<TMakerReturnEntity> entities = new ArrayList<>();

        for (final MakerReturnProductCompositeModel currentRecord: makerReturnRequestValue.getMakerReturnProducts()) {
            if (notMatchKey(preRecord, currentRecord)) { // 新しいキー.新規採番
                voucherNo = numberComponent.createNumber(MNumberTableNameType.T_MAKER_RETURN, MNumberColumnNameType.VOUCHER_NUMBER);
                seq = 1;
            }

            final TMakerReturnEntity entity = new TMakerReturnEntity();
            BeanUtils.copyProperties(makerReturnRequestValue, entity);
            BeanUtils.copyProperties(currentRecord, entity);
            entity.setLogisticsCode(logisticsCode);
            entity.setVoucherNumber(StringUtils.toStringPadding0(voucherNo, NumberConstants.NUMBER_LENGTH));
            entity.setVoucherLine(seq);
            entity.setLgSendType(LgSendType.NO_INSTRUCTION);

            entities.add(entity);

            seq = seq + 1;
            preRecord = currentRecord;
        }

        return entities;
    }

    /**
     * Insert順にソート.
     * @param makerReturnProductRequestValues メーカー返品商品リクエスト値リスト
     */
    private void sortForInsert(final List<MakerReturnProductCompositeModel> makerReturnProductRequestValues) {
        Collections.sort(makerReturnProductRequestValues, Comparator.comparing(MakerReturnProductCompositeModel::getOrderId)
                .thenComparing(MakerReturnProductCompositeModel::getColorCode)
                .thenComparing(MakerReturnProductCompositeModel::getSize));
    }

    /**
     * @param pre 前回のループで処理したメーカー返品情報
     * @param current 現在のループで処理中のメーカー返品情報
     * @return true:キーが不一致
     */
    private boolean notMatchKey(final MakerReturnProductCompositeModel pre, final MakerReturnProductCompositeModel current) {
        return pre == null
                || !pre.getOrderId().equals(current.getOrderId());
    }
}
