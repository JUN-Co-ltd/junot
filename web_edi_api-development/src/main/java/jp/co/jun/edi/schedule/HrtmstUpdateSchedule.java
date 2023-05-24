package jp.co.jun.edi.schedule;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.MKanmstComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.schedule.HrtmstUpdateScheduleComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

// #PRD_0138 #10680 add JFE start
/**
 * 配分率マスタ更新スケジュール.
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT + ".schedule.hrtmst-update-schedule.enabled", matchIfMissing = true)
public class HrtmstUpdateSchedule {
	private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.hrtmst-update-schedule";
	private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

	@Autowired
	private ScheduleBusinessComponent scheduleBusinessComponent;

	@Autowired
	private MKanmstComponent MKanmstComponent;

	@Autowired
	private PropertyComponent propertyComponent;

	@Autowired
	private HrtmstUpdateScheduleComponent hrtmstUpdateScheduleComponent;


	/**
	 * 配分率マスタ更新実行.
	 */
	@Scheduled(cron = PROPERTY_NAME_CRON)
	public void task() {
		try {
			// ユーザID
			final BigInteger userId = scheduleBusinessComponent.getUserId();

			//開始ログ出力
			log.info(LogStringUtil.of("executeHaibunSummary")
					.message("Start processing of HrtmstUpdateSchedule.")
					.build());

			//日計日を取得
			final String nitymd = MKanmstComponent.getNitymd();

			//#PRD_0159 #10680 mod JFE start
			//日計日が取得できない場合ログを出力し終了
			//if (nitymd != null) {
			if (nitymd != null && !(nitymd.equals(""))) {
			//#PRD_0159 #10680 mod JFE end

				//店舗別配分率マスタより、当日（日計日）連携されたものを取得する 連携日＝当日（日計日）のレコード件数を取得する
				final Integer linkingCount = hrtmstUpdateScheduleComponent.countByNitymd(nitymd);
				//TODO 変数名変更
				if (linkingCount != 0) {
					//結合後、課コードで集約し結果を配分率マスタに設定する
						//配分率マスタ削除・登録
						hrtmstUpdateScheduleComponent.deleteInsertByHaibunSummaryTarget(nitymd,propertyComponent.getCommonProperty().getAdminUserAccountName(),userId);
				} else
					log.info(LogStringUtil.of("getCount.")
							.message("not found Haibun Summary Target.")
							.build());
			} else
				log.info(LogStringUtil.of("getNitymd")
						.message("not found Nitymd m_kanmst.")
						.build());
			//終了ログ出力
			log.info(LogStringUtil.of("executeHaibunSummary")
					.message("End processing of HrtmstUpdateSchedule.")
					.build());

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
// #PRD_0138 #10680 add JFE end
