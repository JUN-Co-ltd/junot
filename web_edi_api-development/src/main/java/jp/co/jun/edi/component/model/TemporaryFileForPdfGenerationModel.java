package jp.co.jun.edi.component.model;

import java.nio.file.Path;

import lombok.Data;

/**
 * TemporaryFileForPdfGenerationModel.
 */
@Data
public class TemporaryFileForPdfGenerationModel {
    /** 一時XMLファイルパス. */
    private Path xmlFilePath;
    /** 一時PDFファイルパス. */
    private Path pdfFilePath;
    /** 一時フォルダパス. */
    private String temporayFolder;
    /** PDFファイル正式名称. */
    private String fileName;
}
