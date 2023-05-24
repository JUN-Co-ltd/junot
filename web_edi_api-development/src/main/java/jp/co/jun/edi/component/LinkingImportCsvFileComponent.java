package jp.co.jun.edi.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.constants.CharsetsConstants;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;

/**
 * CSVファイルをダウンロードし、データを読み込むコンポーネント.
 * @param <T>
 */
@Component
public abstract class LinkingImportCsvFileComponent<T> extends GenericComponent {

    /** CSV設定. */
    private static final CSVFormat CSV_FORMAT =
            CSVFormat.DEFAULT // デフォルトのCSV形式を指定
            .withIgnoreEmptyLines(false) // 空行を無視する
            .withIgnoreSurroundingSpaces(true) // 値をtrimして取得する
            .withRecordSeparator("\r\n") // 改行コードCRLF
            .withDelimiter(',') // 区切りカンマ
            .withEscape('"') // エスケープ文字ダブルクォート
            .withQuoteMode(QuoteMode.NONE); // 囲み文字なし

    @Autowired
    private S3Component s3Component;

    /**
     * CSVデータをModelに詰め替え.
     *
     * @param csvRecord CSVレコード(1行)
     * @return 詰め替え後のModel
     */
    abstract T convertCsvDataToModel(CSVRecord csvRecord);

    /**
     * @return 一時保存ディレクトリ
     */
    abstract String generateTempDirectory();

    /**
     * CSVデータを読み込む.
     * CSVファイルフォーマット、文字コードはデフォルトを使用
     *
     * @param file CSVファイル
     * @return データ取込用CSVファイルModelリスト
     * @throws Exception 例外
     */
    public List<T> readCsvData(final File file) throws Exception {
        return readCsvData(file, CSV_FORMAT, CharsetsConstants.MS932);
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
    public List<T> readCsvData(final File file, final CSVFormat format, final Charset charSet) throws Exception {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), charSet));
                CSVParser parser = format.parse(br);) {
            // CSVデータをモデルに変換する
            final List<T> list = parser.getRecords().stream()
                    .map(csvRecord -> convertCsvDataToModel(csvRecord)).collect(Collectors.toList());
            return list;
        }
    }

    /**
     * S3からCSVファイルをダウンロードし、一時保存ディレクトリに保存する.
     *
     * @param wmsLinkingFileEntity 倉庫連携ファイル情報
     * @return ダウンロードしたCSVファイル
     * @throws Exception IOException
     */
    public File downloadCsvFile(final TWmsLinkingFileEntity wmsLinkingFileEntity) throws Exception {
        // CSVファイル一時保存パス取得
        final String tmpDirectory = generateTempDirectory();
        final Path csvPath = Paths.get(tmpDirectory);

        // CSVファイルをダウンロード
        final File confirmFile = new File(csvPath.toFile(), wmsLinkingFileEntity.getFileName());
        final String key = wmsLinkingFileEntity.getS3Prefix() + "/" + wmsLinkingFileEntity.getS3Key();
        s3Component.downloadFileToTmpDirectory(key, confirmFile);

        return confirmFile;
    }

    /**
     * データファイルを削除する.
     * 一時保存ディレクトリ内のデータを全て削除
     *
     * @throws Exception IOException
     */
    public void deleteFiles() throws Exception {
        final String tmpDirectory = generateTempDirectory();
        final File instructionDir = new File(tmpDirectory);
        FileUtils.cleanDirectory(instructionDir);
    }
}
