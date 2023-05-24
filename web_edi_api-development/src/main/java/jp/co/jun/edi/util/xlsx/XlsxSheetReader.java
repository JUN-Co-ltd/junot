package jp.co.jun.edi.util.xlsx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Excelシートのリーダー.
 * @param <K> キー
 * @param <R> 行
 */
public abstract class XlsxSheetReader<K, R> {
    private final Workbook workbook;
    private final DataFormatter formatter = new DataFormatter();

    /** 最大レコードサイズ. */
    private Integer maxSize;

    /** リストの初期化サイズ. */
    private Integer initialCapacity;

    /** シート名. */
    private String sheetName;

    /** シート. */
    private Sheet sheet;

    /** 行番号. */
    private int rowIndex = -1;

    /** 最終行番号. */
    private int lastRowIndex = 0;

    /**
     * @param workbook {@link Workbook} instance.
     */
    public XlsxSheetReader(final Workbook workbook) {
        this.workbook = workbook;
    }

    /**
     * @param maxSize 最大レコードサイズ.
     * @return {@link XlsxSheetReader}
     */
    public XlsxSheetReader<K, R> maxSize(final int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    /**
     * @param initialCapacity リストの初期化サイズ
     * @return {@link XlsxSheetReader}
     */
    public XlsxSheetReader<K, R> initialCapacity(final int initialCapacity) {
        this.initialCapacity = initialCapacity;
        return this;
    }

    /**
     * @param startRowIndex 開始行番号
     * @return {@link XlsxSheetReader}
     */
    public XlsxSheetReader<K, R> startRowIndex(final int startRowIndex) {
        this.rowIndex = startRowIndex - 1;
        return this;
    }

    /**
     * @param sheetName シート名
     * @return {@link XlsxSheetReader}
     */
    public XlsxSheetReader<K, R> sheetName(final String sheetName) {
        this.sheetName = sheetName;
        this.sheet = this.workbook.getSheet(sheetName);
        return this;
    }

    /**
     * シート名を取得する.
     *
     * @return シート名
     */
    public String getSheetName() {
        return this.sheetName;
    }

    /**
     * 最大レコード数を取得する.
     *
     * @return 最大レコード数
     */
    public Integer getMaxSize() {
        return this.maxSize;
    }

    /**
     * 次のレコードがある場合に true を返す.
     *
     * @return 次のレコードの有無.
     */
    public boolean hasNext() {
        if (sheet == null) {
            // 対象シートなし
            return false;
        }

        // 次の行を取得
        final Row row = sheet.getRow(rowIndex);

        if (row == null) {
            // 対象行なし
            return false;
        }

        if (StringUtils.isEmpty(getKey(row))) {
            // キーが空
            return false;
        }

        return true;
    }

    /**
     * レコードを読み込み、リストで返却する.
     *
     * @return レコードのリスト.
     */
    public List<R> read() {
        if (sheet == null) {
            // 対象シートなし
            return Collections.emptyList();
        }

        setLastRowIndex();

        final List<R> rows = newList();

        Row row;

        while ((row = nextRow()) != null) {
            rows.add(getRow(row));
        }

        return rows;
    }

    /**
     * キーを取得する.
     *
     * @param row {@link Row} instance.
     * @return キー
     */
    protected abstract String getKey(Row row);

    /**
     * 行を読み込む.
     *
     * @param row {@link Row} instance.
     * @return シート情報
     */
    protected abstract R getRow(Row row);

    /**
     * 書式の形式に変換した文字列を取得する.
     * <p>
     * 値をトリムし、トリム後の値が空文字の場合は、nullを返却する.
     * </p>
     *
     * @param cell {@link Cell} instance.
     * @return セルの値
     */
    protected String getFormatStringValue(final Cell cell) {
        if (cell == null) {
            return null;
        }

        return StringUtils.trimToNull(this.formatter.formatCellValue(cell));
    }

    /**
     * 指定した初期化サイズで生成した空のリストを生成する.
     * 指定がない場合、サイズ指定なしで空のリストを生成する.
     *
     * @return リスト
     */
    private List<R> newList() {
        if (this.initialCapacity == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(this.initialCapacity.intValue());
        }
    }

    /**
     * 最終行を設定する.
     */
    private void setLastRowIndex() {
        if (sheet == null) {
            return;
        }

        if (maxSize == null) {
            lastRowIndex = sheet.getLastRowNum();
        } else {
            lastRowIndex = rowIndex + maxSize.intValue();
        }
    }

    /**
     * 次の行を取得する.
     *
     * @return 行番号で指定された行。最終行番号を超えている場合や、キーが存在しない場合は、nullを返却する。
     */
    private Row nextRow() {
        rowIndex++;

        if (rowIndex > lastRowIndex) {
            // 最終行番号を超えている
            return null;
        }

        if (sheet == null) {
            return null;
        }

        final Row row = sheet.getRow(rowIndex);

        if (row == null) {
            // 対象行なし
            return null;
        }

        if (StringUtils.isEmpty(getKey(row))) {
            // キーが空
            return null;
        }

        return row;
    }
}
