package jp.co.jun.edi.component.item;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.GenericComponent;
import jp.co.jun.edi.component.ItemArticleNumberValidateComponent;
import jp.co.jun.edi.component.JunpcHinmstComponent;
import jp.co.jun.edi.component.MItemComponent;
import jp.co.jun.edi.component.model.MItemModel;
import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.SkuModel;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.type.JanType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.RegistStatusType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.validation.group.BulkRegistValidationGroup;

/**
 * 品番検証用のコンポーネント.
 */
@Component
public class ItemValidateComponent extends GenericComponent {
    /** 品番のリソース名. */
    private static final String RESOURCE_ITEM = "item";

    /** SKUのリソース名. */
    private static final String RESOURCE_SKU = "sku";

    @Autowired
    private MItemComponent mItemComponent;

    @Autowired
    private TItemRepository itemRepository;

    @Autowired
    private JunpcHinmstComponent junpcHinmstComponent;

    @Autowired
    private ItemArticleNumberValidateComponent itemArticleNumberValidateComponent;

    @Autowired
    private CompositionValidateComponent compositionValidateComponent;

    /**
     * 検証用ビルダーを生成する.
     *
     * @return {@link ValidateBuilder}
     */
    public Validator getValidator() {
        return new Validator();
    }

    /**
     * バリデーター.
     */
    public class Validator {
        /** 品番に関連するマスタデータ. */
        private MItemModel masterData;

        /** 品番. */
        private ItemModel item;

        /** 登録ステータス. */
        private RegistStatusType registStatus;

        /** バリデーショングループリスト. */
        private List<Object> validationGroups;

        /** 一括登録. */
        private boolean isBulkRegist = false;

        /** バリデーションの結果. */
        private List<ResultMessage> resultMessages;

        /**
         * @param masterData 品番に関連するマスタデータ.
         * @return {@link Validator}
         */
        public Validator masterData(final MItemModel masterData) {
            this.masterData = masterData;
            return this;
        }

        /**
         * @param validationGroups バリデーショングループリスト.
         * @return {@link Validator}
         */
        public Validator validationGroups(final List<Object> validationGroups) {
            this.validationGroups = validationGroups;

            if (this.validationGroups.contains(BulkRegistValidationGroup.class)) {
                // 一括登録時のみに実行するバリデーショングループが設定されている場合
                isBulkRegist = true;
            }

            return this;
        }

        /**
         * @param item 品番.
         * @return {@link Validator}
         */
        public Validator item(final ItemModel item) {
            this.item = item;
            this.registStatus = RegistStatusType.convertToType(item.getRegistStatus());
            return this;
        }

        /**
         * @return バリデーションの結果.
         */
        public List<ResultMessage> validate() {
            resultMessages = new ArrayList<>();

            // マスタデータの検索キーを設定
            masterData.setMasterDataSearchKey(item);

            // 検索キーが変更された場合、それぞれのコードに関連するマスタデータを取得して設定
            mItemComponent.setMasterDataIfSearchKeyWasChanged(masterData);

            Optional.of(true)
                    // 検証
                    .filter(v -> isValid())
                    // JAN/UPCが有効か検証
                    .filter(v -> isValidArticleNumber())
                    // 品番が有効か検証
                    .filter(v -> isValidPartNo());

            return resultMessages;
        }

        /**
         * 検証する.
         *
         * @return 検証結果
         */
        private boolean isValid() {
            if (isBulkRegist) {
                // 一括登録の場合
                // 日・週の検証
                validateDateAndWeek();

                // アイテムコードの検証
                validateItemCode();

                // SKUの検証
                validateSkus();

                // 生産メーカー情報の検証
                validateMdfMaker();

                // 担当情報の検証
                validateStaff();

                // 原産国の検証
                validateCooCode();

                // 丸井情報の検証
                validateMarui();

                // 統計情報の検証
                validateStatisticsInfo();
            }

            // 組成の検証
            validateCompositions();

            return resultMessages.isEmpty();
        }

