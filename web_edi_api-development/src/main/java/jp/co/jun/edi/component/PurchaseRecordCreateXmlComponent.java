//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.ibm.icu.text.SimpleDateFormat;

import jp.co.jun.edi.component.model.PurchaseRecord.PurchaseRecordDetailXmlModel;
import jp.co.jun.edi.component.model.PurchaseRecord.PurchaseRecordHeadXmlModel;
import jp.co.jun.edi.component.model.PurchaseRecord.PurchaseRecordListXmlModel;
import jp.co.jun.edi.component.model.PurchaseRecord.PurchaseRecordSectionXmlModel;
import jp.co.jun.edi.component.model.PurchaseRecord.PurchaseRecordXmlModel;
import jp.co.jun.edi.entity.PurchaseRecordCsvEntity;
import jp.co.jun.edi.model.PurchaseRecordSearchConditionModel;
import jp.co.jun.edi.repository.PurchaseRecordCsvCompositeRepository;

/**
 * xmlを作成するコンポーネント.
 */
@Component
public class PurchaseRecordCreateXmlComponent {


	/** 1ページに表示可能なサイズの最大値(縦). */
	private static final int SIZE_DISPLAY_ROW_MAX_SIZE = 29;

	/** 頁の先頭行は仕入先名を表示するためのカウント */
	private Integer countEntity = 0;

	private String SUPPLIER_CODE = "";

	//仕入先ごとの合計
	private Integer countBrandPart = 0;
	private Integer sumBrandLot = 0;
	private BigDecimal sumBrandPrice = new BigDecimal("0");
	private BigDecimal sumMsuu = new BigDecimal("0");

	//出力内容の合計
	private Integer totalSumBrandLot = 0;
	private BigDecimal totalMsuu = new BigDecimal("0");
	private BigDecimal totalSumBrandPrice = new BigDecimal("0");

	@Autowired
	private PurchaseRecordCsvCompositeRepository purchaseRecordCsvCompositeRepository;

	/**
	 * XMLデータファイルを生成する.
	 *
	 * @param deliveryId 納品ID
	 * @param deliveryCount 納品依頼回数
	 * @param orderId 発注ID
	 * @param xmlPath XMLファイルパス
	 * @throws Exception 例外
	 * @throws IOException 例外
	 */
	public void createXml(final Path xmlPath, PurchaseRecordSearchConditionModel searchmodel)
			throws Exception, IOException {

		final PurchaseRecordListXmlModel model = new PurchaseRecordListXmlModel();
		// ヘッダ情報を生成
		final PurchaseRecordHeadXmlModel headModel = genaratedHeadXmlModel();

		//PRD_0152 del JFE start
//		// 各ページ情報を生成
//		final PurchaseRecordDetailXmlModel detailModel = new PurchaseRecordDetailXmlModel();
//		//ヘッダー情報(作成日と時刻）
//		detailModel.setPageHead(headModel);
		//PRD_0152 del JFE end
		model.setPageDetails(genaratedDetailXmlModel(headModel, searchmodel));

		// ModelをXMLデータに変換
		final StringWriter strModel = new StringWriter();
		JAXB.marshal(model, strModel);

		// XMLファイルを一時ディレクトリへ出力
		try (BufferedWriter writer = Files.newBufferedWriter(xmlPath)) {
			writer.append(strModel.toString());
		}
	}

	/**
	 * PickingListHeadXmlModel生成.
	 *
	 * @param deliveryId 納品ID
	 * @return ヘッダXMLモデル
	 */
	private PurchaseRecordHeadXmlModel genaratedHeadXmlModel() {

		final PurchaseRecordHeadXmlModel headModel = new PurchaseRecordHeadXmlModel();

		Date nowdate = new Date();
		SimpleDateFormat Dateformat = new SimpleDateFormat("yyyy/MM/dd  HH:mm");
		String d = Dateformat.format(nowdate);
		headModel.setDate(d);

		return headModel;
	}

