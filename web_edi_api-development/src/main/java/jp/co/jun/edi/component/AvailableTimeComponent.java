package jp.co.jun.edi.component;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.AvailableTimeStartEndModel;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * JUNoT利用可能時間判定のコンポーネント.
 */
@Slf4j
@Component
public class AvailableTimeComponent {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".component.available-time-component";

    /**
     * 利用可能時間（文字列）.
     *
     * <p>指定方法："HH:mm:ss-HH:mm:ss,HH:mm:ss-HH:mm:ss"（ダブルクオーテーション不要）</p>
     *
     * <pre>
     * 7時から22時半までの場合は、"07:00:00-22:30:00"。
     * 22時から翌朝の8時までの場合は、"22:00:00-23:59:59,00:00:00-08:00:00"。
     * 利用可能時間を指定しない場合、""。
     * </pre>
     */
    @Value("${" + PROPERTY_NAME_PREFIX + ".available-times}")
    private String[] stringAvailableTimes;

    /**
     * 利用可能時間（クライアント返却用文字列）.
     */
    @Value("${" + PROPERTY_NAME_PREFIX + ".available-times}")
    private String availableTimesForString;

    /**
     * 利用可能時間.
     */
    private Set<AvailableTimeStartEndModel> availableTimes;

    /**
     * 利用可能時間の有無.
     */
    private boolean availableTimeEnabled;

    /**
     * Beanコンテキスト生成後に呼び出される.
     */
    @PostConstruct
    public void postConstruct() {
        this.availableTimes = new HashSet<>(stringAvailableTimes.length);

        for (final String stringAvailableTime : stringAvailableTimes) {
            // JUNoT利用可能開始時間と終了時間に分割
            final String[] times = StringUtils.split(stringAvailableTime, "-");

            // 整形後のJUNoT利用可能時間帯を追加(コロン除去)
            this.availableTimes.add(new AvailableTimeStartEndModel(
                    Integer.parseInt(StringUtils.replace(times[0], ":", "")),
                    Integer.parseInt(StringUtils.replace(times[1], ":", ""))));
        }

        this.availableTimeEnabled = !availableTimes.isEmpty();

        log.info(LogStringUtil.of("postConstruct").value("availableTimes", availableTimesForString).build());
    }

    /**
     * 利用可能時間の有無を返却する.
     *
     * @return true(利用可能時間の指定あり)/false(利用可能時間の指定なし)
     */
    public boolean isAvailableTimeEnabled() {
        return availableTimeEnabled;
    }

    /**
     * 利用可能時間を返却する.
     *
     * @return 利用可能時間
     */
    public String getAvailableTimes() {
        return availableTimesForString;
    }

    /**
     * 利用可能時間外かを判定する.
     *
     * @param now 現在時刻
     * @return true(利用可能時間外)/false(利用可能時間内)
     */
    public boolean isUnavailableTime(final ZonedDateTime now) {
        if (!availableTimeEnabled) {
            return false;
        }

        // 現在時間をHH:mm:ss→HHmmssに変換する
        final int nowTime = now.getHour() * 10000
                + now.getMinute() * 100
                + now.getSecond();

        for (final AvailableTimeStartEndModel availableTime : availableTimes) {
            // 時間内の場合はJUNoT利用可能
            if (availableTime.getStartTime() <= nowTime && nowTime <= availableTime.getEndTime()) {
                return false;
            }
        }

        return true;
    }
}
