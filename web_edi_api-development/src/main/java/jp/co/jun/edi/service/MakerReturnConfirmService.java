package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.MNumberComponent;
import jp.co.jun.edi.entity.TMakerReturnEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.entity.constants.NumberConstants;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.MakerReturnInstructionListModel;
import jp.co.jun.edi.model.MakerReturnInstructionModel;
import jp.co.jun.edi.repository.TMakerReturnRepository;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.service.parameter.ApprovalServiceParameter;
import jp.co.jun.edi.service.response.ApprovalServiceResponse;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.MNumberColumnNameType;
import jp.co.jun.edi.type.MNumberTableNameType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.WmsLinkingStatusType;

/**
 * LG送信(メーカー返品)用Service.
 */
@Service
public class MakerReturnConfirmService extends GenericUpdateService<ApprovalServiceParameter<MakerReturnInstructionListModel>, ApprovalServiceResponse> {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private TMakerReturnRepository makerReturnRepository;

    @Autowired
    private TWmsLinkingFileRepository wmsLinkingFileRepository;

    // PRD_0073 del SIT start
    //@Autowired
    //private TReturnVoucherRepository returnVoucherRepository;
    // PRD_0073 del SIT end

    @Autowired
    private MNumberComponent numberComponent;

    @Override
    protected ApprovalServiceResponse execute(final ApprovalServiceParameter<MakerReturnInstructionListModel> serviceParameter) {
        final MakerReturnInstructionListModel makerReturnInstructionListModel = serviceParameter.getItem();

        final List<MakerReturnInstructionModel> reqMakerReturns = makerReturnInstructionListModel.getMakerReturnConfirms();

        // 各リクエストパラメータのリスト(重複除去)
        final List<BigInteger> orderIds =
                makerReturnInstructionListModel.getMakerReturnConfirms()
                .stream()
                .map(MakerReturnInstructionModel::getOrderId)
                .distinct()
                .collect(Collectors.toList());
        final List<String> voucherNumbers =
                makerReturnInstructionListModel.getMakerReturnConfirms()
                .stream()
                .map(MakerReturnInstructionModel::getVoucherNumber)
                .distinct()
                .collect(Collectors.toList());

        // リクエストパラメータに該当する納品情報、メーカー返品情報取得
        final List<TMakerReturnEntity> noFilterDbMakerReturns = makerReturnRepository.findByLgKeyList(orderIds, voucherNumbers).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // リクエストのキーと合致するレコード抽出
        final List<TMakerReturnEntity> dbMakerReturns = noFilterDbMakerReturns.stream()
                .filter(m -> existsMatchKey(reqMakerReturns, m)).collect(Collectors.toList());

        // バリデーションチェック
        final ResultMessages rsltMsg = checkValidate(reqMakerReturns, dbMakerReturns);
        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }

        // 倉庫連携ファイル情報登録
        final TWmsLinkingFileEntity wmsLinkingFileEntity = insertWmsLinkingFile();

        // 更新
        confirm(dbMakerReturns, wmsLinkingFileEntity, serviceParameter.getLoginUser().getUserId());

