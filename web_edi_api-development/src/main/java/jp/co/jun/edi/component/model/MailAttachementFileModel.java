package jp.co.jun.edi.component.model;

import java.io.File;

import lombok.Data;
/**
 *
 * MailAttachementFileModel.
 *
 */
@Data
public class MailAttachementFileModel {
    /** 添付ファイル. */
    private File file;
    /** ファイル名. */
    private String fileName;
}
