package jp.co.jun.edi.model;

import java.util.LinkedHashMap;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * マルチパートファイルのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class MultipartFileModel extends GenericModel {
    private static final long serialVersionUID = 1L;

    /** マルチパートファイル（シリアライズ対象外とする）. */
    private transient MultipartFile file;

    @Override
    public Object getLogObject() {
        return toLogObject(this);
    }

    /**
     * @param file {@link MultipartFile} instance
     * @return {@link MultipartFileModel} instance
     */
    public static MultipartFileModel of(final MultipartFile file) {
        final MultipartFileModel model = new MultipartFileModel();

        model.setFile(file);

        return model;
    }

    /**
     * ログ出力用オブジェクトに変換.
     *
     * @param model {@link MultipartFileModel} instance
     * @return ログ出力用オブジェクト
     */
    private static LinkedHashMap<String, Object> toLogObject(final MultipartFileModel model) {
        return new LinkedHashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("originalFilename", model.file.getOriginalFilename());
            }
        };
    }
}
