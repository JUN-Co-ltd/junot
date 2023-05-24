package jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.groups.Default;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import jp.co.jun.edi.constants.RegexpConstants;
import jp.co.jun.edi.model.CompositionModel;
import jp.co.jun.edi.util.xlsx.XlsxSheetReader;
import lombok.Data;

/**
 * 品番・商品一括登録用の組成のシート情報.
 */
@Data
public class CompositionSheet implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_CODE_PREFIX = "{jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet.CompositionSheet";

    /** シート名. */
    private static final String SHEET_NAME = "組成取込用";

    /** 開始行番号. */
    private static final int START_ROW_INDEX = 5;

    /** 行番号. */
    private int rowIndex;

    /** 「品番」の列番号. */
    private static final int PART_NO_COLNUM_INDEX = 0;

    /** 「品番」. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".partNo.NotBlank}", groups = { Default.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".partNo.Pattern}", regexp = RegexpConstants.PART_NO, groups = { Default.class })
    private String partNo;

    /** 「カラー」の列番号. */
    private static final int COLOR_CODE_COLNUM_INDEX = 1;

    /** 「カラー」. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".colorCode.NotBlank}", groups = { Default.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".colorCode.Pattern}", regexp = RegexpConstants.COLOR_CODE_00, groups = { Default.class })
    private String colorCode;

    /** 「部位」の列番号. */
    private static final int PARTS_CODE_COLNUM_INDEX = 2;

    /** 「部位」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".partsCode.Pattern}", regexp = RegexpConstants.PARTS_CODE, groups = { Default.class })
    private String partsCode;

    /** 「素材」の列番号. */
    private static final int COMPOSITION_CODE_COLNUM_INDEX = 3;

    /** 「素材」. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".compositionCode.NotBlank}", groups = { Default.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".compositionCode.Pattern}", regexp = RegexpConstants.COMPOSITION_CODE, groups = { Default.class })
    private String compositionCode;

    /** 「混率」の列番号. */
    private static final int PERCENT_COLNUM_INDEX = 4;

    /** 「混率」. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".percent.NotBlank}", groups = { Default.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".percent.Pattern}", regexp = RegexpConstants.PERCENT, groups = { Default.class })
    private String percent;

    /**
     * 「組成取込用」シートのリーダーを返却する.
     *
     * @param workbook {@link Workbook} instance.
     * @return レコードのリスト.
     */
    public static XlsxSheetReader<String, CompositionSheet> getReader(
            final Workbook workbook) {
        return new XlsxSheetReader<String, CompositionSheet>(workbook) {
            @Override
            protected String getKey(final Row row) {
                return getFormatStringValue(row.getCell(PART_NO_COLNUM_INDEX));
            }

            @Override
            protected CompositionSheet getRow(final Row row) {
                final CompositionSheet sheet = new CompositionSheet();

                sheet.setRowIndex(row.getRowNum());
                sheet.setPartNo(getFormatStringValue(row.getCell(PART_NO_COLNUM_INDEX)));
                sheet.setColorCode(getFormatStringValue(row.getCell(COLOR_CODE_COLNUM_INDEX)));
                sheet.setPartsCode(getFormatStringValue(row.getCell(PARTS_CODE_COLNUM_INDEX)));
                sheet.setCompositionCode(getFormatStringValue(row.getCell(COMPOSITION_CODE_COLNUM_INDEX)));
                sheet.setPercent(getFormatStringValue(row.getCell(PERCENT_COLNUM_INDEX)));

                return sheet;
            }
        }.sheetName(SHEET_NAME).startRowIndex(START_ROW_INDEX);
    }

    /**
     * {@link CompositionModel} に変換する.
     *
     * @param row {@link CompositionSheet} instance.
     * @return {@link CompositionModel} instance.
     */
    public static CompositionModel toComposition(
            final CompositionSheet row) {
        final CompositionModel model = new CompositionModel();

        model.setPartNo(row.getPartNo());
        model.setColorCode(row.getColorCode());
        model.setPartsCode(row.getPartsCode());
        model.setCompositionCode(row.getCompositionCode());
        model.setPercent(Integer.parseInt(row.getPercent()));

        return model;
    }
}
