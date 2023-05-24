package jp.co.jun.edi.component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jp.co.jun.edi.constants.CharsetsConstants;
import jp.co.jun.edi.model.DeliveryStoreSkuFormValue;
import jp.co.jun.edi.model.DeliveryStoreUploadCsvModel;
import jp.co.jun.edi.model.DeliveryStoreUploadCsvStore;
import lombok.extern.slf4j.Slf4j;

/**
 * 店舗配分アップロードファイルを読み込むコンポーネント.
 * @author jun
 *
 */
@Slf4j
@Component
public class DeliveryStoreUploadCsvReadComponent {
    /** CSV設定. */
    private static final CSVFormat CSV_FORMAT =
            CSVFormat.DEFAULT // デフォルトのCSV形式を指定
            .withIgnoreEmptyLines(false) // 空行を無視する
            .withIgnoreSurroundingSpaces(true) // 値をtrimして取得する
            .withRecordSeparator("\r\n") // 改行コードCRLF
            .withDelimiter(',') // 区切りカンマ
            .withEscape('"') // エスケープ文字ダブルクォート
            .withQuoteMode(QuoteMode.NONE); // 囲み文字すべて

    /** 発注番号行. */
    private static final int NUMBER_0 = 0;

    /** 回数行、発注番号・回数・品番列. */
    private static final int NUMBER_1 = 1;

    /** 店舗コード列. */
    private static final int NUMBER_2 = 2;

    /** 品番列.*/
    private static final int NUMBER_3 = 3;

    /** 配分開始列*/
    private static final int NUMBER_5 = 5;

// PRD_0119#8396 mod JFE start
/*    *//** 色行.*//*
    private static final int NUMBER_6 = 6;

    *//** サイズ行.*//*
    private static final int NUMBER_7 = 7;
*/
    /** SKU行. */
    private static final int NUMBER_6 = 6;
// PRD_0119#8396 mod JFE end

    /** 明細. */
    private static final String DETAIL = "明細";

    /**
     * CSVデータを読み込む.
     * CSVファイルフォーマット、文字コードはデフォルトを使用
     *
     * @param file CSVファイル
     * @return データ取込用CSVファイルModelリスト
     * @throws Exception 例外
     */
	//PRD_0120#8343 mod JFE start
    public DeliveryStoreUploadCsvModel readCsvData(final MultipartFile file) {
    //public List<DeliveryStoreUploadCsvModel> readCsvData(final MultipartFile file) {
        // List<DeliveryStoreUploadCsvModel> returnModels;
        DeliveryStoreUploadCsvModel returnModels;
    	//PRD_0120#8343 mod JFE End
        try {
            returnModels = readCsvData(file, CSV_FORMAT, CharsetsConstants.SJIS);
        } catch (Exception e){
        	returnModels = null;
        	log.error(e.getMessage(), e);
        }
        return returnModels;
    }