	/**
	 * DetailXmlModel生成.
	 *
	 * @param deliveryId 納品ID
	 * @param headModel ヘッダモデル
	 * @param headSkuModels カラムヘッダ(sku)
	 * @return 更新後のページ(1~)
	 */
	private List<PurchaseRecordDetailXmlModel> genaratedDetailXmlModel(
			final PurchaseRecordHeadXmlModel headModel,
			final PurchaseRecordSearchConditionModel searchmodel) {

		//合計を初期化
		countEntity = 0;
		totalSumBrandLot = 0;
		totalMsuu = new BigDecimal("0");
		totalSumBrandPrice = new BigDecimal("0");

		final PageRequest pageRequest = PageRequest.of(0, 100);

		final List<PurchaseRecordCsvEntity> entities = purchaseRecordCsvCompositeRepository
				.findPDFDetailBySearchCondition(searchmodel, pageRequest);

		//仕入先コード単位で出力内容まとめる。
		//まとめたらそれをループさせる。各配列の最後に集計行を追加してみる。
		final List<PurchaseRecordXmlModel> recordModels = new ArrayList<PurchaseRecordXmlModel>();
		final Map<String, List<PurchaseRecordCsvEntity>> map = entities.stream()
		     // PRD_0189 #10181 jfe add start
                // 仕入先でソートする。消化委託は最後
		        .sorted(Comparator.comparing(PurchaseRecordCsvEntity::getSupplierCode,Comparator.nullsLast(Comparator.naturalOrder())))
		     // PRD_0189 #10181 jfe add end
				.collect(Collectors.groupingBy(PurchaseRecordCsvEntity::getSupplierCode));

		map.values().forEach(v -> {
			final List<PurchaseRecordXmlModel> rModels = v.stream()
					.map(entity -> createModel(entity))
					.collect(Collectors.toList());
			recordModels.addAll(rModels);
			//集計行作る。
			final PurchaseRecordXmlModel totalRow = new PurchaseRecordXmlModel();
			totalRow.setSupplierCode("<仕入先計>");
			totalRow.setPartNo(countBrandPart.toString());
			totalRow.setFixArrivalCount(sumBrandLot.toString());
			totalRow.setUnitPriceSum(sumBrandPrice.toString());
			totalRow.setMKyu(sumMsuu.toString());
			recordModels.add(totalRow);
			countEntity += 1;
		});

		//最終行に全部の合計を設定する
		final PurchaseRecordXmlModel resultRow = new PurchaseRecordXmlModel();
		resultRow.setSupplierCode("<合計>");
		resultRow.setFixArrivalCount(totalSumBrandLot.toString());
		resultRow.setMKyu(totalMsuu.toString());
		resultRow.setUnitPriceSum(totalSumBrandPrice.toString());
		recordModels.add(resultRow);

		//ここからページ(と集計)を設定
		final int pageRowCnt = (recordModels.size() / SIZE_DISPLAY_ROW_MAX_SIZE) + 1;

		final List<PurchaseRecordDetailXmlModel> tmpDetailModels = new ArrayList<PurchaseRecordDetailXmlModel>();
		for (int pageRowIdx = 0; pageRowIdx < pageRowCnt; pageRowIdx++) {
			// ページに収まるrowの数を設定
			final Integer startRowIdx = (pageRowIdx) * SIZE_DISPLAY_ROW_MAX_SIZE;
			Integer endRowIdx = (pageRowIdx + 1) * SIZE_DISPLAY_ROW_MAX_SIZE;
			if (endRowIdx > recordModels.size()) {
				endRowIdx = recordModels.size();
			}
			List<PurchaseRecordXmlModel> subModels = recordModels.subList(
					startRowIdx,
					endRowIdx);

			// セクション登録

			PurchaseRecordSectionXmlModel pageRecordSection = new PurchaseRecordSectionXmlModel();
			pageRecordSection.setDetails(subModels);

			// ページセクションの生成
			PurchaseRecordDetailXmlModel pageModel = new PurchaseRecordDetailXmlModel();
			pageModel.setPageHead(headModel);

			// ページ番号を設定(仮置き。外で振る)
			pageModel.setPageNumber(String.valueOf((pageRowIdx + 1)));
			pageModel.setRecordSection(pageRecordSection);

			tmpDetailModels.add(pageModel);
		}
		return tmpDetailModels;
	}

