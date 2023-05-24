package jp.co.jun.edi.component.bulkregist;

import java.io.IOException;
// PRD_0023 && No_65 add JFE start
import java.math.BigDecimal;
// PRD_0023 && No_65 add JFE end
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.groups.Default;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.SmartValidator;
import org.springframework.web.multipart.MultipartFile;

import jp.co.jun.edi.component.GenericComponent;
import jp.co.jun.edi.component.MItemComponent;
import jp.co.jun.edi.component.MessageComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.item.ItemValidateComponent;
import jp.co.jun.edi.component.model.BulkRegistItemPropertyModel;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.model.BulkRegistItemModel;
import jp.co.jun.edi.model.BulkRegistItemResultModel;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.ItemCreateService;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.RegistStatusType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
// PRD_0023 && No_65 add JFE start
import jp.co.jun.edi.util.NumberUtils;
// PRD_0023 && No_65 add JFE end
import jp.co.jun.edi.util.xlsx.bulkRegistItem.BulkRegistItem;
import jp.co.jun.edi.util.xlsx.bulkRegistItem.BulkRegistItemInfo;
import jp.co.jun.edi.util.xlsx.bulkRegistItem.BulkRegistItemSheetInfo;
import jp.co.jun.edi.util.xlsx.bulkRegistItem.BulkRegistItemUtils;
import jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet.ItemSheet;
import jp.co.jun.edi.validation.group.BulkRegistValidationGroup;
import jp.co.jun.edi.validation.group.ConfirmValidationGroup;
import jp.co.jun.edi.validation.group.CreateValidationGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * 品番・商品一括登録用のコンポーネント.
 */
@Component
@Slf4j
public class BulkRegistItemComponent extends GenericComponent {
    private static final String MESSAGE_CODE_PREFIX = BulkRegistItemComponent.class.getName();

    private static final Locale LOCALE = Locale.JAPANESE;

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private SmartValidator smartValidator;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private MessageComponent messageComponent;

    @Autowired
    private MItemComponent mItemComponent;

    @Autowired
    private ItemValidateComponent itemValidateComponent;

    @Autowired
    private ItemCreateService createService;

