package jp.co.jun.edi.schedule;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.co.jun.edi.component.schedule.MaterialOrderLinkingErrorMailForwardComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.exception.ScheduleException;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 資材発注連携エラーメールを転送する.
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT + ".schedule.material-order-linkage-error-mail-forward-schedule.enabled", matchIfMissing = true)
public class MaterialOrderLinkingErrorMailForwardSchedule {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.material-order-linkage-error-mail-forward-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Value("${" + PROPERTY_NAME_PREFIX + ".status}")
    private int status;
    @Value("${" + PROPERTY_NAME_PREFIX + ".target}")
    private String target;

    @Autowired
    private MaterialOrderLinkingErrorMailForwardComponent materialOrderLinkingErrorComponent;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 資材発注連携エラーメール転送を実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        try {
            if (status == 0) {
                // 通常実行
                execute();
            } else {
                // リカバリ実行
                recovery();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * リカバリ実行.
     *
     * @throws Exception
     *             例外
     */
    // TODO 警告無視設定をしている。本対策をどうするか検討が必要
    @SuppressFBWarnings(value = "SIC_INNER_SHOULD_BE_STATIC_ANON", justification = "TypeReference<> is for Jackson")
    private void recovery() throws Exception {
        if (StringUtils.isEmpty(target)) {
            // プロパティにtargetが設定されていない場合エラー
            throw new ScheduleException(LogStringUtil.of("task").message("property has not target.").build());
        }

        final Map<String, List<String>> targetData = objectMapper.readValue(target, new TypeReference<Map<String, List<String>>>() { });

        for (Entry<String, List<String>> entry : targetData.entrySet()) {
            final String key = entry.getKey();
            if (CollectionUtils.isEmpty(entry.getValue())) {
                // ターゲットとなるオーダー識別コードが存在しない場合、次の処理を実行
                log.error(LogStringUtil.of("recovery").message("Target information set in the property is invalid data.").value("s3key", key).build());
                continue;
            }
            materialOrderLinkingErrorComponent.mailForward(key, targetData.get(key));
        }
    }

    /**
     * 通常実行.
     *
     * @throws Exception
     *             例外
     */
    private void execute() throws Exception {
        for (final S3ObjectSummary os : materialOrderLinkingErrorComponent.getObjectSummaries()) {
            // S3からメールを取得し、内容を解析して、該当する担当者にメールを転送する
            materialOrderLinkingErrorComponent.mailForward(os.getKey(), null);
        }
    }
}