    /**
     * CSVデータを読み込む.
     *
     * @param file CSVファイル
     * @param format CSVフォーマット
     * @param charSet 文字コード
     * @return データ取込用CSVファイルModelリスト
     * @throws Exception 例外
     */
	//PRD_0120#8343 mod JFE start
//    public List<DeliveryStoreUploadCsvModel> readCsvData(final MultipartFile file, final CSVFormat format, final Charset charSet) throws Exception {
     public DeliveryStoreUploadCsvModel readCsvData(final MultipartFile file, final CSVFormat format, final Charset charSet) throws Exception {
	//PRD_0120#8343 mod JFE End
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), charSet));
                CSVParser parser = format.parse(br);) {
        	//PRD_0120#8343 mod JFE start
//        	final List<DeliveryStoreUploadCsvModel> returnModels = new ArrayList<DeliveryStoreUploadCsvModel>();
//            int rowCount = 0;
//            String orderNo = ""; // 発注番号
//            String deliveryCount = ""; // 回数
//            String partNo = ""; // 品番
//            List<String> colors = new ArrayList<String>();
//            List<String> sizes = new ArrayList<String>();
//
//
//            for(final CSVRecord csvRecord : parser.getRecords()) {
//                switch(rowCount) {
//                case NUMBER_0: // 発注番号
//                    orderNo = csvRecord.get(NUMBER_1);
//                    break;
//                case NUMBER_1: // 回数
//                    deliveryCount = csvRecord.get(NUMBER_1);
//                    break;
//                case NUMBER_3: // 品番
//                    partNo = csvRecord.get(NUMBER_1);
            final DeliveryStoreUploadCsvModel returnModels = new DeliveryStoreUploadCsvModel();
            returnModels.setStores(new ArrayList<DeliveryStoreUploadCsvStore>());
            int rowCount = 0;
            List<String> colors = new ArrayList<String>();
            List<String> sizes = new ArrayList<String>();


            for(final CSVRecord csvRecord : parser.getRecords()) {
                switch(rowCount) {
                case NUMBER_0: // 発注番号
                	returnModels.setOrderNo(csvRecord.get(NUMBER_1));
                    break;
                case NUMBER_1: // 回数
                	returnModels.setDeliveryCount(csvRecord.get(NUMBER_1));
                    break;
                case NUMBER_3: // 品番
                    returnModels.setPartNo(csvRecord.get(NUMBER_1));
                    break;
                //PRD_0120#8343 mod JFE end
                // PRD_0119#8396 mod JFE start
                /*case NUMBER_6: // 色
                    for(int i = NUMBER_5; i < csvRecord.size(); i++) {
                        if(csvRecord.get(i).equals("")) {
                            break;
                        }
                        colors.add(StringUtils.leftPad(csvRecord.get(i), 2, '0'));
                    }
                    break;
                case NUMBER_7: // サイズ
                    for(int i = NUMBER_5; i < csvRecord.size(); i++) {
                        if(csvRecord.get(i).equals("")) {
                            break;
                        }
                        sizes.add(csvRecord.get(i));
                    }
                    break;*/
                 case NUMBER_6: // SKU
                	 for(int i = NUMBER_5; i < csvRecord.size(); i++) {
                		 if(csvRecord.get(i).equals("")) {
                             break;
                         }
                		 String[] strings = csvRecord.get(i).split("-");
                		 colors.add(strings[1]); // 色
                		 sizes.add(strings[2]);  // サイズ
                	 }
                	 break;
                // PRD_0119#8396 mod JFE end
                default:
                    if(csvRecord.get(NUMBER_0).equals(DETAIL)) {
                        //PRD_0120#8343 mod JFE Start
                        // convertCsvDataToModel(csvRecord, returnModels, orderNo, deliveryCount, partNo, colors, sizes);
                        convertCsvDataToModel(csvRecord, returnModels, colors, sizes);
                        //PRD_0120#8343 mod JFE end
                    }
                    break;
                }

                rowCount++;
            }

            return returnModels;
        }
    }
	//PRD_0120#8343 mod JFE start
//    public void convertCsvDataToModel(
//    		final CSVRecord csvRecord,
//    		final List<DeliveryStoreUploadCsvModel> returnModels,
//    		final String orderNo,
//    		final String deliveryCount,
//    		final String partNo,
//    		final List<String> colors,
//    		final List<String> sizes
//    		) {
//        final int sku = colors.size();
//
//        for(int i = 0; i < sku; i++) {
//            final DeliveryStoreUploadCsvModel model = new DeliveryStoreUploadCsvModel();
//
//            // 発注番号
//            model.setOrderNo(orderNo);
//            // 回数
//            model.setDeliveryCount(deliveryCount);
//            // 品番
//            model.setPartNo(partNo);
//            // 店舗コード
//            model.setStoreCode(csvRecord.get(NUMBER_2));
//            // 色
//            model.setColorCode(colors.get(i));
//            // サイズ
//            model.setSize(sizes.get(i));
//            // 配分数
//            model.setDeliveryLot(csvRecord.get(NUMBER_5 + i));
//
//            returnModels.add(model);
//        }
//    }
    public void convertCsvDataToModel(
    		final CSVRecord csvRecord,
    		final DeliveryStoreUploadCsvModel returnModels,
    		final List<String> colors,
    		final List<String> sizes
    		) {
        final int sku = colors.size();
        final DeliveryStoreUploadCsvStore store = new DeliveryStoreUploadCsvStore();
        final List<DeliveryStoreSkuFormValue> deliveryStoreSkuFormValues = new ArrayList<>();
        // 店舗コード
        store.setStoreCode(csvRecord.get(NUMBER_2));

        for(int i = 0; i < sku; i++) {
        	final DeliveryStoreSkuFormValue value = new DeliveryStoreSkuFormValue();
            // 色
        	value.setColorCode(colors.get(i));
            // サイズ
        	value.setSize(sizes.get(i));
            // 配分数
        	value.setDeliveryLot(csvRecord.get(NUMBER_5 + i));

        	deliveryStoreSkuFormValues.add(value);
        }

        store.setDeliveryStoreSkuFormValues(deliveryStoreSkuFormValues);
        returnModels.getStores().add(store);
    }
	//PRD_0120#8343 mod JFE end
}
