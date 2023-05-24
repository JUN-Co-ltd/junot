package jp.co.jun.edi.component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.constants.CharsetsConstants;
import jp.co.jun.edi.entity.schedule.LinkingCreateCsvFileCommonEntity;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.BusinessType;

/**
 * 指示CSV作成コンポーネント.
 * @param <T>
 */
@Component
public abstract class LinkingCreateCsvFileComponent<T extends LinkingCreateCsvFileCommonEntity>
extends GenericComponent {

    @Autowired
    private PropertyComponent propertyComponent;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // CSV設定
    private static final CSVFormat CSV_FORMAT =
            CSVFormat.EXCEL.withEscape('"').withQuoteMode(QuoteMode.NONE)
            .withRecordSeparator("\r\n").withDelimiter(',').withNullString("");

    // CSVファイル接尾辞：.csv
    private static final String CSV_FILE_SUFFIX = ".csv";

    /**
     * 一時フォルダ名とファイル名が同一の場合、CSVファイルを作成し、一時フォルダに出力する.
     *
     * @param entites 指示ファイル作成データリスト
     * @return 生成したCSVファイル
     * @throws Exception IOException
     */
    public File createCsvFile(final List<T> entites) throws Exception {
        return createCsvFile(entites, getBusinessType());
    }

    /**
     * 一時フォルダ名とファイル名が異なる場合、CSVファイルを作成し、一時フォルダに出力する.
     *
     * @param entites 指示ファイル作成データリスト
     * @param filePrefix ファイル名接頭辞
     * @return 生成したCSVファイル
     * @throws Exception IOException
     */
    public File createCsvFile(final List<T> entites, final BusinessType filePrefix) throws Exception {
        // CSVファイル一時保存パス取得：/opt/junot-api/tmp/shipment/[XX]
        // ([XX]:業務区分)
        final String tmpDirectory = propertyComponent.getBatchProperty().getShipmentProperty().getShipmentTmpDirectory()
                + getBusinessType().getValue();
        final Path csvPath = Paths.get(tmpDirectory);

        // 指示データファイル名：[XX]_JUNOT_日付(YYYYMMDD)_時刻(HHMMSS).csv
        // 例）[XX]_JUNOT_20200403_130000.csv
        final String systemName = propertyComponent.getBatchProperty().getShipmentProperty().getSystemName();
        final String fileName = filePrefix.getValue() + "_" + systemName + "_"
                + ZonedDateTime.now().format(DATE_TIME_FORMATTER) + CSV_FILE_SUFFIX;

        // CSVファイルを作成し、一時ディレクトリへ出力
        final File instructionFile = new File(csvPath.toFile(), fileName);
        createShipmentFileInfo(instructionFile, entites);

        return instructionFile;
    }

    /**
     * 指示データファイルを削除する.
     * 一時保存ディレクトリ(/opt/junot-api/tmp/shipment/[XX])内のデータを全て削除
     *
     * @throws Exception IOException
     */
    public void deleteFiles() throws Exception {
        final String tmpDirectory =
                propertyComponent.getBatchProperty().getShipmentProperty().getShipmentTmpDirectory()
                + getBusinessType().getValue();
        final File instructionDir = new File(tmpDirectory);
        FileUtils.cleanDirectory(instructionDir);
    }

    /**
     * 指示データファイルを作成.
     *
     * @param file 指示データファイル
     * @param entites 指示ファイル作成データリスト
     * @throws Exception 例外
     */
    private void createShipmentFileInfo(
            final File file,
            final List<T> entites) throws Exception {
        try (Writer wt = new OutputStreamWriter(new FileOutputStream(file), CharsetsConstants.MS932);
                CSVPrinter printer = new CSVPrinter(wt, CSV_FORMAT);) {
            for (T entity : entites) {
                // ボディ行を出力する
                createRecord(printer, entity);
            }
        }
    }

    /**
     * 業務区分を返す.
     *
     * @return 業務区分
     */
    abstract BusinessType getBusinessType();

    /**
     * レコードに書き込む中身を定義する.
     *
     *
     * @param printer CSVPrinter
     * @param entity 指示ファイル作成データ
     * 記述例：
     * printer.printRecord(
     *     DateUtils.formatFromDate(entity.getSqManageDate(), "yyyyMMdd"), // 管理情報日付
     *     DateUtils.formatFromDate(entity.getSqManageAt(), "HHmmss"), // 管理情報時間
     * :
     * );
     * @throws Exception 例外
     */
    abstract void createRecord(CSVPrinter printer, T entity) throws Exception;

    /**
     * 入力型: Integerを出力型:BigDecimalに変換する.
     * 値がない場合はnullを返却
     *
     * @param value Integer型の数値
     * @return BigDecimal型の数値
     */
    public BigDecimal generateIntegerToBigDecimal(final Integer value) {
        if (value == null) {
            return null;
        } else {
            return BigDecimal.valueOf(value);
        }
    }

    /**
     * 入力型: BooleanTypeを出力型:Stringに変換する.
     * TRUEの場合は"1"を返却、FALSEの場合は"0"を返却
     * 値がない場合はnullを返却
     *
     * @param value BooleanType型の値
     * @return String型のパラメータ
     */
    public String generateBooleanTypeToString(final BooleanType value) {
        if (value == BooleanType.FALSE) {
            return "0";
        }
        if (value == BooleanType.TRUE) {
            return "1";
        }
        return null;
    }

    /**
     * 入力型: Stringを出力型:String(開始位置から指定文字数まで)に変換する.
     * 値がない場合はnullを返却
     *
     * @param value 文字列
     * @param start 開始位置
     * @param length 桁数(文字列長)
     * @return 桁数制限した文字列
     */
    public String generateSubString(final String value, final int start, final int length) {
        if (value == null) {
            return null;
        } else {
            return value.substring(start, start + length);
        }
    }
}