    /**
     * 非同期で登録する.
     *
     * @param authentication {@link Authentication} instance
     * @param loginUser {@link CustomLoginUser} instance
     * @param model {@link BulkRegistItemModel} instance
     */
    @Async("ThreadBulkRegist")
    public void registAsync(
            final Authentication authentication,
            final CustomLoginUser loginUser,
            final BulkRegistItemModel model) {
        try {
            final SecurityContext ctx = SecurityContextHolder.createEmptyContext();
            ctx.setAuthentication(authentication);
            SecurityContextHolder.setContext(ctx);

            model.getItems().forEach(item -> {
                createService.call(CreateServiceParameter.<ItemModel>builder().loginUser(loginUser).item(item).build());
            });
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * {@link BulkRegistItemModel} に変換.
     *
     * @param file {@link MultipartFile} instance
     * @param registStatus {@link RegistStatusType}
     * @return {@link BulkRegistItemModel} instance
     */
    public BulkRegistItemModel toItem(final MultipartFile file, final RegistStatusType registStatus) {
        return new Validator()
                // 言語設定
                .locale(LOCALE)
                // ファイル設定
                .file(file)
                // 登録ステータス設定
                .registStatus(registStatus)
                // プロパティ設定
                .property(this.propertyComponent.getCommonProperty().getBulkRegistItem())
                // 検証実行
                .validate();
    }

    /**
     * バリデーター.
     */
    public class Validator {
        /** 言語. */
        private Locale locale;

        /** ファイル. */
        private MultipartFile file;

        /** 登録ステータス. */
        private RegistStatusType registStatus;

        /** バリデーショングループリスト. */
        private List<Object> validationGroups;

        /** プロパティ. */
        private BulkRegistItemPropertyModel property;

        /** 品番検証. */
        private ItemValidateComponent.Validator itemValidator;

        /** 結果. */
        private BulkRegistItemModel result;

        /** バリデーションの結果. */
        private List<String> errors;

        /** JANコードの重複チェック用. */
        private Set<String> janCodeSet = new HashSet<>();

        /** UPCコードの重複チェック用. */
        private Set<String> upcCodeSet = new HashSet<>();

        /**
         * @param locale 言語.
         * @return {@link Validator}
         */
        public Validator locale(final Locale locale) {
            this.locale = locale;
            return this;
        }

        /**
         * @param file {@link MultipartFile} instance
         * @return {@link Validator}
         */
        public Validator file(final MultipartFile file) {
            this.file = file;
            return this;
        }

        /**
         * @param registStatus {@link RegistStatusType}
         * @return {@link Validator}
         */
        public Validator registStatus(final RegistStatusType registStatus) {
            this.registStatus = registStatus;
            this.validationGroups = toValidationHints(registStatus);
            return this;
        }

        /**
         * 検証用のヒントに変換.
         *
         * @param registStatus {@link RegistStatusType}
         * @return 検証用のヒント
         */
        private List<Object> toValidationHints(final RegistStatusType registStatus) {
            final List<Object> validationGroups = new ArrayList<>();

            validationGroups.add(Default.class);
            // 登録時のみに実行するバリデーショングループを追加
            validationGroups.add(CreateValidationGroup.class);
            // 一括登録時のみに実行するバリデーショングループを追加
            validationGroups.add(BulkRegistValidationGroup.class);

            switch (registStatus) {
            case PART:
                // 品番確定時のみに実行するバリデーショングループを追加
                validationGroups.add(ConfirmValidationGroup.class);
                break;

            default:
                break;
            }

            return validationGroups;
        }

        /**
         * @param property {@link BulkRegistItemPropertyModel}
         * @return {@link Validator}
         */
        public Validator property(final BulkRegistItemPropertyModel property) {
            this.property = property;
            return this;
        }

        /**
         * メッセージを取得する.
         *
         * @param code メッセージコード
         * @param args 引数のリスト
         * @return メッセージ
         */
        private String getMessage(final String code, final Object... args) {
            return messageSource.getMessage(code, args, locale);
        }

        /**
         * @return {@link BulkRegistItemModel}
         */
        public BulkRegistItemModel validate() {
            result = new BulkRegistItemModel();

            try (BulkRegistItemUtils.Reader reader = BulkRegistItemUtils.getReader(
                    file.getInputStream(), property)) {
                // データ読み込み
                final List<BulkRegistItem> rows = reader.read();

                if (isValidRecordSize(rows, reader.getInfo())) {
                    itemValidator = itemValidateComponent.getValidator()
                            // マスタデータ設定
                            .masterData(mItemComponent.getMasterData())
                            // バリデーショングループリスト設定
                            .validationGroups(validationGroups);

                    // ソート
                    BulkRegistItem.sort(rows);

                    result.setResults(new ArrayList<>(rows.size()));
                    result.setErrors(new ArrayList<>(property.getItemErrorCount()));
                    result.setItems(new ArrayList<>(rows.size()));

                    rows.stream()
                            // SmartValidatorでオブジェクトを検証
                            .filter(row -> isValidSmartValidator(row))
                            // 整合性を検証
                            .filter(row -> isValidIntegrity(row))
                            // ItemModel に変換
                            .map(row -> toItem(row))
                            // PRD_0023 && No_65 add JFE start
                            // 原価合計を検証
                            .filter(item -> isValidTotalCost(item))
                            // PRD_0023 && No_65 add JFE end
                            // 品番の検証
                            .filter(item -> isValidItem(item))
                            .forEach(item -> {
                                // 正常の結果を追加
                                result.getResults().add(BulkRegistItemResultModel.toNormalResult(item));
                                result.getItems().add(item);
                            });

                    // 結果をマージ
                    result.setResults(BulkRegistItemResultModel.mergeAndSort(result.getResults()));
                }
            } catch (IOException e) {
                log.error("IOException occurred.", e);
                // 例外エラーを設定
                setExceptionError();
            } catch (EncryptedDocumentException e) {
                log.error("EncryptedDocumentException occurred.", e);
                // 例外エラーを設定
                setExceptionError();
            } catch (Exception e) {
                log.error("Exception occurred.", e);
                // 例外エラーを設定
                setExceptionError();
            }

            return result;
        }

        /**
         * レコード数を検証する.
         *
         * @param rows rows
         * @param info {@link BulkRegistItemInfo} instance
         * @return 検証結果
         */
        private boolean isValidRecordSize(final List<BulkRegistItem> rows, final BulkRegistItemInfo info) {
            if (CollectionUtils.isEmpty(rows)) {
                // レコードなしエラーを設定
                result.setResults(Collections.emptyList());
                result.setErrors(new ArrayList<>());
                result.getErrors().add(getMessage(MESSAGE_CODE_PREFIX + ".record.NotEmpty"));
                result.setItems(Collections.emptyList());

                return false;
            }

            errors = new ArrayList<>();

            info.getSheetInfos().forEach(v -> {
                // シート情報の検証
                validateSheetInfo(v);
            });

            if (!errors.isEmpty()) {
                result.setResults(Collections.emptyList());
                result.setErrors(errors);
                result.setItems(Collections.emptyList());

                return false;
            }

            return true;
        }

        /**
         * シート情報の検証.
         *
         * @param info {@link BulkRegistItemSheetInfo} instance
         */
        private void validateSheetInfo(final BulkRegistItemSheetInfo info) {
            if (info.isOver()) {
                // レコード数オーバーを設定
                errors.add(getMessage(MESSAGE_CODE_PREFIX + ".record.Max", info.getSheetName(), Integer.toString(info.getMaxSize())));
            }
        }

        /**
         * エラーを追加する.
         *
         * @param partNo 品番
         * @param errors エラーリスト
         */
        private void addErrors(final String partNo, final List<String> errors) {
            final int size = result.getErrors().size();
            final int errorCount = property.getItemErrorCount();

            if (size < errorCount) {
                final int toIndex = errorCount - size;
                final int errorsSize = errors.size();

                final List<String> errorsSubList;

                if (toIndex < errorsSize) {
                    errorsSubList = errors.subList(0, toIndex);
                } else {
                    errorsSubList = errors.subList(0, errorsSize);
                }

                final String code = MESSAGE_CODE_PREFIX + ".Message";
                final Object[] values = new Object[2];
                values[0] = partNo;

                result.getErrors().addAll(errorsSubList.stream()
                        .map(v -> {
                            values[1] = v;
                            return getMessage(code, values);
                        })
                        .collect(Collectors.toList()));
            }
        }

        /**
         * SmartValidatorでオブジェクトを検証する.
         *
         * @param row {@link BulkRegistItem} instance
         * @return 検証結果
         */
        private boolean isValidSmartValidator(final BulkRegistItem row) {
            if (log.isDebugEnabled()) {
                log.debug(LogStringUtil.of("isValidSmartValidator").value("row", row).build());
            }

            final BindingResult bindingResult = new DataBinder(row).getBindingResult();

            smartValidator.validate(row, bindingResult, validationGroups.toArray());

            if (bindingResult.hasErrors()) {
                // エラーの結果を追加
                result.getResults().add(BulkRegistItemResultModel.toErrorResult(row));
                // 単項目エラーを追加
                addErrors(row.getPartNo(), bindingResult.getAllErrors().stream()
                        .map(mapper -> mapper.getDefaultMessage()).collect(Collectors.toList()));

                return false;
            }

            return true;
        }

        /**
         * 整合性を検証する.
         * ItemModelでは検証できない内容のみチェックする.
         *
         * @param row {@link BulkRegistItem} instance
         * @return 検証結果
         */
        private boolean isValidIntegrity(final BulkRegistItem row) {
            errors = new ArrayList<>();

            final ItemSheet itemSheet = row.getItems().get(0);

            // 投入日と投入週の検証（ItemModelでは投入週に年を持たないため、本コンポーネントで年と週番号をチェックする）
            if (StringUtils.isNotEmpty(itemSheet.getDeploymentDate()) && StringUtils.isNotEmpty(itemSheet.getDeploymentWeek())) {
                if (!StringUtils.equals(DateUtils.stringYMDToYYYYWW(itemSheet.getDeploymentDate()), itemSheet.getDeploymentWeek())) {
                    errors.add(messageComponent.toMessage(ResultMessage.fromCode(MessageCodeType.CODE_010).field("deploymentWeek"), locale));
                }
            }

            // P終了日とP終了週の検証（ItemModelではP終了週に年を持たないため、本コンポーネントで年と週番号をチェックする）
            if (StringUtils.isNotEmpty(itemSheet.getPendDate()) && StringUtils.isNotEmpty(itemSheet.getPendWeek())) {
                if (!StringUtils.equals(DateUtils.stringYMDToYYYYWW(itemSheet.getPendDate()), itemSheet.getPendWeek())) {
                    errors.add(messageComponent.toMessage(ResultMessage.fromCode(MessageCodeType.CODE_010).field("pendWeek"), locale));
                }
            }

            // JAN/UPCの重複の検証
            if (CollectionUtils.isNotEmpty(row.getSkuJans()) && CollectionUtils.isNotEmpty(row.getSkuUpcs())) {
                errors.add(getMessage(MESSAGE_CODE_PREFIX + ".skus.JanAndUpc.Duplicate"));
            }

            // シート内のJANの重複の検証
            row.getSkuJans().stream()
                    .filter(sku -> StringUtils.isNotEmpty(sku.getJanCode()))
                    .forEach(sku -> {
                        if (janCodeSet.contains(sku.getJanCode())) {
                            errors.add(messageComponent.toMessage(ResultMessage.fromCode(MessageCodeType.CODE_I_20, sku.getJanCode()), locale));
                        } else {
                            janCodeSet.add(sku.getJanCode());
                        }
                    });

            // シート内のUPCの重複の検証
            row.getSkuUpcs().stream()
                    .filter(sku -> StringUtils.isNotEmpty(sku.getUpcCode()))
                    .forEach(sku -> {
                        if (upcCodeSet.contains(sku.getUpcCode())) {
                            errors.add(messageComponent.toMessage(ResultMessage.fromCode(MessageCodeType.CODE_I_20, sku.getUpcCode()), locale));
                        } else {
                            upcCodeSet.add(sku.getUpcCode());
                        }
                    });

            if (!errors.isEmpty()) {
                // エラーの結果を追加
                result.getResults().add(BulkRegistItemResultModel.toErrorResult(row));
                addErrors(row.getPartNo(), errors);

                return false;
            }

            return true;
        }

        /**
         * {@link BulkRegistItem} から、{@link ItemModel} に変換する.
         *
         * @param row {@link BulkRegistItem} instance
         * @return {@link ItemModel} instance
         */
        private ItemModel toItem(final BulkRegistItem row) {
            final ItemModel item = BulkRegistItem.toItem(row, registStatus);

            if (log.isDebugEnabled()) {
                log.debug(LogStringUtil.of("toItem").value("item", item).build());
            }

            return item;
        }

        /**
         * 品番を検証する.
         *
         * @param item {@link ItemModel} instance
         * @return 検証結果
         */
        private boolean isValidItem(final ItemModel item) {
            final List<ResultMessage> resultMessages = itemValidator.item(item).validate();

            if (!resultMessages.isEmpty()) {
                // エラーの結果を追加
                result.getResults().add(BulkRegistItemResultModel.toErrorResult(item));
                addErrors(item.getPartNo(), resultMessages.stream().map(
                        resultMessage -> messageComponent.toMessage(resultMessage, locale)).collect(Collectors.toList()));

                return false;
            }

            return true;
        }

//       PRD_0023 && No_65 add JFE start
        /**
         * 原価合計を検証する
         *
         * @param item {@link ItemModel} instance
         * @return 検証結果
         */
        private boolean isValidTotalCost(final ItemModel item) {
        	errors = new ArrayList<>();
        	final BigDecimal otherCost = item.getOtherCost();
        	final BigDecimal totalCost = otherCost.add(NumberUtils.defaultInt(item.getMatlCost()))
        											.add(NumberUtils.defaultInt(item.getAccessoriesCost()))
        											.add(NumberUtils.defaultInt(item.getProcessingCost()));

        	final BigDecimal retailPrice = item.getRetailPrice();

            if (totalCost.compareTo(retailPrice) > 0) {
            	result.getResults().add(BulkRegistItemResultModel.toErrorResult(item));
            	errors.add(getMessage(MESSAGE_CODE_PREFIX + ".record.CostIsGreaterThanRetailPrice"));
            	addErrors(item.getPartNo(), errors);
            	return false;
            }

            return true;
        }
//      PRD_0023 && No_65 add JFE end

        /**
         * 例外エラーを設定.
         */
        private void setExceptionError() {
            result.setResults(Collections.emptyList());
            // Exceptionを設定
            result.setErrors(new ArrayList<>());
            result.getErrors().add(getMessage(MESSAGE_CODE_PREFIX + ".Exception"));
            result.setItems(Collections.emptyList());
        }
    }
}
