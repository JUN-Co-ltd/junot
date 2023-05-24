package jp.co.jun.edi.schedule;

import javax.annotation.PostConstruct;
import javax.json.Json;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * スケジュール共通処理.
 */
@Component
@Slf4j
public abstract class GenericSchedule {
    /**
     * アプリケーション起動時の処理.
     */
    @PostConstruct
    void postConstruct() {
        log.info(Json.createObjectBuilder().add("postConstruct", Json.createObjectBuilder()
                .add("initialDelay", getInitialDelay())
                .add("fixedRate", getFixedRate()))
                .build().toString());
    }

    /**
     * @return 最初のtaskを開始する時間. 単位はms.
     */
    protected abstract long getInitialDelay();

    /**
     * @return taskの実行開始時点から次のtaskを実行する時間を取得. 単位はms.
     */
    protected abstract long getFixedRate();

    /**
     * taskの実行.
     */
    protected abstract void task();
}
