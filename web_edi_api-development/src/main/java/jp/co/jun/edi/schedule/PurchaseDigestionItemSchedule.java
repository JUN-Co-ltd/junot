package jp.co.jun.edi.schedule;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.MKanmstComponent;
import jp.co.jun.edi.component.PurchaseDigestionItemScheduleComponent;
import jp.co.jun.edi.component.PurchaseDigestionItemStatusComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.MKanmstEntity;
import jp.co.jun.edi.entity.TPurchaseDigestionItemPDFEntity;
import jp.co.jun.edi.entity.TPurchaseFileInfoEntity;
import jp.co.jun.edi.repository.TPurchaseDigestionItemPdfRepository;
import jp.co.jun.edi.repository.TPurchaseFileInfoRepository;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

//PRD_0134 #10654 add JEF start
/**
 * 仕入明細伝票(消化委託)PDF生成スケジュール.
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT
		+ ".schedule.purchase-digestion-item-schedule.enabled", matchIfMissing = true)
public class PurchaseDigestionItemSchedule {
	private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.purchase-digestion-item-schedule";
	private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

	@Autowired
	private TPurchaseFileInfoRepository tPurchasesFileInfoRepository;

	@Autowired
	private PurchaseDigestionItemScheduleComponent purchaseDigestionItemScheduleComponent;

	@Autowired
	private PurchaseDigestionItemStatusComponent purchaseDigestionItemStatusComponent;

	@Autowired
	private ScheduleBusinessComponent scheduleBusinessComponent;

	@Autowired
	private MKanmstComponent kanmstComponent;

	@Autowired
	private TPurchaseDigestionItemPdfRepository tPurchaseDigestionItemPdfRepository;

	/**
	 * 仕入明細（伝票）PDF作成実行.
	 */
	@Scheduled(cron = PROPERTY_NAME_CRON)
	public void task() {
		try {
			// ユーザID
			final BigInteger userId = scheduleBusinessComponent.getUserId();

			// 日計日が２１日の場合にバッチを実行する
			final MKanmstEntity kanmstEntity = kanmstComponent.getMKanmstEntity();
			if (StringUtils.isEmpty(kanmstEntity.getNitymd())) {
				// 日計日がない場合はエラー
				log.error(LogStringUtil.of("task").message("no nitymd error.").build());
				return;
			}
			//日計日が取得出来たら、Date型に変換。併せてFromとToで取得範囲の日付を設定。
			final Date nitymd = DateUtils.stringToDate(kanmstEntity.getNitymd());
			final String yyMMdd = DateUtils.formatFromDate(nitymd, "yyyy年MM月dd日");
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(nitymd);
			final int day = calendar.get(Calendar.DATE);
			final String year = Integer.toString(calendar.get(Calendar.YEAR));
			String MMfrom = "";
			String MMto = "";
			//calendarの月は0始まりなので、1月の場合は0になる。→12月になるように対応
			if (calendar.get(Calendar.MONTH) == 0) {
				MMfrom = "12";
			} else {
				MMfrom = Integer.toString((calendar.get(Calendar.MONTH)));
			}
			MMto = Integer.toString((calendar.get(Calendar.MONTH) + 1));
			//取得した月が1桁の場合は前ゼロ
			if (MMfrom.length() == 1) {
				MMfrom = "0" + MMfrom;
			}
			if (MMto.length() == 1) {
				MMto = "0" + MMto;
			}
			final String yyyyMMfrom = year + "-" + MMfrom + "-" + "21 00:00:00";
			// PRD_0209 && TEAM_ALBUS-41 add start
//			final String yyyyMMTo = year + "-" + MMto + "-" + "21 23:59:59";
			final String yyyyMMTo = year + "-" + MMto + "-" + "20 23:59:59";
			// PRD_0209 && TEAM_ALBUS-41 add end
			//フォルダ名用年月(yyyyMM)
			final String yyyyMM = year + MMto;
			//日計日が２１日ではない場合処理を終了する。
			if (day != 21) {
				return;
			}
			// 仕入伝票ファイル情報取得（消化委託かつ未送信）
			final List<TPurchaseFileInfoEntity> listTPurchasesVoucherEntity = getPurchaseVoucher(yyyyMMfrom, yyyyMMTo)
					.getContent();

			// PDF作成に必要な情報を取得
			final List<TPurchaseDigestionItemPDFEntity> listTPurchasesDigestionItemPDFEntity = getPurchaseDigestionItem(
					yyyyMMfrom, yyyyMMTo);

			//取得した情報に伝区は無いので設定をする
			List<TPurchaseDigestionItemPDFEntity> listPurchaseItem = createDenk(listTPurchasesDigestionItemPDFEntity);

			// ステータスを 処理中 に更新
			purchaseDigestionItemStatusComponent.updateStatusForBeingProcessed(listTPurchasesVoucherEntity, userId);

			// 仕入先、製品工場、入荷場所、伝区、品番、上代、単価でソートをする。
			final List<TPurchaseDigestionItemPDFEntity> sortedlistTPurchasesDigestionPDFEntity = getUniqueSizeList(
					listPurchaseItem);

			// 仕入伝票(消化委託)作成処理
			//データが無ければ処理を終了
			if (sortedlistTPurchasesDigestionPDFEntity.isEmpty()) {
				// 空の場合はエラー
				log.error(LogStringUtil.of("task").message("no sortedlistTPurchasesDigestionPDFEntity error. PDF作成情報が1件も存在しません。")
						.value("yyyyMMfrom", yyyyMMfrom)
						.value("yyyyMMto", yyyyMMTo)
						.build());
				return;
			}else {
			purchaseDigestionItemScheduleComponent.execute(sortedlistTPurchasesDigestionPDFEntity,yyyyMM,yyMMdd);
			}// ステータスを 完了 に更新
			purchaseDigestionItemStatusComponent.updateStatusForCompleted(listTPurchasesVoucherEntity, userId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 伝区の設定.
	 * @param List<TPurchaseDigestionItemPDFEntity> listTPurchasesDigestionItemPDFEntity
	 * @throws 伝区を追加したList<TPurchaseDigestionItemPDFEntity>
	 */
	private List<TPurchaseDigestionItemPDFEntity> createDenk(
			List<TPurchaseDigestionItemPDFEntity> listTPurchasesDigestionItemPDFEntity) {
		//伝票区分の設定
		for (int i = 0; i < listTPurchasesDigestionItemPDFEntity.size(); i++) {
			String pType = listTPurchasesDigestionItemPDFEntity.get(i).getPurchaseType().toString(); //仕入区分
			String sirkbn = listTPurchasesDigestionItemPDFEntity.get(i).getSirkbn();//仕入先区分

			String denk1 = "";
			String denk2 = "";
			//伝区の上桁を設定
			if (sirkbn.equals("00")) {
		        //PRD_0177 mod JFE start
//				if (pType.equals("3")) {//返品(3)の場合
	            if (pType.equals("RETURN_PURCHASE")) {//返品(3)の場合
		        //PRD_0177 mod JFE end
					denk1 = "6";//振替仕入返品
				} else {
					denk1 = "5";//振替仕入
				}
			} else {
                //PRD_0177 mod JFE start
//              if (pType.equals("3")) {//返品(3)の場合
                if (pType.equals("RETURN_PURCHASE")) {//返品(3)の場合
                //PRD_0177 mod JFE end
					denk1 = "4";//振替仕入返品
				} else {
					denk1 = "3";//振替仕入
				}
			}

			//伝区の下桁を設定 //費目は無いので01固定になる？ →下一桁を使用するので１
			denk2 = "1";//費目

			listTPurchasesDigestionItemPDFEntity.get(i).setPurchaseVoucherType(denk1 + denk2);
		}
		return listTPurchasesDigestionItemPDFEntity;
	}

	/**
	 * 仕入伝票管理を取得.
	 * @param yyyyMMFrom
	 * @param yyyyMMto
	 * @return 仕入伝票ファイル情報
	 * @throws Exception 例外
	 */
	private Page<TPurchaseFileInfoEntity> getPurchaseVoucher(String yyyyMMFrom, String yyyyMMto) throws Exception {
		//        // 処理前のデータを取得する
		return tPurchasesFileInfoRepository.findBySendStatus(yyyyMMFrom, yyyyMMto,
				PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("created_at"))));
	}

	/**
	 * PDF作成に必要な情報を取得.
	 * @param yyyyMMFrom
	 * @param yyyyMMto
	 * @return 仕入情報
	 * @throws Exception 例外
	 */
	private List<TPurchaseDigestionItemPDFEntity> getPurchaseDigestionItem(String yyyyMMFrom, String yyyyMMto)
			throws Exception {
		// 処理前のデータを取得する
		return tPurchaseDigestionItemPdfRepository.findByPurchaseDigestionItem(yyyyMMFrom, yyyyMMto,
				PageRequest.of(0, Integer.MAX_VALUE));
	}

	/**
	 * ソート順でソートした重複しないサイズコードリストを取得.
	 * @param purchaseList 仕入情報情報
	 * @return サイズコードリスト
	 */
	private List<TPurchaseDigestionItemPDFEntity> getUniqueSizeList(
			final List<TPurchaseDigestionItemPDFEntity> purchaseList) {
		return purchaseList.stream().sorted(
				// 仕入先でソートする。
				Comparator.comparing(TPurchaseDigestionItemPDFEntity::getSupplierCode)
						// 製品工場
						.thenComparing(Comparator.comparing(TPurchaseDigestionItemPDFEntity::getMdfMakerFactoryCode,
								Comparator.nullsFirst(Comparator.naturalOrder())))
						// 入荷場所
						.thenComparing(Comparator.comparing(TPurchaseDigestionItemPDFEntity::getArrivalPlace,
								Comparator.nullsFirst(Comparator.naturalOrder())))
						// 伝区
						.thenComparing(Comparator.comparing(TPurchaseDigestionItemPDFEntity::getPurchaseVoucherType,
								Comparator.nullsFirst(Comparator.naturalOrder())))
						// 品番
						.thenComparing(Comparator.comparing(TPurchaseDigestionItemPDFEntity::getPartNo,
								Comparator.nullsFirst(Comparator.naturalOrder())))
						// 上代
						.thenComparing(Comparator.comparing(TPurchaseDigestionItemPDFEntity::getRetailPrice,
								Comparator.nullsFirst(Comparator.naturalOrder())))
						// 単価
						.thenComparing(Comparator.comparing(TPurchaseDigestionItemPDFEntity::getUnitPrice,
								Comparator.nullsFirst(Comparator.naturalOrder()))))
				.collect(Collectors.toList());
	}
}
//PRD_0134 #10654 add JEF end
