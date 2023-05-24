package jp.co.jun.edi.schedule;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.AccountPurchaseConfirmLinkingCreateDatFileComponent;
import jp.co.jun.edi.component.MKanmstComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.schedule.AccountPurchaseConfirmFileLinkingScheduleComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.MKanmstEntity;
import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.repository.TPurchaseRepository;
import jp.co.jun.edi.type.FileInfoStatusType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 会計仕入確定ファイル作成スケジュール.
 * ・会計仕入確定データDATファイル作成
 * ・DATファイルをS3へアップロード
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT
        + ".schedule.account-purcase-confirm-file-linking-schedule.enabled", matchIfMissing = true)
public class AccountPurchaseConfirmFileLinkingSchedule {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.account-purcase-confirm-file-linking-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private AccountPurchaseConfirmLinkingCreateDatFileComponent createDatFileComponent;

    @Autowired
    private AccountPurchaseConfirmFileLinkingScheduleComponent scheduleComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    @Autowired
    private MKanmstComponent kanmstComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TPurchaseRepository tPurchaseRepository;

    /**
     * 仕入ファイル連携実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        // 最大処理日数：日計日より過去14日分(日計日含む)
        final MKanmstEntity kanmstEntity = kanmstComponent.getMKanmstEntity();
        if (StringUtils.isEmpty(kanmstEntity.getNitymd())) {
            // 日計日がない場合はエラー
            log.error(LogStringUtil.of("task").message("no nitymd error.").build());
            return;
        }
        final Date nitymd = DateUtils.stringToDate(kanmstEntity.getNitymd());
        final int maxProcesses = propertyComponent.getBatchProperty().getAccountPurchaseConfirmMaxProcesses();

        // 仕入情報リスト取得
        final Page<TPurchaseEntity> purchasePages = tPurchaseRepository.findByAccountLinkingStatusAndNitymd(
                FileInfoStatusType.FILE_UNPROCESSED.getValue(),
                nitymd,
                maxProcesses,
                PageRequest.of(0, Integer.MAX_VALUE));

        if (purchasePages.hasContent()) {
            final List<TPurchaseEntity> purchases = purchasePages.getContent();
            // ユーザID取得
            final BigInteger userId = scheduleBusinessComponent.getUserId();

            // 仕入情報の会計連携ステータスを更新(ファイル処理中)
            scheduleComponent.updateAccountLinkingStatus(FileInfoStatusType.FILE_PROCESSING, purchases, userId);

            // 仕入情報を計上日でグルーピング
            final Map<Date, List<TPurchaseEntity>> purchaseMap = purchases.stream().collect(
                    Collectors.groupingBy(
                            TPurchaseEntity::getRecordAt,
                            // 計上日昇順
                            TreeMap::new,
                            Collectors.toList()));

            // DATファイル作成
            execute(purchaseMap, kanmstEntity.getNitymd(), userId);
        }
    }

    /**
     * 会計仕入確定データファイル作成の実行.
     *
     * @param purchaseMap 仕入情報Map
     * @param nitymdStr 日計日
     * @param userId システムユーザーID
     */
    public void execute(final Map<Date, List<TPurchaseEntity>> purchaseMap,
            final String nitymdStr,
            final BigInteger userId) {
        try {
            // 計上日毎にスケジュール実行
            for (final Entry<Date, List<TPurchaseEntity>> entry : purchaseMap.entrySet()) {
                final Date recordAt = entry.getKey(); // 計上日
                final List<TPurchaseEntity> purchases = entry.getValue(); // 仕入情報リスト

                log.info(LogStringUtil.of("executeByRecordAtPurchases")
                        .message("Start processing of AccountPurchaseConfirmFileLinkingSchedule.")
                        .value("record_at", recordAt.toString())
                        .value("nitymd", nitymdStr)
                        .build());

                try {
                    scheduleComponent.executeByRecordAtPurchases(purchases, recordAt, nitymdStr, userId);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    // 仕入情報の会計連携ステータスを更新(ファイル処理エラー)
                    scheduleComponent.updateAccountLinkingStatus(FileInfoStatusType.FILE_ERROR, purchases, userId);
                }

                log.info(LogStringUtil.of("executeByRecordAtPurchases")
                        .message("End processing of AccountPurchaseConfirmFileLinkingSchedule.")
                        .build());
            }

            // 一時保存したファイルを削除
            createDatFileComponent.deleteFiles();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