        return ApprovalServiceResponse.builder().build();
    }

    /**
     * @param reqMakerReturns リクエストの仕入リスト
     * @param dbMakerReturns DBのメーカー返品情報
     * @return リクエストのキーと合致するメーカー返品情報
     */
    private boolean existsMatchKey(final List<MakerReturnInstructionModel> reqMakerReturns, final TMakerReturnEntity dbMakerReturns) {
        return reqMakerReturns.stream().anyMatch(m -> isMatchLgKey(dbMakerReturns, m));
    }

    /**
     * @param dbMakerReturns DBのメーカー返品情報
     * @param req リクエストパラメータ
     * @return true:LG送信のキーが同じ
     */
    private boolean isMatchLgKey(final TMakerReturnEntity dbMakerReturns, final MakerReturnInstructionModel req) {
        return dbMakerReturns.getOrderId().equals(req.getOrderId())
                && dbMakerReturns.getVoucherNumber().equals(req.getVoucherNumber());
    }

    /**
     * バリデーションチェックを行う.
     * @param requestList リクエストデータリスト
     * @param dbMakerReturns DB登録済のメーカー返品情報リスト
     * @return ResultMessages
     */
    private ResultMessages checkValidate(
            final List<MakerReturnInstructionModel> requestList,
            final List<TMakerReturnEntity> dbMakerReturns) {

        final ResultMessages rsltMsg = ResultMessages.warning();

        // DBから取得できなければエラー
        if (dbMakerReturns.isEmpty()) {
            return ResultMessages.warning().add(MessageCodeType.CODE_002);
        }

        // メーカー返品情報のバリデーション
        requestList.stream().forEach(r -> addIfMakerReturnsError(r, dbMakerReturns, rsltMsg));

        return rsltMsg;
    }

    /**
     * エラーがあればエラー情報を追加する.
     * @param req リクエストパラメータ
     * @param dbMakerReturns DBのメーカー返品情報リスト
     * @param rsltMsg エラーメッセージ
     */
    private void addIfMakerReturnsError(
            final MakerReturnInstructionModel req,
            final List<TMakerReturnEntity> dbMakerReturns,
            final ResultMessages rsltMsg) {

        final List<TMakerReturnEntity> filteredList = filterByLgKey(req, dbMakerReturns);

        // 存在しない場合エラー
        if (filteredList.isEmpty()) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_MR_005, getMessage("code.400_MR_05", req.getVoucherNumber(), req.getOrderId())));
            return;
        }

        // LG送信指示済みの場合エラー
        final boolean exitstLgErr = filteredList.stream().anyMatch(lg -> LgSendType.INSTRUCTION == lg.getLgSendType());
        if (exitstLgErr) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_MR_002, getMessage("code.400_MR_06", req.getVoucherNumber(), req.getOrderId())));
        }
    }

    /**
     * @param req リクエストパラメータ
     * @param makerReturns DBのメーカー返品情報リスト
     * @return LG送信のキーで抽出したメーカー返品情報リスト
     */
    private List<TMakerReturnEntity> filterByLgKey(final MakerReturnInstructionModel req, final List<TMakerReturnEntity> makerReturns) {
        return makerReturns.stream().filter(p -> isMatchLgKey(p, req)).collect(Collectors.toList());
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
     * メーカー返品情報更新.
     * LG送信済に更新.
     * @param dbMakerReturns リクエストのキーで抽出したDBのメーカー返品情報リスト
     * @param wmsLinkingFileEntity 登録済の倉庫連携ファイル情報
     * @param userId ユーザーID
     */
    private void confirm(
            final List<TMakerReturnEntity> dbMakerReturns,
            final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final BigInteger userId) {

        sortForUpdate(dbMakerReturns);
        prepareSaveData(dbMakerReturns, wmsLinkingFileEntity, userId);
        makerReturnRepository.saveAll(dbMakerReturns);

    }

    /**
     * 更新用データの作成.
     * @param makerReturns 更新用のメーカー返品情報Entityリスト(リクエストのキーで抽出したDBのメーカー返品情報リスト)
     * @param wmsLinkingFileEntity 登録済の倉庫連携ファイル情報
     * @param userId ユーザーID
     */
    private void prepareSaveData(final List<TMakerReturnEntity> makerReturns, final TWmsLinkingFileEntity wmsLinkingFileEntity, final BigInteger userId) {

        final Date currentDate = new Date();

        TMakerReturnEntity preMakerReturn = null; // 前回ループで処理したメーカー返品情報
        int seqCnt = 1; // リクエスト単位の連番
        int seqCntByNumber = 1; // キー単位の連番
        String instructNo = StringUtils.EMPTY;    // 指示番号

        for (final TMakerReturnEntity p: makerReturns) {
            if (notMatchLgKey(preMakerReturn, p)) {

                // 指示番号採番
                instructNo = numberComponent.createNumberSetZeroPadding(MNumberTableNameType.T_MAKER_RETURN,
                                                                        MNumberColumnNameType.INSTRUCT_NUMBER,
                                                                        NumberConstants.INSTRUCT_NUMBER_LENGTH);
                // カウントを初期化
                seqCntByNumber = 1;

                // PRD_0073 del SIT start
                //// 返品伝票管理登録
                //insertReturnsVoucher(p.getVoucherNumber(), p.getOrderId());
                // PRD_0073 del SIT end
            }

            // 倉庫連携ファイル情報ID
            p.setWmsLinkingFileId(wmsLinkingFileEntity.getId());

            // 日付
            p.setManageDate(currentDate);

            // 時間
            p.setManageAt(currentDate);

            // 採番
            p.setManageNumber(wmsLinkingFileEntity.getManageNumber());

            // 連番
            p.setSequence(seqCnt);

            // 指示番号
            p.setInstructNumber(instructNo);
            // 指示番号行
            p.setInstructNumberLine(seqCntByNumber);

            // LG
            p.setLgSendType(LgSendType.INSTRUCTION);

            // 更新ユーザID
            p.setCreatedUserId(userId);

            seqCnt = seqCnt + 1;
            seqCntByNumber = seqCntByNumber + 1;
            preMakerReturn = p;
        }

    }

    /**
     * 更新順にソート.
     * @param list LG送信対象リスト
     */
    private void sortForUpdate(final List<TMakerReturnEntity> list) {
        Collections.sort(list, Comparator.comparing(TMakerReturnEntity::getOrderId)
                .thenComparing(TMakerReturnEntity::getVoucherNumber)
                .thenComparing(TMakerReturnEntity::getId));
    }

    /**
     * @param preMakerReturn 前回のループで処理したメーカー返品情報
     * @param makerReturn 現在のループで処理中のメーカー返品情報
     * @return true:発注番号と伝票番号が不一致
     */
    private boolean notMatchLgKey(final TMakerReturnEntity preMakerReturn, final TMakerReturnEntity makerReturn) {
        return preMakerReturn == null
                || !preMakerReturn.getOrderId().equals(makerReturn.getOrderId())
                || !preMakerReturn.getVoucherNumber().equals(makerReturn.getVoucherNumber());
    }

    // PRD_0073 del SIT start
    ///**
    // * 返品伝票管理情報情報登録.
    // * @param voucherNumber 伝票場合
    // * @param orderId 発注ID
    // */
    //private void insertReturnsVoucher(final String voucherNumber, final BigInteger orderId) {
    //    final TReturnVoucherEntity entity = new TReturnVoucherEntity();
    //
    //    entity.setVoucherNumber(voucherNumber);
    //    entity.setOrderId(orderId);
    //    entity.setStatus(SendMailStatusType.UNPROCESSED);
    //
    //    returnVoucherRepository.save(entity);
    //}
    // PRD_0073 del SIT end

    /**
     * 倉庫連携ファイル情報登録.
     * @return 登録された倉庫連携ファイル情報
     */
    private TWmsLinkingFileEntity insertWmsLinkingFile() {

        // 管理Noを取得
        String manageNo = numberComponent.createNumberSetZeroPadding(MNumberTableNameType.T_MAKER_RETURN,
                                                                      MNumberColumnNameType.MANAGE_NUMBER,
                                                                      NumberConstants.CONTROL_NUMBER_LENGTH);


        final TWmsLinkingFileEntity entity = new TWmsLinkingFileEntity();
        // PRD_0089 mod JFE start
        entity.setBusinessType(BusinessType.RETURN_INSTRUCTION);
//        entity.setBusinessType(BusinessType.PURCHASE_INSTRUCTION);
        // PRD_0089 mod JFE end
        entity.setManageNumber(manageNo);
        entity.setWmsLinkingStatus(WmsLinkingStatusType.FILE_NOT_CREATE);

        wmsLinkingFileRepository.save(entity);

        return entity;
    }
}