	/**
	 * レコードを生成.
	 *
	 * @param entity DB登録データ
	 * @param deliveryId 納品ID
	 * @param entity sku数
	 * @param skuEntities 行のskuに登録対象のエンティティ
	 * @return 一覧データモデル
	 */
	private PurchaseRecordXmlModel createModel(
			final PurchaseRecordCsvEntity entity) {
	  //PRD_0195 JFE mod start
//		final PurchaseRecordXmlModel model = new PurchaseRecordXmlModel();
//
//		BeanUtils.copyProperties(entity, model);
//
//		//PDF出力用に、仕入先コードの後ろにスペースを追加する。
//		String supCode = model.getSupplierCode();
//		model.setSupplierCode(supCode + " ");
//		//PRD_0193 #11702 JFE add start
//        String logCode = model.getLogisticsCode();//入荷場所
//		//PRD_0193 #11702 JFE add end
//
//		if (SUPPLIER_CODE.equals(entity.getSupplierCode()) && !(countEntity % SIZE_DISPLAY_ROW_MAX_SIZE == 0)) {
//			model.setSupplierCode("");
//			model.setSupplierName("");
//			model.setArrivalPlace("");
//			model.setLogisticsCode("");
//		} else {
//			SUPPLIER_CODE = entity.getSupplierCode();
//			//ブランド単位の集計用変数初期化
//			countBrandPart = 0;
//			sumBrandLot = 0;
//			sumBrandPrice = new BigDecimal("0");
//			sumMsuu = new BigDecimal("0");
//		}
//
//		//伝区用
//		String pType =  model.getPurchaseType(); //仕入区分
//		String sirkbn = model.getSirkbn();//仕入先区分
//		String hmk = model.getExpense_item();//費目
//		Integer orderNo = model.getOrder_no();//発注No
//
//		String denk1 = "";
//		String denk2 = "";
//        //PRD_0193 #11702 JFE add start
//        String pCount = model.getPurchase_count();//引取回数
//        if((pType.equals("3")||pType.equals("9")) && (logCode.equals("18")||logCode.equals("19"))) {
//            model.setPurchaseType(pCount);
//        }else {
//        //PRD_0193 #11702 JFE add end
//        // PRD_0189 #10181 jfe add start
//		//PRD_0190 mod JFE start
////        if(orderNo != null && !pType.isEmpty() && !sirkbn.isEmpty() && !hmk.isEmpty()) {
//        if(orderNo != null && pType != null && sirkbn != null && hmk != null) {
//        //PRD_0190 mod JFE end
//        // PRD_0189 #10181 jfe add end
//		//伝区の上桁を設定
//		if (sirkbn.equals("00")) {
//			if(pType.equals("3")) {//返品(3)の場合
//				denk1 = "6";//振替仕入返品
//			}else {
//				denk1 = "5";//振替仕入
//			}
//		}else {
//			if(pType.equals("3")) {//返品(3)の場合
//				denk1 = "4";//振替仕入返品
//			}else {
//				denk1 = "3";//振替仕入
//			}
//		}
//
//		//伝区の下桁を設定
//		if(hmk.isEmpty() || orderNo ==0) {
//			hmk = "01";
//		}
//
//		if (hmk.equals("01") || hmk.equals("04")|| hmk.equals("24") || hmk.equals("05")) {
//			denk2 = hmk.substring(hmk.length() -1);
//		}else if( hmk.equals("20")|| hmk.equals("30")) {
//			denk2 = hmk.substring(0,1);
//		}
//        // PRD_0189 #10181 jfe add start
//        }
//        // PRD_0189 #10181 jfe add end
//		model.setPurchaseType(denk1 + denk2);
//		//PRD_0193 #11702 JFE add start
//        }
//		//PRD_0193 #11702 JFE add end
//
//		countEntity += 1;
//		countBrandPart += 1;
//		sumBrandLot += Integer.parseInt(entity.getFixArrivalCount());
//		sumBrandPrice = sumBrandPrice.add(new BigDecimal(entity.getUnitPriceSum()));
//		sumMsuu = sumMsuu.add(new BigDecimal(entity.getMKyu()));
//		totalSumBrandLot += Integer.parseInt(entity.getFixArrivalCount());
//		totalMsuu = totalMsuu.add(new BigDecimal(entity.getMKyu()));
//		totalSumBrandPrice = totalSumBrandPrice.add(new BigDecimal(entity.getUnitPriceSum()));
//		return model;
		final PurchaseRecordXmlModel model = new PurchaseRecordXmlModel();

		BeanUtils.copyProperties(entity, model);

		//PDF出力用に、仕入先コードの後ろにスペースを追加する。
		String supCode = model.getSupplierCode();
		model.setSupplierCode(supCode + " ");
		//PRD_0193 #11702 JFE add start
        String logCode = model.getLogisticsCode();//入荷場所
		//PRD_0193 #11702 JFE add end

		if (SUPPLIER_CODE.equals(entity.getSupplierCode()) && !(countEntity % SIZE_DISPLAY_ROW_MAX_SIZE == 0)) {
			model.setSupplierCode("");
			model.setSupplierName("");
			model.setArrivalPlace("");
			model.setLogisticsCode("");
		} else {
			SUPPLIER_CODE = entity.getSupplierCode();
			//ブランド単位の集計用変数初期化
			countBrandPart = 0;
			sumBrandLot = 0;
			sumBrandPrice = new BigDecimal("0");
			sumMsuu = new BigDecimal("0");
		}

		//伝区用
		String pType =  model.getPurchaseType(); //仕入区分
		String sirkbn = model.getSirkbn();//仕入先区分
		String hmk = model.getExpense_item();//費目
		Integer orderNo = model.getOrder_no();//発注No

		String denk1 = "";
		String denk2 = "";
        //PRD_0193 #11702 JFE add start
        String pCount = model.getPurchase_count();//引取回数

        if(pType == null) pType = "";
        if(logCode == null) logCode = "";
        if(orderNo == null || orderNo == 0) hmk = "01";//発注情報が存在しない場合は費目を01に設定する

        //PRD_0202 JFE mod start
//        if((pType.equals("3")||pType.equals("9")) && (logCode.equals("18")||logCode.equals("19"))) {
//            model.setPurchaseType(pCount);
//        }else if(pType.equals("2")) {
//            model.setPurchaseType("32");
//        }else if(pType.equals("4")) {
//            model.setPurchaseType("33");
//        }else if(pType.equals("5")) {
//            model.setPurchaseType("");
        if((pType.equals("2")||pType.equals("5")||pType.equals("9"))){
            model.setPurchaseType(pCount);
        }else if(pType.equals("4")) {
            model.setPurchaseType("33");
        }else if(pType.equals("3")&&(logCode.equals("18")||logCode.equals("19"))) {
            model.setPurchaseType(pCount);
        //PRD_0202 JFE mod end
        }else {
            if(sirkbn != null) {
		//伝区の上桁を設定
		if (sirkbn.equals("00")) {
			if(pType.equals("3")) {//返品(3)の場合
				denk1 = "6";//振替仕入返品
			}else {
				denk1 = "5";//振替仕入
			}
		}else {
			if(pType.equals("3")) {//返品(3)の場合
				denk1 = "4";//振替仕入返品
			}else {
				denk1 = "3";//振替仕入
			}
		}

		//伝区の下桁を設定
		if (hmk.equals("01") || hmk.equals("04")|| hmk.equals("24") || hmk.equals("05")) {
			denk2 = hmk.substring(hmk.length() -1);
		}else if( hmk.equals("20")|| hmk.equals("30")) {
			denk2 = hmk.substring(0,1);
		}
        // PRD_0189 #10181 jfe add start
        }
        // PRD_0189 #10181 jfe add end
		model.setPurchaseType(denk1 + denk2);
		//PRD_0193 #11702 JFE add start
        }
		//PRD_0193 #11702 JFE add end

		countEntity += 1;
		countBrandPart += 1;
		sumBrandLot += Integer.parseInt(entity.getFixArrivalCount());
		sumBrandPrice = sumBrandPrice.add(new BigDecimal(entity.getUnitPriceSum()));
		sumMsuu = sumMsuu.add(new BigDecimal(entity.getMKyu()));
		totalSumBrandLot += Integer.parseInt(entity.getFixArrivalCount());
		totalMsuu = totalMsuu.add(new BigDecimal(entity.getMKyu()));
		totalSumBrandPrice = totalSumBrandPrice.add(new BigDecimal(entity.getUnitPriceSum()));
		return model;
	//PRD_0195 JFE mod end
	}

}
//PRD_0133 #10181 add JFE end