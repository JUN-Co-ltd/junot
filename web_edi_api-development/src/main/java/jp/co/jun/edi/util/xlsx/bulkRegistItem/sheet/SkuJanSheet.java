package jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.groups.Default;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import jp.co.jun.edi.constants.RegexpConstants;
import jp.co.jun.edi.model.ExternalSkuModel;
import jp.co.jun.edi.model.SkuModel;
import jp.co.jun.edi.util.xlsx.XlsxSheetReader;
import lombok.Data;

/**
 * 品番・商品一括登録用のSKU(JAN)のシート情報.
 */
@Data
public class SkuJanSheet implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_CODE_PREFIX = "{jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet.SkuJanSheet";

    /** シート名. */
    private static final String SHEET_NAME = "SKU取込用(JAN)";

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
    @Pattern(message = MESSAGE_CODE_PREFIX + ".colorCode.Pattern}", regexp = RegexpConstants.COLOR_CODE, groups = { Default.class })
    private String colorCode;

    /** 「サイズ」の列番号. */
    private static final int SIZE_COLNUM_INDEX = 2;

    /** 「サイズ」. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".size.NotBlank}", groups = { Default.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".size.Pattern}", regexp = RegexpConstants.SIZE, groups = { Default.class })
    private String size;

    /** 「JAN」の列番号. */
    private static final int JAN_CODE_COLNUM_INDEX = 3;

    /** 「JAN」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".janCode.Pattern}", regexp = RegexpConstants.JAN_CODE, groups = { Default.class })
    private String janCode;

    /** 「他社品番」の列番号. */
    private static final int EXTERNAL_PART_NO_COLNUM_INDEX = 4;

    /** 「他社品番」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".externalPartNo.Pattern}", regexp = RegexpConstants.EXTERNAL_PART_NO, groups = { Default.class })
    private String externalPartNo;

    /** 「他社カラー」の列番号. */
    private static final int EXTERNAL_COLOR_CODE_COLNUM_INDEX = 5;

    /** 「他社カラー」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".externalColorCode.Pattern}", regexp = RegexpConstants.EXTERNAL_COLOR_CODE, groups = { Default.class })
    private String externalColorCode;

    /** 「他社サイズ」の列番号. */
    private static final int EXTERNAL_SIZE_COLNUM_INDEX = 6;

    /** 「他社サイズ」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".externalSize.Pattern}", regexp = RegexpConstants.EXTERNAL_SIZE, groups = { Default.class })
    private String externalSize;

    /**
     * シートのリーダーを返却する.
     *
     * @param workbook {@link Workbook} instance.
     * @return レコードのリスト.
     */
    public static XlsxSheetReader<String, SkuJanSheet> getReader(
            final Workbook workbook) {
        return new XlsxSheetReader<String, SkuJanSheet>(workbook) {
            @Override
            protected String getKey(final Row row) {
                return getFormatStringValue(row.getCell(PART_NO_COLNUM_INDEX));
            }

            @Override
            protected SkuJanSheet getRow(final Row row) {
                final SkuJanSheet sheet = new SkuJanSheet();

                sheet.setRowIndex(row.getRowNum());
                sheet.setPartNo(getFormatStringValue(row.getCell(PART_NO_COLNUM_INDEX)));
                sheet.setColorCode(getFormatStringValue(row.getCell(COLOR_CODE_COLNUM_INDEX)));
                sheet.setSize(getFormatStringValue(row.getCell(SIZE_COLNUM_INDEX)));
                sheet.setJanCode(getFormatStringValue(row.getCell(JAN_CODE_COLNUM_INDEX)));
                sheet.setExternalPartNo(getFormatStringValue(row.getCell(EXTERNAL_PART_NO_COLNUM_INDEX)));
                sheet.setExternalColorCode(getFormatStringValue(row.getCell(EXTERNAL_COLOR_CODE_COLNUM_INDEX)));
                sheet.setExternalSize(getFormatStringValue(row.getCell(EXTERNAL_SIZE_COLNUM_INDEX)));

                return sheet;
            }
        }
        .sheetName(SHEET_NAME)
        .startRowIndex(START_ROW_INDEX);
    }

    /**
     * {@link SkuModel} に変換する.
     *
     * @param row {@link SkuJanSheet} instance.
     * @return {@link SkuModel} instance.
     */
    public static SkuModel toSku(
            final SkuJanSheet row) {
        final SkuModel model = new SkuModel();

        model.setPartNo(row.getPartNo());
        model.setColorCode(row.getColorCode());
        model.setJanCode(row.getJanCode());
        model.setSize(row.getSize());

        if (StringUtils.isNotEmpty(row.getExternalPartNo())
                || StringUtils.isNotEmpty(row.getExternalColorCode())
                || StringUtils.isNotEmpty(row.getExternalSize())) {
            final ExternalSkuModel externalSkuModel = new ExternalSkuModel();
            externalSkuModel.setExternalPartNo(row.getExternalPartNo());
            externalSkuModel.setExternalColorCode(row.getExternalColorCode());
            externalSkuModel.setExternalSize(row.getExternalSize());
            model.setExternalSku(externalSkuModel);
        }

        return model;
    }
}