        /**
         * JAN/UPCがすでに存在するか検証する.
         *
         * @return 検証結果
         */
        private boolean isValidArticleNumber() {
            if (isBulkRegist) {
                // 一括登録の場合
                // JAN/UPCの検証
                validateArticleNumber();
            }

            return resultMessages.isEmpty();
        }

        /**
         * 品番がすでに存在するか検証する.
         *
         * @return 検証結果
         */
        private boolean isValidPartNo() {
            if (isBulkRegist) {
                // 一括登録の場合
                // 品番重複の検証
                validatePartNoExists();
            }

            return resultMessages.isEmpty();
        }

        /**
         * 日・週の検証.
         */
        private void validateDateAndWeek() {
            // 投入日・週の検証
            validateDateAndWeek(item.getDeploymentDate(), item.getDeploymentWeek(), "deploymentDate", "deploymentWeek");

            // P終了日・週の検証
            validateDateAndWeek(item.getPendDate(), item.getPendWeek(), "pendDate", "pendWeek");

            if (item.getDeploymentDate() != null || item.getPendDate() != null) {
                if (item.getDeploymentDate() == null || item.getPendDate() == null || item.getDeploymentDate().after(item.getPendDate())) {
                    // 「投入日」>「P終了日」の場合、エラー
                    resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_I_23).resource(RESOURCE_ITEM));
                }
            }
        }

        /**
         * 日付・週番号の検証.
         *
         * <pre>
         * 日付または週番号が空の場合、エラーとする。
         * 日付から計算した週番号と週番号が不一致の場合、エラーとする。
         * </pre>
         *
         * @param date 日付
         * @param week 週番号
         * @param fieldDate 項目名：日付
         * @param fieldWeek 項目名：週
         */
        private void validateDateAndWeek(
                final Date date,
                final Integer week,
                final String fieldDate,
                final String fieldWeek) {
            if (RegistStatusType.PART == this.registStatus) {
                if (date == null) {
                    resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_008).field(fieldDate).resource(RESOURCE_ITEM));
                }

                if (week == null) {
                    resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_008).field(fieldWeek).resource(RESOURCE_ITEM));
                }
            }

            if (date != null || week != null) {
                if (date == null || week == null || DateUtils.calcWeek(date) != week.intValue()) {
                    resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_010).field(fieldWeek).resource(RESOURCE_ITEM));
                }
            }
        }

        /**
         * アイテムコードの検証.
         *
         * <pre>
         * アイテムコードが空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validateItemCode() {
            if (StringUtils.isNotEmpty(item.getItemCode()) && masterData.getItem() == null) {
                resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_I_25).resource(RESOURCE_ITEM));
            }
        }

        /**
         * SKUの検証.
         */
        private void validateSkus() {
            // 存在チェック
            if (CollectionUtils.isEmpty(item.getSkus())) {
                resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_008).field("sku").resource(RESOURCE_SKU));

                return;
            }

            final String hscd = item.getBrandCode() + item.getItemCode();

            for (final SkuModel sku : item.getSkus()) {
                if (isSkuDuplicate(sku, item.getSkus())) {
                    // カラー・サイズが重複する場合、エラー
                    resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_I_26, sku.getColorCode(), sku.getSize()).resource(RESOURCE_SKU));
                }

                if (isNotColorExist(sku)) {
                    // 登録されていないカラーが入力されている場合、エラー
                    resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("colorCode").value(sku.getColorCode()).resource(RESOURCE_SKU));
                }

                if (isNotSizeExist(hscd, sku)) {
                    // 登録されていないサイズが入力されている場合、エラー
                    resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("size").value(sku.getSize()).resource(RESOURCE_SKU));
                }

                if ((JanType.IN_HOUSE_JAN == item.getJanType()) && (sku.getExternalSku() != null)) {
                    // 自社JANかつ、他社品番情報が入力されている場合、エラー
                    resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_I_30, sku.getColorCode(), sku.getSize()).resource(RESOURCE_SKU));
                }

            }
        }

        /**
         * 重複チェック.
         * 引数内でSKUの重複の有無を確認する.
         *
         * @param sku SKU
         * @param skus SKUのリスト
         * @return true: 重複あり false:重複なし
         */
        public boolean isSkuDuplicate(final SkuModel sku, final List<SkuModel> skus) {
            final long count = skus.stream().filter(v -> (StringUtils.equals(v.getColorCode(), sku.getColorCode())
                    && (StringUtils.equals(v.getSize(), sku.getSize())))).count();

            if (count > 1) {
                return true;
            }

            return false;

        }

        /**
         * @param sku SKU
         * @return true:カラーがマスタに存在しない
         */
        private boolean isNotColorExist(final SkuModel sku) {
            return masterData.getColors().stream().noneMatch(v -> StringUtils.equals(v.getCode1(), sku.getColorCode()));
        }

        /**
         * @param hscd ブランドコード＋アイテムコード
         * @param sku SKU
         * @return true:サイズがマスタに存在しない
         */
        private boolean isNotSizeExist(final String hscd, final SkuModel sku) {
            return masterData.getSizes().stream().noneMatch(v -> StringUtils.equals(v.getSzkg(), sku.getSize())
                    && StringUtils.equals(v.getHscd(), hscd));
        }

        /**
         * 組成の検証.
         */
        private void validateCompositions() {
            if (masterData.getItem() == null) {
                // データがない場合、処理を終了する
                return;
            }

            final List<ResultMessage> compositionErrorMessages =
                    compositionValidateComponent.validateCompositions(masterData, item, registStatus, isBulkRegist);
            resultMessages.addAll(compositionErrorMessages);
        }

        /**
         * 生産メーカー情報の検証.
         */
        private void validateMdfMaker() {
            // 生産メーカーの検証
            validateMdfMakerCode();

            // 生産工場の検証
            validateMdfMakerFactoryCode();
        }

        /**
         * 生産メーカーの検証.
         *
         * <pre>
         * 生産メーカーコードが空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validateMdfMakerCode() {
            // マスタ検索時のコード値を比較
            if (StringUtils.isNotEmpty(masterData.getMdfMakerCode()) && masterData.getMdfMaker() == null) {
                resultMessages.add(
                        ResultMessage.fromCode(MessageCodeType.CODE_009).field("mdfMakerCode").value(masterData.getMdfMakerCode()).resource(RESOURCE_ITEM));
            }
        }

        /**
         * 生産工場の検証と生産工場名の設定.
         *
         * <pre>
         * 生産工場コードが空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validateMdfMakerFactoryCode() {
            // マスタ検索時のコード値を比較
            if (StringUtils.isNotEmpty(masterData.getMdfMakerFactoryCode())) {
                if (masterData.getMdfMakerFactory() == null) {
                    resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("mdfMakerFactoryCode")
                            .value(masterData.getMdfMakerFactoryCode())
                            .resource(RESOURCE_ITEM));
                } else {
                    // 生産工場名を設定
                    item.setMdfMakerFactoryName(masterData.getMdfMakerFactory().getName());
                }
            }
        }

        /**
         * 担当情報の検証.
         */
        private void validateStaff() {
            // 担当 - 企画担当の検証
            validatePlannerCode();

            // 担当 - 製造担当の検証
            validateMdfStaffCode();

            // 担当 - パターンナーの検証
            validatePatanerCode();
        }

        /**
         * 担当 - 企画担当の検証.
         *
         * <pre>
         * 企画担当コードが空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validatePlannerCode() {
            if (StringUtils.isNotEmpty(item.getPlannerCode()) && masterData.getPlanner() == null) {
                resultMessages
                        .add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("plannerCode").value(item.getPlannerCode()).resource(RESOURCE_ITEM));
            }
        }

        /**
         * 担当 - 製造担当の検証.
         *
         * <pre>
         * 製造担当コードが空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validateMdfStaffCode() {
            if (StringUtils.isNotEmpty(item.getMdfStaffCode()) && masterData.getMdfStaff() == null) {
                resultMessages.add(
                        ResultMessage.fromCode(MessageCodeType.CODE_009).field("mdfStaffCode").value(item.getMdfStaffCode()).resource(RESOURCE_ITEM));
            }
        }

        /**
         * 担当 - パターンナーの検証.
         *
         * <pre>
         * パターンナーコードが空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validatePatanerCode() {
            if (StringUtils.isNotEmpty(item.getPatanerCode()) && masterData.getPataner() == null) {
                resultMessages
                        .add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("patanerCode").value(item.getPatanerCode()).resource(RESOURCE_ITEM));
            }
        }

        /**
         * 原産国の検証.
         *
         * <pre>
         * 原産国コードが空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validateCooCode() {
            if (StringUtils.isNotEmpty(item.getCooCode())
                    && masterData.getOriginCountries().stream().noneMatch(v -> StringUtils.equals(v.getCode1(), item.getCooCode()))) {
                resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("cooCode").value(item.getCooCode()).resource(RESOURCE_ITEM));
            }
        }

        /**
         * 丸井情報の検証.
         */
        private void validateMarui() {
            // 丸井 - 丸井品番の検証
            validateMaruiGarmentNo();

            // 丸井 - Voi区分の検証
            validateVoiCode();
        }

        /**
         * 丸井品番の検証.
         *
         * <pre>
         * - 1 初期値設定処理
         *  - 1-1 丸井品番が空の場合、以下の処理を行う。
         *   - 1-1-1 丸井品番マスタからブランドコード、アイテムコードに紐付く初期値を取得する。
         *   - 1-1-2 初期値が存在しない場合、エラーとする。
         *
         * - 2 チェック処理
         *  - 2-1 入力された丸井品番が、ブランドに紐づく丸井品番に存在しない場合、エラーとする。
         * </pre>
         *
         * <p>以下は、画面側のチェック内容。</p>
         * <pre>
         * - 1 初期値設定処理
         *  - 1-1 丸井品番が空もしくは、「000000」の場合、以下の処理を行う。
         *   - 1-1-1 ブランドに紐づく丸井品番のリストが存在しない場合、
         *           「000000」を初期値として設定し、処理を終了する。
         *           (チェック処理もスキップする)
         *
         *   - 1-1-2 ブランド・アイテムに紐づく丸井品番が存在する場合
         *           以下の条件に該当する丸井品番を初期値として設定し、処理を終了する。
         *           (チェック処理もスキップする)
         *
         *           (1) item30に1が設定されている丸井品番
         *           (2) 一番コード値が小さい丸井品番
         *
         * - 2 チェック処理
         *  - 2-1 品番登録時に、丸井品番が空もしくは、「000000」の場合、エラーとし、処理を終了する。
         *  - 2-2 入力された丸井品番が、ブランドに紐づく丸井品番に存在しない場合、エラーとする。
         * </pre>
         */
        private void validateMaruiGarmentNo() {
            final List<MCodmstEntity> list = masterData.getMaruiItems();

            if (StringUtils.isEmpty(item.getMaruiGarmentNo())) {
                final Optional<MCodmstEntity> mCodmstEntityOptional =
                        // 丸井品番リストからアイテムコードでフィルタ
                        mItemComponent.findByInitial(list.stream().filter(v -> StringUtils.equals(v.getCode2(), masterData.getItemCode())));

                if (mCodmstEntityOptional.isPresent()) {
                    // 初期値を設定
                    mCodmstEntityOptional.ifPresent(v -> item.setMaruiGarmentNo(v.getCode3()));
                    return;
                }

                if (RegistStatusType.PART == this.registStatus) {
                    // 品番登録時は、ブランドコードとアイテムコードに紐付く初期値が存在しないため、エラーとする
                    resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_008).field("maruiGarmentNo").resource(RESOURCE_ITEM));
                }

                return;
            }

            if (list.stream().noneMatch(v -> StringUtils.equals(v.getCode3(), item.getMaruiGarmentNo()))) {
                // ブランドコードに紐付く丸井品番が存在しない場合、エラーとする
                resultMessages
                        .add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("maruiGarmentNo").value(item.getMaruiGarmentNo()).resource(RESOURCE_ITEM));
                return;
            }
        }

        /**
         * Voi区分の検証.
         *
         * <pre>
         * Voi区分が空文字の場合、初期値を設定する。
         * Voi区分が空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validateVoiCode() {
            final List<MCodmstEntity> list = masterData.getVoiSections();

            if (StringUtils.isEmpty(item.getVoiCode())) {
                mItemComponent.findByInitial(list.stream())
                        // 初期値を設定
                        .ifPresent(v -> item.setVoiCode(v.getCode1()));
                return;
            }

            if (list.stream().noneMatch(v -> StringUtils.equals(v.getCode1(), item.getVoiCode()))) {
                resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("voiCode").value(item.getVoiCode()).resource(RESOURCE_ITEM));
            }
        }

        /**
         * 統計情報の検証.
         */
        private void validateStatisticsInfo() {
            // 統計情報 - 素材の検証
            validateMaterialCode();

            // 統計情報 - ゾーンの検証
            validateZoneCode();

            // 統計情報 - サブブランドの検証
            validateSubBrandCode();

            // 統計情報 - テイストの検証
            validateTasteCode();

            // 統計情報 - タイプ1の検証
            validateType1Code();

            // 統計情報 - タイプ2の検証
            validateType2Code();

            // 統計情報 - タイプ3の検証
            validateType3Code();

            // 統計情報 - 展開の検証
            validateOutletCode();
        }

        /**
         * 統計情報 - 素材の検証.
         *
         * <pre>
         * 素材が空文字の場合、初期値を設定する。
         * 素材が空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validateMaterialCode() {
            final List<MCodmstEntity> list = masterData.getMaterials();

            if (StringUtils.isEmpty(item.getMaterialCode())) {
                mItemComponent.findByInitial(list.stream())
                        // 初期値を設定
                        .ifPresent(v -> item.setMaterialCode(v.getCode1()));
                return;
            }

            if (list.stream().noneMatch(v -> StringUtils.equals(v.getCode1(), item.getMaterialCode()))) {
                resultMessages
                        .add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("materialCode").value(item.getMaterialCode()).resource(RESOURCE_ITEM));
            }
        }

        /**
         * 統計情報 - ゾーンの検証.
         *
         * <pre>
         * ゾーンが空文字の場合、初期値を設定する。
         * ゾーンが空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validateZoneCode() {
            final List<MCodmstEntity> list = masterData.getZones();

            if (StringUtils.isEmpty(item.getZoneCode())) {
                mItemComponent.findByInitial(list.stream())
                        // 初期値を設定
                        .ifPresent(v -> item.setZoneCode(v.getCode1()));
                return;
            }

            if (list.stream().noneMatch(v -> StringUtils.equals(v.getCode1(), item.getZoneCode()))) {
                resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("zoneCode").value(item.getZoneCode()).resource(RESOURCE_ITEM));
            }
        }

        /**
         * 統計情報 - サブブランドの検証.
         *
         * <pre>
         * サブブランドが空文字の場合、初期値を設定する。
         * サブブランドが空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validateSubBrandCode() {
            final List<MCodmstEntity> list = masterData.getSubBrands();

            if (StringUtils.isEmpty(item.getSubBrandCode())) {
                mItemComponent.findByInitial(list.stream())
                        // 初期値を設定
                        .ifPresent(v -> item.setSubBrandCode(v.getCode2()));
                return;
            }

            if (list.stream().noneMatch(v -> StringUtils.equals(v.getCode2(), item.getSubBrandCode()))) {
                resultMessages
                        .add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("subBrandCode").value(item.getSubBrandCode()).resource(RESOURCE_ITEM));
            }
        }

        /**
         * 統計情報 - テイストの検証.
         *
         * <pre>
         * テイストが空文字の場合、初期値を設定する。
         * テイストが空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validateTasteCode() {
            final List<MCodmstEntity> list = masterData.getTastes();

            if (StringUtils.isEmpty(item.getTasteCode())) {
                mItemComponent.findByInitial(list.stream())
                        // 初期値を設定
                        .ifPresent(v -> item.setTasteCode(v.getCode2()));
                return;
            }

            if (list.stream().noneMatch(v -> StringUtils.equals(v.getCode2(), item.getTasteCode()))) {
                resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("tasteCode").value(item.getTasteCode()).resource(RESOURCE_ITEM));
            }
        }

        /**
         * 統計情報 - タイプ1の検証.
         *
         * <pre>
         * タイプ1が空文字の場合、初期値を設定する。
         * タイプ1が空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validateType1Code() {
            final List<MCodmstEntity> list = masterData.getType1s();

            if (StringUtils.isEmpty(item.getType1Code())) {
                mItemComponent.findByInitial(list.stream())
                        // 初期値を設定
                        .ifPresent(v -> item.setType1Code(v.getCode1()));
                return;
            }

            if (list.stream().noneMatch(v -> StringUtils.equals(v.getCode1(), item.getType1Code()))) {
                resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("type1Code").value(item.getType1Code()).resource(RESOURCE_ITEM));
            }
        }

        /**
         * 統計情報 - タイプ2の検証.
         *
         * <pre>
         * タイプ2が空文字の場合、初期値を設定する。
         * タイプ2が空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validateType2Code() {
            final List<MCodmstEntity> list = masterData.getType2s();

            if (StringUtils.isEmpty(item.getType2Code())) {
                mItemComponent.findByInitial(list.stream())
                        // 初期値を設定
                        .ifPresent(v -> item.setType2Code(v.getCode1()));
                return;
            }

            if (list.stream().noneMatch(v -> StringUtils.equals(v.getCode1(), item.getType2Code()))) {
                resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("type2Code").value(item.getType2Code()).resource(RESOURCE_ITEM));
            }
        }

        /**
         * 統計情報 - タイプ3の検証.
         *
         * <pre>
         * タイプ3が空文字の場合、初期値を設定する。
         * タイプ3が空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validateType3Code() {
            final List<MCodmstEntity> list = masterData.getType3s();

            if (StringUtils.isEmpty(item.getType3Code())) {
                mItemComponent.findByInitial(list.stream())
                        // 初期値を設定
                        .ifPresent(v -> item.setType3Code(v.getCode1()));
                return;
            }

            if (list.stream().noneMatch(v -> StringUtils.equals(v.getCode1(), item.getType3Code()))) {
                resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("type3Code").value(item.getType3Code()).resource(RESOURCE_ITEM));
            }
        }

        /**
         * 統計情報 - 展開の検証.
         *
         * <pre>
         * 展開が空文字の場合、初期値を設定する。
         * 展開が空文字以外 かつ マスタにレコードが存在しない場合、エラーとする。
         * </pre>
         */
        private void validateOutletCode() {
            final List<MCodmstEntity> list = masterData.getOutlets();

            if (StringUtils.isEmpty(item.getOutletCode())) {
                mItemComponent.findByInitial(list.stream())
                        // 初期値を設定
                        .ifPresent(v -> item.setOutletCode(v.getCode1()));
                return;
            }

            if (list.stream().noneMatch(v -> StringUtils.equals(v.getCode1(), item.getOutletCode()))) {
                resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_009).field("outletCode").value(item.getOutletCode()).resource(RESOURCE_ITEM));
            }
        }

        /**
         * JAN/UPCを検証する.
         */
        private void validateArticleNumber() {
            resultMessages.addAll(itemArticleNumberValidateComponent.validateArticleNumber(item));
        }

        /**
         * 品番がすでに存在するか検証する.
         */
        private void validatePartNoExists() {
            if (itemRepository.existsByPartNo(item.getPartNo())) {
                // 品番が存在する場合、エラーとする
                resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_I_01).resource(RESOURCE_ITEM));
                return;
            }

            if (junpcHinmstComponent.existsByPartNoAndYear(item.getPartNo(), item.getYear())) {
                // 発注生産に品番が存在する場合、エラーとする
                resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_I_02).resource(RESOURCE_ITEM));
            }
        }
    }
}
