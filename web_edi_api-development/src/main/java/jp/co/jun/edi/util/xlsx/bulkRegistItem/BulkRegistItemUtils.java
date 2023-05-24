package jp.co.jun.edi.util.xlsx.bulkRegistItem;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import jp.co.jun.edi.component.model.BulkRegistItemPropertyModel;
import jp.co.jun.edi.util.xlsx.XlsxSheetReader;
import jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet.CompositionSheet;
import jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet.ItemSheet;
import jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet.SkuJanSheet;
import jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet.SkuUpcSheet;

/**
 * 品番・商品一括登録Excel操作用ユーティリティ.
 */
public final class BulkRegistItemUtils {
    /**
     */
    private BulkRegistItemUtils() {
    }

    /**
     * Excelリーダーを生成する.
     *
     * @param inp The {@link InputStream} to read data from.
     * @param property {@link BulkRegistItemPropertyModel} instance.
     * @return {@link Reader}
     * @throws IOException if an error occurs while reading the data
     * @throws EncryptedDocumentException If the Workbook given is password protected
     */
    public static Reader getReader(
            final InputStream inp,
            final BulkRegistItemPropertyModel property) throws IOException, EncryptedDocumentException {
        return new Reader(WorkbookFactory.create(inp), property);
    }

    /**
     * Excelリーダー.
     */
    public static class Reader implements Closeable {
        private final Workbook workbook;
        private final XlsxSheetReader<String, ItemSheet> itemSheetReader;
        private final XlsxSheetReader<String, SkuJanSheet> skuJanSheetReader;
        private final XlsxSheetReader<String, SkuUpcSheet> skuUpcSheetReader;
        private final XlsxSheetReader<String, CompositionSheet> compositionSheetReader;

        private Map<String, BulkRegistItem> map;

        /**
         * @param workbook {@link Workbook} instance.
         * @param property {@link BulkRegistItemPropertyModel} instance.
         */
        public Reader(
                final Workbook workbook,
                final BulkRegistItemPropertyModel property) {
            this.workbook = workbook;
            itemSheetReader = ItemSheet.getReader(workbook)
                    .maxSize(property.getItemMaxSize())
                    .initialCapacity(property.getItemInitialCapacity());
            skuJanSheetReader = SkuJanSheet.getReader(workbook)
                    .maxSize(property.getSkuJanMaxSize())
                    .initialCapacity(property.getSkuJanInitialCapacity());
            skuUpcSheetReader = SkuUpcSheet.getReader(workbook)
                    .maxSize(property.getSkuUpcMaxSize())
                    .initialCapacity(property.getSkuUpcInitialCapacity());
            compositionSheetReader = CompositionSheet.getReader(workbook)
                    .maxSize(property.getCompositionMaxSize())
                    .initialCapacity(property.getCompositionInitialCapacity());
        }

        /**
         * シート読み込み.
         *
         * @return データリスト
         */
        public List<BulkRegistItem> read() {
            // 商品・品番の読み込み
            readItem();

            // SKU(JAN)の読み込み
            readSkuJan();

            // SKU(UPC)の読み込み
            readSkuUpc();

            // 組成の読み込み
            readComposition();

            return new ArrayList<>(map.values());
        }

        /**
         * シート情報取得.
         *
         * @return シート情報
         */
        public BulkRegistItemInfo getInfo() {
            return BulkRegistItemInfo.builder()
                    .item(BulkRegistItemSheetInfo.builder()
                            .sheetName(itemSheetReader.getSheetName())
                            .over(itemSheetReader.hasNext())
                            .maxSize(itemSheetReader.getMaxSize())
                            .build())
                    .skuJan(BulkRegistItemSheetInfo.builder()
                            .sheetName(skuJanSheetReader.getSheetName())
                            .over(skuJanSheetReader.hasNext())
                            .maxSize(skuJanSheetReader.getMaxSize())
                            .build())
                    .skuUpc(BulkRegistItemSheetInfo.builder()
                            .sheetName(skuUpcSheetReader.getSheetName())
                            .over(skuUpcSheetReader.hasNext())
                            .maxSize(skuUpcSheetReader.getMaxSize())
                            .build())
                    .composition(BulkRegistItemSheetInfo.builder()
                            .sheetName(compositionSheetReader.getSheetName())
                            .over(compositionSheetReader.hasNext())
                            .maxSize(compositionSheetReader.getMaxSize())
                            .build())
                    .build();
        }

        /**
         * 商品・品番の読み込み.
         */
        private void readItem() {
            final List<ItemSheet> rows = itemSheetReader.read();

            map = new LinkedHashMap<>(rows.size());

            rows.forEach((row) -> {
                getValue(row.getPartNo()).getItems().add(row);
            });
        }

        /**
         * SKU(JAN)の読み込み.
         */
        private void readSkuJan() {
            skuJanSheetReader.read().forEach((row) -> {
                getValue(row.getPartNo()).getSkuJans().add(row);
            });
        }

        /**
         * SKU(UPC)の読み込み.
         */
        private void readSkuUpc() {
            skuUpcSheetReader.read().forEach((row) -> {
                getValue(row.getPartNo()).getSkuUpcs().add(row);
            });
        }

        /**
         * 組成の読み込み.
         */
        private void readComposition() {
            compositionSheetReader.read().forEach((row) -> {
                getValue(row.getPartNo()).getCompositions().add(row);
            });
        }

        /**
         * マップから値を取得する.
         * 値がない場合は、値をマップに追加する.
         *
         * @param key キー
         * @return 値
         */
        private BulkRegistItem getValue(final String key) {
            BulkRegistItem value = map.get(key);

            if (value == null) {
                value = BulkRegistItem.builder()
                        .partNo(key)
                        .items(new ArrayList<>())
                        .skuJans(new ArrayList<>())
                        .skuUpcs(new ArrayList<>())
                        .compositions(new ArrayList<>())
                        .build();

                map.put(key, value);
            }

            return value;
        }

        @Override
        public void close() throws IOException {
            this.workbook.close();
        }
    }
}
