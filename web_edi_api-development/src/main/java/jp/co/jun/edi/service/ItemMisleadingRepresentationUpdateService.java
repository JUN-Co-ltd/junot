package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.MisleadingRepresentationComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.mail.ItemMisleadingRepresentationUpdateSendMailComponent;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.TMisleadingRepresentationEntity;
import jp.co.jun.edi.entity.TSkuEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderSupplierEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.ItemMisleadingRepresentationModel;
import jp.co.jun.edi.model.MisleadingRepresentationModel;
import jp.co.jun.edi.model.mail.ItemMisleadingRepresentationUpdateSendModel;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TMisleadingRepresentationRepository;
import jp.co.jun.edi.repository.TSkuRepository;
import jp.co.jun.edi.repository.extended.ExtendedTItemRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderSupplierRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;
import jp.co.jun.edi.type.LinkingStatusType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.MisleadingRepresentationType;
import jp.co.jun.edi.type.QualityApprovalType;
import jp.co.jun.edi.util.DateUtils;

/**
 * 品番・優良誤認情報更新処理.
 */
@Service
public class ItemMisleadingRepresentationUpdateService extends GenericUpdateService<UpdateServiceParameter<ItemMisleadingRepresentationModel>,
UpdateServiceResponse<ItemMisleadingRepresentationModel>> {

    @Autowired
    private TMisleadingRepresentationRepository misleadingRepresentationRepository;

    @Autowired
    private TItemRepository itemRepository;

    @Autowired
    private ExtendedTItemRepository exTItemRepository;

    @Autowired
    private TSkuRepository skuRepository;

    @Autowired
    private ExtendedTOrderSupplierRepository extendedTOrderSupplierRepository;

    @Autowired
    private MisleadingRepresentationComponent misleadingRepresentationComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private ItemMisleadingRepresentationUpdateSendMailComponent itemMisleadingRepresentationUpdateSendMailComponent;

    @Autowired
    private ItemComponent itemComponent;

    private static final Map<QualityApprovalType, String> QUALITY_APPROVAL_TEXT = new HashMap<QualityApprovalType, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(QualityApprovalType.NON_TARGET, "対象外");
            put(QualityApprovalType.TARGET, "対象");
            put(QualityApprovalType.PART, "一部承認");
            put(QualityApprovalType.ACCEPT, "承認済");
        }
    };

    @Override
    protected UpdateServiceResponse<ItemMisleadingRepresentationModel>
    execute(final UpdateServiceParameter<ItemMisleadingRepresentationModel> serviceParameter) {

        final List<MisleadingRepresentationModel> inputMisleadingRepresentationList = serviceParameter.getItem().getMisleadingRepresentations();
        final BigInteger id = serviceParameter.getItem().getId();
        final CustomLoginUser loginUser = serviceParameter.getLoginUser();

        // DBから最新の品番情報を取得する
        // 存在しない(削除済み)の場合は業務エラー
        final TItemEntity itemEntity = itemRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // チェック処理
        checkBusinessValid(inputMisleadingRepresentationList,
                serviceParameter.getPreItem().getMisleadingRepresentations(),
                itemEntity);

        // 登録用優良誤認情報作成
        final List<TMisleadingRepresentationEntity> saveTMisleadingRepresentationEntity =
                generateEntityForInsert(id, inputMisleadingRepresentationList, loginUser);

        // 優良誤認情報を登録
        misleadingRepresentationRepository.saveAll(saveTMisleadingRepresentationEntity);

        // 品番を更新
        saveItem(itemEntity, inputMisleadingRepresentationList);

        // メールを送信
        final ItemMisleadingRepresentationUpdateSendModel sendModel = generateSendMailModel(itemEntity.getId(), saveTMisleadingRepresentationEntity);
        itemMisleadingRepresentationUpdateSendMailComponent.sendMail(sendModel, loginUser.getAccountName());

        // 返却値
        final ItemMisleadingRepresentationModel responseModel = new ItemMisleadingRepresentationModel();
        responseModel.setId(itemEntity.getId());

        return UpdateServiceResponse.<ItemMisleadingRepresentationModel>builder().item(responseModel).build();
    }

    /**
     * 登録用Entity作成.
     *
     * @param partNoId 品番ID
     * @param inputMisleadingRepresentationList 画面上入力された優良誤認情報のリスト
     * @param loginUser ログインユーザ情報
     * @return 登録用優良誤認情報Entityのリスト
     */
    private List<TMisleadingRepresentationEntity> generateEntityForInsert(
            final BigInteger partNoId,
            final List<MisleadingRepresentationModel> inputMisleadingRepresentationList,
            final CustomLoginUser loginUser) {

        // DBに登録されている優良誤認情報を取得
        final List<TMisleadingRepresentationEntity> currentMisleadingRepresentationList =
                misleadingRepresentationRepository.findByPartNoId(partNoId, PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        // modelをEntityに詰め替える
        return inputMisleadingRepresentationList.stream()
                .map(model -> generateMisleadingRepresentationEntity(model, currentMisleadingRepresentationList, loginUser))
                .collect(Collectors.toList());

    }

    /**
     * 優良誤認承認のEntityを作成.
     *
     * @param inputModelModel 優良誤認承認モデル
     * @param currentModelList 優良誤認承認Entityのリスト
     * @param loginUser ログインユーザ情報
     * @return 優良誤認承認Entity
     */
    private TMisleadingRepresentationEntity generateMisleadingRepresentationEntity(
            final MisleadingRepresentationModel inputModelModel,
            final List<TMisleadingRepresentationEntity> currentModelList,
            final CustomLoginUser loginUser) {

        final Optional<TMisleadingRepresentationEntity> optEntity = currentModelList.stream()
                .filter(entity -> entity.getId().compareTo(inputModelModel.getId()) == 0)
                .findFirst();

        if (!optEntity.isPresent()) {
            // 対応するDBの登録データが無い時はException
            throw new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002));
        }

        return generateEntityForUpdate(inputModelModel, optEntity.get(), loginUser);
    }

    /**
     * チェック処理.
     * @param inputModelList 入力された優良誤認情報のリスト
     * @param preModelList 変更前優良誤認情報リスト
     * @param itemEntity 品番情報
     */
    private void checkBusinessValid(
            final List<MisleadingRepresentationModel> inputModelList,
            final List<MisleadingRepresentationModel> preModelList,
            final TItemEntity itemEntity) {

        // 外部連携区分:JUNoT登録以外の場合、更新不可
        itemComponent.validateReadOnly(itemEntity.getExternalLinkingType());

        // 承認対象のデータが無ければエラー
        if (CollectionUtils.isEmpty(inputModelList)) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_002));
        }

        // 全て承認対象外の時はエラー
        if (!misleadingRepresentationComponent.isMisleadingRepresentationTarget(itemEntity)) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_Q_001));
        }

        // 入力データチェック
        final List<ResultMessage> errorResultMessageList = checkInputValid(itemEntity, inputModelList, preModelList);

        if (CollectionUtils.isNotEmpty(errorResultMessageList)) {
            throw new BusinessException(ResultMessages.warning().addAll(errorResultMessageList));
        }
    }

    /**
     * 入力値チェック.
     *
     * @param itemEntity 品番情報
     * @param inputModelList 画面から入力された優良誤認情報Modelのリスト
     * @param preModelList 変更前優良誤認情報Modelのリスト
     * @return エラーコードリスト
     */
    private List<ResultMessage> checkInputValid(final TItemEntity itemEntity,
            final List<MisleadingRepresentationModel> inputModelList,
            final List<MisleadingRepresentationModel> preModelList) {

        final List<ResultMessage> errorResultMessageList = new ArrayList<>();

        // 原産国チェック
        final List<MisleadingRepresentationModel> cooInput =
                filterMisleadingRepresentationType(MisleadingRepresentationType.COUNTRY_OF_ORIGIN, inputModelList);
        final MisleadingRepresentationModel cooPre =
                filterMisleadingRepresentationType(MisleadingRepresentationType.COUNTRY_OF_ORIGIN, preModelList).get(0);
        final List<ResultMessage> cooMsgs = checkCooValid(itemEntity, cooInput, cooPre);
        errorResultMessageList.addAll(cooMsgs);

        // 組成チェック
        final List<MisleadingRepresentationModel> comInput =
                filterMisleadingRepresentationType(MisleadingRepresentationType.COMPOSITION, inputModelList);
        final List<MisleadingRepresentationModel> comPre =
                filterMisleadingRepresentationType(MisleadingRepresentationType.COMPOSITION, preModelList);
        final List<ResultMessage> comMsgs = checkCompositionValid(itemEntity, comInput, comPre);
        errorResultMessageList.addAll(comMsgs);

        // 有害物質のチェック
        final List<MisleadingRepresentationModel> harmInput =
                filterMisleadingRepresentationType(MisleadingRepresentationType.HARMFUL_STATUS, inputModelList);
        final MisleadingRepresentationModel harmPre =
                filterMisleadingRepresentationType(MisleadingRepresentationType.HARMFUL_STATUS, preModelList).get(0);
        final List<ResultMessage> harmMsgs = checkHarmfulStatusValid(itemEntity, harmInput, harmPre);
        errorResultMessageList.addAll(harmMsgs);

        return errorResultMessageList;
    }

    /**
     * 原産国チェック.
     *
     * @param itemEntity DBに登録されている品番情報
     * @param inputCooModelList 画面から入力された優良誤認情報のリスト
     * @param preCooModel 変更前原産国優良誤認情報
     * @return エラーチェック内容リスト
     */
    private List<ResultMessage> checkCooValid(
            final TItemEntity itemEntity,
            final List<MisleadingRepresentationModel> inputCooModelList,
            final MisleadingRepresentationModel preCooModel) {

        final List<ResultMessage> errorResultMessageList = new ArrayList<>();

        if (CollectionUtils.size(inputCooModelList) != 1) {
            // 原産国の承認データが1件以外
            errorResultMessageList.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_002));
            return errorResultMessageList;
        }

        final MisleadingRepresentationModel cooModel = inputCooModelList.get(0);

        if (!DateUtils.isSameTimestanp(cooModel.getUpdatedAt(), preCooModel.getUpdatedAt())) {
            // 変更前と差異がある場合
            errorResultMessageList.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_003, cooModel));
        }

        // データ整合チェック
        if (!isCooMismatchApproval(itemEntity.getCooCode(), cooModel)) {
            // 優良誤認対象の時
            errorResultMessageList.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_004, cooModel));
        }

        return errorResultMessageList;
    }

    /**
     * 原産国の承認データチェック.
     * <pre>
     *  品番の原産国の承認要否と画面入力値の状態をチェックする
     *
     *  品番の原産国が承認対象の場合は、承認/未承認どちらでも可
     *  品番の原産国が承認対象外の場合は、未承認のみ可
     * </pre>
     *
     * @param itemCooCode 品番の原産国コード
     * @param cooModel 入力値の原産国承認データ
     * @return true:データ整合　false:データ不整合
     */
    private boolean isCooMismatchApproval(final String itemCooCode, final MisleadingRepresentationModel cooModel) {

        // 品番の原産国が優良誤認対象の場合は、承認/未承認のどちらでも可
        if (misleadingRepresentationComponent.isQualityCooTarget(itemCooCode)) {
            return true;
        }

        // 品番の原産国が対象外の場合、承認は不可
        if (isApproval(cooModel)) {
            return false;
        }

        return true;
    }

    /**
     * 組成のチェック処理.
     *
     * @param itemEntity 品番情報
     * @param inputModelList 組成のリスト
     * @param preModelList 組成の変更前優良誤認情報リスト
     * @return エラーコードリスト
     */
    private List<ResultMessage> checkCompositionValid(
            final TItemEntity itemEntity,
            final List<MisleadingRepresentationModel> inputModelList,
            final List<MisleadingRepresentationModel> preModelList) {

        final List<ResultMessage> errorResultMessageList = new ArrayList<ResultMessage>();

        if (CollectionUtils.isEmpty(inputModelList) || CollectionUtils.isEmpty(preModelList)) {
            errorResultMessageList.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_005));
            return errorResultMessageList;
        }

        // 変更前情報との比較
        if (inputModelList.size() != preModelList.size()) {
            errorResultMessageList.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_010, inputModelList));
            return errorResultMessageList;
        }

        inputModelList.stream().forEach(inputModel -> {
            final MisleadingRepresentationModel preModel =  preModelList.stream()
                    .filter(model -> model.getId().compareTo(inputModel.getId()) == 0)
                    .findFirst()
                    .orElse(null);

            if (!DateUtils.isSameTimestanp(inputModel.getUpdatedAt(), preModel.getUpdatedAt())) {
                // 変更前と差異がある場合
                errorResultMessageList.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_006, inputModel));
            }
        });

        return errorResultMessageList;
    }

    /**
     * 有害物質のバリデーション.
     * @param itemEntity 品番情報
     * @param inputModelList 有害物質のリスト
     * @param preModel 変更前有害物質のリスト
     * @return エラーコードリスト
     */
    private List<ResultMessage> checkHarmfulStatusValid(
            final TItemEntity itemEntity,
            final List<MisleadingRepresentationModel> inputModelList,
            final MisleadingRepresentationModel preModel) {

        final List<ResultMessage> errorResultMessageList = new ArrayList<>();

        if (CollectionUtils.size(inputModelList) != 1) {
            // 有害物質の承認データが1件以外
            errorResultMessageList.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_007));
            return errorResultMessageList;
        }

        final MisleadingRepresentationModel inputModel = inputModelList.get(0);

        if (!DateUtils.isSameTimestanp(inputModel.getUpdatedAt(), preModel.getUpdatedAt())) {
            // 変更前と差異がある場合
            errorResultMessageList.add(ResultMessage.fromCode(MessageCodeType.CODE_Q_008, inputModel));
        }

        return errorResultMessageList;
    }

    /**
     * 入力値の優良誤認承認状態.
     * 承認日もしくは承認アカウント名のどちらかが入っていれば承認
     *
     * @param misleadingRepresentationModel 優良誤認承認データ
     * @return true:承認 false:未承認
     */
    private boolean isApproval(final MisleadingRepresentationModel misleadingRepresentationModel) {

        if (StringUtils.isNotEmpty(misleadingRepresentationModel.getApprovalUserAccountName())
                || !Objects.isNull(misleadingRepresentationModel.getApprovalAt())) {
            return true;
        }

        return false;
    }

    /**
     * 品番情報更新.
     * @param itemEntity 品番情報
     * @param misleadingRepresentationModelList 優良誤認情報
     */
    private void saveItem(final TItemEntity itemEntity, final List<MisleadingRepresentationModel> misleadingRepresentationModelList) {

        // 原産国
        final MisleadingRepresentationModel cooModel =
                filterMisleadingRepresentationType(MisleadingRepresentationType.COUNTRY_OF_ORIGIN, misleadingRepresentationModelList)
                .get(0);

        // 組成リスト
        final List<MisleadingRepresentationModel> compositonList =
                filterMisleadingRepresentationType(MisleadingRepresentationType.COMPOSITION, misleadingRepresentationModelList);

        // 有害物質
        final MisleadingRepresentationModel harmfulStatusModel =
                filterMisleadingRepresentationType(MisleadingRepresentationType.HARMFUL_STATUS, misleadingRepresentationModelList)
                .get(0);

        // 原産国の承認ステータスをセット
        final QualityApprovalType cooAppType = QualityApprovalType.convertToType(itemEntity.getQualityCooStatus());
        final int cooStatus = convertMisleadingRepresentation(cooAppType, cooModel).getValue();
        itemEntity.setQualityCooStatus(cooStatus);

        // 組成の承認ステータスをセット
        final QualityApprovalType comAppType = QualityApprovalType.convertToType(itemEntity.getQualityCompositionStatus());
        final int comStatus = convertMisleadingRepresentation(itemEntity.getId(), comAppType, compositonList).getValue();
        itemEntity.setQualityCompositionStatus(comStatus);

        // 有害物質の承認ステータスをセット
        final QualityApprovalType harmAppType = QualityApprovalType.convertToType(itemEntity.getQualityHarmfulStatus());
        final int harmStatus = convertMisleadingRepresentation(harmAppType, harmfulStatusModel).getValue();
        itemEntity.setQualityHarmfulStatus(harmStatus);

        // 連携ステータス初期化
        itemEntity.setLinkingStatus(LinkingStatusType.TARGET);

        // 品番情報を更新
        itemRepository.save(itemEntity);
    }

    /**
     * 承認状態から優良誤認承認ステータスを取得する.
     *
     * @param itemQualityApprovalType 品番の優良誤認状態
     * @param mr 承認状態がセットされた優良誤認情報
     * @return QualityApprovalType 承認状態から判断した優良誤認承認ステータス
     */
    public QualityApprovalType convertMisleadingRepresentation(final QualityApprovalType itemQualityApprovalType,
            final MisleadingRepresentationModel mr) {

        // 対象外の場合は変換無し
        if (itemQualityApprovalType == QualityApprovalType.NON_TARGET) {
            return QualityApprovalType.NON_TARGET;
        }

        if (Objects.nonNull(mr.getApprovalAt()) && StringUtils.isNoneBlank(mr.getApprovalUserAccountName())) {
            // 承認
            return QualityApprovalType.ACCEPT;
        }

        return QualityApprovalType.TARGET;
    }

    /**
     * 承認状態から優良誤認承認ステータスを取得する.
     *
     * @param partNoId 品番ID
     * @param itemQualityApprovalType 品番の優良誤認状態
     * @param mrList 承認状態がセットされた優良誤認情報リスト
     * @return QualityApprovalType 承認状態から判断した優良誤認承認ステータス
     */
    public QualityApprovalType convertMisleadingRepresentation(
            final BigInteger partNoId,
            final QualityApprovalType itemQualityApprovalType,
            final List<MisleadingRepresentationModel> mrList) {

        // 対象外の場合は変換無し
        if (itemQualityApprovalType == QualityApprovalType.NON_TARGET) {
            return QualityApprovalType.NON_TARGET;
        }

        // 重複なしのカラーコードリスト
        final List<String> colorCodeList = skuRepository.findByPartNoId(partNoId,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("colorCode"))))
                .stream()
                .map(TSkuEntity::getColorCode)
                .distinct()
                .collect(Collectors.toList());

        final long count = mrList.stream()
                .filter(entity -> Objects.nonNull(entity.getApprovalAt()) && StringUtils.isNoneBlank(entity.getApprovalUserAccountName()))
                .count();

        if (count == colorCodeList.size()) {
            // カラーコードと承認の件数が同じ場合は承認
            return QualityApprovalType.ACCEPT;
        }

        if (count > 0) {
            // 承認の件数が1件以上の場合は一部承認
            return QualityApprovalType.PART;
        }

        return QualityApprovalType.TARGET;
    }

    /**
     * 指定した優良誤認対象区分の承認情報を取得する.
     *
     * @param modelList 優良誤認承認情報Modelのリスト
     * @param type フィルタリングするタイプ
     * @return フィルタリングした優良誤認承認情報Modelのリスト
     */
    private List<MisleadingRepresentationModel> filterMisleadingRepresentationType(
            final MisleadingRepresentationType type,
            final List<MisleadingRepresentationModel> modelList) {

        if (Objects.isNull(type)) {
            return modelList;
        }

        return modelList.stream()
                .filter(model -> model.getMisleadingRepresentationType() == type)
                .collect(Collectors.toList());
    }

    /**
     * 更新用Entity作成.
     * @param inputModel 画面で入力された優良誤認情報
     * @param currentEntity DBに登録されている優良誤認情報Entity
     * @param loginUser ログインユーザ情報
     * @return 登録用優良誤認情報Entity
     */
    private TMisleadingRepresentationEntity generateEntityForUpdate(
            final MisleadingRepresentationModel inputModel,
            final TMisleadingRepresentationEntity currentEntity,
            final CustomLoginUser loginUser) {

        Date approvalAt = null;
        String approvalUserAccountName = null;

        // 日付と承認者が両方入っている
        if (ObjectUtils.allNotNull(inputModel.getApprovalAt(), inputModel.getApprovalUserAccountName())) {
            if (DateUtils.isSameDay(inputModel.getApprovalAt(), currentEntity.getApprovalAt())) {
                // EntityとModelが同じ日付 → 日付の変更はしない。
                approvalAt = currentEntity.getApprovalAt();
                approvalUserAccountName = currentEntity.getApprovalUserAccountName();
            } else {
                // 異なる場合はチェックの付け直しなので、再セットする
                approvalAt = DateUtils.createNow();
                approvalUserAccountName = loginUser.getAccountName();
            }
        }

        // 承認日
        currentEntity.setApprovalAt(approvalAt);
        // 承認者
        currentEntity.setApprovalUserAccountName(approvalUserAccountName);
        // メモ
        currentEntity.setMemo(inputModel.getMemo());

        return currentEntity;
    }

    /**
     * 優良誤認更新メール用Model作成.
     *
     * @param partNoId 品番ID
     * @param saveTMisleadingRepresentationEntity 優良誤認情報
     * @return 優良誤認更新メール用Model
     */
    public ItemMisleadingRepresentationUpdateSendModel generateSendMailModel(
            final BigInteger partNoId,
            final List<TMisleadingRepresentationEntity> saveTMisleadingRepresentationEntity) {

        final ItemMisleadingRepresentationUpdateSendModel sendModel = new ItemMisleadingRepresentationUpdateSendModel();

        final ExtendedTItemEntity exItemEntity = exTItemRepository.findById(partNoId).orElse(new ExtendedTItemEntity());

        final List<String> memoList = saveTMisleadingRepresentationEntity.stream()
                .filter(entity -> !StringUtils.isEmpty(entity.getMemo()))
                .map(TMisleadingRepresentationEntity::getMemo)
                .collect(Collectors.toList());

        // 品番に紐づく最新のメーカーコードを取得
        final ExtendedTOrderSupplierEntity extendedTOrderSupplierEntity =
                extendedTOrderSupplierRepository.findById(exItemEntity.getCurrentProductOrderSupplierId()).orElseThrow(
                        () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // 品番ID
        sendModel.setPartNoId(exItemEntity.getId());
        // 品番
        sendModel.setPartNo(exItemEntity.getPartNo());
        // 品名
        sendModel.setProductName(exItemEntity.getProductName());
        // 生産メーカーコード
        sendModel.setMdfMakerCode(extendedTOrderSupplierEntity.getSupplierCode());
        // 生産メーカー名
        sendModel.setMdfMakerName(extendedTOrderSupplierEntity.getSupplierName());
        // 生産メーカー担当
        sendModel.setMdfMakerStaffId(extendedTOrderSupplierEntity.getSupplierStaffId());
        // 製造担当
        sendModel.setMdfStaffCode(exItemEntity.getMdfStaffCode());
        // 企画担当
        sendModel.setPlannerCode(exItemEntity.getPlannerCode());
        // パタンナー
        sendModel.setPatanerCode(exItemEntity.getPatanerCode());
        // 優良誤認承認区分（組成）
        sendModel.setQualityCompositionSstatusText(QUALITY_APPROVAL_TEXT.get(QualityApprovalType.convertToType(exItemEntity.getQualityCompositionStatus())));
        // 優良誤認承認区分（国）
        sendModel.setQualityCooStatusText(QUALITY_APPROVAL_TEXT.get(QualityApprovalType.convertToType(exItemEntity.getQualityCooStatus())));
        // 優良誤認承認区分（有害物質）
        sendModel.setQualityHarmfulStatusText(QUALITY_APPROVAL_TEXT.get(QualityApprovalType.convertToType(exItemEntity.getQualityHarmfulStatus())));
        // 備考
        sendModel.setMemoList(memoList);
        // URL
        sendModel.setUrl(propertyComponent.getCommonProperty().getJunotUrl());
        // 件名接頭辞
        sendModel.setSubjectPrefix(propertyComponent.getCommonProperty().getSendMailTemplateEmbeddedCharacterSubjectPrefix());

        return sendModel;
    }
}
