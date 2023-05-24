package jp.co.jun.edi.component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.constants.CharsetsConstants;
import jp.co.jun.edi.entity.TagdatEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * TAGDAT CSVファイルを作成し、CSVファイルを削除するコンポーネント.
 */
@Component
@Slf4j
public class TagdatCreateCsvFileComponent {

    // CSV設定
    private static final CSVFormat CSV_FORMAT = CSVFormat.EXCEL.withEscape('"').withQuoteMode(QuoteMode.NONE).withRecordSeparator("\r\n").withDelimiter(',').withNullString("");

    /** CSVファイル：接頭辞. */
    private static final String TMP_FILE_PREFIX_INFO = "Tagdat";

    /** CSVファイル：接尾辞. */
    private static final String TMP_FILE_SUFFIX_INFO = ".csv";

    @Autowired
    private PropertyComponent propertyComponent;

    /**
     * CSVファイルを作成する.
     *
     * @param userId
     *            ユーザID
     * @param listTagdatEntity
     *            TAGDAT情報エンティティ
     * @return 生成したファイルのリスト
     * @throws Exception
     *             IOException
     */
    public File createCsvFile(final BigInteger userId, final List<TagdatEntity> listTagdatEntity) throws Exception {
    	// 1行目取得
    	final TagdatEntity firstEntity = listTagdatEntity.get(0);

    	// 一時フォルダ取得
    	final Path dirPath = Paths.get(propertyComponent.getBatchProperty().getTagdatProperty().getTmpDirectory());

        // CSVファイルを作成
        final File fileTagdatInfo = new File(dirPath.toFile(),
        		TMP_FILE_PREFIX_INFO + firstEntity.getBrkg() + TMP_FILE_SUFFIX_INFO);
        createTagdatInfo(fileTagdatInfo, listTagdatEntity);

        return fileTagdatInfo;

    }

    /**
     * ファイルを削除する.
     *
     * @param files
     *            CSVファイル
     * @throws IOException
     *             IOException
     */
    public void deleteFiles(final List<File> files) throws IOException {
        for (final File file : files) {
            // ファイルを削除
            if (file != null && file.exists()) {
                if (!file.delete()) {
                    log.error("tagdat attachement temporary filedelete error.");
                }
            }
        }
    }

    /**
     * CSVファイルを作成.
     *
     * @param file
     *            ファイル
     * @param listTagdatEntity
     *            TAGDAT情報リストエンティティ
     * @throws IOException
     *             IOException
     */
    private void createTagdatInfo(final File file, final List<TagdatEntity> listTagdatEntity) throws IOException {
        try (Writer wt = new OutputStreamWriter(new FileOutputStream(file), CharsetsConstants.MS932); CSVPrinter printer = new CSVPrinter(wt, CSV_FORMAT);) {

            // ボディ行
        	printBody(printer, listTagdatEntity);
        }
    }

    /**
     * CSVボディ行を出力する.
     *
     * @param printer
     *            PrintWriter
     * @param listTagdatEntity
     *            TAGDAT情報リストエンティティ
     * @throws IOException
     *             IOException
     */
    private void printBody(final CSVPrinter printer, final List<TagdatEntity> listTagdatEntity) throws IOException {

        // ボディ行出力
        for (TagdatEntity tagdatEntity : listTagdatEntity) {
            printer.printRecord(tagdatEntity.getCrtymd(), // 作成日
            		tagdatEntity.getBrkg(), // ブランド
            		tagdatEntity.getSeq(), // SEQ
            		tagdatEntity.getDatrec(), // 年度
            		tagdatEntity.getSeason(), // シーズン
            		tagdatEntity.getHskg(), // 品番・品種
            		tagdatEntity.getTuban(), // 品番・通番
            		tagdatEntity.getJodai(), // 上代
            		tagdatEntity.getHacno(), // 発注番号
            		tagdatEntity.getNkai(), // 引取回数
            		tagdatEntity.getIro(), // カラー
            		tagdatEntity.getSize(), // サイズコード
            		tagdatEntity.getSzkg(), // サイズ記号
            		tagdatEntity.getNw7(), // NW7
            		tagdatEntity.getJan(), // JAN
            		tagdatEntity.getZjodai(), // 税込上代
            		tagdatEntity.getYobi(), // 予備
            		tagdatEntity.getCrthms(), // 作成時間
            		tagdatEntity.getSyubt() // 送信種別
            );
        }
    }
}
